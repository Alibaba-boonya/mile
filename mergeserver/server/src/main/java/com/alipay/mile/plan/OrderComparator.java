/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alipay.mile.Constants;
import com.alipay.mile.FieldDesc;
import com.alipay.mile.Record;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.OrderDesc;

/**
 *
 * ��������ıȽ���
 * 
 * @author yuzhong.zhao
 * @version $Id: OrderComparator.java,v 0.1 2011-5-16 ����10:48:53 yuzhong.zhao Exp $
 */
public class OrderComparator implements Comparator<Record>, Serializable {
    private static final long serialVersionUID = 4890487109060075783L;

    // ������
    private List<OrderDesc>   orderFields;

    // ��������ѡ�����е�λ��
    private List<Integer>     index;

    /**
     * 
     * @param orderFields
     * @param selectFields
     * @throws SqlExecuteException
     */
    public OrderComparator(List<OrderDesc> orderFields, List<FieldDesc> selectFields)
                                                                                     throws SqlExecuteException {
        if (null == orderFields || orderFields.isEmpty()) {
            throw new SqlExecuteException("������Ϊ��");
        }
        if (null == selectFields || selectFields.isEmpty()) {
            throw new SqlExecuteException("ѡ����Ϊ��");
        }
        this.orderFields = orderFields;
        this.index = new ArrayList<Integer>(orderFields.size());

        for (int i = 0; i < orderFields.size(); i++) {
            int j = selectFields.indexOf(orderFields.get(i).field);
            if (j < 0) {
                throw new SqlExecuteException("��ѡ�������Ҳ���������");
            }
            index.add(j);
        }
    }

    /**
     * �Ƚ����м�¼�Ĵ�С
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public int compare(Record r1, Record r2) {
        int ret = 0;

        for (int i = 0; i < orderFields.size(); i++) {
            // ȡ�����������
            OrderDesc orderDesc = orderFields.get(i);
            Object o1 = r1.data.get(index.get(i));
            Object o2 = r2.data.get(index.get(i));

            if (null == o1 && null == o2) {
                continue;
            } else if (null == o1) {
                ret = -1;
            } else if (null == o2) {
                ret = 1;
            } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                Comparable<Object> comp1 = (Comparable<Object>) o1;
                Comparable<Object> comp2 = (Comparable<Object>) o2;
                ret = comp1.compareTo(comp2);
            } else {
                return 0;
            }

            if (ret != 0) {
                // ������Ĵ���
                if (orderDesc.type == Constants.ORDER_TYPE_DESC) {
                    ret = -ret;
                }
                break;
            }
        }
        return ret;
    }

    public List<OrderDesc> getOrderFields() {
        return orderFields;
    }

    public void setOrderFields(List<OrderDesc> orderFields) {
        this.orderFields = orderFields;
    }

    public List<Integer> getIndex() {
        return index;
    }

    public void setIndex(List<Integer> index) {
        this.index = index;
    }

}
