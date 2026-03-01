package operato.logis.couriers.service.cj.invno;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.Invoice;
import xyz.anythings.base.service.api.IInvoiceNoGenerator;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.util.ValueUtil;

/**
 * CJ 대한통운용 송장 번호 생성 서비스
 * 
 * @author shortstop
 */
@Component("cjInvoiceNoGenerator")
public class CjInvoiceNoGenerator implements IInvoiceNoGenerator {
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;

	@Override
	public int generateInvoiceNumbers(Long domainId, String comCd, String customerCd, String fromNoStr, String toNoStr) {
		
		long fromNo = ValueUtil.toLong(fromNoStr);
		long toNo = ValueUtil.toLong(toNoStr);
		
		if(fromNo < 0) {
			throw new ElidomRuntimeException("Invoice FromNo. must be greater than 0!");
		}
		
		if(toNo < 1) {
			throw new ElidomRuntimeException("Invoice FromNo. must be greater than 1!");
		}
		
		if(fromNo > toNo) {
			throw new ElidomRuntimeException("Invoice FromNo. must be less than ToNo. (FromNo. : " + fromNo + ", ToNo. : " + toNo + ")!");
		}
		
		int generatedCount = 0;
		List<Invoice> invoiceNoList = new ArrayList<Invoice>(1000);
		
		for(long i = fromNo ; i <= toNo ; i++) {
			String trackNo = this.generateInvoiceNo(i);
			
			if(ValueUtil.isNotEmpty(trackNo)) {
				generatedCount++;
				Invoice invoiceNo = new Invoice();
				invoiceNo.setId(trackNo);
				invoiceNo.setComCd(comCd);
				invoiceNo.setCustomerCd(customerCd);
				invoiceNo.setDomainId(domainId);
				invoiceNo.setUsedFlag(false);
				invoiceNoList.add(invoiceNo);
				
				if(invoiceNoList.size() == 1000) {
					this.queryManager.insertBatch(invoiceNoList);
					invoiceNoList.clear();
				}
			}
		}
		
		if(!invoiceNoList.isEmpty()) {
			this.queryManager.insertBatch(invoiceNoList);
			invoiceNoList.clear();
		}
		
		return generatedCount;
	}

	@Override
	public String nextInvoiceId(Long domainId, String stageCd, String comCd, String customerCd) {
		
		// 1. InvoiceNo 리소스로 락킹
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("id", "name", "description");
		condition.addFilter("name", "Invoice");
		@SuppressWarnings("unused")
		Resource invoiceNoRcs = this.queryManager.selectByConditionWithLock(Resource.class, condition);
		
		// 2. 사용하지 않은 송장 번호를 하나 추출
		String sql = "SELECT ID, USED_FLAG, COM_CD FROM INVOICES WHERE DOMAIN_ID = :domainId AND USED_FLAG = :usedFlag #if($comCd) AND COM_CD = :comCd #end #if($customerCd) AND CUSTOMER_CD = :customerCd #end ORDER BY ID ASC";
		Map<String, Object> params = ValueUtil.newMap("domainId,usedFlag", domainId, false);
		if(ValueUtil.isNotEmpty(comCd)) {
			params.put("comCd", comCd);
		}
		if(ValueUtil.isNotEmpty(customerCd)) {
			params.put("customerCd", customerCd);
		}
		List<Invoice> invoiceNoList = this.queryManager.selectListBySql(sql, params, Invoice.class, 1, 1);
		
		if(ValueUtil.isEmpty(invoiceNoList)) {
			if(ValueUtil.isEmpty(comCd)) {
				throw new ElidomRuntimeException(MessageUtil.getMessage("NOT_EXIST_AVAILABLE_INVOICE"));
			} else {
				throw new ElidomRuntimeException(MessageUtil.getMessage("NO_INVOICE_AVAILABLE_CUSTOMER","고객사 [{0}]용으로 사용 가능한 송장 번호가 존재하지 않습니다",ValueUtil.newStringList(comCd)));
			}
		}
		
		// 3. 사용했다는 플래그를 올린 후 저장
		Invoice invoiceNo = invoiceNoList.get(0);
		invoiceNo.setUsedFlag(true);
		this.queryManager.update(invoiceNo, "usedFlag", "updatedAt");
		
		// 4. 송장 번호를 리턴
		return invoiceNo.getId();
	}

	/**
	 * trackNo를 받아 송장 번호를 생성 
	 * 
	 * @param trackNo
	 * @return
	 */
	private String generateInvoiceNo(long trackNo) {
		
		// 1. trackNo의 앞 두 자리를 붙인다.
		StringBuffer invoiceNo = new StringBuffer();
		String trackNoStr = ValueUtil.toString(trackNo);
		invoiceNo.append(trackNoStr.substring(0, 2));
		
		// 2. trackNo의 세 번째부터 열 한번째 자름 
		String targetNoStr = trackNoStr.substring(2, 11);
		
		// 3. 2에서 만들어진 숫자에 오른쪽에서 9 번째 자리를 잘라서 추가
		int firstIdx = targetNoStr.length() - 9;
		String middleNoStr = targetNoStr.substring(firstIdx, firstIdx + 9);
		invoiceNo.append(middleNoStr);
		
		// 4. checkBit를 마지막에 추가
		// 2번의 자른 문자열을 숫자로 변환
		int targetNo = ValueUtil.toInteger(targetNoStr);
		int checkBit = targetNo % 7;
		invoiceNo.append(checkBit);
		
		// 5. 리턴
		return invoiceNo.toString();
	}
}
