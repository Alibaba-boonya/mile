/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alipay.mile.SqlResultSet;

/**
 * @author jin.qian
 * @version $Id: SqlExecuteRsMessage.java,v 0.1 2011-4-6 ����05:40:21 jin.qian Exp
 *          $
 */
public class SqlExecuteRsMessage extends AbstractMessage {

    /** ״̬ */
    private List<KeyValueData> stat         = new ArrayList<KeyValueData>();
    /** ����� */
    private SqlResultSet       sqlResultSet = new SqlResultSet();

    /**
     * ������
     */
    public SqlExecuteRsMessage() {
        super();
        setType(MT_MC_SQL_RS);
    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);

        // ����stat
        if (stat == null || stat.isEmpty()) {
            os.writeInt(0);

        } else {
            os.writeInt(stat.size());

            for (KeyValueData keyValueData : stat) {
                keyValueData.writeToStream(os);
            }
        }
        // ����sqlResultSet
        sqlResultSet.writeToStream(os);

    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#readFromStream(java.io.DataInput)
     */
    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);

        // ����columnMetas
        int j;
        // ����stat
        j = is.readInt();

        for (int i = 0; i < j; i++) {
            KeyValueData keyValueData = new KeyValueData();
            keyValueData.readFromStream(is);
            this.stat.add(keyValueData);
        }
        // ����values
        sqlResultSet.readFromStream(is);

    }

    public List<KeyValueData> getStat() {
        return stat;
    }

    public void setStat(List<KeyValueData> stat) {
        this.stat = stat;
    }

    public void setSqlResultSet(SqlResultSet sqlResultSet) {
        this.sqlResultSet = sqlResultSet;
    }

    public SqlResultSet getSqlResultSet() {
        return sqlResultSet;
    }

}
