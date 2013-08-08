/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_set_operator.h
 *
 *    Description:  ��rowid���ϵĽ���������������㣬����hash��������
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

#ifndef SET_OPERATOR_H
#define SET_OPERATOR_H
#include "rowid_list.h"

struct segment_query_rowids {
	/*segment id��*/
	uint16_t sid;

	/*�ö�����docid��*/
	uint32_t max_docid;

	/*doc id list*/
	struct rowid_list * rowids;

	/*�б�*/
	struct list_head rowids_list;
};

/**
 *	������segment���Ӧ��rowid���ϵĲ�����ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ�
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_union(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb);

/**
 *	������segment���Ӧ��rowid���ϵĽ�����ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_intersection(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb);

/**
 *	������segment���Ӧ��rowid���ϵĲ��ע�⼯�������ı������е�����
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct list_head* seg_rowid_minus(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb);



struct list_head* seg_rowid_fulltext_hence(MEM_POOL_PTR pMemPool,
		struct list_head* lista, struct list_head* listb);


int32_t seg_rowid_count(struct list_head* seglist);


void seg_rowid_setscore(MEM_POOL_PTR pMemPool, struct list_head* seglist,
		double score);

void print_seg_rowid_list(struct list_head* list);

/**
 *	������rowid���ϵĲ�����ע�⼯�������ı������е����ݣ����������lista��
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_union(MEM_POOL_PTR pMemPool, struct rowid_list* lista,
		struct rowid_list* listb);

/**
 *	������rowid���ϵĽ�����ע�⼯�������ı������е����ݣ����������lista��
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_intersection(MEM_POOL_PTR pMemPool,
		struct rowid_list* lista, struct rowid_list* listb);

/**
 *	������rowid���ϵĲ��ע�⼯�������ı������е����ݣ����������lista��
 *	@param		pMemPool	�ڴ�أ���Ŀǰ��ʵ���в���ͨ���ڴ�����������ڴ棬���Ժ���Ż������п��ܻ������������ڴ� 
 *	@param 		lista
 *	@param 		listb
 *	@return		
 */
struct rowid_list* rowid_minus(MEM_POOL_PTR pMemPool, struct rowid_list* lista,
		struct rowid_list* listb);


struct rowid_list* rowid_fulltext_hence(MEM_POOL_PTR pMemPool,
		struct rowid_list* lista, struct rowid_list* listb);


void rowid_list_setscore(MEM_POOL_PTR mem_pool, struct rowid_list* id_list,
		double score);

#endif
