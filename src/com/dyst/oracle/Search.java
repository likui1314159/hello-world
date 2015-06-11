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
 * 实现Oralce查询
 * @author likui
 */
public class Search {	
	/**
	 *查询类型1
	 * 功能描述: 查询特定车牌类型的历史轨迹<br>
	 * sql:查询语句   <br>
	 * from 起始查询记录<br>
	 * pagsize:每页显示记录数<br>
	 * sort : 排序字段<br>
	 * sortType 排序类型<br>
	 * @throws SQLException <br>
	 * @return 识别记录对象列表
	 */	
	public List<Sbnew> TDCPGJCX(String sql,int from ,int pagsize,String sort,String sortType) throws SQLException{	
		//连接数据库
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
//		System.out.println(sql);
		String orderStr="";//排序字段与类型
		try {
			QueryRunner qr = new QueryRunner();
			//oracle不能 分页查询，同时排序，除非使用再嵌入一层的查询方式. 单数据量大时效率较低
			//另一种解决方法，对排序的字段进行主键或者索引，即可先按该字段排序。然后再rownum,方法不变
			//因识别表时用tpid1做主键，且以通过时间为生成序列，故在此使用tpid1进行排序
			if(!"".equals(sort.trim())&&!"".equals(sortType)){//生成排序sql
				orderStr = " order by tpid1  "+sortType ;
			}
			
			if(pagsize != 0){//如果起始查询记录和每页显示记录数不为0，分页查询
				 sql = "SELECT * FROM (" + sql + " and ROWNUM <= " + (from + pagsize) + orderStr +") WHERE rn > " + from;
			}else{
				sql+=orderStr;
			}
			
			//执行查询
		    List<Sbnew> listTxsj = new ArrayList<Sbnew>();
			listTxsj = (List<Sbnew>)qr.query(connection, sql, new BeanListHandler<Sbnew>(Sbnew.class));
			//分页查询
//			System.out.println(listTxsj.size() + "--Oracle数据库查询总数");
            return listTxsj;		
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Oracle数据库查询异常");
		}finally{
			if(connection!=null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 *31一张表实现方式
	 * 功能描述: 查询特定车牌类型的历史轨迹<br>
	 * sql:查询语句   <br>
	 * from 起始查询记录<br>
	 * pagsize:每页显示记录数<br>
	 * @throws SQLException <br>
	 * @return 识别记录对象列表
	 */	
	public List<Txsjcx> TDCPGJCX31(String sql,int from ,int pagsize) throws SQLException{	
//		Date date1 = new Date();
		//连接数据库
//		DateConnection conn = new DateConnection();//获取数据库连接
//		Connection connection = conn.getconnect();
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
		try {
			QueryRunner qr = new QueryRunner();
			//如果起始查询记录和每页显示记录数不为0，分页查询
			if(pagsize != 0){
				 sql = "select * FROM ( select cphid, jcdid, cplx, sbsj, cdid, tpid1,ROWNUM rn from ("
					 + sql + ") where ROWNUM<=" + (from + pagsize) + ")  WHERE rn >= " + from;//如果改为1张表实现，需要修改方法和实体类。
			}
		    
//			System.out.println(sql);
			
			//执行查询
		    List<Txsjcx> listTxsj = new ArrayList<Txsjcx>();
			listTxsj = (List<Txsjcx>)qr.query(connection, sql, new BeanListHandler<Txsjcx>(Txsjcx.class));
			
//			//部署后删除	
//			Date date2 = new Date();
//			double d = (date2.getTime()-date1.getTime());
//			System.out.println("查询Oracle数据库耗时："+d/1000+"秒");
			
			//分页查询
            return listTxsj;		
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Oracle数据库查询异常");
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 * 功能描述: 按照指定sql语句返回查询记录总数
	 * @throws SQLException 
	 */	
	public Integer getTDCPGJCXCount(String sql) throws SQLException{	
		Date date1 = new Date();
		//连接数据库
//		DateConnection conn = new DateConnection();//获取数据库连接
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
						Long len = rs.getLong(1);//或者rs.getLong("count")
//						System.out.println(len+"--Oracle数据库查询总数");
						return len;
					}
					return 0L;
				}
			});
			
//			//部署后删除	
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("查询Oracle数据库记录总数耗时："+d/1000+"秒");
//		    System.out.println(sql);
			return count.intValue();
		} catch (Exception e) {
			throw new SQLException("Oracle数据库查询异常");
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);//归还连接池连接
			}
		}
	}
	
	/**
	 * 更新Oracle识别记录
	 * tpid1 图片id
	 * cphm1 修改后的车牌号码
	 * cplx1 修改后的车牌类型
	 */
	public String updateOracleSb(String tpid1,String cphm1,String cplx1) throws Exception {
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		Connection connection = dbCon.getConnection("db");
		Statement st = null;
		String sql = "";
		try {
			//车牌号码及图片id不能为空
			if(tpid1 != null && !"".equals(tpid1.trim()) 
					&& cphm1 != null && !"".equals(cphm1.trim())){
				//如果车牌类型null，则置为空字符串
				if(cplx1 == null){
					cplx1 = "";
				}
				
				//新表结构
				sql = " update SB set cphm1 = '" + cphm1 + "', cplx1 = '" + cplx1 + "' where tpid1 = '" + tpid1 + "'";
				//旧表格式
//				sql = " update SB set cphid='"+cphm1+"',cplx='"+cplx1+"' where tpid1='"+tpid1+"'";
				
				st = connection.createStatement();// 操作数据库
			    st.execute(sql); // 更新记录
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