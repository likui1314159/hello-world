package com.dyst.webservice;

import com.dyst.service.ClientServiceQuery;
import com.dyst.service.Ddpzfx;
import com.dyst.service.FrequentlyAppear;
import com.dyst.service.SbUpdate;
import com.dyst.service.TjService;
import com.dyst.util.Config;
import com.dyst.util.XmlCreater;

public class InAccessServiceImpl implements IInAccessService {
	/**
	 * �����켣��ѯ
	 * @param systemType
	 *            ϵͳ����
	 * @param businessType
	 *            ҵ������  01:��ʶ���ѯ 02:ͼƬ��ַ��ѯ ��03:δʶ���ѯ��04:����ģ����ѯ
	 * @param sn
	 *            У����
	 * @param xml
	 *            ����XML����
	 * @return XML�ļ��ַ���
	 */
	public String executes(String systemType, String businessType, String sn, String data,
			String xml) {
         Config config = Config.getInstance();//������Ϣ��
         XmlCreater xmlcreate = new XmlCreater();
         businessType = businessType.trim();
         if(sn != null && "hello,world".equals(sn.trim())){//�Ϸ���У��
//	    	ClientService clientService = new ClientService();//����filter��ʽʵ��
	    	ClientServiceQuery clientService = new ClientServiceQuery();//����query��ʽʵ��
	    	if("01".equals(businessType) || "04".equals(businessType) 
	    			|| "03".equals(businessType) || "05".equals(businessType)){//�켣��ѯ
				String xmlstr = clientService.gjcx(xml, businessType, data);
				return xmlstr;
	    	}else if("02".equals(businessType)||"07".equals(businessType)){//ͼƬ��ѯ
	    		return clientService.tpcx(xml,businessType);
	    	}else if("06".equals(businessType)){//ʶ���¼����
	    		SbUpdate sb = new SbUpdate();
	    		return sb.updateSb(xml);
	    	}else if("08".equals(businessType)){//ͳ�Ʋ�ѯ
	    		TjService tj = new TjService();
	    		return tj.tjcx(xml);
	    	}else if("09".equals(businessType)){// Ƶ�����ֵ����
	    		FrequentlyAppear f = new FrequentlyAppear();
	    		return f.frequentlyApp(xml);
	    	}else if("10".equals(businessType)){//�����ײ�ȶԷ���
	    		Ddpzfx ddpz = new Ddpzfx();
	    		return ddpz.Pzfx(xml);
	    	}else{
	    		//�޸����Ͳ�ѯҵ�񷽷�"02:����ҵ�����ʹ��벻����"
	    		return xmlcreate.createErrorXml(config.getErrorCode02());
	    	}
	    }else{
	    	//���õ�λ���Ϸ�������Error��XML����"01:���õ�λ���Ϸ������ѯ�Ƿ�����������Ȩ�ޣ�"
	    	return xmlcreate.createErrorXml(config.getErrorCode01());
	    }
	}
}