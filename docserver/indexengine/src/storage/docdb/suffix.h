/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_suffix.h
 *
 *    Description:  ��hash������filter�������沨��ʽ����
 *
 *        Version:  1.0
 *        Created:  2011/05/06 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi, yuzhong.zhao
 *        Company:  alipay
 *
 * =====================================================================================
 */



#include "../../common/def.h"
#include "../../common/list.h"
#include "../../protocol/packet.h"
#include "set_operator.h"
#include "db.h"

#ifndef SUFFIX_H
#define SUFFIX_H


//hashջ�����Ϣ
struct hash_stack_node{
	struct list_head  node_list;
	struct list_head* hash_result;
};





/**
  * ����hash�沨�����ʽ��������
  * @param  table_name
  * @param  conditions ��ϣ�沨�����ʽ
  * @param  mem_pool �ڴ��
  * @return �ɹ�����segnment_query_rowids�Ľ����
  **/ 
struct list_head* query_by_hash_conditions(char *table_name, struct condition_array* conditions, struct list_head *seg_list, MEM_POOL* mem_pool);




#endif
