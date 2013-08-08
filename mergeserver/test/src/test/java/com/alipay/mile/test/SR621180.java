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
 * @version $Id: SR621180.java,v 0.1 2012-11-7 ����07:27:05 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("inǶ�� &!= ��ѯ")
public class SR621180 extends DocdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=?";
        Object[] params = new Object[5];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult = null;
            params[3] = Long.valueOf(i);
            params[4] = "a" + String.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);

                // Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Assert.isFalse(true, "�����쳣");
            }

            Assert.areEqual(true, insertResult.isSuccessful(), "Ԥ�ڲ������ɹ�");
            int a = insertResult.hashCode();
            System.out.println("ww" + a);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�����쳣");
        }
    }

    @Test
    @Subject("in����ֵ) ��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621180() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, 11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST in (select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? )";
        String[] params = new String[2];
        params[0] = "127.0.0.1";
        params[1] = "127.0.0.9";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(0, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("in��null), abcΪnull ��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621181() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, 11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST in (select abc from TEST_DAILY indexwhere TEST_IP=?)";
        String[] params = new String[2];
        params[0] = "127.0.0.1";
        params[1] = "127.0.0.1";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(0, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("����in(?,?) ��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621182() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST in (?,?) order by GMT_TEST";
        Object[] params = new Object[3];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(2);
        params[2] = Long.valueOf(8);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(2, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(1).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("���in������or&and��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621183() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST,11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST in (?,?) or 11 in(?,?) order by GMT_TEST";
        Object[] params = new Object[5];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(2);
        params[2] = Long.valueOf(8);
        params[3] = "a1";
        params[4] = "a9";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(4, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(1), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual("a1", queryResult.getQueryResult().get(0).get("11"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(1).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual("a2", queryResult.getQueryResult().get(1).get("11"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(2).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual("a8", queryResult.getQueryResult().get(2).get("11"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(3).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual("a9", queryResult.getQueryResult().get(3).get("11"), "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("in(Ƕ����������)��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621184() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST,11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST in(select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST=? )";
        Object[] params = new Object[3];
        params[0] = "127.0.0.1";
        params[1] = "127.0.0.1";
        params[2] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(3), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");

    }

    @Test
    @Subject("��ֵ�е�!=��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621185() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST,11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST !=? order by GMT_TEST";
        Object[] params = new Object[2];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(4);

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(5), queryResult.getQueryResult().get(4).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ַ�����!=��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621186() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST,11 from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST !=? and 11 !=? order by GMT_TEST";
        Object[] params = new Object[3];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(4);
        params[2] = "a6";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(8, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(1), queryResult.getQueryResult().get(1).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(2).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(3), queryResult.getQueryResult().get(3).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(5), queryResult.getQueryResult().get(4).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(7), queryResult.getQueryResult().get(5).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(6).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(7).get("GMT_TEST"),
            "Ԥ�ڲ�ѯ���");

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
            Assert.isFalse(true, "ɾ���쳣");
        }
    }
}
