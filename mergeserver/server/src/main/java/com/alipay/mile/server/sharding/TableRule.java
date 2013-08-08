/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.sharding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * �������ݷ�װ��
 * @author jin.qian
 * @version $Id: TableRule.java, v 0.1 2011-5-10 ����03:22:36 jin.qian Exp $
 */
public class TableRule {
    //���е�sharding����
    private Map<String, List<ShardingRule>> columnRules = new HashMap<String, List<ShardingRule>>();

    public Map<String, List<ShardingRule>> getColumnRules() {
        return columnRules;
    }

    public void setColumnRules(Map<String, List<ShardingRule>> columnRules) {
        this.columnRules = columnRules;
    }
}
