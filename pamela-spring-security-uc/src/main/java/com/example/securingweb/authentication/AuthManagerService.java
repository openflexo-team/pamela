package com.example.securingweb.authentication;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthManagerService {

	private CustomAuthenticationProvider authenticationProvider;

	private ModelFactory factory;

	public AuthManagerService() {
		ModelContext context;
		try {
			context = new ModelContext(CustomAuthenticationProvider.class);
			factory = new ModelFactory(context);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}

		authenticationProvider = factory.newInstance(CustomAuthenticationProvider.class);
	}

	public CustomAuthenticationProvider getAuthenticationProvider() {
		return authenticationProvider;
	}

	public SessionInfo makeNewSessionInfo() {
		SessionInfo returned = factory.newInstance(SessionInfo.class);
		returned.setAuthenticationProvider(getAuthenticationProvider());
		return returned;
	}

}
