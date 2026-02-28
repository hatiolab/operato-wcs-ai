/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.comm.rabbitmq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * anythings-comm-rabbitmq 모듈 정보 파일
 * 
 * @author yang
 */
@Component("anythingsCommRabbitmqModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/anythings-comm-rabbitmq.properties")
public class ModuleProperties implements IModuleProperties {
	
	/**
	 * 모듈명
	 */
	@Value("${anythings.comm.rabbitmq.name}")
	private String name;
	
	/**
	 * 버전
	 */
	@Value("${anythings.comm.rabbitmq.version}")
	private String version;
	
	/**
	 * Module Built Time 
	 */
	@Value("${anythings.comm.rabbitmq.built.at}")
	private String builtAt;	
	
	/**
	 * 모듈 설명
	 */
	@Value("${anythings.comm.rabbitmq.description}")
	private String description;
	
	/**
	 * 부모 모듈
	 */
	@Value("${anythings.comm.rabbitmq.parentModule}")
	private String parentModule;
	
	/**
	 * 모듈 Base Package
	 */
	@Value("${anythings.comm.rabbitmq.basePackage}")
	private String basePackage;
	
	/**
	 * Scan Service Path
	 */
	@Value("${anythings.comm.rabbitmq.scanServicePackage}")
	private String scanServicePackage;
	
	/**
	 * Scan Entity Path
	 */
	@Value("${anythings.comm.rabbitmq.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * @return
	 */
	@Value("${anythings.comm.rabbitmq.projectName}")
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