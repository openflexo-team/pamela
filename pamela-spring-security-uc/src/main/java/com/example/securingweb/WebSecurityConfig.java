package com.example.securingweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.example.securingweb.authentication.AuthManagerService;
import com.example.securingweb.authentication.CustomAuthenticationProvider;
import com.example.securingweb.authentication.MyHttpSessionEventPublisher;
import com.example.securingweb.authentication.MyUserDetailsService;

// import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
//import org.springframework.security.core.userdetails.User;

@Configuration
@EnableWebSecurity
// @ImportResource({ "classpath:webSecurityConfig.xml" })
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private MyUserDetailsService userDetailsService;

	@Autowired
	private AuthManagerService authManagerService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests()
				.antMatchers("/", "/home", "/register", "/js/**", "/css/**", "/images/**", "/fonts/**", "/login/**", "/successRegister",
						"/doRegister", "/h2/**")
				.permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll().loginProcessingUrl("/dologin")
				.defaultSuccessUrl("/brest", true)
				// .failureUrl("/login.html?error=true")
				.failureHandler(authenticationFailureHandler()).and().logout().permitAll().and().sessionManagement()
				// .sessionFixation().migrateSession()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).invalidSessionUrl("/invalidSession.html").maximumSessions(2)
				.expiredUrl("/sessionExpired.html");
		// @formatter:on

		// SessionAuthenticationStrategy sessionAuthenticationStrategy = http
		// .getSharedObject(SessionAuthenticationStrategy.class);

		// http.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy)
	}

	@Bean
	public AuthenticationProvider daoAuthenticationProvider() {
		// DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

		CustomAuthenticationProvider provider = authManagerService.getAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(this.userDetailsService);
		return provider;
	}

	/*
	 * @Autowired public void configureGlobal(AuthenticationManagerBuilder auth)
	 * throws Exception {
	 * auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder()
	 * ); }
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	// @Autowired
	// private LogoutSuccessHandler logoutSuccessHandler() {
	// TODO Auto-generated method stub
	// return new CustomLogoutSuccessHandler();
	// return null;
	// }

	@Autowired
	public AuthenticationFailureHandler authenticationFailureHandler() {

		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @Autowired public void configureGlobal(AuthenticationManagerBuilder auth)
	 * throws Exception { auth .inMemoryAuthentication()
	 * .withUser("user").password("password").roles("USER","ADMIN"); }
	 */

	/*
	 * @Bean
	 * 
	 * @Override public UserDetailsService userDetailsService() { UserDetails user =
	 * User.withDefaultPasswordEncoder() .username("user") .password("password")
	 * .roles("USER") .build(); System.out.println(user.getPassword()); return new
	 * InMemoryUserDetailsManager(user); }
	 */

	/*@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	
		// CustomAuthenticationProvider authProvider = authManagerService.getAuthenticationProvider();
		// auth.authenticationProvider(authProvider);
	
	}*/

	/*
	 * private AuthenticationSuccessHandler successHandler() { return new
	 * MySimpleUrlAuthenticationSuccessHandler(); }
	 */

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new MyHttpSessionEventPublisher();
	}

}
