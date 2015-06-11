package com.dyst.util;

import java.util.concurrent.CountDownLatch;
/**
 * ͼƬ��ַ��ѯ�߳���
 * @author Administrator
 *
 */
public class storageThread extends Thread {
	private CountDownLatch threadsSignal;//�߳�ͬ��
	private String localPath;//����·�����ҵ�ͼƬ�󣬸��Ƶ���·����
	private String smbPath;//����·��
	
	public storageThread(CountDownLatch threadsSignal,String localPath,String smbPath) {
		this.threadsSignal = threadsSignal;
		this.localPath = localPath;
		this.smbPath = smbPath;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void  run() {
		try {
			//���ͼƬ���ڣ����Ƶ�������
			if(CopyPicFromServer.exists(smbPath)){
				CopyPicFromServer.copyImage(smbPath, localPath);
//				threadsSignal.countDown();// �̼߳�������1,ִ�����������
				while(threadsSignal.getCount()>0){
					threadsSignal.countDown();// �̼߳�������1,ִ�����������
				}
			}else{
				threadsSignal.countDown();
			}
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
