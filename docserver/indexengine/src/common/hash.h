/*
 * =====================================================================================
 *
 *       Filename:  hi_hash.h
 *
 *    Description:  hash�����Ķ���
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


#include "../common/def.h"


#ifndef HASH_H
#define HASH_H

/*�ж��Ƿ�Ϊ����*/
#define ISNUM(ch) (ch) >= '0' && (ch) <= '9'

/*������ֽ���*/
#define MAX_HASH_LEN 128



/**
  * ����types����ȡ64λ��hashֵ
  * @param  data ����
  * @param  types ��������
  * @return ����64λ��ϣ���ֵ
  **/ 
uint64_t get_hash_value(struct low_data_struct* data);

#endif
