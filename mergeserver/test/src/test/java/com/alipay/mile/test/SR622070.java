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
 * @version $Id: SR622070.java,v 0.1 2012-11-9 ����03:44:06 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("�ַ����еľۺϺ���ͳ��")
public class SR622070 extends LevdbTestTools {
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
        for (int i = 0; i < 10; i++) {
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            params[6] = "test" + i;
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
    @Subject("�ۺϺ����������Ҵ�����,ͳ���ַ�����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622070() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[1];
        params[0] = "rowkey";
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
    public void TC622071() {
        stepInfo("ִ�в�ѯ");
        String sql = "select squaresum(ABC) as B from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
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
    public void TC622072() {
        stepInfo("ִ�в�ѯ");
        String sql = "select stddev(ABC) as C from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
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
    @Subject("�ۺϺ���,ƽ�������б�����ѯ,ͳ���к��ı�")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622073() {
        stepInfo("ִ�в�ѯ");
        String sql = "select avg(ABC) as D from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.NaN, queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");

    }

    @Test
    @Subject("�ۺϺ�����count�Ҵ�����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622074() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
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
    public void TC622075() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
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
    public void TC622076() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual("test9", queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����min�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622077() {
        stepInfo("ִ�в�ѯ");
        String sql = "select min(ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual("test0", queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
    }

    @Test
    @Subject("�ۺϺ�����sum�Ҵ�����,ͳ���к��б�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622078() {
        stepInfo("ִ�в�ѯ");
        String sql = "select sum(ABC) as A from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
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
    @Subject("�ۺϺ���,���оۺϺ���֧�����б�����ѯ,ͳ��null")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622079() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(ABC) as A,squaresum(ABC) as B,stddev(ABC) as C,avg(ABC) as D,sum(ABC) as E,min(ABC) as F, max(ABC) as G,count(ABC) as H, count(*) as I,count(distinct ABC) as J from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
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
        Assert.areEqual("test0", queryResult.getQueryResult().get(0).get("F"), "Ԥ�ڽ��");
        Assert.areEqual("test9", queryResult.getQueryResult().get(0).get("G"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("H"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("I"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("J"), "Ԥ�ڽ��");

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
