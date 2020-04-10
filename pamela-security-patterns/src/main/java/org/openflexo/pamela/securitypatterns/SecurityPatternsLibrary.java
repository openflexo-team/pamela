package org.openflexo.pamela.securitypatterns;

import org.openflexo.pamela.patterns.DeclarePatterns;
import org.openflexo.pamela.patterns.PatternLibrary;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternFactory;
import org.openflexo.pamela.securitypatterns.authorization.AuthorizationPatternFactory;

@DeclarePatterns({ AuthenticatorPatternFactory.class, AuthorizationPatternFactory.class })
public class SecurityPatternsLibrary implements PatternLibrary {

}
