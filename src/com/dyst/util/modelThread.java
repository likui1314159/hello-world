package com.dyst.util;

import java.util.concurrent.CountDownLatch;

/**
 * ͼƬ��ַ��ѯ�߳���
 * @author Administrator
 *
 */
public class modelThread implements Runnable {
	private CountDownLatch threadsSignal1;
	private String localPath;
	public modelThread(CountDownLatch threadsSignal1,String localPath) {
		this.threadsSignal1 = threadsSignal1;
		this.localPath = localPath;
	}
	@SuppressWarnings("static-access")
	public synchronized  void  run() {
		try {
//			picPath =
			if(CopyPicFromServer.exists(localPath)){
				threadsSignal1.countDown();// �̼߳�������1,ִ�����������
			}else{
				threadsSignal1.countDown();
			}
			
		} catch (Exception e) {
			threadsSignal1.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
