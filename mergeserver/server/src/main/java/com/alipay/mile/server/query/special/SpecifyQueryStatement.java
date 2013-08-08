/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query.special;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alipay.mile.Expression;
import com.alipay.mile.server.query.Statement;
import com.alipay.mile.server.query.TimeHint;


/**
 * condition statement for specify query.
 * @author bin.lb
 *
 */
public class SpecifyQueryStatement extends Statement {
    /** table id*/
    public int                 tableId;
    /** segmentʱ����ˣ�������С��ѯ��segment����������� */
    public TimeHint            hint;
    /** ��Hash��BTree���������� */
    public Expression          hashWhere;
    /** ��filter���������� */
    public Expression          filterWhere;
    /**  sub select */
    public List<SubSelect>     subSelects;
    /** sub select desc */
    public List<SubSelectDesc> subSelectDesc = new ArrayList<SubSelectDesc>();
	
    @Override
	public void writeToStream(DataOutput os,
			Map<Object, List<Object>> paramBindMap) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
