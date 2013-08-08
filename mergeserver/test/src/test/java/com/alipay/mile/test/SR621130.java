/**
 * 
 */
package com.alipay.mile.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * @author xiaoju.luo
 * @version $Id: SR621130.java,v 0.1 2012-11-7 ����01:59:03 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("CTU�����ѯ�ӿڣ�preCtuClusterQuery")
public class SR621130 extends DocdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("ִ�в���");
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
                params[0] = new String("a" + i);
                params[8] = new Date().getTime();
                params[9] = Long.valueOf(i);
                MileInsertResult insertResult = getApplationClientImpl().preInsert(sql, params,
                    timeOut);
                Assert.isTrue(insertResult.isSuccessful(), "��ѯִ�гɹ�");
            }
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("���Դ��������Ĳ������ӿڵĹ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621130() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388986728,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "TEST_ID");
        selectFields.add(1, "TEST_IP");
        selectFields.add(2, "GMT_TEST");
        selectFields.add(3, "TEST_NAME");
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        MileQueryResult queryResult = null;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterQuery(tableName, selectFields,
                condition, clusterField, topField, orderField, orderType, limit, offset, params1,
                timeOut);

            // Assert.assertNotNull(queryResult);
            // boolean b = queryResult.getQueryResult().isEmpty();
            // String str = "";
            // for (int i = 0; i < 7; i++) {
            // for (String key : queryResult.getQueryResult().get(i).keySet()) {
            // str += queryResult.getQueryResult().get(i).get(key).toString();
            // }
            // }
            // Assert.assertEquals("bba88ccbba77ccbba66ccbba55ccbba44ccbba33ccbba22cc",
            // str);
        } catch (Exception e) {
            Logger.info("�쳣Ϊ" + e);
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڲ�ѯ����ж�");
        Assert.areEqual(7, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        String str = "";
        for (int i = 0; i < 7; i++) {
            for (String key : queryResult.getQueryResult().get(i).keySet()) {
               // str += queryResult.getQueryResult().get(i).get(key).toString();
            	str += queryResult.getQueryResult().get(i);
            }
        }

      //  System.out.println("���"+str);
        Assert.areEqual("{TEST_ID=a8, TEST_NAME=cc, GMT_TEST=8, TEST_IP=bb}{TEST_ID=a8, TEST_NAME=cc, GMT_TEST=8, TEST_IP=bb}{TEST_ID=a8, TEST_NAME=cc, GMT_TEST=8, TEST_IP=bb}{TEST_ID=a8, TEST_NAME=cc, GMT_TEST=8, TEST_IP=bb}{TEST_ID=a7, TEST_NAME=cc, GMT_TEST=7, TEST_IP=bb}{TEST_ID=a7, TEST_NAME=cc, GMT_TEST=7, TEST_IP=bb}{TEST_ID=a7, TEST_NAME=cc, GMT_TEST=7, TEST_IP=bb}{TEST_ID=a7, TEST_NAME=cc, GMT_TEST=7, TEST_IP=bb}{TEST_ID=a6, TEST_NAME=cc, GMT_TEST=6, TEST_IP=bb}{TEST_ID=a6, TEST_NAME=cc, GMT_TEST=6, TEST_IP=bb}{TEST_ID=a6, TEST_NAME=cc, GMT_TEST=6, TEST_IP=bb}{TEST_ID=a6, TEST_NAME=cc, GMT_TEST=6, TEST_IP=bb}{TEST_ID=a5, TEST_NAME=cc, GMT_TEST=5, TEST_IP=bb}{TEST_ID=a5, TEST_NAME=cc, GMT_TEST=5, TEST_IP=bb}{TEST_ID=a5, TEST_NAME=cc, GMT_TEST=5, TEST_IP=bb}{TEST_ID=a5, TEST_NAME=cc, GMT_TEST=5, TEST_IP=bb}{TEST_ID=a4, TEST_NAME=cc, GMT_TEST=4, TEST_IP=bb}{TEST_ID=a4, TEST_NAME=cc, GMT_TEST=4, TEST_IP=bb}{TEST_ID=a4, TEST_NAME=cc, GMT_TEST=4, TEST_IP=bb}{TEST_ID=a4, TEST_NAME=cc, GMT_TEST=4, TEST_IP=bb}{TEST_ID=a3, TEST_NAME=cc, GMT_TEST=3, TEST_IP=bb}{TEST_ID=a3, TEST_NAME=cc, GMT_TEST=3, TEST_IP=bb}{TEST_ID=a3, TEST_NAME=cc, GMT_TEST=3, TEST_IP=bb}{TEST_ID=a3, TEST_NAME=cc, GMT_TEST=3, TEST_IP=bb}{TEST_ID=a2, TEST_NAME=cc, GMT_TEST=2, TEST_IP=bb}{TEST_ID=a2, TEST_NAME=cc, GMT_TEST=2, TEST_IP=bb}{TEST_ID=a2, TEST_NAME=cc, GMT_TEST=2, TEST_IP=bb}{TEST_ID=a2, TEST_NAME=cc, GMT_TEST=2, TEST_IP=bb}", str, "��ѯԤ�ڽ��");
    }

    @Test
    @Subject("���Դ��������Ĳ���clusterFieldΪ����,��鲻�����ж��cluster�б������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621131() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388815390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID,TEST_IP,TEST_NAME,EXTERNAL_ID,CLIENT_ID,EVENT_NAME";
        String topField = "max(GMT_TEST)";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "TEST_ID");
        selectFields.add(1, "TEST_IP");
        selectFields.add(2, "GMT_TEST");
        selectFields.add(3, "TEST_NAME");
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);

        stepInfo("ִ�в�ѯ");
        try {
            getApplationClientImpl().preCtuClusterQuery(tableName, selectFields, condition,
                clusterField, topField, orderField, orderType, limit, offset, params1, timeOut);
        } catch (Exception e) {
            stepInfo("Ԥ�ڲ�ѯ�쳣�ж�");
            Logger.info("ִ���쳣" + e);
            Assert.areEqual("�������ж��cluster��!", e.getMessage(), "Ԥ���쳣��Ϣ");
        }
    }

    @Test
    @Subject("���limit-offset<=0����ѯ�쳣")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621132() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388885390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "TEST_ID");
        selectFields.add(1, "TEST_IP");
        selectFields.add(2, "GMT_TEST");
        selectFields.add(3, "TEST_NAME");
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 1;
        int offset = 5;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);

        stepInfo("ִ�в�ѯ");
        try {
            getApplationClientImpl().preCtuClusterQuery(tableName, selectFields, condition,
                clusterField, topField, orderField, orderType, limit, offset, params1, timeOut);

        } catch (SqlExecuteException e) {
            stepInfo("Ԥ�ڲ�ѯ�쳣�ж�");
            Assert.areEqual("�ڲ�ѯ��offsetӦ��С��limit!", e.getMessage(), "Ԥ�ڲ�ѯ");
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("���ʱ�䷶Χ����Ϊ�գ����ؽ����Ϊ0")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621133() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388885390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "TEST_ID");
        selectFields.add(1, "TEST_IP");
        selectFields.add(2, "GMT_TEST");
        selectFields.add(3, "TEST_NAME");
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(9);
        params1[2] = Long.valueOf(1);
        MileQueryResult queryResult = null;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterQuery(tableName, selectFields,
                condition, clusterField, topField, orderField, orderType, limit, offset, params1,
                timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڲ�ѯ������ж�");
        boolean b = queryResult.getQueryResult().isEmpty();
        Assert.isTrue(b, "Ԥ�ڽ��Ϊtrue");
    }

    @Test
    @Subject("���Դ���List����Ϊ��ֵ�쳣�Ĳ���")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621134() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        List<String> selectFields = new ArrayList<String>();
        String condition = "indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        MileQueryResult queryResult = null;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterQuery(tableName, selectFields,
                condition, clusterField, topField, orderField, orderType, limit, offset, params1,
                timeOut);
        } catch (Exception e) {
            stepInfo("Ԥ�ڲ�ѯ������ж�");
            Assert.areEqual(null, queryResult, "Ԥ���쳣�Ľ����ΪNULL");
        }
    }

    @Test
    @Subject("���Դ������Ϊnull���ӿڵ��쳣����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621135() {
        stepInfo("����ϵ�в���");
        String condition = "indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        MileQueryResult queryResult = null;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterQuery(null, null, condition,
                clusterField, topField, orderField, orderType, limit, offset, params1, timeOut);
        } catch (Exception e) {
            stepInfo("Ԥ�ڲ�ѯ������ж�");
            Assert.areEqual(null, queryResult, "Ԥ���쳣�Ľ����ΪNULL");
        }

    }

    @Test
    @Subject("���Բ�ѯ���ֶε��������͸�������������Ͳ�һ�����ӿڵ��쳣���ԣ��жϷ��صĲ�ѯ����б�Ϊ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621136() {
        stepInfo("����ϵ�в���");
        String tableName = "TEST_DAILY";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "a1");
        selectFields.add(1, "b1");
        selectFields.add(2, "value");
        selectFields.add(3, "c1");
        String condition = "indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "a1";
        String topField = "max(GMT_TEST)";
        String orderField = "GMT_TEST";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = new Double(1);
        params1[2] = new Double(9);
        MileQueryResult queryResult = null;

        stepInfo("ִ�в�ѯ");
        try {
            queryResult = getApplationClientImpl().preCtuClusterQuery(tableName, selectFields,
                condition, clusterField, topField, orderField, orderType, limit, offset, params1,
                timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "�쳣ʧ��");
        }

        stepInfo("Ԥ�ڲ�ѯ������ж�");
        boolean result = queryResult.getQueryResult().isEmpty();
        Assert.areEqual(true, result, "Ԥ�ڽ��Ϊ��");
    }

    @After
    public void tearDown() {
        stepInfo("ִ��ɾ������");
        try {
            String sql = "delete from TEST_DAILY indexwhere TEST_NAME=?";
            Object[] params2 = new Object[1];
            params2[0] = "cc";
            getApplationClientImpl().preDelete(sql, params2, timeOut);
        } catch (Exception e) {

            Assert.isFalse(true, "ִ���쳣");
        }
    }
}
