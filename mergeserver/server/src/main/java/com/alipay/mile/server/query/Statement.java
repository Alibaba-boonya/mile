/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * sql �������
 * @author jin.qian
 * @version $Id: Statement.java, v 0.1 2011-5-10 ����05:43:30 jin.qian Exp $
 */
public abstract class Statement {

    /** ����  */
    public String tableName;

    /** ��sql��估�󶨱���д�뵽�������  */
    public abstract void writeToStream(DataOutput os, Map<Object, List<Object>> paramBindMap) throws IOException;
}
