/*
 * MileHashMap.h
 *
 *  Created on: 2012-8-27
 *      Author: yuzhong.zhao
 */

#ifndef MILEHASHMAP_H_
#define MILEHASHMAP_H_

#include "def.h"
#include "MileIterator.h"
#include "Equals.h"
#include "HashCoding.h"
#include "MileList.h"



class MileEntry{
public:
	void* key;
	void* value;
};




class MileHashMapIterator: public MileIterator{
private:
	uint32_t loc;
	MileIterator* current;
	uint32_t bucket_size;
	MileList** data_array;
public:
	MileHashMapIterator(uint32_t bucket_size, MileList** data_array);
	~MileHashMapIterator();
	virtual void First();
	virtual void Next();
	virtual int8_t IsDone();
	virtual void *CurrentItem();
};



class MapValueIterator : public MileIterator{
private:
	MileHashMapIterator* iter;
public:
	MapValueIterator(MileHashMapIterator* iter);
	~MapValueIterator();
	virtual void First();
	virtual void Next();
	virtual int8_t IsDone();
	virtual void *CurrentItem();
};



class MileHashMap {
private:
	//��ֵ����
	Equals* equal_func;
	//���뺯��
	HashCoding* hash_func;
	//Ͱ�Ĵ�С
	uint32_t bucket_size;
	//�洢���ݵ�����
	MileList** data_array;
	//
	uint32_t num;
	//�ڴ��
	MEM_POOL_PTR mem_pool;
public:
	MileHashMap(Equals* equals, HashCoding* hash, MEM_POOL_PTR mem_pool);
	~MileHashMap();
	void Put(void* key, void* value);
	void* Get(void* key);
	int Contains(void* key);
	int Size();
	MileIterator* CreateIterator();
	MileIterator* CreateValueIterator();
};

#endif /* MILEHASHMAP_H_ */
