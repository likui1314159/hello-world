package com.dyst.oracle;

import java.sql.*;    
import java.text.SimpleDateFormat;
import java.util.*;    
import java.util.Date;    

import com.dyst.util.Config;
import com.dyst.util.StringUtil;
   
/**   
 * 管理类DBConnectionManager支持对一个或多个由属性文件定义的数据库连接池的   
 * 访问.客户程序可以调用getInstance()方法访问本类的唯一实例   
 */   
@SuppressWarnings("unchecked")
public class DBConnectionManager {    
        
    public static void main(String[] args) {    
//        DBConnectionManager connectionManager = DBConnectionManager.getInstance();    
    }    
        
    static private DBConnectionManager instance; // 唯一实例    
    static private int clients; //  客户数量  
	private Vector drivers = new Vector();    
    
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private Config config = Config.getInstance();///获取配置文件信息
    String logFolder = config.getLogFolder();//日志文件存储路径
    String maxCon = config.getDbMaxCon();//连接池最大允许连接数
    int initCon = Integer.parseInt(config.getDbInit());//初始化连接数
    int timeOut = Integer.parseInt(config.getDbtimeOut());//数据库连接超时时间，单位：毫秒
    
   
    private Hashtable pools = new Hashtable();    
   
    /**   
     * 返回唯一实例.如果是第一次调用此方法,则创建实例   
     *    
     * @return DBConnectionManager 唯一实例   
     */   
    static synchronized public DBConnectionManager getInstance() {    
        if (instance == null) {    
            instance = new DBConnectionManager();    
        }    
        clients++;    
        return instance;    
    }    
   
    /**   
     * 建构私有函数以防止其它对象创建本类实例   
     */   
    private DBConnectionManager() {    
        init();    
    }    
   
    /**   
     * 将连接对象返回给由名字指定的连接池   
     *    
     * @param name   
     *            在属性文件中定义的连接池名字   
     * @param con   
     *            连接对象   
     */   
    public void freeConnection(String name, Connection con) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            pool.freeConnection(con);    
        }    
    }    
   
    /**   
     * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数 限制,则创建并返回新连接   
     *    
     * @param name   
     *            在属性文件中定义的连接池名字   
     * @return Connection 可用连接或null   
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
     * 获得一个可用连接.若没有可用连接,且已有连接数小于最大连接数限制, 则创建并返回新连接.否则,在指定的时间内等待其它线程释放连接.   
     *    
     * @param name   
     *            连接池名字   
     * @param time   
     *            以毫秒计的等待时间   
     * @return Connection 可用连接或null   
     */   
    public Connection getConnection(String name, long time) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection(time);    
        }    
        return null;    
    }    
   
    /**   
     * 关闭所有连接,撤销驱动程序的注册   
     */   
    public synchronized void release() {    
        // 等待直到最后一个客户程序调用    
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
                StringUtil.writerTXT(logFolder, "撤销JDBC驱动程序 " + driver.getClass().getName() + "的注册");
            } catch (SQLException e) {    
            	StringUtil.writerTXT(logFolder, "无法撤销下列JDBC驱动程序的注册: " + driver.getClass().getName()+e);
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
     * 读取属性完成初始化   
     */   
    private void init() {    
    	
        loadDrivers();    
        DBConnectionPool pool = null;
        try {    
      	    pool = new DBConnectionPool("db", config.getUrl(),    
                     config.getUser(),config.getPassword(),Integer.parseInt(maxCon));    
            pools.put("db", pool);    
            StringUtil.writerTXT(logFolder, "创建连接池DB成功");
      } catch (Exception e) {    
      	StringUtil.writerTXT(logFolder, "创建连接池DB失败"+e);
      }    
        createConn(pool);
    }    
   
    /**   
     * 装载和注册所有JDBC驱动程序   
     * @param props   
     *            属性   
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
                StringUtil.writerTXT(logFolder,"成功注册JDBC驱动程序" + driverClassName);    
            } catch (Exception e) {    
            	e.printStackTrace();
            	StringUtil.writerTXT(logFolder,"无法注册JDBC驱动程序: " + driverClassName + ", 错误: " + e);    
            }    
        }    
    }    
   
    /** ************************************************************* */   
    /** ********************内部类DBConnectionPool******************** */   
    /** ************************************************************* */   
    /**   
     * 此内部类定义了一个连接池.它能够根据要求创建新连接,直到预定的最 大连接数为止.
     * 在返回连接给客户程序之前,它能够验证连接的有效性.   
     */   
    class DBConnectionPool {    
        private int checkedOut; // 当前连接数    
   
        private Vector freeConnections = new Vector(); // 保存所有可用连接    
   
        private int maxConn; // 此连接池允许建立的最大连接数    
   
        private String name; // 连接池名字    
   
        private String password; // 密码或null    
   
        private String URL; // 数据库的JDBC URL    
   
        private String user; // 数据库账号或null    
   
        /**   
         * 创建新的连接池   
         *    
         * @param name   
         *            连接池名字   
         * @param URL   
         *            数据库的JDBC URL   
         * @param user   
         *            数据库帐号或 null   
         * @param password   
         *            密码或 null   
         * @param maxConn   
         *            此连接池允许建立的最大连接数   
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
         * 将不再使用的连接返回给连接池   
         *    
         * @param con   
         *            客户程序释放的连接   
         */   
        public synchronized void freeConnection(Connection con) {    
            // 将指定连接加入到向量末尾    
            freeConnections.addElement(con);    
            checkedOut--;    
            notifyAll(); // 删除等待队列中的所有线程    
        }    
   
        /**   
         * 从连接池获得一个可用连接.如果没有空闲的连接且当前连接数小于最大连接   
         * 数限制,则创建新连接.如原来登记为可用的连接不再有效,则从向量删除之, 然后递归调用自己以尝试新的可用连接.   
         */   
        public synchronized Connection getConnection() {    
            Connection con = null;    
//            System.out.println(" oracle_freeConnections.size(线程池现有连接)"+freeConnections.size());    
            if (freeConnections.size() > 0) {    
                // 获取向量中第一个可用连接    
                con = (Connection) freeConnections.firstElement();    
                freeConnections.removeElementAt(0);    
                try {    
                    if (con.isClosed()) {    
                    	StringUtil.writerTXT(logFolder,"从连接池" + name + "删除一个无效连接" );
                        // 递归调用自己,尝试再次获取可用连接    
                        con = getConnection(timeOut);    
                    }    
                } catch (SQLException e) {    
                	StringUtil.writerTXT(logFolder,"从连接池" + name + "删除一个无效连接"+e ); 
                    // 递归调用自己,尝试再次获取可用连接    
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
         * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法.   
         *    
         * @param timeout   
         *            以毫秒计的等待时间限制   
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
         * 关闭所有连接   
         */   
        public synchronized void release() {    
            Enumeration allConnections = freeConnections.elements();    
            while (allConnections.hasMoreElements()) {    
                Connection con = (Connection) allConnections.nextElement();    
                try {    
                    con.close();    
                    StringUtil.writerTXT(logFolder, "关闭连接池" + name + "中的一个连接");
                } catch (SQLException e) {    
                	  StringUtil.writerTXT(logFolder,  "无法关闭连接池" + name + "中的连接");
                }    
            }    
            freeConnections.removeAllElements();    
        }    
   
        /**   
         * 创建新的连接   
         */   
        private Connection newConnection() {    
            Connection con = null;    
            try {    
                if (user == null) {    
                    con = DriverManager.getConnection(URL);    
                } else {    
                    con = DriverManager.getConnection(URL, user, password);    
                }    
                StringUtil.writerTXT(logFolder, "连接池" + name + "创建一个新的连接");
            } catch (SQLException e) {    
            	StringUtil.writerTXT(logFolder, e+"无法创建下列URL的连接: " + URL);
                return null;    
            }    
            return con;    
        }    
    }    
}    
