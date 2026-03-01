/* Copyright © HatioLab Inc. All rights reserved. */
package operato.logis.light.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * 샘플 Boot 모듈 정보 파일
 * 
 * @author yang
 */
@Component("logisLightModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/operato-logis-light.properties")
public class ModuleProperties implements IModuleProperties {

	/**
	 * 모듈명
	 */
	@Value("${operato.logis.light.name}")
	private String name;

	/**
	 * 버전
	 */
	@Value("${operato.logis.light.version}")
	private String version;

	/**
	 * Module Built Time
	 */
	@Value("${operato.logis.light.built.at}")
	private String builtAt;

	/**
	 * 모듈 설명
	 */
	@Value("${operato.logis.light.description}")
	private String description;

	/**
	 * 부모 모듈
	 */
	@Value("${operato.logis.light.parentModule}")
	private String parentModule;

	/**
	 * 모듈 Scada Package
	 */
	@Value("${operato.logis.light.basePackage}")
	private String basePackage;

	/**
	 * Scan Service Path
	 */
	@Value("${operato.logis.light.scanServicePackage}")
	private String scanServicePackage;

	/**
	 * Scan Entity Path
	 */
	@Value("${operato.logis.light.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * 
	 * @return
	 */
	@Value("${operato.logis.light.projectName}")
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