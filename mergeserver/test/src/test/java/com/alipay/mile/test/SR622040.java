/**
 * 
 */
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
import com.alipay.mile.client.result.MileExportResult;
import com.alipay.mile.client.result.MileInsertResult;

/**
 * @author xiaoju.luo
 * @version $Id: SR622040.java,v 0.1 2012-11-9 ����02:02:04 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("�����뵼��")
public class SR622040 extends LevdbTestTools {
    /**��ʱ*/
    private int timeOut = 5000;

    @Before
    public void setUp() {

        MileInsertResult insertResult = null;
        stepInfo("��������");
        // һ����������
        try {
            String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
            Object[] params = new Object[6];
            params[0] = "12345";
            params[1] = "milemac";
            params[2] = "127.0.0.2";
            params[3] = Long.valueOf(100);
            params[4] = "rowkey";
            params[5] = Long.valueOf(100);
            insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            //	Logger.info("docid: " + insertResult.getDocId());
        } catch (Exception e) {
            Assert.isFalse(true, "����ʧ��");
        }
    }

    @Test
    @Subject("����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622040() {
        MileExportResult exportResult = null;
        stepInfo("��������");
        // һ����������
        try {
            String sql = "export to ? from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST=?";
            Object[] params = new Object[3];
            params[0] = "/home/admin/susu/mile_levdb/export.txt";
            params[1] = "rowkey";
            params[2] = Long.valueOf(100);
            exportResult = getApplationClientImpl().preExport(sql, params, timeOut);
            Logger.infoText("aaa", exportResult.getDocState().toString());
        } catch (Exception e) {
            Logger.error("����" + e);
            Assert.isFalse(true, "����ʧ��");
        }
    }

    //	@Test
    //	@Subject("�����쳣")
    //	@Priority(PriorityLevel.HIGHEST)
    //	@Tester("xiaoju.luo")
    //	public void TC621041() {
    //		MileExportResult exportResult = null;
    //		stepInfo("��������");
    //		// һ����������
    //		try {
    //			String sql = "export to path TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST=?";
    //			Object[] params = new Object[3];
    //			params[0] = "/home/admin/susu/mile_levdb/export.txt";
    //			params[1] = "rowkey";
    //			params[2] = Long.valueOf(100);
    //			exportResult = getApplationClientImpl().preExport(sql, params, timeOut);
    //		    Logger.infoText("aaa", exportResult.getDocState().toString());
    //			} catch (Exception e) {
    //				Logger.error("����"+e);
    //				Assert.isFalse(true, "����ʧ��");
    //			}
    //	}

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            Object[] params = new Object[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯ�쳣");
        }
    }
}
