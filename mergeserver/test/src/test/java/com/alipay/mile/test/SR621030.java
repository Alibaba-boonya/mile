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
import com.alipay.mile.client.result.MileUpdateResult;

/**
 * update���и���
 * 
 * @author xiaoju.luo
 * @version $Id: SR621030.java,v 0.1 2012-11-2 ����06:11:01 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("���и���")
public class SR621030 extends DocdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {

        stepInfo("��������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[4];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.3";
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                // Logger.info("docid: " + insertResult.getDocId());

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
    public void TC621030() {

        stepInfo("���и���");
        String sqlUpdate = "update TEST_DAILY set GMT_TEST=? indexwhere TEST_IP=?";
        Object[] params1 = new Object[2];
        params1[0] = Long.valueOf(111);
        params1[1] = "127.0.0.3";

        try {
            getApplationClientImpl().preUpdate(sqlUpdate, params1, timeOut);
            Thread.sleep(1000);
        } catch (Exception e) {
            Assert.isFalse(true, "����ִ���쳣");
        }

        stepInfo("���и��º��ѯ����");
        String sqlSelect = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=? limit 1";
        Object[] params2 = new Object[1];
        params2[0] = "127.0.0.3";
        MileQueryResult queryResult1 = null;
        try {
            queryResult1 = getApplationClientImpl().preQueryForList(sqlSelect, params2, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "����ִ���쳣");
        }
        stepInfo("ȷ�ϸ��³ɹ�");
        Logger.infoText("���Ϊ" + queryResult1.getQueryResult());
        Assert.areEqual(Long.valueOf(111), queryResult1.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");
    }

    @Test
    @Subject("���и���,û��indexwhere&where")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621031() {

        stepInfo("���и��²���");
        String sqlUpdate = "update TEST_DAILY set GMT_TEST=?";
        Object[] params1 = new Object[1];
        params1[0] = Long.valueOf(222);

        MileUpdateResult updateResult = null;

        try {
            updateResult = getApplationClientImpl().preUpdate(sqlUpdate, params1, timeOut);
            Thread.sleep(1000);
        } catch (Exception e) {
            Assert.isFalse(true, "����ִ���쳣");
        }
        Assert.areEqual(true, updateResult.isSuccessful(), "Ԥ�ڽ��");

        stepInfo("���и��º��ѯ����");
        String sqlSelect = "select GMT_TEST from TEST_DAILY indexwhere TEST_IP=?";
        Object[] params2 = new Object[1];
        params2[0] = "127.0.0.3";
        MileQueryResult queryResult1 = null;
        try {
            queryResult1 = getApplationClientImpl().preQueryForList(sqlSelect, params2, timeOut);

        } catch (Exception e) {
            Logger.info("����TC621031�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ִ���쳣");
        }

        Assert.areEqual(10, queryResult1.getQueryResult().size(), "�������С�ж�");
        stepInfo("ȷ�ϸ��³ɹ�");
        Assert.areEqual(Long.valueOf(222), queryResult1.getQueryResult().get(0).get("GMT_TEST"),
            "Ԥ�ڽ��");

    }

    @Test
    @Subject("���и����쳣��������filter�������ҷ�hash����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621032() {

        stepInfo("���и��²���");
        String sqlUpdate = "update TEST_DAILY set TEST_ID=? indexwhere TEST_IP=?";
        Object[] params1 = new Object[2];
        params1[0] = "123459";
        params1[1] = "127.0.0.3";
        MileUpdateResult updateResult = null;
        try {
            updateResult = getApplationClientImpl().preUpdate(sqlUpdate, params1, timeOut);
            Thread.sleep(1000);
        } catch (Exception e) {
            Logger.info("�쳣" + e);
            Assert.isTrue(false, "����ִ���쳣");
        }

        Assert.areEqual(false, updateResult.isSuccessful(), "Ԥ���쳣");

    }

    @After
    public void tearDown() {
        MileDeleteResult deleteNumber = null;
        try {
            stepInfo("ɾ�����ݲ���indexwhere");
            String sql = "delete from TEST_DAILY";
            Object[] params = new Object[1];
            params[0] = null;
            deleteNumber = getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "��������ʧ��");
        }
        Assert.areEqual(10, deleteNumber.getDeleteNum(), "Ԥ��ɾ������");
    }
}
