/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alipay.mile.util.SimpleThreadFactory;

/**
 * ժҪ��־���������ʱ��ӡ����ժҪ��־
 * 
 * @author yuzhong.zhao
 * @version $Id: DigestLogUtil.java, v 0.1 2012-5-23 ����08:24:44 yuzhong.zhao Exp $
 */
public class DigestLogUtil {

    /** ��־��ӡ������ */
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1,
                                                         new SimpleThreadFactory(
                                                             "DigestLogPrintSchedule", true));

    /**
     * ע����־��ʱ��ӡ����
     * 
     * @param command
     * @param initialDelay
     * @param delay
     * @param unit
     */
    public synchronized static void registDigestTask(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        SCHEDULER.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
