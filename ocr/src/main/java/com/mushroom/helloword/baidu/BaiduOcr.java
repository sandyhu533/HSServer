package com.mushroom.helloword.baidu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

public class BaiduOcr {

	private static BaiduOcr instance;
	
	//设置APPID/AK/SK
    public static final String APP_ID = "17306720";
    public static final String API_KEY = "VAkgSRWVHKGtqwmMVH7ncm9A";
    public static final String SECRET_KEY = "EzLMgnhojhWt1kz7ImVPklcaf5g3igWV";
    
    private AipOcr aipOcr;
    
    // 单例模式
    private BaiduOcr() {
    	// 初始化AipOcr
    	aipOcr = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
    	aipOcr.setConnectionTimeoutInMillis(2000);
    	aipOcr.setSocketTimeoutInMillis(60000);
    }
    
	// 获取实例
	public static BaiduOcr getInstance(){
        if(instance==null){
            instance = new BaiduOcr();
        }
        return instance;
    }

	/**
	 * 调用百度OCR识别图片文字
	 * @param imagePath
	 * @param accurate 高精度版
	 * @return
	 */
    public JSONObject ocrWithPosInfo(String imagePath, boolean accurate) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("recognize_granularity", "big");
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "false");
        options.put("detect_language", "true");
        options.put("vertexes_location", "false");
        options.put("probability", "false");
        
        JSONObject res = null;
        
        // 是否调用高精度版
        if (accurate == true) {
        	// 参数为本地路径
        	// 通用文字识别（含位置高精度版）接口
            res = aipOcr.accurateGeneral(imagePath, options);
        } else {
        	// 通用文字识别（含位置信息版）接口   
            res = aipOcr.general(imagePath, options);
        }

        return res;
    }

	/**
	 * 调用百度OCR识别单句
	 * @param imagePath
	 * @param accurate 高精度版
	 * @return
	 */
    public JSONObject ocrSentence(String imagePath) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("detect_direction", "false");
        
        JSONObject res = aipOcr.basicAccurateGeneral(imagePath, options);

        return res;
    }
    
 // 处理百度OCR返回的JSON数据
 	public JSONObject resolveWordsJsonData(JSONObject res) {
 		
 		// 处理得到的文字和位置数据
 		JSONArray wordsArray = res.getJSONArray("words_result");
 		
 		// 合并句子，删除位置信息，存入数组
 		List<String> words = new ArrayList<String>();
 		
 		int lastWordCnt = -1;
 		
 		for(int i=0;i<wordsArray.length();i++) {
 			
 			JSONObject object = wordsArray.getJSONObject(i);
 			String wordStr = object.getString("words");
 			JSONObject locationObject = object.getJSONObject("location");
 			int leftLoc = locationObject.getInt("left");
 			
 			// 靠左，加入上一个word
 			if(i>0 && leftLoc<100) {
 				words.set(lastWordCnt, words.get(lastWordCnt)+wordStr);
 			} else {
 				words.add(wordStr);
 				lastWordCnt += 1;
 			}
 		}
 			
 		// 转为JSON
 		JSONObject object = new JSONObject();
 		object.put("words", words);
 	
 		return object;
 	}
 	
 	// 处理百度OCR返回的JSON数据
 	public JSONObject resolveSentenceJsonData(JSONObject res) {
 		
 		// 处理得到的文字和位置数据
 		JSONArray wordsArray = res.getJSONArray("words_result");
 		
 		// 合并句子，存入数组
 		String sentence = new String();
 		
 		for(int i=0;i<wordsArray.length();i++) {
 			
 			JSONObject object = wordsArray.getJSONObject(i);
 			String wordStr = object.getString("words");
 			
 			sentence = sentence + wordStr;

 		}
 			
 		// 转为JSON
 		JSONObject object = new JSONObject();
 		object.put("words", sentence);
 	
 		return object;
 	}		
}
