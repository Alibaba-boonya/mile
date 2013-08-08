package com.alipay.mile.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * sharding 3.4�������쳣����
 * ע�͵�����������ȥ�������������ú���Ҫ��
 * @author xiaoju.luo
 * @version $Id: SR621060.java,v 0.1 2012-11-5 ����07:05:16 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("sharding����")
public class SR621060 extends DocdbTestTools {
    int timeOut = 5000;

    // TEST_DAILY TEST_ID string 3:-3,-2 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12355,��35%2")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621060() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isTrue(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12355";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isTrue(true, "��ѯִ���쳣");
        }

        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // TEST_DAILY TEST_ID string 3 2 1,2
    @Test
    @Subject("sharding����sharding3���е�stringȡhashcodeȻ���2ȡģ,���ڽڵ�1��docNo2����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621061() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "�ȴ�ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12355";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // TEST_DAILY TEST_ID string 3 2 1,2
    @Test
    @Subject("sharding����sharding3,���е�stringȡhashcodeȻ���2ȡģ,���ڽڵ�0��docNo1����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621062() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12354";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Logger.info("�ȴ��쳣��" + e);
            Assert.isFalse(true, "ִ��ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12354";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // @Test
    // public void setUp(){
    // Long cTime=System.currentTimeMillis();
    // System.out.println("��ǰʱ�䣺"+cTime);
    // Long dTime=cTime-100000000;
    // System.out.println("���ʱ�䣺"+dTime);
    // }
    //	
    // TEST_DAILY GMT_TEST longlong 4 >-100000000 1,2
    @Test
    @Subject("sharding����sharding4����,GMT_TEST")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621063() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = 1363649761971L;
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Logger.info("�ȴ��쳣Ϊ��" + e);
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12355";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert
            .areEqual(1363649761971L, queryResult.getQueryResult().get(0).get("GMT_TEST"), "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // // TEST_DAILY GMT_TEST longlong 4 >-100000000 1,2
    // @Test
    // @Subject("sharding����sharding4�쳣,GMT_TEST=")
    // @Priority(PriorityLevel.HIGHEST)
    // @Tester("xiaoju.luo")
    // public void TC621064() {
    // stepInfo("��������");
    // try {
    // String sql =
    // "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
    // Object[] params = new Object[4];
    // params[0] = "12355";
    // params[1] = "milemac";
    // params[2] = "127.0.0.2";
    // for (int i = 0; i < 9; i++) {
    // params[3] = 1353550397609L;
    // MileInsertResult insertResult = getApplationClientImpl()
    // .preInsert(sql, params, timeOut);
    // }
    // } catch (Exception e) {
    // Logger.info("�쳣��" + e);
    // Assert.areEqual(true,
    // e.getMessage().contains("��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"),
    // "Ԥ���쳣");
    // }
    // }
}
