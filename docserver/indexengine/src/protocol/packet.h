/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_packet.h
 *
 *    Description:  ��merge serverͨ�ŵı������ݽṹ
 *
 *        Version:  1.0
 *        Created:  2011/05/06 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yuzhong.zhao
 *        Company:  alipay
 *
 * =====================================================================================
 */


#ifndef PACKET_H
#define PACKET_H

#include "../common/profiles.h"
#include "../common/common_util.h"
#include "../common/ResultSet.h"
#include "../common/HashSet.h"
#include "../common/MileArray.h"
#include "../storage/binlog.h"
#include "../storage/MileHandler.h"
#include "databuffer.h"
#include "subselect.h"


/* define message type */
#define MT_VG_MD 0x2100 // merge 2 document  operate command type
#define MT_MD_EXE_INSERT  (MT_VG_MD | 0x01)
#define MT_MD_EXE_DELETE  (MT_VG_MD | 0x02)
#define MT_MD_EXE_DELETE_BY_ID  (MT_VG_MD | 0x12)
#define MT_MD_EXE_UPDATE  (MT_VG_MD | 0x03)
#define MT_MD_EXE_UPDATE_BY_ID  (MT_VG_MD | 0x13)
#define MT_MD_EXE_QUERY  (MT_VG_MD | 0x04)
#define MT_MD_EXE_SPEC_QUERY  (MT_VG_MD | 0x05)
#define MT_MD_EXE_EXPORT (MT_VG_MD | 0x06)
#define MT_MD_EXE_GET_KVS  (MT_VG_MD | 0x21)  // get info by ids
#define MT_MD_GET_STATE	(MT_VG_MD | 0x22)    // get the docserver state


#define MT_VG_DM  0x2200  // document 2 merge  maintain command type
#define MT_DM_RS  (MT_VG_DM | 0x01) // exe reponse
#define MT_MD_HEART (MT_VG_DM | 0x31) // heartbeat
#define MT_DM_SQL_EXC_ERROR	(MT_VG_DM | 0x02) //error response
#define MT_DM_STATE_RS	(MT_VG_DM | 0x03) //state response
#define MT_DM_SQ_RS (MT_VG_DM | 0x04) // specify query response

#define MT_VG_MT_S  0x4100 // maintain client message
#define MT_VG_MT_R  0x4200 // maintain server message

#define MT_VG_SM 0x5100 // slave to master
#define MT_SM_GET_BINLOG (MT_VG_SM | 0x01 ) // get binlog

#define MT_VG_COMMON_R  0x6200 // common response
#define MT_COMMON_OK  (MT_VG_COMMON_R | 0x01) // OK
#define MT_COMMON_ERROR  (MT_VG_COMMON_R | 0x02) // ERROR


// CD : client to docserver
#define MT_VG_CD 0x7000
// SC : storage control
#define MT_VG_SC 0x7100
#define MT_VG_DC 0x7200

// storage engine control message type
#define MT_CD_EXE_LOAD (MT_VG_SC | 0x01)  //���ض�
#define MT_CD_EXE_UNLOAD (MT_VG_SC | 0x02)  //ж�ض�
#define MT_CD_EXE_INDEX (MT_VG_SC | 0x03)  //�½�����
#define MT_CD_EXE_UNINDEX (MT_VG_SC | 0x04)  //��������
#define MT_CD_EXE_COMPRESS (MT_VG_SC | 0x05)  //ѹ��
#define MT_CD_STAT (MT_VG_SC | 0x06)  //docid״̬��ѯ
#define MT_CD_EXE_CP (MT_VG_SC | 0x07)  //checkpoint
#define MT_CD_EXE_REPLACE (MT_VG_SC | 0x08)  //checkpoint
#define MT_CD_EXE_LDB_CONTROL (MT_VG_SC | 0x0b) // ldb storage engine control message

// client to docserver but not storage control
#define MT_CD_EXE_GET_LOAD_THRESHOLD (MT_VG_CD | 0x09)  // get load threshold
#define MT_CD_EXE_SET_LOAD_THRESHOLD (MT_VG_CD | 0x0a)  // set load threshold

#define MT_VG_TEST_REQ 0x9800
#define MT_TEST_REQ_ECHO (MT_VG_TEST_REQ | 0x00) // echo request message for test

#define MT_VG_TEST_RES 0x9900
#define MT_TEST_RES_ECHO (MT_VG_TEST_RES | 0x00) // echo response message for test


/*docserver���ؿͻ���С���ߵ�����*/
#define MT_DC_EXE_RS (MT_VG_DC | 0x01)  //�����ִ�н�� 64λ����
#define MT_DC_STAT_RS (MT_VG_DC | 0x02)    //״̬�Ĳ�ѯ
#define MT_DC_ERROR (MT_VG_DC | 0x03) // error packet
#define MT_DC_LDB_CONTROL (MT_VG_DC | 0x04) // ldb control result


#define ACCESS_TYPE_COMMON 0
#define ACCESS_TYPE_DISTINCT 5
#define ACCESS_TYPE_DISTINCT_COUNT 6

#define DEFAULT_QUERY_RESULT_DATASET_SIZE 1024

//mile��Ϣ���İ�ͷ
struct mile_message_header{
	//��Ϣ����,���������4���ֽ�
	uint32_t message_length; 
	//Э�����汾��
	uint8_t version_major;
	//Э��ΰ汾��
	uint8_t version_minor;
	//��Ϣ����
	uint16_t message_type;	
	//��Ϣid
	uint32_t message_id;
};

//�����е�����
enum condition_type{
	//���ʽ
	CONDITION_EXP = 1,
	//�߼���
	LOGIC_AND = 2,
	//�߼���
	LOGIC_OR = 3,
	//���ϲ
	HC_SET_MINUS = 123,	
	//�����������ʽ
	HC_UNION_HASH_EXP = 19
};

// ѡ��������
enum select_field_type {
	VALUE_SELECT    = 0,
	FUNCTION_SELECT = 1,
	STAR_SELECT     = 2
};



// condition expression
struct condition_t {
	// condition type
	enum condition_type type;
	// field name
	char *field_name;
	// comparator
	enum compare_type comparator;
	// value number, ��comparator��between��inʱ��value�����ж��
	uint32_t value_num;
	// values
	struct low_data_struct *values;
	// value��hashֵ�����飬���ڼ�������еĲ�ѯ�Ż�
	struct low_data_struct *hash_values;
};

// condition expression array
struct condition_array {
	uint32_t n;
	struct condition_t *conditions;
};

struct select_field_t {
	// select type
	enum select_field_type type;
	// field name (refer field name for function)
	char *field_name;
	// alise name
	char *alise_name;
	// function type
	enum function_type func_type;
	// name for function select
	char *select_name;
	// expression for range function
	struct condition_array* range_exp;
	// normalized value array's index, -1 for STAR_SELECT
	int32_t value_index;
};

struct select_field_array {
	uint32_t n;
	struct select_field_t *select_fields;
};

// group field
struct group_field_t {
	char *field_name;

	// normalized value array's index.
	int32_t value_index;
};

// group field array
struct group_field_array {
	uint32_t n;
	struct group_field_t *group_fields;
};

// order field
struct order_field_t {
	char *field_name;
	enum order_types order_type;

	// normalized value array's index.
	int32_t value_index;
};

// order field array
struct order_field_array {
	uint32_t n;
	struct order_field_t *order_fields;
};


// insert packet
struct insert_packet{
	// ���� 
	char *table_name;
	// ����
	uint32_t column_num;
	// ָ��������ݵ�����
	struct low_data_struct *datas;
};

// query packet
struct query_packet {
	// ��ѯ����, �Ƿ�distinct
	uint16_t access_type;
	// table name
	char *table_name;
	// ѡ��������
	struct select_field_array select_field;
	// ��ʾ������, ����4��, �ֱ��Ƕδ���ʱ����Сֵ���δ���ʱ�����ֵ�����޸�ʱ����Сֵ�����޸�ʱ�����ֵ
	struct hint_array hi_array;
	// indexwhere expression
	struct condition_array index_cond;
	// filter expression
	struct condition_array filter_cond;
	// group feilds
	struct group_field_array group_array;
	// group order fields
	struct order_field_array group_order_array;
	// group limit;
	uint32_t group_limit;
	// order fields
	struct order_field_array order_array;
	// limit
	uint32_t limit;
};




// update packet 
struct update_packet {
	// table name
	char *table_name;
	// update field
	char *field_name;
	// update filed data
	struct low_data_struct data;
	// segment hint
	struct hint_array hi_array;
	// indexwhere expression
	struct condition_array index_cond;
	// filter expression
	struct condition_array filter_cond;
};



// delete packet
struct delete_packet {
	// table name
	char *table_name;
	// segment hint
	struct hint_array hi_array;
	// indexwhere expression
	struct condition_array index_cond;
	// filter expression
	struct condition_array filter_cond;
};


// export packet
struct export_packet {
	char *table_name; // table name
	char *save_path; // result save path
	struct hint_array hi_array; // segment hint for docdb
	struct condition_array index_cond;
	struct condition_array filter_cond;
	int64_t limit;
};



struct set_load_threshold_packet {
	double value;
};



//ͳ����Ϣ
struct stat_info{
	uint16_t name_len;
	uint8_t* name;
	struct low_data_struct* ldata;
};


//ͳ����Ϣ������
struct stat_info_array{
	//���鳤��
	uint32_t n;
	//ͳ����Ϣ
	struct stat_info *stat;
};




//mergeserver��docserver��״̬��ѯ��
struct get_state_packet{
	//��ȡ״̬�ĸ���
	uint32_t n;
	//��ȡ״̬������
	uint8_t** state_names;
};




/**
 *	��rbuf�ж�ȡ��̬�������ͣ�������mile�ĵײ�洢������
 *	@param 	pMemPool			�ڴ��
 *  @param	rbuf				read buffer
 *	@param 	ldata				mile�ײ�Ĵ洢�ṹ
 *	@return						������
 */
int32_t read_dyn_value(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf, struct low_data_struct* ldata);



/**
 *	��sbuf��д�붯̬��������
 *  @param	sbuf				send buffer
 *	@param 	ldata				mile�ײ�Ĵ洢�ṹ
 *	@return						������
 */
int32_t write_dyn_value(struct data_buffer* sbuf, struct low_data_struct* ldata);



/**
 *	�����������ݰ�
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						��������, ��������ʱ����null
 */
struct insert_packet* parse_insert_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);



/**
 *	����ɾ�����ݰ�
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						ɾ������, ��������ʱ����null
 */
struct delete_packet* parse_delete_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);

/**
 * parse export packet
 */
struct export_packet *parse_export_packet(MEM_POOL_PTR mem, struct data_buffer *rbuf);




/**
 *	�����������ݰ�
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						��������, ��������ʱ����null
 */
struct update_packet* parse_update_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);




int32_t parse_select_field_array(MEM_POOL_PTR mem, struct data_buffer *rbuf, struct select_field_array *select_field);

/**
 *	������ѯ���ݰ�
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						��ѯ����, ��������ʱ����null
 */
struct query_packet* parse_query_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);






/**
 *	�����ѯ������ݰ�����sbuf��д����Ӧ����
 *	@param	result				��ѯ�����
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_query_result_packet(ResultSet* result, struct mile_message_header* msg_head, struct data_buffer* sbuf);




/**
 *	������������ݰ�����sbuf��д����Ӧ����
 *	@param	docid				�������ݵ�docid
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_insert_result_packet(uint64_t docid, struct mile_message_header* msg_head, struct data_buffer* sbuf);




/**
 *	������½�����ݰ�����sbuf��д����Ӧ����
 *	@param	update_num			�������ݵ�����
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_update_result_packet(uint32_t update_num, struct mile_message_header* msg_head, struct data_buffer* sbuf);




/**
 *	����ɾ��������ݰ�����sbuf��д����Ӧ����
 *	@param	delete_num			ɾ�����ݵ�����
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_delete_result_packet(uint32_t delete_num, struct mile_message_header* msg_head, struct data_buffer* sbuf);

/**
 * generate export result packet
 */
void gen_export_result_packet(uint64_t export_num, struct mile_message_header *msg_head, struct data_buffer *sbuf);

/**
 *	������������ݰ�����sbuf��д����Ӧ����
 *	@param	result_code			������
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_error_packet(int32_t result_code, struct mile_message_header* msg_head, struct data_buffer* sbuf);







/**
 * parse slave_sync_req
 */
struct slave_sync_req *parse_slave_sync_req( MEM_POOL_PTR mem, struct data_buffer *rbuf );




/**
 * generate get binlog response packet
 */
void gen_slave_sync_res_packet(struct slave_sync_res *binlog_res, struct mile_message_header* msg_head, struct data_buffer* sbuf);




/**
 * generage get binlog request packet
 */
void gen_slave_sync_req_packet( uint32_t message_id, uint64_t offset, struct data_buffer *sbuf);








/**
 *	����get_state��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						get_state����, ��������ʱ����null
 */
struct get_state_packet* parse_get_state_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);





/**
 *	����״̬������ݰ�����sbuf��д����Ӧ����
 *	@param	stat_array			״̬��Ϣ
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_state_result_packet(struct stat_info_array* stat_array, struct mile_message_header* msg_head, struct data_buffer* sbuf);






// TODO
struct set_load_threshold_packet *parse_set_load_threshold_packet(MEM_POOL_PTR pMemPool, struct data_buffer *rbuf);


// TODO
void gen_dc_get_load_threshold_packet(double load, struct mile_message_header* msg_head, struct data_buffer* sbuf);

// TODO
void gen_dc_set_load_threshold_packet(double load, struct mile_message_header* msg_head, struct data_buffer* sbuf);

// docsever to client error packet
void gen_docserver_client_error_packet(uint32_t rc, struct mile_message_header *msg_head, struct data_buffer *sbuf);


#endif
