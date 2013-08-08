/*
 * =====================================================================================
 *
 *       Filename:  hi_table.c
 *
 *    Description:  ��Ľṹ��Ϣ���������еĶ�
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
 

#include "table.h"

static int32_t table_pk_check(struct table_manager* table,uint32_t row_id,struct row_data* rdata,MEM_POOL* mem_pool);

static struct segment_manager* init_segment_manager_instance(struct table_manager* table,struct segment_meta_data* meta_data, uint16_t sid);

static int32_t table_add_segment(struct table_manager* table,int32_t error,MEM_POOL* mem_pool);

static int32_t table_build_index(struct table_manager* table,struct index_field_meta* index_meta,char* field_name,char* index_field_name,enum index_key_alg index_type,enum field_types data_type);



static void query_data_and_insert_index(struct segment_manager* segment,
									    uint32_t docid,
									    enum index_key_alg index_type,
									    enum field_types data_type,
									    char* field_name,
										char* index_field_name,
									    MEM_POOL* mem_pool);



struct table_manager* table_init(struct table_config* config,MEM_POOL* mem_pool)
{
	struct table_manager* table = (struct table_manager*)mem_pool_malloc(mem_pool,sizeof(struct table_manager));
	memset(table,0,sizeof(struct table_manager));

	/*����memap���ڴ��е�����ʱ������Ϣ*/
	table->table_meta = config->table_meta;

	table->mem_pool = mem_pool;

	/*��ʼ��Ŀ¼*/
	table->storage_dirs = all_str_append(config->storage_dirs, mem_pool, "/%s", table->table_meta->table_name);
	table->work_space = table->storage_dirs->strs[0];
	if(mkdirs(table->work_space) < 0)
	{
		log_error("table����Ŀ¼��׼ȷ");
		return NULL;
	}

	if(config->max_segment_num == 0)
		table->max_segment_num = MAX_SEGMENT_NUM;
	
	table->max_segment_num = config->max_segment_num;
	table->row_limit = config->row_limit;
	table->hash_compress_num = config->hash_compress_num;

	//��ʼ�����еĶ�meta��Ϣ
	sprintf(table->segment_meta_filename,"%s/segment.meta",table->work_space);
	table->segment_meta = (struct segment_meta_data*)get_mmap_memory(table->segment_meta_filename,SEGMENT_RUNTIME_SIZE(table));
	assert(table->segment_meta);

	table->segments = (struct segment_manager**)mem_pool_malloc(mem_pool,sizeof(struct segment_manager*)*
		table->max_segment_num);
	memset(table->segments,0,sizeof(struct segment_manager*) * table->max_segment_num);
		
	
	//��ʼ����������Ϣ
	struct segment_meta_data* meta_data = table->segment_meta;
	uint16_t i;
	struct segment_manager* segment = NULL;
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{
		//�ж϶��Ƿ��Ѿ���ʼ����
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;	

		segment = init_segment_manager_instance(table,meta_data,i);

		/*���ϵͳ�տ�ʼ����ʱ������ʱ����Ҫ����*/
	    if(meta_data->create_time== 0L)
	    {
	   	   meta_data->create_time = time(NULL);
		   Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_INIT);
	    }

		assert(segment);

		table->segments[i] = segment;
	}


	//����������Ϣת����string_map�ṹ
	table->index_meta_hash = init_string_map(mem_pool,MAX_INDEX_FIELD_NUM);
	struct index_field_meta* index_meta = NULL;
	struct table_meta_data* table_meta = table->table_meta;
	for(i=0; i<table_meta->index_field_count; i++)
	{
		index_meta = (struct index_field_meta*)mem_pool_malloc(mem_pool,sizeof(struct index_field_meta));
		memset(index_meta,0,sizeof(struct index_field_meta));
		memcpy(index_meta,table_meta->index_meta+i,sizeof(struct index_field_meta));
		
		string_map_put(table->index_meta_hash, table_meta->index_meta[i].field_name,(void*)index_meta,1);
	}

	//��ʼ��������
	if(pthread_mutex_init(&table->write_protect_locker,NULL) != 0)
	{
		log_error("��������ʼ��ʧ��");
		return NULL;
	}

	if(pthread_rwlock_init(&table->read_protect_locker,NULL) != 0)
	{
		log_error("��д����ʼ��ʧ��");
		return NULL;
	}

	table->store_raw = config->store_raw;

	//��ʼ��������table�ı�־
	Mile_AtomicOrPtr(&table->table_meta->stat,TABLE_INIT);

	return table;
}



//���һ����Ϣ
//��Ҫ�������segment_current���Ѿ�����ʼ���ˣ�˵�����ʱ�����еĶ��Ѿ��������
static int32_t table_add_segment(struct table_manager* table,int32_t error,MEM_POOL* mem_pool)
{
	char segment_name[MAX_SEGMENT_NAME];
	struct segment_meta_data* meta_data;
	struct table_meta_data* table_meta = table->table_meta;
	uint16_t i;
	int32_t ret = MILE_RETURN_SUCCESS;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	//��һ���εĿ�
	meta_data = table->segment_meta;

	if(error == ERROR_EXCEED_LIMIT)
	{
		for(i=0;i<table->max_segment_num;i++)
		{
			//����ö�δ��ʼ�������ҵ�
			if(!(Mile_AtomicGetPtr(&(meta_data + (i + table->table_meta->segment_current) % table->max_segment_num)->flag)&SEGMENT_INIT))
				break;

		}
		if(i == table->max_segment_num)
		{
			log_error("���еĶ��Ѿ���������");
			return ERROR_EXCEED_SEGMENT_NUM;
		}

		i = (i + table->table_meta->segment_current) % table->max_segment_num;
		meta_data += i;

		log_info("�ҵ�%u�κţ�����Ϊ��ǰ��",i);

		Mile_AtomicSetPtr(&table->table_meta->segment_current,i);

		//���µ���index_meta��Ϣ��string_map������漰���޸�index_meta_hash���Ͳ�ѯ����check_index�����̰߳�ȫ������
		pthread_rwlock_wrlock(&table->read_protect_locker);
		for(i=0; i<table_meta->index_field_count; i++)
		{
			string_map_remove(table->index_meta_hash,(table_meta->index_meta+i)->field_name);
			string_map_put(table->index_meta_hash, (table_meta->index_meta+i)->field_name,(void*)(table_meta->index_meta+i),1);
		}
		pthread_rwlock_unlock(&table->read_protect_locker);
	}
	else
	{
		meta_data += Mile_AtomicGetPtr(&table->table_meta->segment_current);	
	}
	
	memset(segment_name,0,sizeof(segment_name));
	sprintf(segment_name,"%s_segment_%06u",table->table_meta->table_name,Mile_AtomicGetPtr(&table->table_meta->segment_current));
	
	memset(meta_data,0,sizeof(struct segment_meta_data));

	//����Ӷε�ʱ�򣬲鿴��Ӧ�Ķ�Ŀ¼�Ƿ���ڣ���������򱨴�
	char segment_workspace[FILENAME_MAX_LENGTH];
	struct stat stats;
	memset(segment_workspace,0,sizeof(segment_workspace));
	sprintf(segment_workspace,"%s/%s",table->work_space,segment_name);
	if (lstat(segment_workspace, &stats) == 0 && S_ISDIR (stats.st_mode)) 
    {
		log_error("Ŀ¼�Ѿ����� %s",segment_workspace);
		return ERROR_SEGMENT_INIT_FAILED;
    }

	//������漰������������ԭʼ��Ϣ��������Ҫ�Ӷ���
	pthread_rwlock_rdlock(&table->read_protect_locker);
	table->segments[Mile_AtomicGetPtr(&table->table_meta->segment_current)] = 
		init_segment_manager_instance(table,meta_data,Mile_AtomicGetPtr(&table->table_meta->segment_current));

	if(table->segments[Mile_AtomicGetPtr(&table->table_meta->segment_current)] == NULL)
	{
		pthread_rwlock_unlock(&table->read_protect_locker);
		return ERROR_SEGMENT_INIT_FAILED;
	}

	/*bugfix ���޸Ķε�ʱ��ŵ����� ���ڽ��γ�ʼ���ͽ���־�ſ������ʱ�򣬶ε���Ϣ��δ��ֵ��table->segments������
	����table_index_equal_query��ʱ�򣬾ͻ�����̰߳�ȫ���� by yunliang.shi 2012-12-21*/
    if(meta_data->create_time== 0L)
    {
   	   meta_data->create_time = time(NULL);
	   Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_INIT);
    }
	pthread_rwlock_unlock(&table->read_protect_locker);

	return ret;
}



int32_t table_ensure_index(struct table_manager* table,
						 char* field_name,
						 enum index_key_alg index_type,
						 enum field_types data_type,
						 MEM_POOL* mem_pool)
{
	struct table_meta_data* table_meta = table->table_meta;
	struct index_field_meta* index_meta = table_meta->index_meta;

	uint16_t i;
	uint8_t j;
	int32_t ret;


	char  index_field_name[MAX_FIELD_NAME];
	memset(index_field_name, 0, sizeof(index_field_name));
	if(index_type == HI_KEY_ALG_FULLTEXT)
	{
		sprintf(index_field_name, "$%s", field_name);
	}
	else
	{
		strcpy(index_field_name, field_name);
	}


	pthread_rwlock_rdlock(&table->read_protect_locker);
	for(i=0; i<table_meta->index_field_count; i++,index_meta++)
	{
		if(strcmp(index_meta->field_name,index_field_name) == 0)
		{
			//�Ȳ��ң��ж���û�У���ֹ�ظ��ύ
			for(j=0; j<index_meta->index_count; j++)
			{
				if(index_meta->indexs[j].index_type == index_type)
				{
					log_warn("%s %d�����ѽ���",index_field_name,index_type);
					pthread_rwlock_unlock(&table->read_protect_locker);
					return MILE_RETURN_SUCCESS;
				}
			}

			//û�ҵ��������
			pthread_rwlock_unlock(&table->read_protect_locker);

			//������ԭʼ��Ϣֻ�������ε�ʱ�����ʵ���ֻ�����������޸�
			pthread_rwlock_wrlock(&table->read_protect_locker);
			index_meta->indexs[index_meta->index_count].index_type = index_type;
			index_meta->indexs[index_meta->index_count].data_type = data_type;
			index_meta->index_count++;
			pthread_rwlock_unlock(&table->read_protect_locker);
			
			ret = table_build_index(table,index_meta,field_name,index_field_name,index_type,data_type);
			
			return ret;
		}
	}

	//��������û�ҵ�
	pthread_rwlock_unlock(&table->read_protect_locker);

	pthread_rwlock_wrlock(&table->read_protect_locker);
	index_meta = table_meta->index_meta + table_meta->index_field_count;
	strcpy(index_meta->field_name,index_field_name);
	index_meta->index_count = 1;
	index_meta->indexs[0].index_type = index_type;
	index_meta->indexs[0].data_type = data_type;
	table_meta->index_field_count++;
	pthread_rwlock_unlock(&table->read_protect_locker);
	
	ret = table_build_index(table,index_meta,field_name,index_field_name,index_type,data_type);

	return ret;
}


int32_t table_del_index(struct table_manager* table,char* field_name,enum index_key_alg index_type)
{
	struct table_meta_data* table_meta = table->table_meta;
	struct index_field_meta* index_meta = table_meta->index_meta;

	uint16_t i;
	uint8_t j;
	uint8_t found = 0;

	char  index_field_name[MAX_FIELD_NAME];
	memset(index_field_name, 0, sizeof(index_field_name));
	if(index_type == HI_KEY_ALG_FULLTEXT)
	{
		sprintf(index_field_name, "$%s", field_name);
	}
	else
	{
		strcpy(index_field_name, field_name);
	}
	

	log_info("��ʼִ��ɾ��������%s ��������%u",index_field_name,index_type);

	log_info("��ɾ��������Ԫ������Ϣ");

	//������Ԫ������Ϣ��ֻ����ɾ�����½�������ʱ��db�ϲ����
	for(i=0; i<table_meta->index_field_count; i++,index_meta++)
	{
		if(strcmp(index_meta->field_name,index_field_name) == 0)
		{
			//�Ȳ��ң��ж���û�У���ֹ�ظ��ύ
			for(j=0; j<index_meta->index_count; j++)
			{
				if(index_meta->indexs[j].index_type == index_type)
				{
					uint8_t k;
					found = 1;
					
					for(k = j;k < index_meta->index_count-1; k++)
					{	
						memmove(index_meta->indexs+k,index_meta->indexs+k+1,sizeof(struct index_field_stat));
					}
					
					//������һ��
					memset(index_meta->indexs+k,0,sizeof(struct index_field_stat));

					//˵�������ֻ��Ψһ��һ������
					if(--index_meta->index_count == 0)
					{
						uint16_t m;
						for(m = i;m<table_meta->index_field_count-1; m++)
						{
							memmove(table_meta->index_meta + m, table_meta->index_meta+m+1, sizeof(struct index_field_meta));
						}

						//������һ��
						memset(table_meta->index_meta + m,0,sizeof(struct index_field_meta));
						
						table_meta->index_field_count--;
					}

					break;
				}
			}
		}

		if(found)
			break;
	}	

	if(!found)
	{
		log_warn("δ�ҵ���Ҫɾ���������� %s  %u",index_field_name,index_type);
		return MILE_RETURN_SUCCESS;
	}

	log_info("��ʼɾ��string_map�����������뽫ֹͣ�������Ĳ���...");

	//��д��
	//����������ֹɾ��������ʱ�򣬷�ֹ����ʱ�򣬻����̰߳�ȫ����
	pthread_rwlock_wrlock(&table->read_protect_locker);
	
	string_map_remove(table->index_meta_hash,index_field_name);

	for(i=0; i<table_meta->index_field_count; i++)
	{
		string_map_remove(table->index_meta_hash,(table_meta->index_meta+i)->field_name);
		string_map_put(table->index_meta_hash, (table_meta->index_meta+i)->field_name,(void*)(table_meta->index_meta+i),1);
	}

	struct segment_meta_data* meta_data = table->segment_meta;
	
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;

		segment_del_index(table->segments[i],index_field_name,index_type);
	}
	
	pthread_rwlock_unlock(&table->read_protect_locker);

	return MILE_RETURN_SUCCESS;
}



static void query_data_and_insert_index(struct segment_manager* segment,
									    uint32_t docid,
									    enum index_key_alg index_type,
									    enum field_types data_type,
									    char* field_name,
										char* index_field_name,
									    MEM_POOL* mem_pool)
{
	struct low_data_struct* data = NULL;
	struct low_data_struct null_data;
	int32_t ret;
	null_data.len = 0;
	
	//�ж���û��ɾ��
	if(segment_is_docid_deleted(segment,docid))
		return;
	
	//��ѯԭʼ������
	data = segment_data_query_col(segment,index_field_name,docid,mem_pool);


	//���뵽������
	if(data != NULL)
	{
		//�滻������������
		data->field_name = index_field_name;

		//У�����������Ƿ���ȷ
		if(data->type != data_type)
		{
			if(data->type != HI_TYPE_NULL)
				log_error("%s %u �������Ͳ�ƥ�� data_type:%u  �������� data_type:%u",field_name,
																					 index_type,
																					 data_type,
																					 data->type);
			null_data.field_name = data->field_name;
			ret = segment_index_insert(segment,&null_data,index_type,docid,mem_pool);
		}
		else
		{
			ret = segment_index_insert(segment,data,index_type,docid,mem_pool);
		}

		
		if(ret < 0)
		{
			log_error("%s %d��������ʧ�� ret %d",field_name,index_type,ret);
			return;
		}
	}

	return;
}

static int32_t table_build_index(struct table_manager* table,struct index_field_meta* index_meta,char* field_name, char* index_field_name, enum index_key_alg index_type,enum field_types data_type)
{
	MEM_POOL* mem_pool_local = mem_pool_init(MB_SIZE);
	uint16_t i;
	uint8_t j;
	int32_t ret;	
	struct segment_meta_data* meta_data;

	log_info("��ʼ���� ����:%s ����:%u...",index_field_name,index_type);
	
	//�ȶԵ�ǰ���������
	uint16_t sid = Mile_AtomicGetPtr(&table->table_meta->segment_current);

	if(!(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag) & SEGMENT_INIT))
	{
		log_info("��ǰ��δ��ʼ�� %u",sid);
		goto FIN;
	}
	
	log_info("��ʼ�Ե�ǰ��:%u��������...",sid);


	//����ط�Ҳ�����д������Ϊ���segment���index��hash����ṹ���в������ڶ�ȡ��ʱ�򣬴����̰߳�ȫ����
	pthread_rwlock_wrlock(&table->read_protect_locker);
	ret = segment_ensure_index(table->segments[sid],index_field_name,index_type,table->mem_pool);
	pthread_rwlock_unlock(&table->read_protect_locker);
	
	if(ret != 0)
		return ret;

	//��׷��ǰ��
	uint32_t docid;
	struct index_field_meta* index_hash_meta;
	for(docid = 0; docid < segment_get_rowcount(table->segments[sid]); docid++)
	{
		mem_pool_reset(mem_pool_local);

		//����ط����üӶ�����ֻ���ȡԭʼ�����ݣ����µ������в������ݣ�������µ����������ǲ��ᱻ���
		query_data_and_insert_index(table->segments[sid],
									docid,
									index_type,
									data_type,
									field_name,
									index_field_name,
									mem_pool_local);
	

		//������ڵ�ǰ�Σ����Ҳ鿴��ǰ��docid�Ƿ�͵�ǰ�ε�docid��10�����ڣ��򽫱���ס
		if(sid ==  Mile_AtomicGetPtr(&table->table_meta->segment_current) &&
			segment_get_rowcount(table->segments[sid])-docid < 10)
		{
			//����
			table_lock(table);

			log_info("��������ǰ��:%u ��ǰ׷��������docid:%u �ܵ�docid��:%u",sid,
																			   docid,
																			   segment_get_rowcount(table->segments[sid]));

			//��ʣ���10������ȫ������
			while(++docid < segment_get_rowcount(table->segments[sid]))
			{
				mem_pool_reset(mem_pool_local);
				
				query_data_and_insert_index(table->segments[sid],
											docid,
											index_type,
											data_type,
											field_name,
											index_field_name,
											mem_pool_local);
			}

			//�����漰���޸�index_meta_hash���Ͷ���check_index������Դ����
			pthread_rwlock_wrlock(&table->read_protect_locker);
			log_info("���±��������Ϣ����ǰ���������ڿɲ���״̬.....");
			index_hash_meta = (struct index_field_meta*)string_map_get(table->index_meta_hash,index_field_name);
			if(index_hash_meta == NULL)
			{
				string_map_put(table->index_meta_hash,index_field_name,(void*)index_meta ,1);
			}
			else
				memmove(index_hash_meta,index_meta,sizeof(struct index_field_meta));

			//�����ѹ��������Ҫ�ٴ���ѹ��
			if(Mile_AtomicGetPtr(&table->segments[sid]->meta_data->flag) & SEGMENT_COMPRESS)
			{
				Mile_AtomicAndPtr(&table->segments[sid]->meta_data->flag,~SEGMENT_COMPRESS);
				Mile_AtomicOrPtr(&table->segments[sid]->meta_data->flag,SEGMENT_FULL);
				log_info("�ö�%u ������ѹ��״̬ ����Ϊ��ѹ��",sid);
			}
			
			pthread_rwlock_unlock(&table->read_protect_locker);
			
			table_unlock(table);
		}

	}

	
	
	FIN:
	
	meta_data = table->segment_meta;
	
	log_info("��ʼ������Ķν�����������...");

	//������Ķν��в���
	for(i=0; i<table->max_segment_num; i++,meta_data++)
	{
		if(!(Mile_AtomicGetPtr(&meta_data->flag) & SEGMENT_FULL))
			continue;

		if(i == sid)
			continue;

		log_info("��ʼ�Զ�%u %s�� ����%u����",i,index_field_name,index_type);

		segment_ensure_index(table->segments[i],index_field_name,index_type,table->mem_pool);

		for(docid = 0; docid < table->row_limit; docid++)
		{

			mem_pool_reset(mem_pool_local);
			query_data_and_insert_index(table->segments[i],
										docid,
										index_type,
										data_type,
										field_name,
										index_field_name,
										mem_pool_local);
		}

		//�����ѹ��������Ҫ�ٴ���ѹ��
		if(Mile_AtomicGetPtr(&meta_data->flag) & SEGMENT_COMPRESS)
		{
			Mile_AtomicAndPtr(&meta_data->flag,~SEGMENT_COMPRESS);
			Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_FULL);
			log_info("�ö�%u ������ѹ��״̬ ����Ϊ��ѹ��",i);
		}
	}

	// flush index to disk.
	table_checkpoint(table);

	//����������ϣ����Խ��ܲ�ѯ
	log_info("����������������...");

	
	//�����漰���޸�index_meta_hash���Ͷ���check_index������Դ����
	pthread_rwlock_wrlock(&table->read_protect_locker);
	
	for(j=0; j< index_meta->index_count; j++)
	{
		if(index_meta->indexs[j].index_type == index_type)
		{
			index_meta->indexs[j].flag = 1;
			log_info("���±������״̬��%s ����%u ���ڿɲ�ѯ״̬",index_meta->field_name,index_meta->indexs[j].index_type);
		}
	}
	
	string_map_remove(table->index_meta_hash,index_field_name);

	string_map_put(table->index_meta_hash,index_field_name,(void*)index_meta ,1);

	pthread_rwlock_unlock(&table->read_protect_locker);
	
	mem_pool_destroy(mem_pool_local);

	return MILE_RETURN_SUCCESS;
}

static int make_segment_dir(const struct str_array_t *storage_dirs, const char *work_space, const char *segment_name, uint16_t sid)
{
	char work_dir[PATH_MAX];
	sprintf(work_dir, "%s/%s", work_space, segment_name);
	
	struct stat st;
	if (stat(work_dir, &st) == 0 && S_ISDIR(st.st_mode)) // segment dir exist
		return 0;

	// create real dir
	char real_dir[PATH_MAX];
	sprintf(real_dir, "%s/%s", storage_dirs->strs[sid % storage_dirs->n], segment_name);
	if (mkdirs(real_dir) != 0) {
		return -1;
	}

	if (strcmp(work_dir, real_dir) == 0)
		return 0;

	// symbolic link real_dir to work_dir
	if (real_dir[0] != '/') { // relative path
		char absolute_path[PATH_MAX];
		if (realpath(real_dir, absolute_path) == NULL) {
			log_error("get absolute path failed, path real_dir %s, errno %d", real_dir, errno);
			return -1;
		}
		strcpy(real_dir, absolute_path);
	}
	if (symlink(real_dir, work_dir) != 0) {
		log_error("symlink %s to %s failed, errno %d", real_dir, work_dir, errno);
		return -1;
	}

	return 0;
}


static struct segment_manager* init_segment_manager_instance(struct table_manager* table,struct segment_meta_data* meta_data, uint16_t sid)
{
	//��ʼ������Ϣ
	struct segment_manager* segment = NULL;
	struct segment_config seg_config;

	memset(&seg_config,0,sizeof(struct segment_config));
	sprintf(seg_config.segment_name,"%s_segment_%06u",table->table_meta->table_name,sid);
	strcpy(seg_config.work_space,table->work_space);
	seg_config.hash_compress_num = table->hash_compress_num;
	seg_config.index_field_count = table->table_meta->index_field_count;
	seg_config.row_limit = table->row_limit;
	seg_config.meta_data = meta_data;
	seg_config.sid = sid;
	seg_config.index_fields = table->table_meta->index_meta;

	if (make_segment_dir(table->storage_dirs, seg_config.work_space, seg_config.segment_name, sid) != 0) {
		log_error("make segment dir failed, segment name %s", seg_config.segment_name);
		return NULL;
	}

	if((segment = segment_init(&seg_config,table->mem_pool)) == NULL)
    {
    	log_error("�γ�ʼ������ �κ�:%u ����:%s",sid,seg_config.segment_name);
		memset(meta_data,0,sizeof(struct segment_meta_data));
    	return NULL;
	}
	
	return segment;
}


int32_t table_recover(struct table_manager* table, uint16_t sid, uint32_t docid)
{	
	struct segment_meta_data* meta_data;
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	meta_data = table->segment_meta + sid;

	if(!(Mile_AtomicGetPtr(&meta_data->flag) & SEGMENT_INIT))
	{
		return ERROR_SEGMENT_NOT_INIT;	

	}

	return segment_recover(table->segments[sid], docid);
}

uint64_t table_get_segment_ctime(struct table_manager* table,uint16_t sid)
{
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	/*ͨ���εı�־���ж�*/
	//��ȡmeta data
	if(!(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag) & SEGMENT_INIT))
		return 0;
    else
		return Mile_AtomicGetPtr(&(table->segment_meta+sid)->create_time);
}


uint64_t table_get_segment_mtime(struct table_manager* table,uint16_t sid)
{
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	/*ͨ���εı�־���ж�*/
	//��ȡmeta data
	if(!(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
		return 0;
    else
		return Mile_AtomicGetPtr(&(table->segment_meta+sid)->modify_time);
}



int32_t table_prepare_insert(struct table_manager* table,uint16_t* sid,uint32_t* docid,uint8_t flag,MEM_POOL* mem_pool)
{
	struct segment_meta_data* meta_data;
	int32_t ret;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	//�����ϲ��ṩsid
	if(flag == DOCID_BY_BINLOG)
	{
		struct segment_manager* segment;
		
		meta_data = table->segment_meta + *sid;
		segment = table->segments[*sid];
		
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
		{
			table->segments[*sid] = init_segment_manager_instance(table,meta_data,*sid);
			if(table->segments[*sid] == NULL)
			{
				return ERROR_SEGMENT_INIT_FAILED;
			}

			if(meta_data->create_time== 0L)
		    {
		   	   meta_data->create_time = time(NULL);
			   Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_INIT);
		    }
			table->new_seg_complete = 1;
		}

		//������ǰ��
		Mile_AtomicSetPtr(&table->table_meta->segment_current,*sid);

		return MILE_RETURN_SUCCESS;
	}
	
	//��ȡ�ε�Ԫ������Ϣ
	meta_data = table->segment_meta + Mile_AtomicGetPtr(&table->table_meta->segment_current);

	//��һ������ʱ�����ܷ����κ�Ϊ0��һ���ζ�û�г�ʼ��
	if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
		ret = ERROR_SEGMENT_NOT_INIT;
	else
		ret = segment_exceed_limit(table->segments[Mile_AtomicGetPtr(&table->table_meta->segment_current)]);

	if(ret == ERROR_EXCEED_LIMIT || ret == ERROR_SEGMENT_NOT_INIT )
	{		
		//��Ҫ����һ��segment
		log_info("��Ҫ����һ���� ��ǰ��:%u",Mile_AtomicGetPtr(&table->table_meta->segment_current));
		PROFILER_BEGIN("add one segment");
		if((ret=table_add_segment(table,ret,mem_pool))<0)
		{
			PROFILER_END();
			// set new segment add complete flag
			table->new_seg_complete = 1;
			return ret;
		}
		PROFILER_END();
		log_info("�δ������ ��ǰ��:%u",Mile_AtomicGetPtr(&table->table_meta->segment_current));
		// set new segment add complete flag
		table->new_seg_complete = 1;
	}

	*sid = Mile_AtomicGetPtr(&table->table_meta->segment_current);
	*docid = segment_get_rowcount(table->segments[Mile_AtomicGetPtr(&table->table_meta->segment_current)]);

	return ret;
}



static int32_t table_pk_check(struct table_manager* table,uint32_t row_id,struct row_data* rdata,MEM_POOL* mem_pool)
{
	/*
	uint16_t i;
	struct schema_info* schema = table->schema;
	struct hint_array* time_cond = get_time_hint(mem_pool);
	struct list_head* rowids_list_h;
	struct segment_query_rowids* node;

	for(i=0;i<rdata->field_count;i++)
	{
		//�жϸ����Ƿ�Ψһ��
		if(schema->fld_info[rdata->field_datas[i].fid]->unique)
		{
			rowids_list_h = table_query_by_value(table,rdata->field_datas[i].fid,time_cond,rdata->field_datas[i].data,mem_pool);

			//�ж��Ƿ�Ϊ��
			list_for_each_entry(node,rowids_list_h,rowids_list){
				if(node->rowids != NULL && node->rowids->rowid_num != 0)
					return PK_FAIL;
			}

		}
	}
	*/

	return PK_OK;
}





int32_t table_insert(struct table_manager* table,uint16_t sid,uint32_t docid,struct row_data* rdata,MEM_POOL* mem_pool)
{
	int32_t ret = MILE_RETURN_SUCCESS;
	uint16_t i;
	uint8_t j;
	j = 0;
	
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
		
	}

	if(!(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
    {
     	return ERROR_SEGMENT_NOT_INIT;
	}

	//�ж���û��PK��
	if(table_pk_check(table,docid,rdata,mem_pool))
		return ERROR_PK_CONFLICT;

	//�Ȳ���ԭʼ����
	if( table->store_raw && (ret = segment_data_insert(table->segments[sid],rdata,docid,mem_pool)) < 0)
	{
		return ret;
	}

	//�������ԭʼֵ������Ҫ��modify time�ĵ�
	if( !table->store_raw ){
		Mile_AtomicSetPtr(&table->segments[sid]->meta_data->modify_time,time(NULL));
	}

	//����������
	struct low_data_struct* data = rdata->datas;
	struct index_field_meta* index_meta = NULL;
	struct low_data_struct null_data;
	null_data.len = 0;

	PROFILER_BEGIN("index insert");
	for(i=0; i<rdata->field_count; i++,data++)
	{
		//ȡ��������Ϣ
		index_meta = (struct index_field_meta *)string_map_get(table->index_meta_hash, data->field_name);

		if(index_meta == NULL)
		{
			log_debug("%s û��������",data->field_name);
			continue;
		}

		for(j=0; j<index_meta->index_count; j++)
		{
			//У�����������Ƿ���ȷ
			if(data->type != index_meta->indexs[j].data_type)
			{
				if(data->type != HI_TYPE_NULL)
					log_error("%s %u �������Ͳ�ƥ�� data_type:%u  �������� data_type:%u",index_meta->field_name,
	  																					 index_meta->indexs[j].index_type,
	  																					 index_meta->indexs[j].data_type,
	  																					 data->type);
				null_data.field_name = data->field_name;
				ret = segment_index_insert(table->segments[sid],&null_data,index_meta->indexs[j].index_type,docid,mem_pool);
			}
			else
			{
				ret = segment_index_insert(table->segments[sid],data,index_meta->indexs[j].index_type,docid,mem_pool);
			}
			
			if(ret < 0)
			{
				log_warn("%s�� ����%d ����ʧ�� ret:%d",data->field_name,index_meta->indexs[j].index_type,ret);

				//��ʹ����ʧ���ˣ�ҲҪ��docid����
				segment_set_rowcount(table->segments[sid], docid);
				PROFILER_END();
				return ret;
			}
		}
	}
	PROFILER_END();

	//���������к�������ȫ������ɹ��������
	segment_set_docid_inserted(table->segments[sid],docid);

	//����
	segment_set_rowcount(table->segments[sid], docid);

	if( Mile_AtomicGetPtr(&table->segments[sid]->meta_data->row_count) == table->row_limit ) { // segment full
		segment_set_flag(table->segments[sid],SEGMENT_FULL);

		// set segment add complete flag
		table->new_seg_complete = 0;

		if( create_mmap_switch_thread(table->table_meta->table_name, sid) != 0) {
			log_error( "create segment full thread failed" );
			return ERROR_MMAP_SWITCH;
		}
	}

	return ret;
}


int32_t table_update(struct table_manager* table,
				   uint16_t sid,
				   uint32_t docid, 
				   struct low_data_struct* new_data,
				   struct low_data_struct** old_data,
				   MEM_POOL* mem_pool)
{
	int32_t ret;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	/*ͨ���εı�־���ж�*/
    //��ȡmeta data
	if(sid >= table->max_segment_num || !(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
         return ERROR_SEGMENT_NOT_INIT;

	//�鿴�Ƿ�����ǰ�ε�rowcount
	if(docid >= segment_get_rowcount(table->segments[sid]))
         return ERROR_EXCEED_CURRENT;
	
	//����Ƿ�֧��hash
	if(check_index(table,new_data->field_name,HI_KEY_ALG_HASH,NULL))
		return ERROR_ONLY_FILTER_SUPPORT;

	ret = segment_data_update(table->segments[sid],docid,new_data,old_data,mem_pool);
	
	if(ret < 0)
		return ret;

	if(check_index(table,new_data->field_name,HI_KEY_ALG_FILTER,NULL))
	{
		ret = segment_index_update(table->segments[sid],docid,new_data,old_data,mem_pool);
	}

	return ret;
}



//��ѯ���ϲ㴫��Ĳ���������Ҫ��ѯ����
//���ȶ�ÿ���н��б�����һ������û�д洢ԭʼֵ����ֱ�ӽ���merge��
struct row_data* table_data_query_row(struct table_manager* table,uint16_t sid,uint32_t docid,MEM_POOL* mem_pool)
{
	struct row_data* ret = NULL;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL || !table->store_raw)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	/*ͨ���εı�־���ж�*/
    //��ȡmeta data
	if(sid >= table->max_segment_num || !(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
         return NULL;

	ret = segment_data_query_row(table->segments[sid],docid,mem_pool);
	
	return ret;
}


struct low_data_struct* table_data_query_col(struct table_manager* table,uint16_t sid,char* field_name,uint32_t docid,MEM_POOL* mem_pool)
{
	struct low_data_struct* ret = NULL;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	/*ͨ���εı�־���ж�*/
	//��ȡmeta data
	if(sid >= table->max_segment_num || !(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
		 return NULL;

	//���������filter�����Ļ������Բ�ȥ���̲�
	enum field_types data_type;
	if(check_index(table,field_name,HI_KEY_ALG_FILTER,&data_type) == 1 && data_type != HI_TYPE_STRING)
	{
		ret = segment_index_value_query(table->segments[sid],field_name,docid,mem_pool);
		
		//������ǿ�ֵ�Ļ�����˵���Ѿ��鵽��
		if(ret->len != 0)
		{
			ret->type = data_type;
			ret->field_name = field_name;
			return ret;
		}
	}

	if(!table->store_raw)
		return ret;
	
	ret = segment_data_query_col(table->segments[sid],field_name,docid,mem_pool);

	return ret;
}



int32_t check_index(struct table_manager* table,char* field_name,enum index_key_alg index_type,enum field_types* data_types)
{
	//��������Ƿ�֧��filter����
	PROFILER_BEGIN("check_index");
	struct index_field_meta* index_meta =(struct index_field_meta*)string_map_get(table->index_meta_hash, field_name);
	if(index_meta == NULL)
	{
		log_debug("%s �У�û�н� %d ����",field_name,index_type);
		PROFILER_END();
		return 0;
	}

	uint8_t i;
	for(i=0; i<index_meta->index_count; i++)
	{
		if(index_meta->indexs[i].index_type == index_type && index_meta->indexs[i].flag == 1)
		{
			if(data_types != NULL)
				*data_types = index_meta->indexs[i].data_type;

			PROFILER_END();
			return 1;
		}
	}

	log_debug("%s �У�û�н� %d ����",field_name,index_type);
	PROFILER_END();
	return 0;
}

struct low_data_struct* table_index_value_query(struct table_manager* table,uint16_t sid,char* field_name,uint32_t docid,MEM_POOL* mem_pool)
{
	struct low_data_struct* ret = NULL;
	
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	/*ͨ���εı�־���ж�*/
	//��ȡmeta data
	if(sid >= table->max_segment_num || !(Mile_AtomicGetPtr(&(table->segment_meta+sid)->flag)&SEGMENT_INIT))
		 return NULL;

	enum field_types data_type;
	if(!check_index(table,field_name,HI_KEY_ALG_FILTER,&data_type))
		return NULL;

	ret = segment_index_value_query(table->segments[sid],field_name,docid,mem_pool);

	//��ȫԪ������Ϣ
	if(ret != NULL)
	{
		//������Ҫ��һ��ת����������ַ����Ļ�����ת��ΪLONGLONG
		if(data_type == HI_TYPE_STRING)
			ret->type = HI_TYPE_LONGLONG;
		else
			ret->type = data_type;
		
		ret->field_name = field_name;
	}
	
	return ret;
}








struct list_head* table_seghint_query(struct table_manager* table, struct hint_array* time_cond, MEM_POOL* mem_pool)
{
	uint16_t i;
	struct list_head* rowids_list_h;
	struct segment_query_rowids* node;
	struct segment_meta_data* meta_data;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	//��ʼ��ͷ
	rowids_list_h = (struct list_head*)mem_pool_malloc(mem_pool,sizeof(struct list_head));
	memset(rowids_list_h,0,sizeof(struct list_head));
	
	INIT_LIST_HEAD(rowids_list_h);

	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;


		if(!time_cond || !(Mile_AtomicGetPtr(&table->segments[i]->meta_data->modify_time) <= time_cond->hints[0] || 
			Mile_AtomicGetPtr(&table->segments[i]->meta_data->create_time) >= time_cond->hints[1]))
		{
			//�鵽�������ӵ��б���
			node = (struct segment_query_rowids*)mem_pool_malloc(mem_pool,sizeof(struct segment_query_rowids));
			node->rowids = NULL;
			node->sid = i;
			node->max_docid = meta_data->row_count;
			INIT_LIST_HEAD(&node->rowids_list);
			list_add(&node->rowids_list,rowids_list_h);
			continue;
		}
		
	}

	return rowids_list_h;
}



uint32_t table_index_count_query(struct table_manager* table, 
							     struct list_head* seg_list,
								 struct low_data_struct* data,
								 MEM_POOL * mem_pool)
{
	uint32_t ret = 0;
	struct segment_query_rowids *p;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	if(!check_index(table,data->field_name,HI_KEY_ALG_FULLTEXT,NULL))
		return 0;

	list_for_each_entry(p, seg_list, rowids_list)
	{

		//�Զ�ִ����Ӧ�Ĳ���
		ret += segment_index_count_query(table->segments[p->sid],data,mem_pool);
	
	}


	return ret;
}

static struct list_head* index_equal_query(struct table_manager* table,
										   struct list_head* seg_list,
										   struct low_data_struct* data,
										   enum index_key_alg index_type,
										   MEM_POOL* mem_pool)
{
	struct segment_query_rowids *p;
	struct list_head* rowids_list_h;
	struct segment_query_rowids* node;
	struct rowid_list* rowids;
	struct segment_meta_data* meta_data;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	if(!check_index(table,data->field_name,index_type,NULL))
		return NULL;

	//��ʼ��ͷ
	rowids_list_h = (struct list_head*)mem_pool_malloc(mem_pool,sizeof(struct list_head));
	memset(rowids_list_h,0,sizeof(struct list_head));
	
	INIT_LIST_HEAD(rowids_list_h);



	list_for_each_entry(p, seg_list, rowids_list)
	{

		//�Զ�ִ����Ӧ�Ĳ���
		rowids = segment_index_equal_query(table->segments[p->sid],data,index_type,mem_pool);
	
		//�鵽�������ӵ��б���
		node = (struct segment_query_rowids*)mem_pool_malloc(mem_pool,sizeof(struct segment_query_rowids));
		node->rowids = rowids;
		node->sid = p->sid;
		node->max_docid = p->max_docid;
		INIT_LIST_HEAD(&node->rowids_list);
		list_add(&node->rowids_list,rowids_list_h);
	}


	return rowids_list_h;
}



struct list_head* table_fulltext_index_equal_query(struct table_manager* table,
											   	   struct list_head* seg_list,
										           struct low_data_struct* data,
										           MEM_POOL* mem_pool)
{
	return index_equal_query(table,seg_list,data,HI_KEY_ALG_FULLTEXT,mem_pool);
}



//������״̬ΪSEGMENT_INIT�Ķν��в�ѯ�����ԶεĴ���ʱ����޸�ʱ����й���
struct list_head* table_index_equal_query(struct table_manager* table,
										  struct list_head* seg_list,
										  struct low_data_struct* data,
										  MEM_POOL* mem_pool)
{
	return index_equal_query(table,seg_list,data,HI_KEY_ALG_HASH,mem_pool);
}





/*-----------------------------------------------------------------------------
 *  ��btree����, ��ָ���ı�����кţ����з�Χ��ѯ�����ж�ɨ
 *-----------------------------------------------------------------------------*/
struct list_head* table_index_range_query(struct table_manager* table, char* field_name, \
		struct hint_array* time_cond, struct db_range_query_condition * range_condition, MEM_POOL* mem_pool)
{
	uint16_t i;
	struct list_head * rowids_list_h;
	struct rowid_list * rowids;
	struct segment_query_rowids * seg_node;
	struct segment_meta_data* meta_data;


	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	if(!check_index(table,field_name,HI_KEY_ALG_BTREE,NULL))
		return NULL;
	

	//��ʼ��ͷ
	rowids_list_h = (struct list_head*)mem_pool_malloc(mem_pool, sizeof(struct list_head));
	memset(rowids_list_h, 0, sizeof(struct list_head));

	INIT_LIST_HEAD(rowids_list_h);

	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;

	for ( i = 0; i < table->max_segment_num; i += 1,meta_data++)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;
		
		if(!(Mile_AtomicGetPtr(&table->segments[i]->meta_data->modify_time) <= time_cond->hints[0] || 
			Mile_AtomicGetPtr(&table->segments[i]->meta_data->create_time) >= time_cond->hints[1]))

		{
			rowids = segment_index_range_query(table->segments[i], field_name, range_condition, mem_pool);

			//�鵽�������ӵ��б���
			seg_node = (struct segment_query_rowids*)mem_pool_malloc(mem_pool, sizeof(struct segment_query_rowids));
			seg_node->rowids = rowids;
			seg_node->sid = i;
			INIT_LIST_HEAD(&seg_node->rowids_list);
			list_add(&seg_node->rowids_list, rowids_list_h);
		}
	}

	return rowids_list_h;

}





int64_t table_get_record_num(struct table_manager* table, struct list_head* seg_list)
{
	struct segment_meta_data* meta_data;
	struct segment_query_rowids *p;
	int64_t record_num;


	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;
	record_num = 0;

	list_for_each_entry(p, seg_list, rowids_list)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data[p->sid].flag)&SEGMENT_INIT))
			continue;
		record_num += Mile_AtomicGetPtr(&meta_data[p->sid].row_count)-Mile_AtomicGetPtr(&meta_data[p->sid].del_count);
	}


	return record_num;
}





int64_t table_get_delete_num(struct table_manager* table, struct list_head* seg_list)
{
	struct segment_meta_data* meta_data;
	struct segment_query_rowids *p;
	int64_t delete_num;


	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;
	delete_num = 0;

	list_for_each_entry(p, seg_list, rowids_list)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data[p->sid].flag)&SEGMENT_INIT))
			continue;
		delete_num += Mile_AtomicGetPtr(&meta_data[p->sid].del_count);
	}


	return delete_num;
}





int32_t table_del_docid(struct table_manager* table,uint16_t sid,uint32_t docid)
{	int32_t ret;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	/*ͨ���εı�־���ж�*/
	//��ȡmeta data
	if(!(Mile_AtomicGetPtr(&(table->segment_meta + sid)->flag)&SEGMENT_INIT))
		return ERROR_SEGMENT_NOT_INIT;

	ret = segment_del_docid(table->segments[sid],docid);
	return ret;
}


int32_t table_is_docid_deleted(struct table_manager* table,uint16_t sid,uint32_t docid)
{
	int32_t ret;
	
	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	/*�ж϶κ���û������Χ*/
	if(!(Mile_AtomicGetPtr(&(table->segment_meta + sid)->flag)&SEGMENT_INIT))
		return ERROR_SEGMENT_NOT_INIT;

	ret = segment_is_docid_deleted(table->segments[sid],docid);
	return ret;
}



int32_t table_set_segment_current(struct table_manager* table, uint16_t sid)
{
	if(table_lock(table) != MILE_RETURN_SUCCESS)
	{
		return ERROR_LOCK_FAILED;
	}
	
	table->table_meta->segment_current = sid;
	table_unlock(table);
	return MILE_RETURN_SUCCESS;
}

int32_t table_lock(struct table_manager* table)
{
	if(pthread_mutex_lock(&table->write_protect_locker) != 0)
	{
		log_error("��д��ʧ��");
		return ERROR_LOCK_FAILED;
	}

	return MILE_RETURN_SUCCESS;
}

 
void table_unlock(struct table_manager* table)
{
	pthread_mutex_unlock(&table->write_protect_locker);
}


void table_read_lock(struct table_manager* table)
{
	pthread_rwlock_rdlock(&table->read_protect_locker);
}


void table_read_unlock(struct table_manager* table)
{
	pthread_rwlock_unlock(&table->read_protect_locker);
}


int32_t table_del(struct table_manager* table)
{
	Mile_AtomicOrPtr(&table->table_meta->stat,TABLE_DEL);
	return 0;
}


void print_query_rowids(struct list_head* rowids_list_h)
{
	struct segment_query_rowids* node;

	/*����*/
	list_for_each_entry(node,rowids_list_h,rowids_list){
			fprintf(stdout,"sid:%u rowid:",node->sid);
			print_rowid_list(node->rowids);
	}	
}


void table_release(struct table_manager* table)
{
	uint16_t i;
	struct segment_meta_data* meta_data;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return;
	}

	//�ͷŸ�����
	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{	
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;
		
		segment_release(table->segments[i]);
	}

	msync(table->segment_meta,SEGMENT_RUNTIME_SIZE(table),MS_SYNC);        // make sure synced
	munmap(table->segment_meta, SEGMENT_RUNTIME_SIZE(table));

	pthread_mutex_destroy(&table->write_protect_locker);
	pthread_rwlock_destroy(&table->read_protect_locker);
	
	return;
}



int32_t table_compress(struct table_manager* table,MEM_POOL* mem_pool)
{
	int32_t ret;
	uint16_t i;
	struct segment_meta_data* meta_data;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}
	
	ret = MILE_RETURN_SUCCESS;
	//�ͷŸ�����
	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;

	for(i=0; i<table->max_segment_num; i++,meta_data++)
	{
		//�鿴�ö��Ƿ��ѱ�ѹ��
		if(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_COMPRESS)
		{
			continue;
		}
				
		//�鿴�ö��Ƿ��ڴ�ѹ��״̬
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_FULL))
		{
			continue;
		}
		
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
		{
			continue;
		}
		
		log_info("segment %u ��ʼѹ�� ��ǰ�� :%u",i,Mile_AtomicGetPtr(&table->table_meta->segment_current));

		//ѹ����ʱ��Ӷ���
		pthread_rwlock_rdlock(&table->read_protect_locker);
		ret = segment_compress(table->segments[i],mem_pool);
		pthread_rwlock_unlock(&table->read_protect_locker);
		
	
		if(ret != MILE_RETURN_SUCCESS)
		{
			log_error("segment %u ѹ��ʧ�� ret:%d",i,ret);
			break;
		}

		log_info("segment %u ѹ����� ret:%d",i,ret);

		//�л���ʱ���д��
		pthread_rwlock_wrlock(&table->read_protect_locker);
		
		ret = segment_compress_switch(table->segments[i]);
		//���öμ�Ϊ��ѹ��
		Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_COMPRESS);
		
		pthread_rwlock_unlock(&table->read_protect_locker);
	}
	
	return ret;
}



void table_checkpoint(struct table_manager* table)
{
	uint16_t i;
	struct segment_meta_data* meta_data;

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return;
	}
	
	//�ͷŸ�����
	//��ȡԪ������Ϣ
	meta_data = table->segment_meta;
	
	//��checkpoint��ʱ����Ҫ�Ӷ���
	pthread_rwlock_rdlock(&table->read_protect_locker);
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{	
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;


		segment_checkpoint(table->segments[i]);
	}
	pthread_rwlock_unlock(&table->read_protect_locker);

	msync(table->segment_meta,SEGMENT_RUNTIME_SIZE(table),MS_SYNC);        // make sure synced
	return;
}




int32_t table_load_segment(struct table_manager* table,int16_t sid,char* segment_dir,MEM_POOL* mem_pool)
{
	struct segment_meta_data* old_meta_data;
	struct segment_meta_data* new_meta_data;
	void* mem_mmaped = NULL;
	int32_t ret = MILE_RETURN_SUCCESS;
	MEM_POOL* mem_pool_local = (MEM_POOL*)mem_pool_init(MB_SIZE);

	do
	{

		log_info("��ʼ���м��ض� ...");

		/*--------------��ȡ�ε�Ԫ������Ϣ-----------------*/
		//��ȡ�ϵĶε�Ԫ������Ϣ�����ж�
		old_meta_data = table->segment_meta + sid;
		if((Mile_AtomicGetPtr(&(old_meta_data)->flag)&SEGMENT_INIT))
		{
			log_error("�˿��ѱ�ռ:%u",sid);
			ret = ERROR_SEGMENT_IS_TAKEN;
			break;
		}

		log_info("��ȡ�����ص�Ŀ�Ķκ� %u",sid);

		//��ȡ�µĶε�Ԫ������Ϣ
		char meta_file_name[FILENAME_MAX_LENGTH];
		memset(meta_file_name,0,sizeof(meta_file_name));
		sprintf(meta_file_name,"%s/meta.dat",segment_dir);

		//mmapӳ�䴦��
		new_meta_data =(struct segment_meta_data*)get_mmap_memory(meta_file_name,sizeof(struct segment_meta_data)); 
		if(new_meta_data == NULL){
			log_error("map file failed: %s", strerror(errno));
			ret = ERROR_FILE_OP_FAILED;
			break;
		}

		log_info("���ضε�Ԫ������Ϣ ctime:%lu mtime:%lu ptime:%lu flag:%u rowcount:%u delcount:%u",new_meta_data->create_time,
    																								new_meta_data->modify_time,
    																								new_meta_data->checkpoint_time,
    																								new_meta_data->flag,
    																								new_meta_data->row_count,
    																								new_meta_data->del_count);
		
		/*-----------���û�ָ���Ķ�Ŀ¼����������ǰtable����Ŀ¼����������--------------------*/

		/*-----------------��ʼ����-----------------*/
		struct segment_config seg_config;
		memset(&seg_config,0,sizeof(struct segment_config));
		
		/*---------------------���������в�ȫ--------------------------*/
		
		char index_meta_filename[FILENAME_MAX_LENGTH];
		memset(index_meta_filename,0,sizeof(index_meta_filename));
		sprintf(index_meta_filename,"%s/index.dat",segment_dir);

		mem_mmaped = get_mmap_memory(index_meta_filename,sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM+ sizeof(uint16_t)); 
		if(mem_mmaped == NULL)
		{
			log_error("map file failed: %s", strerror(errno));
			ret = ERROR_FILE_OP_FAILED;
			break;
		}
		char segment_work_space[FILENAME_MAX_LENGTH];
		sprintf(seg_config.segment_name,"%s_segment_%06u",table->table_meta->table_name,sid);
		sprintf(segment_work_space,"%s/%s",table->work_space,seg_config.segment_name);
		seg_config.meta_data = old_meta_data;
		seg_config.row_limit = table->row_limit;
		seg_config.hash_compress_num = table->hash_compress_num;
		seg_config.index_field_count = *(uint16_t*)mem_mmaped;  /*��ʼ��ʱ����������*/
		seg_config.index_fields = (struct index_field_meta*)((char *)mem_mmaped + sizeof(uint16_t));
		strcpy(seg_config.work_space,table->work_space);

		
		memset(segment_work_space,0,sizeof(segment_work_space));
		sprintf(segment_work_space,"%s/%s",table->work_space,seg_config.segment_name);

		log_info("��������Ŀ¼ old:%s new:%s",segment_dir,segment_work_space);
		if(rename(segment_dir,segment_work_space) < 0)
		{
			log_error("rename file [%s] failed",strerror(errno));
			ret = -1;
			break;
		}

		
		if((table->segments[sid] = segment_init(&seg_config,mem_pool)) == NULL)
		{
			ret = ERROR_SEGMENT_INIT_FAILED;
			break;
		}
		log_info("��ʼ���ε����ݽṹ");

		uint16_t i;
		uint8_t j;

		
		struct index_field_meta* index_meta = table->table_meta->index_meta;
		struct index_field_manager* index_field = NULL;
		struct low_data_struct* data = NULL;
		uint32_t docid;

		log_info("��ʼ��ȫ���� ...");
		
		//�����Ȼ�Ƕε�����Ԫ���ݽ��з��ʣ����Ǹĵû���ֻ�����½������ĵط��޸ģ��ϲ����ƣ�load unload���½�������ѹ������������
		//ͬʱ����
		for(i=0; i<table->table_meta->index_field_count; i++,index_meta++)
		{
			for(j=0; j<index_meta->index_count; j++)
			{
				mem_pool_reset(mem_pool_local);
				
				index_field = segment_get_index_instance(table->segments[sid],index_meta->field_name,index_meta->indexs[j].index_type);

				log_info("��ǰ��������Ϣ field name:%s index type:%u data type:%u flag:%u",index_meta->field_name,
																						index_meta->indexs[j].index_type,
																						index_meta->indexs[j].data_type,
																						index_meta->indexs[j].flag);
				
				//����ȱʧ
				if(index_field == NULL)
				{
					log_info("����ȱʧ���������� ...");

					//���ֻ���sid����������ṹ�����޸ģ�����ʱ���sid�ı�ʶδ����ΪINIT�����Բ������̰߳�ȫ����
					segment_ensure_index(table->segments[sid],index_meta->field_name,index_meta->indexs[j].index_type,table->mem_pool);

					for(docid = 0; docid<table->row_limit; docid++)
					{
						//��ѯԭʼ������
						data = segment_data_query_col(table->segments[sid],index_meta->field_name,docid,mem_pool_local);

						//���뵽������
						if(data != NULL)
						{
							ret = segment_index_insert(table->segments[sid],data,index_meta->indexs[j].index_type,docid,mem_pool_local);
							if(ret < 0)
							{
								log_error("%s %d��������ʧ�� ret %d",index_meta->field_name,index_meta->indexs[j].index_type,ret);
								continue;
							}
						}
					}

				}
			}

		}


		//���α�ʶ��ΪINIT
		log_info("������ȫ��ϣ�����%u��ʶΪINIT״̬",sid);
		
		//bugfix ��load�ε�ʱ����segment��Ϣ��δ��ʼ���ã���ֱ�ӽ�meta��Ϣ�����������̰߳�ȫ by yunliang.shi 2011-12-10
		memcpy(old_meta_data,new_meta_data,sizeof(struct segment_meta_data));
		Mile_AtomicAndPtr(&table->segments[sid]->meta_data->flag,~SEGMENT_DUMP);
		Mile_AtomicOrPtr(&table->segments[sid]->meta_data->flag,SEGMENT_INIT);

	}while(0);

	munmap(mem_mmaped, sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM+ sizeof(uint16_t));
	munmap(new_meta_data, sizeof(struct segment_meta_data));
	mem_pool_destroy(mem_pool_local);
	return MILE_RETURN_SUCCESS;
}


static int32_t traversal_dir_func(char* dir,void* arg)
{
	uint16_t i;
	
	struct table_manager* table = (struct table_manager*) arg;
	struct segment_meta_data* meta_data = table->segment_meta;

	for(i=0;i < table->max_segment_num;i++, meta_data++)
	{
		//����ö�δ��ʼ�������ҵ�
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			break;

	}
	if(i == table->max_segment_num)
	{
		log_error("���еĶ��Ѿ���������");
		return ERROR_EXCEED_SEGMENT_NUM;
	}

	return table_load_segment(table,i,dir,table->mem_pool);
}



int32_t table_replace_all_segments(struct table_manager* table,char* segments_dir,MEM_POOL* mem_pool)
{
	struct segment_meta_data* meta_data;
	uint16_t i;
	int32_t ret;
	
	//�ȼ�д����������
	pthread_rwlock_wrlock(&table->read_protect_locker);

	//ж�����еĶ�
	meta_data = table->segment_meta;
	for(i=0;i<table->max_segment_num;i++,meta_data++)
	{
		//����ö�δ��ʼ��������Ҫ��ȥ��ѯ
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
			continue;
	
		ret = table_unload_segment(table,i,0);
		if(ret < 0)
		{
			log_error("ж�ض�ʧ�� %s  %u",table->table_meta->table_name,i);
			goto RET;
		}

	}

	if(segments_dir != NULL)
		//����segments_dirָ��Ŀ¼�����еĶ�
		ret = traversal_single_deep_childdir(segments_dir,traversal_dir_func,(void*)table);


	RET:
		pthread_rwlock_unlock(&table->read_protect_locker);
		return ret;
}




//�����������еĶΣ����ñ��ΪIS_SEGMENT_DUMP���ѹ��ڵĶ��ƶ���ָ����Ŀ¼�£�����runtime.dat���Ƶ�ָ����Ŀ¼��
int32_t table_unload_segment(struct table_manager* table,int16_t sid,uint8_t thread_safe)
{
	int32_t ret = MILE_RETURN_SUCCESS;
	struct segment_manager* segment;
	struct segment_meta_data* meta_data;

	do
	{
		log_info("��ʼ���ж�ж�� ...");
		
		if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
		{
			log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
			return ERROR_TABLE_DELETED;
			break;
		}

		/*-----------����ͨ��meta�鿴�˶ε�״̬---------------*/
		meta_data = table->segment_meta + sid;
		
		//����ö�δ��ʼ������ʧ��
		if(!(Mile_AtomicGetPtr(&meta_data->flag)&SEGMENT_INIT))
		{
			log_error("dump�Ķ�:%u δ��ʼ��:%d",sid,meta_data->flag);
			ret = ERROR_SEGMENT_NOT_INIT;
		}

		segment = table->segments[sid];

		//��̫���ܷ���
		if(segment == NULL)
		{
			log_error("��δ��ʼ��");
			ret = ERROR_SEGMENT_NOT_INIT;
			break;
		}

		/*------���ε�Ԫ������Ϣд�뵽�ε�Ŀ¼��-----------*/
		char meta_file_name[FILENAME_MAX_LENGTH];
		void* meta_memaped = NULL;
		memset(meta_file_name,0,sizeof(meta_file_name));
		sprintf(meta_file_name,"%s/meta.dat",segment->work_space);

		log_info("�ε�Ԫ������Ϣ���������� ctime:%lu mtime:%lu ptime:%lu flag:%u rowcount:%u delcount:%u",   meta_data->create_time,
  																										     meta_data->modify_time,
  																										     meta_data->checkpoint_time,
  																										     meta_data->flag,
  																										     meta_data->row_count,
  																										     meta_data->del_count);

		//mmapӳ�䴦��
		meta_memaped = get_mmap_memory(meta_file_name,sizeof(struct segment_meta_data)); 
		if(meta_memaped == NULL){
			log_error("map file failed: %s", strerror(errno));
			ret = ERROR_FILE_OP_FAILED;
			break;
		}

		//���ñ��������������Ŀ¼��flush��������
		Mile_AtomicOrPtr(&meta_data->flag,SEGMENT_DUMP);
		log_info("���α��Ϊ SEGMENT_DUMP flag:%d",meta_data->flag);

		//��������
		memcpy(meta_memaped,meta_data,sizeof(struct segment_meta_data));

		//flush��������
		msync(meta_memaped,sizeof(struct segment_meta_data),MS_SYNC);        // make sure synced
	   	munmap(meta_memaped,sizeof(struct segment_meta_data));	


		/*------���ε�ǰ���������������ڣ���Ϊ��ǰ�϶���û�н������������ģ����Բ��ÿ���string_map��������segment�ѽ���������һ����index_metaһ��*/
		char index_file_name[FILENAME_MAX_LENGTH];
		memset(index_file_name,0,sizeof(index_file_name));
		sprintf(index_file_name,"%s/index.dat",segment->work_space);

		log_info("����ǰ������Ϣ���������� ...");

		//������Ϣ����
		meta_memaped = get_mmap_memory(index_file_name,sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM+ sizeof(uint16_t)); 
		if(meta_memaped == NULL){
			log_error("map file failed: %s", strerror(errno));
			ret = ERROR_FILE_OP_FAILED;
			break;
		}
		
		*(uint16_t*)meta_memaped = table->table_meta->index_field_count;
		memcpy((char *)meta_memaped + sizeof(uint16_t), table->table_meta->index_meta, sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM);

		uint16_t i;
		uint8_t j;
		struct index_field_meta* index_meta = table->table_meta->index_meta;
		for(i=0; i<table->table_meta->index_field_count; i++,index_meta++)
		{
			for(j=0; j<index_meta->index_count; j++)
			{
				log_info("��ǰ��������Ϣ field name:%s index type:%u data type:%u flag:%u", index_meta->field_name,
																							index_meta->indexs[j].index_type,
																							index_meta->indexs[j].data_type,
																							index_meta->indexs[j].flag);
			}
		}

		msync(meta_memaped,sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM+ sizeof(uint16_t),MS_SYNC);        // make sure synced
	   	munmap(meta_memaped, sizeof(struct index_field_meta)*MAX_INDEX_FIELD_NUM+ sizeof(uint16_t));	


		/*---------------�ͷŸö�----------------------*/
		//��д��
		if(thread_safe)
			pthread_rwlock_wrlock(&table->read_protect_locker);
		log_info("����Ԫ������Ϣ���");
			
		//��նε�Ԫ������Ϣ
		memset(segment->meta_data,0,sizeof(struct segment_meta_data));
	    segment_release(segment);
		table->segments[sid] = NULL;
		if(thread_safe)
			pthread_rwlock_unlock(&table->read_protect_locker);

		/*-----------------�������εĹ���Ŀ¼------------------------*/
		char segment_file_name[FILENAME_MAX_LENGTH];
		memset(segment_file_name,0,sizeof(segment_file_name));

		sprintf(segment_file_name,"%s/%s_dump",table->work_space,segment->segment_name);


		log_info("������Ŀ¼ old:%s new:%s",segment->work_space,segment_file_name);
		
		if(rename(segment->work_space,segment_file_name) < 0)
		{
			log_error("rename file [%s] failed",strerror(errno));
			ret = ERROR_FILE_OP_FAILED;
			break;
		}

	}while(0);

	return ret;
}





struct segment_meta_data* table_query_segment_stat(struct table_manager* table,uint16_t* max_segment_num,MEM_POOL* mem_pool)
{
	struct segment_meta_data* segment_meta = (struct segment_meta_data*)mem_pool_malloc(mem_pool,table->max_segment_num * sizeof(struct segment_meta_data));
	memset(segment_meta,0,table->max_segment_num * sizeof(struct segment_meta_data));

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}

	//��ȡԪ������Ϣ
	memcpy(segment_meta,table->segment_meta,table->max_segment_num * sizeof(struct segment_meta_data));
	
	*max_segment_num = table->max_segment_num;

	return segment_meta;
}



struct index_field_meta* table_query_index_stat(struct table_manager* table,uint16_t* index_field_count,MEM_POOL* mem_pool)
{
	struct index_field_meta* index_meta = (struct index_field_meta*)mem_pool_malloc(mem_pool,table->table_meta->index_field_count * sizeof(struct index_field_meta));
	memset(index_meta,0,table->table_meta->index_field_count * sizeof(struct index_field_meta));

	if(Mile_AtomicGetPtr(&table->table_meta->stat)&TABLE_DEL)
	{
		log_warn("�ñ���ɾ����tablename:%s",table->table_meta->table_name);
		return NULL;
	}


	//��ȡ����Ԫ������Ϣ
	pthread_rwlock_rdlock(&table->read_protect_locker);
	memcpy(index_meta,table->table_meta->index_meta,table->table_meta->index_field_count * sizeof(struct index_field_meta));
	*index_field_count = table->table_meta->index_field_count;
	pthread_rwlock_unlock(&table->read_protect_locker);
	return index_meta;
}

int table_mmap_switch( struct table_manager *table, uint16_t sid)
{
	if( Mile_AtomicGetPtr(&table->table_meta->stat) & TABLE_DEL) {
		log_warn("table deleted, tablename:%s",table->table_meta->table_name);
		return ERROR_TABLE_DELETED;
	}

	int ret;

	table_read_lock( table );
	ret = segment_mmap_switch( table->segments[sid] );
	table_read_unlock( table );

	return ret;
}



