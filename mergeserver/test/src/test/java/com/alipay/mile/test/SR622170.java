/**
 * 
 */
package com.alipay.mile.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
 * @version $Id: SR622170.java,v 0.1 2012-11-9 ����07:09:00 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("timehint��ѯ")
public class SR622170 extends LevdbTestTools {

    private int timeOut    = 5000;
    List<Long>  docidsList = new ArrayList<Long>();

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[7];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";
        params[5] = "rowkey";
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = new Date().getTime() + i;
            params[4] = String.valueOf(i);
            params[6] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                //Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Assert.isFalse(true, "�����쳣");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�����쳣");
        }
    }

    @Test
    @Subject("timehint��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622170() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, 11 from TEST_VELOCITY seghint (0, "
                     + String.valueOf(new Date().getTime() + 10) + ", 0, "
                     + String.valueOf(new Date().getTime() + 10) + ") indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }

        stepInfo("��ѯ����ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(10, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 10; i++) {
            Assert.areEqual("12345", resultList.get(i).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("milemac", resultList.get(i).get("TEST_NAME"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
        }
    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from CTU_VELOCIT indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ɾ���쳣");
        }
    }

}
