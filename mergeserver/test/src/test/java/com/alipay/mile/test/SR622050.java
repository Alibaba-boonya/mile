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
 * @author xiaoju.luo
 * @version $Id: SR622050.java,v 0.1 2012-11-9 ����02:29:10 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("sharding����")
public class SR622050 extends LevdbTestTools {
    /** ��ʱ */
    private int timeOut = 5000;

    @Test
    @Subject("sharding����sharding1����: GMT_TEST >20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622050() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(30);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(30), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params3 = new Object[2];
        params3[0] = "rowkey";
        params3[1] = "12345";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ɾ��ִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    //	@Test
    //	@Subject("sharding����sharding1�쳣 GMT_TEST=20")
    //	@Priority(PriorityLevel.HIGHEST)
    //	@Tester("xiaoju.luo")
    //	public void TC622051() {
    //		stepInfo("��������");
    //		try {
    //			String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
    //			Object[] params = new Object[6];
    //			params[0] = "12345";
    //			params[1] = "milemac";
    //			params[2] = "127.0.0.2";
    //			params[4] = "rowkey";
    //			for (int i = 0; i < 9; i++) {
    //				params[3] = Long.valueOf(20);
    //				params[5] = Long.valueOf(i);
    //				MileInsertResult insertResult = getApplationClientImpl()
    //						.preInsert(sql, params, timeOut);
    //				// Logger.info("docid: " + insertResult.getDocId());
    //			}
    //		} catch (Exception e) {
    //			// �����쳣����ȡ�쳣������Ԥ���ж�
    //			Logger.info("TC622051�쳣" + e);
    //			Assert.areEqual(true, e.getMessage().contains(
    //					"��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    //		}
    //
    //	}

    //	@Test
    //	@Subject("sharding����sharding1�쳣 GMT_TEST<20")
    //	@Priority(PriorityLevel.HIGHEST)
    //	@Tester("xiaoju.luo")
    //	public void TC622052() {
    //		stepInfo("��������");
    //		try {
    //			String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
    //			Object[] params = new Object[6];
    //			params[0] = "12345";
    //			params[1] = "milemac";
    //			params[2] = "127.0.0.2";
    //			params[4] = "rowkey";
    //			for (int i = 0; i < 9; i++) {
    //				params[3] = Long.valueOf(10);
    //				params[5] = Long.valueOf(i);
    //				MileInsertResult insertResult = getApplationClientImpl()
    //						.preInsert(sql, params, timeOut);
    //				// Logger.info("docid: " + insertResult.getDocId());
    //			}
    //		} catch (Exception e) {
    //			// �����쳣����ȡ�쳣������Ԥ���ж�
    //			Assert.areEqual(true, e.getMessage().contains(
    //					"��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    //		}
    //
    //	}

    // sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=10")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622053() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(10);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";

        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622054() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(20);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            }
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "����ʧ��");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(20), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,GMT_TEST=10")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622055() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(10);
                params[5] = Long.valueOf(i);
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
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    @Test
    @Subject("sharding����sharding2����,10<GMT_TEST<20")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622056() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(15);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Logger.info("docid: " + insertResult.getDocId());
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
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(15), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    ////	// sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    ////	@Test
    ////	@Subject("sharding����sharding2�쳣,GMT_TEST>20")
    ////	@Priority(PriorityLevel.HIGHEST)
    ////	@Tester("xiaoju.luo")
    ////	public void TC622057() {
    ////		stepInfo("��������");
    ////		try {
    ////			String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
    ////			Object[] params = new Object[6];
    ////			params[0] = "12345";
    ////			params[1] = "milemac";
    ////			params[2] = "127.0.0.2";
    ////			params[4] = "rowkey";
    ////			for (int i = 0; i < 9; i++) {
    ////				params[3] = Long.valueOf(21);
    ////				params[5] = Long.valueOf(i);
    ////				MileInsertResult insertResult = getApplationClientImpl()
    ////						.preInsert(sql, params, timeOut);
    ////				Logger.info("docid: " + insertResult.getDocId());
    ////			}
    ////		} catch (Exception e) {
    ////
    ////			// ץȡ�쳣��Ԥ���жϣ�����ʧ��
    ////			Logger.info("�쳣Ϊ��" + e);
    ////			Assert.areEqual(true, e.getMessage().contains(
    ////					"��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    ////		}
    ////	}
    ////
    ////	// sharding2: CTU_EVENT_DAILY GMT_TEST longlong 2 [10,20] 1,2
    ////	@Test
    ////	@Subject("sharding����sharding2�쳣,GMT_TEST<10")
    ////	@Priority(PriorityLevel.HIGHEST)
    ////	@Tester("xiaoju.luo")
    ////	public void TC622058() {
    ////		stepInfo("��������");
    ////		try {
    ////			String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
    ////			Object[] params = new Object[6];
    ////			params[0] = "12345";
    ////			params[1] = "milemac";
    ////			params[2] = "127.0.0.2";
    ////			params[4] = "rowkey";
    ////			for (int i = 0; i < 9; i++) {
    ////				params[3] = Long.valueOf(6);
    ////				params[5] = Long.valueOf(i);
    ////				MileInsertResult insertResult = getApplationClientImpl()
    ////						.preInsert(sql, params, timeOut);
    ////			}
    ////		} catch (Exception e) {
    ////			Logger.info("�쳣Ϊ��" + e);
    ////			// ץȡ�쳣��Ԥ���жϣ�����ʧ��
    ////			Assert.areEqual(true, e.getMessage().contains(
    ////					"��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    ////			// Assert.isFalse(true, "����ʧ��");
    ////		}
    ////	}

    // CTU_EVENT_DAILY TEST_ID string 3:-3,-2 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12345,��34%2")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622059() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
            }
            Thread.sleep(2000);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12345";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert.areEqual(Long.valueOf(6), queryResult.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }
}
