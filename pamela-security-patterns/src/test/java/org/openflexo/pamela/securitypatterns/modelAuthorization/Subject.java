package org.openflexo.pamela.securitypatterns.modelAuthorization;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AccessResource;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationSubject;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ResourceID;
import org.openflexo.pamela.securitypatterns.authorization.annotations.SubjectID;

@ModelEntity
@AuthorizationSubject(patternID = PermissionChecker.PATTERN)
public interface Subject {
	String ID = "id";
	String STRINGID = "stringid";

	@Initializer
	default void init(Integer id, String strID) {
		setID(id);
		setStringID(strID);
	}

	@Getter(value = ID, defaultValue = "-1")
	@SubjectID(patternID = PermissionChecker.PATTERN, paramID = PermissionChecker.SUBJECTID)
	int getID();

	@Getter(value = STRINGID)
	@SubjectID(patternID = PermissionChecker.PATTERN, paramID = PermissionChecker.SUBJECTSTRINGID)
	String getStringID();

	@Setter(value = STRINGID)
	void setStringID(String id);

	@Setter(value = ID)
	void setID(int val);

	@AccessResource(patternID = PermissionChecker.PATTERN, methodID = "get")
	double getResource(@ResourceID(patternID = PermissionChecker.PATTERN, paramID = PermissionChecker.RESOURCEID) String ResID);

	@AccessResource(patternID = PermissionChecker.PATTERN, methodID = "set")
	void setResource(@ResourceID(patternID = PermissionChecker.PATTERN, paramID = PermissionChecker.RESOURCEID) String ResId, double val);

}
