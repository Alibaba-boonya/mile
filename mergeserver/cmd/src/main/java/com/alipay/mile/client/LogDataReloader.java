/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alipay.mile.message.TypeCode;
import com.alipay.mile.util.ByteConveror;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: LogDataReloader.java, v 0.1 2011-11-17 ����02:31:06 yuzhong.zhao Exp $
 */
public class LogDataReloader {
    private static final Logger         LOGGER             = Logger.getLogger("LOGDATA-RELOAD");

    /** ���ڲ�ѯ�Ͳ����client��  */
    private ApplationClientImpl         applationClientImpl;

    /** ���ݼ�������ÿ�ű��Ӧһ�����ݼ�����  */
    private Map<String, RecordReloader> recordReloaders    = new HashMap<String, RecordReloader>();

    /** ����sql��ģʽ  */
    private static final Pattern        INSERT_SQL_PAT       = Pattern
                                                               .compile("insert\\s+into\\s+((\\w|_)+)\\s+");

    /** �����е�ģʽ  */
    private static final Pattern        INSERT_FIELD_PAT     = Pattern
                                                               .compile("\\s+((\\w|_)+)\\s*=\\s*\\?");

    /** �������ͷ��ģʽ */
    private static final Pattern        INSERT_PARAM_HEAD_PAT = Pattern.compile("#params#");

    /** ���������ģʽ */
    private static final Pattern        INSERT_PARAM_PAT     = Pattern.compile("(\\w+):(.*)");

    public LogDataReloader(ApplationClientImpl applationClientImpl) {
        this.applationClientImpl = applationClientImpl;
    }

    /**
     * ע��һ����¼��¼��
     * 
     * @param tableName             ����
     * @param hashIndexes           hash������
     * @param filterIndexes         filter������
     */
    public void registRecordLoader(String tableName, String[] hashIndexes, String[] filterIndexes) {
        RecordReloader recordReloader = new RecordReloader();
        recordReloader.setApplationClientImpl(applationClientImpl);
        recordReloader.setTableName(tableName);
        recordReloader.setHashIndexes(hashIndexes);
        recordReloader.setFilterIndexes(filterIndexes);
        recordReloaders.put(tableName, recordReloader);
    }

    /**
     * ����һ����־
     * 
     * @param logLine       һ����־
     */
    private void parseLogLine(String logLine) {
        String tableName;
        List<String> fieldList = new ArrayList<String>();
        List<Object> paramList = new ArrayList<Object>();

        Matcher matcher = INSERT_SQL_PAT.matcher(logLine);
        if (matcher.find()) {
            tableName = matcher.group(1);
            String insertSql = logLine.substring(matcher.start());

            Matcher fieldMatcher = INSERT_FIELD_PAT.matcher(logLine);
            while (fieldMatcher.find()) {
                fieldList.add(fieldMatcher.group(1));
            }

            Matcher paramHeadMatcher = INSERT_PARAM_HEAD_PAT.matcher(logLine);
            if (paramHeadMatcher.find()) {
                String paramValues = logLine.substring(paramHeadMatcher.end());
                String[] paramArray = paramValues.split("#col#");

                for (int i = 1; i < paramArray.length; i++) {
                    if (StringUtils.isBlank(paramArray[i])) {
                        paramList.add(null);
                    } else {
                        Matcher paramMatcher = INSERT_PARAM_PAT.matcher(paramArray[i]);
                        if (paramMatcher.find()) {
                            paramList.add(ByteConveror.preString2value(TypeCode
                                .getTCByName(paramMatcher.group(1)), paramMatcher.group(2)));
                        }
                    }
                }

            }

            if (fieldList.size() == paramList.size()) {
                Map<String, Object> sqlMap = new HashMap<String, Object>();
                for (int i = 0; i < fieldList.size(); i++) {
                    sqlMap.put(fieldList.get(i), paramList.get(i));
                }

                RecordReloader recordReloader = recordReloaders.get(tableName);
                int result = recordReloader.loadData(sqlMap);
                if (result == 1) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("���ݲ�¼�ɹ���" + insertSql);
                    }
                } else if (result == 0) {
                    LOGGER.warn("�����Ѿ����ڣ�" + insertSql);
                } else {
                    LOGGER.error("���ݲ�¼ʧ�ܣ�" + insertSql);
                }
            }
        }

    }

    /**
     * ������־�ļ����������ݲ�¼
     * 
     * @param logFileNames      ��־�ļ��������ܻ��ж��
     */
    public void parseLogFile(List<String> logFileNames) {
        FileReader fileReader = null;
        BufferedReader bufReader = null;

        for (String fileName : logFileNames) {
            fileReader = null;
            bufReader = null;
            try {
                fileReader = new FileReader(fileName);
                bufReader = new BufferedReader(fileReader);

                String logLine = bufReader.readLine();
                while (logLine != null) {
                    parseLogLine(logLine);
                    logLine = bufReader.readLine();
                }

                if (bufReader != null) {
                    bufReader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (Exception e) {
                LOGGER.error("����־�ļ�" + fileName + "�в�ȫ����ʱ����, ", e);
            }
        }

    }

    public void setApplationClientImpl(ApplationClientImpl applationClientImpl) {
        this.applationClientImpl = applationClientImpl;
    }

    public ApplationClientImpl getApplationClientImpl() {
        return applationClientImpl;
    }

}
