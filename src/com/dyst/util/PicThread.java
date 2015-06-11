package com.dyst.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.dyst.oracle.JcdOracle;

/**
 * ͼƬ��ַ��ѯ�߳���
 * @author Administrator
 */
@SuppressWarnings("unchecked")
public class PicThread extends Thread {
	private CountDownLatch threadsSignal;//�߳���
	private String pic = "";//ͼƬid
	private List ListPic = new ArrayList();//��Ž��
	private String flag = "";
	
	//���췽��
	public PicThread(CountDownLatch threadsSignal, String pic, String flag, List listPic) {
		this.threadsSignal = threadsSignal;//�߳���
		this.pic = pic;//ͼƬid
		this.ListPic = listPic;//��Ž��
		this.flag = flag;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			String picURL = Config.getInstance().getPicURL();//ͼƬ����ǰ׺
			//��������ڽ���ͼƬ����,ͣ������ͼƬid��Ҫ�������һ�����
			String xtFlag = Config.getInstance().getSysFlag();
			
			//����ͼƬid��ʽ����ȡ��ͬ�ķ���
			if(pic != null && !"".equals(pic) && pic.length() >= 24){
				if(!"00000000000000000000000000000000000".equals(pic)){
					//��ȡ����id
					String jcdid = pic.substring(16, 24);
					
					//-----ͼƬid����Ϊ27λ--,,,,,,�����м�����ͼƬid�����쳣�Ĵ�����
					if(pic.length() == 27){
						jcdid = pic.substring(17, 25);//����id	
					}
					
					//���ݲ�ͬϵͳ���������͵���
					if("1".equals(xtFlag) && !jcdid.contains("A")){//����
						//��ȡͼƬ
						CopyPicFromServer1.CopyPicReturnPath(pic, flag);//��ϵͳ��ʽ
						ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//����洢
					}else if("1".equals(xtFlag) && jcdid.contains("A")){//����ͣ����
						//��ȡͼƬ
						ListPic.add(CopyPicFromServer.PicCall(pic, flag));
					}else if("2".equals(xtFlag)){//�ں�
						String httpStr = JcdOracle.getTpcflj(jcdid);
						//��ȡǰ׺���ں��в��ּ���ͼƬ�����ǰ�ˣ������·��������Ҫ��ǰ�˻�ȡͼƬ���ݲ����浽���ػ����У��ٷ���ͼƬ����
						if(httpStr != null && !"".equals(httpStr)){//���ǰ׺��Ϊ�գ�
							CopyPicFromServer.CopyPicReturnPath(pic, flag);//����洢
							ListPic.add(CopyPicFromServer.CopyPicReturnPath2(pic, flag, httpStr));//ǰ�˴洢
						}else {
							ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//����洢
						}
					}else if("3".equals(xtFlag)){//����
						ListPic.add(CopyPicFromServer.CopyPicReturnPath(pic, flag));//����洢
					}
				}else{//������ͼƬ
					ListPic.add(picURL + File.separator + pic + ".jpg");;//ͼƬǰ׺+ͼƬ�ɣ�
				}
			}
			
			threadsSignal.countDown();// �̼߳�������1,ִ�������
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�������
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
