package operato.logis.couriers.service.cj.addr.soap.client;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import operato.logis.couriers.service.cj.addr.soap.wsdl.AddressSearchRequest;
import operato.logis.couriers.service.cj.addr.soap.wsdl.AddressSearchResponse;
import operato.logis.couriers.service.cj.addr.soap.wsdl.GetAddressInformationByValue2;
import operato.logis.couriers.service.cj.addr.soap.wsdl.GetAddressInformationByValue2Response;

/**
 * 주소 정제 서비스 호출 클라이언트
 * 
 * @author shortstop
 */
public class AddressServiceClient extends WebServiceGatewaySupport {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 주소 정제 서비스 호출
	 * 
	 * @param boxType 박스타입 (1: 극소, 2: 소, 3:중, 4:대) 
	 * @param clientMgmtCustCd 고객관리 거래처 코드 (일반적인 기업고객은 고객코드와 동일)
	 * @param clientNo 고객코드
	 * @param contractLarcCd 계약상품코드 (01 : 상수 고정)
	 * @param fareDiv 운임구분 (01 : 선불, 02 : 착불, 03 : 신용)
	 * @param orderNo 주문번호 : WebService에 다량의 요청을 호출할 경우 구분할 수 있는 값, NULL이어도 상관없음
	 * @param prngDivCd 예약구분 (01 : 일반, 02 : 반품)
	 * @param receiverAddr 보내는분 주소
	 * @param sendPrsAddr 받는분 주소
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AddressSearchResponse getAddressInfo(
			String boxType, 
			String clientMgmtCustCd, 
			String clientNo, 
			String contractLarcCd, 
			String fareDiv, 
			String orderNo, 
			String prngDivCd, 
			String receiverAddr, 
			String sendPrsAddr) {

		GetAddressInformationByValue2 request = new GetAddressInformationByValue2();
		AddressSearchRequest arg = new AddressSearchRequest();
		arg.setBoxTyp(boxType);
		arg.setClntMgmCustCd(clientMgmtCustCd);
		arg.setClntNum(clientNo);
		arg.setCntrLarcCd(contractLarcCd);
		arg.setFareDiv(fareDiv);
		arg.setOrderNo(orderNo);
		arg.setPrngDivCd(prngDivCd);
		arg.setRcvrAddr(receiverAddr);
		arg.setSndprsnAddr(sendPrsAddr);
		request.setArg0(arg);

		this.logger.info("Requesting address for " + receiverAddr);

		JAXBElement<GetAddressInformationByValue2Response> response = 
			(JAXBElement<GetAddressInformationByValue2Response>)getWebServiceTemplate()
				.marshalSendAndReceive("http://address.doortodoor.co.kr/address/address_webservice.korex", request,
					new SoapActionCallback("http://webservice.address.nplus.doortodoor.co.kr/getAddressInformationByValue2"));
		
		return (response == null) ? null : response.getValue().getReturn();
	}

}
