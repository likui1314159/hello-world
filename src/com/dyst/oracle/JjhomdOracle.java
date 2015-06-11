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
	 * 加载一级红名单
	 */
	public static void getJjhomds(){
		//连接数据库
		DBConnectionManager dbCon = null;
		Connection connection = null;
		QueryRunner qr = new QueryRunner();
		try {
			dbCon = DBConnectionManager.getInstance();
			connection = dbCon.getConnection("db");
			 
			//执行查询
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
	 *隐藏一级红名单 
	 */
	@SuppressWarnings("finally")
	public static boolean hideJjhomd(String cphm, String cplx){
		boolean hideFlag = false;//是否隐藏红名单 true隐藏
		Jjhomd jjhomd = null;
		try {
			//判断是否隐藏
			if(cphm != null && !"".equals(cphm.trim())
					&& cplx != null && !"".equals(cplx.trim())){
				for(int j = 0;j < jjhomdList.size();j++){
					jjhomd = jjhomdList.get(j);
					if(jjhomd != null && jjhomd.getCphid() != null && !"".equals(jjhomd.getCphid().trim())
						&& jjhomd.getCplx() != null && !"".equals(jjhomd.getCplx().trim())){
						//如果车牌号码及车牌颜色匹配，则需要隐藏，然后退出循环
						if(jjhomd.getCphid().trim().equals(cphm.trim())
							&& jjhomd.getCplx().trim().equals(cplx.trim())){
							hideFlag = true;//是否隐藏红名单 true隐藏
							break;
						}
					}
				}
			}else {
				hideFlag = false;//是否隐藏红名单 true隐藏
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			return hideFlag;
		}
	}
}
