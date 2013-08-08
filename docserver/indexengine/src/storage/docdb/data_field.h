/*
 * =====================================================================================
 *
 *       Filename:  hi_data_field.h
 *
 *    Description: ������
 *
 *        Version:  1.0
 *        Created: 	2011��09��01�� 11ʱ41��55�� 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi, yunliang.shi@alipay.com
 *        Company:  alipay.com
 *
 * =====================================================================================
 */

#ifndef DATA_FIELD_H
#define DATA_FIELD_H

#include "../../common/def.h"
#include "filter_index.h"


/*�����е�����*/
struct data_field_config{
	/*����Ŀ¼*/
	char    work_space[FILENAME_MAX_LENGTH];
	
	/*������*/
	uint32_t  row_limit;
};


/*�����еĹ���*/
struct data_field_manager{
	/*����Ŀ¼*/
	char    work_space[FILENAME_MAX_LENGTH];

	/*������*/
	uint32_t  row_limit;

	
	/*--------������----------*/
	//filter����
	struct filter_index_manager* filter_data;
};


/**
  * �����еĳ�ʼ�������������ݲ�ͬ��������������ʼ����ͬ�Ľṹ��Ŀǰ֧��hash��filter
  * @param  config ���� 
  * @param  mem_pool �ڴ��ģ��
  * @return ����index_field_manager�ṹ
  **/ 
struct data_field_manager* data_field_init(struct data_field_config* config,MEM_POOL* mem_pool);


/**
  * ���ݲ�ͬ����������������ָ����ֵ
  * @param  data_field
  * @param  rdata Ҫ�����һ�����ݣ�ÿ�а��������Լ�����
  * @param  docid �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t data_field_insert(struct data_field_manager* data_field,struct row_data* rdata,uint32_t docid,MEM_POOL* mem_pool);


/**
  * ����һ��ֵ��ֻ��filter��������֧��
  * @param  data_field
  * @param  new_data Ҫ���µ�ֵ
  * @param  old_data �����ϵ�������
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t data_field_update(struct data_field_manager* data_field,
						struct low_data_struct* new_data,
						struct low_data_struct** old_data,
						uint32_t docid,
						MEM_POOL* mem_pool);



/**
  * ����������docid���Ҷ�Ӧ��ԭʼֵ
  * @param  data_field
  * @param  field_name ����
  * @param  docid 
  * @param  mem_pool �ڴ��
  * @return �ɹ�����low_data_struct��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* data_field_query_col(struct data_field_manager* data_field,char* field_name,uint32_t docid,MEM_POOL* mem_pool);


/**
  * ����docid��������һ�е�����
  * @param  data_field
  * @param  docid
  * @param  mem_pool �ڴ��
  * @return �ɹ�����row_data��ʧ�ܷ���NULL
  **/ 
struct row_data* data_field_query_row(struct data_field_manager* data_field,uint32_t docid,MEM_POOL* mem_pool);


/**
  * �ͷŵײ����ݽṹ�����ǲ��ͷ��ڴ�
  *	@param data_field
  **/
void data_field_release(struct data_field_manager* data_field);


/**
  * ͬ��memap
  *	@param data_field
  **/
void data_field_checkpoint(struct data_field_manager* data_field);

/**
 * Recover data to docid. (clear data which >= docid).
 */
int data_field_recover(struct data_field_manager *data_field, uint32_t docid);


#endif // DATA_FIELD_H


