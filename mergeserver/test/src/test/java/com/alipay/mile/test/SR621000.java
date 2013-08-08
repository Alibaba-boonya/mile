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
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * ģ����ѯ������֧������
 * 
 * @author xiaoju.luo
 * @version $Id: SR621000.java,v 0.1 2012-10-30 ����03:44:29 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("ģ����ѯ")
public class SR621000 extends DocdbTestTools {

    int timeOut = 5000;

    @Before
    public void setUp() {

        stepInfo("��������");
        MileInsertResult insertResult;
        String sql = "insert into TEST_ADDRESS TEST_VALUE=? with wordseg(TEST_VALUE)=(?,?)";
        Object[] params = new Object[3];

        try {
            // ����1
            params[0] = "�����찲��";
            params[1] = "����";
            params[2] = "�찲��";
            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }
            // ����2
            params[0] = "�����㳡";
            params[1] = "����";
            params[2] = "�㳡";
            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }
            // ����3
            params[0] = "��������";
            params[1] = "����";
            params[2] = "����";

            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }
            // ����4
            params[0] = "����������";
            params[1] = "����";
            params[2] = "������";

            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }

            // ����5
            String sql1 = "insert into TEST_ADDRESS TEST_VALUE=? with wordseg(TEST_VALUE)=(?,?,?,?)";
            Object[] params1 = new Object[5];
            params1[0] = "����������ȫ�۵·���";
            params1[1] = "����";
            params1[2] = "������";
            params1[3] = "ȫ�۵�";
            params1[4] = "����";

            insertResult = getApplationClientImpl().preInsert(sql1, params1, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }

            // ����6
            String sql2 = "insert into TEST_ADDRESS TEST_VALUE=? with wordseg(TEST_VALUE)=(?,?,?,?,?)";
            Object[] params2 = new Object[6];
            params2[0] = "����������ȫ�۵·����8��";
            params2[1] = "����";
            params2[2] = "������";
            params2[3] = "ȫ�۵�";
            params2[4] = "����";
            params2[5] = "��8��";
            insertResult = getApplationClientImpl().preInsert(sql2, params2, timeOut);
            if (!insertResult.isSuccessful()) {
                Assert.isFalse(true, "����ʧ��");
            }
        } catch (Exception e) {
            Logger.info("�쳣" + e);
            Assert.isFalse(true, "�����쳣");

        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "����ʧ��");

        }
    }

    @Test
    // �������ȷ��û�а���ƥ��ȸߵͼ�������
    @Subject("ģ����ѯ,3���ִʶ���ƥ�䣬����2���ִ���ͬʱƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621000() {
        stepInfo("1.ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?,?,?)";
        String[] params = new String[5];

        try {
            params[0] = "����";
            params[1] = "����";
            params[2] = "������";
            params[3] = "ȫ�۵�";
            params[4] = "��8��";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);

            stepInfo("2.�Բ�ѯ������е�ֵ�����ж�");
            //��ӡ��־
            System.out.println("�����"+queryResult.getSqlResultSet());
            Assert.areEqual("����������ȫ�۵·����8��", queryResult.getQueryResult().get(0).get("TEST_VALUE"),
                "Ԥ�ڽ��");
        } catch (Exception e) {
            Logger.warn("����0�쳣:" + e);
            Assert.isFalse(true, "��ѯʧ��");
        }

    }

    @Test
    @Subject("ģ����ѯ,3���ִ�ֻ��1���ִ���ƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621001() {
        stepInfo("1.ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?)";
        String[] params = new String[3];

        try {
            params[0] = "�Ͼ�";
            params[1] = "����";
            params[2] = "������";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();

            stepInfo("2.�Բ�ѯ������ж�");
            int sizeValue = resultList.size();
            Assert.areEqual(4, sizeValue, "��С����");
        } catch (Exception e) {

            Assert.isFalse(true, "��ѯʧ��");
        }

    }

    @Test
    @Subject("ģ����ѯ,3���ִ�ֻ��1���ִ���ƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621002() {
        stepInfo("1.ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?)";
        String[] params = new String[3];

        try {
            params[0] = "�Ͼ�";
            params[1] = "����";
            params[2] = "����";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();

            stepInfo("2.�Բ�ѯ������ж�");
            int sizeValue = resultList.size();
            Assert.areEqual(3, sizeValue, "�������С����");
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯʧ��");
        }

    }

    @Test
    @Subject("ģ����ѯ,3���ִ�2��ͬʱƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621003() {
        stepInfo("1.ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?)";
        String[] params = new String[3];

        try {
            params[0] = "����";
            params[1] = "����";
            params[2] = "�人";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();

            stepInfo("2.�Բ�ѯ������ж�");
            int sizeValue = resultList.size();
            Assert.areEqual(3, sizeValue, "�������С����");

        } catch (Exception e) {
            Assert.isFalse(true, "��ѯʧ��");
        }

    }

    @Test
    @Subject("ģ����ѯ,3���ִ�û����1���ִ���ƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621004() {
        stepInfo("1.ִ�в�ѯ");
        String sql = "select TEST_VALUE from TEST_ADDRESS indexwhere TEST_VALUE match (?,?,?)";
        String[] params = new String[3];
        List<Map<String, Object>> resultList = null;
        try {
            params[0] = "�Ͼ�";
            params[1] = "����";
            params[2] = "���";
            MileQueryResult queryResult = getApplationClientImpl().preQueryForList(sql, params,
                timeOut);
            resultList = queryResult.getQueryResult();

        } catch (Exception e) {
            Assert.isFalse(true, "��ѯʧ��");
        }

        stepInfo("2.�Բ�ѯ������ж�");
        int sizeValue = resultList.size();
        Assert.areEqual(0, sizeValue, "�������С����");

    }

    @After
    public void tearDown() {
        MileDeleteResult delResult = null;
        try {
             String sql = "delete from TEST_ADDRESS";
             Object[] params = new Object[1];
             params[0] = null;
             delResult = getApplationClientImpl().preDelete(sql, params, timeOut);
             //deleteNumber = getApplationClientImpl().preDelete(sql, params, timeOut);
        	
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
        Assert.areEqual(6, delResult.getDeleteNum(), "ɾ��Ԥ������");
    }
}
