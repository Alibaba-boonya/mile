/*
 * =====================================================================================
 *
 *       Filename:  block_manage.cpp
 *
 *    Description: 	block_manage ����  
 *
 *        Version:  1.0
 *        Created:  2011��03��07�� 16ʱ42��32��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  shuai.li (algol), shuai.li@alipay.com
 *        Company:  alipay.com
 *
 * =====================================================================================
 */

#include "def.h"
extern "C"
{
#include "../src/include/hi_block.h"
#include "../../indexengine/src/include/mem.h"
}

TEST(BLOCK_MANAGERTest, HandleNoneZeroInput)  {
    MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);     
	//����block_init
	struct block_index * pBlockIndex = block_init("mile_index", pMemPool);
	ASSERT_EQ(strcmp(pBlockIndex->file_name, "mile_index"), 0);
//	ASSERT_EQ(pBlockIndex->nBlock, 1);
	ASSERT_EQ(pBlockIndex->pBlockEntry[0]->block_id, 0);	
//	ASSERT_EQ(pBlockIndex->pBlockEntry[0]->pos, 0);	
	//����block_malloc
	int32 block_id, block_pos;
	int res = block_malloc(pBlockIndex, &block_id, &block_pos, M_1M, pMemPool); /* �ȷ���1M */
	ASSERT_EQ(res, 0);
//	ASSERT_EQ(pBlockIndex->nBlock, 1);
//	ASSERT_EQ(pBlockIndex->pBlockEntry[0]->pos, M_1M);	
	//��Ҫ����block��ʱ��
	res = block_malloc(pBlockIndex, &block_id, &block_pos, 60*M_1M, pMemPool); /* ����60M */
	ASSERT_EQ(res, 0);
//    ASSERT_EQ(block_id, 0);
//    ASSERT_EQ(block_pos, M_1M);
//	ASSERT_EQ(pBlockIndex->nBlock, 1);
//	ASSERT_EQ(pBlockIndex->pBlockEntry[0]->pos, 61*M_1M);	
	res = block_malloc(pBlockIndex, &block_id, &block_pos, 6*M_1M, pMemPool); /* ����6M */
	ASSERT_EQ(res, 0);
//    ASSERT_EQ(block_id, 1);
//    ASSERT_EQ(block_pos, 0);
//	ASSERT_EQ(pBlockIndex->nBlock, 2);
//	ASSERT_EQ(pBlockIndex->pBlockEntry[1]->pos, 6*M_1M);	
	//����block_address
	void *pAddr = block_address(pBlockIndex, 0, 0);
	ASSERT_EQ(pAddr, pBlockIndex->pBlockEntry[0]->entry_mmaped);
	pAddr = block_address(pBlockIndex, 1, 2);
	ASSERT_EQ(pAddr, pBlockIndex->pBlockEntry[1]->entry_mmaped+2);
	block_destory(pBlockIndex);

    mem_pool_destroy(pMemPool);
}

