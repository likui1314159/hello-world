package com.dyst.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.dyst.util.StringUtil;

public class ESClient {

	private static   ESClient inStance = new ESClient();

	 private ESClient() {
		 System.out.println("��ʼ��----------");
//	    client=getclient();
	 }

	public static ESClient getInstance() {
//		if (inStance == null) {
//			inStance = new ESClient();
//			System.out.println("ESCLIENTʵ����  ����������������������������������������-");
//		}
		return inStance;
	}

	// Node node = nodeBuilder().node();
	public final Client client = getclient();

	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//
	public  Client getclient() {
		// return getTransclient();
		//Web ������Ҫ�뼯Ⱥ������ͬһ�����С�
		// Node node =
		// NodeBuilder.nodeBuilder().clusterName("elasticsearch").client(true).node();
		// return node.client();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
//		if (client == null) {
			TransportClient clientTran = new TransportClient();
			try {
				// ����Ӷ����ַ
				clientTran
				 .addTransportAddresses(
						 new InetSocketTransportAddress("100.100.21.114",
						 9300));
//						 .addTransportAddresses(
//						 new InetSocketTransportAddress(
//						 "100.100.21.115", 9300))
//						 .addTransportAddresses(
//						 new InetSocketTransportAddress(
//						 "100.100.21.116", 9300))
//						 .addTransportAddresses(
//						 new InetSocketTransportAddress(
//						 "100.100.21.117", 9300))
//						.addTransportAddresses(new InetSocketTransportAddress(
//								"100.100.21.118", 9300));
				Date date2 = new Date();
				double d = (date2.getTime() - date1.getTime());
				System.out.println("��ȡ���Ӻ�ʱ��" + d / 1000 + "��");
//				client = clientTran;
				// return client;
			} catch (Exception e) {
				StringUtil.writerTXT("�쳣��Ϣʱ�䣺" + sdf.format(new Date())
						+ "�쳣���ݣ�" + e.getMessage());
				if (client != null) {
					client.close();
				}
				return null;
			}
//		}
		return clientTran;
	}
}
// public TransportClient getTransPortClient() {
// Settings settings = ImmutableSettings.settingsBuilder().put(
// "cluster.name", "elasticsearch")// ��Ⱥ��
// .put("client.transport.sniff", true)// �Զ��Ѽ�Ⱥ�µĻ�����ӵ��б���
// .put("client.transport.ignore_cluster_name", true)// ���Ӽ�Ⱥ��
// .put("cluster.transport.nodes_sampler_interval", "10s")// �ڵ�֮��10s���һ�Σ�ͨ��ping
// // .put("index.translog.flush_threshold_ops", "100000")
// // .put("index.merge.policy.merge_factor", "1000")
// // .put("index.refresh_interval:", "-1")
// .build();
// TransportClient tc = new TransportClient(settings);
// try {
// tc.addTransportAddress(new InetSocketTransportAddress(
// "100.100.21.114", 9300));
// tc.addTransportAddress(new InetSocketTransportAddress(
// "100.100.21.115", 9300));
// tc.addTransportAddress(new InetSocketTransportAddress(
// "100.100.21.116", 9300));
// tc.addTransportAddress(new InetSocketTransportAddress(
// "100.100.21.117", 9300));
// tc.addTransportAddress(new InetSocketTransportAddress(
// "100.100.21.118", 9300));
// return tc;
// } catch (Exception e) {
// StringUtil.writerTXT("�쳣��Ϣʱ�䣺" + sdf.format(new Date()) + "�쳣���ݣ�"
// + e.getMessage());
// if (tc != null) {
// tc.close();
// }
// return null;
// }
// }

