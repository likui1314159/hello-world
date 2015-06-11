package com.dyst.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.dyst.oracle.JcdOracle;

/**
 * 图片地址查询线程类
 * @author Administrator
 */
@SuppressWarnings("unchecked")
public class PicThread extends Thread {
	private CountDownLatch threadsSignal;//线程数
	private String pic = "";//图片id
	private List ListPic = new ArrayList();//存放结果
	private String flag = "";
	
	//构造方法
	public PicThread(CountDownLatch threadsSignal, String pic, String flag, List listPic) {
		this.threadsSignal = threadsSignal;//线程数
		this.pic = pic;//图片id
		this.ListPic = listPic;//存放结果
		this.flag = flag;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			String picURL = Config.getInstance().getPicURL();//图片调用前缀
			//如果是深圳交警图片调用,停车场的图片id需要另外调用一个借口
			String xtFlag = Config.getInstance().getSysFlag();
			
			//根据图片id形式，调取不同的方法
			if(pic != null && !"".equals(pic) && pic.length() >= 24){
				if(!"00000000000000000000000000000000000".equals(pic)){
					//获取监测点id
					String jcdid = pic.substring(16, 24);
					
					//-----图片id长度为27位--,,,,,,由于有几个点图片id出现异常的处理方法
					if(pic.length() == 27){
						jcdid = pic.substring(17, 25);//监测点id	
					}
					
					//根据不同系统及监测点类型调用
					if("1".equals(xtFlag) && !jcdid.contains("A")){//深圳
						//调取图片
						CopyPicFromServer1.CopyPicReturnPath(pic, flag);//老系统方式
						ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//中央存储
					}else if("1".equals(xtFlag) && jcdid.contains("A")){//深圳停车场
						//调取图片
						ListPic.add(CopyPicFromServer.PicCall(pic, flag));
					}else if("2".equals(xtFlag)){//乌海
						String httpStr = JcdOracle.getTpcflj(jcdid);
						//获取前缀，乌海有部分监测点图片存放在前端，如果有路径，则需要到前端获取图片数据并保存到本地缓存中，再返回图片链接
						if(httpStr != null && !"".equals(httpStr)){//如果前缀不为空，
							CopyPicFromServer.CopyPicReturnPath(pic, flag);//中央存储
							ListPic.add(CopyPicFromServer.CopyPicReturnPath2(pic, flag, httpStr));//前端存储
						}else {
							ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//中央存储
						}
					}else if("3".equals(xtFlag)){//宝安
						ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//中央存储
					}
				}else{//红名单图片
					ListPic.add(picURL + File.separator + pic + ".jpg");;//图片前缀+图片ＩＤ
				}
			}
			
			threadsSignal.countDown();// 线程计数器减1,执行完操作
		} catch (Exception e) {
			threadsSignal.countDown();// 线程计数器减1,执行完操作
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
