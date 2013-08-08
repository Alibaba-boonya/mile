/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * sql�����
 * 
 * @author yuzhong.zhao
 * @version $Id: SqlResultSet.java,v 0.1 2011-5-16 下午03:01:38 yuzhong.zhao Exp
 *          $
 */
public class SqlResultSet {
    // ����docserver����ķ���״��
    public List<DocDigestData> docState;
    // �������
    public List<FieldDesc>     fields;
    // ���������
    public Collection<Record>  data;

    public SqlResultSet() {
        this.docState = new ArrayList<DocDigestData>();
        this.fields = new ArrayList<FieldDesc>();
        this.data = new ArrayList<Record>();
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
    
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("\n");
        for (FieldDesc fieldDesc : fields) {
            sb.append("[" + fieldDesc.aliseName + "] ");
        }
        sb.append("\n");

        if (data == null) {
            sb.append(data);
        } else {
            for (Record record : data) {
                for (Object obj : record.data) {
                    sb.append(obj).append("  ");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void writeToStream(DataOutput os) throws IOException {
        os.writeInt(docState.size());
        for (DocDigestData docDigestData : docState) {
            docDigestData.writeToStream(os);
        }
        os.writeInt(fields.size());
        for (FieldDesc fieldDesc : fields) {
            fieldDesc.commonPartToStream(os);
        }
        os.writeInt(data.size());
        for (Record record : data) {
            record.writeToStream(os);
        }
    }

    public void readFromStream(DataInput is) throws IOException {
        int size;

        // ����docserver״̬
        size = is.readInt();
        for (int i = 0; i < size; i++) {
            DocDigestData docDigestData = new DocDigestData();
            docDigestData.readFromStream(is);
            docState.add(docDigestData);
        }
        // ��������
        size = is.readInt();
        for (int i = 0; i < size; i++) {
            FieldDesc fieldDesc = new FieldDesc();
            fieldDesc.commonPartFromStream(is);
            fields.add(fieldDesc);
        }
        // ������¼
        size = is.readInt();
        for (int i = 0; i < size; i++) {
            Record record = new Record();
            record.readFromStream(is);
            data.add(record);
        }
    }
}
