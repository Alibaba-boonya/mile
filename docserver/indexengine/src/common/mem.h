/*
 * =====================================================================================
 *
 *       Filename:  mem.h
 *
 *    Description:  simple mem manager
 *
 *        Version:  1.0
 *        Created:  2011/02/16 16ʱ44��36��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  liang.chenl 
 *        Company:  
 *
 * =====================================================================================
 //�ڴ�ĵ�ַ*/


#ifndef MEM_H
#define MEM_H

#include "def.h"
#include "list.h"

#ifndef VALGRIND_MEM_TEST

typedef struct mem_pool {
    void* address; 
    uint32_t  size;      //�ڴ�����С
    int32_t pos;        //��ǰ�ڴ��λ��
    struct mem_pool* next;    //�Ѿ��ù����ڴ��б�
    uint8_t thread_safe;
	pthread_mutex_t thread_safe_locker;
} MEM_POOL, *MEM_POOL_PTR;

#else

typedef struct mem_pool {
    uint32_t  size;      //�ڴ�����С
    struct list_head mem_list_h;
} MEM_POOL, *MEM_POOL_PTR;

struct mem_ptr {
	void * ptr;
	struct list_head mem_list;
};

#endif // VALGRIND_MEM_TEST

/* *
 *�ڴ�س�ʼ��
 * @param size : �ڴ��С��λ(byte)
 * @return : ������ڴ��ָ��
 * */
MEM_POOL_PTR mem_pool_init(size_t size);  
/* *
 * ���ڴ������Ϊ�̰߳�ȫ
 * @param �ڴ��
 * 
 * */
void mem_pool_set_threadsafe(MEM_POOL_PTR pMemPool);
/* *
 * ��һ���ڴ�ط���һ��С�ڴ� ,��ǰ�鲻��ʱ���ڴ�ػ���չ
 * @param size : Ҫ������ڴ��С(byte)
 * 
 * */
void* mem_pool_malloc(MEM_POOL_PTR pMemPool, size_t size);
/* *
 * ���ڴ�أ��ָ�����ʼ״̬
 * (1) ʹ��λ������0
 * (2) �ͷ�������ڴ�
 * */
void mem_pool_reset(MEM_POOL_PTR pMemPool);

/* *
 * ���ڴ����ȫ����
 * */
void mem_pool_destroy(MEM_POOL_PTR pMemPool);                                                                                      

#ifdef __cplusplus
// for c++ placement new
#define NEW(pool, CLASS) new(mem_pool_malloc((pool), sizeof(CLASS))) CLASS

// call destructor
template <typename T>
inline void DELETE(T *p) { p->~T(); }

#endif // __cplusplus

#endif //MEM_H
