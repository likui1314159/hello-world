package com.dyst.oracle;

import java.sql.*;    
import java.text.SimpleDateFormat;
import java.util.*;    
import java.util.Date;    

import com.dyst.util.Config;
import com.dyst.util.StringUtil;
   
/**   
 * ������DBConnectionManager֧�ֶ�һ�������������ļ���������ݿ����ӳص�   
 * ����.�ͻ�������Ե���getInstance()�������ʱ����Ψһʵ��   
 */   
@SuppressWarnings("unchecked")
public class DBConnectionManager {    
        
    public static void main(String[] args) {    
//        DBConnectionManager connectionManager = DBConnectionManager.getInstance();    
    }    
        
    static private DBConnectionManager instance; // Ψһʵ��    
    static private int clients; //  �ͻ�����  
	private Vector drivers = new Vector();    
    
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private Config config = Config.getInstance();///��ȡ�����ļ���Ϣ
    String logFolder = config.getLogFolder();//��־�ļ��洢·��
    String maxCon = config.getDbMaxCon();//���ӳ��������������
    int initCon = Integer.parseInt(config.getDbInit());//��ʼ��������
    int timeOut = Integer.parseInt(config.getDbtimeOut());//���ݿ����ӳ�ʱʱ�䣬��λ������
    
   
    private Hashtable pools = new Hashtable();    
   
    /**   
     * ����Ψһʵ��.����ǵ�һ�ε��ô˷���,�򴴽�ʵ��   
     *    
     * @return DBConnectionManager Ψһʵ��   
     */   
    static synchronized public DBConnectionManager getInstance() {    
        if (instance == null) {    
            instance = new DBConnectionManager();    
        }    
        clients++;    
        return instance;    
    }    
   
    /**   
     * ����˽�к����Է�ֹ�������󴴽�����ʵ��   
     */   
    private DBConnectionManager() {    
        init();    
    }    
   
    /**   
     * �����Ӷ��󷵻ظ�������ָ�������ӳ�   
     *    
     * @param name   
     *            �������ļ��ж�������ӳ�����   
     * @param con   
     *            ���Ӷ���   
     */   
    public void freeConnection(String name, Connection con) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            pool.freeConnection(con);    
        }    
    }    
   
    /**   
     * ���һ�����õ�(���е�)����.���û�п�������,������������С����������� ����,�򴴽�������������   
     *    
     * @param name   
     *            �������ļ��ж�������ӳ�����   
     * @return Connection �������ӻ�null   
     */   
    public Connection getConnection(String name) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        //System.out.println(" pool == "+pool);    
        if (pool != null) {    
            return pool.getConnection();    
        }    
        return null;    
    }    
   
    /**   
     * ���һ����������.��û�п�������,������������С���������������, �򴴽�������������.����,��ָ����ʱ���ڵȴ������߳��ͷ�����.   
     *    
     * @param name   
     *            ���ӳ�����   
     * @param time   
     *            �Ժ���Ƶĵȴ�ʱ��   
     * @return Connection �������ӻ�null   
     */   
    public Connection getConnection(String name, long time) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection(time);    
        }    
        return null;    
    }    
   
    /**   
     * �ر���������,�������������ע��   
     */   
    public synchronized void release() {    
        // �ȴ�ֱ�����һ���ͻ��������    
        if (--clients != 0) {    
            return;    
        }    
        Enumeration allPools = pools.elements();    
        while (allPools.hasMoreElements()) {    
            DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();    
            pool.release();    
        }    
        Enumeration allDrivers = drivers.elements();    
        while (allDrivers.hasMoreElements()) {    
            Driver driver = (Driver) allDrivers.nextElement();    
            try {    
                DriverManager.deregisterDriver(driver);    
                StringUtil.writerTXT(logFolder, "����JDBC�������� " + driver.getClass().getName() + "��ע��");
            } catch (SQLException e) {    
            	StringUtil.writerTXT(logFolder, "�޷���������JDBC���������ע��: " + driver.getClass().getName()+e);
            }    
        }    
    }    
   
    private void createConn(DBConnectionPool pool ){
    	 Connection conn = null;
    	for(int i=0;i<initCon;i++){
    		conn = pool.newConnection();
    		freeConnection("db", conn);    
    	}
    }
    // --------------------------------------------------------------------------------    
   
    /**   
     * ��ȡ������ɳ�ʼ��   
     */   
    private void init() {    
    	
        loadDrivers();    
        DBConnectionPool pool = null;
        try {    
      	    pool = new DBConnectionPool("db", config.getUrl(),    
                     config.getUser(),config.getPassword(),Integer.parseInt(maxCon));    
            pools.put("db", pool);    
            StringUtil.writerTXT(logFolder, "�������ӳ�DB�ɹ�");
      } catch (Exception e) {    
      	StringUtil.writerTXT(logFolder, "�������ӳ�DBʧ��"+e);
      }    
        createConn(pool);
    }    
   
    /**   
     * װ�غ�ע������JDBC��������   
     * @param props   
     *            ����   
     */   
    private void loadDrivers() {    
        String driverClasses = config.getDriver();    
        StringTokenizer st = new StringTokenizer(driverClasses);    
        while (st.hasMoreElements()) {    
            String driverClassName = st.nextToken().trim();    
            try {    
                Driver driver = (Driver) Class.forName(driverClassName)    
                        .newInstance();    
                DriverManager.registerDriver(driver);    
                drivers.addElement(driver);    
                StringUtil.writerTXT(logFolder,"�ɹ�ע��JDBC��������" + driverClassName);    
            } catch (Exception e) {    
            	e.printStackTrace();
            	StringUtil.writerTXT(logFolder,"�޷�ע��JDBC��������: " + driverClassName + ", ����: " + e);    
            }    
        }    
    }    
   
    /** ************************************************************* */   
    /** ********************�ڲ���DBConnectionPool******************** */   
    /** ************************************************************* */   
    /**   
     * ���ڲ��ඨ����һ�����ӳ�.���ܹ�����Ҫ�󴴽�������,ֱ��Ԥ������ ��������Ϊֹ.
     * �ڷ������Ӹ��ͻ�����֮ǰ,���ܹ���֤���ӵ���Ч��.   
     */   
    class DBConnectionPool {    
        private int checkedOut; // ��ǰ������    
   
        private Vector freeConnections = new Vector(); // �������п�������    
   
        private int maxConn; // �����ӳ������������������    
   
        private String name; // ���ӳ�����    
   
        private String password; // �����null    
   
        private String URL; // ���ݿ��JDBC URL    
   
        private String user; // ���ݿ��˺Ż�null    
   
        /**   
         * �����µ����ӳ�   
         *    
         * @param name   
         *            ���ӳ�����   
         * @param URL   
         *            ���ݿ��JDBC URL   
         * @param user   
         *            ���ݿ��ʺŻ� null   
         * @param password   
         *            ����� null   
         * @param maxConn   
         *            �����ӳ������������������   
         */   
        public DBConnectionPool(String name, String URL, String user,    
                String password, int maxConn) {    
            this.name = name;    
            this.URL = URL;    
            this.user = user;    
            this.password = password;    
            this.maxConn = maxConn;
        }    
   
        /**   
         * ������ʹ�õ����ӷ��ظ����ӳ�   
         *    
         * @param con   
         *            �ͻ������ͷŵ�����   
         */   
        public synchronized void freeConnection(Connection con) {    
            // ��ָ�����Ӽ��뵽����ĩβ    
            freeConnections.addElement(con);    
            checkedOut--;    
            notifyAll(); // ɾ���ȴ������е������߳�    
        }    
   
        /**   
         * �����ӳػ��һ����������.���û�п��е������ҵ�ǰ������С���������   
         * ������,�򴴽�������.��ԭ���Ǽ�Ϊ���õ����Ӳ�����Ч,�������ɾ��֮, Ȼ��ݹ�����Լ��Գ����µĿ�������.   
         */   
        public synchronized Connection getConnection() {    
            Connection con = null;    
//            System.out.println(" oracle_freeConnections.size(�̳߳���������)"+freeConnections.size());    
            if (freeConnections.size() > 0) {    
                // ��ȡ�����е�һ����������    
                con = (Connection) freeConnections.firstElement();    
                freeConnections.removeElementAt(0);    
                try {    
                    if (con.isClosed()) {    
                    	StringUtil.writerTXT(logFolder,"�����ӳ�" + name + "ɾ��һ����Ч����" );
                        // �ݹ�����Լ�,�����ٴλ�ȡ��������    
                        con = getConnection(timeOut);    
                    }    
                } catch (SQLException e) {    
                	StringUtil.writerTXT(logFolder,"�����ӳ�" + name + "ɾ��һ����Ч����"+e ); 
                    // �ݹ�����Լ�,�����ٴλ�ȡ��������    
                    con = getConnection(timeOut);    
                }    
            } else if (maxConn == 0 || checkedOut < maxConn) {    
                con = newConnection();    
            }    
            if (con != null) {    
                checkedOut++;    
            }    
//            System.out.println("con == "+con);    
            return con;    
        }    
   
        /**   
         * �����ӳػ�ȡ��������.����ָ���ͻ������ܹ��ȴ����ʱ�� �μ�ǰһ��getConnection()����.   
         *    
         * @param timeout   
         *            �Ժ���Ƶĵȴ�ʱ������   
         */   
        public synchronized Connection getConnection(long timeout) {    
            long startTime = new Date().getTime();    
            Connection con;    
            while ((con = getConnection()) == null) {    
                try {    
                    wait(timeout);    
                } catch (InterruptedException e) {    
                }    
                if ((new Date().getTime() - startTime) >= timeout) {    
                    return null;    
                }    
            }    
            return con;    
        }    
   
        /**   
         * �ر���������   
         */   
        public synchronized void release() {    
            Enumeration allConnections = freeConnections.elements();    
            while (allConnections.hasMoreElements()) {    
                Connection con = (Connection) allConnections.nextElement();    
                try {    
                    con.close();    
                    StringUtil.writerTXT(logFolder, "�ر����ӳ�" + name + "�е�һ������");
                } catch (SQLException e) {    
                	  StringUtil.writerTXT(logFolder,  "�޷��ر����ӳ�" + name + "�е�����");
                }    
            }    
            freeConnections.removeAllElements();    
        }    
   
        /**   
         * �����µ�����   
         */   
        private Connection newConnection() {    
            Connection con = null;    
            try {    
                if (user == null) {    
                    con = DriverManager.getConnection(URL);    
                } else {    
                    con = DriverManager.getConnection(URL, user, password);    
                }    
                StringUtil.writerTXT(logFolder, "���ӳ�" + name + "����һ���µ�����");
            } catch (SQLException e) {    
            	StringUtil.writerTXT(logFolder, e+"�޷���������URL������: " + URL);
                return null;    
            }    
            return con;    
        }    
    }    
}    
