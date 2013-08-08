/**
 * 
 */
package com.alipay.mile.test;

import java.util.Date;

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
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * @author xiaoju.luo
 * @version $Id: SR621120.java,v 0.1 2012-11-7 ����01:20:32 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("CTU�����ѯ�ӿڣ�PreCtuClusterCountQuery")
public class SR621120 extends DocdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("ִ��Ԥ�������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_IP=? TEST_NAME=? EXTERNAL_ID=? CLIENT_ID=? EVENT_NAME=? g1=? U=? time=? GMT_TEST=?";
        Object[] params = new Object[10];
        params[1] = "bb";
        params[2] = "cc";
        params[3] = "dd";
        params[4] = "ee";
        params[5] = "ff";
        params[6] = "gg";
        params[7] = "name";
        try {
            for (int i = 0; i < 10; i++) {
                params[0] = new String("aa" + i);
                params[8] = new Date().getTime();
                params[9] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                // Logger.infoText("docidΪ��" + insertResult.getDocId());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Assert.isFalse(true, "�����쳣");
        }
    }

    @Test
    @Subject("���Դ��������Ĳ������ӿڵĹ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621120() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388889343,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        int queryResult = 0;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(tableName, condition,
                clusterField, topField, params1, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("Ԥ�ڲ�ѯ����ж�");
        Assert.areEqual(7, queryResult, "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("���Դ������cluster�Ĳ����쳣��������Ҫ�׳��쳣")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621121() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1338889343,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID,TEST_IP,TEST_NAME,EXTERNAL_ID,CLIENT_ID,EVENT_NAME";

        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        int queryResult = 0;
        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(tableName, condition,
                clusterField, topField, params1, timeOut);

        } catch (SqlExecuteException e) {
            Logger.info("SqlExecuteException" + e);
            Assert.areEqual("cluster��ֻ�ܰ���һ��!", e.getMessage(), "Ԥ���쳣");

        } catch (Exception e) {
            Logger.info("�����쳣" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        // stepInfo("Ԥ�ڲ�ѯ����ж�");
        // Assert.areEqual(0, queryResult, "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("���Դ���Ĳ�������Ϊ��ʱ���������СΪ0")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621122() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388885390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        // params1[0] = "cc";
        // params1[1] = Long.valueOf(1);
        // params1[2] = Long.valueOf(9);
        int queryResult = 0;
        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(tableName, condition,
                clusterField, topField, params1, timeOut);

        } catch (Exception e) {
            Logger.info("�쳣Ϊ��" + e);
            Assert.isFalse(true, "��ѯ�쳣");
        }
        stepInfo("Ԥ�ڲ�ѯ����ж�");
        Assert.areEqual(0, queryResult, "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("���Դ���Ĳ�������Ϊnullʱ���ӿڱ��쳣")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621123() {
        stepInfo("����ϵ�в���");
        String tableName = "";
        String condition = "seghint(0,1388815390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";

        stepInfo("ִ�в�ѯ");
        int queryResult = 0;
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(tableName, condition,
                clusterField, topField, null, timeOut);

        } catch (Exception e) {
            // Logger.info("�쳣" + e);
            Assert.areEqual(true, e.getClass().equals(SqlExecuteException.class), "Ԥ���쳣");
        }
        stepInfo("Ԥ�ڲ�ѯ����ж�");
        Assert.areEqual(0, queryResult, "Ԥ�ڲ�ѯ�������С");
    }

    @Test
    @Subject("���Դ���Ĳ���stringΪ�ո�ʱ����� mile���﷨���󣬽ӿڱ��쳣")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621124() {
        stepInfo("����ϵ�в���");
        String condition = "";
        String clusterField = "";
        String topField = "";
        int queryResult = 0;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(null, condition,
                clusterField, topField, null, timeOut);
        } catch (Exception e) {
            // Logger.error("SqlExecuteException" + e);
            Assert.areEqual(true, e.getMessage().contains("String index out of range: -1"), "Ԥ���쳣");
        }
    }

    @Test
    @Subject("���Դ���Ĳ������������Ͳ��ǲ���ʱ���������ͣ��ӿڷ��ؽ��Ϊ0���쳣")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621125() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388815390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = new Double(1);
        params1[2] = new Double(9);

        stepInfo("ִ�в�ѯ");
        int queryResult = 0;
        try {
            queryResult = getApplationClientImpl().preCtuClusterCountQuery(tableName, condition,
                clusterField, topField, params1, timeOut);

        } catch (Exception e) {
            Logger.error("SqlExecuteException" + e);
            Assert.isFalse(true, "��ѯʧ��");
        }
        stepInfo("Ԥ�ڲ�ѯ����ж�");
        Assert.areEqual(0, queryResult, "Ԥ�ڲ�ѯ�������С");
    }

    @After
    public void tearDown() {
        stepInfo("ִ������ɾ��");
        String sql = "delete from TEST_DAILY indexwhere TEST_NAME=?";
        Object[] params2 = new Object[1];
        params2[0] = "cc";
        MileDeleteResult dResult = null;
        try {
            dResult = getApplationClientImpl().preDelete(sql, params2, timeOut);
        } catch (Exception e) {

            Assert.isFalse(true, "ɾ���쳣");
        }
        Assert.isTrue(dResult.getDeleteNum() > 0, "ɾ���������0");

    }

}
