/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_common_util.h
 *
 *    Description:  һЩ�����Ĳ�������
 *
 *        Version:  1.0
 *        Created:  2011/06/30 
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yuzhong.zhao
 *        Company:  alipay
 *
 * =====================================================================================
 */



#ifndef COMMON_UTIL_H
#define COMMON_UTIL_H

#include <math.h>
#include "log.h"
#include "list.h"
#include "mem.h"
#include "def.h"

// string array
struct str_array_t {
	size_t n;
	char **strs;
};

// append string to all strings in str_array_t
struct str_array_t *all_str_append(const struct str_array_t *src, MEM_POOL_PTR mem, const char *fmt, ...) __attribute__((format(printf, 3, 4)));


/**
 * 	�Ƚ�����Ԫ��a��b�Ĵ�С
 *	@param 	a
 *	@param 	b
 *	@param 	field_type	Ԫ�ص���������
 *	@return				��a>bʱ������1��a<bʱ������-1��a=bʱ������0�����������֧�ֵ��������ͣ�����-2
 */
int8_t compare(const void* a, const void* b, enum field_types field_type);






/**
 *	��������ָ��ָ�������
 *	@param a
 *	@param b
 *  @param len			ָ�����ݵ��ֽڳ���
 */
void swap(void* a, void* b, uint32_t len);






/**
 * 	�Ƚ�����Ԫ��a��b�Ĵ�С
 *	@param 	a
 *	@param 	b
 *	@param 	field_type	Ԫ�ص���������
 *	@return				��a>bʱ������1��a<bʱ������-1��a=bʱ������0�����������֧�ֵ��������ͣ�����-2
 */
int8_t compare_ld(const struct low_data_struct* a, const struct low_data_struct* b);





/**
 * 	��һ��low data�е����ݿ�������һ��low data��
 *	@param 	pMemPool	�ڴ��
 *	@param 	des			Ŀ������
 *	@param	src			Դ����
 *	@return				
 */
void copy_low_data_struct(MEM_POOL_PTR pMemPool, struct low_data_struct* des, struct low_data_struct* src);




int32_t is_ld_equal(struct low_data_struct* a, struct low_data_struct* b);



/**
 * 	��a��b�ĺͷŵ�a����
 *	@param 	a			
 *	@param 	b
 *	@return		�ɹ�����0,ʧ�ܷ���-1
 */
int32_t add_data(struct low_data_struct* a, struct low_data_struct* b);



/**
 * ��ldת��Ϊdouble����
 */
double ld_to_double(struct low_data_struct* ld);





struct select_row_t* init_select_row_t(MEM_POOL_PTR mem_pool, uint32_t n);


struct select_fields_t* init_select_fields_t(MEM_POOL_PTR mem_pool, uint32_t n);





/**
  * ��һ���������л���һ��low_data_struct
  * @param  rdata һ������
  * @param  mem_pool �ڴ��ģ��
  * @return ����low_data_struct
  **/ 
struct low_data_struct*  rowdata_to_lowdata(struct row_data* rdata,MEM_POOL* mem_pool);

/**
  * ��һ�����������л���һ��row_data
  * @param  rdata һ������
  * @param  mem_pool �ڴ��ģ��
  * @return ����row_data
  **/
struct row_data*  lowdata_to_rowdata(struct low_data_struct* rdata,MEM_POOL* mem_pool);


#endif
