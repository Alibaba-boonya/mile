/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.alipay.mile.DocDigestData;
import com.alipay.mile.Record;
import com.alipay.mile.SqlResultSet;
import com.alipay.mile.message.AccessRsMessage;
import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.GetkvStatement;
import com.alipay.mile.util.ByteConveror;

/**
 * ͨ��������docid��docserver��ȡ��¼
 * 
 * @author yuzhong.zhao
 * @version $Id: GetKVStep.java,v 0.1 2011-5-19 07:46:10 yuzhong.zhao Exp $
 */
public class GetKVStep implements ExecuteStep {
	// getkv���
	private GetkvStatement getkvStatement;
	// ��Ϣ������
	private final MessageProcessor messageProcessor;

	/**
	 * Dochint����ʹ��
	 * 
	 * @param tableName
	 * @param selectColumns
	 * @param docIdList
	 * @param messageProcessor
	 */
	public GetKVStep(GetkvStatement getkvStatement, MessageProcessor messageProcessor) {
		this.getkvStatement = getkvStatement;
		this.messageProcessor = messageProcessor;
	}

	/**
	 * ������ѯ�������
	 * 
	 * @param rsMessage
	 *            �������
	 * @param data
	 *            �����
	 * @throws IOException
	 */
	private void parseResultMessage(AccessRsMessage rsMessage,
			Collection<Record> data) throws IOException {

		DataInput input = new DataInputStream(new ByteArrayInputStream(
				rsMessage.getValues()));
		for (int i = 0; i < rsMessage.getResultRows(); i++) {

			Record record = new Record();
			record.docid = rsMessage.getNodeId();
			record.docid = (record.docid << 48) + input.readLong();
			record.data = new ArrayList<Object>(getkvStatement.selectFields.size());

			for (int j = 0; j < getkvStatement.selectFields.size(); j++) {
				record.data.add(ByteConveror.getData(input));
			}

			data.add(record);
		}
	}

	/**
	 * ִ��getkv����
	 * 
	 * @param input
	 *            ����
	 * @param params
	 *            �����б�
	 * @param outPut
	 *            ��ʱʱ��
	 * @return ��װ���sql���������¼��ѯ���
	 * @throws SqlExecuteException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws IllegalSqlException 
	 * @throws Exception
	 * @see com.alipay.mile.plan.ExecuteStep#execute(java.lang.Object,
	 *      java.util.List, int)
	 */
	public Object execute(Object input, Map<Object, List<Object>> paramBindMap,
			int timeOut) throws SqlExecuteException, IOException,
			InterruptedException, ExecutionException, IllegalSqlException {
		// ������Ϣ��������Ϣ��ȡ�ý��
		AccessRsMessage rsMessage = (AccessRsMessage) messageProcessor
				.getKVMessage(getkvStatement, paramBindMap, timeOut);
		SqlResultSet result = new SqlResultSet();
		result.fields = getkvStatement.selectFields;
		result.data = new ArrayList<Record>();
		DocDigestData docDigestData = new DocDigestData();
		docDigestData.setNodeId(rsMessage.getNodeId());
		docDigestData.setRowCount(rsMessage.getResultRows());
		// ���ؽ������������ж�
		if (rsMessage.getValues() == null) {
			docDigestData.setSuccess(false);
		} else {
			parseResultMessage(rsMessage, result.data);
			docDigestData.setSuccess(true);
		}
		result.docState.add(docDigestData);
		return result;
	}

}
