/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_db.h
 *
 *    Description:  ����DB��Ľӿ��������Լ�DB����Ҫά���Ľṹ��Ϣ����
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
 
#ifndef DB_H
#define DB_H
#include "../../common/def.h"
#include "table.h"
#include "../../common/file_op.h"
#include "../binlog.h"
#include "../../common/stat_collect.h"
#include "../../common/common_util.h"
#include <sys/types.h>
#include <stdint.h>

#define MAX_TABLE_NUM 0xff
#define LOG_LEVEL_NAME 8
#define USEC_PER_MSEC 1000

enum db_stat{
	RECOVER_DATA = 0,
	RECOVER_FIN = 1
};

/*db��������Ϣ*/
struct db_conf{
	/* server config */
	uint16_t cpu_threshold;
	char sync_addr[IP_ADDRESS_MAX_LENGTH];
	
	struct str_array_t storage_dirs;

	char binlog_dir[FILENAME_MAX_LENGTH];

	uint32_t checkpoint_interval;
	uint32_t binlog_maxsize;
	uint8_t  binlog_flag; /*�Ƿ��binlog��־*/
	uint64_t binlog_threshold; /*��־�೤ʱ����׷���ϵ�����ֵ*/
	int64_t binlog_sync_interval; /*binlog��ͬ��ʱ�䣬����,*/
	uint8_t role;
	uint32_t hash_compress_num;
	uint16_t max_segment_num;
	uint32_t row_limit;
	uint16_t profiler_threshold;

	struct string_map* table_store_only_index;
	
	int32_t max_result_size;
	
	int32_t cut_threshold;
};

struct data_import_field_info{
	 char field_name[MAX_FIELD_NAME];
     enum index_key_alg index_type;
     enum field_types data_type;
};

struct data_import_conf{
	char   table_name[MAX_TABLE_NAME];
	char   encode_type[128];
	char   split[5];
	uint16_t field_count;
	struct data_import_field_info* fields_info;
};



/*��¼binlog��Ԫ������Ϣ*/
typedef struct binlog_meta{
	uint64_t last_checkpoint;  //the time of last checkpoint

	/*slave��������¼binlogͬ����λ����Ϣ*/
	uint64_t offset;
}BL_META, *BL_META_PTR;



/*db��Ϣ*/
struct db_manager{
	/*���еı���Ϣ����һ��string_map�ṹӳ������ tablename->struct table_manager*/
	struct string_map* tables;

	/*���������־û���������*/
	uint8_t* table_count;

	/*���б������־û���������*/
	struct table_meta_data* table_metas;

	/*�������ͱ�Ԫ������Ϣ��hash ��������*/
	struct string_map* table_metas_hash;

	/*binlog��Ԫ������Ϣ*/
	BL_META_PTR bl_meta;

	/*binlog��ʼͬ����ʱ��㣬����*/
	uint64_t catch_up_start;

	/*binlog��ʼͬ����ʱ���Ѿ���ɵ�ƫ����*/
	uint64_t offset_start;
	
	struct str_array_t *storage_dirs;
	char *work_space; // work space is the first dir of storage_dirs
	
	char binlog_dir[FILENAME_MAX_LENGTH];

	/*bin log��Ϣ*/
	BL_WRITER_PTR binlog_writer;

	/*checkpoint��ʱ����*/
	uint32_t checkpoint_interval;

	/*binlog�����ļ��������־��*/
	uint32_t binlog_maxsize;

	/*�Ƿ��binlog��־*/
	uint8_t binlog_flag;

	/*binlog��ͬ��ʱ�䣬���룬<0��ʾ�Ӳ�sync*/
	int64_t binlog_sync_interval;

	uint32_t hash_compress_num;

	uint16_t max_segment_num;

	uint32_t row_limit;

    MEM_POOL* mem_pool;

	pthread_t checkpoint_tid;

	pthread_t sync_tid;
	
	/*��д��*/
	pthread_rwlock_t locker;

	pthread_mutex_t task_locker;

	/*��־�೤ʱ����׷���ϵ�����ֵ*/
	uint64_t binlog_threshold;

	/*db��״̬����Ҫ�������ָ���ʱ�����ټ�binlog��*/
	uint8_t db_stat;

	uint8_t readable;

	uint8_t role;

	struct string_map* table_only_store_index;
};

/**
 * segment full thread parameters
 */
struct segment_full_param_t {
	char *tablename;
	uint16_t sid;
	MEM_POOL_PTR mem;
};

/**
 * parameters for db_recover_check()
 */
struct recover_check_param_t {
	MEM_POOL_PTR mem;
	struct string_map *table_sid;
	char *last_table;
	uint16_t last_sid;
};



struct access_way_t{
	enum data_access_type_t access_type;
	// number of actual select fields
	uint32_t actual_sel_n;
	// the actual select fields name
	char** actual_fields_name;
	// the array of the actual select type
	enum select_types_t* actual_select_type;
};


/**
  * ��ʼ��һ��db��Ϣ����������Ϊȫ�ֱ���
  * @param  conf db��������Ϣ
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t db_init(struct db_conf* conf);


/**
  * ��ȡdb��Ϣ
  * @return �ɹ�����db info��ʧ�ܷ���NULL
  **/
struct db_manager* get_db();


/**
  * ж��sid����Σ����ҰѶεù���Ŀ¼����Ϊ��dump��׺���ļ�
  * @param	table_name ����
  * @param	sid�κ�
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t db_unload_segment(char* table_name,uint16_t sid,MEM_POOL* mem_pool);


/**
  * �û�ָ����Ҫload�Ķεù���Ŀ¼���Լ��κţ��ú������Ѹö�load�����������segment_dir�Ƶ�table�Ĺ���Ŀ¼��
  * @param	table_name ����
  * @param	sid�κ�
  * @param  segment_dir��Ҫ��load�Ķ�Ŀ¼
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t db_load_segment(char* table_name,int16_t sid,char* segment_dir,MEM_POOL* mem_pool);


/**
  * ��ָ��table�����ж�ж�ص�������ָ����segments_dirĿ¼�¼������жΣ����ʱ��Σ������ܽ��ж�ȡ
  * @param	table_name ����
  * @param  segments_dir��Ҫ��load�Ķ�Ŀ¼
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ��<0
  **/ 
int32_t db_replace_all_segments(char* table_name,char* segments_dir,MEM_POOL* mem_pool);


/**
  * ���õ�ǰ����κ�
  * @param	table_name ����
  * @param  sid�κ�
  * @return �ɹ�����0
  **/
int32_t db_set_segment_current(char* table_name, uint16_t sid);




/**
  * ����һ������Ϣ
  * @param  table_name ����
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
struct table_manager* db_create_table(char* table_name);



/**
  * ɾ��һ������Ϣ
  * @param	table_name ����
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/
int32_t db_del_table(char* table_name);



/**
  * ��ָ���ı��첽���һ��������(ͬ��)
  * @param  table_name ����
  * @param  field_name ����
  * @param  index_type �¼���������
  * @param  data_type  ��������
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t db_ensure_index(char* table_name,
					  char* field_name,
					  enum index_key_alg index_type,
					  enum field_types data_type, 
					  MEM_POOL* mem_pool);


/**
  * ��ָ���ı�ɾ������
  * @param  table_name ����
  * @param  field_name ����
  * @param  index_type �¼���������
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/ 
int32_t db_del_index(char* table_name,char* field_name,enum index_key_alg index_type,MEM_POOL* mem_pool);



/**
  * ��ָ���ı����һ�м�¼
  * @param  table_name ����
  * @param  sid �κ�
  * @param  docid �к�
  * @param  rdata һ�е�����
  * @param  flag:0��ʾ���ϲ�ָ��sid docid��1��ʾ���Լ����ɶκ�
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t db_insert(char* table_name, uint16_t* sid, uint32_t* docid, struct row_data* rdata, uint8_t flag, MEM_POOL* mem_pool);


/**
  * ��ָ���ı����һ������¼��������ֵ
  * @param  table_name ����
  * @param  sid �κ�
  * @param  docid �к�
  * @param  new_data ������
  * @param  old_data ������
  * @param  flag:0��ʾ���ϲ�ָ��sid docid��1��ʾ���Լ����ɶκ�
  * @param  mem_pool�ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t db_update(char* table_name,
				uint16_t sid,
				uint32_t docid, 
				struct low_data_struct* new_data,
				struct low_data_struct** old_data,
				MEM_POOL* mem_pool);



/**
  * ��¼����ĵ�ǰִ�гɹ���λ�ã��Լ����ݿ�ʼͬ����λ�ã��Լ���ǰ��ͬ����λ�ã������ٶȣ��Ӷ���ȡͬ����״̬
  * @param  offset_cur ��ǰ��ƫ����
  * @param  offset_left ʣ�����־ƫ����
  * @return ����Ѿ�׷����master���򷵻�MILE_SLAVE_CACTCH_UP�����򷵻�MILE_RETURN_SUCCESS
  **/ 
int32_t db_slave_set_offset(uint64_t offset_cur,uint64_t offset_left);



/**
  * slave���յ�master���Ļ�������ϲ������bl_record�����ô˽ӿ�ִ��
  * �������ݻָ����
  * @param  bl_record binlog��һ�����������ݼ�¼
  * @param  mem_pool �ڴ��
  * @return �ɹ�����MILE_RETURN_SUCCESS�����򷵻ض�Ӧ�Ĵ�����
  **/ 
int32_t db_execute_binrecord(struct binlog_record* bl_record,MEM_POOL* mem_pool);


/**
  * slave�ڿ�ʼ׷�ϵ�ʱ����Ҫ��¼��ʼ��ʱ��㣬���ڲ��٣���������Ҫͬ����ƫ����
  * @return ͬ����ʼ��λ��
  **/
uint64_t db_start_catch_up();



/**
  * �����кŽ���ɾ��
  * @param  table_name ����
  * @param  sid �κ�
  * @param  row_id �к�
  * @param  mem_pool�ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
int32_t db_del_docid(char* table_name,uint16_t sid,uint32_t docid,MEM_POOL* mem_pool);



/**
  * ����rowid�����Ҷ�Ӧ��value��һ�ο��Բ��Ҷ���
  * @param  table_name ����
  * @param  sid �κ�
  * @param  docid �к�
  * @param  field_names Ҫ��ѯ������
  *	@param	field_num Ҫ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ����ض���value�����飬ʧ�ܷ���NULL
  **/
struct low_data_struct** db_data_query_multi_col(char* table_name,uint16_t sid, uint32_t docid, char** field_names,uint32_t field_num, enum data_access_type_t data_access_type, MEM_POOL* mem_pool);




/**
  * ����������docid���Ҷ�Ӧ��ԭʼֵ
  * @param  table_name ����
  * @param  sid �κ�
  * @param  field_name ����
  * @param  docid 
  * @param  mem_pool �ڴ��
  * @return �ɹ�����low_data_struct��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* db_data_query_col(char* table_name,uint16_t sid,char* field_name,uint32_t docid,MEM_POOL* mem_pool);




/**
  * ����docid��������һ�е�����
  * @param  table_name ����
  * @param  sid �κ�
  * @param  docid
  * @param  mem_pool �ڴ��
  * @return �ɹ�����row_data��ʧ�ܷ���NULL
  **/ 
struct row_data* db_data_query_row(char* table_name,uint16_t sid,uint32_t docid,MEM_POOL* mem_pool);


/**
  * btree��Χ��ѯ������������row id list
  * @param  table_name ����
  * @param  sid �κ�
  * @param  field_name ����
  * @param  range_condition ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct list_head* db_index_range_query(char* table_name, char* field_name, \
			struct hint_array* time_cond, struct db_range_query_condition * range_condition, MEM_POOL* mem_pool);




/**
  * ����docid�������ַ���hash���64λ���ͣ�������filter�е�ԭʼֵ
  * @param  table_name ����
  * @param  sid �κ�
  * @param  field_name ����
  * @param  docid �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* db_index_value_query(char* table_name,uint16_t sid,char* field_name,uint32_t docid,MEM_POOL* mem_pool);





/**
  * ����seghint�����Ҷ�Ӧ��segid lists
  * @param  table_name ����
  * @param  time_cond seghint����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list_head��ʧ�ܷ���NULL
  **/ 
struct list_head* db_seghint_query(char* table_name, struct hint_array* time_cond, MEM_POOL* mem_pool);





/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��hash����֧��
  * @param  table_name ����
  * @param  seg_list ���б�
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list_head��ʧ�ܷ���NULL
  **/ 
struct list_head* db_index_equal_query(char* table_name,
									   struct list_head* seg_list,
									   struct low_data_struct* data,
									   MEM_POOL* mem_pool);


/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��fulltext����֧��
  * @param  table_name ����
  * @param  seg_list ���б�
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list_head��ʧ�ܷ���NULL
  **/ 
struct list_head* db_fulltext_index_equal_query(char* table_name,
									 		    struct list_head* seg_list,
									            struct low_data_struct* data,
									            MEM_POOL* mem_pool);


/**
  * ���ݷִ�����ѯ���������ĳ��ȣ�ֻ֧��ȫ������
  * @param  table_name ����
  * @param  seg_list ���б�
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return ���ص��������ĳ��ȣ�����ʱ����ֵС��0
  **/
uint32_t db_fulltext_index_length_query(char* table_name, struct list_head* seg_list, struct low_data_struct* data, MEM_POOL_PTR mem_pool);



/**
  * ��ȡ���е�docids��ÿ���ΰ������Ķκ�
  * @param  ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����segment_query_alldocids��ʧ�ܷ���NULL
  **/ 
struct list_head* db_query_all_docids(char* table_name,MEM_POOL* mem_pool);



/**
  * ��ָ���ı�ӻ�����
  * @param  table_name ����
  **/ 
int32_t db_lock_table(char* table_name);


/**
  * ��ָ���ı����
  * @param  table_name ����
  **/ 
void db_unlock_table(char* table_name);


/**
  * ��ָ���ı�Ӷ�
  * @param  table_name ����
  **/ 
void db_readlock_table(char* table_name);


/**
  * ��ָ���ı����
  * @param  table_name ����
  **/ 
void db_unreadlock_table(char* table_name);



void db_read_lock();


void db_read_unlock();



/**
  * ��ָ���ı�Ͷν���ѹ��
  * @param  table_name ����
  * @param  sid �κ�
  **/ 
int32_t db_compress(char* table_name,MEM_POOL* mem_pool);


/**
  * �ж�ָ����doc id�Ƿ���Ϊɾ��
  * @param  table_name ����
  * @param  sid �κ�
  * @param  row_id �к�
  * @return ��ɾ������1��δɾ������0  
  **/
int32_t db_is_docid_deleted(char* table_name,uint16_t sid,uint32_t docid);


/**
  * ͨ��������timehint����ö�Ӧ�εļ�¼����
  * @param  table_name	����
  *	@param	seg_list	������
  *	@return	���ؼ�¼��
  **/
int64_t db_get_record_num(char* table_name, struct list_head* seg_list);




/**
  * ͨ��������timehint����ö�Ӧ�ε�ɾ����¼����
  * @param  table_name	����
  *	@param	seg_list	������
  *	@return	����ɾ����¼��
  **/
int64_t db_get_delete_num(char* table_name, struct list_head* seg_list);



/**
  * ��ȡ�εĴ���ʱ��
  * @param  table_name ����
  * @param  sid �κ�
  *	@return	������Ѿ���ʼ���ã��򷵻���Ӧ�Ĵ���ʱ�䣬���򷵻�0
  **/
uint64_t db_get_segment_ctime(char* table_name,uint16_t sid);

/**
  * ��ȡ�εĴ���ʱ��
  * @param  table_name ����
  * @param  sid �κ�
  *	@return	������Ѿ���ʼ���ã��򷵻���Ӧ�Ĵ���ʱ�䣬���򷵻�0
  **/
uint64_t db_get_segment_mtime(char* table_name,uint16_t sid);


/**
  * ��ȡ�εĴ���ʱ��
  * @param  table_name ����
  * @param  max_segment_num ���Ķ���
  * @param  mem_pool �ڴ��
  *	@return	segment_meta_data �ε�Ԫ������Ϣ
  **/
struct segment_meta_data* db_query_segment_stat(char* table_name,uint16_t* max_segment_num,MEM_POOL * mem_pool);


/**
  * ��ȡ�εĴ���ʱ��
  * @param  table_name ����
  * @param  index_field_count ��������
  * @param  mem_pool �ڴ��
  *	@return	index_field_meta �����������
  **/
struct index_field_meta* db_query_index_stat(char* table_name,uint16_t* index_field_count,MEM_POOL * mem_pool);



/**
  * ��ȡĳ���е���������
  * @param  table_name ����
  * @param  field_name ����
  *	@return	field_types ������������
  **/
enum field_types db_query_data_type(char* table_name,char* field_name);




/**
  * ��ȡĳ���е����ݷ�������
  * @param  table_name ����
  * @param  field_name ����
  *	@return	field_access_type_t �е����ݷ�������
  **/
enum field_access_type_t db_get_data_access_type(char* table_name, char* field_name);




/**
  * �ͷ�db��Ϣ
  * @return �ɹ�����0��ʧ�ܷ���-1
  **/
void db_release();


/**
  * ͬ��memap
  * @return 
  **/
void db_checkpoint();



/**
 * change db status to readable.
 */
void set_db_readable( void );

/**
 * change db status to unreadable.
 */
void set_db_unreadable( void );

/**
 * create a thread to do mmap switch
 * @tablename
 * @sid
 */
int create_mmap_switch_thread(char *tablename, uint16_t sid);

enum index_key_alg  db_binrecord_to_index(struct binlog_record* bl_record,
												 char** table_name,
												 char** field_name,
												 enum field_types* data_type,
												 MEM_POOL* mem_pool);

enum index_key_alg  db_binrecord_to_dindex(struct binlog_record* bl_record,
											      char** table_name,
											      char** field_name,
											      MEM_POOL* mem_pool);

												 
struct row_data* db_binrecord_to_insert(struct binlog_record* bl_record,
											   char** table_name, 
											   MEM_POOL* mem_pool);

struct low_data_struct* db_binrecord_to_update(struct binlog_record* bl_record,
													  char** table_name, 
													  char** field_name,
													  MEM_POOL* mem_pool);

void db_binrecord_to_load(struct binlog_record* bl_record,
								 char** table_name,  
 							     char** segment_dir,
 							     MEM_POOL* mem_pool);

void db_binrecord_to_unload(struct binlog_record* bl_record,
								   char** table_name, 
								   MEM_POOL* mem_pool);

void db_binrecord_to_compress(struct binlog_record* bl_record,
								     char** table_name, 
								     MEM_POOL* mem_pool);


#endif
