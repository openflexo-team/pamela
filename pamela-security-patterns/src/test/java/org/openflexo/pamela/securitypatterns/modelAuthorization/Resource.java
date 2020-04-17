package org.openflexo.pamela.securitypatterns.modelAuthorization;

import org.openflexo.pamela.annotations.*;
import org.openflexo.pamela.factory.AccessibleProxyObject;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AccessResource;
import org.openflexo.pamela.securitypatterns.authorization.annotations.PermissionCheckerGetter;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ProtectedResource;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ResourceID;

@ProtectedResource(patternID = PermissionChecker.PATTERN)
@ModelEntity
@ImplementationClass(Resource.ResourceImp.class)
public interface Resource extends AccessibleProxyObject {
	String ID = "resid";
	String R1 = "r1";
	String CHECKER = "checker";

	@Initializer
	default void init(String id, Double R1, PermissionChecker checker) {
		setChecker(checker);
		setID(id);
		setR1(R1);
	}

	@Initializer
	default void init(String id, Double R1){
		setID(id);
		setR1(R1);
	}

	@ResourceID(patternID = PermissionChecker.PATTERN, paramID = PermissionChecker.RESOURCEID)
	@Getter(value = ID)
	String getID();

	@Setter(value = ID)
	void setID(String val);

	@PermissionCheckerGetter(patternID = PermissionChecker.PATTERN)
	@Getter(value = CHECKER)
	PermissionChecker getChecker();

	@Setter(value = CHECKER)
	void setChecker(PermissionChecker checker);

	@Getter(value = R1, defaultValue = "0")
	@AccessResource(patternID = PermissionChecker.PATTERN, methodID = "get")
	double getR1();

	@Setter(value = R1)
	@AccessResource(patternID = PermissionChecker.PATTERN, methodID = "set")
	void setR1(double val);

	@Override
	String toString();

	abstract class ResourceImp implements Resource{

		@Override
		public String toString() {
			return "Resource " + getID();
		}
	}
}
