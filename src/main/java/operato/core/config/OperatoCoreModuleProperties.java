package operato.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import xyz.elidom.sys.system.config.module.IModuleProperties;
import xyz.elidom.util.FormatUtil;

/**
 * Operato Core 모듈 정보 파일
 * 
 * @author shortstop
 */
@Component
@EnableConfigurationProperties
@PropertySource("classpath:/properties/operato-core.properties")
public class OperatoCoreModuleProperties implements IModuleProperties {
	/**
	 * 모듈명
	 */
	@Value("${operato.core.name}")
	private String name;
	
	/**
	 * 버전
	 */
	@Value("${operato.core.version}")
	private String version;
	
	/**
	 * 최근 모듈 빌드 시간  
	 */
	@Value("${operato.core.built.at}")
	private String builtAt;
	
	/**
	 * 모듈 설명
	 */
	@Value("${operato.core.description}")
	private String description;
	
	/**
	 * 부모 모듈 
	 */
	@Value("${operato.core.parentModule}")
	private String parentModule;
	
	/**
	 * 모듈 Base Package
	 */
	@Value("${operato.core.basePackage}")
	private String basePackage;
	
	/**
	 * Scan Service Path
	 */
	@Value("${operato.core.scanServicePackage}")
	private String scanServicePackage;
	
	/**
	 * Scan Entity Path
	 */
	@Value("${operato.core.scanEntityPackage}")
	private String scanEntityPackage;
	
	/**
	 * Project Name
	 * 
	 * @return
	 */
	@Value("${operato.core.projectName}")
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
