/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.benchmark.util;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: FileLine.java, v 0.1 2012-11-12 ����04:59:51 yuzhong.zhao Exp $
 */
public class FileLine {
    /** �ļ��м�¼ */
    private String text;
    /** �ļ��� */
    private String file;
    /** �ļ��к� */
    private long lineNum;
    
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getFile() {
        return file;
    }
    public void setFile(String file) {
        this.file = file;
    }
    public void setLineNum(long lineNum) {
        this.lineNum = lineNum;
    }
    public long getLineNum() {
        return lineNum;
    }

    
    
}
