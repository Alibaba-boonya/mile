/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_segment.h
 *
 *    Description:  �εĹ�����Ϣ���壬�Լ��ӿڶ��壬�൱���ӱ�ĸ���
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

#ifndef SEGMENT_H 
#define SEGMENT_H

#include "index_field.h"
#include "data_field.h"
#include "bitmark.h"
#include "../../common/string_map.h"

#define MAX_SEGMENT_NAME 100

#define MAX_INDEX_FIELD_NUM 100

#define MAX_INDEX_PER_FIELD 10

#define INDEX_FIELD_KEY "%s %d"


/*����״̬*/
enum segment_stat{
	SEGMENT_INIT  = 0x01,  /*��ʾ�ö��ѳ�ʼ��*/
    SEGMENT_DUMP  = 0x02,      /*��ʾ�ö��ѱ�dump*/
    SEGMENT_FULL  = 0x04,        /*��ʾ�ö��Ѿ�����*/
    SEGMENT_COMPRESS = 0x08     /*��ʾ�ö��Ѿ�ѹ��*/
};


struct index_field_stat{
	/*��������*/
	enum index_key_alg index_type;

	enum field_types data_type;

	/*1:�����Ѿ��������  0:��ʾ�������ڽ��� -1:��ʾ����ʧ��*/
	uint8_t flag;
}__attribute__ ((packed));


/*���������ͣ���table��ά��*/
struct index_field_meta{
	/*����*/
	char field_name[MAX_FIELD_NAME];

	/*��������*/
	uint8_t index_count;

	/*������Ϣ*/
	struct index_field_stat indexs[MAX_INDEX_PER_FIELD];

}__attribute__ ((packed));


struct segment_config{
	char work_space[FILENAME_MAX_LENGTH];

	/*����*/
	char segment_name[MAX_SEGMENT_NAME];
	
	/*�������������Լ�������Ϣ*/
	uint8_t index_field_count;

	/*��������Ϣ*/
	struct index_field_meta* index_fields;
	
	/*ѹ���ȣ���Ҫ�������ļ����ȡ*/
	uint32_t hash_compress_num;
	
	uint32_t row_limit;

	struct segment_meta_data* meta_data;

	/*�κ�*/
	uint16_t sid;
};


struct segment_meta_data{
	/*����ʱ�� ��־û�*/
	time_t create_time;

	/*����޸�ʱ�� ��־û�*/
	time_t modify_time;

	/*�����еı�־λ*/
	time_t checkpoint_time;

    /*�εı�־*/
	uint8_t flag;
	
	/*�Ѳ��������������Ժ���Ҫ�������������ֵ������doc id ��־û�*/
	uint32_t row_count;

	/*��ɾ������������־û�*/
	uint32_t del_count;
}__attribute__ ((packed));


struct segment_manager{
	/*����·��*/
	char work_space[FILENAME_MAX_LENGTH];

	/*����*/
	char segment_name[MAX_SEGMENT_NAME];

	/*�ε�Ԫ������Ϣ�ļ���*/
	char meta_filename[FILENAME_MAX_LENGTH];

	/*�κ�*/
	uint16_t sid;

	/*ԭʼ������*/
	struct data_field_manager* data_field;

	/*�������ǣ�������+����������Ϊkeyֵ��index_field_manager��Ϊvalueֵ*/
	struct string_map* index_fields;
	
	/*����*/
	uint32_t row_limit;

	/*�ε�Ԫ������Ϣ*/
	struct segment_meta_data* meta_data;

	/*ɾ�����*/
	struct bitmark_manager* del_bitmap;	

	/*ѹ���ȣ���Ҫ�������ļ����ȡ*/
	uint32_t hash_compress_num;
};


/**
  * �εĳ�ʼ������
  * @param  config segment������
  * @param mem_pool �ڴ��ģ��
  * @return ����segment_info�ṹ
  **/ 
struct segment_manager* segment_init(struct segment_config* config, MEM_POOL* mem_pool);



/**
  * �����������������ͣ���ȡ�����е�ʵ��
  * @param  segment������
  * @param  field_name ����
  * @param  index_type ��������
  * @return ����index_field_manager�ṹ
  **/ 
struct index_field_manager* segment_get_index_instance(struct segment_manager* segment,char* field_name,enum index_key_alg index_type);


/**
  * ��ָ���Ķβ���һ��ԭʼ���ݼ�¼
  * @param  segment ����Ϣ
  * @param  rdata һ��������Ϣ
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_data_insert(struct segment_manager* segment,struct row_data* rdata,uint32_t docid, MEM_POOL *mem_pool);



/**
  * ��ָ���Ķθ���һ��ԭʼ���ݼ�¼
  * @param  segment ����Ϣ
  * @param  field_name ����
  * @param  docid �к�
  * @param  new_data �µ�������
  * @param  old_data �ϵ�����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_data_update(struct segment_manager* segment,
						  uint32_t docid, 
						  struct low_data_struct* new_data,
				   		  struct low_data_struct** old_data,
				   		  MEM_POOL* mem_pool);



/**
  * ��ָ���Ķθ���һ���������ݼ�¼��ֻ��filter����֧��
  * @param  segment ����Ϣ
  * @param  field_name ����
  * @param  docid �к�
  * @param  new_data �µ�������
  * @param  old_data �ϵ�����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_index_update(struct segment_manager* segment,
						   uint32_t docid, 
						   struct low_data_struct* new_data,
						   struct low_data_struct** old_data,
						   MEM_POOL* mem_pool);


/**
  * ����������docid���Ҷ�Ӧ��ԭʼֵ
  * @param  segment ����Ϣ
  * @param  field_name ����
  * @param  docid 
  * @param  mem_pool �ڴ��
  * @return �ɹ�����low_data_struct��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* segment_data_query_col(struct segment_manager* segment,char* field_name,uint32_t docid,MEM_POOL* mem_pool);



/**
  * ����docid��������һ�е�����
  * @param  segment ����Ϣ
  * @param  docid
  * @param  mem_pool �ڴ��
  * @return �ɹ�����row_data��ʧ�ܷ���NULL
  **/ 
struct row_data* segment_data_query_row(struct segment_manager* segment,uint32_t docid,MEM_POOL* mem_pool);


/**
  * ��ָ���Ķβ���һ��������¼
  * @param  segment ����Ϣ
  * @param  data һ��������Ϣ
  * @param  index_type ��������
  * @param  docid �к�
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_index_insert(struct segment_manager* segment,
						   struct low_data_struct* data,
						   enum index_key_alg index_type, 
						   uint32_t docid, 
						   MEM_POOL *mem_pool);

/**
  * ��������
  * @param  segment ����Ϣ
  * @param  field_name ����
  * @param  index_type ��������
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_ensure_index(struct segment_manager* segment,char* field_name,enum index_key_alg index_type, MEM_POOL* mem_pool);



/**
  * ɾ������
  * @param  segment ����Ϣ
  * @param  field_name ����
  * @param  index_type ��������
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32_t segment_del_index(struct segment_manager* segment,char* field_name,enum index_key_alg index_type);



/**
  * btree��Χ��ѯ������������row id list
  * @param  index
  * @param  range_condition ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list * segment_index_range_query(struct segment_manager* segment, char* field_name,\
		struct db_range_query_condition * range_condition, MEM_POOL* mem_pool);



/**
  * ����docid�������ַ���hash���64λ����
  * @param  index
  * @param  docid �к�
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* segment_index_value_query(struct segment_manager* segment,char* field_name,uint32_t docid,MEM_POOL* mem_pool);



/**
  * ����value�����Ҷ�Ӧ��row id lists����ֵ��ѯ��ֻ��hash����֧��
  * @param  field
  * @param  data ����
  * @param  row_count��ǰ�洢������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct rowid_list* segment_index_equal_query(struct segment_manager* segment,struct low_data_struct* data, enum index_key_alg index_type, MEM_POOL* mem_pool);


/**
  * ����value�����Ҷ�Ӧ��Ͱ��������ݣ���ֵ��ѯ��ֻ�зִ�����֧��
  * @param  segment
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����uint32��ʧ�ܷ���NULL
  **/ 
uint32_t segment_index_count_query(struct segment_manager* segment,struct low_data_struct* data,MEM_POOL* mem_pool);


/**
  * �λָ�
  * @param  
  **/
int32_t segment_recover(struct segment_manager* segment, uint32_t docid);

/**
  * ��ָ���Ķε������н���ѹ��
  * @param  segment ����Ϣ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/
int32_t segment_compress(struct segment_manager* segment,MEM_POOL* mem_pool);


/**
  * ��ָ���Ķε������н���ѹ���л�
  * @param  segment ����Ϣ
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/
int32_t segment_compress_switch(struct segment_manager* segment);


/**
  * �����кŽ���ɾ��
  * @param  segment ����Ϣ
  * @param  row_id �к�
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
int32_t segment_del_docid(struct segment_manager* segment,uint32_t docid);



/**
  * �ж�ָ����doc id�Ƿ���Ϊɾ��
  * @param  segment ����Ϣ
  * @param  docid �к�
  * @return ��ɾ������1��δɾ������0 
  **/
int32_t segment_is_docid_deleted(struct segment_manager* segment, uint32_t docid);


/**
  * �������ݳɹ�ʱ�����ñ��
  * @param  segment ����Ϣ
  * @param  docid �к�
  * @return ����0��ʧ��<0
  **/
int32_t segment_set_docid_inserted(struct segment_manager* segment, uint32_t docid);


/**
  * �������ݳɹ�ʱ�����ñ��
  * @param  segment ����Ϣ
  * @param  flag �α��
  * @return 
  **/
void segment_set_flag(struct segment_manager* segment, enum segment_stat flag);


/**
  * ��ȡ�α��
  * @param  segment ����Ϣ
  * @return 
  **/
enum segment_stat segment_get_flag(struct segment_manager* segment);


/**
  * �ͷŵײ����ݽṹ�����ǲ��ͷ��ڴ�
  * @param  segment ����Ϣ
  **/
void segment_release(struct segment_manager* segment);


/**
  * ͬ��memap
  * @param  segment ����Ϣ
  **/
void segment_checkpoint(struct segment_manager* segment);


/**
  * ��ȡָ���εĵ�ǰ�к�
  * @param  segment ����Ϣ
  **/
uint32_t segment_get_rowcount(struct segment_manager* segment);


/**
  * set rowcount to docid + 1
  * @param  segment ����Ϣ
  * @param docid
  **/
void segment_set_rowcount(struct segment_manager* segment, uint32_t docid);


/**
  * �鿴ָ�����Ƿ񳬹�limit
  * @param  segment ����Ϣ
  * @return ��������1�����򷵻�0
  **/
int32_t segment_exceed_limit(struct segment_manager* segment);

/**
 * switch all index's mmap to real file, when segment full
 * @param segment
 */
int segment_mmap_switch( struct segment_manager *segment);

#endif //SEGMENT_H

