package xyz.elidom.core.entity;

import xyz.elidom.core.entity.relation.DataSrcRef;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.entity.basic.ElidomStampHook;

@Table(name = "procedures", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,name", indexes = {
		@Index(name = "ix_procedures_0", columnList = "domain_id,name", unique = true) })
public class Procedure extends ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 508440850958227096L;

	@PrimaryKey
	@Column(name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "name", nullable = false, length = 36)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "param1")
	private String param1;

	@Column(name = "param2")
	private String param2;

	@Column(name = "param3")
	private String param3;

	@Column(name = "param4")
	private String param4;

	@Column(name = "param5")
	private String param5;

	@Column(name = "param6")
	private String param6;

	@Column(name = "param7")
	private String param7;

	@Column(name = "param8")
	private String param8;

	@Column(name = "param9")
	private String param9;

	@Column(name = "param10")
	private String param10;

	@Ignore
	private String script;

	@Column(name = "data_src_id", length = 40)
	private String dataSrcId;

	@Relation(field = "dataSrcId")
	private DataSrcRef dataSrc;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getParam6() {
		return param6;
	}

	public void setParam6(String param6) {
		this.param6 = param6;
	}

	public String getParam7() {
		return param7;
	}

	public void setParam7(String param7) {
		this.param7 = param7;
	}

	public String getParam8() {
		return param8;
	}

	public void setParam8(String param8) {
		this.param8 = param8;
	}

	public String getParam9() {
		return param9;
	}

	public void setParam9(String param9) {
		this.param9 = param9;
	}

	public String getParam10() {
		return param10;
	}

	public void setParam10(String param10) {
		this.param10 = param10;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getDataSrcId() {
		return dataSrcId;
	}

	public void setDataSrcId(String dataSrcId) {
		this.dataSrcId = dataSrcId;
	}

	public DataSrcRef getDataSrc() {
		return dataSrc;
	}

	public void setDataSrc(DataSrcRef dataSrc) {
		this.dataSrc = dataSrc;
	
		if (this.dataSrc != null) {
			String refId = this.dataSrc.getId();
			if (refId != null)
				this.dataSrcId = refId;
		}
	
		if (this.dataSrcId == null) {
			this.dataSrcId = "";
		}
	}
}
