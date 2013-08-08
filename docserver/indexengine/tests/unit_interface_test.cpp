#include "def.h"
extern "C"
{
#include "../src/common/mem.h"
#include "../src/storage/db.h"
#include "../src/storage/config_parser.h"
}


void db_initialize(MEM_POOL* mem_pool)
{
	struct db_conf* config = config_parser("docserver.conf",mem_pool);
	g_running_flag = 1;
	ASSERT_EQ(MILE_RETURN_SUCCESS,db_init(config));
}




void data_insert_test(MEM_POOL* mem_pool)
{
	system("rm -rf /tmp/binlog");
	system("rm -rf /tmp/log");
	system("rm -rf /tmp/storage");
	
    db_initialize(mem_pool);

	//��������
    uint16 sid = 0;
    uint32 docid = 0;
    int32 ret;
    
    uint16 field_count = 3;
    enum field_types types[5] = {HI_TYPE_STRING,HI_TYPE_LONG,HI_TYPE_TINY,HI_TYPE_LONGLONG,HI_TYPE_DOUBLE};

    ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
    ASSERT_EQ(MILE_RETURN_SUCCESS,ret);
  
    struct row_data* query_row_ret = NULL;
	struct low_data_struct* query_col_ret = NULL;

    query_row_ret = db_data_query_row("hello",sid,docid,mem_pool);
    ASSERT_EQ(1,verify_row_data(query_row_ret,field_count,types));

	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_STRING));
  
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONG",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_LONG));

    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_TINY",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_TINY));

  	//��ͬ��������
	field_count = 5;
    ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
    ASSERT_EQ(MILE_RETURN_SUCCESS,ret);
	
    query_row_ret = db_data_query_row("hello",sid,docid,mem_pool);
    ASSERT_EQ(1,verify_row_data(query_row_ret,field_count,types));

	//��������
	query_row_ret = db_data_query_row("world",sid,docid,mem_pool);
	ASSERT_EQ(0,verify_row_data(query_row_ret,field_count,types));

	//sid����
	query_row_ret = db_data_query_row("hello",sid+1,docid,mem_pool);
	ASSERT_EQ(0,verify_row_data(query_row_ret,field_count,types));

	//docid����
	query_row_ret = db_data_query_row("hello",sid,docid+1,mem_pool);
	ASSERT_EQ(0,verify_row_data(query_row_ret,field_count,types));
 
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_STRING));
  
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONG",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_LONG));
  
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONGLONG",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));
  
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_TINY",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_TINY));
  
    query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_DOUBLE",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_DOUBLE));

	//��������
	query_col_ret = db_data_query_col("world",sid,"HI_TYPE_DOUBLE",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_DOUBLE));

	//sid����
	query_col_ret = db_data_query_col("hello",sid+1,"HI_TYPE_LONGLONG",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));

	//docid����
	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONGLONG",docid+1,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));

	//��������
	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LON",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_LONG));

	//��������
	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONG",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));


	//��һ������
	field_count = 3;
	ret = db_insert("world", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
    ASSERT_EQ(MILE_RETURN_SUCCESS,ret);
	ASSERT_EQ(sid,0);
	ASSERT_EQ(docid,0);

	query_row_ret = db_data_query_row("world",sid,docid,mem_pool);
    ASSERT_EQ(1,verify_row_data(query_row_ret,field_count,types));

	query_col_ret = db_data_query_col("world",sid,"HI_TYPE_STRING",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_STRING));
  
    query_col_ret = db_data_query_col("world",sid,"HI_TYPE_LONG",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_LONG));

    query_col_ret = db_data_query_col("world",sid,"HI_TYPE_TINY",docid,mem_pool);
    ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_TINY));

	
	db_release();
}


void data_update_test(MEM_POOL* mem_pool)
{
	system("rm -rf /tmp/binlog");
	system("rm -rf /tmp/log");
	system("rm -rf /tmp/storage");
	
    db_initialize(mem_pool);

	uint16 field_count = 3;
    enum field_types types[5] = {HI_TYPE_STRING,HI_TYPE_LONG,HI_TYPE_TINY,HI_TYPE_LONGLONG,HI_TYPE_DOUBLE};

	//��������
    uint16 sid = 0;
    uint32 docid = 0;
    int32 ret;
	ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);

	struct low_data_struct* new_data = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	get_low_data(new_data,HI_TYPE_STRING,mem_pool);

	struct low_data_struct* old_data = NULL;

	memset(new_data->field_name,0,20);
	strcpy(new_data->field_name,"HI_TYPE_LONG");

	//�������е���
	ret = db_update("hello",sid,docid,new_data,&old_data,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);
	ASSERT_EQ(1,verify_low_data(old_data,HI_TYPE_LONG));


	//��ѯһ��
	struct low_data_struct* query_col_ret = NULL;
	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONG",docid,mem_pool);
	ASSERT_STREQ(query_col_ret->field_name,"HI_TYPE_LONG");
	query_col_ret->field_name = (char*)mem_pool_malloc(mem_pool,20);
	strcpy(query_col_ret->field_name,"HI_TYPE_STRING");
	ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_STRING));

	//���²����ڵ���
	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONGLONG",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));
	get_low_data(new_data,HI_TYPE_LONGLONG,mem_pool);
	ret = db_update("hello",sid,docid,new_data,&old_data,mem_pool);

	query_col_ret = db_data_query_col("hello",sid,"HI_TYPE_LONGLONG",docid,mem_pool);
	ASSERT_EQ(1,verify_low_data(query_col_ret,HI_TYPE_LONGLONG));

	//���²����ڵĶ�
	ret = db_update("hello",sid+1,docid,new_data,&old_data,mem_pool);
	ASSERT_EQ(ERROR_SEGMENT_NOT_INIT,ret);

	//���²����ڵ�docid
	ret = db_update("hello",sid,docid+1,new_data,&old_data,mem_pool);
	ASSERT_EQ(ERROR_EXCEED_CURRENT,ret);
	ASSERT_EQ(0,verify_low_data(old_data,HI_TYPE_STRING));

	//���²����ڵı���
	ret = db_update("world",sid,docid,new_data,&old_data,mem_pool);
	ASSERT_EQ(ret,ERROR_TABLE_NOT_EXIT);

	db_release();
}



void index_create_test(MEM_POOL* mem_pool)
{
	system("rm -rf /tmp/binlog");
	system("rm -rf /tmp/log");
	system("rm -rf /tmp/storage");

	int32 ret;
	
	db_initialize(mem_pool);

	//�ȴ������ٽ��������ٲ�����
	ret = db_ensure_index("hello","HI_TYPE_STRING",HI_KEY_ALG_HASH,HI_TYPE_STRING,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	uint16 field_count = 3;
    enum field_types types[5] = {HI_TYPE_STRING,HI_TYPE_LONG,HI_TYPE_TINY,HI_TYPE_LONGLONG,HI_TYPE_DOUBLE};

	//��������
    uint16 sid = 0;
    uint32 docid = 0;

	struct low_data_struct* new_data = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	get_low_data(new_data,HI_TYPE_STRING,mem_pool);

	ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	struct list_head* index_equal_ret = NULL;
	index_equal_ret = db_index_equal_query("hello",get_time_hint(mem_pool),new_data,mem_pool);

	struct segment_query_rowids* node;
	list_for_each_entry(node,index_equal_ret,rowids_list){
           ASSERT_EQ(node->sid,0);
		   ASSERT_EQ(node->rowids->rowid_num,1);
		   ASSERT_EQ(node->rowids->head->rowid_array[0],0);
	}  

	//���Ը��»ᱻ�ܾ�
	struct low_data_struct* old_data = NULL;
	ret = db_update("hello",sid,docid,new_data,&old_data,mem_pool);
	ASSERT_EQ(ret,ERROR_ONLY_FILTER_SUPPORT);

	//��hash���ϵ���fiter�����Ĳ�ѯ�ӿ�
	struct low_data_struct* index_value_ret = NULL; 
	index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
	ASSERT_EQ(0,verify_low_data(index_value_ret,HI_TYPE_STRING));


	//��ͬһ���Ͻ���filter����
	ret = db_ensure_index("hello","HI_TYPE_STRING",HI_KEY_ALG_FILTER,HI_TYPE_STRING,mem_pool);
	ASSERT_EQ(ret,MILE_RETURN_SUCCESS);

	ret = db_ensure_index("hello","HI_TYPE_LONG",HI_KEY_ALG_FILTER,HI_TYPE_LONG,mem_pool);

	index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
	ASSERT_EQ(*(uint64*)index_value_ret->data,get_hash_value(new_data));

	index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_LONG",docid,mem_pool);
	ASSERT_EQ(*(uint32*)index_value_ret->data,6666);

	db_release();
}


void index_del_test(MEM_POOL* mem_pool)
{
	system("rm -rf /tmp/binlog");
	system("rm -rf /tmp/log");
	system("rm -rf /tmp/storage");

	int32 ret;
	
	db_initialize(mem_pool);

	//�ȴ������ٽ��������ٲ�����
	ret = db_ensure_index("hello","HI_TYPE_STRING",HI_KEY_ALG_HASH,HI_TYPE_STRING,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	ret = db_ensure_index("hello","HI_TYPE_STRING",HI_KEY_ALG_FILTER,HI_TYPE_STRING,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	ret = db_ensure_index("hello","HI_TYPE_LONGLONG",HI_KEY_ALG_FILTER,HI_TYPE_LONGLONG,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	uint16 field_count = 5;
    enum field_types types[5] = {HI_TYPE_STRING,HI_TYPE_LONG,HI_TYPE_TINY,HI_TYPE_LONGLONG,HI_TYPE_DOUBLE};

	//��������
    uint16 sid = 0;
    uint32 docid = 0;

	struct low_data_struct* new_data = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	get_low_data(new_data,HI_TYPE_STRING,mem_pool);

	uint32 i;
	for(i=0;i<100;i++)
	{
		ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
		ASSERT_EQ(MILE_RETURN_SUCCESS,ret);
	}

	struct list_head* index_equal_ret = NULL;
	struct segment_query_rowids* node;
	index_equal_ret = db_index_equal_query("hello",get_time_hint(mem_pool),new_data,mem_pool);
	list_for_each_entry(node,index_equal_ret,rowids_list){
           ASSERT_EQ(node->sid,0);
		   ASSERT_EQ(node->rowids->rowid_num,100);
	}

	struct low_data_struct* index_value_ret = NULL; 
	for(i=0;i<100;i++)
	{
		index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_LONGLONG",i,mem_pool);
		ASSERT_EQ(*(uint64*)index_value_ret->data,8888);
	}

	for(i=0;i<100;i++)
	{
		index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
		ASSERT_EQ(*(uint64*)index_value_ret->data,get_hash_value(new_data));
	}
	
	//ɾ��hash����
	ret = db_del_index("hello","HI_TYPE_STRING",HI_KEY_ALG_HASH,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	index_equal_ret = db_index_equal_query("hello",get_time_hint(mem_pool),new_data,mem_pool);
	if(index_equal_ret != NULL)
		FAIL();

	//
	for(i=0;i<100;i++)
	{
		index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_STRING",docid,mem_pool);
		ASSERT_EQ(*(uint64*)index_value_ret->data,get_hash_value(new_data));
	}

	for(i=0;i<100;i++)
	{
		index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_LONGLONG",i,mem_pool);
		ASSERT_EQ(*(uint64*)index_value_ret->data,8888);
	}

	//ɾ��filter����
	ret = db_del_index("hello","HI_TYPE_STRING",HI_KEY_ALG_FILTER,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_STRING",0,mem_pool);
	if(index_value_ret != NULL)
		FAIL();

	for(i=0;i<100;i++)
	{
		index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_LONGLONG",i,mem_pool);
		ASSERT_EQ(*(uint64*)index_value_ret->data,8888);
	}

	//ɾ��filter����
	ret = db_del_index("hello","HI_TYPE_LONGLONG",HI_KEY_ALG_FILTER,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	index_value_ret = db_index_value_query("hello",sid,"HI_TYPE_LONGLONG",0,mem_pool);
	if(index_value_ret != NULL)
		FAIL();
}

void simple_interface_test(MEM_POOL* mem_pool)
{
	system("rm -rf /tmp/binlog");
	system("rm -rf /tmp/log");
	system("rm -rf /tmp/storage");

	int32 ret;
	
	db_initialize(mem_pool);

	//ɾ���ӿ�
	uint16 field_count = 3;
    enum field_types types[5] = {HI_TYPE_STRING,HI_TYPE_LONG,HI_TYPE_TINY,HI_TYPE_LONGLONG,HI_TYPE_DOUBLE};

	//��������
    uint16 sid = 0;
    uint32 docid = 0;

	struct low_data_struct* new_data = (struct low_data_struct*)mem_pool_malloc(mem_pool,sizeof(struct low_data_struct));
	get_low_data(new_data,HI_TYPE_STRING,mem_pool);

	ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);


	ret = db_del_docid("hello",sid,docid,mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	struct row_data* query_row_ret = NULL;
	ret = db_is_docid_deleted("hello",sid,docid);
	ASSERT_EQ(1,ret);

	ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
	ASSERT_EQ(MILE_RETURN_SUCCESS,ret);

	//����2����ɾ��1��
	int64 record_num_ret = db_get_record_num("hello",get_time_hint(mem_pool));
	ASSERT_EQ(1,record_num_ret);

	char** field_names;
	field_names = (char**)mem_pool_malloc(mem_pool,2*sizeof(char*));
	memset(field_names,0,field_count*sizeof(char*));
	
	field_names[0] = (char*)mem_pool_malloc(mem_pool,20);
	memset(field_names[0],0,20);
	strcpy(field_names[0],"HI_TYPE_STRING");

	field_names[1] = (char*)mem_pool_malloc(mem_pool,20);
	memset(field_names[1],0,20);
	strcpy(field_names[1],"HI_TYPE_LONG");

	struct row_data* multi_col_ret = NULL;
	multi_col_ret = db_data_query_multi_col("hello",sid,docid,field_names,2,mem_pool);
	ASSERT_EQ(1,verify_row_data(multi_col_ret,2,types));

	//������У�����һ��û��
	memset(field_names[0],0,20);
	strcpy(field_names[0],"HI_TYPE_TINY");

	memset(field_names[1],0,20);
	strcpy(field_names[1],"HI_TYPE_LONGLONG");
	multi_col_ret = db_data_query_multi_col("hello",sid,docid,field_names,2,mem_pool);
	ASSERT_EQ(2,multi_col_ret->field_count);
	ASSERT_EQ(1,verify_low_data(multi_col_ret->datas,HI_TYPE_TINY));
	ASSERT_EQ(0,(multi_col_ret->datas+1)->len);

	field_count = 5;
	ret = db_insert("hello", &sid, &docid,get_row_data(field_count,types,mem_pool), DOCID_BY_SELF, mem_pool);
	
	memset(field_names[0],0,20);
	strcpy(field_names[0],"HI_TYPE_LONGLONG");

	memset(field_names[1],0,20);
	strcpy(field_names[1],"HI_TYPE_LONG");

	multi_col_ret = db_data_query_multi_col("hello",sid,docid,field_names,2,mem_pool);
	ASSERT_EQ(2,multi_col_ret->field_count);
	ASSERT_EQ(1,verify_low_data(multi_col_ret->datas,HI_TYPE_LONGLONG));
	ASSERT_EQ(1,verify_low_data(multi_col_ret->datas+1,HI_TYPE_LONG));

	//ɾ����
	db_del_table("hello");
	query_row_ret = db_data_query_row("hello",sid,docid,mem_pool);
	ASSERT_EQ(0,verify_row_data(query_row_ret,field_count,types));

	db_release();
}

TEST(DB_FUNCTION_TEST, HandleNoneZeroInput)  { 
  MEM_POOL* mem_pool = mem_pool_init(M_1M);

  //��ʼ��DB
  init_profile(1000,mem_pool);

  //ԭʼ���ݲ��룬�в�ѯ�Լ��в�ѯ���ܲ���
  data_insert_test(mem_pool);

  //���¹��ܲ���
  data_update_test(mem_pool);

  //�������ܲ���
  index_create_test(mem_pool);

  //����ɾ�����ܲ���
  index_del_test(mem_pool);

  //�����򵥽ӿڵĹ��ܲ���
  simple_interface_test(mem_pool);

  mem_pool_destroy(mem_pool);
}


