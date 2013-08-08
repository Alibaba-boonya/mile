/*
 * =====================================================================================
 *
 *       Filename:  hi_hashcompress.h
 *
 *    Description:  ��һ��������ʱ�򣬶�hash�н���ѹ������С�ڴ濪��
 *
 *        Version:  1.0
 *        Created: 	2011��08��18�� 11ʱ41��55�� 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi, yunliang.shi@alipay.com
 *        Company:  alipay.com
 *
 * =====================================================================================
 */

#ifndef HASHCOMPRESS_H
#define HASHCOMPRESS_H

#ifndef USE_MEM_COMPRESS

#include "../../common/def.h"
#include "hash_index.h"
#include "doclist.h"
#include "rowid_list.h"
#include "../../common/hash.h"

#define BUF_NUM 10000

#define HASH_SEEKS_MEMAP_SIZE(hash_compress) (hash_compress->hash_mod/hash_compress->hash_compress_num+1)*sizeof(struct hash_seeks)+sizeof(uint32_t)

struct hash_compress_config{
	/*����·��*/
	char work_space[FILENAME_MAX_LENGTH];
	uint32_t row_limit;
	uint32_t hash_compress_num;
};

//һ����Ԫ�Ĵ洢�ṹ�����ڴ����ϵ�
struct hash_docs{
	uint64_t hash_value;
	
	//��λdocids
	uint32_t num;
	uint32_t docids[0];
}__attribute__ ((packed));


//ÿ10��hash docs��¼һ��ƫ����,�洢���ڴ���
struct hash_seeks{
	uint64_t hash_value;

	//10��hash_docs��ƫ�Ƶ�ַ�Լ�����
	uint32_t offset;
	uint32_t len;
}__attribute__ ((packed));



struct hash_compress_manager{
	char index_file_name[FILENAME_MAX_LENGTH];
	char data_file_name[FILENAME_MAX_LENGTH];

	/*�����ļ�����������*/
	int data_fd;

	/*�����Ľṹ��ӳ�䵽�ڴ���*/
	struct hash_seeks* index_mmap;

	/*��������*/
	uint32_t* index_count;

	/*hashͰ*/
	uint32_t hash_mod;

	uint32_t row_limit;

	/*hashѹ���ı���*/
	uint32_t hash_compress_num;
};




/**
  * hashѹ����ʼ�������ڼ����Ѿ���ʼ���������ļ�
  * @param  config ������Ϣ����������Ŀ¼��hashͰ��
  * @param  mem_pool�ڴ��
  * @return �ɹ�����segment_hash_compress�ṹ��ʧ�ܷ���NULL
  **/ 
struct hash_compress_manager* hash_commpress_init(struct hash_compress_config* config,MEM_POOL* mem_pool);



/**
  * ��hash�н���ѹ��������ѹ���Ķ��ֲ���������
  * @param  hash_field Ҫѹ����hash������
  * @param  hash_compress_num ÿ�����ٸ�hashͰ��һ�����ֲ�������
  * @param  mem_pool�ڴ��
  * @return �ɹ�����segment_hash_compress��ʧ�ܷ���NULL
  **/ 
struct hash_compress_manager* hash_compress_load(struct hash_index_manager* hash_index,uint32_t hash_compress_num,MEM_POOL* mem_pool);



/**
  * hash compress��ѹ����ѯ�����ȸ��ݶ��ֲ��ҷ�����λ�����ĸ��飬Ȼ��Ӵ����ж�ȡ����
  * @param  hash_compress �нṹ��Ϣ
  * @param  data Ҫ��ѯ��ֵ
  * @param  mem_pool�ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list* hash_compress_query(struct hash_compress_manager* hash_compress,struct low_data_struct* data,MEM_POOL* mem_pool);



/**
  * �ͷŽṹ����Ҫ�ر��ļ�
  * @param  hash_compress ��Ҫ�ͷŵĽṹ
  **/
void hash_compress_release(struct hash_compress_manager* hash_compress);


/**
  * �ͷŽṹ����Ҫ�ر��ļ����Լ�ɾ�������ļ�������!!!
  * @param  hash_compress ��Ҫ�ͷŵĽṹ
  **/
void hash_compress_destroy(struct hash_compress_manager* hash_compress);



#endif // USE_MEM_COMPRESS

#endif // HASHCOMPRESS_H
