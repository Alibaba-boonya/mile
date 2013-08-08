/*
 * =====================================================================================
 *
 *       Filename:  hi_hashindex.c
 *
 *    Description:  һ��������ʽ�ļ򵥵�hash�ṹ���Լ������ֵ��ѯ�ӿ�ʵ��
 *
 *        Version:  1.0
 *        Created: 	2011��04��09�� 11ʱ41��55�� 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi, yunliang.shi@alipay.com
 *        Company:  alipay.com
 *
 * =====================================================================================
 */

#include "hash_index.h"


struct hash_index_manager* hash_index_init(struct hash_index_config* config,MEM_POOL* mem_pool)
{
	struct hash_index_manager* hash_index = (struct hash_index_manager*)mem_pool_malloc(mem_pool,sizeof(struct hash_index_manager));   

	assert(hash_index != NULL);
	
	memset(hash_index,0,sizeof(struct hash_index_manager));

	//��ֵ��������
	hash_index->limit = config->row_limit;
	
	//Ͱֱ�Ӻ�row_limitһ��
	hash_index->hashmod = config->row_limit;

	hash_index->is_full = config->is_full;
	pthread_mutex_init(&hash_index->mmap_lock, NULL);

	//��ʼ���ļ�
	sprintf(hash_index->file_name,"%s/hash.idx",config->work_space);

	if( config->is_full || mile_conf.all_mmap ) {
		//mmapӳ�䴦��
		hash_index->mem_mmaped =(struct hash_bucket*)get_mmap_memory(hash_index->file_name,HASH_INDEX_MMAP_SIZE(hash_index)); 
	} else {
		// alloc memory like malloc
		hash_index->mem_mmaped = (struct hash_bucket*)alloc_file_memory(hash_index->file_name,HASH_INDEX_MMAP_SIZE(hash_index));
	}

	assert(hash_index->mem_mmaped != NULL);

	//��ʼ��doclist
	struct doclist_config dconfig;
	dconfig.row_limit = hash_index->limit;
	dconfig.is_full = config->is_full;
	strcpy(dconfig.work_space,config->work_space);
	hash_index->doclist = doclist_init(&dconfig, mem_pool);
	
	assert(hash_index->doclist != NULL);
	
	return hash_index;
}


int32_t hash_index_recover(struct hash_index_manager* hash_index, uint32_t docid)
{
	//�ж��Ƿ���Ҫ�ָ���������ݰ汾���ã�����Ҫ�ָ�
	if(hash_index->doclist->version != DATA_STOAGE_VERSION)
	{
		log_warn("���ݰ汾���� %"PRIu32, hash_index->doclist->version);
		return 0;
	}

	
	//���doclist��docid���������
	memset(hash_index->doclist->mem_mmaped + sizeof(uint32_t) + docid * sizeof(struct doc_row_unit), 0, (hash_index->doclist->row_limit - docid) * sizeof(struct doc_row_unit));

	//���hash�������д���docid��ֵ�� offset
	struct hash_bucket* hbucket = NULL;
	uint32_t max_offset = sizeof(struct doc_row_unit)*docid +sizeof(uint32_t);
	uint32_t i;
	for(i=0; i<hash_index->hashmod + 1; i++)
	{
		hbucket = hash_index->mem_mmaped + i;
		if(hbucket->offset >= max_offset )
			hbucket->offset = 0;
	}

	if(docid > 0) {
		struct doc_row_unit* doc = NULL;
		uint32_t offset = 0;
		uint32_t bucket_no = 0;
		for(i = docid - 1; ; i--)
		{	
			doc = GET_DOC_ROW_STRUCT(hash_index->doclist, i);

			if(doc->doc_id == 0 && doc->next == 0)
			{
				if(i == 0)
					break;
				else
					continue;
			}	

			while(!(doc->next & 0x80000000))
			{
				doc = NEXT_DOC_ROW_STRUCT(hash_index->doclist, doc->next);
			}
			
			bucket_no = doc->next & 0x7fffffff;
			hbucket = hash_index->mem_mmaped + bucket_no;
			offset = i *sizeof(struct doc_row_unit) + sizeof(uint32_t);
			if(hbucket->offset < offset)
				hbucket->offset = offset;
			
			if(i == 0)
				break;
		}
	}

	// clear hash_value if doclist is empty.
	for(i=0; i<hash_index->hashmod + 1; i++)
	{
		hbucket = hash_index->mem_mmaped + i;
		if(hbucket->offset == 0)
			hbucket->hash_value = 0;
	}

	return 0;
}




//�������
//1.��ֵ���
//��ֱ�Ӷ�λ����hashmod��Ͱ
//2.�ǿ�ֵ���
//����ݲ����ֵ��hash��ͨ��hashֵ��hashmodȡģ���Ӷ���λ���洢���ĸ�Ͱ�ϣ�������Ͱ��hash value�����ֱ�Ӳ��룬���
//���ȣ���˵����ͻ�ˣ�����Ҫ��������ҵ��Լ���Ͱ
int32_t hash_index_insert(struct hash_index_manager* hash_index,struct low_data_struct* data,uint32_t docid)
{
	struct hash_bucket* bucket;
	uint64_t hash_value;
	uint32_t loc;
	uint32_t i;
	uint32_t offset;

	//����value��һ��hash
	PROFILER_BEGIN("get hash value");
	hash_value = get_hash_value(data);
	PROFILER_END();
	
	//�ж��Ƿ�Ϊ��ֵ
	if(data->len == 0)
	{
		bucket = hash_index->mem_mmaped+hash_index->hashmod;

		//��ϣ���λ��û�����ֵ���ڣ�����doclist�ӿڲ���
		if(bucket->hash_value == 0)
		{
			if((offset = doclist_insert(hash_index->doclist,docid,0, hash_index->hashmod)) == 0)
				return ERROR_INSERT_REPEAT;
			
			//ע��hash_valueһ��Ҫ��offset֮��д����Ϊ������ѯ��ʱ�����ȥ��value���ж�value�Ƿ�Ϊ0
			Mile_AtomicSetPtr(&bucket->offset, offset);
			Mile_AtomicSetPtr(&bucket->hash_value, hash_value);
		}
		else
		{
			offset = doclist_insert(hash_index->doclist,docid,bucket->offset, hash_index->hashmod);
			Mile_AtomicSetPtr(&bucket->offset, offset);
		}
		return MILE_RETURN_SUCCESS;
	}
	
	
	//ȡģ
	loc = hash_value%hash_index->hashmod;
	i = loc;

	do
	{
		bucket = hash_index->mem_mmaped+i;

		//��ϣ���λ��û�����ֵ���ڣ�����doclist�ӿڲ���
		if(bucket->hash_value == 0)
		{
			if((offset = doclist_insert(hash_index->doclist,docid,0, i)) == 0 )
				return ERROR_INSERT_REPEAT;
			//ע��hash_valueһ��Ҫ��offset֮��д����Ϊ������ѯ��ʱ�����ȥ��value���ж�value�Ƿ�Ϊ0
			Mile_AtomicSetPtr(&bucket->offset, offset);
			Mile_AtomicSetPtr(&bucket->hash_value, hash_value);
			return MILE_RETURN_SUCCESS;
		}

		//��ϣ���λ����ֵ���ڣ��������
		if(bucket->hash_value == hash_value)
		{
			if((offset = doclist_insert(hash_index->doclist,docid,bucket->offset, i)) == 0)
				return ERROR_INSERT_REPEAT;
			Mile_AtomicSetPtr(&bucket->offset, offset);
			return MILE_RETURN_SUCCESS;
		}
		
		i = (i+1)%hash_index->hashmod;
	}
	while(i!=loc);

	log_error("hash ���Գ�ͻ");
	return ERROR_HASH_CONFLICT;
}


struct rowid_list* get_rowid_list(struct hash_index_manager* hash_index,struct doc_row_unit* doc_row,MEM_POOL* mem_pool)
{
	struct rowid_list* rowids = rowid_list_init(mem_pool);

	//��������doc row�б�
	while(doc_row != NULL)
	{
		rowid_list_add(mem_pool,rowids,doc_row->doc_id);
		if((doc_row->next & 0x80000000) || (doc_row->next == 0))
			break;
		doc_row = NEXT_DOC_ROW_STRUCT(hash_index->doclist,doc_row->next);
	}
	return rowids;
}



//Ҳ�Ƿ��������
//1.��ֵ ֱ�ӷ���Ͱ�������doclist
//2.�ǿ� ���dataȡhashֵ���ҵ��Լ���Ͱ�������Ͱû��ֵ����˵�������ڣ�������ڣ�����ȣ��򷵻ظ�Ͱ�µ�����doclist���������������
//һ����ͰΪ�գ���˵�����ֵһ�������ڣ�Ҳ����Ҫ����������
struct rowid_list* hash_index_query(struct hash_index_manager* hash_index,struct low_data_struct* data,MEM_POOL* mem_pool)
{
	struct hash_bucket* bucket;
	struct rowid_list* ret;
	uint64_t hash_value;
	uint64_t hash_value_in_hash_info;
	uint32_t loc;
	uint32_t offset;
	uint32_t i;

	//���Ϊ��ֵ�Ļ�����ѵ�hashmod��Ͱ���ظ��ϲ�
	if(data->len == 0)
	{
		bucket = hash_index->mem_mmaped+hash_index->hashmod;
		
		hash_value_in_hash_info = Mile_AtomicGetPtr(&bucket->hash_value);
		/*ֻҪΪ�գ���϶�������*/
		if(hash_value_in_hash_info == 0)
		{
			return NULL;
		}

		offset = Mile_AtomicGetPtr(&bucket->offset);
		ret = get_rowid_list(hash_index,NEXT_DOC_ROW_STRUCT(hash_index->doclist, offset),mem_pool);
		return ret;
	}
	
	
	//����value��һ��hash
	PROFILER_BEGIN("get hash value");
	hash_value = get_hash_value(data);
	PROFILER_END();

	//ȡģ
	loc = hash_value%hash_index->hashmod;

	//�����λ����hash��ֵ������Ҫ����Ѱ��
	i=loc;
	do
	{
		bucket = hash_index->mem_mmaped+i;

		hash_value_in_hash_info = Mile_AtomicGetPtr(&bucket->hash_value);
		/*���ѭ���Ĺ����У�ֻҪ��һ��Ϊ�գ���϶�������*/
		if(hash_value_in_hash_info == 0)
		{
			return NULL;
		}
		
		//�ҳ�hashֵ��ȵط�
		if(hash_value_in_hash_info == hash_value)
		{
		   offset = Mile_AtomicGetPtr(&bucket->offset);
		   ret = get_rowid_list(hash_index,NEXT_DOC_ROW_STRUCT(hash_index->doclist, offset),mem_pool);
		   return ret;
		}
		
		i = (i+1)%hash_index->hashmod;
	}
	while(i!=loc);

	log_debug("��ѯ�������ֵ");
	return NULL;
}




void hash_index_destroy(struct hash_index_manager* hash_index)
{
	if(hash_index->mem_mmaped != NULL)
	{
        munmap(hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index));
	}

	//�ͷ�doclist�ṹ
	doclist_destroy(hash_index->doclist);

	//ɾ����Ӧ���ļ�������
	remove(hash_index->file_name);
	return;
}


void hash_index_release(struct hash_index_manager* hash_index)
{
	// double checked locking
	if(hash_index->is_full || mile_conf.all_mmap) {
		doclist_release(hash_index->doclist);
		if(hash_index->mem_mmaped != NULL)
			msync(hash_index->mem_mmaped,HASH_INDEX_MMAP_SIZE(hash_index),MS_SYNC); // make sure synced

	} else {
		pthread_mutex_lock( &hash_index->mmap_lock );

		doclist_release(hash_index->doclist);

		if(NULL != hash_index->mem_mmaped) {
			if( hash_index->is_full )
				msync(hash_index->mem_mmaped,HASH_INDEX_MMAP_SIZE(hash_index),MS_SYNC); // make sure synced
			else
				flush_memory(hash_index->file_name, hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index), 0); // no disk write limit
		}

		pthread_mutex_unlock( &hash_index->mmap_lock );
	}

	munmap(hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index));
	return;
}



void hash_index_checkpoint(struct hash_index_manager* hash_index)
{
	// double checked locking
	if( hash_index->is_full || mile_conf.all_mmap ) {
		log_debug("mmap hash index checkpoint");

		doclist_checkpoint(hash_index->doclist);
		if(hash_index->mem_mmaped != NULL) 
			msync(hash_index->mem_mmaped,HASH_INDEX_MMAP_SIZE(hash_index),MS_SYNC); // make sure synced

	} else {
		log_debug("malloc hash index checkpoint");
		pthread_mutex_lock( &hash_index->mmap_lock );

		doclist_checkpoint(hash_index->doclist);

		if(NULL != hash_index->mem_mmaped) {
			if( hash_index->is_full )
				msync(hash_index->mem_mmaped,HASH_INDEX_MMAP_SIZE(hash_index),MS_SYNC); // make sure synced
			else 
				flush_memory(hash_index->file_name, hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index), mile_conf.disk_write_limit);
		}

		pthread_mutex_unlock( &hash_index->mmap_lock );
	}
	
	return;
}

int hash_index_mmap_switch(struct hash_index_manager *hash_index)
{
	if( !hash_index->is_full && !mile_conf.all_mmap ) {
		pthread_mutex_lock( &hash_index->mmap_lock );
		// flush memory to disk
		if( flush_memory(hash_index->file_name, hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index), mile_conf.disk_write_limit) != 0 ) {
			pthread_mutex_unlock( &hash_index->mmap_lock );
			return ERROR_MMAP_SWITCH;
		}

		// switch mmaped file to hash index file
		if( switch_mmaped_file(hash_index->file_name, hash_index->mem_mmaped, HASH_INDEX_MMAP_SIZE(hash_index)) != 0 ) {
			pthread_mutex_unlock( &hash_index->mmap_lock );
			return ERROR_MMAP_SWITCH;
		}
		usleep(MMAP_SWITCH_SLEEP_INTERVAL * 1000);

		int ret = doclist_mmap_switch(hash_index->doclist);
		if( MILE_RETURN_SUCCESS != ret ) {
			pthread_mutex_unlock( &hash_index->mmap_lock );
			return ERROR_MMAP_SWITCH;
		}
		hash_index->is_full = 1;
		pthread_mutex_unlock( &hash_index->mmap_lock );

		usleep(MMAP_SWITCH_SLEEP_INTERVAL * 1000);
	}

	return MILE_RETURN_SUCCESS;
}

