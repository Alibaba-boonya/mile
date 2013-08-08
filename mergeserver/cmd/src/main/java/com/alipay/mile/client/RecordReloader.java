/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: LogDataLoader.java, v 0.1 2011-11-15 ����09:37:11 yuzhong.zhao Exp $
 */
public class RecordReloader {
    //���ڲ�ѯ�Ͳ����client��
    private ApplationClientImpl applationClientImpl;

    //����
    private String              tableName;

    //hash������
    private String[]        hashIndexes;

    //filter������
    private String[]        filterIndexes;

    private class SqlParamPair {
        // sql���
        public String   sql;
        // param����
        public Object[] params;
    }

    /**
     * �ж����м�¼�Ƿ����
     * 
     * @param record1       ��¼1
     * @param record2       ��¼2
     * @return
     */
    private boolean isRecordEqual(Map<String, Object> record1, Map<String, Object> record2) {
        if (null == record1 && null == record2) {
            return true;
        } else if (null == record1 || null == record2) {
            return false;
        } else if (record1.size() != record2.size()) {
            return false;
        } else {
            for (Entry<String, Object> entry : record1.entrySet()) {
                Object o1 = entry.getValue();
                Object o2 = record2.get(entry.getKey());
                if (null == o1 && null == o2) {
                    continue;
                } else if (null == o1 || null == o2) {
                    return false;
                } else if (o1.equals(o2)) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * �������sql
     * 
     * @param sqlMap                ÿ�ж�Ӧ�Ĳ���
     * @return                      ����sql��估��Ӧ�Ĳ����б�
     */
    private SqlParamPair genInsertSql(Map<String, Object> sqlMap) {
        SqlParamPair insertSql = new SqlParamPair();
        int i = 0;

        insertSql.sql = "insert into " + tableName + " ";
        insertSql.params = new Object[sqlMap.size()];

        for (Entry<String, Object> entry : sqlMap.entrySet()) {
            insertSql.sql = insertSql.sql + entry.getKey() + "=? ";
            insertSql.params[i++] = entry.getValue();
        }

        return insertSql;
    }

    /**
     * �����ѯsql
     * 
     * @param sqlMap                ÿ�ж�Ӧ�Ĳ���
     * @return                      ��ѯsql��估��Ӧ�Ĳ����б�
     */
    private SqlParamPair genQuerySql(Map<String, Object> sqlMap) {
        int i;
        SqlParamPair querySql = new SqlParamPair();
        querySql.sql = "select ";
        querySql.params = new Object[hashIndexes.length + filterIndexes.length];

        i = 1;
        for (String field : sqlMap.keySet()) {
            if (i < sqlMap.keySet().size()) {
                querySql.sql = querySql.sql + field + ",";
            } else {
                querySql.sql = querySql.sql + field;
            }
            i++;
        }
        querySql.sql = querySql.sql + " from " + tableName + " indexwhere ";

        if (hashIndexes == null || hashIndexes.length == 0) {
            return null;
        }

        for (i = 0; i < hashIndexes.length; i++) {
            if (i == 0) {
                querySql.sql = querySql.sql + hashIndexes[i] + "=? ";
            } else {
                querySql.sql = querySql.sql + " and " + hashIndexes[i] + "=? ";
            }
            querySql.params[i] = sqlMap.get(hashIndexes[i]);
        }

        for (i = 0; i < filterIndexes.length; i++) {
            if (i == 0) {
                querySql.sql = querySql.sql + " where " + filterIndexes[i] + "=? ";
            } else {
                querySql.sql = querySql.sql + " and " + filterIndexes[i] + "=? ";
            }
            querySql.params[i + hashIndexes.length] = sqlMap.get(filterIndexes[i]);
        }

        return querySql;
    }

    /**
     * ��docserver�в�¼����
     * 
     * @param sqlMap                    ÿ�м���Ӧ�Ĳ���
     * @return                          ����1����ʾ���ݲ�¼�ɹ�������-1����ʾ���ݲ�¼ʧ�ܣ�����0����ʾ�����Ѿ�д�룬�����в�¼
     */
    protected int loadData(Map<String, Object> sqlMap) {

        SqlParamPair insertSql = genInsertSql(sqlMap);
        SqlParamPair querySql = genQuerySql(sqlMap);
        MileQueryResult mileQueryResult = null;
        MileInsertResult mileInsertResult = null;

        try {
            // ����ѹ��
            Thread.sleep(100);
            // �Ƚ��в�ѯ���ж�mile���Ƿ��Ѿ��洢����Ӧ�ļ�¼
            mileQueryResult = applationClientImpl.preQueryForList(querySql.sql, querySql.params,
                3000);
            if (mileQueryResult.isSuccessful()) {
                List<Map<String, Object>> queryResult = mileQueryResult.getQueryResult();

                for (Map<String, Object> map : queryResult) {
                    if (isRecordEqual(map, sqlMap)) {
                        // �Ѿ��м�¼�ˣ����ٽ��в�¼
                        return 0;
                    }
                }
            }else{
                return -1;
            }

            // mile��û�ж�Ӧ�ļ�¼�����в�¼
            mileInsertResult = applationClientImpl.preInsert(insertSql.sql, insertSql.params, 1000);
            if(!mileInsertResult.isSuccessful()){
                return -1;
            }

        } catch (Exception e) {
            return -1;
        }

        return 1;
    }

    public void setApplationClientImpl(ApplationClientImpl applationClientImpl) {
        this.applationClientImpl = applationClientImpl;
    }

    public ApplationClientImpl getApplationClientImpl() {
        return applationClientImpl;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String[] getHashIndexes() {
        return hashIndexes;
    }

    public void setHashIndexes(String[] hashIndexes) {
        this.hashIndexes = hashIndexes;
    }

    public String[] getFilterIndexes() {
        return filterIndexes;
    }

    public void setFilterIndexes(String[] filterIndexes) {
        this.filterIndexes = filterIndexes;
    }

}
