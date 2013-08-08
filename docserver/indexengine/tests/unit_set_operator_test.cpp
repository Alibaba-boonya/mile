#include "def.h"
extern "C"
{
#include "../../indexengine/src/include/mem.h"
#include "../../indexengine/src/include/hyperindex_set_operator.h"
#include "./seg_rowid_list_test.h"
}


















TEST(SET_OPERATOR_TEST, ROWID_UNION){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);


	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "10, 8, 7, 6, 5, 4, 2, 1");


	struct rowid_list* lista;
	struct rowid_list* listb;
	struct rowid_list* result;
	uint32 i, j, k;


	//���������ռ��Ĳ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 0);
	result = gen_rowid_list(pMemPool, rowid_array_b, 0);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//����������ͬ���ϵĲ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 5);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));

	
	//���Կռ��ͷǿռ��ϵĲ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 5);
	lista = rowid_union(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//���Էǿռ��ϺͿռ��Ĳ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);
	result = gen_rowid_list(pMemPool, rowid_array_a, 5);
	lista = rowid_union(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//���������ǿռ��ϵĲ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 5);
	result = gen_rowid_list(pMemPool, result_array, 8);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�����һЩ�߽�������в���
	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ����СΪROWID_ARRAY_SIZE�ļ�����һ���ռ��Ĳ���
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ���ռ���һ����СΪROWID_ARRAY_SIZE�ļ��ϵĲ���
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < 2*ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 2*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE*2);	
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	for(i = 0; i < 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < 4*ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE*4);		
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);		
	lista = rowid_union(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2�����������в�����ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_a[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE*3; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}

	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE*3);
	lista = rowid_union(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//��������ݵĲ���
	for(i = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0, j = 0, k = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		uint32 rd = rand()%3;
		if(rd == 0)
		{
			rowid_array_a[j++] = result_array[i];
		}else if(rd == 1){
			rowid_array_b[k++] = result_array[i];
		}else{
			rowid_array_a[j++] = result_array[i];
			rowid_array_b[k++] = result_array[i];
		}
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, j);
	listb = gen_rowid_list(pMemPool, rowid_array_b, k);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE*4);
	lista = rowid_union(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	mem_pool_destroy(pMemPool);
}

















TEST(SET_OPERATOR_TEST, ROWID_INTERSECTION){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);


	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "7, 4");


	struct rowid_list* lista;
	struct rowid_list* listb;
	struct rowid_list* result;
	uint32 i, j, k, l;


	//���������ռ��Ľ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 0);
	result = gen_rowid_list(pMemPool, rowid_array_b, 0);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//����������ͬ���ϵĽ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 5);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));

	
	//���Կռ��ͷǿռ��ϵĽ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_intersection(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//���Էǿռ��ϺͿռ��Ľ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_intersection(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//���������ǿռ��ϵĽ���
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 5);
	result = gen_rowid_list(pMemPool, result_array, 2);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�����һЩ�߽�������в���
	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ����СΪROWID_ARRAY_SIZE�ļ�����һ���ռ��Ľ���
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);	
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ���ռ���һ����СΪROWID_ARRAY_SIZE�ļ��ϵĽ���
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < 2*ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 2*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, result_array, 0);	
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	for(i = 0; i < 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < 4*ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, result_array, 0);		
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);		
	lista = rowid_intersection(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2�����������в�����ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_a[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 2*ROWID_ARRAY_SIZE-i;
	}

	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE);
	lista = rowid_intersection(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//��������ݵĲ���
	for(i = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0, j = 0, k = 0, l = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		uint32 rd = rand()%3;
		if(rd == 0)
		{
			rowid_array_a[j++] = result_array[i];
		}else if(rd == 1){
			rowid_array_b[k++] = result_array[i];
		}else{
			rowid_array_a[j++] = result_array[i];
			rowid_array_b[k++] = result_array[i];
			result_array[l++] = result_array[i];
		}
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, j);
	listb = gen_rowid_list(pMemPool, rowid_array_b, k);	
	result = gen_rowid_list(pMemPool, result_array, l);
	lista = rowid_intersection(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	mem_pool_destroy(pMemPool);
}





TEST(SET_OPERATOR_TEST, ROWID_MINUS){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);


	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "10, 5, 1");


	struct rowid_list* lista;
	struct rowid_list* listb;
	struct rowid_list* result;
	uint32 i, j, k, l;


	//���������ռ��Ĳ
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 0);
	result = gen_rowid_list(pMemPool, rowid_array_b, 0);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//����������ͬ���ϵĲ
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);
	ASSERT_TRUE(rowid_list_equals(lista, result));

	
	//���Կռ��ͷǿռ��ϵĲ
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 5);
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_minus(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));


	//���Էǿռ��ϺͿռ��Ĳ
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);
	result = gen_rowid_list(pMemPool, rowid_array_a, 5);
	lista = rowid_minus(pMemPool, lista, listb);	
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//���������ǿռ��ϵĲ
	lista = gen_rowid_list(pMemPool, rowid_array_a, 5);
	listb = gen_rowid_list(pMemPool, rowid_array_b, 5);
	result = gen_rowid_list(pMemPool, result_array, 3);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�����һЩ�߽�������в���
	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ����СΪROWID_ARRAY_SIZE�ļ�����һ���ռ��Ĳ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_a, 0);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//һ���ռ���һ����СΪROWID_ARRAY_SIZE�ļ��ϵĲ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, 0);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE����������������ȫ��ͬ
	for(i = 0; i < 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 2*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 2*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE);	
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	for(i = 0; i < 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_a[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 1; i <= 4*ROWID_ARRAY_SIZE; i+=2)
	{
		rowid_array_b[i/2] = 4*ROWID_ARRAY_SIZE-i;
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);		
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	



	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2����������������ȫ��ͬ
	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, rowid_array_a, 0);		
	lista = rowid_minus(pMemPool, lista, listb);
	//print_rowid_list(lista);	
	ASSERT_TRUE(rowid_list_equals(lista, result));	




	//�������ϴ�С��ΪROWID_ARRAY_SIZE*2�����������в�����ͬ
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_a[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_a[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(; i < ROWID_ARRAY_SIZE*2; i++)
	{
		rowid_array_b[i] = 3*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0; i < ROWID_ARRAY_SIZE; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}

	lista = gen_rowid_list(pMemPool, rowid_array_a, ROWID_ARRAY_SIZE*2);
	listb = gen_rowid_list(pMemPool, rowid_array_b, ROWID_ARRAY_SIZE*2);	
	result = gen_rowid_list(pMemPool, result_array, ROWID_ARRAY_SIZE);
	lista = rowid_minus(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	//��������ݵĲ���
	for(i = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		result_array[i] = 4*ROWID_ARRAY_SIZE-i;
	}
	for(i = 0, j = 0, k = 0, l = 0; i < ROWID_ARRAY_SIZE*4; i++)
	{
		uint32 rd = rand()%3;
		if(rd == 0)
		{
			rowid_array_a[j++] = result_array[i];
			result_array[l++] = result_array[i];
		}else if(rd == 1){
			rowid_array_b[k++] = result_array[i];
		}else{
			rowid_array_a[j++] = result_array[i];
			rowid_array_b[k++] = result_array[i];
		}
	}
	lista = gen_rowid_list(pMemPool, rowid_array_a, j);
	listb = gen_rowid_list(pMemPool, rowid_array_b, k);	
	result = gen_rowid_list(pMemPool, result_array, l);
	lista = rowid_minus(pMemPool, lista, listb);
	ASSERT_TRUE(rowid_list_equals(lista, result));	


	mem_pool_destroy(pMemPool);
}









TEST(SET_OPERATOR_TEST, SEG_ROWID_UNION){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	int32 result_code;


	struct list_head *lista = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *listb = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *result = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));

	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);




	result_code = seg_rowid_union(pMemPool, NULL, NULL);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);



	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "10, 8, 7, 6, 5, 4, 2, 1");
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 5);
	result_code = seg_rowid_union(pMemPool, lista, listb);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);




	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 0, rowid_array_b, 5);
	gen_seg_rowid_list(pMemPool, result, 0, result_array, 8);

	set_array(rowid_array_a, "11, 8, 4, 3, 2");
	set_array(rowid_array_b, "13, 12, 9, 8, 5, 4, 2");
	set_array(result_array, "13, 12, 11, 9, 8, 5, 4, 3, 2");
	gen_seg_rowid_list(pMemPool, lista, 1, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 7);
	gen_seg_rowid_list(pMemPool, result, 1, result_array, 9);

	result_code = seg_rowid_union(pMemPool, lista, listb);
	ASSERT_TRUE(check_seg_rowid_list_equal(lista, result));


	mem_pool_destroy(pMemPool);
}





TEST(SET_OPERATOR_TEST, SEG_ROWID_INTERSECTION){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	int32 result_code;


	struct list_head *lista = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *listb = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *result = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));

	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);




	result_code = seg_rowid_intersection(pMemPool, NULL, NULL);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);



	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "7, 4");
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 5);
	result_code = seg_rowid_intersection(pMemPool, lista, listb);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);




	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 0, rowid_array_b, 5);
	gen_seg_rowid_list(pMemPool, result, 0, result_array, 2);

	set_array(rowid_array_a, "11, 8, 4, 3, 2");
	set_array(rowid_array_b, "13, 12, 9, 8, 5, 4, 2");
	set_array(result_array, "8, 4, 2");
	gen_seg_rowid_list(pMemPool, lista, 1, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 7);
	gen_seg_rowid_list(pMemPool, result, 1, result_array, 3);

	result_code = seg_rowid_intersection(pMemPool, lista, listb);
	ASSERT_TRUE(check_seg_rowid_list_equal(lista, result));



	mem_pool_destroy(pMemPool);
}




TEST(SET_OPERATOR_TEST, SEG_ROWID_MINUS){
	MEM_POOL_PTR pMemPool = mem_pool_init(M_1M);
	uint32* rowid_array_a = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* rowid_array_b = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	uint32* result_array = (uint32*) mem_pool_malloc(pMemPool, sizeof(uint32)*ROWID_ARRAY_SIZE*8);
	int32 result_code;


	struct list_head *lista = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *listb = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));
	struct list_head *result = (struct list_head*) mem_pool_malloc(pMemPool, sizeof(struct list_head));

	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);




	result_code = seg_rowid_minus(pMemPool, NULL, NULL);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);



	set_array(rowid_array_a, "10, 7, 5, 4, 1");
	set_array(rowid_array_b, "8, 7, 6, 4, 2");
	set_array(result_array, "10, 5, 1");
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 5);
	result_code = seg_rowid_minus(pMemPool, lista, listb);
	ASSERT_EQ(result_code, ERROR_SET_OPERATION);




	INIT_LIST_HEAD(lista);	
	INIT_LIST_HEAD(listb);	
	INIT_LIST_HEAD(result);
	gen_seg_rowid_list(pMemPool, lista, 0, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 0, rowid_array_b, 5);
	gen_seg_rowid_list(pMemPool, result, 0, result_array, 3);

	set_array(rowid_array_a, "11, 8, 4, 3, 2");
	set_array(rowid_array_b, "13, 12, 9, 8, 5, 4, 2");
	set_array(result_array, "11, 3");
	gen_seg_rowid_list(pMemPool, lista, 1, rowid_array_a, 5);
	gen_seg_rowid_list(pMemPool, listb, 1, rowid_array_b, 7);
	gen_seg_rowid_list(pMemPool, result, 1, result_array, 2);

	result_code = seg_rowid_minus(pMemPool, lista, listb);
	ASSERT_TRUE(check_seg_rowid_list_equal(lista, result));



	mem_pool_destroy(pMemPool);
}







