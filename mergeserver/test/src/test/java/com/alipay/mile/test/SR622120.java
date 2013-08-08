/**
 * 
 */
package com.alipay.mile.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/** �����������͵Ĳ�ѯ��֧��
 * @author xiaoju.luo
 * @version $Id: SR622120.java,v 0.1 2012-11-9 ����06:22:48 xiaoju.luo Exp $
 */

public class SR622120 extends LevdbTestTools {

    int timeOut = 5000;

    @Before
    public void SetUp() {
        MileInsertResult insertResult;
        stepInfo("��������");
        //һ����������
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=? BYE=? SH=? INE=? DU=? FL=? LO=?";
        Object[] params = new Object[12];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.2";
        params[4] = "rowkey";
        params[6] = Byte.valueOf((byte) 000);
        params[7] = Short.valueOf((short) 10);
        params[8] = Integer.valueOf(111);
        params[9] = Double.valueOf(222);
        params[10] = Float.valueOf(333);
        params[11] = Long.valueOf(444);
        for (int i = 0; i < 10; i++) {
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Logger.info("�쳣Ϊ��" + e);
                Assert.isFalse(true, "���������쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "�ȴ��쳣");
        }
    }

    @Test
    @Subject("������������ֵ�Ĳ�ѯ,ͳ���ַ�����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622120() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, ABC,BYE,SH,INE,DU,FL,LO from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        Assert.areEqual(10, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        stepInfo("��ѯ����ж�");
        Assert.areEqual("12345", queryResult.getQueryResult().get(0).get("TEST_ID"), "Ԥ�ڽ��");
        Assert.areEqual(Byte.valueOf((byte) 000), queryResult.getQueryResult().get(0).get("BYE"),
            "Ԥ�ڽ��");
        Assert.areEqual(Short.valueOf((short) 10), queryResult.getQueryResult().get(0).get("SH"),
            "Ԥ�ڽ��");
        Assert.areEqual(Integer.valueOf(111), queryResult.getQueryResult().get(0).get("INE"),
            "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(222), queryResult.getQueryResult().get(0).get("DU"), "Ԥ�ڽ��");
        Assert.areEqual(Float.valueOf(333), queryResult.getQueryResult().get(0).get("FL"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(444), queryResult.getQueryResult().get(0).get("LO"), "Ԥ�ڽ��");
    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }
    }
}
