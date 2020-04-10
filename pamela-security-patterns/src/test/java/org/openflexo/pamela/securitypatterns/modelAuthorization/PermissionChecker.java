package org.openflexo.pamela.securitypatterns.modelAuthorization;

import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.securitypatterns.authorization.annotations.AuthorizationChecker;
import org.openflexo.pamela.securitypatterns.authorization.annotations.CheckAccess;
import org.openflexo.pamela.securitypatterns.authorization.annotations.MethodID;
import org.openflexo.pamela.securitypatterns.authorization.annotations.ResourceID;
import org.openflexo.pamela.securitypatterns.authorization.annotations.SubjectID;

@AuthorizationChecker(patternID = PermissionChecker.PATTERN)
@ModelEntity
public interface PermissionChecker {
	String PATTERN = "pattern";
	String SUBJECTID = "subject";
	String RESOURCEID = "resource";
	String SUBJECTSTRINGID = "subjectStringID";

	@CheckAccess(patternID = PATTERN)
	default boolean check(@SubjectID(patternID = PATTERN, paramID = SUBJECTSTRINGID) String strID,
			@SubjectID(patternID = PATTERN, paramID = SUBJECTID) int subId,
			@ResourceID(patternID = PATTERN, paramID = RESOURCEID) String resId, @MethodID(patternID = PATTERN) String methodID) {
		return (strID.compareTo("admin") == 0 && subId == 2 && (resId.compareTo("Pool") == 0 || resId.compareTo("Pool2") == 0)
				&& (methodID.compareTo("get") == 0 || methodID.compareTo("set") == 0))
				|| (subId == 1 && (resId.compareTo("Pool") == 0 || resId.compareTo("Pool2") == 0) && (methodID).compareTo("get") == 0);
	}

}
