/*
 * docdb_packet.h
 *
 *  Created on: 2012-8-28
 *      Author: yuzhong.zhao
 */

#ifndef DOCDB_PACKET_H_
#define DOCDB_PACKET_H_

#include "../../protocol/packet.h"
#include "segment.h"

struct load_segment_packet{
	char* table_name;
	uint16_t sid;
	char* segment_dir;
};

struct replace_segment_packet{
	char* table_name;
	char* segment_dir;
};


struct unload_segment_packet{
	char* table_name;
	uint16_t sid;
};

struct compress_packet{
	char* table_name;
};

struct ensure_index_packet{
	char* table_name;
	char* field_name;
	enum index_key_alg index_type;
	enum field_types data_type;
};

struct del_index_packet{
	char* table_name;
	char* field_name;
	enum index_key_alg index_type;
};

struct doc_stat_packet{
	char* table_name;
	uint8_t type; /*1 �ε�״̬ 2������״̬*/
};



// delete by docids packet
struct delete_docid_packet {
	// table name
	char *table_name;
	// docid number
	uint32_t docid_num;
	// docids
	uint64_t *docids;
};


// update by docid
struct update_docid_packet {
	// table name
	char *table_name;
	// update field
	char *field_name;
	// update filed data
	struct low_data_struct data;
	// docid number
	uint32_t docid_num;
	// docids
	uint64_t *docids;
};



// get kvs packet
struct get_kvs_packet {
	// table name
	char *table_name;
	// docid number
	uint32_t docid_num;
	// docids
	uint64_t *docids;
	// select fields
	struct select_field_array select_field;
};




/**
 *	����delete_by_id����
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						delete_by_id����, ��������ʱ����null
 */
struct delete_docid_packet* parse_delete_docid_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);




/**
 *	����update_by_id��
 *  @param	mem   	 			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						update_by_id����, ��������ʱ����null
 */
struct update_docid_packet *parse_update_docid_packet(MEM_POOL_PTR mem, struct data_buffer *rbuf);




/**
 *	����get_kvs��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						get_kvs����, ��������ʱ����null
 */
struct get_kvs_packet* parse_get_kvs_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����load_segment_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						load segment����, ��������ʱ����null
 */
struct load_segment_packet* parse_load_segment_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����replace_segment_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						load segment����, ��������ʱ����null
 */
struct replace_segment_packet* parse_replace_segment_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����unload_segment_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						unload segment����, ��������ʱ����null
 */
struct unload_segment_packet* parse_unload_segment_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����ensure_index_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						ensure index����, ��������ʱ����null
 */
struct ensure_index_packet* parse_ensure_index_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����del_index_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						del index����, ��������ʱ����null
 */
struct del_index_packet* parse_del_index_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����doc_stat_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						doc stat����, ��������ʱ����null
 */
struct doc_stat_packet* parse_doc_stat_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);


/**
 *	����compress_packet��
 *  @param	pMemPool			�ڴ��
 *	@param 	rbuf				read buffer
 *	@return						compress����, ��������ʱ����null
 */
struct compress_packet* parse_compress_packet(MEM_POOL_PTR pMemPool, struct data_buffer* rbuf);



/**
 *	����docserver�ͻ��˲�ѯ������ݰ�����sbuf��д����Ӧ����
 *	@param	result_code			��ѯ�����
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_dc_response_packet(int32_t result_code, struct mile_message_header* msg_head, struct data_buffer* sbuf);


/**
 *	����docserver�ͻ��˲�ѯ��״̬���ݰ�����sbuf��д����Ӧ����
 *  @param  meta_data           �ε�Ԫ������Ϣ
 *  @param  max_segment_num     ��������
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_dc_segment_stat_packet(struct segment_meta_data* meta_data,uint16_t max_segment_num,struct mile_message_header* msg_head, struct data_buffer* sbuf);



/**
 *	����docserver�ͻ��˲�ѯ����״̬���ݰ�����sbuf��д����Ӧ����
 *  @param  meta_data           ������Ԫ������Ϣ
 *  @param  index_field_count     ��������
 *	@param	msg_head			���ݰ�ͷ
 *	@param 	sbuf				sender buffer
 *	@return
 */
void gen_dc_index_stat_packet(struct index_field_meta* meta_data,uint16_t index_field_count,struct mile_message_header* msg_head, struct data_buffer* sbuf);





#endif /* DOCDB_PACKET_H_ */
