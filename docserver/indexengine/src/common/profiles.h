/*
 * =====================================================================================
 *
 *       Filename:  hi_profiles.h
 *
 *    Description:  ��¼ÿ���Ĳ�ѯʱ�䣬������ֵ�����ӡ����־��
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


#ifndef PROFILES_H
#define PROFILES_H
#include <pthread.h>
#include <sys/time.h>
#include "list.h"
#include "def.h"
#include "mem.h"

#define MESSAGE_LEN 128


#define PROFILER_START(s) do { if(get_globle_profile()->status == 1) start_profile((s)); } while(0)
#define PROFILER_STOP() do { if(get_globle_profile()->status == 1) reset_profile(); } while (0)
#define PROFILER_BEGIN(s) do { if(get_globle_profile()->status == 1) begin_profile((s)); } while(0)
#define PROFILER_END() do { if(get_globle_profile()->status == 1) end_profile(); } while(0)
#define PROFILER_DUMP() do { if(get_globle_profile()->status == 1) dump_profile(); } while(0)
#define PROFILER_SET_THRESHOLD(sh) get_globle_profile()->threshold = (sh)
#define PROFILER_SET_STATUS(st) get_globle_profile()->status = (st)



struct entry{
	struct entry* first;
	struct entry* parent;
	
	/*��һ��ջ��ջ��ͷ*/
	struct list_head entry_list_h;

	struct list_head entry_list;

	/*��¼ջ�ĸ���*/
	uint16_t entry_count;

	char message[MESSAGE_LEN];

	uint64_t stime;
	uint64_t btime;
	uint64_t etime;
};


struct profiles{
	struct entry* e;
	uint16_t threshold;
	int16_t status;
	pthread_key_t entry_key;
	pthread_key_t mem_key;
};


/**
 * ���߳�����ͳ�ƹ���
 *
 * ʹ�÷�����
 * PROFILER_START("test"); // ��ʼ��һ��ͳ��ʵ��
 *
 * PROFILER_BEGIN("entry a"); // ��ʼһ����ʱ��Ԫ
 * PROFILER_END(); // ��������ļ�ʱ��Ԫ
 *
 * PROFILER_BEGIN("entry b");
 * PROFILER_BEGIN("sub entry b1"); // ֧��Ƕ�׵ļ�ʱ��Ԫ
 * PROFILER_END();
 * PROFILER_END()
 *
 * PROFILER_DUMP(); // dump��ʱ��¼
 * PROFILER_STOP(); // �������ͳ��ʵ��
 *
 * ���ò�����
 * PROFILER_SET_STATUS(status); // ���ü�������״̬���������1������ü��������й��ܣ���ʱ��������κο�����Ĭ��Ϊ1
 * PROFILER_SET_THRESHOLD(threshold); // ����dump�ķ�ֵ����һ������ʵ�����ܼ�ʱ���������ֵʱ�Ż�dump��Ϣ����λΪus��Ĭ��Ϊ10000us(10ms)
 */

int32_t init_profile(uint16_t threshold,MEM_POOL* mem_pool);

void start_profile(char* description);

void stop_profile();

void begin_profile(char* description);

void end_profile();

void dump_profile();

void reset_profile();

uint64_t get_time();


struct profiles* get_globle_profile();
#endif

