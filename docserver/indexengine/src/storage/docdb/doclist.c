/*
 * =====================================================================================
 *
 *       Filename:  hi_doclist.c
 *
 *    Description:  ��������ʽ���洢����hashֵ��ͬ��row id���ṩ�����Լ�������ʵ��
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

#include "doclist.h"

struct doclist_manager* doclist_init(struct doclist_config* config,MEM_POOL* mem_pool)
{
   //��ʼ���ṹ
   struct doclist_manager* doclist = (struct doclist_manager*)mem_pool_malloc(mem_pool,sizeof(struct doclist_manager));
   assert(doclist != NULL);
   memset(doclist,0,sizeof(struct doclist_manager));

   sprintf(doclist->file_name,"%s/doclist.idx",config->work_space);

   //��ȫ�ļ�
   //Ҫ����4�ֽڵ��ڴ��ַ�����ڴ洢ƫ����
   doclist->row_limit = config->row_limit;
   doclist->is_full = config->is_full;
   
   if(config->is_full || mile_conf.all_mmap) {
	   //mmapӳ�䴦��
	   doclist->mem_mmaped =(char*)get_mmap_memory(doclist->file_name,DOCLIST_MMAP_SIZE(doclist)); 	
   } else {
		// alloc memory like malloc
	   doclist->mem_mmaped = (char*)alloc_file_memory(doclist->file_name, DOCLIST_MMAP_SIZE(doclist)); 	
   }
   assert(doclist->mem_mmaped != NULL);

   //��ʼ��ƫ����
   ///ͷ4λ��¼ƫ����������¼���ƫ�������ᵼ����ѭ��
   doclist->version = *(uint32_t*)doclist->mem_mmaped;

   if(doclist->version == 0)
   {
		*(uint32_t*)doclist->mem_mmaped = DATA_STOAGE_VERSION;
		doclist->version = *(uint32_t*)doclist->mem_mmaped; 
   }

   return doclist;
}

struct doclist_manager* doclist_init_v2(struct doclist_config* config, uint8_t is_create, uint16_t index, MEM_POOL* mem_pool)
{
   //��ʼ���ṹ
   struct doclist_manager* doclist = (struct doclist_manager*)mem_pool_malloc(mem_pool,sizeof(struct doclist_manager));
   assert(doclist != NULL);
   memset(doclist,0,sizeof(struct doclist_manager));

	
	sprintf(doclist->file_name,"%s/doclist.idx.%"PRIu16,config->work_space, index);

	//�ж��ļ��Ƿ����
	if(is_create != 1 && access(doclist->file_name, F_OK) != 0){
		log_warn("filename: %s not exit", doclist->file_name);
		return NULL;
	}	

   //��ȫ�ļ�
   //Ҫ����4�ֽڵ��ڴ��ַ�����ڴ洢ƫ����
   doclist->row_limit = config->row_limit;
   doclist->is_full = config->is_full;
   
   if(config->is_full || mile_conf.all_mmap) {
	   //mmapӳ�䴦��
	   doclist->mem_mmaped =(char*)get_mmap_memory(doclist->file_name,DOCLIST_MMAP_SIZE(doclist)); 	
   } else {
		// alloc memory like malloc
	   doclist->mem_mmaped = (char*)alloc_file_memory(doclist->file_name, DOCLIST_MMAP_SIZE(doclist)); 	
   }
   assert(doclist->mem_mmaped != NULL);

   //��ʼ��ƫ����
   ///ͷ4λ��¼ƫ����������¼���ƫ�������ᵼ����ѭ��
   doclist->cur_offset = (uint32_t*)doclist->mem_mmaped;

   return doclist;
}


uint32_t doclist_insert_v2(struct doclist_manager* doclist,uint32_t doc_id,uint32_t head_offset, uint32_t bucket_no)
{
	struct doc_row_unit* doc_row = NULL;

	if((*doclist->cur_offset) == doclist->row_limit)
		return -1;
	
    //headΪ�գ�˵����ϣȡģ��δ��ͻ
    //next������0������Ͱ��
	if(head_offset == 0)
	{
		//��map�ڴ���ȡһ��struct doc_row_unit�ṹ
		//ƫ��������docid��
		doc_row = GET_DOC_ROW_STRUCT(doclist, *doclist->cur_offset);
		memset(doc_row,0,sizeof(struct doc_row_unit));
		doc_row->next = 0X80000000 | bucket_no;
	}
	//�����Ϊ�գ�˵��ͬһ��value��Ӧ���doc id
	else
	{
		struct doc_row_unit* doc_row_head = (struct doc_row_unit*)(doclist->mem_mmaped+head_offset);
		if(doc_id <= doc_row_head->doc_id)
		{
			log_warn("docid �������� �����docid:%"PRIu32" ��һ��docid:%"PRIu32, doc_id,doc_row_head->doc_id);
			return 0;
		}

		doc_row = GET_DOC_ROW_STRUCT(doclist, *doclist->cur_offset);
		doc_row->next = head_offset;
	}

	//��ֵdocid
	uint32_t offset = ((*doclist->cur_offset) * sizeof(struct doc_row_unit) + sizeof(uint32_t));
	doc_row->doc_id = doc_id;
	(*doclist->cur_offset) ++;
	return offset;
}

//�����ʱ���������������hash���һ��Ͱ�ǿյģ���ôhead_offsetΪ0��ֻ��Ҫ��offset��ֵ�Ϳ�����
//���Ͱ�Ѿ���ֵ�ˣ������ͷ�巨������ƫ������Ϊָ��
uint32_t doclist_insert(struct doclist_manager* doclist,uint32_t doc_id,uint32_t head_offset, uint32_t bucket_no)
{
	struct doc_row_unit* doc_row = NULL;
	
    //headΪ�գ�˵����ϣȡģ��δ��ͻ
    //next������0������Ͱ��
	if(head_offset == 0)
	{
		//��map�ڴ���ȡһ��struct doc_row_unit�ṹ
		doc_row = GET_DOC_ROW_STRUCT(doclist, doc_id);
		memset(doc_row,0,sizeof(struct doc_row_unit));
		doc_row->next = 0X80000000 | bucket_no;
	}
	//�����Ϊ�գ�˵��ͬһ��value��Ӧ���doc id
	else
	{
		struct doc_row_unit* doc_row_head = (struct doc_row_unit*)(doclist->mem_mmaped+head_offset);
		if(doc_id <= doc_row_head->doc_id)
		{
			log_warn("docid �������� �����docid:%"PRIu32" ��һ��docid:%"PRIu32, doc_id,doc_row_head->doc_id);
			return 0;
		}
		
		doc_row = GET_DOC_ROW_STRUCT(doclist, doc_id);
		doc_row->next = head_offset;
	}

	//��ֵdocid
	doc_row->doc_id = doc_id;
	return GET_OFFSET(doc_id);
}



//��nextƫ����ת��Ϊdoc_row_unit�ṹ��Ϣ
struct doc_row_unit *doclist_next(struct doclist_manager* doclist, uint32_t next)
{
	if((next & 0x80000000) || next == 0)
		return NULL;
	return (struct doc_row_unit*)(doclist->mem_mmaped + sizeof(uint32_t) +next);
}


int32_t doclist_checkpoint(struct doclist_manager* doclist)
{
	if(doclist->mem_mmaped != NULL)
	{
		if( doclist->is_full || mile_conf.all_mmap ) {
			return msync(doclist->mem_mmaped,DOCLIST_MMAP_SIZE(doclist),MS_SYNC); // make sure synced
		} else  {
			return flush_memory(doclist->file_name, doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist), mile_conf.disk_write_limit);
		}
	}
	return 0;
}

void doclist_destroy(struct doclist_manager* doclist)
{
	//�ڴ治��Ҫ�ͷţ�ֻ��Ҫ�ر��ļ���flush������
	if(doclist->mem_mmaped != NULL)
	{
		munmap(doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist));
	}

	//ɾ���ļ�
	remove(doclist->file_name);
	return;
}


void doclist_release(struct doclist_manager* doclist)
{
	//�ڴ治��Ҫ�ͷţ�ֻ��Ҫ�ر��ļ���flush������
	if(doclist->mem_mmaped != NULL)
	{
		if( doclist->is_full || mile_conf.all_mmap ) {
			msync(doclist->mem_mmaped,DOCLIST_MMAP_SIZE(doclist),MS_SYNC); // make sure synced
		} else  {
			flush_memory(doclist->file_name, doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist), 0); // no disk write limit
		}
		munmap(doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist));
	}
	return;
}

int doclist_mmap_switch(struct doclist_manager *doclist)
{
	if( !doclist->is_full && !mile_conf.all_mmap ) {
		if(flush_memory(doclist->file_name, doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist), mile_conf.disk_write_limit) != 0) {
			return ERROR_MMAP_SWITCH;
		}

		if(switch_mmaped_file(doclist->file_name, doclist->mem_mmaped, DOCLIST_MMAP_SIZE(doclist)) != 0 ) {
			return ERROR_MMAP_SWITCH;
		}
		doclist->is_full = 1;
	}
	return MILE_RETURN_SUCCESS;
}
