/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alipay.mile.FieldDesc;
import com.alipay.mile.util.ByteConveror;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: GetkvStatement.java, v 0.1 2012-5-15 ����21:54:27 yuzhong.zhao Exp $
 */
public class GetkvStatement extends Statement {
    /** ��ѯ������Щ�ֶ� */
    public List<FieldDesc> selectFields     = new ArrayList<FieldDesc>();
    /** docid�������û�ֱ��ָ����ѯ */
    public DocHint         dochint;
	
	/* (non-Javadoc)
	 * @see com.alipay.mile.server.query.Statement#writeToStream(java.io.DataOutput, java.util.Map)
	 */
	@Override
	public void writeToStream(DataOutput os,
			Map<Object, List<Object>> paramBindMap) throws IOException {
        //���� tableName
        ByteConveror.writeString(os, tableName);
        //���� selectColumns
        if (selectFields == null || selectFields.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(selectFields.size());
            //���� selectColumns
            for (FieldDesc field : selectFields) {
                field.writeToStream(os, paramBindMap);
            }
        }
        //���� docIdList
        if (dochint == null) {
            os.writeInt(0);
        } else {
            os.writeInt(1);
            os.writeLong(dochint.docId);
        }

	}

}
