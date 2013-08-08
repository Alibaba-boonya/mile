/**
 * 
 */
package com.alipay.mile.test;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * �������ܲ��Ե�����
 * 
 * @author xiaoju.luo
 * @version $Id: SR621110.java,v 0.1 2012-11-7 ����10:27:04 xiaoju.luo Exp $
 */

public class SR621110 extends DocdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("ִ��Ԥ�������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[4];
        params[0] = "abce";
        params[1] = "ctumile";
        params[2] = "127.0.0.1";
        params[3] = Long.valueOf(6);
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "ִ���쳣");
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("count(*)")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621110() {
        stepInfo("ִ��count(*)��ѯ");
        String sql = "select count(*) as a from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params = new Object[1];
        params[0] = "127.0.0.1";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Logger.error("�쳣Ϊ��" + e);
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("a"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("����+count(*),sum+����+group by������ͳ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621111() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_IP,count(*) as a,min(GMT_TEST) as b from TEST_DAILY indexwhere TEST_IP=? group by TEST_IP";

        Object[] params = new Object[1];
        params[0] = "127.0.0.1";
        MileQueryResult queryResult = null;
        try {
            long startTime = System.currentTimeMillis();
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
            long endTime = System.currentTimeMillis();
            long spend = endTime - startTime;
            Logger.info("����ʱ��Ϊ��" + spend + "ms");
        } catch (Exception e) {
            Logger.error("�쳣Ϊ��" + e);
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("ִ�в�ѯ����ж�");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("a"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("b"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("count(*)���� withount indexwhere")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621112() {
        String sql = "select count(*) as v1 from TEST_DAILY";
        Object[] params = null;

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(1, result.size(), "Ԥ�ڽ������С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("v1"), "Ԥ�ڽ��");

    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_DAILY indexwhere TEST_IP=?";
            String[] params = new String[1];
            params[0] = "127.0.0.1";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }
    }
}
