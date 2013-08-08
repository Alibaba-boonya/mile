/**
 * 
 */
package com.alipay.mile.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * @author xiaoju.luo
 * @version $Id: SR621250.java,v 0.1 2013-3-20 ����07:27:50 xiaoju.luo Exp $
 */
/**
 * no creating index for table:
 * case1: less than 1m, insert->pass
   case2: more than 1m ,insert ->fail and error message

  created index for talbe:
  *case1: less than 1m, insert->pass 
   case2: more than 1m ,insert ->fail and error message

stat:less than 1m,insert is ok,but query is failed for no creating index for table
     more than 1m, insert is failed and return "-1" 
     
 */

public class SR621250 extends DocdbTestTools {
	int timeOut = 50000;

	List<Long> docidsList = new ArrayList<Long>();

	public String bigStr(){
		int size = 1048000;
		byte[] x=new byte[size];
		
		for(int i=0;i<size;++i)
		{
			x[i]=65;
		}
//	   String b=new String(x);
//		System.out.println(b);
		return new String(x);
	}
	
	
	@Before
	public void SetUp() {
		MileInsertResult insertResult = null;
		stepInfo("��������");
		// һ����������
		String sql = "insert into TEST A=? ID=?";
		Object[] params = {bigStr(),"123"};
	
		for (int i = 0; i < 10; i++) {

			try {
				insertResult = getApplationClientImpl().preInsert(sql, params,
						timeOut);
				Logger.info("����ID��" + insertResult.getDocId());
			} catch (Exception e) {
				Logger.info("�쳣" + e);
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
	@Subject("��������С��1mʱ����ѯ����")
	@Priority(PriorityLevel.HIGHEST)
	@Tester("xiaoju.luo")
	public void TC621121() {
		stepInfo("����ϵ�в���");
		String sql="select A,ID from TEST indexwhere ID=?";
		Object[] params = new Object[1];
		params[0] = "123";
       
        MileQueryResult	queryResult=null;
		stepInfo("ִ�в�ѯ");
		try {
			queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
		} catch (Exception e) {
			Logger.info("�쳣Ϊ��" + e);
			Assert.isFalse(true, "��ѯ�쳣");
		}
		stepInfo("Ԥ�ڲ�ѯ����ж�");
		Assert.areEqual(10, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
		Assert.areEqual(bigStr(), queryResult.getQueryResult().get(0).get("A"), "����Ԥ����ֵ");
	}
	
	
	@After
	public void tearDown(){
		
	String sql="delete from TEST indexwhere ID=?";
	Object[] params=new Object[1];
	params[0]="123";
	MileDeleteResult deleteResult=null;
	try{
	deleteResult=getApplationClientImpl().preDelete(sql, params, timeOut);
	}catch(Exception e){
		Logger.info("�쳣Ϊ��" + e);
		Assert.isFalse(true, "ɾ���쳣");
	}
	Assert.areEqual(10, deleteResult.getDeleteNum(), "ɾ������������");
}}
