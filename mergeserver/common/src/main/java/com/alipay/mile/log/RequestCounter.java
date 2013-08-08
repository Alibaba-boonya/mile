/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * �̰߳�ȫ���࣬����ͳ��һ��ʱ���ڵķ��������������
 * 
 * @author yuzhong.zhao
 * @version $Id: RequestCounter.java, v 0.1 2012-5-23 ����02:47:06 yuzhong.zhao Exp $
 */
public class RequestCounter {
    private final ConcurrentHashMap<String, RequestDigest> requests;

    public RequestCounter() {
        requests = new ConcurrentHashMap<String, RequestDigest>();
    }

    /**
     * ͳ��һ������, �Գ�ʱ�ʹ����ʱ�䲻����ͳ��
     * 
     * @param source        ������Դ
     * @param time          �����ִ��ʱ��              
     * @param resultCode    0��ʾ�ɹ���1��ʾ��ʱ��-1��ʾ����
     */
    public void addRequest(String source, long time, int resultCode) {
        RequestDigest request = requests.get(source);
        int count = 0;
        int timeOutCount = 0;
        int errCount = 0;
        long totalExcTime = 0;

        if (null != request) {
            count = request.getCount();
            timeOutCount = request.getTimeOutCount();
            errCount = request.getErrCount();
            totalExcTime = request.getTotalExcTime();
        }

        if (resultCode == 0) {
            count++;
            totalExcTime += time;
        } else if (resultCode == 1) {
            timeOutCount++;
        } else {
            errCount++;
        }

        requests.put(source, new RequestDigest(count, timeOutCount, errCount, totalExcTime));
    }

    public void reset() {
        requests.clear();
    }
    
    
    public Map<String, RequestDigest> getRequests(){
        return this.requests;
    }

   
}
