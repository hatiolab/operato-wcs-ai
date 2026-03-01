/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.ops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * operato-logis-ops 모듈 정보 파일
 * 
 * @author shortstop
 */
@Component("operatoLogisOpsModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/operato-logis-ops.properties")
public class ModuleProperties implements IModuleProperties {
	
	/**
	 * 모듈명
	 */
	@Value("${operato.logis.ops.name}")
	private String name;
	
	/**
	 * 버전
	 */
	@Value("${operato.logis.ops.version}")
	private String version;
	
	/**
	 * Module Built Time 
	 */
	@Value("${operato.logis.ops.built.at}")
	private String builtAt;	
	
	/**
	 * 모듈 설명
	 */
	@Value("${operato.logis.ops.description}")
	private String description;
	
	/**
	 * 부모 모듈
	 */
	@Value("${operato.logis.ops.parentModule}")
	private String parentModule;
	
	/**
	 * 모듈 Base Package
	 */
	@Value("${operato.logis.ops.basePackage}")
	private String basePackage;
	
	/**
	 * Scan Service Path
	 */
	@Value("${operato.logis.ops.scanServicePackage}")
	private String scanServicePackage;
	
	/**
	 * Scan Entity Path
	 */
	@Value("${operato.logis.ops.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * @return
	 */
	@Value("${operato.logis.ops.projectName}")
	private String projectName;
	
	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}
	
	public String getBuiltAt() {
		return builtAt;
	}

	public String getDescription() {
		return this.description;
	}
	
	public String getParentModule() {
		return this.parentModule;
	}

	public String getBasePackage() {
		return this.basePackage;
	}

	public String getScanServicePackage() {
		return this.scanServicePackage;
	}

	public String getScanEntityPackage() {
		return this.scanEntityPackage;
	}
	
	public String getProjectName() {
		return this.projectName;
	}

	@Override
	public String toString() {
		return FormatUtil.toJsonString(this);
	}
}