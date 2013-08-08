/**
 * 
 */
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
 * docdb��sharding,ע�͵���������ȥ�������������ú�����Ҫ�õ�
 * 
 * @author xiaoju.luo
 * @version $Id: SR621050.java,v 0.1 2012-11-5 ����05:27:12 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("shariding��������")
public class SR621050 extends DocdbTestTools {
    int timeOut = 5000;

    @Test
    @Subject("sharding����sharding1����: GMT_TEST >20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621050() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(30);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
            }
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? ";
        Object[] params = new Object[1];
        params[0] = "127.0.0.2";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";

        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(30), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // @Test
    // @Subject("sharding����sharding1�쳣 GMT_TEST=20")
    // @Priority(PriorityLevel.HIGHEST)
    // @Tester("xiaoju.luo")
    // public void TC621051() {
    // stepInfo("��������");
    // try {
    // String sql =
    // "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
    // Object[] params = new Object[4];
    // params[0] = "12345";
    // params[1] = "milemac";
    // params[2] = "127.0.0.2";
    // for (int i = 0; i < 9; i++) {
    // params[3] = Long.valueOf(20);
    // MileInsertResult insertResult = getApplationClientImpl()
    // .preInsert(sql, params, timeOut);
    // // Logger.info("docid: " + insertResult.getDocId());
    // }
    // } catch (Exception e) {
    // // �����쳣����ȡ�쳣������Ԥ���ж�
    // Logger.info("TC621051�����쳣��" + e);
    // Assert.areEqual(true, e.getMessage().contains(
    // "��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    // //Assert.isFalse(true, "����ʧ��");
    // }
    //
    // }

    // @Test
    // @Subject("sharding����sharding1�쳣 GMT_TEST<20")
    // @Priority(PriorityLevel.HIGHEST)
    // @Tester("xiaoju.luo")
    // public void TC621052() {
    // stepInfo("��������");
    // try {
    // String sql =
    // "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
    // Object[] params = new Object[4];
    // params[0] = "12345";
    // params[1] = "milemac";
    // params[2] = "127.0.0.2";
    // for (int i = 0; i < 9; i++) {
    // params[3] = Long.valueOf(10);
    // MileInsertResult insertResult = getApplationClientImpl()
    // .preInsert(sql, params, timeOut);
    // // Logger.info("docid: " + insertResult.getDocId());
    // }
    // } catch (Exception e) {
    // // �����쳣����ȡ�쳣������Ԥ���ж�
    // Logger.info("TC621052�����쳣��" + e);
    // Assert.areEqual(true, e.getMessage().contains(
    // "��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    // }
    //
    // }

    // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=10")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621053() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(10);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";

        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621054() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(20);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�ȴ�1��");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(20), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=10")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621055() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(10);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�ȴ�1��");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,10С��GMT_TESTС��20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621056() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(15);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�ȴ�1��");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(15), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    // @Test
    // @Subject("sharding����sharding2�쳣,GMT_TEST>20")
    // @Priority(PriorityLevel.HIGHEST)
    // @Tester("xiaoju.luo")
    // public void TC621057() {
    // stepInfo("��������");
    // try {
    // String sql =
    // "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
    // Object[] params = new Object[4];
    // params[0] = "12345";
    // params[1] = "milemac";
    // params[2] = "127.0.0.2";
    // for (int i = 0; i < 9; i++) {
    // params[3] = Long.valueOf(21);
    // MileInsertResult insertResult = getApplationClientImpl()
    // .preInsert(sql, params, timeOut);
    // }
    // } catch (Exception e) {
    //
    // // ץȡ�쳣��Ԥ���жϣ�����ʧ��
    // Logger.info("TC621057�쳣" + e);
    // Assert.areEqual(true, e.getMessage().contains(
    // "��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    // }
    // }

    // // sharding2: TEST_DAILY GMT_TEST longlong 2 [10,20] 1,2
    // @Test
    // @Subject("sharding����sharding2�쳣,GMT_TEST<10")
    // @Priority(PriorityLevel.HIGHEST)
    // @Tester("xiaoju.luo")
    // public void TC621058() {
    // stepInfo("��������");
    // try {
    // String sql =
    // "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
    // Object[] params = new Object[4];
    // params[0] = "12345";
    // params[1] = "milemac";
    // params[2] = "127.0.0.2";
    // for (int i = 0; i < 9; i++) {
    // params[3] = Long.valueOf(6);
    // MileInsertResult insertResult = getApplationClientImpl()
    // .preInsert(sql, params, timeOut);
    // Logger.info("docid: " + insertResult.getDocId());
    // }
    // } catch (Exception e) {
    //
    // // ץȡ�쳣��Ԥ���жϣ�����ʧ��
    // Assert.areEqual(true, e.getMessage().contains(
    // "��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    // // Assert.isFalse(true, "����ʧ��");
    // }
    // }

    // TEST_DAILY TEST_ID string 3:-3,-2 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12345,��34%2")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621059() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
            Object[] params = new Object[4];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�ȴ�1��");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");

        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.2";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params3 = new Object[1];
        params3[0] = "127.0.0.2";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }
}