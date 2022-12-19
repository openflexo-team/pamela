package com.example.securingweb.authentication;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthManagerService {

	private CustomAuthenticationProvider authenticationProvider;

	private PamelaModelFactory factory;

	public AuthManagerService() {
		PamelaMetaModel pamelaMetaModel;
		try {
			pamelaMetaModel = new PamelaMetaModel(CustomAuthenticationProvider.class);
			factory = new PamelaModelFactory(pamelaMetaModel);
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
