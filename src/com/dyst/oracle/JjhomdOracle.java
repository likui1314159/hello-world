package com.dyst.oracle;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.dyst.entites.Jjhomd;

public class JjhomdOracle {
	public static List<Jjhomd> jjhomdList = new ArrayList<Jjhomd>();
	
	/**
	 * ����һ��������
	 */
	public static void getJjhomds(){
		//�������ݿ�
		DBConnectionManager dbCon = null;
		Connection connection = null;
		QueryRunner qr = new QueryRunner();
		try {
			dbCon = DBConnectionManager.getInstance();
			connection = dbCon.getConnection("db");
			 
			//ִ�в�ѯ
			String sql = "select id, cphid, cplx, honmddj, kssj, jssj, jlzt, zt from Jjhomd where jlzt = '1' and zt = '1' and honmddj = '1' and jssj >= sysdate";
			jjhomdList = (List<Jjhomd>)qr.query(connection, sql, new BeanListHandler<Jjhomd>(Jjhomd.class));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 *����һ�������� 
	 */
	@SuppressWarnings("finally")
	public static boolean hideJjhomd(String cphm, String cplx){
		boolean hideFlag = false;//�Ƿ����غ����� true����
		Jjhomd jjhomd = null;
		try {
			//�ж��Ƿ�����
			if(cphm != null && !"".equals(cphm.trim())
					&& cplx != null && !"".equals(cplx.trim())){
				for(int j = 0;j < jjhomdList.size();j++){
					jjhomd = jjhomdList.get(j);
					if(jjhomd != null && jjhomd.getCphid() != null && !"".equals(jjhomd.getCphid().trim())
						&& jjhomd.getCplx() != null && !"".equals(jjhomd.getCplx().trim())){
						//������ƺ��뼰������ɫƥ�䣬����Ҫ���أ�Ȼ���˳�ѭ��
						if(jjhomd.getCphid().trim().equals(cphm.trim())
							&& jjhomd.getCplx().trim().equals(cplx.trim())){
							hideFlag = true;//�Ƿ����غ����� true����
							break;
						}
					}
				}
			}else {
				hideFlag = false;//�Ƿ����غ����� true����
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			return hideFlag;
		}
	}
}
