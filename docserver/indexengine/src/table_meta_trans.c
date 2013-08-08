/*
 * =====================================================================================
 *
 *       Filename:  table_meta_trans.c
 *
 *    Description:  ���ϰ汾��table_meta���ݲ�һ���������¼�������
 *
 *        Version:  1.0
 *        Created:  06/08/12 19:19:41
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi (zian), yunliang.shi@alipay.com
 *   Organization:  
 *
 * =====================================================================================
 */
#include "storage/docdb/db.h"



#define TABLE_RUNTIME_SIZE_SRC sizeof(struct src_table_meta_data)*MAX_TABLE_NUM + sizeof(uint8_t)

struct src_table_meta_data{
	/* ����??*/
	char   table_name[MAX_TABLE_NAME];

	/* table��?����??*/
	enum table_stat	stat;

	/* �̡�?��?y?��D�䨨?��?segmento?*/
	uint16_t segment_current;

	/* ?�¨�y��D��???��y*/
	uint16_t index_field_count;

	/* ��3��?��??����?��?��??��*/
	struct index_field_meta index_meta[50];
};



int main(int argc, char* argv[])
{
	if(argc < 2)
	{
		printf("%s:workspace",argv[0]);
		return -1;
	}

	char table_meta_filename[FILENAME_MAX_LENGTH];
	memset(table_meta_filename,0,sizeof(table_meta_filename));
	sprintf(table_meta_filename,"%s/table.meta",argv[1]);

	char table_meta_filename_bak[FILENAME_MAX_LENGTH];
	memset(table_meta_filename_bak, 0, sizeof(table_meta_filename_bak));
	sprintf(table_meta_filename_bak, "%s.bak", table_meta_filename);
	
	rename(table_meta_filename, table_meta_filename_bak);	

	void* src_table_meta = get_mmap_memory(table_meta_filename_bak,TABLE_RUNTIME_SIZE_SRC);

	//bak
	void* dst_table_meta = get_mmap_memory(table_meta_filename,TABLE_RUNTIME_SIZE);
	
	uint8_t table_count = *(uint8_t*) src_table_meta;
	uint8_t i;
	*(uint8_t*)dst_table_meta  = table_count;

	struct src_table_meta_data* src_meta = (struct src_table_meta_data*)((char *)src_table_meta + sizeof(uint8_t));
	struct table_meta_data* dst_meta = (struct table_meta_data*)((char *)dst_table_meta + sizeof(uint8_t));
	

	for(i=0; i<table_count; i++, src_meta++, dst_meta++)
	{
		memcpy((void*)dst_meta, (void*)src_meta, sizeof(struct src_table_meta_data));	
	}

	msync(dst_table_meta, TABLE_RUNTIME_SIZE, MS_SYNC);
	
	munmap(dst_table_meta, TABLE_RUNTIME_SIZE);
	munmap(src_table_meta, TABLE_RUNTIME_SIZE_SRC);
	
	return 0;	
}

