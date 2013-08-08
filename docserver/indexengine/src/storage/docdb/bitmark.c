/*
 * =====================================================================================
 *
 *       Filename:  hi_bitmark.c
 *
 *    Description:  ���λ�Ľӿ�ʵ��
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


#include "bitmark.h"

static uint8_t BITMAPMASK[BYTE_SIZE] = 
{
  0x80, 0x40, 0x20, 0x10,
  0x08, 0x04, 0x02, 0x01
};


struct bitmark_manager * bitmark_init(struct bitmark_config* config, MEM_POOL* mem_pool)
{	
    struct bitmark_manager* bitmark = (struct bitmark_manager*)mem_pool_malloc(mem_pool,sizeof(struct bitmark_manager));
	assert(bitmark != NULL);
	memset(bitmark,0,sizeof(struct bitmark_manager));

	//�����ļ�ȫ·��
	sprintf(bitmark->file_name,"%s/%s.bit",config->work_space,config->bitmark_name);

	//��¼limit
	bitmark->row_limit = config->row_limit;

	//����mmap�ڴ�
	bitmark->mem_mmaped = (char *)get_mmap_memory(bitmark->file_name,BITMARK_MMAP_SIZE(bitmark));

	assert(bitmark->mem_mmaped != NULL);

	return bitmark;
}



int32_t bitmark_set(struct bitmark_manager * bitmark, uint32_t docid)
{
   uint32_t quot;
   uint32_t rem;

   	if(docid >= bitmark->row_limit)
	{
		log_error("����limit���ƣ�row_id:%u row_limit:%u",docid,bitmark->row_limit);
		return ERROR_EXCEED_LIMIT;
	}

   //����row_id�����Ӧ��bitλ��
   quot = docid / BYTE_SIZE;
   rem = docid % BYTE_SIZE;

   //������1����
   if (!(bitmark->mem_mmaped[quot]& BITMAPMASK[rem]))
   {
	   bitmark->mem_mmaped[quot] |= BITMAPMASK[rem];
   } 

   return 0;
}

int32_t bitmark_clear(struct bitmark_manager * bitmark,uint32_t docid)
{
   uint32_t quot;
   uint32_t rem;

   if(docid >= bitmark->row_limit)
   {
		log_error("����limit���ƣ�row_id:%u row_limit:%u",docid,bitmark->row_limit);
		return ERROR_EXCEED_LIMIT;
   }

   //����row_id�����Ӧ��bitλ��
   quot = docid / BYTE_SIZE;
   rem = docid % BYTE_SIZE;

   //������0����
   if (bitmark->mem_mmaped[quot]& BITMAPMASK[rem])
   {
      bitmark->mem_mmaped[quot] &= ~ BITMAPMASK[rem];
	  return 1;
   } 
   
   return 0;
}

int32_t bitmark_query(struct bitmark_manager * bitmark, uint32_t docid)
{
   uint32_t quot;
   uint32_t rem;

   if(docid >= bitmark->row_limit)
   {
		log_error("����limit���ƣ�row_id:%u row_limit:%u",docid,bitmark->row_limit);
		return ERROR_EXCEED_LIMIT;
   }
   
   //����row_id�����Ӧ��bitλ��
   quot = docid / BYTE_SIZE;
   rem = docid % BYTE_SIZE;

   //bitmap��ʼ��ʱ��Ĭ����ȫ��ɾ����������һ������ʱ������Ӧ��row_id��bit��Ϊ1
   if (bitmark->mem_mmaped[quot]& BITMAPMASK[rem])
   {
       return 0;
   } 

   return 1;
}


int32_t bitmark_reset(struct bitmark_manager * bitmark)
{
   //�����е�bitmap����0
   memset(bitmark->mem_mmaped,0,BITMARK_MMAP_SIZE(bitmark));
   return 0;
}


void bitmark_release(struct bitmark_manager* bitmark)
{
    //�����������flush��������
    if(bitmark->mem_mmaped != NULL)
	{
		msync(bitmark->mem_mmaped,BITMARK_MMAP_SIZE(bitmark),MS_SYNC);
		munmap(bitmark->mem_mmaped, BITMARK_MMAP_SIZE(bitmark));
		bitmark->mem_mmaped = NULL;
	}

	return;
}

void bitmark_destroy(struct bitmark_manager* bitmark)
{
    //�����������flush��������
    if(bitmark->mem_mmaped != NULL)
	{
		munmap(bitmark->mem_mmaped, BITMARK_MMAP_SIZE(bitmark));
	}

	//ɾ�������ļ�
	remove(bitmark->file_name);

	return;
}


void bitmark_checkpoint(struct bitmark_manager* bitmark)
{
    //�����������flush��������
    if(bitmark->mem_mmaped != NULL)
	{
		msync(bitmark->mem_mmaped,BITMARK_MMAP_SIZE(bitmark),MS_SYNC);        // make sure synced
	}
	return;
}


int bitmark_recover(struct bitmark_manager *bitmark, uint32_t docid)
{
	if(docid >= bitmark->row_limit) {
		log_error("invalid docid, docid %u, row_limit %u", docid, bitmark->row_limit);
		return ERROR_EXCEED_LIMIT;
	}

	uint32_t quot = docid / BYTE_SIZE;
	uint32_t rem = docid % BYTE_SIZE;

	for( ; rem > 0 && rem < BYTE_SIZE; ++rem )
		bitmark->mem_mmaped[quot] &= ~BITMAPMASK[rem];

	if( rem > 0 )
		quot++;

	if(quot < BITMARK_MMAP_SIZE(bitmark))
		memset(bitmark->mem_mmaped + quot, 0, BITMARK_MMAP_SIZE(bitmark) - quot);
	return 0;
}

int bitmark_count(struct bitmark_manager *bitmark)
{
	const static int count_map[] = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };
	int count = 0;
	for(int i = 0; i < BITMARK_MMAP_SIZE(bitmark); i++) {
		char c = bitmark->mem_mmaped[i];
		count += count_map[c & 0x0f];
		count += count_map[(c>>4) & 0x0f];
	}

	return count;
}
