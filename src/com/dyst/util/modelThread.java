package com.dyst.util;

import java.util.concurrent.CountDownLatch;

/**
 * 图片地址查询线程类
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
				threadsSignal1.countDown();// 线程计数器减1,执行完操作后在
			}else{
				threadsSignal1.countDown();
			}
			
		} catch (Exception e) {
			threadsSignal1.countDown();// 线程计数器减1,执行完操作后在
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
