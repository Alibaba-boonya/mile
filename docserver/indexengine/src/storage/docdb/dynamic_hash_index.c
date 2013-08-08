/*
 * =====================================================================================
 *
 *       Filename:  dynamic_hash_index.c
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  2012��09��14�� 14ʱ49��32��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi (zian), yunliang.shi@alipay.com
 *   Organization:  
 *
 * =====================================================================================
 */
#include "dynamic_hash_index.h"
#include <stdio.h>


static int32_t recover(struct dyhash_single_index* single_index, uint32_t docid);
static int32_t insert(struct dyhash_single_index* single_index,struct low_data_struct* data,uint32_t docid);
static struct rowid_list* query(struct dyhash_single_index* single_index,struct low_data_struct* data,MEM_POOL* mem_pool);
static uint32_t count_query(struct dyhash_single_index* single_index, struct low_data_struct* data);
static struct rowid_list* get_rowid_list(struct dyhash_single_index* single_index,struct doc_row_unit* doc_row,MEM_POOL* mem_pool);


struct dyhash_single_index* create_single_index(struct dyhash_signleindex_config* config, MEM_POOL* mem_pool )
{
	char file_name[FILENAME_MAX_LENGTH];
	memset(file_name, 0, sizeof(file_name));	

	//��ʼ���ļ���
	sprintf(file_name, "%s/hash.idx.%"PRIu16, config->work_space, config->index);

	//�ж��ļ��Ƿ����
	if(!config->is_create && access(file_name, F_OK) != 0){
		log_warn("filename: %s not exit", file_name);
		return NULL;
	}	

	//����
	struct dyhash_single_index* single_index = (struct dyhash_single_index*)mem_pool_malloc(mem_pool, sizeof(struct dyhash_single_index));
	memset(single_index, 0, sizeof(struct dyhash_single_index));

	//��ֵ
	strcpy(single_index->file_name, file_name);
	single_index->hashmod = config->hash_mod;
	single_index->limit = config->hash_mod;


	if( config->is_full || mile_conf.all_mmap ) {
		//mmapӳ�䴦��
		single_index->mem_mmaped =(struct dyhash_bucket*)get_mmap_memory(single_index->file_name,DYHASH_INDEX_MMAP_SIZE(single_index)); 
	} else {
		// alloc memory like malloc
		single_index->mem_mmaped = (struct dyhash_bucket*)alloc_file_memory(single_index->file_name,DYHASH_INDEX_MMAP_SIZE(single_index));
	}

	assert(single_index->mem_mmaped != NULL);

	//��ʼ��doclist
	struct doclist_config dconfig;
	

	//doclist��������hashͰ��N��
	dconfig.row_limit = single_index->limit * DOC_MULTIPLE_NUM;
	dconfig.is_full = config->is_full;
	strcpy(dconfig.work_space,config->work_space);
	struct doclist_manager* doclist = doclist_init_v2(&dconfig, config->is_create, config->index, mem_pool);
	
	if(doclist == NULL)
		return NULL;
	
	single_index->doclist = doclist;


	single_index->index = config->index;
	return single_index;
}


struct dyhash_index_manager * dyhash_index_init(struct dyhash_index_config* config, MEM_POOL* mem_pool)
{
	struct dyhash_index_manager* dyhash_index = (struct dyhash_index_manager*)mem_pool_malloc(mem_pool,sizeof(struct dyhash_index_manager));   

	assert(dyhash_index != NULL);

	memset(dyhash_index, 0, sizeof(struct dyhash_index_manager));

	//��ֵ
	dyhash_index->hashmod = config->row_limit;
	dyhash_index->is_full = config->is_full;
	dyhash_index->mem_pool = mem_pool;
	strcpy(dyhash_index->work_space, config->work_space);

	pthread_mutex_init(&dyhash_index->mmap_lock, NULL);

	uint16_t i = 1;
	struct dyhash_signleindex_config sconfig;
	sconfig.hash_mod = dyhash_index->hashmod;
	sconfig.is_full = config->is_full;
	strcpy(sconfig.work_space, config->work_space);	

	struct dyhash_single_index * single_index = NULL;
	do{
		sconfig.index = i;
		if(sconfig.index == 1)
			sconfig.is_create = 1;
		else
			sconfig.is_create = 0;

		single_index = create_single_index(&sconfig, mem_pool);
		
		if(i==1 &&single_index == NULL)
		{
			log_error("hash index init fail %s",config->work_space);
			return NULL;
		}
		
		if(single_index == NULL)
			break;

		if(dyhash_index->head == NULL)
		{
			dyhash_index->head = single_index;
			dyhash_index->tail = dyhash_index->head;
		}
		else
		{
			if(dyhash_index->tail != NULL)
			{
				dyhash_index->tail->next = single_index;	
				dyhash_index->tail = single_index;	
			}
		}

		i++;
	}while(1);

	
	return dyhash_index;
}


int32_t dyhash_index_insert(struct dyhash_index_manager * dyhash_index,struct low_data_struct* data,uint32_t docid)
{
	int32_t ret = 0;
	if((ret = insert(dyhash_index->tail, data, docid)) == 1){
		struct dyhash_signleindex_config sconfig;
		sconfig.hash_mod = dyhash_index->hashmod;
		sconfig.is_full = 0;
		strcpy(sconfig.work_space, dyhash_index->work_space);	

		struct dyhash_single_index * single_index = NULL;
		sconfig.index = dyhash_index->tail->index + 1;
		sconfig.is_create = 1;

		single_index = create_single_index(&sconfig, dyhash_index->mem_pool);
		if(single_index == NULL)
		{
			log_error("%s������������ʧ�� %"PRIu16, dyhash_index->work_space, sconfig.index);
			return -1;
		}
		
		dyhash_index->tail->next = single_index;
		dyhash_index->tail = single_index;
		return insert(dyhash_index->tail, data, docid);
	}	
	
	return ret;
}


struct rowid_list* dyhash_index_query(struct dyhash_index_manager* dyhash_index, struct low_data_struct* data, MEM_POOL* mem_pool)
{
	struct dyhash_single_index* iter = dyhash_index->head; 
	struct rowid_list* result = NULL;
	while(iter != NULL)
	{
		struct rowid_list* temp = query(iter, data, mem_pool);
		result = rowid_union(mem_pool, result, temp);
		iter = iter->next;
	}

	return result;
}


uint32_t dyhash_index_count_query(struct dyhash_index_manager* dyhash_index, struct low_data_struct* data, MEM_POOL* mem_pool)
{
	struct dyhash_single_index* iter = dyhash_index->head; 
	uint32_t result = 0;
	while(iter != NULL)
	{
		result	+= count_query(iter, data);
		iter = iter->next;
	}

	return result;
}

int32_t dyhash_index_recover(struct dyhash_index_manager* dyhash_index, uint32_t docid)
{
	struct dyhash_single_index* iter = dyhash_index->head; 
	int32_t ret = 0;
	while(iter != NULL)
	{
		if(recover(iter, docid) == -1){
			ret = -1;
		}
		iter = iter->next;
	}
	return ret;
}


void dyhash_index_release(struct dyhash_index_manager* dyhash_index)
{
	struct dyhash_single_index* iter = dyhash_index->head;

	// double checked locking
	if(dyhash_index->is_full || mile_conf.all_mmap) {
		
		while(iter != NULL){
			doclist_release(iter->doclist);		

			if(iter->mem_mmaped != NULL){
				msync(iter->mem_mmaped,DYHASH_INDEX_MMAP_SIZE(iter),MS_SYNC); // make sure synced
			}	
		
			munmap(iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter));
			iter = iter->next;
		}
	} else {
		pthread_mutex_lock( &dyhash_index->mmap_lock );

		while(iter != NULL){
			doclist_release(iter->doclist);

			if(NULL != iter->mem_mmaped) {
				if( dyhash_index->is_full )
					msync(iter->mem_mmaped,DYHASH_INDEX_MMAP_SIZE(iter),MS_SYNC); // make sure synced
				else
					flush_memory(iter->file_name, iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter), 0); // no disk write limit
			}

			munmap(iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter));
			iter = iter->next;
		}

		pthread_mutex_unlock( &dyhash_index->mmap_lock );
	}

	return;
}

void dyhash_index_checkpoint(struct dyhash_index_manager* dyhash_index)
{
	struct dyhash_single_index* iter = dyhash_index->head;

	// double checked locking
	if(dyhash_index->is_full || mile_conf.all_mmap) {
		
		while(iter != NULL){
			doclist_checkpoint(iter->doclist);		

			if(iter->mem_mmaped != NULL){
				msync(iter->mem_mmaped,DYHASH_INDEX_MMAP_SIZE(iter),MS_SYNC); // make sure synced
			}			

			iter = iter->next;
		}
	} else {
		pthread_mutex_lock( &dyhash_index->mmap_lock );

		while(iter != NULL){
			doclist_checkpoint(iter->doclist);

			if(NULL != iter->mem_mmaped) {
				if( dyhash_index->is_full )
					msync(iter->mem_mmaped,DYHASH_INDEX_MMAP_SIZE(iter),MS_SYNC); // make sure synced
				else
					flush_memory(iter->file_name, iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter), 0); // no disk write limit
			}

			iter = iter->next;
		}

		pthread_mutex_unlock( &dyhash_index->mmap_lock );
	}

	return;
}


int dyhash_index_mmap_switch(struct dyhash_index_manager *dyhash_index)
{
	struct dyhash_single_index* iter = dyhash_index->head;

	if( !dyhash_index->is_full && !mile_conf.all_mmap ) {
		pthread_mutex_lock( &dyhash_index->mmap_lock );

		while(iter != NULL){
			// flush memory to disk
			if( flush_memory(iter->file_name, iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter), mile_conf.disk_write_limit) != 0 ) {
				pthread_mutex_unlock( &dyhash_index->mmap_lock );
				return ERROR_MMAP_SWITCH;
			}

			// switch mmaped file to hash index file
			if( switch_mmaped_file(iter->file_name, iter->mem_mmaped, DYHASH_INDEX_MMAP_SIZE(iter)) != 0 ) {
				pthread_mutex_unlock( &dyhash_index->mmap_lock );
				return ERROR_MMAP_SWITCH;
			}
			usleep(MMAP_SWITCH_SLEEP_INTERVAL * 1000);

			int ret = doclist_mmap_switch(iter->doclist);
			if( MILE_RETURN_SUCCESS != ret ) {
				pthread_mutex_unlock( &dyhash_index->mmap_lock );
				return ERROR_MMAP_SWITCH;
			}
			dyhash_index->is_full = 1;

			iter = iter->next;
		}



		pthread_mutex_unlock( &dyhash_index->mmap_lock );

		usleep(MMAP_SWITCH_SLEEP_INTERVAL * 1000);
	}

	return MILE_RETURN_SUCCESS;
}

//docid��������
static int32_t recover(struct dyhash_single_index* single_index, uint32_t docid)
{
	//һ��docid����doclist���ڶ�����ҵ����һ���Ǹ�docid��ƫ����
	struct doc_row_unit* doc = NULL;
	struct doclist_manager* doclist = single_index->doclist;

	uint32_t i;
	for(i=0; i<doclist->row_limit; i++){
		doc = GET_DOC_ROW_STRUCT(doclist, i);

		//������ڵ��ڣ�����������
		if(doc->doc_id >= docid)
			break;
	}	

	if(i == doclist->row_limit){
		log_warn("���ûָ�");
		return 0;
	}

	//����offset���Լ�֮������ݶ������
	uint32_t del_docid = i;

	//����ƫ����
	*doclist->cur_offset = del_docid;

	//���doclist��docid���������
	memset(doclist->mem_mmaped + sizeof(uint32_t) + del_docid * sizeof(struct doc_row_unit), 0, (single_index->doclist->row_limit - del_docid) * sizeof(struct doc_row_unit));


	//���hash�������д���del_docid��ֵ�� offset
	struct dyhash_bucket* hbucket = NULL;
	uint32_t max_offset = sizeof(struct doc_row_unit)*del_docid +sizeof(uint32_t);
	for(i=0; i<single_index->hashmod; i++)
	{
		hbucket = single_index->mem_mmaped + i;
		if(hbucket->offset >= max_offset )
			hbucket->offset = 0;
	}

	if(del_docid > 0) {
		struct doc_row_unit* doc = NULL;
		uint32_t offset = 0;
		uint32_t bucket_no = 0;
		for(i = del_docid - 1; ; i--)
		{
			uint32_t cnt = 0;	
			doc = GET_DOC_ROW_STRUCT(single_index->doclist, i);

			if(doc->doc_id == 0 && doc->next == 0)
			{
				if(i == 0)
					break;
				else
					continue;
			}	

			while(!(doc->next & 0x80000000))
			{
				cnt++;
				doc = NEXT_DOC_ROW_STRUCT(single_index->doclist, doc->next);
			}

			cnt++;
		
			bucket_no = doc->next & 0x7fffffff;
			hbucket = single_index->mem_mmaped + bucket_no;
			offset = i *sizeof(struct doc_row_unit) + sizeof(uint32_t);
			if(hbucket->offset < offset){
				hbucket->count = cnt;
				hbucket->offset = offset;
			}
			
			if(i == 0)
				break;
		}
	}

	// clear hash_value if doclist is empty.
	for(i=0; i<single_index->hashmod; i++)
	{
		hbucket = single_index->mem_mmaped + i;
		if(hbucket->offset == 0)
		{
			hbucket->hash_value = 0;
			hbucket->count = 0;
		}	
	}

	return 0;


}

static uint32_t count_query(struct dyhash_single_index* single_index,struct low_data_struct* data)
{
	struct dyhash_bucket* bucket;
	uint64_t hash_value;
	uint64_t hash_value_in_hash_info;
	uint32_t loc;
	uint32_t i;

	if(data->len == 0){
		log_warn("�ִ�Ϊ��");
		return 0;
	}	

	//����value��һ��hash
	PROFILER_BEGIN("get hash value");
	hash_value = get_hash_value(data);
	PROFILER_END();

	int count = 0;

	//ȡģ
	loc = hash_value % single_index->hashmod;

	//�����λ����hash��ֵ������Ҫ����Ѱ��
	i=loc;
	do
	{
		bucket = single_index->mem_mmaped + i;

		hash_value_in_hash_info = Mile_AtomicGetPtr(&bucket->hash_value);
		/*���ѭ���Ĺ����У�ֻҪ��һ��Ϊ�գ���϶�������*/
		if(hash_value_in_hash_info == 0)
		{
			return 0;
		}
		
		//�ҳ�hashֵ��ȵط�
		if(hash_value_in_hash_info == hash_value)
		{
		   return bucket->count;
		}
		
		i = (i+1) % single_index->hashmod;
				
		if(count++ >= DOC_MULTIPLE_NUM){
			log_warn("��ͻ���� %u", count);
			break;
		}
		
	}
	while(i!=loc);

	log_debug("��ѯ�������ֵ");
	return 0;
}


static struct rowid_list* get_rowid_list(struct dyhash_single_index* single_index,struct doc_row_unit* doc_row,MEM_POOL* mem_pool)
{
	struct rowid_list* rowids = rowid_list_init(mem_pool);

	//��������doc row�б�
	while(doc_row != NULL)
	{
		rowid_list_add(mem_pool,rowids,doc_row->doc_id);
		if((doc_row->next & 0x80000000) || (doc_row->next == 0))
			break;
		doc_row = NEXT_DOC_ROW_STRUCT(single_index->doclist,doc_row->next);
	}
	return rowids;
}

static struct rowid_list* query(struct dyhash_single_index* single_index,struct low_data_struct* data,MEM_POOL* mem_pool)
{
	struct dyhash_bucket* bucket;
	struct rowid_list* ret;
	uint64_t hash_value;
	uint64_t hash_value_in_hash_info;
	uint32_t loc;
	uint32_t offset;
	uint32_t i;

	if(data->len == 0){
		log_warn("�ִ�Ϊ��");
		return NULL;
	}	

	//����value��һ��hash
	PROFILER_BEGIN("get hash value");
	hash_value = get_hash_value(data);
	PROFILER_END();

	int count = 0;

	//ȡģ
	loc = hash_value % single_index->hashmod;

	//�����λ����hash��ֵ������Ҫ����Ѱ��
	i=loc;
	do
	{
		bucket = single_index->mem_mmaped + i;

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
		   ret = get_rowid_list(single_index,NEXT_DOC_ROW_STRUCT(single_index->doclist, offset),mem_pool);
		   return ret;
		}
		
		i = (i+1) % single_index->hashmod;
				
		if(count++ >= DOC_MULTIPLE_NUM){
			log_warn("��ͻ���� %u", count);
			break;
		}
		
	}
	while(i!=loc);

	log_debug("��ѯ�������ֵ");
	return NULL;
}


static int32_t insert(struct dyhash_single_index* single_index,struct low_data_struct* data,uint32_t docid)
{
	struct dyhash_bucket* bucket;
	uint64_t hash_value;
	uint32_t loc;
	uint32_t i;
	uint32_t offset;

	if(data->len == 0){
		log_error("�ִ�Ϊ��");
		return -1;
	}

	//����value��һ��hash
	PROFILER_BEGIN("get hash value");
	hash_value = get_hash_value(data);
	PROFILER_END();
	
	
	//ȡģ
	loc = hash_value%single_index->hashmod;
	i = loc;

	int count = 0;
	do
	{
		bucket = single_index->mem_mmaped+i;

		//��ϣ���λ��û�����ֵ���ڣ�����doclist�ӿڲ���
		if(bucket->hash_value == 0)
		{
			//����-1˵��doclist�ռ�����
			offset = doclist_insert_v2(single_index->doclist,docid,0, i);
			if(offset == -1)
				return 1;
			if(offset == 0)
				return offset;
			//ע��hash_valueһ��Ҫ��offset֮��д����Ϊ������ѯ��ʱ�����ȥ��value���ж�value�Ƿ�Ϊ0
			Mile_AtomicSetPtr(&bucket->offset, offset);
			Mile_AtomicSetPtr(&bucket->hash_value, hash_value);
			Mile_AtomicAddPtr(&bucket->count, 1);
			return MILE_RETURN_SUCCESS;
		}

		//��ϣ���λ����ֵ���ڣ��������
		if(bucket->hash_value == hash_value)
		{
			
			offset = doclist_insert_v2(single_index->doclist,docid,bucket->offset, i);
			//����-1˵��doclist�ռ�����
			if(offset == -1)
				return 1;
			if(offset == 0)
				return offset;
			Mile_AtomicSetPtr(&bucket->offset, offset);
			Mile_AtomicAddPtr(&bucket->count, 1);
			return MILE_RETURN_SUCCESS;
		}
		
		i = (i+1) % single_index->hashmod;
		if(count++ >= CONFLICT_RETRY_NUM){
			log_warn("��ͻ���� %u", count);
			break;
		}
	}
	while(i!=loc);

	return 1;
}



