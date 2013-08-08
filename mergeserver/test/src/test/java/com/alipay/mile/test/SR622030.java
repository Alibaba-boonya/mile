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
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * update���и���
 * 
 * @author xiaoju.luo
 * @version $Id: SR621030.java,v 0.1 2012-11-9 ����06:11:01 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("���и���")
public class SR622030 extends LevdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {

        stepInfo("��������");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[6];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.3";
        params[4] = "rowkey";
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "����ʧ��");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("���и���,indexwhere&where")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622030() {

        stepInfo("���и���");
        String sqlUpdate = "update TEST_VELOCITY set GMT_TEST=? indexwhere ROWKEY=?";
        Object[] params1 = new Object[2];
        params1[0] = Long.valueOf(111);
        params1[1] = "rowkey";

        try {
            getApplationClientImpl().preUpdate(sqlUpdate, params1, timeOut);

        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ִ���쳣");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("���и��º��ѯ����");
        String sqlSelect = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params2 = new Object[1];
        params2[0] = "rowkey";
        MileQueryResult queryResult1 = null;
        try {
            queryResult1 = getApplationClientImpl().preQueryForList(sqlSelect, params2, timeOut);

        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ִ���쳣");
        }
        stepInfo("ȷ�ϸ��³ɹ�");
        Logger.infoText("���Ϊ" + queryResult1.getQueryResult().size());
        Assert.areEqual(Long.valueOf(111), queryResult1.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
    }

    //	@Test
    //	@Subject("���и����쳣��������filter�������ҷ�hash����")
    //	@Priority(PriorityLevel.HIGHEST)
    //	@Tester("xiaoju.luo")
    //	public void TC622031() {
    //
    //		stepInfo("���и��²���");
    //		String sqlUpdate = "update TEST_VELOCITY set TEST_ID=? indexwhere ROWKEY=?";
    //		Object[] params1 = new Object[2];
    //		params1[0] = "123459";
    //		params1[1] = "rowkey";
    //
    //		try {
    //			getApplationClientImpl().preUpdate(sqlUpdate, params1, timeOut);
    //
    //		} catch (Exception e) {
    //			Logger.info("�쳣" + e);
    //			// Assert.isTrue(false, "����ִ���쳣");
    //		}
    //
    //		stepInfo("���и��º��ѯ����");
    //		String sqlSelect = "select TEST_ID from TEST_VELOCITY indexwhere ROWKEY=? where TEST_ID=?";
    //		Object[] params2 = new Object[2];
    //		params2[0] = "rowkey";
    //		params2[1] = "123459";
    //		MileQueryResult queryResult1 = null;
    //		try {
    //			queryResult1 = getApplationClientImpl().preQueryForList(sqlSelect,
    //					params2, timeOut);
    //
    //		} catch (Exception e) {
    //			Logger.info("���Ϊ" + e);
    //			Assert.isFalse(true, "����ִ���쳣");
    //		}
    //		// �ж����²��ɹ�
    //		Assert.areEqual(0, queryResult1.getQueryResult().size(), "���º�Ԥ�ڲ�ѯ���");
    //
    //	}

    @After
    public void tearDown() {
        MileDeleteResult deleteNumber = null;
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            deleteNumber = getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "��������ʧ��");
        }
        // Assert.areEqual(10, deleteNumber.getDeleteNum(), "Ԥ��ɾ������");
    }
}
