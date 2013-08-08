/*
 * =====================================================================================
 *
 *       Filename:  hi_bitmark.h
 *
 *    Description:  ���λ�Ľӿڶ��壬����ɾ�����
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
#include "../../common/log.h"
#include "../../common/mem.h"
#include "../../common/file_op.h"

#ifndef BITMARK_H
#define BITMARK_H


#define BITMARK_MMAP_SIZE(bitmark) (bitmark->row_limit/BYTE_SIZE+1)

struct bitmark_config{
	char work_space[FILENAME_MAX_LENGTH];
	char bitmark_name[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
};


struct bitmark_manager{
	/*�ļ���ȫ·��*/
	char  file_name[FILENAME_MAX_LENGTH];

	char*  mem_mmaped;
	uint32_t row_limit;
};



/**
  * ɾ����ǵĳ�ʼ������
  * @param  config ������Ϣ
  * @param  mem_pool �ڴ��
  * @return ����segment_del_bitmask�ṹ��Ϣ��ʧ�ܷ���null
  **/ 
struct bitmark_manager * bitmark_init(struct bitmark_config* config, MEM_POOL* mem_pool);


/**
  * ����row_idָ���ı�ǣ���Ǹ����Ѿ��ɹ�����
  * @param  bitmark   
  * @param  row_id   Ҫ���õ�bitλ
  * @return 0��ʾ�ɹ���<0ʧ��
  **/
int32_t bitmark_set(struct bitmark_manager * bitmark, uint32_t docid);


/**
  * ���row_idָ���ı�ǣ���Ǹ����Ѿ�ɾ��
  * @param  bitmark   ��ǽṹ��Ϣ
  * @param  row_id   Ҫ��յ�bitλ
  * @return 1��ʾ�ɹ���0��ʾ��ɾ��
  **/
int32_t bitmark_clear(struct bitmark_manager * bitmark,uint32_t docid);


/**
  * ��ȡrow_idָ���ı��
  * @param  bitmark   ��ǽṹ��Ϣ
  * @param  row_id   Ҫ��ѯ��bitλ
  * @return 0��ʾδɾ����1��ʾ�ѱ��Ϊɾ����<0ʧ��
  **/
int32_t bitmark_query(struct bitmark_manager * bitmark, uint32_t docid);


/**
  * �����еı��λ����0
  * @param  bitmark   ��ǽṹ��Ϣ
  * @return 0��ʾ�ɹ���<0ʧ��
  **/
int32_t bitmark_reset(struct bitmark_manager * bitmark);


/**
  * �ͷ�bitmark_manager��Ϣ�������ڴ������flush��������
  * @param  bitmark   �ͷ��ڴ�ռ�
  * @return 
  **/
void bitmark_release(struct bitmark_manager* bitmark);


/**
  * ���ڴ������flush��������
  * @param  bitmark �ͷ��ڴ�ռ�
  * @return 
  **/
void bitmark_checkpoint(struct bitmark_manager* bitmark);

/**
 * Recover data to docid. (clear data which >= docid).
 */
int bitmark_recover(struct bitmark_manager *bitmark, uint32_t docid);

/**
 * Return marked bits number.
 */
int bitmark_count(struct bitmark_manager *bitmark);


/**
  * �������ļ�ɾ��������!!!
  * @param  bitmark �ͷ��ڴ�ռ�
  * @return 
  **/
void bitmark_destroy(struct bitmark_manager* bitmark);


#endif

