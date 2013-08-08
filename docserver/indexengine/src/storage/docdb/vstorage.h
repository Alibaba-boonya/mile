/*
 * =====================================================================================
 *
 *       Filename:  hi_vstorage.h
 *
 *    Description:  �������еĴ洢�ӿڶ��壬�Լ�������Ϣ���壬������Ϊ�˺Ͷ����ӿ�ͳһ
 *                  �����һ�������ṹ��locate_info������һ�����������飬��¼ÿ����¼
 *                  ���ļ��д洢��ƫ�����Լ�����
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

#ifndef VSTORAGE_H
#define VSTORAGE_H

#define VSTORAGE_MMAP_SIZE(vstorage) vstorage->row_limit*sizeof(struct locate_info)+sizeof(uint64_t)
#define VSTORAGE_MMAP_SIZE4(vstorage) vstorage->row_limit*sizeof(struct locate_info4)+sizeof(uint64_t)//for length of 4 bytes
struct locate_info{
	uint64_t offset;
    uint16_t len;
}__attribute__ ((packed));

struct locate_info4{
        uint64_t offset;
        uint32_t len;
}__attribute__ ((packed));

struct vstorage_config{
	char	work_space[FILENAME_MAX_LENGTH];
	char	vstorage_name[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
};


struct vstorage_manager{
	/*���ڴ洢������Ϣ���ļ�*/
	char    data_file_name[FILENAME_MAX_LENGTH];
	int32_t	data_fd;

	/*���ڴ洢λ����Ϣ���ļ���	*/
	char    index_file_name[FILENAME_MAX_LENGTH];
	
	uint32_t  row_limit;
	uint64_t* offset;

	/*�洢�䳤�Ľṹ��Ϣ*/
	struct locate_info *  loc_info;     /*memap���ļ����ڴ�*/
	struct locate_info4 * loc_info4;    /*for length with 4 bytes*/
};


/**
  * vstorage_managerģ��ĳ�ʼ�������������ļ�������vsplit_block��Ϣ
  * @param  file_info �ļ���Ϣ
  * @param  mem_pool �ڴ��
  * @return ����vsplit_block��ָ��
  **/ 
  struct vstorage_manager * vstorage_init(struct vstorage_config * config,MEM_POOL* mem_pool);



/**
  * ��valueֵ���뵽ָ����split_block��
  * @param  vblock��Ϣ
  * @param  rdata ��Ҫ�����ֵ������ֵ���ͳ���
  * @param  row_id ָ���к�
  * @param  result Ҳ�Ǻ����ķ���ֵ��������¼��λ����Ϣ
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
  int32_t vstorage_insert(struct vstorage_manager * vstorage,struct low_data_struct* data,uint32_t docid);


/**
  * ��valueֵ���뵽ָ����split_block��
  * @param  vblock��Ϣ
  * @param  new_data ��Ҫ���µ�ֵ������ֵ���ͳ���
  * @param  old_data �ϵ�������
  * @param  row_id ָ���к�
  * @param  result Ҳ�Ǻ����ķ���ֵ��������¼��λ����Ϣ
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 	
  int32_t vstorage_update(struct vstorage_manager * vstorage,
  					   struct low_data_struct* new_data,
  					   struct low_data_struct** old_data, 
  					   uint32_t docid,
  					   MEM_POOL* mem_pool);


/**
  * ����ָ����row_id����ѯlow_data_structֵ
  * @param  vblock ����Ϣ
  * @param  mem_pool�ڴ����Ϣ
  * @param  row_id �к�
  * @return �ɹ����ز�ѯ����low_data_struct�����򷵻�NULL
  **/
  struct low_data_struct*  vstorage_query(struct vstorage_manager * vstorage,uint32_t docid,MEM_POOL* mem_pool);



/**
  * �ͷ�vstorage��Ϣ
  * @param  vstorage ���ͷŵ�block��Ϣ��ֻ���ͷ��ļ������������ͷ��ڴ�
  **/
void vstorage_release(struct vstorage_manager* vstorage);


/**
  * ɾ��������Ϣ������!!!
  * @param  vstorage ���ͷŵ�vstorage��Ϣ��ֻ���ͷ��ļ������������ͷ��ڴ�
  **/
void vstorage_destroy(struct vstorage_manager* vstorage);



/**
  * �����������ļ�flush��������
  * @param  block 
  **/
void vstorage_checkpoint(struct vstorage_manager* vstorage);


/**
 * Recover data to docid. (clear data which >= docid).
 */
int vstorage_recover(struct vstorage_manager *vstorage, uint32_t docid);


#endif /* VSTORAGE_H */
