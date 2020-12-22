package com.example.securingweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.example.securingweb.authentication.AuthenticationInterceptor;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	// Cf https://o7planning.org/fr/11689/tutoriel-spring-boot-interceptor
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// LogInterceptor apply to all URLs.
		registry.addInterceptor(new AuthenticationInterceptor());

		// Old Login url, no longer use.
		// Use OldURLInterceptor to redirect to a new URL.
		// registry.addInterceptor(new OldLoginInterceptor())//
		// .addPathPatterns("/admin/oldLogin");

		// This interceptor apply to URL like /admin/*
		// Exclude /admin/oldLogin
		// registry.addInterceptor(new AdminInterceptor())//
		// .addPathPatterns("/admin/*")//
		// .excludePathPatterns("/admin/oldLogin");
	}

}
