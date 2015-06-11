package com.dyst.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import com.dyst.entites.Sbnew;
import com.dyst.entites.Txsjcx;
/**
 * ʵ��Oralce��ѯ
 * @author likui
 */
public class Search {	
	/**
	 *��ѯ����1
	 * ��������: ��ѯ�ض��������͵���ʷ�켣<br>
	 * sql:��ѯ���   <br>
	 * from ��ʼ��ѯ��¼<br>
	 * pagsize:ÿҳ��ʾ��¼��<br>
	 * sort : �����ֶ�<br>
	 * sortType ��������<br>
	 * @throws SQLException <br>
	 * @return ʶ���¼�����б�
	 */	
	public List<Sbnew> TDCPGJCX(String sql,int from ,int pagsize,String sort,String sortType) throws SQLException{	
		//�������ݿ�
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
//		System.out.println(sql);
		String orderStr="";//�����ֶ�������
		try {
			QueryRunner qr = new QueryRunner();
			//oracle���� ��ҳ��ѯ��ͬʱ���򣬳���ʹ����Ƕ��һ��Ĳ�ѯ��ʽ. ����������ʱЧ�ʽϵ�
			//��һ�ֽ����������������ֶν����������������������Ȱ����ֶ�����Ȼ����rownum,��������
			//��ʶ���ʱ��tpid1������������ͨ��ʱ��Ϊ�������У����ڴ�ʹ��tpid1��������
			if(!"".equals(sort.trim())&&!"".equals(sortType)){//��������sql
				orderStr = " order by tpid1  "+sortType ;
			}
			
			if(pagsize != 0){//�����ʼ��ѯ��¼��ÿҳ��ʾ��¼����Ϊ0����ҳ��ѯ
				 sql = "SELECT * FROM (" + sql + " and ROWNUM <= " + (from + pagsize) + orderStr +") WHERE rn > " + from;
			}else{
				sql+=orderStr;
			}
			
			//ִ�в�ѯ
		    List<Sbnew> listTxsj = new ArrayList<Sbnew>();
			listTxsj = (List<Sbnew>)qr.query(connection, sql, new BeanListHandler<Sbnew>(Sbnew.class));
			//��ҳ��ѯ
//			System.out.println(listTxsj.size() + "--Oracle���ݿ��ѯ����");
            return listTxsj;		
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Oracle���ݿ��ѯ�쳣");
		}finally{
			if(connection!=null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 *31һ�ű�ʵ�ַ�ʽ
	 * ��������: ��ѯ�ض��������͵���ʷ�켣<br>
	 * sql:��ѯ���   <br>
	 * from ��ʼ��ѯ��¼<br>
	 * pagsize:ÿҳ��ʾ��¼��<br>
	 * @throws SQLException <br>
	 * @return ʶ���¼�����б�
	 */	
	public List<Txsjcx> TDCPGJCX31(String sql,int from ,int pagsize) throws SQLException{	
//		Date date1 = new Date();
		//�������ݿ�
//		DateConnection conn = new DateConnection();//��ȡ���ݿ�����
//		Connection connection = conn.getconnect();
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
		try {
			QueryRunner qr = new QueryRunner();
			//�����ʼ��ѯ��¼��ÿҳ��ʾ��¼����Ϊ0����ҳ��ѯ
			if(pagsize != 0){
				 sql = "select * FROM ( select cphid, jcdid, cplx, sbsj, cdid, tpid1,ROWNUM rn from ("
					 + sql + ") where ROWNUM<=" + (from + pagsize) + ")  WHERE rn >= " + from;//�����Ϊ1�ű�ʵ�֣���Ҫ�޸ķ�����ʵ���ࡣ
			}
		    
//			System.out.println(sql);
			
			//ִ�в�ѯ
		    List<Txsjcx> listTxsj = new ArrayList<Txsjcx>();
			listTxsj = (List<Txsjcx>)qr.query(connection, sql, new BeanListHandler<Txsjcx>(Txsjcx.class));
			
//			//�����ɾ��	
//			Date date2 = new Date();
//			double d = (date2.getTime()-date1.getTime());
//			System.out.println("��ѯOracle���ݿ��ʱ��"+d/1000+"��");
			
			//��ҳ��ѯ
            return listTxsj;		
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Oracle���ݿ��ѯ�쳣");
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 * ��������: ����ָ��sql��䷵�ز�ѯ��¼����
	 * @throws SQLException 
	 */	
	public Integer getTDCPGJCXCount(String sql) throws SQLException{	
		Date date1 = new Date();
		//�������ݿ�
//		DateConnection conn = new DateConnection();//��ȡ���ݿ�����
//		Connection connection = conn.getconnect();
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
//		System.out.println(sql);
		try {
//			connection = conn.getconnect();
			QueryRunner qr = new QueryRunner();
			Long count = qr.query(connection, sql, new ResultSetHandler<Long>(){
				public Long handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						Long len = rs.getLong(1);//����rs.getLong("count")
//						System.out.println(len+"--Oracle���ݿ��ѯ����");
						return len;
					}
					return 0L;
				}
			});
			
//			//�����ɾ��	
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("��ѯOracle���ݿ��¼������ʱ��"+d/1000+"��");
//		    System.out.println(sql);
			return count.intValue();
		} catch (Exception e) {
			throw new SQLException("Oracle���ݿ��ѯ�쳣");
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);//�黹���ӳ�����
			}
		}
	}
	
	/**
	 * ����Oracleʶ���¼
	 * tpid1 ͼƬid
	 * cphm1 �޸ĺ�ĳ��ƺ���
	 * cplx1 �޸ĺ�ĳ�������
	 */
	public String updateOracleSb(String tpid1,String cphm1,String cplx1) throws Exception {
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
		Statement st = null;
		String sql = "";
		try {
			//���ƺ��뼰ͼƬid����Ϊ��
			if(tpid1 != null && !"".equals(tpid1.trim()) 
					&& cphm1 != null && !"".equals(cphm1.trim())){
				//�����������null������Ϊ���ַ���
				if(cplx1 == null){
					cplx1 = "";
				}
				
				//�±�ṹ
				sql = " update SB set cphm1 = '" + cphm1 + "', cplx1 = '" + cplx1 + "' where tpid1 = '" + tpid1 + "'";
				//�ɱ��ʽ
//				sql = " update SB set cphid='"+cphm1+"',cplx='"+cplx1+"' where tpid1='"+tpid1+"'";
				
				st = connection.createStatement();// �������ݿ�
			    st.execute(sql); // ���¼�¼
			    return "1";
			}else{
				return "0";
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception(""+e.getMessage());
		} 
		finally {
			if(st!=null){
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(connection!=null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
}