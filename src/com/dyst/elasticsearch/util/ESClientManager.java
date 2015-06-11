package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.*;    

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.dyst.util.Config;
import com.dyst.util.StringUtil;
   
/**   
 * 管理类DBConnectionManager支持对一个或多个由属性文件定义的数据库连接池的   
 * 访问.客户程序可以调用getInstance()方法访问本类的唯一实例   
 */   
@SuppressWarnings("unchecked")
public class ESClientManager {    
        
    public static void main(String[] args) {    
//        ESClientManager connectionManager = ESClientManager.getInstance();    
//        System.out.println(connectionManager.getConnection("es"));
    }    
        
    static private ESClientManager instance; // 唯一实例   ，采用单例模式，在系统启动时加载
    static private int clients; //  客户数量 ，目前正在使用的连接数
	private Hashtable pools = new Hashtable(); //用来存放连接池的hash集合，可以包含多个连接池，本系统只有一个
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Config config = Config.getInstance();///获取配置文件信息
    String  logFolder = config.getLogFolder();//日志文件存储路径
    String serverIp = config.getServerIp();//ES数据库集群IP集合，以半角逗号分隔
    String maxConnect = config.getMaxConnection();//连接池最大允许连接数
    int initCon = Integer.parseInt(config.getInitConnection());//初始化连接数
    String timeOut = config.getTimeOut();//数据库连接超时时间，单位：毫秒
    /**   
     * 返回唯一实例.如果是第一次调用此方法,则创建实例   
     * @return DBConnectionManager 唯一实例   
     */   
    static synchronized public ESClientManager getInstance() {    
    	try {
    		 if (instance == null) {    
    	            instance = new ESClientManager();    
    	        }    
    	        clients++;    
    	        return instance;
		} catch (Exception e) {
			StringUtil.writerTXT("/home/esLog/dyst/hh", "ES数据库连接池初始化异常"+e.getMessage());
			e.printStackTrace();
		}
         return null;
    }    
   
    /**   
     * 建构私有函数以防止其它对象创建本类实例   
     */   
    private ESClientManager() {    
        init();//初始化函数
    }    
   
    /**   
     * 将连接对象返回给由名字指定的连接池   
     * @param name： 在属性文件中定义的连接池名字   
     * @param con:  连接对象   
     */   
    public void freeConnection(String name, Client con) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            pool.freeConnection(con);   
        }    
    }    
   
    /**   
     * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数 限制,则创建并返回新连接  <br>
     * @param name: 在属性文件中定义的连接池名字    <br>
     * @return Client 可用连接或null    <br>
     */   
    public Client getConnection(String name) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection();    
        }    
        return null;    
    }    
   
    /**   
     * 获得一个可用连接.若没有可用连接,且已有连接数小于最大连接数限制, 则创建并返回新连接.否则,在指定的时间内等待其它线程释放连接.    <br>
     * @param name   
     *            连接池名字    <br>
     * @param time   
     *            以毫秒计的等待时间    <br>
     * @return Client 可用连接或null   
     */   
    public Client getConnection(String name, long time) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection(time);    
        }    
        return null;    
    }    
   
    /**   
     * 关闭所有连接
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
    }    
   /**
    * 根据给定初始值创建连接
    * @param pool 连接池对象
    */
    private void createConn(DBConnectionPool pool ){
    	try {
			for(int i=0;i<initCon;i++){
				Client conn =  pool.newConnection(); 
//				System.out.println(conn+"----------------------------------------");
				freeConnection("es", conn);   
				
			}
			StringUtil.writerTXT(logFolder, "完成ES数据库连接池初始化");
		} catch (Exception e) {
			e.printStackTrace();
			StringUtil.writerTXT(logFolder, "创建ES连接池出现异常"+e.getMessage());
		}
    }
    // --------------------------------------------------------------------------------    
    /**   
     * 读取属性完成初始化   
     */   
    private void init() {    
    	 DBConnectionPool pool = new DBConnectionPool(serverIp,Integer.parseInt(maxConnect),"es",Integer.parseInt(timeOut));    
    	 pools.put("es", pool);    
    	 StringUtil.writerTXT(logFolder, "创建连接池成功！");
         createConn(pool);
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
        private String serverIp; // 数据库集群Ip
        @SuppressWarnings("unused")
		private int timeOut=10000;
        /**   
         * 创建新的连接池   
         * @param serverIp    
         *            服务器集群Ip   
         * @param maxConn   
         *            此连接池允许建立的最大连接数   
         */   
        public DBConnectionPool(String serverIp, int maxConn,String name,int timeOut) {    
            this.serverIp = serverIp;    
            this.maxConn = maxConn;
            this.name = name;
            this.timeOut = timeOut;
        }    
   
        /**   
         * 将不再使用的连接返回给连接池   
         * @param con   
         *            客户程序释放的连接   
         */   
        public synchronized void freeConnection(Client con) {    
            // 将指定连接加入到向量末尾    
            freeConnections.addElement(con);    
            checkedOut--; 
//            System.out.println("ES 释放连接，现有链接数:"+freeConnections.size());
            notifyAll(); // 删除等待队列中的所有线程    
        }    
        /**   
         * 从连接池获得一个可用连接.如果没有空闲的连接且当前连接数小于最大连接   
         * 数限制,则创建新连接.如原来登记为可用的连接不再有效,则从向量删除之, 然后递归调用自己以尝试新的可用连接.   
         */   
        public synchronized Client getConnection() {    
            Client con = null;    
//            System.out.println("ES 获取连接 ，现有连接数："+freeConnections.size());    
            if (freeConnections.size() > 0) {    
                try {    
                	// 获取向量中第一个可用连接    
                    con = (Client) freeConnections.firstElement();    
                    freeConnections.removeElementAt(0);
//                  con = getConnection();    
                } catch (Exception e) {    
                    StringUtil.writerTXT(logFolder, "从连接池" + name + "删除一个无效连接"+",异常信息："+e.getMessage());
                    // 递归调用自己,尝试再次获取可用连接    
                    con = getConnection(10000);    
                }    
            } else if (maxConn == 0 || checkedOut < maxConn) {    
                con = newConnection();    
            }    
            if (con != null) {    
                checkedOut++;    
            }    
//          System.out.println("con == "+con);    
            return con;    
        }    
   
        /**   
         * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法.   
         *    
         * @param timeout   
         *            以毫秒计的等待时间限制   
         */   
        public synchronized Client getConnection(long timeout) {    
            long startTime = new Date().getTime();    
            Client con;    
            while ((con = getConnection()) == null) {    
                try {    
                    wait(timeout);    
                } catch (InterruptedException e) {    
                }    
                if ((new Date().getTime() - startTime) >= timeout) {    
                    // wait()返回的原因是超时    
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
            	Client con = (Client) allConnections.nextElement();    
                try {    
                    con.close();    
                    StringUtil.writerTXT(logFolder, "关闭连接池" + name + "中的一个连接");
                } catch (Exception e) {    
                	StringUtil.writerTXT(logFolder, "无法关闭连接池" + name + "中的连接"+",异常信息："+e.getMessage());
                }    
            }    
            freeConnections.removeAllElements();    
        }    
        /**   
         * 创建新的连接   
         */   
        private Client newConnection() {    
//        	NetworkUtils bb = new NetworkUtils();
//        	System.out.println("----------+++++++++++++++++++++++++++++++");
        	/*
        	 * 你可以设置client.transport.sniff为true来使客户端去嗅探整个集群的状态，
        	 * 把集群中其它机器的ip地址加到客户端中，这样做的好处是一
        	 * 般你不用手动设置集群里所有集群的ip到连接客户端，它会自动帮你添加，
        	 * 并且自动发现新加入集群的机器。
        	 */
        	Settings setting = ImmutableSettings.settingsBuilder()
//        	.put("cluster.name","elasticsearch")
        	.put("client.transport.sniff", true)
        	.build();
        	TransportClient clientTran = new TransportClient();
//        	org.elasticsearch.client.transport.TransportClient
//        	org.elasticsearch.client.transport.TransportClient.TransportClient(Settings settings)
        	
        	try {
        		
        		 String strIp[] = serverIp.split(",");
        		 if(strIp!=null&&strIp.length>0){
        			 for(int i=0;i<strIp.length;i++){
            			 clientTran.addTransportAddresses(
        				  new InetSocketTransportAddress(strIp[i],9300));
            		 }
//        			 System.out.println(clientTran+"");
        		 }else{
        			 StringUtil.writerTXT(logFolder, "给定ES集群IP错误，请检查！");
        		 }
//        		 System.out.println(clientTran+"+++++++++++++++++++++++++++++++++++++++");
			} catch (Exception e) {
				StringUtil.writerTXT(logFolder, "创建ES连接出现异常！");
			}
			return clientTran;
        }    
    }    
}    