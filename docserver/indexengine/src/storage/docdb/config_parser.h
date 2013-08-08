/*
 * =====================================================================================
 *
 *       Filename:  hyperindex_schema_parser.h
 *
 *    Description:  ����db�������ļ��ӿ�
 *
 *        Version:  1.0
 *        Created:  2011��04��09�� 11ʱ41��55��
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  yunliang.shi, yunliang.shi@alipay.com
 *        Company:  alipay.com
 *
 * =====================================================================================
 */


#include "../../common/def.h"
#include "db.h"

#ifndef CONFIG_PARSER_H
#define CONFIG_PARSER_H

#define MAX_CONFIG_LINE 1024

struct db_conf *config_parser(const char *file_name, MEM_POOL *mem_pool);


struct data_import_conf *data_import_config_parser(const char *file_name, MEM_POOL *mem_pool);


#endif // CONFIG_PARSER_H

