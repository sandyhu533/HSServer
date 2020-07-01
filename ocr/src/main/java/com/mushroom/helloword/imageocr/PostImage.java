package com.mushroom.helloword.imageocr;

/**
 * 待处理的图片信息
 * @author coconutnut
 *
 */
public class PostImage {

	private int type; // 0-识别全图；1-识别单句
    private String name;
    private String imageBase64;
    
    public PostImage() {}
    
    public PostImage(int type, String name, String imageBase64) {
    	this.type = type;
    	this.name = name;
    	this.imageBase64 = imageBase64;
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

}
