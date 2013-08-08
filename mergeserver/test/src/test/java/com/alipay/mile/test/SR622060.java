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
 * @version $Id: SR622060.java,v 0.1 2012-11-9 ����03:18:45 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("sharding����2")
public class SR622060 extends LevdbTestTools {
    int timeOut = 5000;

    // CTU_EVENT_DAILY TEST_ID string 3:-3,-2 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12355,��35%2,���ڽڵ�1��docN2��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622060() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
                params[5] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            Assert.isTrue(true, "����ʧ��");
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Logger.info("�ȴ��쳣��" + e);
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12355";

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
            Assert.isTrue(true, "��ѯִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    // CTU_EVENT_DAILY TEST_ID string 3 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12355,��35%2,���ڽڵ�1��docN2��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622061() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
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
            Logger.info("�쳣" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12355";

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

    // CTU_EVENT_DAILY TEST_ID string 3 2 1,2
    @Test
    @Subject("sharding����sharding3����,TEST_ID=12354,��35%2,���ڽڵ�0��docN1��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622062() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12354";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = Long.valueOf(6);
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
            Logger.info("�쳣" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12354";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
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
            Assert.isFalse(true, "ɾ��ִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");

    }

    //	@Test
    //	public void setUp() {
    //		Long cTime = System.currentTimeMillis();
    //		System.out.println("��ǰʱ�䣺" + cTime);
    //		Long dTime = cTime - 100000000;
    //		System.out.println("���ʱ�䣺" + dTime);
    //	}
    //
    // CTU_EVENT_DAILY GMT_TEST longlong 4 >-100000000 1,2,�����޶�
    @Test
    @Subject("sharding����sharding4����,GMT_TEST")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622063() {
        stepInfo("��������");
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12355";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[4] = "rowkey";
            for (int i = 0; i < 9; i++) {
                params[3] = 1363559763923L;
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
            Logger.info("�쳣" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = "12355";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(9, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
        Assert
            .areEqual(1363559763923L, queryResult.getQueryResult().get(0).get("GMT_TEST"), "Ԥ�ڽ��");

        stepInfo("ִ���������");
        String deleteSql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params3 = new Object[1];
        params3[0] = "rowkey";
        MileDeleteResult deleteResult = null;
        try {
            deleteResult = getApplationClientImpl().preDelete(deleteSql, params3, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ɾ��ִ���쳣");
        }
        Assert.areEqual(9, deleteResult.getDeleteNum(), "��������Ԥ������");
    }

    // CTU_EVENT_DAILY GMT_TEST longlong 4 >-100000000 1,2, �����޶�
    //	@Test
    //	@Subject("sharding����sharding4�쳣,GMT_TEST=")
    //	@Priority(PriorityLevel.HIGHEST)
    //	@Tester("xiaoju.luo")
    //	public void TC622064() {
    //		stepInfo("��������");
    //		try {
    //			String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
    //			Object[] params = new Object[6];
    //			params[0] = "12355";
    //			params[1] = "milemac";
    //			params[2] = "127.0.0.2";
    //			params[4] = "rowkey";
    //			for (int i = 0; i < 9; i++) {
    //				params[3] = 1343559853389L;
    //				params[5] = Long.valueOf(i);
    //				MileInsertResult insertResult = getApplationClientImpl()
    //						.preInsert(sql, params, timeOut);
    //				Logger.info("docid: " + insertResult.getDocId());
    //			}
    //		} catch (Exception e) {
    //			Logger.info("�쳣Ϊ��" + e);
    //			Assert.areEqual(true, e.getMessage().contains(
    //					"��ִ�в�������ʱsharding����û��ƥ��sharding����Ľڵ�!"), "Ԥ���쳣");
    //		}
    //	}

}
