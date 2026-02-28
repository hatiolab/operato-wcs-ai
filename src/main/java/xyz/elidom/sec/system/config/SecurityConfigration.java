package xyz.elidom.sec.system.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 시큐리티 필터 관련 설정
 * 
 * @author shortstop
 */
@Configuration
@EnableWebSecurity
public class SecurityConfigration {

	@Resource
	private Environment env;
	
	@SuppressWarnings("removal")
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {	
		/*http.authorizeRequests()
			.requestMatchers("**")
			.permitAll()
			.anyRequest().authenticated()
            .and()
        	.sessionManagement()
        	.and()
			.securityContext()
			.and()
        	.csrf().disable();
		
		http.authorizeRequests()
			.and()
        	.headers()
        	.frameOptions()
        	.disable();
		
		return http.build();*/
		
		// Token 사용 방식이기 때문에 csrf disable 
		http.httpBasic().disable().csrf().disable();
		
		// 세션을 사용하지 않기 때문에 STATELESS로 설정
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		// HttpServletRequest를 사용하는 요청들에 대한 접근제한을 설정하겠다.
		// http.authorizeHttpRequests().anyRequest().permitAll();

//        .exceptionHandling()
//        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//        .accessDeniedHandler(jwtAccessDeniedHandler)
//        .and()
//        .authorizeHttpRequests() // HttpServletRequest를 사용하는 요청들에 대한 접근제한을 설정하겠다.
//        //.requestMatchers("/api/authenticate").permitAll() // 로그인 api
//        //.requestMatchers("/api/signup").permitAll() // 회원가입 api
//        //.requestMatchers(PathRequest.toH2Console()).permitAll()// h2-console, favicon.ico 요청 인증 무시
//        .requestMatchers("/favicon.ico").permitAll()
//        .anyRequest().authenticated() // 그 외 인증 없이 접근X
//        .and()
//        .apply(new JwtSecurityConfig(tokenProvider)); // JwtFilter를 addFilterBefore로 등록했던 JwtSecurityConfig class 적용

        return http.build();
	}
}
