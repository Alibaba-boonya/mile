#ifndef USE_MEM_COMPRESS

#include "hashcompress.h"


//���ȫ�ֱ����ǱƲ����ѣ���Ҫʹ��qsort�����ܲ�����ѹ��
struct hash_bucket* global_bucket = NULL;


struct hash_compress_manager* hash_commpress_init(struct hash_compress_config* config,MEM_POOL* mem_pool)
{
	struct hash_compress_manager* hash_compress = 
		(struct hash_compress_manager*)mem_pool_malloc(mem_pool,sizeof(struct hash_compress_manager));
	void* mmap = NULL;
	memset(hash_compress,0,sizeof(struct hash_compress_manager));

	hash_compress->row_limit = config->row_limit;

	/*��Ҫ���Ͽ�ֵ���Ǹ�ָ����Ͱ*/
	hash_compress->hash_mod = config->row_limit+1;
	hash_compress->hash_compress_num = config->hash_compress_num;

	sprintf(hash_compress->data_file_name,"%s/hash_compress.dat",config->work_space);
	sprintf(hash_compress->index_file_name,"%s/hash_compress.idx",config->work_space);
	
	mmap = 
		get_mmap_memory(hash_compress->index_file_name,HASH_SEEKS_MEMAP_SIZE(hash_compress));
	
	if(mmap == NULL)
		return NULL;
	
	hash_compress->index_count = (uint32_t*)mmap;
	hash_compress->index_mmap = mmap+sizeof(uint32_t);

	//�������ļ�
	hash_compress->data_fd = open_file(hash_compress->data_file_name,O_RDWR|O_CREAT|O_APPEND);
	if(hash_compress->data_fd < 0)
		return NULL;

	
	return hash_compress;
}


static int hash_index_compare(const void *p1, const void *p2)
{
	if((global_bucket + *(uint32_t*)p1)->hash_value > (global_bucket + *(uint32_t*)p2)->hash_value)
		return 1;
	else if((global_bucket + *(uint32_t*)p1)->hash_value < (global_bucket + *(uint32_t*)p2)->hash_value)
		return -1;
	else 
		return 0;
}



struct hash_compress_manager* hash_compress_load(struct hash_index_manager* hash_index,uint32_t hash_compress_num,MEM_POOL* mem_pool)
{
	//��ʼ��segment_hash_compress�ṹ
	struct hash_compress_config config;

	if(hash_index == NULL)
	{
		log_error("hash ��Ϊ��");
		return NULL;
	}
	
	memset(&config,0,sizeof(struct hash_compress_config));
	config.row_limit = hash_index->limit;
	config.hash_compress_num = hash_compress_num;
	strncpy(config.work_space,hash_index->file_name,strrchr(hash_index->file_name,'/')-hash_index->file_name);

	struct hash_compress_manager* hash_compress = hash_commpress_init(&config,mem_pool);
	if(!hash_compress)
	{
		log_error("��ʼ���ṹ��ʧ��");
		remove(hash_compress->data_file_name);
		remove(hash_compress->index_file_name);
		return NULL;
	}


	uint32_t* hash_arrays = (uint32_t*)malloc(hash_compress->hash_mod*sizeof(uint32_t));

	uint32_t i;
	for(i=0;i<hash_compress->hash_mod;i++)
	{
		hash_arrays[i] = i;
	}

	//��hash_value��������
	struct hash_bucket* bucket = hash_index->mem_mmaped;
	global_bucket = hash_index->mem_mmaped;
	log_info("%s begin qsort...",hash_compress->index_file_name);
	qsort(hash_arrays,hash_index->hashmod,sizeof(uint32_t),hash_index_compare);
	log_info("%s end qsort...",hash_compress->index_file_name);

	//��ʼÿ10��һ��д�����
	MEM_POOL* mem_pool_local = mem_pool_init(MB_SIZE);
	struct rowid_list* docids = NULL;
	uint32_t docid_buf[BUF_NUM];
	uint32_t count = 0;
	uint8_t is_write = 0;
	uint32_t offset = 0;
	uint32_t len = 0;
	uint32_t j,k,compress_num;
	compress_num = 0;
	
	struct rowid_list_node* p;
	log_info("%s begin write data ...",hash_compress->data_file_name);
	for(j=0; j<hash_compress->hash_mod; j++)
	{
		count = 0;

		if((bucket+hash_arrays[j])->hash_value == 0)
			continue;

		docids = get_rowid_list(hash_index,NEXT_DOC_ROW_STRUCT(hash_index->doclist,(bucket + hash_arrays[j])->offset),mem_pool_local);
		
		docid_buf[count++] = (bucket + hash_arrays[j])->hash_value & 0xffffffff;
		docid_buf[count++] = ((bucket + hash_arrays[j])->hash_value >> 32) & 0xffffffff;
		

		docid_buf[count++] = docids->rowid_num;

		for(k = 0, p = docids->head; k < docids->rowid_num; k++)
		{
			if(k != 0 && k%ROWID_ARRAY_SIZE == 0)
			{
				p = p->next;
			}
			docid_buf[count++] = p->rowid_array[k%ROWID_ARRAY_SIZE];
			is_write = 0;
			
			if(count == BUF_NUM)
			{
						//�ļ��洢
	 			if(write(hash_compress->data_fd, docid_buf, count*sizeof(uint32_t)) != count*sizeof(uint32_t))
	 			{
					log_warn("д�ļ�ʧ��:%s",hash_compress->data_file_name);
					goto FAIL;
	 			}
	  		 	
				count = 0;
				is_write = 1;
			}
		}

		if(!is_write)
		{
			//�ļ��洢
 			if(write(hash_compress->data_fd, docid_buf, count*sizeof(uint32_t)) != count*sizeof(uint32_t))
 			{
 				log_error("д�ļ�ʧ��");
				goto FAIL;
 			}
		}
		

		if(compress_num%hash_compress->hash_compress_num == 0)
		{	
			offset += len;
			(*hash_compress->index_count)++;
			(hash_compress->index_mmap+compress_num/hash_compress->hash_compress_num)->hash_value = (bucket + hash_arrays[j])->hash_value;
			
			(hash_compress->index_mmap+compress_num/hash_compress->hash_compress_num)->offset = offset;
			if(compress_num/hash_compress->hash_compress_num != 0)
			{
				(hash_compress->index_mmap+compress_num/hash_compress->hash_compress_num-1)->len = len;
				 len = 0; 
			}
		}

		len += sizeof(uint64_t)+(1+docids->rowid_num)*sizeof(uint32_t);
		compress_num++;

		mem_pool_reset(mem_pool_local);
	}
	log_info("%s end write data ..",hash_compress->data_file_name);

	//���һ���������ݳ���
	if(compress_num != 0)
		(hash_compress->index_mmap+(compress_num-1)/hash_compress->hash_compress_num)->len = len;
	
	//��hash_seek����msync��������
	msync(hash_compress->index_mmap,(hash_compress->hash_mod/hash_compress->hash_compress_num)*sizeof(struct hash_seeks),MS_SYNC); 

	//Ŀǰ�Ѿ�֪�������ж���Ͱ��ֵ�ˣ����Խ�mmap�Ĵ�С��Ϊcompress_num/hash_compress->hash_compress_num+1
	void* mmap = NULL;
	munmap(hash_compress->index_mmap,HASH_SEEKS_MEMAP_SIZE(hash_compress));
	mmap = 
		get_mmap_memory(hash_compress->index_file_name,(compress_num/hash_compress->hash_compress_num+1)*sizeof(struct hash_seeks)+sizeof(uint32_t));

	log_info("����ӳ���ڴ��С��ԭ����Ҫ%zu�ֽ� Ŀǰ��Ҫ%zu�ֽ�",HASH_SEEKS_MEMAP_SIZE(hash_compress),(compress_num/hash_compress->hash_compress_num+1)*sizeof(struct hash_seeks)+sizeof(uint32_t));

	if(mmap == NULL)
	{
		log_error("ӳ���ļ�ʧ��");
		goto FAIL;
	}

	//���¸�ֵ
	hash_compress->index_count = (uint32_t*)mmap;
	hash_compress->index_mmap = mmap+sizeof(uint32_t);
	
	fsync(hash_compress->data_fd);
	free(hash_arrays);
	mem_pool_destroy(mem_pool_local);
	return hash_compress;

	FAIL:
		free(hash_arrays);
		mem_pool_destroy(mem_pool_local);
		remove(hash_compress->data_file_name);
		remove(hash_compress->index_file_name);
		return NULL;
}



/*
����ط���һ��BUG����δ�鵽������unit_db_integrationʱ����
*/
//struct rowid_list* hash_compress_query(struct hash_compress_manager* hash_compress,struct low_data_struct* data,MEM_POOL* mem_pool)
{
	uint64_t hash_value;
	struct hash_seeks* hseek = NULL; 

	//����value��һ��hash
	if((hash_value = get_hash_value(data))<0)
		return NULL;
	
	//���ֲ��ҷ�
	 uint32_t left = 0;
     uint32_t right = *hash_compress->index_count-1;
     while (left < right) {
      uint32_t mid = (left + right + 1) / 2;
	  hseek = hash_compress->index_mmap+mid;
      if (hseek->hash_value <= hash_value) 
	  {
        left = mid;
      } else {
        right = mid - 1;
      }
    }
    
    struct hash_seeks key;
	key.hash_value = hash_value;

	hseek = hash_compress->index_mmap+left;

	void* buffer = malloc(hseek->len);

	//��ȡ�ļ�
	if(pread(hash_compress->data_fd, buffer, hseek->len, hseek->offset) != hseek->len)
	{
		log_error("��ȡIO�����⣬�ļ���:%s ����:%u ����:%s",hash_compress->data_file_name,hseek->len,strerror(errno));
		free(buffer);
		return NULL;
	}

	uint8_t i;
	struct hash_docs* hdocs = NULL;
	uint32_t offset = 0;
	for(i=0;i<hash_compress->hash_compress_num && offset <= hseek->len;i++)
	{
		hdocs = (struct hash_docs*)(buffer+offset);
		if(hdocs->hash_value == hash_value)
			break;
		
		offset += sizeof(struct hash_docs)+hdocs->num*sizeof(uint32_t);
	}

	struct rowid_list* rlist = rowid_list_init(mem_pool);
	if(i == hash_compress->hash_compress_num || offset > hseek->len)
	{
		log_warn("hseek %llu ��hashֵ������ %llu",hseek->hash_value,hash_value);
		free(buffer);
		return rlist;
	}

	rowid_list_batch_add(mem_pool,rlist,hdocs->docids,hdocs->num);
	free(buffer);
	return rlist;

}




void hash_compress_release(struct hash_compress_manager* hash_compress)
{
	if(hash_compress == NULL);
		return;
	
	//�ر��ļ�������
	close_file(hash_compress->data_fd);

	//unmap�����ڴ�
	munmap(hash_compress->index_mmap,HASH_SEEKS_MEMAP_SIZE(hash_compress));

	return;
}


void hash_compress_destroy(struct hash_compress_manager* hash_compress)
{
	if(hash_compress == NULL);
		return;
	
	//�ر��ļ�������
	close_file(hash_compress->data_fd);

	remove(hash_compress->data_file_name);

	//unmap�����ڴ�
	munmap(hash_compress->index_mmap,HASH_SEEKS_MEMAP_SIZE(hash_compress));

	remove(hash_compress->index_file_name);
	
	return;
}


#endif // USE_MEM_COMPRESS
