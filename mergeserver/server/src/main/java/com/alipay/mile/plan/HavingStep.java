/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.alipay.mile.Constants;
import com.alipay.mile.Expression;
import com.alipay.mile.FieldDesc;
import com.alipay.mile.OperatorExp;
import com.alipay.mile.Record;
import com.alipay.mile.SqlResultSet;
import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.ColumnExp;
import com.alipay.mile.server.query.ColumnSubSelectExp;

/**
 * 
 * having���裬�Ծ����Ľ��������������
 * 
 * @author yuzhong.zhao
 * @version $Id: HavingStep.java, v 0.1 2012-6-26 ����09:02:29 yuzhong.zhao Exp $
 */
public class HavingStep implements ExecuteStep {
    // having����
    private Expression havingCondition;
    // having����ѡ�����е�����λ��
    private Map<FieldDesc, Integer> havingIndex;
    
    
    
    public HavingStep(Expression havingCondition, List<FieldDesc> selectFields) throws SqlExecuteException{
        this.havingCondition = havingCondition;
        this.havingIndex = new HashMap<FieldDesc, Integer>();
        computeHavingIndex(havingCondition, selectFields);
    }
    
    
    
    /**
     * ����having����ѡ�����е�λ��
     *
     * @param havingCondition       having����
     * @param selectFields          ѡ����
     * @throws SqlExecuteException
     * @throws Exception
     */
    private void computeHavingIndex(Expression havingCondition, List<FieldDesc> selectFields)
                                                                                             throws SqlExecuteException {
        if (null == havingCondition) {
            return;
        }

        if (havingCondition.isComposition()) {
            // �ݹ����
            computeHavingIndex(havingCondition.getLeft(), selectFields);
            computeHavingIndex(havingCondition.getRight(), selectFields);
        } else {
            FieldDesc field = null;
            if (havingCondition instanceof ColumnExp) {
                field = ((ColumnExp) havingCondition).column;
            } else if (havingCondition instanceof ColumnSubSelectExp) {
                field = ((ColumnSubSelectExp) havingCondition).column;
            } else {
                throw new SqlExecuteException("unknown expression" + havingCondition);
            }
            int index = selectFields.indexOf(field);
            if (index < 0) {
                throw new SqlExecuteException("��ѡ�������Ҳ���having������" + field);
            } else {
                havingIndex.put(field, index);
            }
        }
    }
    
    
    
    /**
     * �Ƚ��������󣬵�o1<o2ʱ���ظ�������o1>o2ʱ�����������������ʱ����0
     *
     * @param o1
     * @param o2
     * @return
     * @throws SqlExecuteException
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public int compare(Object o1, Object o2) throws SqlExecuteException {
        if (null == o1 && null == o2) {
            return 0;
        } else if (null == o1) {
            return -1;
        } else if (null == o2) {
            return 1;
        } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
            Comparable<Object> comp1 = (Comparable<Object>) o1;
            Comparable<Object> comp2 = (Comparable<Object>) o2;
            return comp1.compareTo(comp2);
        } else {
            throw new SqlExecuteException("���ܽ��бȽϵĶ�������");
        }
    }
    
    
    
    

    /**
     * ����having��������
     *
     * @param record                ��Ҫ���й��˵ļ�¼
     * @param havingCondition       having�������ʽ
     * @param params                having�����еĶ�̬�󶨲���
     * @param timeOut               ��ʱʱ��
     * @return                      �Ƿ�����having����
     * @throws SqlExecuteException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     * @throws IllegalSqlException
     * @throws Exception
     */
    private boolean having(Record record, Expression havingCondition,
            Map<Object, List<Object>> paramBindMap, int timeOut) throws SqlExecuteException,
                                                       IllegalSqlException, IOException,
                                                       InterruptedException, ExecutionException {
        if (havingCondition.isComposition()) {
            // �˽ڵ㲻��Ҷ�ӽڵ�
            OperatorExp operatorExp = (OperatorExp) havingCondition;
            switch (operatorExp.getOperator()) {
                case Constants.EXP_LOGIC_AND:
                    return having(record, havingCondition.getLeft(), paramBindMap, timeOut)
                           && having(record, havingCondition.getRight(), paramBindMap, timeOut);
                case Constants.EXP_LOGIC_OR:
                    return having(record, havingCondition.getLeft(), paramBindMap, timeOut)
                           || having(record, havingCondition.getRight(), paramBindMap, timeOut);
                default:
                    throw new SqlExecuteException("��֧�ֵ��߼������" + operatorExp.getOperator());
            }
        } else {
            // Ҷ�ӽڵ��having��������
            byte comparetor;
            Object left;
            List<Object> right;
            if (havingCondition instanceof ColumnExp) {
                ColumnExp exp = (ColumnExp) havingCondition;
                comparetor = exp.comparetor;
                int index = havingIndex.get(exp.getField());
                left = record.data.get(index);
                right = exp.getBindParams(paramBindMap);
            } else if (havingCondition instanceof ColumnSubSelectExp) {
                ColumnSubSelectExp exp = (ColumnSubSelectExp) havingCondition;
                comparetor = exp.comparetor;
                left = record.data.get(havingIndex.get(exp.column));
                right = exp.getBindParams(paramBindMap);
            } else {
                throw new SqlExecuteException("δ֪��having�������ʽ" + havingCondition);
            }

            switch (comparetor) {
                case Constants.EXP_COMPARE_EQUALS:
                    return left.equals(right.get(0));
                case Constants.EXP_COMPARE_NOT_EQUALS:
                    return !left.equals(right.get(0));
                case Constants.EXP_COMPARE_LT:
                    return compare(left, right.get(0)) < 0;
                case Constants.EXP_COMPARE_GET:
                    return compare(left, right.get(0)) >= 0;
                case Constants.EXP_COMPARE_GT:
                    return compare(left, right.get(0)) > 0;
                case Constants.EXP_COMPARE_LET:
                    return compare(left, right.get(0)) <= 0;
                case Constants.EXP_COMPARE_BETWEEN_LEG:
                    return compare(left, right.get(0)) >= 0 && compare(left, right.get(1)) < 0;
                case Constants.EXP_COMPARE_BETWEEN_LEGE:
                    return compare(left, right.get(0)) >= 0 && compare(left, right.get(1)) <= 0;
                case Constants.EXP_COMPARE_BETWEEN_LG:
                    return compare(left, right.get(0)) > 0 && compare(left, right.get(1)) < 0;
                case Constants.EXP_COMPARE_BETWEEN_LGE:
                    return compare(left, right.get(0)) > 0 && compare(left, right.get(1)) <= 0;
                case Constants.EXP_COMPARE_IN:
                    for (Object rightObj : right) {
                        if (left.equals(rightObj)) {
                            return true;
                        }
                    }
                    return false;
                default:
                    throw new SqlExecuteException("��֧�ֵıȽ������" + comparetor);
            }
        }
    }
    
    
    
    /** 
     * @see com.alipay.mile.plan.ExecuteStep#execute(java.lang.Object, java.util.Map, int)
     */
    @Override
    public Object execute(Object input, Map<Object, List<Object>> paramBindMap, int timeOut)
                                                                                      throws SqlExecuteException,
                                                                                      IOException,
                                                                                      InterruptedException,
                                                                                      ExecutionException,
                                                                                      IllegalSqlException {

        SqlResultSet resultSet;
        
        if (null == input) {
            throw new SqlExecuteException("��ִ��having��ʱ����Ϊ��");
        }
        if (!(input instanceof SqlResultSet)) {
            throw new SqlExecuteException("��ִ��having��ʱ���벻��SqlResultSet����");
        }
        resultSet = (SqlResultSet) input;
        
        List<Record> havingResult = new ArrayList<Record>();
        for(Record record : resultSet.data){
            if(having(record, havingCondition, paramBindMap, timeOut)){
                havingResult.add(record);
            }
        }

        resultSet.data = havingResult;
        return resultSet;
    }



    public Expression getHavingCondition() {
        return havingCondition;
    }



    public void setHavingCondition(Expression havingCondition) {
        this.havingCondition = havingCondition;
    }



    public Map<FieldDesc, Integer> getHavingIndex() {
        return havingIndex;
    }



    public void setHavingIndex(Map<FieldDesc, Integer> havingIndex) {
        this.havingIndex = havingIndex;
    }

}
