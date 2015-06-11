package com.dyst.util;

import java.util.concurrent.CountDownLatch;
/**
 * 图片地址查询线程类
 * @author Administrator
 *
 */
public class storageThread extends Thread {
	private CountDownLatch threadsSignal;//线程同步
	private String localPath;//缓存路径，找到图片后，复制到该路径下
	private String smbPath;//查找路径
	
	public storageThread(CountDownLatch threadsSignal,String localPath,String smbPath) {
		this.threadsSignal = threadsSignal;
		this.localPath = localPath;
		this.smbPath = smbPath;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void  run() {
		try {
			//如果图片存在，则复制到缓存中
			if(CopyPicFromServer.exists(smbPath)){
				CopyPicFromServer.copyImage(smbPath, localPath);
//				threadsSignal.countDown();// 线程计数器减1,执行完操作后在
				while(threadsSignal.getCount()>0){
					threadsSignal.countDown();// 线程计数器减1,执行完操作后在
				}
			}else{
				threadsSignal.countDown();
			}
		} catch (Exception e) {
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
