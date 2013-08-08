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
 * @version $Id: SR621170.java,v 0.1 2012-11-7 ����07:05:16 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("timehint��ѯ")
public class SR621170 extends DocdbTestTools {

    private int timeOut    = 5000;
    List<Long>  docidsList = new ArrayList<Long>();

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=?";
        Object[] params = new Object[5];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = new Date().getTime() + i;
            params[4] = String.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
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
    public void TC621170() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, 11 from TEST_DAILY seghint (0, "
                     + String.valueOf(new Date().getTime() + 10) + ", 0, "
                     + String.valueOf(new Date().getTime() + 10) + ") indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.1";
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
            String sql = "delete from TEST_DAILY indexwhere TEST_IP=?";
            String[] params = new String[1];
            params[0] = "127.0.0.1";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ɾ���쳣");
        }
    }

}
