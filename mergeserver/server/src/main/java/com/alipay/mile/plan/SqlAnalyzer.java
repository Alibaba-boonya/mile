/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alipay.mile.Constants;
import com.alipay.mile.Expression;
import com.alipay.mile.FieldDesc;
import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.ColumnExp;
import com.alipay.mile.server.query.ColumnSubSelectExp;
import com.alipay.mile.server.query.DeleteStatement;
import com.alipay.mile.server.query.ExportStatement;
import com.alipay.mile.server.query.GetkvStatement;
import com.alipay.mile.server.query.InsertStatement;
import com.alipay.mile.server.query.OrderDesc;
import com.alipay.mile.server.query.QueryStatement;
import com.alipay.mile.server.query.SetOperatorExpression;
import com.alipay.mile.server.query.Statement;
import com.alipay.mile.server.query.UpdateStatement;
import com.alipay.mile.server.query.ValueDesc;

/**
 * 
 * sql��������ͨ��sql�����������ִ�мƻ�
 * 
 * @author yuzhong.zhao
 * @version $Id: SqlAnalyzer.java,v 0.1 2011-5-19 08:13:39 yuzhong.zhao Exp $
 */

public class SqlAnalyzer {

    // ����mergeserver��docserverͨѶ����Ϣ������
    private final MessageProcessor messageProcessor;

    public SqlAnalyzer(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    /**
     * �ж�һ�����ʽ�Ƿ����ȫ�ļ���
     * 
     * @param expression
     * @return
     */
    private boolean isFulltextSearch(Expression expression) {
        if (null == expression) {
            return false;
        } else if (expression.isComposition()) {
            return isFulltextSearch(expression.getLeft())
                   || isFulltextSearch(expression.getRight());
        } else if (expression instanceof ColumnExp) {
            ColumnExp columnExp = (ColumnExp) expression;
            return columnExp.getComparetor() == Constants.EXP_COMPARE_MATCH;
        } else if (expression instanceof ColumnSubSelectExp) {
            ColumnSubSelectExp subSelect = (ColumnSubSelectExp) expression;
            return subSelect.getComparetor() == Constants.EXP_COMPARE_MATCH;
        }
        return false;
    }

    private void rewrite(Expression expression, Map<String, FieldDesc> map)
                                                                           throws SqlExecuteException,
                                                                           IllegalSqlException {
        if (null == expression) {
            return;
        } else if (expression.isComposition()) {
            rewrite(expression.getLeft(), map);
            rewrite(expression.getRight(), map);
        } else if (expression instanceof ColumnExp) {
            ColumnExp columnExp = (ColumnExp) expression;
            FieldDesc fieldDesc = map.get(columnExp.column.fieldName);
            if (null != fieldDesc) {
                columnExp.column = fieldDesc;
            }
        } else if (expression instanceof ColumnSubSelectExp) {
            ColumnSubSelectExp subSelect = (ColumnSubSelectExp) expression;
            FieldDesc fieldDesc = map.get(subSelect.column.fieldName);
            if (null != fieldDesc) {
                subSelect.column = fieldDesc;
            }
        }
        return;
    }

    private void rewriteHaving(Expression expression, Map<String, FieldDesc> map,
                               List<FieldDesc> selectFields) throws SqlExecuteException,
                                                            IllegalSqlException {
        if (null == expression) {
            return;
        } else if (expression.isComposition()) {
            rewriteHaving(expression.getLeft(), map, selectFields);
            rewriteHaving(expression.getRight(), map, selectFields);
        } else if (expression instanceof ColumnExp) {
            ColumnExp columnExp = (ColumnExp) expression;
            FieldDesc fieldDesc = map.get(columnExp.column.fieldName);
            if (null != fieldDesc) {
                columnExp.column = fieldDesc;
            } else {
                if (!selectFields.contains(columnExp.column)) {
                    selectFields.add(columnExp.column);
                }
            }
        } else if (expression instanceof ColumnSubSelectExp) {
            ColumnSubSelectExp subSelect = (ColumnSubSelectExp) expression;
            FieldDesc fieldDesc = map.get(subSelect.column.fieldName);
            if (null != fieldDesc) {
                subSelect.column = fieldDesc;
            } else {
                if (!selectFields.contains(subSelect.column)) {
                    selectFields.add(subSelect.column);
                }
            }
        }
        return;
    }

    private void analyze(Expression expression, ExecutePlan executePlan)
                                                                        throws SqlExecuteException,
                                                                        IllegalSqlException {
        if (null == expression) {
            return;
        } else if (expression.isComposition()) {
            analyze(expression.getLeft(), executePlan);
            analyze(expression.getRight(), executePlan);
        } else if (expression instanceof ColumnExp) {
            ColumnExp columnExp = (ColumnExp) expression;
            List<Integer> paramBindList = new ArrayList<Integer>();
            for (ValueDesc valueDesc : columnExp.values) {
                paramBindList.add(valueDesc.parmIndex);
            }
            executePlan.getParamBindingMap().put(columnExp, paramBindList);
        } else if (expression instanceof ColumnSubSelectExp) {
            ColumnSubSelectExp subSelect = (ColumnSubSelectExp) expression;
            ExecutePlan subPlan = analyze(subSelect.subQueryStatement);
            ExecuteStep paramBindStep = new ParamBindStep(subSelect);
            subPlan.addExecuteStep(paramBindStep);
            executePlan.mergePlan(subPlan);
        }
        return;
    }

	private void analyze(ValueDesc value, ExecutePlan plan)
		throws SqlExecuteException, IllegalSqlException {
		List<Integer> bindList = new ArrayList<Integer>();
		bindList.add(value.parmIndex);
		plan.getParamBindingMap().put(value, bindList);
	}

    /**
     * ��дinsert���
     * 
     * @param stmt
     *            sql���
     * @return ��д���sql���
     */
    private InsertStatement rewrite(InsertStatement stmt) {
        return stmt;
    }

    /**
     * ��дdelete��䣬�滻�б���
     * 
     * @param stmt
     *            ɾ�����
     * @return ��д���ɾ�����
     * @throws SqlExecuteException
     * @throws IllegalSqlException
     */
    private DeleteStatement rewrite(DeleteStatement stmt) throws SqlExecuteException,
                                                         IllegalSqlException {
        Map<String, FieldDesc> map = new HashMap<String, FieldDesc>();
        rewrite(stmt.hashWhere, map);
        rewrite(stmt.filterWhere, map);
        return stmt;
    }

    /**
     * ��дupdate��䣬�滻�б���
     * 
     * @param stmt
     *            �������
     * @return ��д��ĸ������
     * @throws SqlExecuteException
     * @throws IllegalSqlException
     */
    private UpdateStatement rewrite(UpdateStatement stmt) throws SqlExecuteException,
                                                         IllegalSqlException {
        Map<String, FieldDesc> map = new HashMap<String, FieldDesc>();
        rewrite(stmt.hashWhere, map);
        rewrite(stmt.filterWhere, map);
        return stmt;
    }

	/**
	 * Rewrite ExportStatement, replace alias name.
	 */
	private ExportStatement rewrite(ExportStatement stmt)
		throws SqlExecuteException, IllegalSqlException {
		Map<String, FieldDesc> map = new HashMap<String, FieldDesc>();
		rewrite(stmt.hashWhere, map);
		rewrite(stmt.filterWhere, map);
		return stmt;
	}

    /**
     * 
     * 
     * @param stmt
     * @return
     * @throws SqlExecuteException
     * @throws IllegalSqlException
     */
    private GetkvStatement rewrite(GetkvStatement stmt) throws SqlExecuteException,
                                                       IllegalSqlException {
        Map<String, FieldDesc> map = new HashMap<String, FieldDesc>();
        GetkvStatement rewriteStatement = new GetkvStatement();

        rewriteStatement.tableName = stmt.tableName;
        rewriteStatement.dochint = stmt.dochint;

        // ��дѡ����
        for (FieldDesc fieldDesc : stmt.selectFields) {
            if (StringUtils.isBlank(fieldDesc.aliseName)) {
                fieldDesc.aliseName = fieldDesc.fieldName;
            }
            if (!StringUtils.equals(fieldDesc.aliseName, fieldDesc.fieldName)) {
                map.put(fieldDesc.aliseName, fieldDesc);
            }
        }

        // ����ѡ����
        for (FieldDesc fieldDesc : stmt.selectFields) {
            if (!rewriteStatement.selectFields.contains(fieldDesc)) {
                rewriteStatement.selectFields.add(fieldDesc);
            }
        }
        return rewriteStatement;
    }

    /**
     * ��д��ѯ��䣬�滻�б���
     * 
     * @param stmt
     *            ��ѯ���
     * @return ��д��Ĳ�ѯ���
     * @throws SqlExecuteException
     * @throws IllegalSqlException
     */
    private QueryStatement rewrite(QueryStatement stmt) throws SqlExecuteException,
                                                       IllegalSqlException {
        Map<String, FieldDesc> map = new HashMap<String, FieldDesc>();
        QueryStatement rewriteStatement = new QueryStatement();
        rewriteStatement.tableName = stmt.tableName;
        rewriteStatement.dochint = stmt.dochint;
        rewriteStatement.hashWhere = stmt.hashWhere;
        rewriteStatement.filterWhere = stmt.filterWhere;
        rewriteStatement.having = stmt.having;
        rewriteStatement.orderFields = stmt.orderFields;
        rewriteStatement.groupByFields = stmt.groupByFields;
        rewriteStatement.groupLimit = stmt.groupLimit;
        rewriteStatement.groupOffset = stmt.groupOffset;
        rewriteStatement.limit = stmt.limit;
        rewriteStatement.offset = stmt.offset;
        rewriteStatement.hint = stmt.hint;
        rewriteStatement.accessType = stmt.accessType;

        // ��дѡ����
        for (FieldDesc fieldDesc : stmt.selectFields) {
            if (StringUtils.isBlank(fieldDesc.aliseName)) {
                fieldDesc.aliseName = fieldDesc.fieldName;
            }
            if (!StringUtils.equals(fieldDesc.aliseName, fieldDesc.fieldName)) {
                map.put(fieldDesc.aliseName, fieldDesc);
            }
        }

        // ����ѡ����
        for (FieldDesc fieldDesc : stmt.selectFields) {
            if (!rewriteStatement.selectFields.contains(fieldDesc)) {
                rewriteStatement.selectFields.add(fieldDesc);
            }
        }

        rewrite(rewriteStatement.hashWhere, map);
        // ����ȫ������
        if (isFulltextSearch(rewriteStatement.hashWhere)) {
            if (null == rewriteStatement.orderFields) {
                rewriteStatement.orderFields = new ArrayList<OrderDesc>();
            }
            OrderDesc orderDesc = new OrderDesc();
            orderDesc.field = new FieldDesc();
            orderDesc.field.fieldName = "matchscore";
            orderDesc.field.aliseName = orderDesc.field.fieldName;
            orderDesc.type = Constants.ORDER_TYPE_DESC;
            rewriteStatement.orderFields.add(orderDesc);
        }
        rewrite(rewriteStatement.filterWhere, map);
        rewriteHaving(rewriteStatement.having, map, rewriteStatement.selectFields);

        // ��дgroupby��
        for (int i = 0; i < rewriteStatement.groupByFields.size(); i++) {
            FieldDesc grpFieldDesc = rewriteStatement.groupByFields.get(i);
            if (StringUtils.isBlank(grpFieldDesc.aliseName)) {
                grpFieldDesc.aliseName = grpFieldDesc.fieldName;
            }

            FieldDesc fieldDesc = map.get(grpFieldDesc.fieldName);
            if (null != fieldDesc) {
                rewriteStatement.groupByFields.set(i, fieldDesc);
            } else {
                // group��һ��Ҫ��ѡ�����У�������ڣ���ѡ���������group��
                if (!rewriteStatement.selectFields.contains(grpFieldDesc)) {
                    rewriteStatement.selectFields.add(grpFieldDesc);
                }
            }
        }

        // ��д������
        for (OrderDesc orderDesc : rewriteStatement.orderFields) {
            if (StringUtils.isBlank(orderDesc.field.aliseName)) {
                orderDesc.field.aliseName = orderDesc.field.fieldName;
            }

            FieldDesc fieldDesc = map.get(orderDesc.field.fieldName);
            if (null != fieldDesc) {
                orderDesc.field = fieldDesc;
            } else {
                // ������һ��Ҫ��ѡ�����У�������ڣ���ѡ���������������
                if (!rewriteStatement.selectFields.contains(orderDesc.field)) {
                    rewriteStatement.selectFields.add(orderDesc.field);
                }
            }
        }

        for (FieldDesc fieldDesc : rewriteStatement.selectFields) {
            if (StringUtils.isBlank(fieldDesc.aliseName)) {
                fieldDesc.aliseName = fieldDesc.fieldName;
            }
        }

        return rewriteStatement;
    }

    /**
     * ��������sql��䣬����ִ�мƻ�
     * 
     * @param stmt
     *            sql���
     * @return ִ�мƻ�
     * @throws IllegalSqlException
     * @throws SqlExecuteException
     */
    public ExecutePlan analyze(Statement stmt) throws IllegalSqlException, SqlExecuteException {
        if (stmt instanceof InsertStatement) {
            return analyze((InsertStatement) stmt);
        } else if (stmt instanceof UpdateStatement) {
            return analyze((UpdateStatement) stmt);
        } else if (stmt instanceof DeleteStatement) {
            return analyze((DeleteStatement) stmt);
        } else if (stmt instanceof QueryStatement) {
            return analyze((QueryStatement) stmt);
        } else if (stmt instanceof SetOperatorExpression) {
            return analyze((SetOperatorExpression) stmt);
        } else if (stmt instanceof ExportStatement) {
			return analyze((ExportStatement) stmt);
		}
        throw new IllegalSqlException("��֧�ֵ�sql����");
    }

    /**
     * ���ɲ�������ִ�мƻ�
     * 
     * @param stmt
     *            ����sql
     * @return ��Ӧ��ִ�мƻ�
     */
    public ExecutePlan analyze(InsertStatement stmt) {
        InsertStatement rewriteStatement = rewrite(stmt);
        ExecutePlan executePlan = new ExecutePlan();
        ExecuteStep executeStep;

        executeStep = new InsertStep(rewriteStatement, messageProcessor);
        executePlan.addExecuteStep(executeStep);

        List<Integer> paramList = new ArrayList<Integer>();
        for (int i = 0; i < stmt.documentValue.size(); i++) {
            paramList.add(i);
        }
        executePlan.getParamBindingMap().put(stmt.documentValue, paramList);
        return executePlan;
    }

    /**
     * ����ɾ������ִ�мƻ�
     * 
     * @param stmt
     *            ɾ��sql
     * @return ��Ӧ��ִ�мƻ�
     */
    public ExecutePlan analyze(DeleteStatement stmt) throws SqlExecuteException,
                                                    IllegalSqlException {
        DeleteStatement rewriteStatement = rewrite(stmt);
        ExecutePlan executePlan = new ExecutePlan();
        ExecuteStep executeStep;

        analyze(rewriteStatement.hashWhere, executePlan);
        analyze(rewriteStatement.filterWhere, executePlan);

        executeStep = new DeleteStep(rewriteStatement, messageProcessor);
        executePlan.addExecuteStep(executeStep);

        return executePlan;
    }

	/**
	 * generate export statement execute plan.
	 */
	public ExecutePlan analyze(ExportStatement stmt)
		throws SqlExecuteException, IllegalSqlException {
		ExportStatement rewriteStmt = rewrite(stmt);
		ExecutePlan plan = new ExecutePlan();

		analyze(stmt.path, plan);
		analyze(rewriteStmt.hashWhere, plan);
		analyze(rewriteStmt.filterWhere, plan);

		ExecuteStep step = new ExportStep(rewriteStmt, messageProcessor);
		plan.addExecuteStep(step);

		return plan;
	}

    /**
     * ���ɸ�������ִ�мƻ�
     * 
     * @param stmt
     *            ����sql
     * @return ��Ӧ��ִ�мƻ�
     */
    public ExecutePlan analyze(UpdateStatement stmt) throws SqlExecuteException,
                                                    IllegalSqlException {
        UpdateStatement rewriteStatement = rewrite(stmt);
        ExecutePlan executePlan = new ExecutePlan();
        ExecuteStep executeStep;

        analyze(rewriteStatement.hashWhere, executePlan);
        analyze(rewriteStatement.filterWhere, executePlan);

        executeStep = new UpdateStep(rewriteStatement, messageProcessor);
        executePlan.addExecuteStep(executeStep);

        List<Integer> paramList = new ArrayList<Integer>();
        paramList.add(0);
        executePlan.getParamBindingMap().put(stmt.updateValue, paramList);

        return executePlan;
    }

    public ExecutePlan analyze(GetkvStatement stmt) throws SqlExecuteException, IllegalSqlException {
        GetkvStatement rewriteStatement = rewrite(stmt);
        ExecutePlan executePlan = new ExecutePlan();
        ExecuteStep executeStep;

        executeStep = new GetKVStep(rewriteStatement, messageProcessor);
        executePlan.addExecuteStep(executeStep);

        executeStep = new OutputStep(stmt.selectFields, rewriteStatement.selectFields, 0, 1);
        executePlan.addExecuteStep(executeStep);

        return executePlan;
    }

    /**
     * ���ɲ�ѯ����ִ�мƻ�
     * 
     * @param stmt
     *            ��ѯsql
     * @return ��Ӧ��ִ�мƻ�
     */
    public ExecutePlan analyze(QueryStatement stmt) throws SqlExecuteException, IllegalSqlException {
        if (null != stmt.dochint) {
            //����docid��ȡԭʼֵ
            GetkvStatement getkvStatement = new GetkvStatement();
            getkvStatement.dochint = stmt.dochint;
            getkvStatement.tableName = stmt.tableName;
            getkvStatement.selectFields = stmt.selectFields;
            return analyze(getkvStatement);
        }

        QueryStatement rewriteStatement = rewrite(stmt);
        ExecutePlan executePlan = new ExecutePlan();
        ExecuteStep executeStep;

        // analyze within expression
        for (FieldDesc desc : rewriteStatement.selectFields) {
            if (null != desc.withinExpr)
                analyze(desc.withinExpr, executePlan);
        }

        analyze(rewriteStatement.hashWhere, executePlan);
        analyze(rewriteStatement.filterWhere, executePlan);
        analyze(rewriteStatement.having, executePlan);

        // ���ѡ���� , ��docserver��ȡ����
        executeStep = new SelectStep(rewriteStatement, messageProcessor);
        executePlan.addExecuteStep(executeStep);

        for (FieldDesc fieldDesc : rewriteStatement.selectFields) {
            // ��������ۺ���, ����group����, ���ڴ�group�Ĳ�ѯ, �����ڴ���docserverʱ���ܹ���limit
            if (fieldDesc.isComputeField() || rewriteStatement.groupByFields.size() > 0) {
                rewriteStatement.limit = 0;
                executeStep = new GroupStep(rewriteStatement.groupByFields,
                    rewriteStatement.selectFields);
                executePlan.addExecuteStep(executeStep);
                break;
            }
        }

        // �������having, ����having����
        if (null != rewriteStatement.having) {
            executeStep = new HavingStep(rewriteStatement.having, rewriteStatement.selectFields);
            executePlan.addExecuteStep(executeStep);
        }

        // �������orderby, ����������
        if (null != rewriteStatement.orderFields && rewriteStatement.orderFields.size() != 0) {
            executeStep = new OrderStep(rewriteStatement.orderFields, rewriteStatement.selectFields);
            executePlan.addExecuteStep(executeStep);
        }

        // �����������
        executeStep = new OutputStep(stmt.selectFields, rewriteStatement.selectFields, stmt.offset,
            stmt.limit);
        executePlan.addExecuteStep(executeStep);

        return executePlan;
    }

    /**
     * ���ɼ��������ѯ����ִ�мƻ�
     * 
     * @param stmt
     *            ���������ѯ���ʽ
     * @return ��Ӧ��ִ�мƻ�
     */
    public ExecutePlan analyze(SetOperatorExpression setExpression) throws SqlExecuteException,
                                                                   IllegalSqlException {

        ExecutePlan executePlan = new ExecutePlan();

        // ������������ִ�мƻ�
        ExecutePlan leftPlan = analyze(setExpression.getLeft());
        ExecuteStep leftBindStep = new ParamBindStep(setExpression.getLeft());
        leftPlan.addExecuteStep(leftBindStep);

        // ������������ִ�мƻ�
        ExecutePlan rightPlan = analyze(setExpression.getRight());
        ExecuteStep rightBindStep = new ParamBindStep(setExpression.getRight());
        rightPlan.addExecuteStep(rightBindStep);

        // ����������ִ�мƻ�����������ִ�мƻ��ϲ����ܵ�ִ�мƻ���
        executePlan.mergePlan(leftPlan);
        executePlan.mergePlan(rightPlan);
        SetOperateStep step = new SetOperateStep(setExpression);
        executePlan.addExecuteStep(step);
        return executePlan;
    }

}
