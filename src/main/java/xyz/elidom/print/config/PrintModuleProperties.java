/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.print.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * print 모듈 정보 파일
 * 
 * @author shortstop
 */
@Component("printModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/print.properties")
public class PrintModuleProperties implements IModuleProperties {
	
	/**
	 * 모듈명
	 */
	@Value("${elings.print.name}")
	private String name;
	
	/**
	 * 버전
	 */
	@Value("${elings.print.version}")
	private String version;
	
	/**
	 * Module Built Time 
	 */
	@Value("${elings.print.built.at}")
	private String builtAt;	
	
	/**
	 * 모듈 설명
	 */
	@Value("${elings.print.description}")
	private String description;
	
	/**
	 * 부모 모듈
	 */
	@Value("${elings.print.parentModule}")
	private String parentModule;
	
	/**
	 * 모듈 Base Package
	 */
	@Value("${elings.print.basePackage}")
	private String basePackage;
	
	/**
	 * Scan Service Path
	 */
	@Value("${elings.print.scanServicePackage}")
	private String scanServicePackage;
	
	/**
	 * Scan Entity Path
	 */
	@Value("${elings.print.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * @return
	 */
	@Value("${elings.print.projectName}")
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