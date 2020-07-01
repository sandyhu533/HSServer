package com.mushroom.util;

/**
 * 记录服务器信息
 * @author coconutnut
 *
 */
public class ServerInfo {
	
	private static ServerInfo instance;

	// 启动时间
	private long startTime ;
	
	// 处理imageocr请求数量
	private int imageocrCnt = 0;
	
	// 单例模式
	private ServerInfo() {
		startTime = System.currentTimeMillis();
	}
	
	// 获取实例
	public static ServerInfo getInstance(){
        if(instance==null){
            instance = new ServerInfo();
        }
        return instance;
    }
	
	// 增加imagecnt计数
	public int addImageocrCnt() {
		imageocrCnt++;
		return imageocrCnt;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getImageocrCnt() {
		return imageocrCnt;
	}
	
}
