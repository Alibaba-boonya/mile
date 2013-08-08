/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * sql��ִ�в��裬ÿһ�������������һ��������
 * 
 * @author yuzhong.zhao
 * @version $Id: ExecuteStep.java,v 0.1 2011-5-15 06:53:25 yuzhong.zhao Exp $
 */

public interface ExecuteStep {

	/**
	 * ִ�в���
	 * 
	 * @param input
	 *            ִ�в��������
	 * @param params
	 *            �����б����ڴ���Ԥ����sql
	 * @param timeOut
	 *            SQLִ�г�ʱʱ��
	 * @return ִ�в�������
	 * @throws SqlExecuteException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IllegalSqlException
	 */
	Object execute(Object input, Map<Object, List<Object>> params, int timeOut)
			throws SqlExecuteException, IOException, InterruptedException,
			ExecutionException, IllegalSqlException;
}
