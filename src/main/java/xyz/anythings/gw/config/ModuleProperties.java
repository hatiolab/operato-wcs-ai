/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.anythings.gw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * 게이트웨이 API 모듈 정보 파일
 * 
 * @author shortstop
 */
@Component("anythingsGwModuleProperties")
@EnableConfigurationProperties
@PropertySource("classpath:/properties/anythings-gw.properties")
public class ModuleProperties implements IModuleProperties {

	/**
	 * 모듈명
	 */
	@Value("${anythings.gw.name}")
	private String name;

	/**
	 * 버전
	 */
	@Value("${anythings.gw.version}")
	private String version;

	/**
	 * Module Built Time
	 */
	@Value("${anythings.gw.built.at}")
	private String builtAt;

	/**
	 * 모듈 설명
	 */
	@Value("${anythings.gw.description}")
	private String description;

	/**
	 * 부모 모듈
	 */
	@Value("${anythings.gw.parentModule}")
	private String parentModule;

	/**
	 * 모듈 Scada Package
	 */
	@Value("${anythings.gw.basePackage}")
	private String basePackage;

	/**
	 * Scan Service Path
	 */
	@Value("${anythings.gw.scanServicePackage}")
	private String scanServicePackage;

	/**
	 * Scan Entity Path
	 */
	@Value("${anythings.gw.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * 모듈에서 사용할 rabbitmq 큐 명칭 
	 */
	@Value("${anythings.gw.rabbitQueue:not_use}")
	private String rabbitQueue;
	/**
	 * Project Name
	 * 
	 * @return
	 */
	@Value("${anythings.gw.projectName}")
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

	public String getRabbitQueue() {
		return this.rabbitQueue;
	}

	@Override
	public String toString() {
		return FormatUtil.toJsonString(this);
	}
}