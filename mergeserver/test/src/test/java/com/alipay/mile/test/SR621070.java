/**
 * 
 */
package com.alipay.mile.test;

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
 * ģ����ѯ��������������ΪӢ��
 * 
 * @author xiaoju.luo
 * @version $Id: SR621070.java,v 0.1 2012-11-6 ����10:54:42 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("wordseg&match")
public class SR621070 extends DocdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        MileInsertResult insertResult;

        String sql = "insert into TEST_ADDRESS TEST_VALUE=? with wordseg(TEST_VALUE)=(?,?,?) ";
        Object[] params = new Object[10];

        try {
            params[0] = "hang zhou shi";
            params[1] = "hang";
            params[2] = "zhou";
            params[3] = "shi";

            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }

            params[0] = "shang hai shi";
            params[1] = "shang";
            params[2] = "hai";
            params[3] = "shi";

            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }

            params[0] = "bei jing shi";
            params[1] = "bei";
            params[2] = "jing";
            params[3] = "shi";

            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }
        } catch (Exception e) {
            Assert.isFalse(true, "����ִ���쳣");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("ģ����ѯ��wordsge��matchƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621070() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?)";
        String[] params = new String[3];
        try {
            params[0] = "hai";
            params[1] = "shi";
            params[2] = "shang";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();
            stepInfo("�Բ�ѯ������ж�");
            Assert.areEqual("shang hai shi", resultList.get(0).get("TEST_VALUE"), "Ԥ�ڵĲ�ѯ���");

        } catch (Exception e) {
        	Logger.warn("�쳣����"+e);
            Assert.isFalse(true, "��ѯִ��ʧ��");
        }
    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_ADDRESS indexwhere TEST_VALUE match (?)";
            String[] params = new String[1];
            params[0] = "shi";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "����ִ���쳣");
        }
    }
}
