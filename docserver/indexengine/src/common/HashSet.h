/*
 * HashSet.h
 *
 *  Created on: 2012-8-17
 *      Author: yuzhong.zhao
 */

#ifndef HASHSET_H_
#define HASHSET_H_
#include "def.h"
#include "MileIterator.h"
#include "Equals.h"
#include "HashCoding.h"
#include "MileList.h"



class HashSetIterator: public MileIterator{
private:
	uint32_t loc;
	MileIterator* current;
	uint32_t bucket_size;
	MileList** data_array;
public:
	HashSetIterator(uint32_t bucket_size, MileList** data_array);
	~HashSetIterator();
	virtual void First();
	virtual void Next();
	virtual int8_t IsDone();
	virtual void *CurrentItem();
};



class HashSet {
private:
	//��ֵ����
	Equals* equal_func;
	//���뺯��
	HashCoding* hash_func;
	//Ԫ�ظ���
	uint32_t num;
	//Ͱ�Ĵ�С
	uint32_t bucket_size;
	//�洢���ݵ�����
	MileList** data_array;
	//�ڴ��
	MEM_POOL_PTR mem_pool;
public:
	HashSet(Equals* equals, HashCoding* hash, MEM_POOL_PTR mem_pool);
	~HashSet();
	void Add(void* data);
	void* Get(void* data);
	int Contains(void* data);
	int Size();
	MileIterator* CreateIterator();
};

#endif /* HASHSET_H_ */
