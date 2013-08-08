#ifndef HI_DB_MOCK_H
#define HI_DB_MOCK_H
#include "../../indexengine/src/include/mem.h"
#include "../../indexengine/src/include/hyperindex_def.h"





/**
 * ���õ���mock_db_query_by_rowid������ĺ�������ֵ
 * @param  data_array	��������ֵ�����飬֮�󵱵�i�ε��÷���mock_db_query_by_rowidʱ���᷵��data_array[i-1]���е�ֵ	
 * @param  n			����Ĵ�С
 * @param  tid
 * @param  sid
 * @param  row_id
 * @param  field_id
 **/
void mock_db_query_by_rowid_output(void** data_array, uint32 n, uint8 tid, uint16 sid, uint32 row_id, uint16 field_id);





/**
  * mock��db_query_by_rowid����
  * @param  tid ���   
  * @param  sid �κ�
  * @param  row_id �к�
  * @param  field_id ���
  * @param  mem_pool �ڴ��
  * @return �ɹ�����rowid_list��ʧ�ܷ���NULL
  **/ 
struct low_data_struct* mock_db_query_by_rowid(uint8 tid,uint16 sid,uint32 row_id,uint16 field_id,MEM_POOL* mem_pool);





/**
  * ��ȡָ�������������
  * @param  tid ���
  * @param  field_id ���
  * @return �ɹ�����field_types��Ϣ��ʧ��<0
  **/ 
enum field_types mock_db_getfield_type(uint8 tid,uint16 field_id);






#endif

