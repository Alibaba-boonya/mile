/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.client.result;

import java.util.List;

import com.alipay.mile.DocDigestData;

/**
 *
 * @author yuzhong.zhao
 * @version $Id: MileSqlResult.java, v 0.1 2011-9-8 ����01:46:23 yuzhong.zhao Exp
 *          $
 */
public class MileSqlResult {

    private List<DocDigestData> docState;

    public List<DocDigestData> getDocState() {
        return docState;
    }

    public void setDocState(List<DocDigestData> docState) {
        this.docState = docState;
    }

    /**
     * sqlִ���Ƿ�ɹ�
     *
     * @return true��ʾִ�гɹ���false��ʾʧ��
     */
    public boolean isSuccessful() {
        if (docState == null || docState.isEmpty()) {
            return false;
        }

        for (DocDigestData docDigestData : docState) {
            if (!docDigestData.isSuccess()) {
                return false;
            }
        }

        return true;
    }

    /**
     * ����sql���ַܷ�����̨docserver��ִ�У�������docserver崻�ʱ��������Ȼ�᷵�ز��ֵĽ������ͨ���˽ӿ��������������������
     *
     * @return ����ֵ��һ��0��1֮��ĸ�������0��ʾ����docserver���޷��أ�1��ʾ�������������
     */
    public double getCompleteness() {
        double totalDoc = docState.size();
        double sucessDoc = 0;

        if (docState == null || docState.isEmpty()) {
            return 0;
        }

        for (DocDigestData docDigestData : docState) {
            if (docDigestData.isSuccess()) {
                sucessDoc += 1;
            }
        }

        if (totalDoc == 0) {
            return 0;
        } else {
            return sucessDoc / totalDoc;
        }
    }
}
