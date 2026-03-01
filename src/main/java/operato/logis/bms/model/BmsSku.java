package operato.logis.bms.model;

/**
 * 상품 정보 
 * @author jhs
 */
public class BmsSku {
	
	// 상품 유형 코드 
	private String skuCd;
	// 상품 이름 
	private String skuNm;
	// 상품 가로 
	private int width;
	// 상품 세로 
	private int length;
	// 상품 높이
	private int height;
	// 상품 부피 
	private int volume;
	// 육면체 대각선 길이 
	private double diagonal;
	
	public BmsSku() {}
	
	public BmsSku(String skuCd, String skuNm, int width, int length, int height) {
		/*
		 * 상품 정보 셋팅
		 * 가로, 세로, 높이 값으로 부피와 대각선 길이를 구함 
		 */
		this.setSkuCd(skuCd);
		this.setSkuNm(skuNm);
		this.setWidth(width);
		this.setLength(length);
		this.setHeight(height);
		this.calVolume();
		this.calDiagonal();
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public double getDiagonal() {
		return diagonal;
	}

	public void setDiagonal(double diagonal) {
		this.diagonal = diagonal;
	}
	
	/**
	 * 대각선 길이 계산
	 */
	public void calDiagonal() {
		this.setDiagonal(Math.sqrt((Math.pow(width, 2) + Math.pow(length, 2) + Math.pow(height, 2))));
	}
	
	public void calVolume() {
		this.setVolume(width * length * height);
	}
	
}
