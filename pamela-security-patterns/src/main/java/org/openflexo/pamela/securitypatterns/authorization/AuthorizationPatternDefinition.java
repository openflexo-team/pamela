package org.openflexo.pamela.securitypatterns.authorization;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.PamelaUtils;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.authorization.annotations.*;

/**
 * Represents an occurrence of an <code>Authorization Pattern</code>. An instance is uniquely identified by the <code>patternID</code> field
 * of associated annotations.<br>
 *
 * It has the responsibility of:
 * <ul>
 * <li>Managing life-cycle of {@link org.openflexo.pamela.securitypatterns.authorization.AuthorizationPatternInstance}, while being notified of the creation of new instances by the
 * {@link org.openflexo.pamela.factory.ModelFactory} and {@link org.openflexo.pamela.ModelContext}</li>
 * <li>Tagging and storing methods which are relevant to the pattern</li>
 * </ul>
 *
 * @author Caine Silva, Sylvain Guerin
 *
 */
public class AuthorizationPatternDefinition extends PatternDefinition {

	protected final static String SUBJECT_ROLE = "Subject";
	protected final static String RESOURCE_ROLE = "Resource";

	/**
	 * Wrapper of all relevant information related to subject accessMethods.
	 */
	public static class SubjectAccessMethodWrapper{

		private final String methodID; //method id of the access method
		private final ArrayList<Integer> realIndexes; // indexes of the actual parameter of the corresponding resource access method.
		private final HashMap<String, Integer> paramMapping; // map between the resource identifier paramID and the index of the parameter for the method

		private SubjectAccessMethodWrapper(String methodID, ArrayList<Integer> realIndexes, HashMap<String, Integer> paramMapping){
			this.methodID = methodID;
			this.realIndexes = realIndexes;
			this.paramMapping = paramMapping;
		}

		protected String getMethodID() {
			return methodID;
		}

		protected ArrayList<Integer> getRealIndexes() {
			return realIndexes;
		}

		protected HashMap<String, Integer> getParamMapping() {
			return paramMapping;
		}
	}

	private ModelEntity<?> subject; //@AuthorizationSubject
	private ModelEntity<?> resource; //@ProtectedResource
	private ModelEntity<?> checker; // @PermissionChecker

	//Subject
	private final HashMap<String, Method> subjectIdGetters;//@SubjectID getters
	private final HashMap<Method, SubjectAccessMethodWrapper> subjectAccessMethods; //@AccessResource im Subject

	//Resource
	private final HashMap<String, Method> resourceAccessMethods; //@AccessResource in Resource
	private final HashMap<String, Method> resourceIdGetters;//@ResourceID getters
	private Method checkerGetter; //@PermissionCheckerGetter

	//PermissionChecker
	private final HashMap<String, Integer> subjectIdParameters;//@SubjectID
	private final HashMap<String, Integer> resourceIdParameters;//@ResourceID
	private Method checkMethod; //@CheckAccess
	private int methodIdIndex; //@MethodID

	private boolean isValid;
	private String message;

	public AuthorizationPatternDefinition(String identifier, ModelContext modelContext) {
		super(identifier, modelContext);
		this.subjectIdParameters = new HashMap<>();
		this.resourceIdParameters = new HashMap<>();
		this.resourceAccessMethods = new HashMap<>();
		this.resourceIdGetters = new HashMap<>();
		this.subjectIdGetters = new HashMap<>();
		this.subjectAccessMethods = new HashMap<>();
		this.methodIdIndex = -1;
		this.isValid = true;
		this.message = "\n";
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {
		for (SubjectAccessMethodWrapper wrapper : this.subjectAccessMethods.values()){
			if (!this.resourceAccessMethods.containsKey(wrapper.getMethodID())){ //Check if access methods are coherent between subjects and resources
				this.isValid = false;
				message += String.format("Unknown access method %s in Resource class.\n", wrapper.getMethodID());
			}
			for (String id : wrapper.getParamMapping().keySet()){ //Check if resource ids as subject access method parameters are valid
				if (!this.resourceIdParameters.containsKey(id)){
					this.isValid = false;
					message += String.format("Unknown resource identifier %s in subject accessMethod.\n", id);
				}
			}
		}
		//Check if ids are coherent with check method
		for (String subjectId : subjectIdParameters.keySet()){
			if (!this.subjectIdGetters.containsKey(subjectId)){
				this.isValid = false;
				this.message += String.format("Unknown subject identifier %s in check method.\n", subjectId);
			}
		}
		for (String resourceId : resourceIdParameters.keySet()){
			if (!this.resourceIdGetters.containsKey(resourceId)){
				this.isValid = false;
				this.message += String.format("Unknown subject identifier %s in check method.\n", resourceId);
			}
		}
		if (!this.isValid){
			throw new ModelDefinitionException(this.message);
		}
	}

	@Override
	public  <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
		if (modelEntity == this.subject || modelEntity == this.resource){
			Set<?> instanceSet = this.getModelContext().getPatternInstances(this);
			AuthorizationPatternInstance patternInstance;
			if (instanceSet == null){
				patternInstance = new AuthorizationPatternInstance(this);
			}
			else {
				patternInstance = (AuthorizationPatternInstance) instanceSet.iterator().next();
			}
			if (modelEntity == this.subject){
				patternInstance.attachSubject(newInstance);
			}
			else if (modelEntity == this.resource){
				patternInstance.attachResource(newInstance);
			}
		}
	}

	protected boolean isValid() {
		return this.isValid;
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method m) {
		for (Method method: this.resourceAccessMethods.values()){
			if (PamelaUtils.methodIsEquivalentTo(m, method)){
				return true;
			}
		}
		for (Method method : this.subjectAccessMethods.keySet()){
			if (PamelaUtils.methodIsEquivalentTo(m, method)){
				return true;
			}
		}
		return PamelaUtils.methodIsEquivalentTo(m, this.checkMethod);
	}

	protected void addSubjectModelEntity(ModelEntity<?> entity) {
		if (this.subject != null){
			this.isValid = false;
			this.message += "Duplicate @AuthorizationSubject annotation with same pattern id " + getIdentifier() + "in model." + System.lineSeparator();
		}
		else {
			this.subject = entity;
			for (Method m : entity.getImplementedInterface().getMethods()) {
				SubjectID idAnnotation = m.getAnnotation(SubjectID.class);
				if (idAnnotation != null && idAnnotation.patternID().compareTo(this.getIdentifier()) == 0) {
					if (!this.subjectIdGetters.containsKey(idAnnotation.paramID())) {
						this.subjectIdGetters.put(idAnnotation.paramID(), m);
					} else {
						message += "Duplicate @SubjectID annotation with same pattern ID (" + this.getIdentifier()
								+ ") and paramID (" + idAnnotation.paramID() + ") in " + entity.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
					}
				}
				AccessResource accessResourceAnnotation = m.getAnnotation(AccessResource.class);
				if (accessResourceAnnotation != null && accessResourceAnnotation.patternID().compareTo(this.getIdentifier()) == 0) {
					if (!this.subjectAccessMethods.containsKey(m)) {

						HashMap<String, Integer> paramMapping = new HashMap<>();
						ArrayList<Integer> realParams = new ArrayList<>();
						for (int i = 0; i < m.getParameterCount(); i++) {
							Parameter param = m.getParameters()[i];
							ResourceID a = param.getAnnotation(ResourceID.class);
							if (a != null && a.patternID().compareTo(this.getIdentifier()) == 0) {
								if (!paramMapping.containsKey(a.paramID())) {
									paramMapping.put(a.paramID(), i);
								} else {
									message += String.format("Duplicate ResourceID annotation with same paramID %s in method %s parameters of class %s",
											a.paramID(), m.getName(), entity.getImplementedInterface().getSimpleName()) + "." + System.lineSeparator();
								}
							} else {
								realParams.add(i);
							}
						}
						this.subjectAccessMethods.put(m, new SubjectAccessMethodWrapper(accessResourceAnnotation.methodID(), realParams, paramMapping));
					}
				}
			}
		}
	}

	protected void addResourceModelEntity(ModelEntity<?> entity) {
		if (this.resource != null){
			this.isValid = false;
			this.message += "Duplicate @ProtectedResource annotation with same pattern id " + getIdentifier() + "in model." + System.lineSeparator();
		}
		else {
			this.resource = entity;
			for (Method m : entity.getImplementedInterface().getMethods()) {
				ResourceID idAnnotation = m.getAnnotation(ResourceID.class);
				if (idAnnotation != null && idAnnotation.patternID().compareTo(getIdentifier()) == 0) {
					if (!this.resourceIdGetters.containsKey(idAnnotation.paramID())) {
						this.resourceIdGetters.put(idAnnotation.paramID(), m);
					}
					else {
						this.message += "Duplicate @ResourceID annotation with same pattern ID (" + this.getIdentifier()
								+ ") and paramID (" + idAnnotation.paramID() + ") in " + entity.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
					}
				}
				AccessResource accessResourceAnnotation = m.getAnnotation(AccessResource.class);
				if (accessResourceAnnotation != null && accessResourceAnnotation.patternID().compareTo(getIdentifier()) == 0) {
					if (!this.resourceAccessMethods.containsKey(accessResourceAnnotation.methodID())) {
						this.resourceAccessMethods.put(accessResourceAnnotation.methodID(), m);
					}
					else {
						message += "Duplicate methodID " + accessResourceAnnotation.methodID() + " in class " + entity.getImplementedInterface().getSimpleName();
					}
				}
				PermissionCheckerGetter checkerGetterAnnotation = m.getAnnotation(PermissionCheckerGetter.class);
				if (checkerGetterAnnotation != null && checkerGetterAnnotation.patternID().compareTo(getIdentifier()) == 0) {
					if (this.checkerGetter == null) {
						this.checkerGetter = m;
					}
					else {
						message += "Duplicate @PermissionCheckerGetter annotated methods with same patternID "
								+ checkerGetterAnnotation.patternID() + " in class " + entity.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
					}
				}
			}
		}
	}

	protected void addCheckerModelEntity(ModelEntity<?> entity) {
		if (this.checker != null){
			this.isValid = false;
			this.message += "Duplicate @AuthorizationChecker annotation with same pattern id " + getIdentifier() + "in model." + System.lineSeparator();
		}
		else {
			this.checker = entity;
			for (Method m : entity.getImplementedInterface().getMethods()) {
				CheckAccess checkAnnotation = m.getAnnotation(CheckAccess.class);
				if (checkAnnotation != null && checkAnnotation.patternID().compareTo(this.getIdentifier()) == 0) {
					if (this.checkMethod == null) {
						if (!boolean.class.isAssignableFrom(m.getReturnType())) {
							this.isValid = false;
							this.message += String.format("Check method %s in class %s does not return a boolean.%s", m.getName(), checker.getImplementedInterface().getSimpleName(), System.lineSeparator());
						}
						this.checkMethod = m;
						for (int i = 0; i < m.getParameters().length; i++) {
							Parameter param = m.getParameters()[i];
							SubjectID subjectAnnotation = param.getAnnotation(SubjectID.class);
							ResourceID resourceAnnotation = param.getAnnotation(ResourceID.class);
							if (subjectAnnotation != null && resourceAnnotation != null) {
								this.isValid = false;
								this.message += "Parameter with both @SubjectID and @ResourceID annotations with same patternID " + this.getIdentifier() + " in class " + checker.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
							}
							if (subjectAnnotation != null && subjectAnnotation.patternID().compareTo(this.getIdentifier()) == 0) {
								if (!this.subjectIdParameters.containsKey(subjectAnnotation.paramID())) {
									this.subjectIdParameters.put(subjectAnnotation.paramID(), i);
								}
								else {
									this.isValid = false;
									this.message += "Duplicate @SubjectID annotation with same patternID "
											+ subjectAnnotation.patternID() + " and paramID " + subjectAnnotation.paramID() + " in method "
											+ m.getName() + " of class " + checker.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
								}
							}
							else if (resourceAnnotation != null && resourceAnnotation.patternID().compareTo(this.getIdentifier()) == 0) {
								if (!this.resourceIdParameters.containsKey(resourceAnnotation.paramID())) {
									this.resourceIdParameters.put(resourceAnnotation.paramID(), i);
								}
								else {
									this.isValid = false;
									this.message += "Duplicate @ResourceID annotation with same patternID "
											+ resourceAnnotation.patternID() + " and paramID " + resourceAnnotation.paramID() + " in method "
											+ m.getName() + " of class " + checker.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
								}
							}
							else if (this.methodIdIndex == -1) {
								this.methodIdIndex = i;
							}
							else {
								this.isValid = false;
								this.message += "Unidentified parameter at position " + i + " in method "
										+ m.getName() + " in class " + checker.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
							}
						}
					}
					else {
						this.isValid = false;
						this.message += "Duplicate @CheckAccess method with same patternID "
								+ checkAnnotation.patternID() + " in class " + checker.getImplementedInterface().getSimpleName() + "." + System.lineSeparator();
					}
				}
			}
		}
	}

	protected ModelEntity<?> getChecker() {
		return checker;
	}

	protected ModelEntity<?> getResource() {
		return this.resource;
	}

	protected ModelEntity<?> getSubject() {
		return this.subject;
	}

	protected HashMap<String, Method> getResourceAccessMethods() {
		return resourceAccessMethods;
	}

	protected HashMap<String, Method> getResourceIdGetters() {
		return resourceIdGetters;
	}

	protected Method getCheckerGetter() {
		return checkerGetter;
	}

	protected HashMap<String, Integer> getSubjectIdParameters() {
		return subjectIdParameters;
	}

	protected HashMap<String, Integer> getResourceIdParameters() {
		return resourceIdParameters;
	}

	protected Method getCheckMethod() {
		return checkMethod;
	}

	protected int getMethodIdIndex() {
		return methodIdIndex;
	}

	protected HashMap<String, Method> getSubjectIdGetters() {
		return subjectIdGetters;
	}

	protected HashMap<Method, SubjectAccessMethodWrapper> getSubjectAccessMethods() {
		return subjectAccessMethods;
	}
}
