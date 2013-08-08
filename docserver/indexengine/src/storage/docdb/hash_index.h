/*
 * =====================================================================================
 *
 *       Filename:  hi_hash_index.h
 *
 *    Description:  һ��������ʽ�ļ򵥵�hash�ṹ���Լ������ֵ��ѯ�ӿڶ���
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
#include "../../common/hash.h"
#include "doclist.h"
#include "rowid_list.h"

#ifndef HASH_INDEX_H
#define HASH_INDEX_H

//��1��Ϊ������һ���ռ䣬���ڷſ�ֵ
//����0~hashmod-1��������������hash�����ǵ�hashmod��Ͱ���������������Ϊ��ֵ����
#define HASH_INDEX_MMAP_SIZE(hash_index) (hash_index->hashmod+1)*sizeof(struct hash_bucket)


struct hash_bucket{
    /*hash���ֵ*/
	uint64_t	hash_value;
	
	/*��¼doclist��ƫ�Ƶ�ַ*/
	uint32_t offset;
}__attribute__ ((packed));


struct hash_index_config{
	/*����·��*/
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
	uint8_t is_full;
};

struct hash_index_manager{
	/*hash��ģ*/
	uint32_t	hashmod;

	/*��¼������*/
	uint32_t	limit;

    /*hash�����洢���ļ�����������*/
	char    file_name[FILENAME_MAX_LENGTH];

	struct hash_bucket*	mem_mmaped;

	/* segment is full */
	volatile uint8_t is_full;

	/* protect checkpoint, release and mmap switch */
	pthread_mutex_t mmap_lock;

	/*doclist�ṹ*/
	struct doclist_manager* doclist;
};


/**
  * hash index��ʼ����������Ҫ��ʼ��hash�����ṹ���Լ�doclist�ṹ
  * @param  file_info �ϲ�������Ϣ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����segment_field_hashindex�ṹ ʧ�ܷ���NULL
  **/ 
struct hash_index_manager* hash_index_init(struct hash_index_config* config,MEM_POOL* mem_pool);


/**
  * hash index�����ָ�, ������ڵ���docid֮������ݡ�
  * @param 
  * @return
  **/ 
int32_t hash_index_recover(struct hash_index_manager* hash_index, uint32_t docid);

/**
  * hash index����һ��ֵ��ͨ��hash�ҵ��Լ���Ͱ�����Ͱ��ռ��������Ѱ��ֱ������һȦ
  * @param  hash_index hash_indexֵ
  * @param  data Ҫ�����ֵ
  * @param  doc_id�к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t hash_index_insert(struct hash_index_manager* hash_index,struct low_data_struct* data,uint32_t docid);


/**
  * ����data��hashֵȡģ����λ�������Ͱ�������Ͱû��ֵ���򲻴��ڣ����������������Ͱû��ֵ���ǿ϶��������ˣ��������£����Ǳ���һȦ�������ʱ��
  * hash�Ѿ��������Գ�ͻ������ˣ���ȡ��doclist��ͷ��Ȼ���װ��rowid_list�ṹ���ظ��ϲ�
  * @param  hash_index hash_indexֵ
  * @param  data Ҫ��ѯ��ֵ
  * @param  mem_pool�ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list* hash_index_query(struct hash_index_manager* hash_index,struct low_data_struct* data,MEM_POOL* mem_pool);


/**
  * ��ȡ��Ͱ��������е�doclist
  * @param  hash_index ��Ҫ�ͷŵĽṹ
  * @param  doc_row ͷ
  * @param  mem_pool�ڴ��
  * @return �������е�docid�б�
  **/
struct rowid_list* get_rowid_list(struct hash_index_manager* hash_index,struct doc_row_unit* doc_row,MEM_POOL* mem_pool);


/**
  * ����!!!!!!!!�ͷŽṹ����Ҫ�ر��ļ���syns memap���ļ��У���ɾ���ļ�
  * @param  hash_index ��Ҫ�ͷŵĽṹ
  **/
void hash_index_destroy(struct hash_index_manager* hash_index);


/**
  * �ͷŽṹ����Ҫ�ر��ļ���syns memap���ļ���
  * @param  hash_index ��Ҫ�ͷŵĽṹ
  **/
void hash_index_release(struct hash_index_manager* hash_index);


/**
  * ��memap����ͬ��
  * @param  hash_index ��Ҫ�ͷŵĽṹ
  **/
void hash_index_checkpoint(struct hash_index_manager* hash_index);


/**
 * switch mmap to real file when segment full
 * @param hash_index
 */
int hash_index_mmap_switch(struct hash_index_manager *hash_index);


#endif /* HASH_INDEX_H */
