/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.rabbitmq.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Basic Authentication Interceptor for RestTemplate
 * Replacement for deprecated BasicAuthorizationInterceptor in Spring 6.x
 */
public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {

	private final String username;
	private final String password;

	public BasicAuthInterceptor(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
		String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
		request.getHeaders().set("Authorization", authHeader);
		return execution.execute(request, body);
	}
}
