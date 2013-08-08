/*
 * =====================================================================================
 *
 *       Filename:  hi_storage.h
 *
 *    Description:  �����еĴ洢�ӿڶ��壬�Լ�������Ϣ����
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
#include "../../common/profiles.h"
#include "bitmark.h"

#ifndef STORAGE_H
#define STORAGE_H

#define STORAGE_MMAP_SIZE(storage) storage->value_size*storage->row_limit

struct storage_config{
	char	work_space[FILENAME_MAX_LENGTH];
	char	storage_name[FILENAME_MAX_LENGTH];
	uint16_t  unit_size;
	uint32_t  row_limit;
};


struct storage_manager{
	char   file_name[FILENAME_MAX_LENGTH];	/*�ļ���ȫ·��*/
	uint16_t  value_size;
	uint32_t  row_limit;
	char *  mem_mmaped;     /*memap���ļ����ڴ�*/

	/*��¼Ϊ��ֵ��rowid*/
	struct bitmark_manager* null_bitmark;
};


/**
  * storage_managerģ��ĳ�ʼ��������memap���ڴ���
  * @param  config ������Ϣ
  * @param mem_pool �ڴ��ģ��
  * @return ����split_block�Ķ�ά����
  **/ 
struct storage_manager * storage_init(struct storage_config* config,MEM_POOL* mem_pool);


/**
  * ��dataֵ���뵽ָ����storage��
  * @param  storage ����Ϣ
  * @param  data ��Ҫ�����ֵ������rdata��len�ֶζ���ʱ��������
  * @param  docid ָ���к�
  * @return �ɹ������кţ�ʧ�ܷ���-1
  **/ 
int32_t storage_insert(struct storage_manager * storage, struct low_data_struct * data, uint32_t docid);


/**
  * ����row_id�������ƫ�Ƶ�ַ����ѯlow_data_struct�����low_data_struct�ǲ���Ҫ�ͷŵ�
  * @param  block ����Ϣ
  * @param  row_id �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ����ز�ѯ����ֵlow_data_struct��ʧ�ܷ���NULL
  **/
struct low_data_struct * storage_query(struct storage_manager * storage, uint32_t docid,MEM_POOL* mem_pool);


/**
  * ����docid�������ƫ�Ƶ�ַ������low_data_struct
  * @param  block ����Ϣ
  * @param  new_data ���µ�����
  * @param  old_data �ϵ�������
  * @param  docid �к�
  * @return �ɹ����ز�ѯ����ֵ��ʧ�ܷ���NULL
  **/
int32_t storage_update(struct storage_manager * storage,
				    struct low_data_struct * new_data,
				    struct low_data_struct** old_data, 
				    uint32_t docid,
				    MEM_POOL* mem_pool);


/**
  * �ͷ�storage��Ϣ
  * @param  storage ���ͷŵ�storage��Ϣ
  **/
void storage_release(struct storage_manager * storage);

/**
  * ɾ���ļ���Ϣ
  * @param  storage ���ͷŵ�storage��Ϣ
  **/
void storage_destroy(struct storage_manager * storage);



/**
  * ���ڴ�flush��������
  * @param  storage storage��Ϣ
  **/
void storage_checkpoint(struct storage_manager * storage);

/**
 * Recover data to docid. (clear data which >= docid).
 */
int storage_recover(struct storage_manager *storage, uint32_t docid);

#endif
