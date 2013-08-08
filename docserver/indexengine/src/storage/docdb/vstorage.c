/*
 * =====================================================================================
 *
 *       Filename:  hi_vstorage.c
 *
 *    Description:  �������еĴ洢�ӿڵ�ʵ��
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

#define _XOPEN_SOURCE 500

#include "../../common/stat_collect.h"
#include "vstorage.h"
#include "inttypes.h"

struct vstorage_manager * vstorage_init(struct vstorage_config * config,MEM_POOL* mem_pool)
{
   struct vstorage_manager * vstorage = NULL;
   char* memap_address = NULL;
   char old_loc_name[FILENAME_MAX_LENGTH],new_loc_name[FILENAME_MAX_LENGTH];
   vstorage = (struct vstorage_manager *)mem_pool_malloc(mem_pool,sizeof(struct vstorage_manager));
   memset(vstorage,0,sizeof(struct vstorage_manager));

   //���ڴ洢������Ϣ���ļ�
   sprintf(vstorage->data_file_name,"%s/%s.dat",config->work_space,config->vstorage_name);

   //���ļ�
   vstorage->data_fd = open_file(vstorage->data_file_name,O_RDWR | O_CREAT | O_APPEND);
   if(vstorage->data_fd < 0) {
       return NULL;
   }
	  
  //�������ڴ洢λ����Ϣ��ƫ��������Ҫmemap���ڴ���
  vstorage->row_limit = config->row_limit;
  //ƴ��
  //  sprintf(vstorage->index_file_name,"%s/%s.loc",config->work_space,config->vstorage_name);
  sprintf(old_loc_name,"%s/%s.loc",config->work_space,config->vstorage_name);
  sprintf(new_loc_name,"%s/%s.loc4",config->work_space,config->vstorage_name);
  
  if(access(old_loc_name, W_OK) == 0)//if old index file exist then use 2 bytes length
  {
      strcpy(vstorage->index_file_name , old_loc_name);
      //memap���ڴ���
        
      memap_address = (char* )get_mmap_memory(vstorage->index_file_name, VSTORAGE_MMAP_SIZE(vstorage));
        
      assert(memap_address != NULL);
        
      //��8�ֽڸ�ƫ����
      //���ƫ������Ҫ������¼�ļ���ǰд���λ��
      vstorage->loc_info = (struct locate_info*)(memap_address+sizeof(uint64_t));
      
      vstorage->loc_info4 = NULL;
     
  } else { //else use 4 bytes length
 
      strcpy(vstorage->index_file_name , new_loc_name);
 
      //memap���ڴ���
  
      memap_address =(char* )get_mmap_memory(vstorage->index_file_name, VSTORAGE_MMAP_SIZE4(vstorage)); 

      assert(memap_address != NULL);

      //��8�ֽڸ�ƫ����
      //���ƫ������Ҫ������¼�ļ���ǰд���λ��
      vstorage->loc_info4 = (struct locate_info4*)(memap_address+sizeof(uint64_t));
      
      vstorage->loc_info = NULL;
   }
  //��ʼ��ƫ����
  vstorage->offset = (uint64_t*)memap_address;
  
  return vstorage;
}


int32_t vstorage_insert(struct vstorage_manager * vstorage,struct low_data_struct* data,uint32_t docid)
{ 
   if(vstorage->loc_info != NULL){//still use 2 bytes as data length
      
   
       /*bugfix ����loc_info��data lenΪ16λ�������������ݳ��Ȳ��ܴ��ڵ���65535 by yunliang.shi 2011-12-21*/
       if(data->len > 0xFFFF){ 
           log_error("���ݳ��ȹ���������16λ���� %u",data->len);
	       return -1;
       }
   
        //������:��д�����
       if(data->len != 0){
           sc_record_value(STAT_ITEM_VSTORE_WRITE, data->len);
   	   if(write(vstorage->data_fd, data->data, data->len) != data->len){
               log_error("д�ļ�ʧ�ܣ��ļ���:%s ����:%u ����%s",vstorage->data_file_name,data->len,strerror(errno));
	       return -1;
   	  }
       }

       //����д�����Ҫ�������ݵĴ洢λ�ã�ͨ��ƫ�����ͳ��ȶ�λ
       (vstorage->loc_info+docid)->offset = *(vstorage->offset);
       (vstorage->loc_info+docid)->len = data->len;

       //�����ļ���ǰƫ��
       *(vstorage->offset) += data->len;
   }else{//use 4 bytes as data length
       
       if(data->len > 0xFFFFFFFF){
           log_error("���ݳ��ȹ���������32λ���� %u", data->len);
               return -1;
       }

       if(data->len != 0){
           sc_record_value(STAT_ITEM_VSTORE_WRITE, data->len);
           if(write(vstorage->data_fd, data->data, data->len) != data->len){
               log_error("д�ļ�ʧ�ܣ��ļ���:%s ����:%u ����%s",vstorage->data_file_name,data->len,strerror(errno));
               return -1;
          }
       }

       //����д�����Ҫ�������ݵĴ洢λ�ã�ͨ��ƫ�����ͳ��ȶ�λ
       (vstorage->loc_info4+docid)->offset = *(vstorage->offset);
       (vstorage->loc_info4+docid)->len = data->len;

       //�����ļ���ǰƫ��
       *(vstorage->offset) += data->len;

   }
	return 0;
}



int32_t vstorage_update(struct vstorage_manager * vstorage,
					 struct low_data_struct* new_data,
					 struct low_data_struct** old_data, 
					 uint32_t docid,
					 MEM_POOL* mem_pool)
{ 
	//�Ȼ�ȡ�ɵ�ֵ
	*old_data = vstorage_query(vstorage,docid,mem_pool);
	
	//���µ�ʱ���������Ϊ0������Ҫ��row_id����Ӧ��λ����Ϣ��len��Ϊ0
	if(new_data->len == 0)
	{
		if(vstorage->loc_info != NULL)
			(vstorage->loc_info + docid)->len = 0;
		else (vstorage->loc_info4 + docid)->len = 0;
		
		return 0;
	}

	return vstorage_insert(vstorage,new_data,docid);
}


struct low_data_struct*  vstorage_query(struct vstorage_manager * vstorage,uint32_t docid,MEM_POOL* mem_pool)
{	
        struct low_data_struct* result = NULL;
	struct locate_info* loc_info = NULL;
        struct locate_info4* loc_info4 = NULL;
	uint64_t offset = 0;

	//����ṹ�ڴ�ռ�
	result = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	memset(result,0,sizeof(struct low_data_struct));

	if(vstorage->loc_info != NULL){// still use 2 bytes length
		loc_info = vstorage->loc_info + docid;
        	result->len = loc_info->len;
		offset = loc_info->offset;		
	} else {// using 4 bytes length
		loc_info4 = vstorage->loc_info4 + docid;
		result->len = loc_info4->len;
		offset = loc_info4->offset;
	}

	if(result->len != 0)
        {
        	//�������ڴ洢���ݵ��ڴ�ռ�
                result->data = mem_pool_malloc(mem_pool,result->len);
                memset(result->data,0,result->len);

                //����ƫ�����ͳ��ȴ��ļ��ж�ȡ����
                sc_record_value(STAT_ITEM_VSTORE_READ, result->len);
                if(pread(vstorage->data_fd, result->data, result->len, offset) != result->len)
                {
                      log_error("��ȡIO�����⣬�ļ���:%s ����:%u ����:%s",vstorage->data_file_name,result->len,strerror(errno));
                         return NULL;
                }
         }
	
	return result;
}



void vstorage_release(struct vstorage_manager* vstorage)
{
	if(vstorage->offset != NULL)
	{
		if(vstorage->loc_info != NULL)
		{
			msync(vstorage->offset,VSTORAGE_MMAP_SIZE(vstorage),MS_SYNC);        // make sure synced
		        munmap(vstorage->offset, VSTORAGE_MMAP_SIZE(vstorage));
		}else if(vstorage->loc_info4 != NULL)
		{
			msync(vstorage->offset,VSTORAGE_MMAP_SIZE4(vstorage),MS_SYNC);        // make sure synced
                        munmap(vstorage->offset, VSTORAGE_MMAP_SIZE4(vstorage));
		}
	}

	close_file(vstorage->data_fd);
	return;
}

void vstorage_destroy(struct vstorage_manager* vstorage)
{
	if(vstorage->loc_info != NULL)
	{
        	munmap(vstorage->offset, VSTORAGE_MMAP_SIZE(vstorage));
	}else if(vstorage->loc_info4 != NULL)
	{
		munmap(vstorage->offset, VSTORAGE_MMAP_SIZE4(vstorage));
	}

	close_file(vstorage->data_fd);

	remove(vstorage->data_file_name);
	remove(vstorage->index_file_name);
	return;
}



void vstorage_checkpoint(struct vstorage_manager* vstorage)
{
	if(vstorage->loc_info != NULL)
	{
		msync(vstorage->offset,VSTORAGE_MMAP_SIZE(vstorage),MS_SYNC);        // make sure synced
	}else if (vstorage->loc_info4 != NULL)
	{
		msync(vstorage->offset,VSTORAGE_MMAP_SIZE4(vstorage),MS_SYNC);        // make sure synced
	}

	//ɶ����
	fsync(vstorage->data_fd);
	return;
}

// TODO problem with vstorage update.
int vstorage_recover(struct vstorage_manager *vstorage, uint32_t docid)
{
	uint64_t offset = 0 , off = 0;
	if( docid >= vstorage->row_limit ) {
		log_error("invalid docid, docid %u, row_limit %u", docid, vstorage->row_limit);
		return ERROR_EXCEED_LIMIT;
	}

	if(vstorage->loc_info != NULL){
		// truncate locate info
		memset((char *)&vstorage->loc_info[docid], 0, (vstorage->row_limit - docid) * sizeof(struct locate_info));

		// get data file offset from locate info
		for(int i = 0; i < docid; i++) {
			off = vstorage->loc_info[i].offset + vstorage->loc_info[i].len;
			if( off > offset )
				offset = off;
		}

	} else if(vstorage->loc_info4 != NULL){
		// truncate locate info
                memset((char *)&vstorage->loc_info4[docid], 0, (vstorage->row_limit - docid) * sizeof(struct locate_info4));

                // get data file offset from locate info
                for(int i = 0; i < docid; i++) {
                        off = vstorage->loc_info4[i].offset + vstorage->loc_info4[i].len;
                        if( off > offset )
                                offset = off;
                }

	}

	*vstorage->offset = offset;
	// truncate data file to offset
	uint64_t data_file_size = get_file_size(vstorage->data_fd);
	if(data_file_size == -1) {
		return -1;
	}

	if(data_file_size < offset) {
		log_error("fatal error when recover vstorage, data file %s, size %" PRIu64 " less than locate info offset %"PRIu64,
				vstorage->data_file_name, data_file_size, offset);
		return -1;
	}

	if(ftruncate(vstorage->data_fd, offset) < 0) {
		log_error("truncate file failed %s, errno %d", vstorage->data_file_name, errno);
		return -1;
	}

	return 0;
}

