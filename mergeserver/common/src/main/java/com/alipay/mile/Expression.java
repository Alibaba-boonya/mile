/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author jin.qian
 * @version $Id: Expression.java, v 0.1 2011-5-10 ����05:03:53 jin.qian Exp $
 */
public abstract class Expression {
    /** ���ʽ���а����ڵ�ĸ��� */
    public int        size     = 1;

    /** ��ڵ� */
    public Expression leftExp  = null;

    /** �ҽڵ� */
    public Expression rightExp = null;

    /**
     * ���and �ڵ�
     * 
     * @param another
     * @return
     */
    public Expression andExp(Expression another) {
        OperatorExp parent = new OperatorExp();
        parent.leftExp = this;
        parent.operator = Constants.EXP_LOGIC_AND;
        parent.rightExp = another;
        parent.size = 1 + this.size + another.size;
        return parent;
    }

    /**
     * ��� or �ڵ�
     * 
     * @param another
     * @return
     */
    public Expression orExp(Expression another) {
        OperatorExp parent = new OperatorExp();
        parent.leftExp = this;
        parent.operator = Constants.EXP_LOGIC_OR;
        parent.rightExp = another;
        parent.size = 1 + this.size + another.size;
        return parent;
    }

    /**
     * �Ƿ����м�ڵ�
     * 
     * @return
     */
    public boolean isComposition() {
        return leftExp != null || rightExp != null;
    }

    /**
     * �õ�������
     * 
     * @return
     */
    public Expression getLeft() {
        return this.leftExp;
    }

    /**
     * �õ�������
     * 
     * @return
     */
    public Expression getRight() {
        return this.rightExp;
    }

    /**
     * �����ʽ��ͨ����������ķ�ʽд�뵽�������
     * 
     * @param os		�����
     * @throws IOException
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws IllegalSqlException 
     * @throws SqlExecuteException 
     */
    public void postWriteToStream(DataOutput os, Map<Object, List<Object>> paramBindMap) throws IOException {
        if (isComposition()) {
            leftExp.postWriteToStream(os, paramBindMap);
            rightExp.postWriteToStream(os, paramBindMap);
        }
        writeToStream(os, paramBindMap);
    }

    /**
     * �����ʽд�뵽�������
     * 
     * @param os
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws IllegalSqlException 
     * @throws SqlExecuteException 
     */
    public abstract void writeToStream(DataOutput os, Map<Object, List<Object>> params) throws IOException;

}
