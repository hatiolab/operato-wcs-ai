package xyz.elidom.orm.entity.basic;

/**
 * Entity가 CRUD 오퍼레이션 전 후에 할 커스텀 서비스 액션들을 정의한다.
 * 
 * @author shortstop
 */
public interface ICustomCrudHook {
	/**
	 * CRUD 커스텀 서비스 이름을 리턴
	 * 
	 * @param point action point : before or after 
	 * @param crudAction crud action : create or update or delete or find
	 * @return
	 */
	public String getCustomCrudServiceName(String point, String crudAction);
	
	/**
	 * CRUD 커스텀 서비스 실행
	 * 
	 * @param customSvcName
	 */
	public void doCustomCrudService(String customSvcName);
}
