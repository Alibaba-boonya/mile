/**
 * 
 */
package com.alipay.mile.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * @author xiaoju.luo
 * @version $Id: SR622190.java,v 0.1 2012-11-9 ����07:35:25 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("intersection&unions��ѯ")
public class SR622190 extends LevdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[7];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";
        params[5] = "rowkey3";
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            params[4] = "a" + String.valueOf(i);
            params[6] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                //	Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Assert.isFalse(true, "�����쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�����쳣");
        }
    }

    @Test
    @Subject("A intersection B��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622190() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST intersection select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST";
        Object[] params = new Object[4];
        params[0] = "rowkey3";
        params[1] = Long.valueOf(5);
        params[2] = "rowkey3";
        params[3] = Long.valueOf(7);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(2, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(1).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("A unions B��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622191() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST unions select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST";
        Object[] params = new Object[5];
        params[0] = "rowkey3";
        params[1] = "rowkey3";
        params[2] = Long.valueOf(5);

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(10, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(9).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("A intersect B unions C��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622192() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST  intersection select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST unions select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST>? order by GMT_TEST";
        Object[] params = new Object[6];
        params[0] = "rowkey";
        params[1] = Long.valueOf(5);
        params[2] = "rowkey3";
        params[3] = Long.valueOf(7);
        params[4] = "rowkey3";
        params[5] = Long.valueOf(5);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(4, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(7), queryResult.getQueryResult().get(1).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(2).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(3).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ�������С");
    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey3";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ɾ���쳣");
        }
    }

}
