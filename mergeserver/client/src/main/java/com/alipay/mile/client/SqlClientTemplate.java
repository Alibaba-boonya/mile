/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileExportResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.client.result.MileUpdateResult;
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * @author jin.qian
 * @version $Id: SqlClientTemplate.java,v 0.1 2011-4-6 ����05:50:33 jin.qian Exp $
 */
public interface SqlClientTemplate {

    /**
     * Ԥ����sqlִ��
     * @param sql
     * @param params
     * @param timeOut
     * @return
     * @throws Exception
     */
    public MileQueryResult preQueryForList(String sql, Object[] params, int timeOut)
                                                                                    throws SqlExecuteException,
                                                                                    IOException,
                                                                                    InterruptedException,
                                                                                    ExecutionException;

    /**
     * Ԥ����sqlִ��
     * @param sql
     * @param params
     * @param timeOut
     * @return
     * @throws Exception
     */
    public MileUpdateResult preUpdate(String sql, Object[] params, int timeOut)
                                                                               throws SqlExecuteException,
                                                                               IOException,
                                                                               InterruptedException,
                                                                               ExecutionException;

    /**
     * Ԥ����sqlִ��
     * @param sql
     * @param params
     * @param timeOut
     * @return
     * @throws Exception
     */
    public MileInsertResult preInsert(String sql, Object[] params, int timeOut)
                                                                               throws SqlExecuteException,
                                                                               IOException,
                                                                               InterruptedException,
                                                                               ExecutionException;

    /**
     * Ԥ����sqlִ��
     * @param sql
     * @param params
     * @param timeOut
     * @return
     * @throws Exception
     */
    public MileDeleteResult preDelete(String sql, Object[] params, int timeOut)
                                                                               throws SqlExecuteException,
                                                                               IOException,
                                                                               InterruptedException,
                                                                               ExecutionException;

    /**
     * Ϊctu��̨�Ĳ�ѯ������������ʱ�ӿڣ�����������ϵͳʹ�ã����ڻ�����ͨ�õĽӿڣ��˽ӿڻ��𲽷�����
     * ͳ�ư��û����˳����ļ�¼����
     * 
     * @param tableName         ����������"t"
     * @param condition         �����ֶΣ�����"seghint(0, 100, 0, 0) indexwhere CP=? where GR>? and GR<?"
     * @param clusterField      Ҫ�ۼ����У�����"UD"
     * @param topField          ��ѡtop��¼�����ݵ��У�����"max(GR)"����"min(GR)"
     * @param params            �����б�
     * @param timeOut           ��ʱ
     * @return
     */
    public int preCtuClusterCountQuery(String tableName, String condition, String clusterField,
                                       String topField, Object[] params, int timeOut)
                                                                                     throws SqlExecuteException,
                                                                                     IOException,
                                                                                     InterruptedException,
                                                                                     ExecutionException;

    /**
     * Ϊctu��̨�Ĳ�ѯ������������ʱ�ӿڣ�����������ϵͳʹ�ã����ڻ�����ͨ�õĽӿڣ��˽ӿڻ��𲽷�����
     * ��ҳ��ʾ���û����˵Ĳ�ѯ���
     * 
     * @param tableName         ����
     * @param selectFields      Ҫѡ�����
     * @param condition         �����ֶΣ�����"seghint(0, 100, 0, 0) indexwhere CP=? where GR>? and GR<?"
     * @param clusterField      Ҫ�ۼ����У�����"UD"
     * @param topField          ��ѡtop��¼�����ݵ��У�����"max(GR)"����"min(GR)"
     * @param orderField        Ҫ������У�����"GR"
     * @param orderType         �������ͣ�˳��/����true��ʾ˳��false��ʾ����
     * @param limit             
     * @param offset            limit=100,offset=90ʱ��ʾ��91����¼����100����¼
     * @param params            �����б�
     * @param timeOut           ��ʱ����λΪms������3000��ʾ��ʱʱ��Ϊ3��
     * @return                  ��ѯ�����
     * @throws Exception
     */
    public MileQueryResult preCtuClusterQuery(String tableName, List<String> selectFields,
                                              String condition, String clusterField,
                                              String topField, String orderField,
                                              boolean orderType, int limit, int offset,
                                              Object[] params, int timeOut)
                                                                           throws SqlExecuteException,
                                                                           IOException,
                                                                           InterruptedException,
                                                                           ExecutionException;

    /**
     * ִ��Ԥ����export���
     * @param sql
     * @param params
     * @param timeOut
     * @return
     * @throws SqlExecuteException
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
	MileExportResult preExport(String sql, Object[] params, int timeOut)
			throws SqlExecuteException, IOException, InterruptedException,
			ExecutionException;

}
