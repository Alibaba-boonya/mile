/*
 * =====================================================================================
 *
 *       Filename:  hi_index_field.h
 *
 *    Description: �����У�����hash��filter����
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

#include "../../common/def.h"
#include "hash_index.h"
#include "filter_index.h"
#include "hashcompress.h"
#include "hash_memcompress.h"
#include "filtercompress.h"
#include "dynamic_hash_index.h"

#ifndef INDEX_FIELD_H
#define INDEX_FIELD_H

#define MAX_FIELD_NAME 100


/*����״̬*/
enum index_field_flag{
	INDEX_FIELD_COMPRESS = 0x01,  /*�������ѱ�ѹ�������ܾ�����*/
    INDEX_FIELD_DEL = 0x02        /*�����ѱ�ɾ��*/
};


/*�����е�����*/
struct index_field_config{
	/*����Ŀ¼*/
	char    work_space[FILENAME_MAX_LENGTH];
	
	/*����*/
	char    field_name[MAX_FIELD_NAME];

	/*�����������*/
	enum index_key_alg index_type;

	/*ѹ���ȣ���Ҫ�������ļ����ȡ*/
	uint32_t hash_compress_num;

	/*������*/
	uint32_t  row_limit;

	/* is segment full */
	uint8_t is_full;
};


/*�����еĹ���*/
struct index_field_manager{
	/*����Ŀ¼*/
	char    work_space[FILENAME_MAX_LENGTH];

	/*����*/
	char    field_name[MAX_FIELD_NAME];

	/*������*/
	uint32_t  row_limit;

	/*��������ṹ*/
	enum index_key_alg index_type;


	/*������״̬*/
	char stat_filename[FILENAME_MAX_LENGTH];
	enum index_field_flag* flag;

	
	/*--------������----------*/
	//filter����
	struct filter_index_manager* filter_index;

	//hash����
	struct hash_index_manager* hash_index; 

	//Btree����
	struct btree * pBtreeIndex;

	//fulltext����
	struct dyhash_index_manager* dyhash_index;


	/*--------ѹ�����������-------*/
	//hashѹ��
	struct hash_compress_manager* hash_compress;

	//filterѹ��
	struct filter_compress_manager* filter_compress;

	uint32_t hash_compress_num;

	//��¼��ǰfield��󳤶�
	char len_filename[FILENAME_MAX_LENGTH];
	
	uint32_t *max_len;
};




/**
  * �����еĳ�ʼ�������������ݲ�ͬ��������������ʼ����ͬ�Ľṹ��Ŀǰ֧��hash��filter
  * @param  config ���� 
  * @param  mem_pool �ڴ��ģ��
  * @return ����index_field_manager�ṹ
  **/ 
struct index_field_manager* index_field_init(struct index_field_config* config,MEM_POOL* mem_pool);

/**
  * �����лָ�
  * @param  
  **/
int32_t index_field_recover(struct index_field_manager* index_field, uint32_t docid);


/**
  * ���ݲ�ͬ����������������ָ����ֵ
  * @param  index
  * @param  data Ҫ��������ݽṹ
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t index_field_insert(struct index_field_manager* index_field,struct low_data_struct* data,uint32_t docid);



/**
  * ����һ��ֵ��ֻ��filter��������֧��
  * @param  index
  * @param  new_data Ҫ���µ�ֵ
  * @param  old_data �����ϵ�������
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t index_field_update(struct index_field_manager* index_field,
						 struct low_data_struct* new_data,
						 struct low_data_struct** old_data,
						 uint32_t docid,
						 MEM_POOL* mem_pool);


/**
  * btree��Χ��ѯ������������row id list
  * @param  index
  * @param  range_condition ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list * index_field_range_query(struct index_field_manager * index_field, \
		struct db_range_query_condition * range_condition, MEM_POOL* mem_pool);



/**
  * ����docid�������ַ���hash���64λ����
  * @param  index
  * @param  docid �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* index_field_value_query(struct index_field_manager* index_field, uint32_t docid, MEM_POOL* mem_pool);



/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��hash����֧��
  * @param  field
  * @param  data ����
  * @param  row_count��ǰ�洢������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list* index_field_equal_query(struct index_field_manager* index_field,struct low_data_struct* data,MEM_POOL* mem_pool);


/**
  * ����value�����Ҷ�Ӧ��Ͱ�ж���
  * @param  field
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����uint32_t��ʧ�ܷ���0
  **/ 
uint32_t index_field_count_query(struct index_field_manager* index_field, struct low_data_struct* data, MEM_POOL* mem_pool);

/**
  * �������н���ѹ��
  * @param  field ����Ϣ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����MILE_RETURN_SUCCESS��ʧ�ܷ��ش�����
  **/ 
int32_t index_field_compress(struct index_field_manager* index_field,MEM_POOL* mem_pool);



/**
  * ��ѹ�����е����л�����ɾ��δѹ����������
  * @param  field ����Ϣ
  * @return �ɹ�����MILE_RETURN_SUCCESS��ʧ�ܷ��ش�����
  **/
int32_t index_field_switch(struct index_field_manager* index_field);


/**
  * �ͷŵײ����ݽṹ�����ǲ��ͷ��ڴ�
  *	@param field
  **/
void index_field_release(struct index_field_manager* index_field);


/**
  * �ͷŵײ����ݽṹ��ɾ�������ļ�������!!!
  *	@param field
  **/
void index_field_destroy(struct index_field_manager* index_field);


/**
  * ͬ��memap
  *	@param field
  **/
void index_field_checkpoint(struct index_field_manager* index_field);

/**
 * switch mmap to real file, when segment full
 * @param index_field
 */
int index_mmap_switch(struct index_field_manager *index_field);

#endif // INDEX_FIELD_H

