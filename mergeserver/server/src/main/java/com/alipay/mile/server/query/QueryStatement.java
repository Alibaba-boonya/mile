/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alipay.mile.Constants;
import com.alipay.mile.Expression;
import com.alipay.mile.FieldDesc;
import com.alipay.mile.util.ByteConveror;

/**
 *
 * @author huabing.du
 * @version $Id: QueryStatement.java, v 0.1 2011-4-18 ����09:59:50 Exp huabing.du $
 */
public class QueryStatement extends QueryStatementExpression {
    /** ��ѯ����  */
    public short           accessType       = Constants.QT_COMMON_QUERY;
    /** ��ѯ������Щ�ֶ� */
    public List<FieldDesc> selectFields     = new ArrayList<FieldDesc>();
    /** segmentʱ����ˣ�������С��ѯ��segment����������� */
    public TimeHint        hint;
    /** docid�������û�ֱ��ָ����ѯ */
    public DocHint         dochint          = null;
    /** ��hash��btree���������� */
    public Expression      hashWhere;
    /** ��filter���������� */
    public Expression      filterWhere;
    /** ��ҪgroupBy(����)���ֶ� */
    public List<FieldDesc> groupByFields    = new ArrayList<FieldDesc>();
    /** groupBy(����)��������ֶ� */
    public List<OrderDesc> groupOrderFields = new ArrayList<OrderDesc>();
    /** groupBy����Ҫ���Ʒ��ص����� */
    public int             groupLimit       = 100;
    /** groupBy�дӵڼ��п�ʼ */
    public int             groupOffset      = 0;
    /** having���� */
    public Expression      having;
    /** ��ҪorderBy(����)���ֶ� */
    public List<OrderDesc> orderFields      = new ArrayList<OrderDesc>();
    /** ���Ʒ��ض����� */
    public int             limit            = Constants.queryResultLimit;
    /** �ӵڼ��п�ʼ */
    public int             offset           = 0;

    @Override
    public void writeToStream(DataOutput os, Map<Object, List<Object>> paramBindMap)
                                                                                    throws IOException {
        // ���� accessType
        os.writeShort(accessType);
        // ���� tableName
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

        // ���� ѡ����
        if (selectFields == null || selectFields.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(selectFields.size());
            for (FieldDesc field : selectFields) {
                field.writeToStream(os, paramBindMap);
            }
        }

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

        // ���� groupFields
        if (groupByFields == null || groupByFields.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(groupByFields.size());
            for (FieldDesc field : groupByFields) {
                ByteConveror.writeString(os, field.fieldName);
            }
        }

        // ���� groupOrderFields
        if (groupOrderFields == null || groupOrderFields.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(groupOrderFields.size());
            for (OrderDesc od : groupOrderFields) {
                od.writeToStream(os);
            }
        }

        // ���� groupLimit
        os.writeInt(groupLimit);

        // ���� orderFields
        if (orderFields == null || orderFields.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(orderFields.size());
            for (OrderDesc od : orderFields) {
                od.writeToStream(os);
            }
        }
        // ���� limit
        os.writeInt(limit);

    }
}
