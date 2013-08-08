/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import com.alipay.mile.Constants;

public abstract class QueryStatementExpression extends Statement {
    public int                      size = 1;
    // left expression
    public QueryStatementExpression leftExpression;
    // right expression
    public QueryStatementExpression rightExpression;

    /**
     * ���IntersectSet �ڵ�
     *
     * @param another
     * @return
     */
    public QueryStatementExpression intersectSetExp(QueryStatementExpression another) {
        SetOperatorExpression parent = new SetOperatorExpression();
        parent.leftExpression = this;
        parent.operator = Constants.EXP_INTERSECTION;
        parent.rightExpression = another;
        parent.size = 1 + this.size + another.size;
        return parent;
    }

    /**
     * ��� UnionSet �ڵ�
     *
     * @param another
     * @return
     */
    public QueryStatementExpression unionSetExp(QueryStatementExpression another) {
        SetOperatorExpression parent = new SetOperatorExpression();
        parent.leftExpression = this;
        parent.operator = Constants.EXP_UNIONSET;
        parent.rightExpression = another;
        parent.size = 1 + this.size + another.size;
        return parent;
    }

    /**
     * �Ƿ����м�ڵ�
     *
     * @return
     */
    public boolean isComposition() {
        return leftExpression != null || rightExpression != null;
    }

    /**
     * �õ�������
     *
     * @return
     */
    public Statement getLeft() {
        return this.leftExpression;
    }

    /**
     * �õ�������
     *
     * @return
     */
    public Statement getRight() {
        return this.rightExpression;
    }


}
