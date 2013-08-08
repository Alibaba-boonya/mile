/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.client;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alipay.mile.Config;

/**
 * ��ctu�Ĵ�����־�в�ȫ���ݣ���ȫ֮ǰ��Ҫ�Լ�¼���в�ѯ�����mile��û�ж�Ӧ�����ݣ�����в�ȫ�����򲻽��в�ȫ
 * 
 * @author yuzhong.zhao
 * @version $Id: LoadCtuDataFromLog.java, v 0.1 2011-11-14 ����02:42:51 yuzhong.zhao Exp $
 */
public class LoadCtuDataFromLog {
    private static final Logger LOGGER = Logger.getLogger("LOGDATA-RELOAD");

    public static final String  LOG4J  = "log4j";

    public static final String  CONFIG = "config";

    public static final String  HELP   = "help";

    /**
     * @param args
     * ����̨�������
     */
    public static void main(String[] args) {
        String filePath;
        BasicConfigurator.configure();
        try {
            OptionParser parser = new OptionParser();
            parser.accepts(LOG4J, "log4j file").withRequiredArg().ofType(String.class);
            parser.accepts(CONFIG, "config file").withRequiredArg().ofType(String.class);
            parser.accepts(HELP);

            OptionSet options = parser.parse(args);
            if (options.has(HELP)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Usage, args: [options] log_file [log_file...]");
                }
                parser.printHelpOn(System.out);
                System.exit(0);
            }

            if (options.has(LOG4J)) {
                PropertyConfigurator.configure((String) options.valueOf(LOG4J));
            } else {
                PropertyConfigurator.configure("etc" + File.separator
                                               + Config.CONFIG_DEFAULT_MILE_LOG_PROPERTIES);
            }

            if (options.has(CONFIG)) {
                filePath = (String) options.valueOf(CONFIG);
            } else {
                filePath = System.getProperty("user.dir") + File.separator + "etc" + File.separator
                           + "mileCliClent.properties.prod";
            }

            ApplationClientImpl applationClientImpl = new ApplationClientImpl();
            applationClientImpl.readProperties(filePath);
            applationClientImpl.init();

            LogDataReloader logDataReloader = new LogDataReloader(applationClientImpl);
            String[] dailyHashIndex = new String[] { "c1", "c2", "c3",
                    "c4" };
            String[] dailyFilterIndex = new String[] { "c5", "c6"};
            String[] riskHashIndex = new String[] {"c1", "c2", "c3",
                    "c4"  };
            String[] riskFilterIndex = new String[] { "c5", "c6"};

            logDataReloader.registRecordLoader("table1", dailyHashIndex, dailyFilterIndex);
            logDataReloader.registRecordLoader("table2", riskHashIndex, riskFilterIndex);

            logDataReloader.parseLogFile(options.nonOptionArguments());
        } catch (Exception e) {
            LOGGER.error("�ָ�����ʱ�����쳣,", e);
        }
        System.exit(0);
    }

}
