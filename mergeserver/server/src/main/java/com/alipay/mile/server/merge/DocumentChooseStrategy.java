/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.server.merge;

import java.util.Collection;
import java.util.List;

import com.alipay.mile.communication.ServerRef;

/**
 * ����ѡ����ʵ�docserver���ϲ��ȡ����Ҫ���͵���docserver�б�����Ҫ�����²�ʹ�õ�ʲô����
 * �����ֹ���һ���Ƕ�дȨ�أ�һ����Roundbinģʽ�������sharding�Ļ������ֹ�����Ҫ����sharding
 * @author yunliang.shi
 * @version $Id: DocumentChooseStrategy.java, v 0.1 2011-7-6 ����01:56:39 yunliang.shi Exp $
 */
public interface DocumentChooseStrategy {

    /**
     * ����serverIdѡ��ɶ���docserver
     * @param serverId
     * @return
     */
    ServerRef chooseReadDocumentServerById(int serverId);

    /**
     * ����serverIdѡ���д��docserver
     * @param serverId
     * @return
     */
    ServerRef chooseWriteDocumentServerById(int serverId);

    /**
     * ���ݲ���ѡ����ʵ�docserver
     * 
     * @param nodeIds
     * @return
     */
    ServerRef chooseInsertDocumentServer(Collection<Integer> nodeIds);

    /**
     * ���ݲ�ѯ�Ĳ���ѡ����ʵ�docserver
     * 
     * @param nodeIds
     * @return
     */
    List<ServerRef> chooseQueryDocumentServer(Collection<Integer> nodeIds);

    /**
     * ��delete��update��ʱ��ѡ����ʵ�docserver
     * 
     * @param nodeIds
     * @return
     */
    List<ServerRef> chooseChangeDocumentServer(Collection<Integer> nodeIds);

}
