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
 * @version $Id: SR622130.java,v 0.1 2012-11-9 ����06:24:59 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("���������͵���ֵ�еľۺϲ�ѯ")
public class SR622130 extends LevdbTestTools {

    int timeOut = 5000;

    @Before
    public void SetUp() {
        MileInsertResult insertResult;
        stepInfo("��������");
        //һ����������
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=? ABC=?";
        Object[] params = new Object[7];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[4] = "rowkey";
        params[6] = null;
        for (int i = 0; i < 4; i++) {
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "���������쳣");
            }
        }

        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[4] = "rowkey";
        params[6] = null;
        for (int i = 4; i < 6; i++) {
            params[3] = Integer.valueOf(i);
            params[5] = Long.valueOf(i);

            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "���������쳣");
            }
        }

        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[4] = "rowkey";
        params[6] = null;
        for (int i = 6; i < 8; i++) {
            params[3] = Float.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "���������쳣");
            }
        }

        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[4] = "rowkey";
        params[6] = null;
        for (int i = 8; i < 10; i++) {
            params[3] = Double.valueOf(i);
            params[5] = Long.valueOf(i);

            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "���������쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�ȴ��쳣");
        }
    }

    @Test
    @Subject("�ۺϺ����������Ҵ�����,ͳ��null��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622130() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(8.25), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����ƽ�������б�����ѯ��ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622011() {
        stepInfo("ִ�в�ѯ");
        String sql = "select squaresum(GMT_TEST) as B from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(285), queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ���,��׼�����б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622012() {
        stepInfo("ִ�в�ѯ");
        String sql = "select stddev(GMT_TEST) as C from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("C"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ���,���оۺϺ���֧�����б�����ѯ,ͳ��null")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622014() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) as A,squaresum(GMT_TEST) as B,stddev(GMT_TEST) as C,avg(GMT_TEST) as D,sum(GMT_TEST) as E,min(GMT_TEST) as F, max(GMT_TEST) as G,count(GMT_TEST) as H, count(*) as I,count(distinct GMT_TEST) as J from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(8.25), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(285), queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("C"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(4.5), queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get("E"), "Ԥ�ڽ��");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("F"), "Ԥ�ڽ��");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("G"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("H"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("I"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("J"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ���,ƽ�������б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622013() {
        stepInfo("ִ�в�ѯ");
        String sql = "select avg(GMT_TEST) as D from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(4.5), queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ�����count�Ҵ�����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622015() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����count(distinct)�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622016() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����max�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622017() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����min�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622018() {
        stepInfo("ִ�в�ѯ");
        String sql = "select min(GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����sum�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622019() {
        stepInfo("ִ�в�ѯ");
        String sql = "select sum(GMT_TEST) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @After
    public void tearDown() {
        try {
            stepInfo("��������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "abdc");
        }
    }
}
