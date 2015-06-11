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
 * ������DBConnectionManager֧�ֶ�һ�������������ļ���������ݿ����ӳص�   
 * ����.�ͻ�������Ե���getInstance()�������ʱ����Ψһʵ��   
 */   
@SuppressWarnings("unchecked")
public class ESClientManager {    
        
    public static void main(String[] args) {    
//        ESClientManager connectionManager = ESClientManager.getInstance();    
//        System.out.println(connectionManager.getConnection("es"));
    }    
        
    static private ESClientManager instance; // Ψһʵ��   �����õ���ģʽ����ϵͳ����ʱ����
    static private int clients; //  �ͻ����� ��Ŀǰ����ʹ�õ�������
	private Hashtable pools = new Hashtable(); //����������ӳص�hash���ϣ����԰���������ӳأ���ϵͳֻ��һ��
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Config config = Config.getInstance();///��ȡ�����ļ���Ϣ
    String  logFolder = config.getLogFolder();//��־�ļ��洢·��
    String serverIp = config.getServerIp();//ES���ݿ⼯ȺIP���ϣ��԰�Ƕ��ŷָ�
    String maxConnect = config.getMaxConnection();//���ӳ��������������
    int initCon = Integer.parseInt(config.getInitConnection());//��ʼ��������
    String timeOut = config.getTimeOut();//���ݿ����ӳ�ʱʱ�䣬��λ������
    /**   
     * ����Ψһʵ��.����ǵ�һ�ε��ô˷���,�򴴽�ʵ��   
     * @return DBConnectionManager Ψһʵ��   
     */   
    static synchronized public ESClientManager getInstance() {    
    	try {
    		 if (instance == null) {    
    	            instance = new ESClientManager();    
    	        }    
    	        clients++;    
    	        return instance;
		} catch (Exception e) {
			StringUtil.writerTXT("/home/esLog/dyst/hh", "ES���ݿ����ӳس�ʼ���쳣"+e.getMessage());
			e.printStackTrace();
		}
         return null;
    }    
   
    /**   
     * ����˽�к����Է�ֹ�������󴴽�����ʵ��   
     */   
    private ESClientManager() {    
        init();//��ʼ������
    }    
   
    /**   
     * �����Ӷ��󷵻ظ�������ָ�������ӳ�   
     * @param name�� �������ļ��ж�������ӳ�����   
     * @param con:  ���Ӷ���   
     */   
    public void freeConnection(String name, Client con) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            pool.freeConnection(con);   
        }    
    }    
   
    /**   
     * ���һ�����õ�(���е�)����.���û�п�������,������������С����������� ����,�򴴽�������������  <br>
     * @param name: �������ļ��ж�������ӳ�����    <br>
     * @return Client �������ӻ�null    <br>
     */   
    public Client getConnection(String name) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection();    
        }    
        return null;    
    }    
   
    /**   
     * ���һ����������.��û�п�������,������������С���������������, �򴴽�������������.����,��ָ����ʱ���ڵȴ������߳��ͷ�����.    <br>
     * @param name   
     *            ���ӳ�����    <br>
     * @param time   
     *            �Ժ���Ƶĵȴ�ʱ��    <br>
     * @return Client �������ӻ�null   
     */   
    public Client getConnection(String name, long time) {    
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);    
        if (pool != null) {    
            return pool.getConnection(time);    
        }    
        return null;    
    }    
   
    /**   
     * �ر���������
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
    }    
   /**
    * ���ݸ�����ʼֵ��������
    * @param pool ���ӳض���
    */
    private void createConn(DBConnectionPool pool ){
    	try {
			for(int i=0;i<initCon;i++){
				Client conn =  pool.newConnection(); 
//				System.out.println(conn+"----------------------------------------");
				freeConnection("es", conn);   
				
			}
			StringUtil.writerTXT(logFolder, "���ES���ݿ����ӳس�ʼ��");
		} catch (Exception e) {
			e.printStackTrace();
			StringUtil.writerTXT(logFolder, "����ES���ӳس����쳣"+e.getMessage());
		}
    }
    // --------------------------------------------------------------------------------    
    /**   
     * ��ȡ������ɳ�ʼ��   
     */   
    private void init() {    
    	 DBConnectionPool pool = new DBConnectionPool(serverIp,Integer.parseInt(maxConnect),"es",Integer.parseInt(timeOut));    
    	 pools.put("es", pool);    
    	 StringUtil.writerTXT(logFolder, "�������ӳسɹ���");
         createConn(pool);
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
        private String serverIp; // ���ݿ⼯ȺIp
        @SuppressWarnings("unused")
		private int timeOut=10000;
        /**   
         * �����µ����ӳ�   
         * @param serverIp    
         *            ��������ȺIp   
         * @param maxConn   
         *            �����ӳ������������������   
         */   
        public DBConnectionPool(String serverIp, int maxConn,String name,int timeOut) {    
            this.serverIp = serverIp;    
            this.maxConn = maxConn;
            this.name = name;
            this.timeOut = timeOut;
        }    
   
        /**   
         * ������ʹ�õ����ӷ��ظ����ӳ�   
         * @param con   
         *            �ͻ������ͷŵ�����   
         */   
        public synchronized void freeConnection(Client con) {    
            // ��ָ�����Ӽ��뵽����ĩβ    
            freeConnections.addElement(con);    
            checkedOut--; 
//            System.out.println("ES �ͷ����ӣ�����������:"+freeConnections.size());
            notifyAll(); // ɾ���ȴ������е������߳�    
        }    
        /**   
         * �����ӳػ��һ����������.���û�п��е������ҵ�ǰ������С���������   
         * ������,�򴴽�������.��ԭ���Ǽ�Ϊ���õ����Ӳ�����Ч,�������ɾ��֮, Ȼ��ݹ�����Լ��Գ����µĿ�������.   
         */   
        public synchronized Client getConnection() {    
            Client con = null;    
//            System.out.println("ES ��ȡ���� ��������������"+freeConnections.size());    
            if (freeConnections.size() > 0) {    
                try {    
                	// ��ȡ�����е�һ����������    
                    con = (Client) freeConnections.firstElement();    
                    freeConnections.removeElementAt(0);
//                  con = getConnection();    
                } catch (Exception e) {    
                    StringUtil.writerTXT(logFolder, "�����ӳ�" + name + "ɾ��һ����Ч����"+",�쳣��Ϣ��"+e.getMessage());
                    // �ݹ�����Լ�,�����ٴλ�ȡ��������    
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
         * �����ӳػ�ȡ��������.����ָ���ͻ������ܹ��ȴ����ʱ�� �μ�ǰһ��getConnection()����.   
         *    
         * @param timeout   
         *            �Ժ���Ƶĵȴ�ʱ������   
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
                    // wait()���ص�ԭ���ǳ�ʱ    
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
            	Client con = (Client) allConnections.nextElement();    
                try {    
                    con.close();    
                    StringUtil.writerTXT(logFolder, "�ر����ӳ�" + name + "�е�һ������");
                } catch (Exception e) {    
                	StringUtil.writerTXT(logFolder, "�޷��ر����ӳ�" + name + "�е�����"+",�쳣��Ϣ��"+e.getMessage());
                }    
            }    
            freeConnections.removeAllElements();    
        }    
        /**   
         * �����µ�����   
         */   
        private Client newConnection() {    
//        	NetworkUtils bb = new NetworkUtils();
//        	System.out.println("----------+++++++++++++++++++++++++++++++");
        	/*
        	 * ���������client.transport.sniffΪtrue��ʹ�ͻ���ȥ��̽������Ⱥ��״̬��
        	 * �Ѽ�Ⱥ������������ip��ַ�ӵ��ͻ����У��������ĺô���һ
        	 * ���㲻���ֶ����ü�Ⱥ�����м�Ⱥ��ip�����ӿͻ��ˣ������Զ�������ӣ�
        	 * �����Զ������¼��뼯Ⱥ�Ļ�����
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
        			 StringUtil.writerTXT(logFolder, "����ES��ȺIP�������飡");
        		 }
//        		 System.out.println(clientTran+"+++++++++++++++++++++++++++++++++++++++");
			} catch (Exception e) {
				StringUtil.writerTXT(logFolder, "����ES���ӳ����쳣��");
			}
			return clientTran;
        }    
    }    
}    