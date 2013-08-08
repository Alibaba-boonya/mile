/*
 * =====================================================================================
 *
 *       Filename:  hi_index_field.c
 *
 *    Description:  �Բ�ͬ���͵��еķ�װ��������merge�е�ʵ��
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


#include "index_field.h"

//�������ͳ�ʼ����Ӧ����
//�����������͵��У���Ҫ����store value���Ƿ�洢ԭʼֵ��������Ҫ��Ҫ��ʼ��һ�������filter�����洢�����е�ԭʼֵ
//hash btree filter_hash�����ǲ��洢ԭʼֵ��
struct index_field_manager* index_field_init(struct index_field_config* config,MEM_POOL* mem_pool)
{
	struct index_field_manager* index_field =  (struct index_field_manager*)mem_pool_malloc(mem_pool,sizeof(struct index_field_manager));
	memset(index_field,0,sizeof(struct index_field_manager));

	assert(index_field != NULL);

	/*��������Ŀ¼*/
	sprintf(index_field->work_space,"%s/%s",config->work_space,config->field_name);
	if(mkdirs(index_field->work_space) < 0)
	{
		log_error("field����Ŀ¼����ʧ��");
		return NULL;
	}

	index_field->index_type = config->index_type;
	index_field->row_limit = config->row_limit;
	strcpy(index_field->field_name,config->field_name);
	index_field->hash_compress_num = config->hash_compress_num;


	switch(index_field->index_type)
	{		
		case HI_KEY_ALG_FULLTEXT:
			{
				struct dyhash_index_config dyhash_config;
				strcpy(dyhash_config.work_space, index_field->work_space);
				dyhash_config.row_limit = index_field->row_limit;
				dyhash_config.is_full = config->is_full;
				index_field->dyhash_index = dyhash_index_init(&dyhash_config, mem_pool);
				assert(index_field->dyhash_index != NULL);
				break;
			}
	
		case HI_KEY_ALG_HASH:
			{
				/*��ʼ���е�״̬��Ϣ*/
				sprintf(index_field->stat_filename,"%s/hash_stat.inf",index_field->work_space);
				index_field->flag= (enum index_field_flag*)get_mmap_memory(index_field->stat_filename,sizeof(enum index_field_flag));
				assert(index_field->flag!= NULL);
	
				//�����ѹ��,��ʼ��hash_compress
				if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
				{
					struct hash_compress_config hcompress_config;
					memset(&hcompress_config,0,sizeof(struct hash_compress_config));
					
					strcpy(hcompress_config.work_space,index_field->work_space);
					hcompress_config.row_limit = index_field->row_limit;
					hcompress_config.hash_compress_num = index_field->hash_compress_num;

					index_field->hash_compress = hash_commpress_init(&hcompress_config,mem_pool);

					assert(index_field->hash_compress != NULL);
				}

				//δѹ������ʼ��hash_index
				else
				{
					/*��ʼ��hash����*/
					struct hash_index_config hash_config; 
					memset(&hash_config,0,sizeof(struct hash_index_config));
					
					hash_config.row_limit = index_field->row_limit;
					hash_config.is_full = config->is_full;
					strcpy(hash_config.work_space,index_field->work_space);
					
					index_field->hash_index = hash_index_init(&hash_config,mem_pool);
					assert(index_field->hash_index != NULL);

				}
				
				break;
			}
		case HI_KEY_ALG_FILTER:
			{
				/*��ʼ���е�״̬��Ϣ*/
				sprintf(index_field->stat_filename,"%s/filter_stat.inf",index_field->work_space);
				index_field->flag= (enum index_field_flag*)get_mmap_memory(index_field->stat_filename,sizeof(enum index_field_flag));
				assert(index_field->flag!= NULL);
				
				/* ��ʼ���д�С */
				sprintf(index_field->len_filename,"%s/len.inf",index_field->work_space);
				index_field->max_len = (uint32_t *)get_mmap_memory(index_field->len_filename, sizeof(uint32_t));
				assert(index_field->max_len);
				
				//�����ѹ������ʼ��filter
				if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
				{
					index_field->filter_compress = filter_compress_init(index_field->work_space,mem_pool);
					assert(index_field->filter_compress);
				}
				//δѹ������ʼ��hash_index
				else
				{
					struct filter_index_config filter_config;

					/*��ʼ���洢hash��filter*/
					filter_config.row_limit = index_field->row_limit;

					/*��filter�����Ļ���Ĭ����64λ�����ͣ������ַ������������ֶ��ɱ�ʾ*/
					filter_config.type = HI_TYPE_LONGLONG;
					filter_config.unit_size = get_unit_size(HI_TYPE_LONGLONG);
					strcpy(filter_config.work_space,index_field->work_space);

					index_field->filter_index = filter_index_init(&filter_config, mem_pool);
					assert(index_field->filter_index != NULL);
			
				}

				break;
			}
			
			
		//TODO ����btree
		case HI_KEY_ALG_BTREE:
			{
				

				break;
			}

		default:
			log_error("��֧�ֵ������� %d",index_field->index_type);
			return NULL;
	}	

	return index_field;
}



int32_t index_field_recover(struct index_field_manager* index_field, uint32_t docid)
{
	int32_t ret;

	//�ܾ����������
	if(index_field == NULL) 
	{
		log_warn("����δ��ʼ��%s",index_field->field_name);
		return ERROR_FIELD_NOT_WORK;
	}
	
	if(index_field->flag != NULL && (Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
    	return 0;

	switch(index_field->index_type)
	{
		case HI_KEY_ALG_HASH:
			 ret = hash_index_recover(index_field->hash_index, docid);    
			 break;
		case HI_KEY_ALG_FILTER:
			 ret = filter_index_recover(index_field->filter_index, docid);
			 break;
		case HI_KEY_ALG_FULLTEXT:
			 ret = dyhash_index_recover(index_field->dyhash_index, docid);
			 break;
		case HI_KEY_ALG_BTREE:
			{
				return MILE_RETURN_SUCCESS;
			}
		default:
			log_error("���е��������Ͳ���ȷ,%d",index_field->index_type);
			return ERROR_NOT_SUPPORT_INDEX;
	}
	
	return ret;
}


int32_t index_field_insert(struct index_field_manager* index_field,struct low_data_struct* data,uint32_t docid)
{
	int32_t ret;

	//�ܾ����������
	if(index_field == NULL) 
	{
		log_warn("����δ��ʼ��%s",index_field->field_name);
		return ERROR_FIELD_NOT_WORK;
	}
	
	if(index_field->flag != NULL && (Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
    	return ERROR_INDEX_FIELD_COMPRESSED;

	switch(index_field->index_type)
	{
		case HI_KEY_ALG_FULLTEXT:
			{
				/*ȫ���в���hash*/
				PROFILER_BEGIN("dyhash index insert");
				if((ret = dyhash_index_insert(index_field->dyhash_index,data,docid)) < 0)
				{
					PROFILER_END();
					return ret;
				}
				PROFILER_END();

				return MILE_RETURN_SUCCESS;

			}
		case HI_KEY_ALG_HASH:
			{
				/*��ϣ�в���hash*/
				PROFILER_BEGIN("hash index insert");
				if((ret = hash_index_insert(index_field->hash_index,data,docid)) < 0)
				{
					PROFILER_END();
					return ret;
				}
				PROFILER_END();

				return MILE_RETURN_SUCCESS;
			}
		case HI_KEY_ALG_BTREE:
			{
				return MILE_RETURN_SUCCESS;
			}
		case HI_KEY_ALG_FILTER:
			{
				//������ַ�������Ҫ�����ݽ���Ԥ����
				if(data->type == HI_TYPE_STRING)
				{
					struct low_data_struct hash_data;
					
					PROFILER_BEGIN("get hash value");
					uint64_t hash_value = get_hash_value(data);
					PROFILER_END();
					
					hash_data.data = &hash_value;
					hash_data.len = get_unit_size(HI_TYPE_LONGLONG);
					hash_data.type = HI_TYPE_LONGLONG;
					hash_data.field_name = data->field_name;

					if(*index_field->max_len < get_unit_size(HI_TYPE_LONGLONG))
					{
						*index_field->max_len = get_unit_size(HI_TYPE_LONGLONG);
						msync(index_field->max_len,sizeof(uint32_t),MS_SYNC);
					}

					PROFILER_BEGIN("filter index insert");
					if((ret = filter_index_insert(index_field->filter_index,&hash_data,docid) < 0) )
					{
						PROFILER_END();
						return ret;
					}
					PROFILER_END();
					
				}
				else
				{
					if(data->len > get_unit_size(HI_TYPE_LONGLONG))
					{
						log_error("���ݳ��ȳ���8���ֽڣ�len:%u",data->len);
						return ERROR_INSERT_FAILDED;
			
					}
					if(*index_field->max_len < data->len)
					{
						*index_field->max_len = data->len;
						msync(index_field->max_len,sizeof(uint32_t),MS_SYNC);
					}
					
					PROFILER_BEGIN("filter index insert");
					if((ret = filter_index_insert(index_field->filter_index,data,docid) < 0) )
					{
						PROFILER_END();
						return ret;
					}
					PROFILER_END();
				}

				return MILE_RETURN_SUCCESS;
			}
		default:
			log_error("���е��������Ͳ���ȷ,%d",index_field->index_type);
			return ERROR_NOT_SUPPORT_INDEX;
	}
}

//ֻ��filter�в��ܸ���
int32_t index_field_update(struct index_field_manager* index_field,
						 struct low_data_struct* new_data,
						 struct low_data_struct** old_data,
						 uint32_t docid,
						 MEM_POOL* mem_pool)
{
	int32_t ret;
	
	//�ܾ����µ�����
	if(index_field == NULL)
	{
		log_warn("����δ��ʼ��%s",index_field->field_name);
		return ERROR_FIELD_NOT_WORK;
	}

	if(index_field->flag != NULL && Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
		return ERROR_INDEX_FIELD_COMPRESSED;

	switch(index_field->index_type)
	{

		case HI_KEY_ALG_FILTER:
		{
			//������ַ�������Ҫ�����ݽ���Ԥ����
			if(new_data->type == HI_TYPE_STRING)
			{
				struct low_data_struct hash_data;

				PROFILER_BEGIN("get_hash_value");
				uint64_t hash_value = get_hash_value(new_data);
				PROFILER_END();
				
				hash_data.data = &hash_value;
				hash_data.len = get_unit_size(HI_TYPE_LONGLONG);

				PROFILER_BEGIN("filter index update");
				if((ret = filter_index_update(index_field->filter_index,&hash_data,old_data,docid,mem_pool) < 0) )
				{
					PROFILER_END();
					return ret;
				}
				PROFILER_END();
			}
			else
			{
				PROFILER_BEGIN("filter index update");
				if((ret = filter_index_update(index_field->filter_index,new_data,old_data,docid,mem_pool) < 0) )
				{
					PROFILER_END();
					return ret;
				}
				PROFILER_END();
			}

			return MILE_RETURN_SUCCESS;
		}	
		default:
			log_warn("ֻ��filter�в���update");
			return ERROR_ONLY_FILTER_SUPPORT;
	}
}



struct low_data_struct* index_field_value_query(struct index_field_manager* index_field, uint32_t docid, MEM_POOL* mem_pool)
{
	struct low_data_struct* ret = NULL;
	if(index_field == NULL)
	{
		struct low_data_struct* data = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
		memset(data,0,sizeof(struct low_data_struct));
		log_warn("����δ�����洢ʵ��");
		return data;
	}

	if(index_field->index_type != HI_KEY_ALG_FILTER )
	{
		log_warn("ֻ��hash btree filterhash�в��ܸ���value��ѯ");
		return NULL;
	}

	PROFILER_BEGIN("filter index query");

	if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
	{
		PROFILER_BEGIN("compress");
		ret = filter_compress_query(index_field->filter_compress,docid,mem_pool);
		PROFILER_END();
	}
	else
	{
		PROFILER_BEGIN("no compress");
		ret = filter_index_query(index_field->filter_index,docid,mem_pool);
		PROFILER_END();
	}

	PROFILER_END();

	return ret;
}

uint32_t index_field_count_query(struct index_field_manager* index_field, struct low_data_struct* data, MEM_POOL* mem_pool)
{
	uint32_t ret = NULL;
	if(index_field == NULL)
	{
		log_warn("����δ�����洢ʵ��");
		return NULL;
	}
	
	if(index_field->index_type != HI_KEY_ALG_FULLTEXT)
	{
		log_warn("ֻ��fulltext�в��ܸ���value��ѯ��Ӧ�ִʵĸ���");
		return 0;
	}

	/* hash */
	PROFILER_BEGIN("dyhash index query");

	//dyhash����
	if(index_field->index_type == HI_KEY_ALG_FULLTEXT){
		PROFILER_BEGIN("count query");

		ret = dyhash_index_count_query(index_field->dyhash_index, data, mem_pool);

		PROFILER_END();
	}


	PROFILER_END();
	return ret;
}


/*ֻ��hash btree filterhash hashfilterhash֧��*/
struct rowid_list* index_field_equal_query(struct index_field_manager* index_field,struct low_data_struct* data,MEM_POOL* mem_pool)
{
	struct rowid_list* ret = NULL;
	if(index_field == NULL)
	{
		log_warn("����δ�����洢ʵ��");
		return NULL;
	}
	
	if(index_field->index_type != HI_KEY_ALG_HASH && index_field->index_type != HI_KEY_ALG_FULLTEXT)
	{
		log_warn("ֻ��hash btree filterhash  fulltext�в��ܸ���value��ѯ");
		return NULL;
	}

	/* hash */
	PROFILER_BEGIN("hash index query");

	//hash����
	if(index_field->index_type  == HI_KEY_ALG_HASH){

		if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
		{
			PROFILER_BEGIN("compress");
			ret = hash_compress_query(index_field->hash_compress,data,mem_pool);
			PROFILER_END();
		}
		else
		{
			PROFILER_BEGIN("no compress");
			ret = hash_index_query(index_field->hash_index,data,mem_pool);
			PROFILER_END();
		}
	}

	//dyhash����
	if(index_field->index_type == HI_KEY_ALG_FULLTEXT){
		PROFILER_BEGIN("no compress");

		ret = dyhash_index_query(index_field->dyhash_index, data, mem_pool);
		PROFILER_END();
	}


	PROFILER_END();
	return ret;
}


int32_t index_field_compress(struct index_field_manager* index_field,MEM_POOL* mem_pool)
{
	if(index_field == NULL || 
	   (Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
	{
		log_warn("������ѹ��");
		return ERROR_NOT_NEED_COMPRESS;
	}

	log_info("��ʼѹ�� %s �� �������� :%u",index_field->field_name,index_field->index_type);
	
	switch(index_field->index_type)
	{
		case HI_KEY_ALG_HASH:

			index_field->hash_compress = hash_compress_load(index_field->hash_index,index_field->hash_compress_num,mem_pool);
			
			if(index_field->hash_compress == NULL)
			{
				log_error("hash ѹ��ʧ�� %s",index_field->field_name);
				return ERROR_COMMPRESS_FAIL;
			}
			
			break;
		case HI_KEY_ALG_FILTER:
			index_field->filter_compress = filter_compress_load(index_field->filter_index->storage,*index_field->max_len,mem_pool);
			if(index_field->filter_compress == NULL)
			{
				log_error("filterѹ��ʧ�� %s",index_field->field_name);
				return ERROR_COMMPRESS_FAIL;
			}

			if(index_field->filter_compress == (struct filter_compress_manager*)0x1)
			{
				log_error("filter����Ҫѹ�� %s",index_field->field_name);
				return ERROR_NOT_NEED_COMPRESS;
			}
			
			break;
		default:
			return ERROR_NOT_SUPPORT_COMMPRESS;
	}

	log_info("ѹ�����");
	return MILE_RETURN_SUCCESS;

}


int32_t index_field_switch(struct index_field_manager* index_field)
{
	if(index_field == NULL || 
	   (Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
	{
		log_warn("������ѹ��");
		return ERROR_FIELD_NOT_WORK;
	}
	switch(index_field->index_type)
	{
		case HI_KEY_ALG_HASH:
			if(index_field->hash_compress == NULL)
			{
				log_error("�жε�ʱ�򣬷���hashѹ��δ�ɹ���field_name:%s",index_field->field_name);
				return ERROR_COMMPRESS_FAIL;
			}

			//�л���־��
			Mile_AtomicOrPtr(index_field->flag,INDEX_FIELD_COMPRESS);

			//ȷ����ɾ��hash����֮ǰ����־λ����
			msync(index_field->flag,sizeof(enum index_field_flag),MS_SYNC);
			
			//ɾ��hash����������
			hash_index_destroy(index_field->hash_index);
			index_field->hash_index = NULL;

			break;
		case HI_KEY_ALG_FILTER:
			if(index_field->filter_compress == NULL)
			{
				log_error("�жε�ʱ�򣬷���filterѹ��δ�ɹ���field_name:%s",index_field->field_name);
				return ERROR_COMMPRESS_FAIL;
			}

			if(index_field->filter_compress == (struct filter_compress_manager*)0x1)
			{
				log_error("�жε�ʱ�򣬷���filter������Ҫѹ����field_name:%s",index_field->field_name);
				return ERROR_NOT_NEED_COMPRESS;
			}
			
			//�л���־��
			Mile_AtomicOrPtr(index_field->flag,INDEX_FIELD_COMPRESS);

			//ȷ����ɾ��filter����֮ǰ����־λ����
			msync(index_field->flag,sizeof(enum index_field_flag),MS_SYNC);
			
			//ɾ��filter����������
			filter_index_destroy(index_field->filter_index);
			index_field->filter_index = NULL;

			break;
		default:
			return ERROR_NOT_SUPPORT_COMMPRESS;
	}
	
	return MILE_RETURN_SUCCESS;
}

void index_field_checkpoint(struct index_field_manager* index_field)
{
	//ѹ������Ҫ��checkpoint
	if(index_field == NULL || (index_field->flag != NULL && (Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)))
	{
		log_warn("����δ�����洢ʵ�� ����ѹ��");
		return;
	}

	switch(index_field->index_type)
	{
		case HI_KEY_ALG_HASH:
			if(!(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
				hash_index_checkpoint(index_field->hash_index);
			break;
		case HI_KEY_ALG_FILTER:
			if(!(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS))
				filter_index_checkpoint(index_field->filter_index);
			break;
		case HI_KEY_ALG_FULLTEXT:
				dyhash_index_checkpoint(index_field->dyhash_index);
			break;
		case HI_KEY_ALG_BTREE:
			//TODO btree
			break;
		default:
			log_error("��֧�ֵ���������,%d",index_field->index_type);
			break; ;
	}
	
	msync(index_field->flag,sizeof(enum index_field_flag),MS_SYNC);
	msync(index_field->max_len,sizeof(uint32_t),MS_SYNC);
	return;
}


void index_field_release(struct index_field_manager* index_field)
{
	if(index_field == NULL)
	{
		log_warn("����δ�����洢ʵ��");
		return;
	}
	
	switch(index_field->index_type)
	{
		case HI_KEY_ALG_FULLTEXT:
			dyhash_index_release(index_field->dyhash_index);		

			break;
		case HI_KEY_ALG_HASH:
			
			if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
				hash_compress_release(index_field->hash_compress);
			else
				hash_index_release(index_field->hash_index);
			
			break;
		case HI_KEY_ALG_FILTER:
			if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
				filter_compress_release(index_field->filter_compress);
			else
				filter_index_release(index_field->filter_index);

			msync(index_field->max_len,sizeof(uint32_t),MS_SYNC);
			munmap(index_field->max_len,sizeof(uint32_t));
			break;
		case HI_KEY_ALG_BTREE:
			//TODO btree
			break;
		default:
			log_error("��֧�ֵ���������,%d",index_field->index_type);
			break; ;
	}
	
	msync(index_field->flag,sizeof(enum index_field_flag),MS_SYNC);
    munmap(index_field->flag, sizeof(enum index_field_flag));
	
	return;
}


void index_field_destroy(struct index_field_manager* index_field)
{
	if(index_field == NULL)
	{
		log_warn("����δ�����洢ʵ��");
		return;
	}
	
	switch(index_field->index_type)
	{
		case HI_KEY_ALG_HASH:
			
			if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
				hash_compress_destroy(index_field->hash_compress);
			else
				hash_index_destroy(index_field->hash_index);
			
			break;
		case HI_KEY_ALG_FILTER:
			if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
			{
				filter_compress_destroy(index_field->filter_compress);
			}
			else
				filter_index_destroy(index_field->filter_index);

			
			 munmap(index_field->max_len,sizeof(uint32_t));
			 remove(index_field->len_filename);
			break;
		case HI_KEY_ALG_BTREE:
			//TODO btree
			break;
		default:
			log_error("��֧�ֵ���������,%d",index_field->index_type);
			break; ;
	}
	
	 munmap(index_field->flag, sizeof(enum index_field_flag));
	 remove(index_field->stat_filename);

	 //ɾ��������Ŀ¼
	 remove(index_field->work_space);
	 return;
}

int index_mmap_switch(struct index_field_manager *index_field)
{
	if(NULL == index_field) {
		log_warn( "no index storage" );
		return MILE_RETURN_SUCCESS;
	}

	switch(index_field->index_type) {
	case HI_KEY_ALG_HASH:
		if(Mile_AtomicGetPtr(index_field->flag) & INDEX_FIELD_COMPRESS)
			return MILE_RETURN_SUCCESS;
		return hash_index_mmap_switch(index_field->hash_index);
	case HI_KEY_ALG_FULLTEXT:
		return dyhash_index_mmap_switch(index_field->dyhash_index);
		break;
	default:
		;
	}
	return MILE_RETURN_SUCCESS;
}

/*-----------------------------------------------------------------------------
 *  btree��Χ��ѯ������������row id list
 *-----------------------------------------------------------------------------*/

//TODO ����btree
struct rowid_list * index_field_range_query(struct index_field_manager * index_field, \
			struct db_range_query_condition * range_condition, MEM_POOL* mem_pool)
{

	return NULL;
}

