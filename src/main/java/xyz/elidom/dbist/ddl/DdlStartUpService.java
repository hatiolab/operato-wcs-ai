package xyz.elidom.dbist.ddl;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import net.sf.common.util.ValueUtils;
import xyz.elidom.sys.SysConstants;

@Service
public class DdlStartUpService {

	@Resource
	public Environment env;

	@Autowired(required = false)
	private Ddl ddl;

	@Autowired(required = false)
	private InitialSetup initialSetup;

	//@EventListener({ ContextRefreshedEvent.class })
	//@Order(Ordered.LOWEST_PRECEDENCE)
	public void ready() {
		// 1. Table Space Setup
		String dataTBSpace = ValueUtils.toString(this.env.getProperty("dbist.ddl.tablespace.data", SysConstants.EMPTY_STRING));
		String idxTBSpace = ValueUtils.toString(this.env.getProperty("dbist.ddl.tablespace.idx", SysConstants.EMPTY_STRING));
		this.ddl.setTableSpace(dataTBSpace, idxTBSpace);

		// 2. ready setup
		if (initialSetup != null) {
			this.initialSetup.readySetup(this.env);
		}
	}
	
	public void start() {
		// initial data setup
		if (initialSetup != null) {
			this.initialSetup.initialSetup(this.env);
		}
	}
}