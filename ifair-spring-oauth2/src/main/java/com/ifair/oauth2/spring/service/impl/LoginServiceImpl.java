package com.ifair.oauth2.spring.service.impl;

import com.ifair.oauth2.spring.service.LoginService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feiying on 16/9/13.
 */
@Service
public class LoginServiceImpl implements LoginService, AuthenticationProvider {

	public boolean login() {
		System.out.printf("aaaaaaaaaa");
		return false;
	}

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		List list = new ArrayList();
		User userDetails = new User("test", "123456", list);
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(userDetails, authentication.getCredentials(), userDetails.getAuthorities());
		return result;
	}

	public boolean supports(Class<?> aClass) {
		return true;
	}
}
