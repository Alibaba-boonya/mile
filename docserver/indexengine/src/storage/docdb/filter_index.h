/*
 * =====================================================================================
 *
 *       Filename:  hi_filter_index.h
 *
 *    Description:  �Բ������Ͷ����ķ�װ���ṩͳһ�Ĵ洢�ӿ�
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
#include "../../common/mem.h"
#include "../../common/file_op.h"
#include "storage.h"
#include "vstorage.h"
#include "rowid_list.h"


#ifndef FILTER_INDEX_H
#define FILTER_INDEX_H


struct filter_index_config{
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
	uint16_t unit_size;
	enum field_types type;
};

struct filter_index_manager{
	//����ʱ������
	struct storage_manager* storage;
	
	//������ʱ������
	struct vstorage_manager* vstorage;

	/*�������ͣ�ֻ��Ϊ��������filter��������ԭʼ���ݴ洢*/
	enum field_types type;
};


/**
  * init filter�ṹ��Ϣ��������table_field����Ϣ����ʼ�������ͱ䳤�洢����
  * @param  field_info ����Ϣ
  * @param  support_null ����ʱ��Ҫ��������������Ӷ�������ֵ��ǣ�����������Ҫ
  * @return �ɹ�����filter��Ϣ��ʧ�ܷ���NULL
  **/ 
struct filter_index_manager* filter_index_init(struct filter_index_config* config, MEM_POOL* mem_pool);


/**
  * ����docid����filter�ײ�洢����һ����¼
  * @param  filter 
  * @param  data Ҫ�����ֵ����������
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t filter_index_insert(struct filter_index_manager * filter_index,struct low_data_struct* data,uint32_t docid);


/**
  * ��filter�ײ�洢����һ����¼����¼��Ϣ������filter��value�ֶΣ��Լ��к�row_id
  * @param  filter 
  * @param  new_rdata Ҫ���µ�ֵ����������
  * @param  old_rdata �ϵ�������
  * @param  row_id �к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t filter_index_update(struct filter_index_manager * filter_index,
					struct low_data_struct* new_data,
					struct low_data_struct** old_data,
					uint32_t docid,
					MEM_POOL* mem_pool);


/**
  * ��filter�ײ�洢��ѯһ����¼����¼��Ϣ������filter��value�ֶΣ��Լ��к�row_id
  * @param  filter 
  * @param  row_id �к�
  * @param  mem_pool �ڴ�أ������ڲ�����ʱ���䷵��ֵ�ڴ�
  * @return �ɹ�����low_data_structֵ��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* filter_index_query(struct filter_index_manager * filter_index,uint32_t docid,MEM_POOL* mem_pool);



/**
  * �ͷ�filter�ṹ��Ϣ�����ݶ����Ͳ�����������ֱ��ͷţ����ͷ��ڴ�
  * @param  filter 
  **/ 
void filter_index_release(struct filter_index_manager* filter_index);


/**
  * �ͷ�filter�ṹ��Ϣ����ɾ�������ļ�������!!
  * @param  filter 
  **/ 
void filter_index_destroy(struct filter_index_manager* filter_index);


/**
  * ͬ��memap
  * @param  filter 
  **/ 
void filter_index_checkpoint(struct filter_index_manager* filter_index);


/**
 * Recover data to docid. (clear data which >= docid).
 */
int filter_index_recover(struct filter_index_manager *filter_index, uint32_t docid);

#endif /* HI_FILTER_H */

