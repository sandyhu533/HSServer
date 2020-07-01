package com.mushroom.helloword;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import com.mushroom.util.ServerInfo;

@Component
public class CustomDisposableBean implements DisposableBean{
 
	// 获取logger
	private static final Logger logger = LoggerFactory.getLogger(CustomDisposableBean.class); 
		
	@Override
	public void destroy() throws Exception {
	
		// 获取服务器信息
		ServerInfo serverInfo = ServerInfo.getInstance();
		
		long endTime = System.currentTimeMillis();
		long runTime = endTime - serverInfo.getStartTime();
		double minutes = runTime / 1000 / 60;
		
		logger.info("HelloWordServer关闭");
		logger.info("本次运行时间"+minutes+"分钟，处理imageocr请求"+serverInfo.getImageocrCnt()+"个");
	}
 
}