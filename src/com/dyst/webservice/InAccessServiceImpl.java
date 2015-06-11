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
	 * 车辆轨迹查询
	 * @param systemType
	 *            系统类型
	 * @param businessType
	 *            业务类型  01:已识别查询 02:图片地址查询 ；03:未识别查询；04:车辆模糊查询
	 * @param sn
	 *            校验码
	 * @param xml
	 *            请求XML报文
	 * @return XML文件字符串
	 */
	public String executes(String systemType, String businessType, String sn, String data,
			String xml) {
         Config config = Config.getInstance();//配置信息类
         XmlCreater xmlcreate = new XmlCreater();
         businessType = businessType.trim();
         if(sn != null && "hello,world".equals(sn.trim())){//合法性校验
//	    	ClientService clientService = new ClientService();//采用filter方式实现
	    	ClientServiceQuery clientService = new ClientServiceQuery();//采用query方式实现
	    	if("01".equals(businessType) || "04".equals(businessType) 
	    			|| "03".equals(businessType) || "05".equals(businessType)){//轨迹查询
				String xmlstr = clientService.gjcx(xml, businessType, data);
				return xmlstr;
	    	}else if("02".equals(businessType)||"07".equals(businessType)){//图片查询
	    		return clientService.tpcx(xml,businessType);
	    	}else if("06".equals(businessType)){//识别记录更新
	    		SbUpdate sb = new SbUpdate();
	    		return sb.updateSb(xml);
	    	}else if("08".equals(businessType)){//统计查询
	    		TjService tj = new TjService();
	    		return tj.tjcx(xml);
	    	}else if("09".equals(businessType)){// 频繁出现点分析
	    		FrequentlyAppear f = new FrequentlyAppear();
	    		return f.frequentlyApp(xml);
	    	}else if("10".equals(businessType)){//多点碰撞比对分析
	    		Ddpzfx ddpz = new Ddpzfx();
	    		return ddpz.Pzfx(xml);
	    	}else{
	    		//无该类型查询业务方法"02:调用业务类型代码不存在"
	    		return xmlcreate.createErrorXml(config.getErrorCode02());
	    	}
	    }else{
	    	//调用单位不合法，生成Error的XML报文"01:调用单位不合法，请查询是否输错或者有无权限！"
	    	return xmlcreate.createErrorXml(config.getErrorCode01());
	    }
	}
}