/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_storage.c
 *
 *    Description:  �����еĴ洢�ӿ�ʵ�֣���������ͨ��memap�������洢��
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

#include "storage.h"


struct storage_manager * storage_init(struct storage_config* config,MEM_POOL* mem_pool)
{
   struct storage_manager* storage = 
   				(struct storage_manager *)mem_pool_malloc(mem_pool,sizeof(struct storage_manager));

   assert(storage != NULL);
   
   memset(storage,0,sizeof(struct storage_manager));

   //��ʼ��block��Ϣ
   storage->value_size = config->unit_size;
   storage->row_limit = config->row_limit;

   //ƴ��:�ļ���
   sprintf(storage->file_name,"%s/%s.dat",config->work_space,config->storage_name);

   //mmapӳ�䴦��
   storage->mem_mmaped =(char*)get_mmap_memory(storage->file_name,STORAGE_MMAP_SIZE(storage)); 

   assert(storage->mem_mmaped != NULL);

   //��ʼ����ֵ��־λ
   struct bitmark_config bitmark_config;
   strcpy(bitmark_config.work_space,config->work_space);
   strcpy(bitmark_config.bitmark_name,"null");
   bitmark_config.row_limit = config->row_limit;

   storage->null_bitmark = bitmark_init(&bitmark_config,mem_pool);
   
   return storage;   
}


int32_t storage_insert(struct storage_manager * storage, struct low_data_struct * data, uint32_t docid)
{
   //�ж��Ƿ�Ϊ��
   //���֧�ֿ�ֵ�Ļ�����ô����洢��ֵ��Ϊ�յĻ��� ����Ҫ�ѱ��Ϊ��1��
   //����ǿ�ֵ�Ļ�������Ҫ�Ա�ǽ��в�����Ĭ��Ϊ��ֵ��
   if(data->len != 0)
   	{
		bitmark_set(storage->null_bitmark,docid);
   	}
   if(data->len == 0)
   	{
   		return 0;
   	}

   //�����洢������ֵ������ע�����ݵĳ��Ȳ�����rdata��lenΪ׼��������block��value_sizeΪ׼
   memcpy(storage->mem_mmaped+storage->value_size*docid,data->data,data->len);
   return 0;
}


struct low_data_struct * storage_query(struct storage_manager * storage, uint32_t docid,MEM_POOL* mem_pool)
{
    struct low_data_struct* result = NULL;

	result = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	memset(result,0,sizeof(struct low_data_struct));

	result->data = mem_pool_malloc(mem_pool,storage->value_size);
	memset(result->data,0,storage->value_size);

	//���֧�ֿ�ֵ���ұ��Ϊ0�Ļ���˵����rowid������Ϊ��
	if(bitmark_query(storage->null_bitmark,docid) == 1)
	{
		return result;
	}

	//����row_id*ÿ�����ݵĳ��ȣ������㵽row_id��Ӧ������
	memcpy(result->data,storage->mem_mmaped+docid*storage->value_size,storage->value_size);
	result->len = storage->value_size;
	return result;
}


int32_t storage_update(struct storage_manager * storage,
					struct low_data_struct * new_data,
					struct low_data_struct** old_data, 
					uint32_t docid,
					MEM_POOL* mem_pool)
{	
	//������µ����ݵĳ���Ϊ0�����Ҵ���֧�ֿ�ֵ���򽫱��λ��Ϊ0
	if(new_data->len == 0 )
	{
		//��ȡ�ϵ�����
		*old_data = storage_query(storage,docid,mem_pool);
		
		bitmark_clear(storage->null_bitmark,docid);
		return 0;
	}

	//�Ȼ�ȡ�ϵ�����
	*old_data = storage_query(storage,docid,mem_pool);

	//�����ϵ�����
    memcpy(storage->mem_mmaped+docid*storage->value_size, new_data->data, storage->value_size);

	//���֧�ֿ�ֵ������Ҫ�ѱ��λ����Ϊ1
	bitmark_set(storage->null_bitmark,docid);
	return 0;
}



void storage_release(struct storage_manager * storage)
{
	if(storage->mem_mmaped != NULL)
	{
		//ͬ��������
		msync(storage->mem_mmaped,STORAGE_MMAP_SIZE(storage),MS_SYNC);        // make sure synced
        munmap(storage->mem_mmaped, STORAGE_MMAP_SIZE(storage));
	}

	//���֧�ֿ�ֵ������Ҫ�ѱ��λ�ͷ�
	if(storage->null_bitmark != NULL)
		bitmark_release(storage->null_bitmark);
	return;
}


void storage_destroy(struct storage_manager * storage)
{
	if(storage->mem_mmaped != NULL)
	{
        munmap(storage->mem_mmaped, STORAGE_MMAP_SIZE(storage));
	}

	remove(storage->file_name);

	//���֧�ֿ�ֵ������Ҫ�ѱ��λ�ͷ�
	if(storage->null_bitmark != NULL)
		bitmark_destroy(storage->null_bitmark);
	return;


}



void storage_checkpoint(struct storage_manager * storage)
{
	if(storage->mem_mmaped != NULL)
	{
		//ͬ��������
		msync(storage->mem_mmaped,STORAGE_MMAP_SIZE(storage),MS_SYNC);        // make sure synced
	}

	//���֧�ֿ�ֵ������Ҫ�ѱ��λ�ͷ�
	bitmark_checkpoint(storage->null_bitmark);
	
	return;
}

int storage_recover(struct storage_manager *storage, uint32_t docid)
{
	if(docid >= storage->row_limit) {
		log_error("invalid docid, docid %u, row_limit %u", docid, storage->row_limit);
		return ERROR_EXCEED_LIMIT;;
	}

	if(bitmark_recover(storage->null_bitmark, docid) != 0) {
		log_error( "recover null bitmark failed" );
		return -1;
	}

	memset(storage->mem_mmaped + storage->value_size * docid, 0, (storage->row_limit - docid) * storage->value_size );

	return 0;
}
