/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */

package com.alipay.mile.server.merge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.alipay.mile.communication.MileClient;
import com.alipay.mile.communication.Node;
import com.alipay.mile.communication.ServerRef;

/**
 * ���ѡ����ԣ�insert��update��delete����ѡ������node��master docserver��query���������ѡ��
 *
 * @author yunliang.shi, yuzhong.zhao
 * @version $Id: RoundbinStrategy.java, v 0.1 2011-7-6 ����02:40:39 yunliang.shi
 *          Exp $
 */
public class RandomStrategy implements DocumentChooseStrategy {
    private static final Logger LOGGER = Logger.getLogger(RandomStrategy.class.getName());

    private final MileClient    mileClient;

    public RandomStrategy(MileClient mileClient) {
        this.mileClient = mileClient;
    }

    @Override
    public ServerRef chooseReadDocumentServerById(int serverId) {
        Node node = mileClient.getNodes().get(serverId);
        List<ServerRef> servers = new ArrayList<ServerRef>();
        if (node.isSelectAvailable()) {
            if (node.getMasterDoc().isAvailable() && node.getMasterDoc().isOnline()
                && !node.getMasterDoc().isQueryBusy()) {
                servers.add(node.getMasterDoc());
            }
            for (ServerRef srf : node.getSlaveDocs()) {
                if (srf.isAvailable() && srf.isOnline() && !srf.isQueryBusy()) {
                    servers.add(srf);
                }
            }
        } else {
            LOGGER.error("�ڽ���ָ��docid�Ĳ�ѯ����ʱ��nodeIdΪ" + serverId + "������docserver��������!");
            ServerRef serverRef = new ServerRef();
            serverRef.setServerId(serverId);
            serverRef.setChannel(null);
            return serverRef;
        }

        if (servers.isEmpty()) {
            LOGGER.error("�ڽ���ָ��docid�Ĳ�ѯ����ʱ��nodeIdΪ" + serverId + "������docserver��������!");
            ServerRef serverRef = new ServerRef();
            serverRef.setServerId(serverId);
            serverRef.setChannel(null);
            return serverRef;
        } else {
            int rand = (int) (Math.random() * (servers.size()));
            return servers.get(rand);
        }
    }

    @Override
    public ServerRef chooseWriteDocumentServerById(int serverId) {
        Node node = mileClient.getNodes().get(serverId);
        if (node.isSelectAvailable()
            && (node.getMasterDoc().isAvailable() && node.getMasterDoc().isOnline() && !node
                .getMasterDoc().isInsertBusy())) {
            return node.getMasterDoc();
        } else {
            LOGGER.error("�ڽ���ָ��docid��д�����ʱ��nodeIdΪ" + serverId + "����docserver������!");
            ServerRef serverRef = new ServerRef();
            serverRef.setServerId(serverId);
            serverRef.setChannel(null);
            return serverRef;
        }
    }

    /**
     * @throws Exception
     * @throws SqlExecuteException
     * @see com.alipay.mile.server.merge.DocumentChooseStrategy#chooseChangeDocumentServer(java.util.Collection)
     */
    @Override
    public List<ServerRef> chooseChangeDocumentServer(Collection<Integer> nodeIds) {
        List<ServerRef> servers = new ArrayList<ServerRef>();
        for (Integer nodeId : nodeIds) {
            if (mileClient.getNodes().get(nodeId).isInsertAvailable()) {
                servers.add(mileClient.getNodes().get(nodeId).getMasterDoc());
            } else {
                LOGGER.error("�ڽ����޸Ĳ���ʱ��nodeIdΪ" + nodeId + "����docserver������!");
                ServerRef serverRef = new ServerRef();
                serverRef.setServerId(nodeId);
                serverRef.setChannel(null);
                servers.add(serverRef);
            }
        }
        return servers;
    }

    /**
     * @throws Exception
     * @see com.alipay.mile.server.merge.DocumentChooseStrategy#chooseInsertDocumentServer(java.util.Collection)
     */
    @Override
    public ServerRef chooseInsertDocumentServer(Collection<Integer> nodeIds) {
        List<ServerRef> servers = new ArrayList<ServerRef>();
        for (Integer nodeId : nodeIds) {
            Node node = mileClient.getNodes().get(nodeId);
            if (node.isInsertAvailable()
                && !mileClient.getNodes().get(nodeId).getMasterDoc().isInsertBusy()) {
                servers.add(mileClient.getNodes().get(nodeId).getMasterDoc());
            }
        }
        if (servers.isEmpty()) {
            LOGGER.error("�ڽ��в������ʱ��û�п��õ���docserver!");
            ServerRef serverRef = new ServerRef();
            serverRef.setServerId(1);
            serverRef.setChannel(null);
            return serverRef;
        }
        int rand = (int) (Math.random() * (servers.size()));
        return servers.get(rand);
    }

    /**
     * @throws Exception
     * @see com.alipay.mile.server.merge.DocumentChooseStrategy#chooseQueryDocumentServer(java.util.Collection)
     */
    @Override
    public List<ServerRef> chooseQueryDocumentServer(Collection<Integer> nodeIds) {
        List<ServerRef> servers = new ArrayList<ServerRef>();
        Node node;
        int rand;
        for (Integer nodeId : nodeIds) {
            node = mileClient.getNodes().get(nodeId);
            if (node.isSelectAvailable()) {
                List<ServerRef> nodeServers = new ArrayList<ServerRef>();
                if (node.getMasterDoc().isAvailable() && node.getMasterDoc().isOnline()) {
                    nodeServers.add(node.getMasterDoc());
                }
                for (ServerRef srf : node.getSlaveDocs()) {
                    if (srf.isAvailable() && srf.isOnline()) {
                        nodeServers.add(srf);
                    }
                }
                //�⴦���룬��ֹ���ӽڵ㣬ͻȻ�����ã���doublecheck������
                if (nodeServers.isEmpty()) {
                    LOGGER.error("�ڽ��в�ѯ����ʱ��nodeIdΪ" + nodeId + "������docserver��������!");
                    ServerRef serverRef = new ServerRef();
                    serverRef.setServerId(nodeId);
                    serverRef.setChannel(null);
                    servers.add(serverRef);
                } else {
                    rand = (int) (Math.random() * (nodeServers.size()));
                    servers.add(nodeServers.get(rand));
                }
            } else if(node.getMasterDoc().isOnline()) {
                LOGGER.error("�ڽ��в�ѯ����ʱ��nodeIdΪ" + nodeId + "docserver���!");
                ServerRef serverRef = new ServerRef();
                serverRef.setServerId(nodeId);
                serverRef.setChannel(null);
                servers.add(serverRef);
            }
        }
        return servers;
    }

}
