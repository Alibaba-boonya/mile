/**
 * 
 */
package com.alipay.mile.test;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * @author xiaoju.luo
 * @version
 */
@RunWith(SpecRunner.class)
@Feature("�ضϺ��count(ĳ��)��ѯ")
public class SR622200 extends LevdbTestTools {

	/** ��ʱ */
	private int timeOut = 5000;
	//private long num = 30000L;
	private long num = 27000L;
	private long cut = 20000L;

	@Before
	public void setUp() {

		String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=? ROWKEY=? GMT_CTEST=?";
		Object[] params = new Object[7];
		params[0] = "12345";
		params[1] = "milemac";
		params[2] = "127.0.0.1";
		params[3] = 100L;
		params[4] = 22L;
		params[5] = "rowkey3";

		for (long i = 0; i < num; i++) {
			MileInsertResult insertResult;
			params[6] = (Long) i;
			try {
				insertResult = getApplationClientImpl().preInsert(sql, params,
						timeOut);

				 Logger.warn("docid: " + insertResult.getDocId());

			} catch (Exception e) {
				Assert.isFalse(true, "��Ԥ���쳣");

			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Assert.isFalse(true, "��Ԥ���쳣");
		}
	}

//	@After
//	public void tearDown() {
//		try {
//
//			stepInfo("ɾ������");
//			String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
//			String[] params = new String[1];
//			params[0] = "rowkey3";
//			getApplationClientImpl().preDelete(sql, params, timeOut);
//
//		} catch (Exception e) {
//			Assert.isFalse(true, "��Ԥ���쳣");
//		}
//	}

	@Test
	public void testCount() {
		String sql = "select count(GMT_TEST) as num from TEST_VELOCITY indexwhere ROWKEY=?";
		Object[] params = new Object[1];
		params[0] = "rowkey3";
		MileQueryResult queryResult = null;
		try {
			queryResult = getApplationClientImpl().preQueryForList(sql, params,
					timeOut);

			Logger.warn("�����" + queryResult);

		} catch (Exception e) {
			Assert.isFalse(true, "��Ԥ���쳣");
		}
		Assert.areEqual(cut, queryResult.getQueryResult().get(0).get("num"),
				"Ԥ�ڽ��");

	}
}
