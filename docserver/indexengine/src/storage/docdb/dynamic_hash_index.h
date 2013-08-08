/*
 * =====================================================================================
 *
 *       Filename:  dynamic_hash_index.h
 *
 *    Description:  ��̬hash����
 *
 *        Version:  1.0
 *        Created:  2012��09��14�� 10ʱ35��51��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi (zian), yunliang.shi@alipay.com
 *   Organization:  
 *
 * =====================================================================================
 */

#include "../../common/def.h"
#include "../../common/profiles.h"
#include "../../common/file_op.h"
#include "../../common/hash.h"
#include "doclist.h"
#include "set_operator.h"

#ifndef DYNMIC_HASH_H
#define DYNMIC_HASH_H

#define DOC_MULTIPLE_NUM 2

#define CONFLICT_RETRY_NUM 100

#define DYHASH_INDEX_MMAP_SIZE(dyhash_index) (dyhash_index->hashmod)*sizeof(struct dyhash_bucket)

struct dyhash_bucket{
    /*hash���ֵ*/
	uint64_t	hash_value;

	/*���ڸ�hash��docid��Ŀ*/
	uint32_t  count;
	
	/*��¼doclist��ƫ�Ƶ�ַ*/
	uint32_t offset;
}__attribute__ ((packed));



struct dyhash_index_config{
	/*����·��*/
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
	uint8_t is_full;
};

struct dyhash_signleindex_config{
	uint16_t index;
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t hash_mod; 
	uint8_t is_full;
	uint8_t is_create;
};

struct dyhash_single_index{
	/*hash��ģ*/
	uint32_t	hashmod;

	/*��¼������*/
	uint32_t	limit;

	/*���*/
	uint16_t index;

    /*hash�����洢���ļ�����������*/
	char    file_name[FILENAME_MAX_LENGTH];

	/*�ڴ�ӳ��*/
	struct dyhash_bucket* mem_mmaped;


	/*doclist�ṹ*/
	struct doclist_manager* doclist;

	/* ��չhash */
	struct dyhash_single_index* next;
};


struct dyhash_index_manager{
	/*����ͷ����һ����������չ*/
	struct dyhash_single_index* head; 

	/*ָ��β��*/
	struct dyhash_single_index* tail;

	/* ����Ŀ¼ */
	char work_space[FILENAME_MAX_LENGTH];

	/*hash��ģ*/
	uint32_t	hashmod;

	/*�ڴ��*/
	MEM_POOL* mem_pool;

	/* segment is full */
	volatile uint8_t is_full;

	/* protect checkpoint, release and mmap switch */
	pthread_mutex_t mmap_lock;
	
};


/* 
 *  ��չhash������ʼ�������������ļ�������ʼ����չhashͰ
 *  @param config����
 *  @param mem_pool �ڴ��
 *  @return ����dyhash_index_manager������ΪNULL
 */
struct dyhash_index_manager * dyhash_index_init(struct dyhash_index_config* config, MEM_POOL* mem_pool);




/**
  * hash index�����ָ�, ������ڵ���docid֮������ݡ�
  * @param 
  * @return
  **/ 
int32_t dyhash_index_recover(struct dyhash_index_manager* dyhash_index, uint32_t docid);

/**
  * hash index����һ��ֵ��ͨ��hash�ҵ��Լ���Ͱ�����Ͱ��ռ��������Ѱ��ֱ������һȦ
  * @param  dyhash_index dyhash_indexֵ
  * @param  data Ҫ�����ֵ
  * @param  doc_id�к�
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t dyhash_index_insert(struct dyhash_index_manager* dyhash_index,struct low_data_struct* data,uint32_t docid);


/**
  * ����data��hashֵȡģ����λ�������Ͱ�������Ͱû��ֵ���򲻴��ڣ����������������Ͱû��ֵ���ǿ϶��������ˣ��������£����Ǳ���һȦ�������ʱ��
  * hash�Ѿ��������Գ�ͻ������ˣ���ȡ��doclist��ͷ��Ȼ���װ��rowid_list�ṹ���ظ��ϲ�
  * @param  dyhash_index dyhash_indexֵ
  * @param  data Ҫ��ѯ��ֵ
  * @param  mem_pool�ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list* dyhash_index_query(struct dyhash_index_manager* dyhash_index,struct low_data_struct* data,MEM_POOL* mem_pool);



/* 
 *  ��ȡһ���ִ������docid����
 *  @param
 *  @return
 */
uint32_t dyhash_index_count_query(struct dyhash_index_manager* dyhash_index, struct low_data_struct* data, MEM_POOL* mem_pool);



/**
  * �ͷŽṹ����Ҫ�ر��ļ���syns memap���ļ���
  * @param  dyhash_index ��Ҫ�ͷŵĽṹ
  **/
void dyhash_index_release(struct dyhash_index_manager* dyhash_index);


/**
  * ��memap����ͬ��
  * @param  dyhash_index ��Ҫ�ͷŵĽṹ
  **/
void dyhash_index_checkpoint(struct dyhash_index_manager* dyhash_index);


/**
 * switch mmap to real file when segment full
 * @param dyhash_index
 */
int dyhash_index_mmap_switch(struct dyhash_index_manager *dyhash_index);



#endif



