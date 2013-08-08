/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alipay.mile.Expression;
import com.alipay.mile.util.ByteConveror;

/**
 * ɾ������Statement
 * @author jin.qian
 * @version $Id: DeleteStatement.java, v 0.1 2011-5-10 ����05:01:50 jin.qian Exp $
 */
public class DeleteStatement extends Statement {

    /** doc id */
    public DocHint    dochint;

    /** TimeHint */
    public TimeHint   hint;

    /** hashWhere */
    public Expression hashWhere;

    /** filterWhere */
    public Expression filterWhere;
    

    

	@Override
	public void writeToStream(DataOutput os,
			Map<Object, List<Object>> paramBindMap) throws IOException {
        //���� tableName
        ByteConveror.writeString(os, tableName);

        // ���� TimeHint
        if (hint == null) {
            hint = new TimeHint();
        }
        os.writeInt(4);
        os.writeLong(hint.startCreateTime);
        os.writeLong(hint.endCreateTime);
        os.writeLong(hint.startUpdateTime);
        os.writeLong(hint.endUpdateTime);

        // ���� indexWhere
        if (null == hashWhere) {
            os.writeInt(0);
        } else {
            os.writeInt(hashWhere.size);
            hashWhere.postWriteToStream(os, paramBindMap);
        }

        // ���� filterWhere
        if (null == filterWhere) {
            os.writeInt(0);
        } else {
            os.writeInt(filterWhere.size);
            filterWhere.postWriteToStream(os, paramBindMap);
        }
	}

}
