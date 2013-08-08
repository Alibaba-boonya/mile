/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alipay.mile.FieldDesc;
import com.alipay.mile.Record;
import com.alipay.mile.SqlResultSet;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.OrderDesc;

/**
 * ���鲽�裬�Բ�ѯ�����������
 *
 * @author yuzhong.zhao
 * @version $Id: OrderStep.java,v 0.1 2011-5-15 06:59:10 yunliang.shi Exp $
 */
public class OrderStep implements ExecuteStep {
    // Ҫ�����������
    private List<OrderDesc> orderFeilds;
    // ����Ƚ���
    private OrderComparator orderComparator;

    public OrderStep(List<OrderDesc> orderFields, List<FieldDesc> selectFields)
                                                                               throws SqlExecuteException {
        this.orderFeilds = orderFields;
        this.orderComparator = new OrderComparator(orderFeilds, selectFields);
    }

    /**
     * @param sessionId    session��
     * @param input        ���򲽵�������SqlResultSet
     * @return             ���򲽵������������SqlResultSet
     * 
     * @throws SqlExecuteException
     * @see com.alipay.mile.plan.ExecuteStep#execute(java.lang.Object)
     */
    public Object execute(Object input, Map<Object, List<Object>> paramBindMap, int timeOut)
                                                                                     throws SqlExecuteException {
        // �������Ϸ���
        if (null == input) {
            throw new SqlExecuteException("ִ��Order stepʱ����Ϊ��");
        }
        if (!(input instanceof SqlResultSet)) {
            throw new SqlExecuteException("ִ��Order stepʱ���벻��SqlResultSet����");
        }

        SqlResultSet resultSet = (SqlResultSet) input;

        List<Record> data = new ArrayList<Record>(resultSet.data);
        Collections.sort(data, orderComparator);
        resultSet.data = data;

        return resultSet;
    }

    public List<OrderDesc> getOrderFeilds() {
        return orderFeilds;
    }

    public void setOrderFeilds(List<OrderDesc> orderFeilds) {
        this.orderFeilds = orderFeilds;
    }

    public OrderComparator getOrderComparator() {
        return orderComparator;
    }

    public void setOrderComparator(OrderComparator orderComparator) {
        this.orderComparator = orderComparator;
    }

}
