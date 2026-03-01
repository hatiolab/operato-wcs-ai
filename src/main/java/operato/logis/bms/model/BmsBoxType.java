package operato.logis.bms.model;

import xyz.elidom.util.ValueUtil;

/**
 * 박스 유형 정보 
 * @author jhs
 */
public class BmsBoxType {
	
	// 박스 유형 코드 
	private String boxTypeCd;
	// 박스 이름 
	private String boxNm;
	// 박스 가로 
	private int width;
	// 박스 세로 
	private int length;
	// 박스 높이
	private int height;
	// 박스 부피 
	private int volume;
	// 박스 여유 공간 
	private int errorRange;
	// 육면체 대각선 길이 (박스 대각선 길이)
	private double diagonal;
	// 부피 추가
	private int volPlus;
	// 부피 제외
	private int volMinus;
	
	public BmsBoxType() {}
	
	public BmsBoxType(String boxTypeCd, String boxNm, int width, int length, int height, int errorRange) {
		/*
		 * 박스 정보 셋팅
		 * 가로, 세로, 높이 값을 기반으로 부피와 대각선 길이를 구함
		 * 부피는 여유 공간을 차감하여 셋팅 함  
		 */
		this.setBoxTypeCd(boxTypeCd);
		this.setBoxNm(boxNm);
		this.setWidth(width);
		this.setLength(length);
		this.setHeight(height);
		this.setErrorRange(errorRange);
		// 박스의 부피에 여유 공간을 차감하여 부피 값을 셋팅 한다.
		this.calVolume();
		// 박스의 대각선 길이 계산 
		this.calDiagonal();
	}
	
	public BmsBoxType(String boxTypeCd, String boxNm, int width, int length, int height, int errorRange, int volPlus, int volMinus) {
		/*
		 * 박스 정보 셋팅
		 * 가로, 세로, 높이 값을 기반으로 부피와 대각선 길이를 구함
		 * 부피는 여유 공간, 추가/제외 부피를 계산하여 셋팅 함  
		 */
		this.setBoxTypeCd(boxTypeCd);
		this.setBoxNm(boxNm);
		this.setWidth(width);
		this.setLength(length);
		this.setHeight(height);
		this.setErrorRange(errorRange);
		this.setVolPlus(volPlus);
		this.setVolMinus(volMinus);
		// 박스의 부피에 여유 공간, 추가/제외 부피를 계산하여 부피 값을 셋팅 한다.
		this.calVolume();
		// 박스의 대각선 길이 계산 
		this.calDiagonal();
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}

	public String getBoxNm() {
		return boxNm;
	}

	public void setBoxNm(String boxNm) {
		this.boxNm = boxNm;
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

	public int getErrorRange() {
		return errorRange;
	}

	public void setErrorRange(int errorRange) {
		this.errorRange = errorRange;
	}

	public double getDiagonal() {
		return diagonal;
	}

	public void setDiagonal(double diagonal) {
		this.diagonal = diagonal;
	}

	public int getVolPlus() {
		return volPlus;
	}

	public void setVolPlus(int volPlus) {
		this.volPlus = volPlus;
	}

	public int getVolMinus() {
		return volMinus;
	}

	public void setVolMinus(int volMinus) {
		this.volMinus = volMinus;
	}
	
	public void calDiagonal() {
		this.setDiagonal(Math.sqrt((Math.pow(width, 2) + Math.pow(length, 2) + Math.pow(height, 2))));
	}
	
	/**
	 * 박스 부피 계산 
	 * 전체 부피 - 오차 범위 + 추가 부피 - 제외 부피 
	 * 오차 범위(%)에 대한 계산 값을 차감
	 * 특정 조건에 따라 부피 추가/제외 필요한 경우 계산
	 * 부자재의 경우 제외할 부피 (volMinus) 값을 계산하여 셋팅
	 */
	public void calVolume() {
		// 전체 부피 - 오차 범위 + 추가 부피 - 제외 부피
		this.setVolume((width * length * height) - (width * length * height * (ValueUtil.checkValue(errorRange, 0) / 100)) + ValueUtil.checkValue(volPlus, 0) - ValueUtil.checkValue(volMinus, 0));
	}
	
}
