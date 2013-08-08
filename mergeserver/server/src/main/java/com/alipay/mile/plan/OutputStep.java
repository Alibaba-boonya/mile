/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alipay.mile.FieldDesc;
import com.alipay.mile.Record;
import com.alipay.mile.SqlResultSet;
import com.alipay.mile.mileexception.IllegalSqlException;
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * ������裬�Ѳ�ѯ�����ת���ɸ�client������ṹ
 * 
 * @author yuzhong.zhao
 * @version $Id: OutputStep.java,v 0.1 2011-5-19 04:37:37 yuzhong.zhao Exp $
 */
public class OutputStep implements ExecuteStep {
	// ѡ����
	private List<FieldDesc> selectFields;

	// ѡ�����ڽ�����е�λ��
	private List<Integer> selectIndex;
	
	// �������offset
	private int offset;

	// �Խ������С������
	private int limit;

	public OutputStep(List<FieldDesc> selectFields, List<FieldDesc> resultFields, int offset, int limit) throws IllegalSqlException {
		this.offset = offset;
		this.limit = limit;
		this.selectFields = selectFields;
		this.selectIndex = new ArrayList<Integer>();
		
		if(offset > limit){
		    throw new IllegalSqlException("�ڲ�ѯ��offsetӦ��С��limit!");
		}
		
		for (FieldDesc fieldDesc : selectFields) {
			int index = resultFields.indexOf(fieldDesc);
			if (index < 0) {
				throw new IllegalSqlException("�ڽ�������Ҳ�����" + fieldDesc);
			} else {
				selectIndex.add(index);
			}
		}
		
	}

	/**
	 * @param input
	 *            �����������ΪSqlResultSet
	 * @param params
	 *            Ԥ����sql�Ĳ����б�
	 * @return ����������ΪResultSet
	 * @throws SqlExecuteException
	 * @throws Exception
	 * @see com.alipay.mile.plan.ExecuteStep#execute(java.lang.Object,
	 *      java.util.List, int)
	 */
	public Object execute(Object input, Map<Object, List<Object>> paramBindMap,
			int timeOut) throws SqlExecuteException {
		// ����������
		if (null == input) {
			throw new SqlExecuteException("��ִ��output stepʱ����Ϊ��");
		}
		if (!(input instanceof SqlResultSet)) {
			throw new SqlExecuteException("��ִ��Output stepʱ���벻ΪSqlResultSet����");
		}
		SqlResultSet resultSet = (SqlResultSet) input;

		// ��mergeserver���в�ѯ�Ĺ����У����ѯһЩ������У���mergeserver�Ĳ�ѯ������ҳ�ʵ��ѡ�����
		SqlResultSet output = new SqlResultSet();
		output.fields = selectFields;
		output.docState = resultSet.docState;		

		// �������������������жϽ������С�Ƿ�offset������������ؿռ�¼����
		if (resultSet.data.size() < offset) {
			return output;
		} else {
			int up = limit < resultSet.data.size() ? limit : resultSet.data
					.size();
			if (resultSet.data instanceof List<?>) {
				// ����������List�ṹ�ģ���ô����ȡoffset
				List<Record> data = (List<Record>) resultSet.data;
				for (int i = offset; i < up; i++) {
					Record record = data.get(i);
					Record retRecord = new Record();
					retRecord.docid = record.docid;
					retRecord.data = new ArrayList<Object>();
					for (int j = 0; j < selectFields.size(); j++) {
						retRecord.data.add(record.data.get(selectIndex.get(j)));
					}
					output.data.add(retRecord);
				}
			} else {
				// ������������List�ṹ�ģ�����up��offsetȡǰup-offset����¼
				int i = up - offset;
				for (Record record : resultSet.data) {
					if (i <= 0) {
						break;
					}
					for (int j = 0; j < selectFields.size(); j++) {
						Record retRecord = new Record();
						retRecord.docid = record.docid;
						retRecord.data = new ArrayList<Object>();
						retRecord.data.add(record.data.get(selectIndex.get(j)));
						output.data.add(retRecord);
					}
					i--;
				}
			}

		}

		return output;
	}

}
