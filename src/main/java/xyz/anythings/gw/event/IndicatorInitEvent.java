package xyz.anythings.gw.event;

import xyz.anythings.gw.entity.Indicator;

/**
 * 표시기 초기화 완료 보고에 대한 처리 이벤트
 * 
 * @author shortstop
 */
public class IndicatorInitEvent extends AbstractGatewayEvent {

	/**
	 * 표시기
	 */
	private Indicator indicator;
	/**
	 * 스테이지 코드
	 */
	private String stageCd;
	
	/**
	 * 생성자
	 * 
	 * @param eventStep
	 * @param indicator
	 * @param stageCd
	 */
	public IndicatorInitEvent(short eventStep, Indicator indicator, String stageCd) {
		this.setEventStep(eventStep);
		this.setIndicator(indicator);
	}

	public Indicator getIndicator() {
		return indicator;
	}

	public void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}

}
