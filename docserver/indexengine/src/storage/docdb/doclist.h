/*
 * =====================================================================================
 *
 *       Filename:  hi_doclist.h
 *
 *    Description:  ��������ʽ���洢����hashֵ��ͬ��row id���ṩ�����Լ������ӿ�
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

#include "../../common/def.h"
#include "storage.h"
#include "vstorage.h"
#include "../../common/profiles.h"


#ifndef DOCLIST_H
#define DOCLIST_H


#define DOCLIST_MMAP_SIZE(doclist) doclist->row_limit*sizeof(struct doc_row_unit)+sizeof(uint32_t)

/*����offset��ȡstruct doc_row_unit�ṹ*/
#define GET_DOC_ROW_STRUCT(doclist,doc_id) (struct doc_row_unit*)(doclist->mem_mmaped+sizeof(uint32_t) + doc_id * sizeof(struct doc_row_unit)) 

#define NEXT_DOC_ROW_STRUCT(doclist, next) (struct doc_row_unit*)(doclist->mem_mmaped + next)

#define GET_OFFSET(doc_id) (sizeof(uint32_t) + doc_id * sizeof(struct doc_row_unit))

/*�洢doc_id�Ľṹ�����������ʽ�洢*/
struct doc_row_unit{
	uint32_t	doc_id;
	uint32_t  next;
}__attribute__ ((packed));


struct doclist_config{
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
	uint8_t is_full;
};


struct doclist_manager{
	/*�ļ�����������*/
	char    file_name[FILENAME_MAX_LENGTH];

	/*���洢��row����*/
	uint32_t	row_limit;

    /*ԭ����ƫ�ƣ�Ŀǰ��Ϊ�汾���汾�����λΪ1*/
	uint32_t version;

	/*��ǰƫ����*/
	uint32_t* cur_offset;

	/* segment is full */
	volatile uint8_t is_full;
	
	char* mem_mmaped; /*memap���ڴ��е����ݽṹ*/
};


/**
  * doclist�ĳ�ʼ�������������ϲ㴫��Ĳ�ͬ�������ͣ�����ʼ���������߲������Ĵ洢��
  * @param  file_info ����Ŀ¼���Լ�limit����Ϣ
  * @param  mem_pool �ڴ��
  * @return ����segment_doclist��Ϣ
  **/ 
struct doclist_manager* doclist_init(struct doclist_config* config,MEM_POOL* mem_pool);



/**
  * doclist v2�ĳ�ʼ�������������ϲ㴫��Ĳ�ͬ�������ͣ�����ʼ���������߲������Ĵ洢��
  * @param  config ����Ŀ¼���Լ�limit����Ϣ
  * @param  is_create �Ƿ񴴽�
  * @param  index �ļ�����
  * @param  mem_pool �ڴ��
  * @return ����segment_doclist��Ϣ
  **/ 
struct doclist_manager* doclist_init_v2(struct doclist_config* config, uint8_t is_create, uint16_t index, MEM_POOL* mem_pool);



/**
  * V2 ����һ��ֵ�������head��Ҫע���£�hash�㴫���head_offset����Ϊ0��˵��hash��û�г�ͻ�����head_offset��Ϊ0�������ͷ�巨��hash����ͷ
  * @param  doclist 
  * @param  doc_id �к�
  * @param  head_offset ������˵��
  * @return ���� ƫ����
  **/ 
uint32_t doclist_insert_v2(struct doclist_manager* doclist,uint32_t doc_id,uint32_t head_offset, uint32_t bucket_no);


/**
  * ����һ��ֵ�������head��Ҫע���£�hash�㴫���head_offset����Ϊ0��˵��hash��û�г�ͻ�����head_offset��Ϊ0�������ͷ�巨��hash����ͷ
  * @param  doclist 
  * @param  doc_id �к�
  * @param  head_offset ������˵��
  * @return ���� ƫ����
  **/ 
uint32_t doclist_insert(struct doclist_manager* doclist,uint32_t doc_id,uint32_t head_offset, uint32_t bucket_no);


/**
  * ��memap���ڴ�����ݽṹflush������
  * @param  doclist
  * @return �ɹ�����0 ʧ�ܷ���-1
  **/ 
int32_t doclist_checkpoint(struct doclist_manager* doclist);

/**
  * ��memap���ڴ�����ݽṹflush������
  * @param  doclist
  * @param  next 
  * @return ����nextָ���doc_row_unit
  **/ 
struct doc_row_unit *doclist_next(struct doclist_manager* doclist, uint32_t next);

/**
  * �ͷ�doclist���ļ�������memap���ڴ�����ݽṹflush�����̣����������ͷŶ����򲻶��������ݽṹ
  * @param  doclist
  * @return 
  **/ 
void doclist_release(struct doclist_manager* doclist);


/**
  * ����!!!!!�ͷ�doclist���ļ�������memap���ڴ�����ݽṹflush�����̣�ɾ����Ӧ�����������ݽṹ
  * @param  doclist
  * @return 
  **/ 
void doclist_destroy(struct doclist_manager* doclist);

/**
 * switch mmap to real file when segment full
 * @param doclist
 */
int doclist_mmap_switch(struct doclist_manager *doclist);

#endif /* DOCLIST_H */


