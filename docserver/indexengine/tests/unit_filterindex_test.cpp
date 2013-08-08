#include "def.h"
extern "C"
{
#include "../src/common/mem.h"
#include "../src/storage/filter_index.h"
}


TEST(FILTER_INDEX_TEST, HandleNoneZeroInput)  {
    int32 ret;
    MEM_POOL_PTR mem_pool = mem_pool_init(M_1M);
	
	system("rm -rf /tmp/filter_test");
    
    struct filter_index_config config;
    char dir_path[] = "/tmp/filter_test";
    config.unit_size = 8;
    config.row_limit = 100;
	strcpy(config.work_space,dir_path);
    config.type = HI_TYPE_LONGLONG;

	mkdirs(dir_path);
	
    init_profile(1000,mem_pool);
	
   //���� 
   struct filter_index_manager* filter_index = filter_index_init(&config,mem_pool);
   uint32 docid = 0;
   
   //����һ��ֵ
   struct low_data_struct insert_data;
   struct low_data_struct* old_data;

   get_low_data(&insert_data,HI_TYPE_LONGLONG,mem_pool);
   
   ret = filter_index_insert(filter_index,&insert_data,docid);
   ASSERT_EQ(ret,0);
   
   //��ѯһ��ֵ
   struct low_data_struct* query_data = NULL;
   query_data = filter_index_query(filter_index,docid,mem_pool);
   ASSERT_EQ(*((uint64*)query_data->data),8888);
   ASSERT_EQ(query_data->len,8);
   
   //����һ��ֵ
   uint64 value = 6666;
   insert_data.data= &value;
   ret = filter_index_update(filter_index,&insert_data,&old_data,docid,mem_pool);
   ASSERT_EQ(ret,0);
   
   query_data = filter_index_query(filter_index,0,mem_pool);
   ASSERT_EQ(*((uint64*)query_data->data),6666);
   ASSERT_EQ(query_data->len,8);


   struct low_data_struct null_data;
   null_data.len = 0;
   null_data.data = NULL;

   docid = 1;
   
   //����һ����ֵ������һ���ǿ�ֵ
   ret = filter_index_insert(filter_index,&null_data,docid);
   ASSERT_EQ(ret,0);

   query_data = filter_index_query(filter_index,docid,mem_pool);
   ASSERT_EQ(query_data->len,0);

   ret = filter_index_update(filter_index,&insert_data,&old_data,docid,mem_pool);
   ASSERT_EQ(ret,0);
   
   //��֤ԭʼֵ
   ASSERT_EQ(old_data->len,0);
   
   //��ѯ
   query_data = filter_index_query(filter_index,1,mem_pool);
   query_data = filter_index_query(filter_index,0,mem_pool);
   ASSERT_EQ(*((uint64*)query_data->data),6666);
   ASSERT_EQ(query_data->len,8);

   //����һ���ǿ�ֵ������һ����ֵ
   docid = 2;
  
   //����һ��ֵ
   ret = filter_index_insert(filter_index,&insert_data,docid);
   ASSERT_EQ(ret,0);

   //����ֵ
   ret = filter_index_update(filter_index,&null_data,&old_data,docid,mem_pool);
   ASSERT_EQ(ret,0);
   ASSERT_EQ(*((uint64*)old_data->data),6666);
   ASSERT_EQ(old_data->len,8);

   query_data = filter_index_query(filter_index,docid,mem_pool);
   ASSERT_EQ(query_data->len,0);

   //�������룬����ѯ
   for(uint32 i=docid+1;i<config.row_limit;i++)
  { 
       insert_data.data = (void*)&i;
       ret = filter_index_insert(filter_index,&insert_data,i);
       ASSERT_EQ(ret,0);
    
       query_data = filter_index_query(filter_index,i,mem_pool);
       ASSERT_EQ(*((uint64*)query_data->data),i);
  }

   
 filter_index_release(filter_index);
 filter_index = NULL;


 //��������֤
 system("rm -rf /tmp/filter_test");
 
 mkdirs(dir_path);

 //������
 config.type = HI_TYPE_STRING;
 init_profile(1000,mem_pool);
 filter_index = filter_index_init(&config,mem_pool);  
 
 get_low_data(&insert_data,HI_TYPE_STRING,mem_pool);
 
 ret = filter_index_insert(filter_index,&insert_data,0);
 ASSERT_EQ(ret,0);

 //��ѯ
 query_data = filter_index_query(filter_index,0,mem_pool);
 ASSERT_STREQ((char*)query_data->data,"ali");
 ASSERT_EQ(query_data->len,5);

 //���¿�ֵ
 insert_data.data = NULL;
 insert_data.len = 0;
 ret = filter_index_update(filter_index,&insert_data,&old_data,0,mem_pool);
 ASSERT_EQ(ret,0);
 query_data = filter_index_query(filter_index,0,mem_pool);
 ASSERT_EQ(query_data->len,0);

 filter_index_release(filter_index);


 mem_pool_destroy(mem_pool);
}

