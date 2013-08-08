#include "def.h"
extern "C"
{
#include "../src/common/mem.h"
#include "../src/storage/bitmark.h"
}
TEST(BITMARK_TEST, HandleNoneZeroInput)  {
    int32 ret;
    int32 value;
    MEM_POOL* mem_pool = mem_pool_init(M_1M);  
    struct bitmark_config config; 
	strcpy(config.bitmark_name,"null");
	strcpy(config.work_space,"/tmp/bitmark_test");
	config.row_limit = 10;

	system("rm -rf /tmp/bitmark_test");
	init_profile(1000,mem_pool);
	mkdirs(config.work_space);

    struct bitmark_manager* bitmark = bitmark_init(&config, mem_pool);

    //ȫ������
    bitmark_reset(bitmark);

    //��λ
    //��ʾ6���Ѳ������ֵ
    ret = bitmark_set(bitmark,6);
    ASSERT_EQ(ret,0);
    
    //��ѯ
    value = bitmark_query(bitmark,6);
    ASSERT_EQ(value,0); 
	
    value = bitmark_query(bitmark,8);
    ASSERT_EQ(value,1);

    //ɾ��6�е�ֵ
    ret = bitmark_clear(bitmark,6);
    ASSERT_EQ(ret,0);

    mem_pool_destroy(mem_pool);
}

