/*
 * =====================================================================================
 *
 *       Filename:  rowid_list.h
 *
 *    Description:  rowid�ļ��ϣ����ÿ�״��������ݽṹ�����ڲ��õ���������rowid�е�rowid������ģ�����ʽΪ����
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



#ifndef ROWID_LIST_H
#define ROWID_LIST_H
#include "../../common/log.h"
#include "../../common/mem.h"
#include "../../common/list.h"
#include "../../common/def.h"


#define ROWID_ARRAY_SIZE 256


typedef int32_t (*ROWID_LIST_FUNC) (uint32_t, void*);



struct rowid_list_node{
	uint32_t rowid_array[ROWID_ARRAY_SIZE];
	double* score_array;
	struct rowid_list_node* next;
};


struct rowid_list{
	uint32_t rowid_num;
	struct rowid_list_node* head;
	struct rowid_list_node* tail;
};





/**
 * ��ʼ��һ��rowid_list
 *
 * @param pMemPool		�ڴ��
 * @retrun				ָ��rowid_list��ָ��
 */
struct rowid_list* rowid_list_init(MEM_POOL_PTR pMemPool);





/**
 * ��rowid_list������µ�rowid��ע�����ݿ��е�rowid�����ǽ����
 *
 * @param pMemPool		�ڴ��
 * @param id_list		rowid������
 * @param rowid
 */
void rowid_list_add(MEM_POOL_PTR pMemPool, struct rowid_list* id_list, uint32_t rowid);






void rowid_list_batch_add(MEM_POOL_PTR pMemPool, struct rowid_list* id_list, uint32_t* rowids,uint32_t num);


/**
 * ��������rowid_list
 *
 * @param id_list		rowid������
 * @param func			��ÿ��rowid���в����ĺ���
 * @param deadline_time	��ʱʱ��
 * @param user_data		func��������Ҫ����������
 * @return				�ɹ�ʱ����MILE_RETURN_SUCCESS�� �쳣ʱ����ֵ<0
 */
int32_t rowid_list_for_each(struct rowid_list* id_list, ROWID_LIST_FUNC func, int64_t deadline_time, void* user_data);





/**
 * �ж�����rowid_list�Ƿ���ͬ
 *
 * @param lista			
 * @param listb			
 * @return				����rowid_list��ͬʱ, ����1; ���򷵻�0
 */
int32_t rowid_list_equals(struct rowid_list* lista, struct rowid_list* listb);





/* 
 * ===  FUNCTION  ======================================================================
 *         Name:  rowid_qsort
 *  Description:  ��һ����״����rowlist����rowid���������򣬴Ӵ�С
 *  	@param row_array �洢���ǿ�״����������ָ��
 *  	@param start ����rowid�Ŀ�ʼ��λ��
 *  	@param end   ����rowid����λ��
 * 		@return 
 * =====================================================================================
 */
void rowid_qsort(struct rowid_list_node ** row_array, int start, int end);





/* 
 * ===  FUNCTION  ======================================================================
 *         Name:  print_rowid_list
 *  Description:  ���id_list������rowid
 *  	@param 	id_list
 * 		@return 			
 * =====================================================================================
 */
void print_rowid_list(struct rowid_list* id_list);




/* 
 * ===  FUNCTION  ======================================================================
 *         Name:  print_rowid_list_to_buffer
 *  Description:  ���id_list������rowid��buffer����
 *  	@param 	id_list
 *		@param	buffer		�ַ�����
 *		@param	size		�ַ�����Ĵ�С
 * 		@return 			д����ַ���
 * =====================================================================================
 */
int32_t print_rowid_list_to_buffer(struct rowid_list* id_list, char* buffer, int32_t size);







#endif
