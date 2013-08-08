package com.alipay.mile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DocDigestData {
    //docserver�Ľ����
    private int     nodeId;
    //ִ��ʱ��
    private long    excTime;
    //docserver�Ƿ�ִ�гɹ�
    private boolean success;
    //docserver���صļ�¼����
    private long    rowCount;

    public void printClientDigestLog(StringBuffer sb) {
        sb.append(excTime).append(",").append(rowCount);
    }

    public void writeToStream(DataOutput os) throws IOException {
        os.writeInt(nodeId);
        os.writeLong(excTime);
        os.writeBoolean(success);
        os.writeLong(rowCount);
    }

    public void readFromStream(DataInput is) throws IOException {
        this.nodeId = is.readInt();
        this.excTime = is.readLong();
        this.success = is.readBoolean();
        this.rowCount = is.readLong();
    }

    public long getExcTime() {
        return excTime;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setExcTime(long excTime) {
        this.excTime = excTime;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
