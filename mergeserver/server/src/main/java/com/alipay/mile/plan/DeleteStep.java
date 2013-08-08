/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.alipay.mile.Constants;
import com.alipay.mile.DocDigestData;
import com.alipay.mile.FieldDesc;
import com.alipay.mile.Record;
import com.alipay.mile.SqlResultSet;
import com.alipay.mile.message.AccessRsMessage;
import com.alipay.mile.message.Message;
import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;
import com.alipay.mile.server.query.DeleteStatement;

/**
 * ɾ�����裬����ɾ����¼��Ŀ
 * 
 * @author yuzhong.zhao
 * @version $Id: DeleteStep.java,v 0.1 2011-5-20 04:24:19 yuzhong.zhao Exp $
 */
public class DeleteStep implements ExecuteStep {

	/** ɾ������Statement */
	private DeleteStatement deleteStatement;

	/** message���� */
	private MessageProcessor messageProcessor;

	/** ����������¼ɾ����¼�� */
	private FieldDesc deleteNumColumn;

	public DeleteStep(DeleteStatement deleteStatement,
			MessageProcessor messageProcessor) {
		this.deleteStatement = deleteStatement;
		this.messageProcessor = messageProcessor;
		this.deleteNumColumn = new FieldDesc();
		this.deleteNumColumn.fieldName = Constants.DELETE_RETURN_COLUMN_NAME;
		this.deleteNumColumn.aliseName = this.deleteNumColumn.fieldName;
	}

	/**
	 * ִ��deleteִ�мƻ�
	 * 
	 * @param sessionId
	 *            session��
	 * @param input
	 *            ����
	 * @param params
	 *            �����б�
	 * @param timeOut
	 *            ��ʱʱ��
	 * @return ��װ���sql���������¼ɾ����¼��
	 * @throws SqlExecuteException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws IllegalSqlException
	 * @see com.alipay.mile.plan.ExecuteStep#execute(java.lang.Object,
	 *      java.util.List, int)
	 */
	public Object execute(Object input, Map<Object, List<Object>> paramBindMap,
			int timeOut) throws SqlExecuteException, IOException,
			InterruptedException, ExecutionException, IllegalSqlException {

		// ����delete���
		List<Message> rsMessageList = messageProcessor.deleteMessage(
				deleteStatement, paramBindMap, timeOut);
		SqlResultSet result = new SqlResultSet();
		int deleteNum = 0;

		// ����Docserver���صĽ��
		for (Message message : rsMessageList) {
			AccessRsMessage rsMessage = (AccessRsMessage) message;

			DocDigestData docDigestData = new DocDigestData();
			docDigestData.setExcTime(rsMessage.getExcTime());
			docDigestData.setRowCount(rsMessage.getResultRows());
			docDigestData.setNodeId(rsMessage.getNodeId());
			if (rsMessage.getValues() == null) {
				docDigestData.setSuccess(false);
			} else {
				docDigestData.setSuccess(true);
				DataInput inputStream = new DataInputStream(
						new ByteArrayInputStream(rsMessage.getValues()));
				// ͳ��ɾ����¼��
				deleteNum += inputStream.readInt();
			}

			result.docState.add(docDigestData);
		}

		result.fields.add(deleteNumColumn);
		Record record = new Record();
		record.data.add(deleteNum);
		result.data.add(record);
		return result;
	}

	public DeleteStatement getDeleteStatement() {
		return deleteStatement;
	}

	public void setDeleteStatement(DeleteStatement deleteStatement) {
		this.deleteStatement = deleteStatement;
	}

	public MessageProcessor getMessageProcessor() {
		return messageProcessor;
	}

	public void setMessageProcessor(MessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}

	public FieldDesc getDeleteNumColumn() {
		return deleteNumColumn;
	}

	public void setDeleteNumColumn(FieldDesc deleteNumColumn) {
		this.deleteNumColumn = deleteNumColumn;
	}

}
