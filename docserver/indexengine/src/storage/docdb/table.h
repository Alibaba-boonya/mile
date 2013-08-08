/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_table.h
 *
 *    Description:  ��Ľṹ��Ϣ���������еĶ�
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
#include "segment.h"
#include <time.h>
#include <pthread.h>
#include <memory.h>


#define MAX_SEGMENT_NUM (1<<10)

#define MAX_TABLE_NAME 100

#define SEGMENT_RUNTIME_SIZE(table) table->max_segment_num * sizeof(struct segment_meta_data) 

#define TABLE_RUNTIME_SIZE sizeof(struct table_meta_data)*MAX_TABLE_NUM + sizeof(uint8_t)


#ifndef TABLE_H
#define TABLE_H


/*���ݱ�״̬*/
enum table_stat{
	TABLE_INIT    = 0x01,  /*��ʾ�ñ��ѳ�ʼ��*/
    TABLE_DEL     = 0x02        /*��ʾ�ñ��Ѿ�����*/
};


/*PK�Ľ��*/
#define PK_OK 0
#define PK_FAIL 1


#define STORE_RAW_DATA 1




struct segment_not_expired{
	uint16_t sid;
	struct list_head sid_list;
};

struct segment_query_values{
	/*segment id��*/
	uint8_t sid;

	/*data*/
	struct low_data_struct* data;

	/*�б�*/
	struct list_head values_list;
};

struct table_config{
	/*�洢Ŀ¼*/
	struct str_array_t *storage_dirs;


	/*֧�ֵ�������*/
	uint16_t max_segment_num;

	/*ѹ���ȣ���Ҫ�������ļ����ȡ*/
	uint32_t hash_compress_num;

	uint32_t row_limit;

	uint8_t store_raw;

	/*table��Ԫ������Ϣ*/
	struct table_meta_data* table_meta;
};


struct table_meta_data{
	/*����*/
	char   table_name[MAX_TABLE_NAME];

	/*table�ı��*/
	enum table_stat	stat;

	/*��ǰ����д���segment��*/
	uint16_t segment_current;

	/*�����еĸ���*/
	uint16_t index_field_count;

	/*ӳ�䵽�ڴ�ĵ�ַ*/
	struct index_field_meta index_meta[MAX_INDEX_FIELD_NUM];
};

struct table_manager{
	// stroage dir
	struct str_array_t *storage_dirs;
	/*����Ŀ¼��Ϊ�洢Ŀ¼�ĵ�һ��*/
	char *work_space;

	/*�洢segment��Ԫ������Ϣ*/
	char segment_meta_filename[FILENAME_MAX_LENGTH];

	/*table��Ԫ������Ϣ*/
	struct table_meta_data* table_meta;

	/*�ñ��µ����ж���Ϣ*/
	struct segment_manager** segments;

	/*�ε�Ԫ������Ϣ*/
	struct segment_meta_data* segment_meta;

	/*֧�ֵ�������*/
	uint16_t max_segment_num;

	/*���ڼ�������ʹ�ã�����->index_field_meta*/
	struct string_map* index_meta_hash;

	/*ѹ���ȣ���Ҫ�������ļ����ȡ*/
	uint32_t hash_compress_num;

	uint32_t row_limit;

	uint8_t store_raw;;

	/*д������*/
	pthread_mutex_t write_protect_locker;

	/*����������������ѹ�����μ���ж�صĶ�����*/
	pthread_rwlock_t read_protect_locker;

	/* �¶��Ƿ��Ѽ�����ɵı�ǣ�ֻ���¶μ������֮����ܿ�ʼ��table��mmap_switch��checkpoint�����϶�����ˢ������ */
	/* ʹ�ô˱���Ա���table_add_segment�е�д���ȴ�mmap_switch��db_checkpoint�еĶ�������ɲ���ʱ����� */
	uint8_t new_seg_complete;
	
	MEM_POOL* mem_pool;
};




/**
  * ��ʼ����Ϣ
  * @param  config ��������Ϣ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����table��Ϣ��ʧ�ܷ���NULL
  **/ 
struct table_manager* table_init(struct table_config* config,MEM_POOL* mem_pool);



/**
  * ��ָ��������ڵ���docid���������
  * @param  
  **/
int32_t table_recover(struct table_manager* table, uint16_t sid, uint32_t docid);

/**
  * ��������
  * @param  table ����Ϣ
  * @param  field_name ����
  * @param  index_type ��������
  * @param  data_type ��������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t table_ensure_index(struct table_manager* table,
     					 char* field_name,
     					 enum index_key_alg index_type,
     					 enum field_types data_type,
     					 MEM_POOL* mem_pool);


/**
  * �������
  * @param  table ����Ϣ
  * @param  field_name ����
  * @param  index_type ��������
  * @param  data_type ��������
  * @return �ɹ�����1��ʧ�ܷ���0
  **/ 
int32_t check_index(struct table_manager* table,char* field_name,enum index_key_alg index_type,enum field_types* data_type);


/**
  * ɾ������
  * @param  table ����Ϣ
  * @param  field_name ����
  * @param  index_type ��������
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/
int32_t table_del_index(struct table_manager* table,char* field_name,enum index_key_alg index_type);



/**
  * ��ȡ�εĴ���ʱ��
  * @param  table ����Ϣ 
  * @param  sid �κ�
  * @return ������Ѿ���ʼ���ã��򷵻���Ӧ�Ĵ���ʱ�䣬���򷵻�0
  **/ 
uint64_t table_get_segment_ctime(struct table_manager* table,uint16_t sid);


/**
  * ��ȡ�ε��޸�ʱ��
  * @param  table ����Ϣ 
  * @param  sid �κ�
  * @return ������Ѿ���ʼ���ã��򷵻���Ӧ�Ĵ���ʱ�䣬���򷵻�0
  **/ 
uint64_t table_get_segment_mtime(struct table_manager* table,uint16_t sid);


/**
  * ����flag���жϣ������ϲ��ṩ�κţ������Լ����ɣ�����Լ����ɣ������Ѿ�����limit����Ҫ�����µĶΣ�����ϲ�ָ������鿴sid�Ƿ�
  * �Ѿ����ڣ��������������Ҫ��ʼ��������Լ�����������Ҫ���ضε�rowcount
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  docid �к�
  * @flag 1��ʾ�Լ�������0��ʾ���ϲ��ṩ
  * @param  mem_pool ���ڴ�������������µĶ�
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t table_prepare_insert(struct table_manager* table,uint16_t* sid,uint32_t* docid,uint8_t flag,MEM_POOL* mem_pool);


/**
  * ��ָ���ı����һ�м�¼�����������У��Լ�������
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  docid �к�
  * @param  rdata һ�е�����
  * @param  mem_pool ���ڴ�������������µĶ�
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t table_insert(struct table_manager* table,uint16_t sid,uint32_t docid,struct row_data* rdata,MEM_POOL* mem_pool);


/**
  * ��ָ���ı����һ������¼��������ֵ
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  docid �к�
  * @param  new_data ������
  * @param  old_data ������
  * @param  flag:0��ʾ���ϲ�ָ��sid docid��1��ʾ���Լ����ɶκ�
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t table_update(struct table_manager* table,
				   uint16_t sid,
				   uint32_t docid, 
				   struct low_data_struct* new_data,
				   struct low_data_struct** old_data,
				   MEM_POOL* mem_pool);



/**
  * ����������docid���Ҷ�Ӧ��ԭʼֵ
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  field_name ����
  * @param  docid 
  * @param  mem_pool �ڴ��
  * @return �ɹ�����low_data_struct��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* table_data_query_col(struct table_manager* table,uint16_t sid,char* field_name,uint32_t docid,MEM_POOL* mem_pool);



/**
  * ����docid��������һ�е�����
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  docid
  * @param  mem_pool �ڴ��
  * @return �ɹ�����row_data��ʧ�ܷ���NULL
  **/ 
struct row_data* table_data_query_row(struct table_manager* table,uint16_t sid,uint32_t docid,MEM_POOL* mem_pool);


/**
  * btree��Χ��ѯ������������row id list
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  field_name ����
  * @param  range_condition ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct list_head* table_index_range_query(struct table_manager* table, char* field_name, \
			struct hint_array* time_cond, struct db_range_query_condition * range_condition, MEM_POOL* mem_pool);




/**
  * ����docid�������ַ���hash���64λ����
  * @param  table ����Ϣ
  * @param  sid �κ�
  * @param  field_name ����
  * @param  docid �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* table_index_value_query(struct table_manager* table,
												uint16_t sid,
												char* field_name,
												uint32_t docid,
												MEM_POOL* mem_pool);





/**
  * ����seghint�����Ҷ�Ӧ��seg id lists
  * @param  table ����Ϣ
  * @param  time_cond seghint��Ϣ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/
struct list_head* table_seghint_query(struct table_manager* table, struct hint_array* time_cond, MEM_POOL* mem_pool);


/**
  * ����value�����Ҷ�Ӧ��countֵ
  * @param  table ����Ϣ
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
uint32_t table_index_count_query(struct table_manager* table, 
							     struct list_head* seg_list,
								 struct low_data_struct* data,
								 MEM_POOL * mem_pool);



/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��hash����֧��
  * @param  table ����Ϣ
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct list_head* table_index_equal_query(struct table_manager* table,
										  struct list_head* seg_list,
										  struct low_data_struct* data,
										  MEM_POOL* mem_pool);


/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��full text����֧��
  * @param  table ����Ϣ
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct list_head* table_fulltext_index_equal_query(struct table_manager* table,
											   	   struct list_head* seg_list,
										           struct low_data_struct* data,
										           MEM_POOL* mem_pool);


/**
  * ����time_cond���˶Σ�����ͳ�ƹ��˺�Ķεü�¼����
  * @param	table ����Ϣ 
  * @param  seg_list	������
  * @return �ɹ����ؼ�¼��
  **/ 
int64_t table_get_record_num(struct table_manager* table, struct list_head* seg_list);





/**
  * ����time_cond���˶Σ�����ͳ�ƹ��˺�Ķε�ɾ����¼����
  * @param	table ����Ϣ
  * @param  seg_list	������
  * @return �ɹ�����ɾ����¼��
  **/
int64_t table_get_delete_num(struct table_manager* talbe, struct list_head* seg_list);




/**
  * �����кŽ���ɾ��
  * @param	table ����Ϣ
  * @param  sid �κ�
  * @param  docid �к�
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
int32_t table_del_docid(struct table_manager* table,uint16_t sid,uint32_t docid);



/**
  * �ж�ָ����doc id�Ƿ���Ϊɾ��
  * @param	table ����Ϣ
  * @param  sid �κ�
  * @param  row_id �к�
  * @return ��ɾ������1��δɾ������0  
  **/
int32_t table_is_docid_deleted(struct table_manager* table,uint16_t sid,uint32_t docid);


/**
  * ���õ�ǰ����κ�
  * @param	table ����Ϣ
  * @param  sid�κ�
  * @return �ɹ�������0
  **/
int32_t table_set_segment_current(struct table_manager* table, uint16_t sid);


/**
  * ɾ����
  * @param	table ����Ϣ
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t table_del(struct table_manager* table);


/**
  * ж��sid����Σ����ҰѶεù���Ŀ¼����Ϊ��dump��׺���ļ�
  * @param	table ����Ϣ
  * @param	sid�κ�
  * @param	thread_safe �Ƿ�Ҫ�̰߳�ȫ
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t table_unload_segment(struct table_manager* table,int16_t sid,uint8_t thread_safe);


/**
  * �û�ָ����Ҫload�Ķεù���Ŀ¼���Լ��κţ��ú������Ѹö�load�����������segment_dir�Ƶ�table�Ĺ���Ŀ¼��
  * @param	table ����Ϣ
  * @param	sid�κ�
  * @param  segment_dir��Ҫ��load�Ķ�Ŀ¼
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t table_load_segment(struct table_manager* table,int16_t sid,char* segment_dir,MEM_POOL* mem_pool);


/**
  * ��ָ��table�����ж�ж�ص�������ָ����segments_dirĿ¼�¼������жΣ����ʱ��Σ������ܽ��ж�ȡ
  * @param	table ����Ϣ
  * @param  segment_dir��Ҫ��load�Ķ�Ŀ¼
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t table_replace_all_segments(struct table_manager* table,char* segments_dir,MEM_POOL* mem_pool);


/**
  * ��ȡ�ε�Ԫ������Ϣ
  * @param  table_name ����
  * @param  max_segment_num ���Ķ���
  * @param  mem_pool �ڴ��
  *	@return	segment_meta_data �ε�Ԫ������Ϣ
  **/
struct segment_meta_data* table_query_segment_stat(struct table_manager* table,uint16_t* max_segment_num,MEM_POOL* mem_pool);


/**
  * ��ȡ���������Ϣ
  * @param  table_name ����
  * @param  index_field_count ��������
  * @param  mem_pool �ڴ��
  *	@return	index_field_meta �����������
  **/
struct index_field_meta* table_query_index_stat(struct table_manager* table,uint16_t* index_field_count,MEM_POOL* mem_pool);


/**
  * ��ָ���Ķμӻ�����
  * @param  tid ���
  * @return �ɹ�����MILE_RETURN_SUCCESS��ʧ�ܷ���ERROR_LOCK_FAILED
  **/ 
int32_t table_lock(struct table_manager* table);


/**
  * ��ָ���Ķν���
  * @param  tid ���
  **/ 
void table_unlock(struct table_manager* table);


void table_read_lock(struct table_manager* table);


void table_read_unlock(struct table_manager* table);



/**
  * ����ָ���Ķκţ���ָ���Ķε�hash�н���ѹ��
  * @param	table ����Ϣ
  * @param  sid �κ�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list head���ϲ����ͨ�����������ȡ�����жν����ʧ�ܷ���NULL
  **/ 
int32_t table_compress(struct table_manager* table,MEM_POOL* mem_pool);


/**
  * ��ӡsegment_query_rowids�ṹ��Ϣ
  * @param  rowids_list_h ����ͷ
  * @return 
  **/
void print_query_rowids(struct list_head* rowids_list_h);


/**
  * �ͷű���Ϣ�������ͷ��ڴ�
  * @param	table ����Ϣ
  **/ 
void table_release(struct table_manager* table);


/**
  * ͬ��memap
  * @param	table ����Ϣ
  **/ 
void table_checkpoint(struct table_manager* table);

/**
 * switch mmap to real file when segment full
 * @param table
 * @param sid
 */
int table_mmap_switch( struct table_manager *table, uint16_t sid);

// in db.h and db.c
extern int create_mmap_switch_thread(char *tablename, uint16_t sid);

#endif

