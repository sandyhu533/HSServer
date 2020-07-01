package com.mushroom.helloword.xunfei;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mushroom.helloword.kmeans.Kmeans;

public class XunfeiOcr {

		// 判断最左侧位置
		private static int LEFT_POS = 100;
		// 判断换页间隔
		private static int GAP_LENGTH = 100;
	
		private static XunfeiOcr instance;
		
	    // 手写文字识别webapi接口地址
		private static final String WEBOCR_URL = "http://webapi.xfyun.cn/v1/service/v1/ocr/handwriting";
		// 应用APPID(必须为webapi类型应用,并开通手写文字识别服务,参考帖子如何创建一个webapi应用：http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=36481)
		private static final String TEST_APPID = "5d874591";
		// 接口密钥(webapi类型应用开通手写文字识别后，控制台--我的应用---手写文字识别---相应服务的apikey)
		private static final String TEST_API_KEY = "7dcaafe16937280badb6d9f4c22e38ef";
		// 测试图片文件存放位置
//		private static final String IMAGE_FILE_PATH = "testImage1.jpg";

		// 单例模式
	    private XunfeiOcr() {}
	    
		// 获取实例
		public static XunfeiOcr getInstance(){
	        if(instance==null){
	            instance = new XunfeiOcr();
	        }
	        return instance;
	    }
	    
		/**
		 * 组装http请求头
		 * 
		 * @param aue
		 * @param engineType
		 * @return
		 * @throws UnsupportedEncodingException
		 * @throws ParseException 
		 */
		private static Map<String, String> constructHeader(String language, String location) throws UnsupportedEncodingException, ParseException {
			// 系统当前时间戳
			String X_CurTime = System.currentTimeMillis() / 1000L + "";
			// 业务参数
			String param = "{\"language\":\""+language+"\""+",\"location\":\"" + location + "\"}";
			String X_Param = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
			// 接口密钥
			String apiKey = TEST_API_KEY;
			// 讯飞开放平台应用ID
			String X_Appid = TEST_APPID;
			// 生成令牌
			String X_CheckSum = DigestUtils.md5Hex(apiKey + X_CurTime + X_Param);
			// 组装请求头
			Map<String, String> header = new HashMap<String, String>();
			header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			header.put("X-Param", X_Param);
			header.put("X-CurTime", X_CurTime);
			header.put("X-CheckSum", X_CheckSum);
			header.put("X-Appid", X_Appid);
			return header;
		}

		/**
		 * 传入图片路径进行手写识别
		 * @param imagePath
		 * @return
		 * @throws IOException
		 * @throws ParseException
		 */
	    public JSONObject imageOcr(String imagePath) throws IOException,ParseException{
	    	Map<String, String> header = constructHeader("cn|en", "true");
			// 读取图像文件，转二进制数组，然后Base64编码
			byte[] imageByteArray = FileUtil.read2ByteArray(imagePath);
			String imageBase64 = new String(Base64.encodeBase64(imageByteArray), "UTF-8");
			String bodyParam = "image=" + imageBase64;
			String resultStr = HttpUtil.doPost(WEBOCR_URL, header, bodyParam);
			
			// 处理并形成JSON
			JSONObject res = strToJsonData(resultStr);
		
			return res;
	    }
	    
	    /**
	     * 处理结果字符串，生成JSON
	     * @param resultStr
	     * @return
	     */
	    public JSONObject strToJsonData(String resultStr) {
	    	
	    	// 转为JSON
	    	JSONObject res = new JSONObject(resultStr); 
	    	
	    	// 处理得到的文字和位置数据
	    	JSONArray blockArray = res.getJSONObject("data").getJSONArray("block");
	    	
	    	// 获取识别信息
	    	List<WordWithPos> wordWithPosArray = new ArrayList<WordWithPos>();

	    	int lastBottomLoc = 0;
	    	
	 		// 遍历block数组
	 		for(int i=0;i<blockArray.length();i++) {
	 			
	 			//获取line数组
	 			JSONArray lineArray = blockArray.getJSONObject(i).getJSONArray("line");
	 			
	 			// 遍历line数组
	 			for(int j=0;j<lineArray.length();j++) {
	 				
	 				// 获取当前行
	 				JSONObject oneLine = lineArray.getJSONObject(j);
	 				
	 				// 获取最左侧位置
	 				JSONObject locObject = oneLine.getJSONObject("location").getJSONObject("top_left");
	 				int leftLoc = locObject.getInt("x");
	 				int upLoc = locObject.getInt("y");
	 				
	 				// 计算gap
	 				int gap = upLoc - lastBottomLoc;

	 				// 获取下界
	 				locObject = oneLine.getJSONObject("location").getJSONObject("right_bottom");
	 				lastBottomLoc = locObject.getInt("y");
	 				
	 				// 获取word数组
	 				JSONArray wordArray = oneLine.getJSONArray("word");
	 				
	 				String wordStr = "";
	 				
	 				// 遍历word
	 				for(int k=0;k<wordArray.length();k++) {
	 					// 将所有内容拼在一起
	 					wordStr += wordArray.getJSONObject(k).getString("content");
	 				}
	 				
	 				wordWithPosArray.add(new WordWithPos(wordStr,leftLoc,gap,1));

	 			}
	 		}
	 		
	 		// 获取范围
			int[] posRange = calGap(wordWithPosArray);
			for(int i=0;i<posRange.length;i++) {
				System.out.println("【posRange】"+posRange[i]);
			}
			
			// 给wordWithPosArray打标签
			for(int i=0;i<wordWithPosArray.size();i++) {
				int leftPos = wordWithPosArray.get(i).leftPos;
				if(leftPos<=posRange[0]) {
					wordWithPosArray.get(i).type = 0;
				}else if(leftPos<=posRange[1]) {
					wordWithPosArray.get(i).type = 1;
				}else if(leftPos<=posRange[2]) {
					wordWithPosArray.get(i).type = 2;
				}else if(leftPos<=posRange[3]) {
					wordWithPosArray.get(i).type = 3;
				}
				System.out.println("["+wordWithPosArray.get(i).type+"]["+wordWithPosArray.get(i).leftPos+"]["+wordWithPosArray.get(i).upGap+"]"+wordWithPosArray.get(i).content);
			}
			
			// 处理类型为0的
			int cnt=0;
			while(cnt<wordWithPosArray.size()) {
	 			if(wordWithPosArray.get(cnt).type==0) {
	 				if(cnt==0) {
	 					wordWithPosArray.get(cnt).type=1;
	 				}else {
	 					// 加入上一个wordWithPos
		 				 wordWithPosArray.get(cnt-1).content += wordWithPosArray.get(cnt).content;
		 				 wordWithPosArray.remove(cnt);	
	 				}
	 			}else {
	 				cnt++;
	 			}
	 		}
			
			// 分页
			List<List<WordWithPos>> pageArray = new ArrayList<List<WordWithPos>>();
			
			int pageCnt = 0;
			// 删除位置信息，存入JSONArray
			for(int i=0;i<wordWithPosArray.size();i++) {
				
				if(i==0) {
					// 新页
					List<WordWithPos> newPage = new ArrayList<WordWithPos>();
					pageArray.add(newPage);
				}else {
					// 判断是否换页
					if(wordWithPosArray.get(i).upGap>GAP_LENGTH) {
						// 新页
						List<WordWithPos> newPage = new ArrayList<WordWithPos>();
						pageArray.add(newPage);
						pageCnt++;
					}
				}
				
				// 加入页中
				pageArray.get(pageCnt).add(wordWithPosArray.get(i));
			}
			
			// 处理页中只有低级元素而没有高级元素的情况
			for(int i=0;i<pageArray.size();i++) {
				
				List<WordWithPos> onePage = pageArray.get(i);
				
				// 是否存在类型为1的元素
				boolean flag = false;

				while(flag==false) {
					// 遍历元素
					for(int e=0;e<onePage.size();e++) {
						if(onePage.get(e).type==1) {
							flag=true;
							break;
						}
					}
					
					// 不存在类型为1的
					if(flag==false) {
						// 遍历元素，所有type前移
						for(int e=0;e<onePage.size();e++) {
							onePage.get(e).type = onePage.get(e).type-1;
						}
					}
				}
				
			}
			
			// 打印输出
			System.out.println("【处理后】");
			for(int i=0;i<pageArray.size();i++) {
				System.out.println("----第"+i+"页----");
				for(int j=0;j<pageArray.get(i).size();j++) {
					System.out.println("["+pageArray.get(i).get(j).type+"]"+pageArray.get(i).get(j).content);
				}
			}
			
			// 构建JSON
			JSONArray pages = new JSONArray();
			
			for(int i=0;i<pageArray.size();i++) {
				// 新页
				JSONArray newWordWithPosJSONArray = new JSONArray();
				List<WordWithPos> page = pageArray.get(i);

				for(int j=0;j<page.size();j++) {
					// 文字和类型转为JSON，加入页中
			 		JSONObject wordWithPosJSON = new JSONObject();
			 		wordWithPosJSON.put("content", page.get(j).content);
			 		wordWithPosJSON.put("type", page.get(j).type);
			 		
			 		newWordWithPosJSONArray.put(wordWithPosJSON);
				}
				
				JSONObject newPageObject = new JSONObject();
				newPageObject.put("page", newWordWithPosJSONArray);
				pages.put(newPageObject);
			}
			
	 		// 构建返回JSON
	 		JSONObject object = new JSONObject();
	 		object.put("words", pages);

	 		System.out.println("----------------------");
	 		System.out.println(object);
	 		System.out.println("----------------------");
	 		
	    	return object;
	    }
	    
	 // 计算四个范围
	 public int[] calGap(List<WordWithPos> wordWithPosArray){
		 
		 // 初始化dataSet
		 ArrayList<float[]> dataSet=new ArrayList<float[]>();
		 
		 // 去除所有值小于LEFT_POS的，其它加入dataSet
		 for(int i=0;i<wordWithPosArray.size();i++) {
			 int num = wordWithPosArray.get(i).leftPos;
			 if(num>=LEFT_POS) {
				 dataSet.add(new float[]{num,0});
			 }
		  }
		 
		 int[] gap = new int[3];
		 for(int i=0;i<3;i++) {
			 gap[i]=16384;
		 }

		// 初始化一个Kmean对象
		Kmeans k=new Kmeans(3);
					
		// 设置原始数据集
		k.setDataSet(dataSet);
		// 执行算法
		k.execute();
		// 得到聚类结果
		ArrayList<ArrayList<float[]>> cluster=k.getCluster();
		
		for(int i=0;i<cluster.size();i++) {
			ArrayList<float[]> dataArray = cluster.get(i);
			System.out.println("dataArray"+i+" : "+dataArray);
			
			gap[i]=getMax(dataArray);
		}
		 
		 // gap从小到大
		 Arrays.sort(gap);
		 
		 int[] posGap = new int[4];
		 posGap[0] = LEFT_POS;
		 posGap[1] = gap[0];
		 posGap[2] = gap[1];
		 posGap[3] = gap[2];
		 
		 return posGap;
	 }
	 
	    // 获取最大值
		public static int getMax(ArrayList<float[]> dataArray) {
			float max = 0;
			
			for (int i = 0; i < dataArray.size(); i++) {
				float num = dataArray.get(i)[0];
				if(num > max) {
					max = num;
				}
			}
			
			return (int)max;
		}
	   
		
}
