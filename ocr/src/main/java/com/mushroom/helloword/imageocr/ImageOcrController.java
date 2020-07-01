package com.mushroom.helloword.imageocr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mushroom.helloword.baidu.BaiduOcr;
import com.mushroom.helloword.xunfei.XunfeiOcr;
import com.mushroom.util.ImageFileUtil;
import com.mushroom.util.ServerInfo;

@RestController
@RequestMapping("/imageocr")
public class ImageOcrController {

	// 选择OCR
	private static final String OCR_PLATFORM = "Xunfei";   // "Baidu"或"Xunfei"
	
	// 获取logger
	private static final Logger logger = LoggerFactory.getLogger(ImageOcrController.class); 
	
	@GetMapping
	public String getAll(HttpServletRequest request){

		//获取IP地址
	    String ipAddress = IpUtil.getIpAddr(request);

		logger.info("[ip]"+ipAddress+" GET访问/imageocr");
		
	    return "image OCR 运行中";
	}	
	
	@PostMapping
    public String addOne(HttpServletRequest request, @RequestBody PostImage newImage){
		
		logger.debug("获取图片，name: " + newImage.getName());
		
		// 获取图片的base64串
		String imageContent = newImage.getImageBase64();
		// 获取图片名
		String imageName = newImage.getName();
		// 加时间戳
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		// 拼接文件名
		imageName = timeStamp + "_" + imageName + ".jpg";
		
		// 获取文件路径
		Path imagePath = Paths.get(imageName);
		// 保存图片
		ImageFileUtil.writeFile(imagePath,ImageFileUtil.base64Decoding(imageContent));
		
		logger.debug("收到图片，path: " + imagePath);
		
		JSONObject res = null;
		long time = 0;
		
		if (OCR_PLATFORM == "Xunfei") {
			// 调用讯飞OCR
			// 讯飞OCR
			logger.debug("调用讯飞OCR");
			long startTime = System.currentTimeMillis();
			
			XunfeiOcr xunfeiOcr = XunfeiOcr.getInstance();

			// TODO
			try {
				res = xunfeiOcr.imageOcr(imagePath.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			
			long endTime = System.currentTimeMillis();
			time = endTime - startTime;
			logger.debug("讯飞OCR处理完成，用时"+time/1000+"s");
			
		} 
//		else if (OCR_PLATFORM == "Baidu") {
//			// 调用百度OCR
//			logger.debug("调用百度OCR");
//			long startTime = System.currentTimeMillis();
//			BaiduOcr baiduOcr = BaiduOcr.getInstance();
//			
//			// 根据请求OCR方式调用
//			if (newImage.getType()==0) {
//				// 高精度带位置信息版
//				res = baiduOcr.ocrWithPosInfo(imagePath.toString(),true);
//				
//				try {
//					// 处理返回的JSON数据
//					res = baiduOcr.resolveWordsJsonData(res);
//				} catch(Exception e) {
//					logger.error("解析OCR返回数据错误！res:["+res+"]");
//				}
//				
//			} else {
//				// 高精度版，识别单句
//				res = baiduOcr.ocrSentence(imagePath.toString());
//				// 处理返回的JSON数据
//				res = baiduOcr.resolveSentenceJsonData(res);
//			}
//			
//			long endTime = System.currentTimeMillis();
//			time = endTime - startTime;
//			logger.debug("百度OCR处理完成，用时"+time/1000+"s");
//		}
		
		// 删除文件
		try{
            File file = new File(imageName);
            if(!file.delete()){
            	logger.error("图片删除失败");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
		
		//获取IP地址
	    String ipAddress =IpUtil.getIpAddr(request);
	    
	    // 增加imagecnt计数
	    ServerInfo.getInstance().addImageocrCnt();
	    
		// logger记录
		logger.info("[ip]"+ipAddress+" [type]"+newImage.getType()+" [image]"+imageName+" [time]"+time/1000);
		
		// 返回位置信息JSON
		return res.toString();
   }
	
	
	
}
