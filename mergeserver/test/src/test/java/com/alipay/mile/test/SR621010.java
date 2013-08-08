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
 * �ۺϺ���������string����ͳ�Ʋ����࣬���ڲ����޷�ͨ��
 * 
 * @author xiaoju.luo
 * @version $Id: SR621001.java,v 0.1 2012-10-31 ����08:44:24 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("�ַ���&���ֻ�ϾۺϺ���")
public class SR621010 extends DocdbTestTools {
    int timeOut = 2000;

    @Before
    public void SetUp() {
        MileInsertResult insertResult;

        stepInfo("��������");
        // һ����������
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[4];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";

        for (int i = 0; i < 10; i++) {
            params[3] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                // Logger.info("docid: " + insertResult.getDocId());

            } catch (Exception e) {
                Assert.isFalse(true, "���������쳣");
            }
        }

        // ͳ���е��ı�����
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[3] = "�ı�����";
        try {
            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�ȴ��쳣");
        }
    }

    @Test
    @Subject("�ۺϺ����������Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621010() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) as A from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����ƽ�������б�����ѯ��ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621011() {
        stepInfo("ִ�в�ѯ");
        String sql = "select squaresum(GMT_TEST) as B from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ���,��׼�����б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621012() {
        stepInfo("ִ�в�ѯ");
        String sql = "select stddev(GMT_TEST) as C from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("C"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ���,���оۺϺ���֧�����б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621014() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) as A,squaresum(GMT_TEST) as B,stddev(GMT_TEST) as C,avg(GMT_TEST) as D,sum(GMT_TEST) as E,min(GMT_TEST) as F, max(GMT_TEST) as G,count(GMT_TEST) as H, count(*) as I,count(distinct GMT_TEST) as J from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("C"), "Ԥ�ڽ��");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("E"), "Ԥ�ڽ��");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("F"), "Ԥ�ڽ��");
        Assert.areEqual(null, queryResult.getQueryResult().get(0).get("G"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(11), queryResult.getQueryResult().get(0).get("H"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(11), queryResult.getQueryResult().get(0).get("I"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(11), queryResult.getQueryResult().get(0).get("J"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ���,ƽ�������б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621013() {
        stepInfo("ִ�в�ѯ");
        String sql = "select avg(GMT_TEST) as D from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");

    }

    // �����ѯ�﷨��Ҫȷ��
    @Test
    @Subject("�ۺϺ���,���оۺϺ���֧�����б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621015() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST, variance(GMT_TEST) as A,squaresum(GMT_TEST) as B,stddev(GMT_TEST) as C,avg(GMT_TEST) as D,sum(GMT_TEST) as E,min(GMT_TEST) as F, max(GMT_TEST) as G,count(GMT_TEST) as H, count(*) as I,count(distinct GMT_TEST) as J from TEST_DAILY indexwhere TEST_IP=? group by GMT_TEST having sum(GMT_TEST)<? limit 20";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = Double.valueOf(1000);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.infoText("���" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        int sizeNumber = queryResult.getQueryResult().size();
        Assert.areEqual(10, sizeNumber, "Ԥ�ڽ������С");
        Logger.info("�����" + queryResult.getQueryResult());

    }

    // ��һ�����ٲ�ѯ���ı������
    @Test
    @Subject("�ۺϿ��ٺ���������֧�ֵľۺϺ����Ҵ�����,ͳ��������Ϲ���������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621016() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) within(GMT_TEST > ?) as A,squaresum(GMT_TEST) within(GMT_TEST>?) as B,stddev(GMT_TEST) within(GMT_TEST>=?) as C,avg(GMT_TEST) within(GMT_TEST>?) as D,sum(GMT_TEST) within(GMT_TEST>?)as E,min(GMT_TEST) within(GMT_TEST>?) as F, max(GMT_TEST) within(GMT_TEST>?) as G,count(GMT_TEST) within(GMT_TEST>?) as H, count(*) within(GMT_TEST>?) as I,count(distinct GMT_TEST) within(GMT_TEST>?) as J from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[12];
        params[0] = Long.valueOf(1);
        params[1] = Long.valueOf(1);
        params[2] = Long.valueOf(0);
        params[3] = Long.valueOf(1);
        params[4] = Long.valueOf(1);
        params[5] = Long.valueOf(1);
        params[6] = Long.valueOf(1);
        params[7] = Long.valueOf(1);
        params[8] = Long.valueOf(1);
        params[9] = Long.valueOf(1);
        params[10] = "127.0.0.2";
        params[11] = "12345";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(5.25), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(284), queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("C"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(5.5), queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(44), queryResult.getQueryResult().get(0).get("E"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(0).get("F"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("G"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("H"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("I"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����count*�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621017() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(*) as A from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Long.valueOf(11), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    // ��Ҫ����Ⱥȷ�ϣ����ı��Ĺ�������count(distinct)��ô�����
    @Test
    @Subject("�ۺϺ�����count(distinct)�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621018() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as A from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.2";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Long.valueOf(11), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @After
    public void tearDown() {
        try {
            stepInfo("��������");
            String sql = "delete from TEST_DAILY indexwhere TEST_IP=?";
            String[] params = new String[1];
            params[0] = "127.0.0.2";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "abdc");
        }
    }

}
