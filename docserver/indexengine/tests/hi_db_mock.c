#include "hi_db_mock.h"






int32 mock_db_init(uint32 column_num, enum field_types* fields_type_array, enum index_key_alg* index_type_array)
{
	if(column_num > TEST_MAX_FIELD_NUM)
	{
		return -1;
	}

	uint32 i, j;

	for(i = 0; i < column_num; i++)
	{
		mock_fields_type_array[i] = fields_type_array[i];
		mock_index_type_array[i] = index_type_array[i];
	}

	for(i = 0; i < TEST_MAX_SEG_NUM; i++)
	{
		for(j = 0; j < TEST_MAX_ROW_NUM; j++)
		{
			mock_delete_mask[i][j] = 0;
		}
	}

	current_seg = 0;
	current_row = 0;
	field_num = column_num;
	
	return 0;
}







/**
  * ��ָ���ı����һ�м�¼��data��һ��ָ�����飬���鳤��Ϊ����������������limit���¿���һ����
  * @param  tid ���
  * @param  segment_id �κ�
  * @param  row_id �к�
  * @param  data һ�е�����
  * @return �ɹ�����rowid��ʧ�ܷ���<0
  **/ 
int32 db_insert(uint8 tid, uint16* segment_id, uint32* row_id, struct low_data_struct** data)
{
	uint32 i;
	
	if(current_seg >= TEST_MAX_SEG_NUM && current_row >= TEST_MAX_ROW_NUM)
	{
		printf("��������mock���ݿ���������!\n");
		exit(-1);
	}
	
	if(current_row >= TEST_MAX_ROW_NUM)
	{
		current_seg++;
		current_row = 0;
	}

	for(i = 0; i < field_num; i++)
	{
		mock_data_source[current_seg][current_row][i].data = (*data+i)->data;
		mock_data_source[current_seg][current_row][i].len = (*data+i)->len;
	}

	*segment_id = current_seg;
	*row_id = current_row;
	current_row++;

	return MILE_RETURN_SUCCESS;
}





/**
  * ��ָ���ı�����и���ֵ��ֻ��filter������֧��
  * @param  tid ���
  * @param  sid �κ�
  * @param  new_rdata ���µ�����
  * @param  row_id �к�
  * @param  field_id ���
  * @return �ɹ�����0��ʧ�ܷ���<0
  **/ 
int32 db_update(uint8 tid,uint16 sid,struct low_data_struct* new_rdata,uint32 row_id,uint16 field_id)
{
	mock_data_source[sid][row_id][field_id].data = new_rdata->data;
	mock_data_source[sid][row_id][field_id].len = new_rdata->len;
	return MILE_RETURN_SUCCESS;
}









/**
  * ����rowid�����Ҷ�Ӧ��value��ֻ��filter��������֧��
  * @param  tid ���   
  * @param  sid �κ�
  * @param  row_id �к�
  * @param  field_id ���
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* db_query_by_rowid(uint8 tid, uint16 sid,uint32 row_id,uint16 field_id, MEM_POOL* mem_pool)
{
	if(mock_delete_mask[sid][row_id])
	{
		return NULL;
	}
	struct low_data_struct* result = (struct low_data_struct*) mem_pool_malloc(mem_pool, sizeof(struct low_data_struct));
	result->len = mock_data_source[sid][row_id][field_id].len;
	result->data = mem_pool_malloc(mem_pool, result->len);

	memcpy(result->data, mock_data_source[sid][row_id][field_id].data, result->len);
	return result;

}





/**
  * ����value�����Ҷ�Ӧ��doc id lists��ֻ��hash����֧�֣����ж�ɨ
  * @param  tid ���
  * @param  field_id ���
  * @param  data ����
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list head���ϲ����ͨ�����������ȡ�����жν����ʧ�ܷ���NULL
  **/ 
struct list_head* db_query_by_value(uint8 tid,uint16 field_id,struct low_data_struct* data, MEM_POOL* mem_pool)
{
	int32 i, j, k, n;


	//��ʼ��ͷ
	struct list_head* list_h = (struct list_head*)mem_pool_malloc(mem_pool,sizeof(struct list_head));
	
	INIT_LIST_HEAD(list_h);

	struct segment_query_rowids* seg_rowids; 

	n = 0;
	for(i = 0; i <= current_seg; i++)
	{
		if(i == current_seg)
		{
			k = current_row;
		}else{
			k = TEST_MAX_ROW_NUM;
		}

		seg_rowids = (struct segment_query_rowids*) mem_pool_malloc(mem_pool, sizeof(struct segment_query_rowids));
		seg_rowids->sid = i;
		seg_rowids->rowids = rowid_list_init(mem_pool);
		list_add(&seg_rowids->rowids_list, list_h);

		for(j = k-1; j >= 0; j--)
		{
			if(compare(data->data, mock_data_source[i][j][field_id].data, mock_fields_type_array[field_id]) == 0)
			{
				rowid_list_add(mem_pool, seg_rowids->rowids, j);
				n++;
			}
		}
			
	}


	return list_h;
}






/**
  * �����кŽ���ɾ��
  * @param  tid ���
  * @param  sid �κ�
  * @param  row_id �к�
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
int32 db_del_rowid(uint8 tid,uint16 sid,uint32 row_id)
{
	mock_delete_mask[sid][row_id] = 1;
	return MILE_RETURN_SUCCESS;
}





/**
  * �ж�ָ����doc id�Ƿ���Ϊɾ��
  * @param  tid ���
  * @param  sid �κ�
  * @param  row_id �к�
  * @return ��ɾ������1��δɾ������0  
  **/
int32 db_is_rowid_deleted(uint8 tid,uint16 sid,uint32 row_id)
{
	return mock_delete_mask[sid][row_id];
}




/**
  * ��ȡָ�������������
  * @param  tid ���
  * @param  field_id ���
  * @return �ɹ�����field_types��Ϣ��ʧ��<0
  **/ 
enum field_types db_getfield_type(uint8 tid,uint16 field_id)
{
	return mock_fields_type_array[field_id];
}





/**
  * ��ȡdoc server�Ľڵ���
  * @return �ɹ����ؽڵ���
  **/ 
uint16 get_node_id()
{
	return 1;
}




/**
  * ��ȡָ�������������
  * @param  tid ���
  * @param  field_id ���
  * @return �ɹ�����index_key_alg��Ϣ��ʧ��<0
  **/ 
enum index_key_alg db_getindex_type(uint8 tid, uint16 field_id)
{
	return mock_index_type_array[field_id];
}



/**
  * ��btree������Χ��ѯ,���������ݷ�Χ�ڲ�ѯ
  * @param  tid ��� 
  * @param  field_id ���
  * @param  range_condition ��ѯ������
  * @param  mem_pool �ڴ��
  * @return �ɹ�����list head���ϲ����ͨ�����������ȡ�����жν����ʧ�ܷ���NULL
  **/ 
struct list_head * db_btree_query_range(uint8 tid,	uint16 field_id, \
		struct db_range_query_condition *range_condition, MEM_POOL *pMemPool)
{
	int32 i, j, k, n;
	int8 cmp_res;

	//��ʼ��ͷ
	struct list_head* list_h = (struct list_head*)mem_pool_malloc(pMemPool,sizeof(struct list_head));
	
	INIT_LIST_HEAD(list_h);

	struct segment_query_rowids* seg_rowids; 

	n = 0;
	for(i = 0; i <= current_seg; i++)
	{
		if(i == current_seg)
		{
			k = current_row;
		}else{
			k = TEST_MAX_ROW_NUM;
		}

		seg_rowids = (struct segment_query_rowids*) mem_pool_malloc(pMemPool, sizeof(struct segment_query_rowids));
		seg_rowids->sid = i;
		seg_rowids->rowids = rowid_list_init(pMemPool);
		list_add(&seg_rowids->rowids_list, list_h);

		for(j = k-1; j >= 0; j--)
		{
			if(range_condition->min_flag >= 0)
			{
				cmp_res = compare(mock_data_source[i][j][field_id].data, range_condition->min_key->data, mock_fields_type_array[field_id]);
				if((cmp_res <= 0 && range_condition->min_flag == 1) || (cmp_res < 0 && range_condition->min_flag == 0))
				{
					continue;
				}
			}

			if(range_condition->max_flag >= 0)
			{
				cmp_res = compare(mock_data_source[i][j][field_id].data, range_condition->max_key->data, mock_fields_type_array[field_id]);
				if((cmp_res >= 0 && range_condition->max_flag == 1) || (cmp_res > 0 && range_condition->max_flag == 0))
				{
					continue;
				}
			}

			rowid_list_add(pMemPool, seg_rowids->rowids, j);
			n++;
		}
			
	}


	return list_h;	
}
















	













