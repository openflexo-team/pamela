/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */
package org.openflexo.pamela.factory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.KeyValueCoding;
import org.openflexo.pamela.SpecifiableProxyObject;
import org.openflexo.pamela.model.PAMELAVisitor;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Utility interface used to capitalize constants in the context of PAMELA interpreter (see {@link ProxyMethodHandler}
 * 
 * @author sylvain
 *
 */
public class IProxyMethodHandler {

	public static Method PERFORM_SUPER_GETTER;
	public static Method PERFORM_SUPER_SETTER;
	public static Method PERFORM_SUPER_ADDER;
	public static Method PERFORM_SUPER_ADDER_AT_INDEX;
	public static Method PERFORM_SUPER_REMOVER;
	public static Method PERFORM_SUPER_DELETER;
	public static Method PERFORM_SUPER_UNDELETER;
	public static Method PERFORM_SUPER_FINDER;
	public static Method PERFORM_SUPER_GETTER_ENTITY;
	public static Method PERFORM_SUPER_SETTER_ENTITY;
	public static Method PERFORM_SUPER_ADDER_ENTITY;
	public static Method PERFORM_SUPER_REMOVER_ENTITY;
	public static Method PERFORM_SUPER_DELETER_ENTITY;
	public static Method PERFORM_SUPER_FINDER_ENTITY;
	public static Method PERFORM_SUPER_SET_MODIFIED;
	public static Method IS_MODIFIED;
	public static Method SET_MODIFIED;
	public static Method IS_SERIALIZING;
	public static Method IS_DESERIALIZING;
	public static Method TO_STRING;
	public static Method GET_PROPERTY_CHANGE_SUPPORT;
	public static Method GET_DELETED_PROPERTY;
	public static Method CLONE_OBJECT;
	public static Method CLONE_OBJECT_WITH_CONTEXT;
	public static Method IS_CREATED_BY_CLONING;
	public static Method IS_BEING_CLONED;
	public static Method DELETE_OBJECT;
	public static Method UNDELETE_OBJECT;
	public static Method IS_DELETED;
	public static Method EQUALS_OBJECT;
	public static Method EQUALS_OBJECT_USING_FILTER;
	public static Method UPDATE_WITH_OBJECT;
	public static Method ACCEPT_VISITOR;
	public static Method ACCEPT_WITH_STRATEGY_VISITOR;
	public static Method GET_EMBEDDED;
	public static Method GET_EMBEDDED_VALIDABLE;
	public static Method GET_REFERENCED;
	public static Method DESTROY;
	public static Method HAS_KEY;
	public static Method OBJECT_FOR_KEY;
	public static Method SET_OBJECT_FOR_KEY;
	public static Method GET_TYPE_FOR_KEY;
	public static Method ENABLE_ASSERTION_CHECKING;
	public static Method DISABLE_ASSERTION_CHECKING;

	public static final String DELETED = "deleted";
	public static final String UNDELETED = "undeleted";
	public static final String MODIFIED = "modified";
	public static final String DESERIALIZING = "deserializing";
	public static final String SERIALIZING = "serializing";

	static {
		try {
			PERFORM_SUPER_GETTER = AccessibleProxyObject.class.getMethod("performSuperGetter", String.class);
			PERFORM_SUPER_SETTER = AccessibleProxyObject.class.getMethod("performSuperSetter", String.class, Object.class);
			PERFORM_SUPER_ADDER = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class);
			PERFORM_SUPER_ADDER_AT_INDEX = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class,
					Integer.TYPE);
			PERFORM_SUPER_REMOVER = AccessibleProxyObject.class.getMethod("performSuperRemover", String.class, Object.class);
			PERFORM_SUPER_DELETER = DeletableProxyObject.class.getMethod("performSuperDelete",
					Array.newInstance(Object.class, 0).getClass());
			PERFORM_SUPER_UNDELETER = DeletableProxyObject.class.getMethod("performSuperUndelete", Boolean.TYPE);
			PERFORM_SUPER_FINDER = AccessibleProxyObject.class.getMethod("performSuperFinder", String.class, Object.class);
			PERFORM_SUPER_GETTER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperGetter", String.class, Class.class);
			PERFORM_SUPER_SETTER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperSetter", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_ADDER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperAdder", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_REMOVER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperRemover", String.class, Object.class,
					Class.class);
			PERFORM_SUPER_DELETER_ENTITY = DeletableProxyObject.class.getMethod("performSuperDelete", Class.class,
					Array.newInstance(Object.class, 0).getClass());
			PERFORM_SUPER_FINDER_ENTITY = AccessibleProxyObject.class.getMethod("performSuperFinder", String.class, Object.class,
					Class.class);
			IS_SERIALIZING = AccessibleProxyObject.class.getMethod("isSerializing");
			IS_DESERIALIZING = AccessibleProxyObject.class.getMethod("isDeserializing");
			IS_MODIFIED = AccessibleProxyObject.class.getMethod("isModified");
			IS_DELETED = DeletableProxyObject.class.getMethod("isDeleted");
			SET_MODIFIED = AccessibleProxyObject.class.getMethod("setModified", boolean.class);
			PERFORM_SUPER_SET_MODIFIED = AccessibleProxyObject.class.getMethod("performSuperSetModified", boolean.class);
			DELETE_OBJECT = DeletableProxyObject.class.getMethod("delete", Array.newInstance(Object.class, 0).getClass());
			UNDELETE_OBJECT = DeletableProxyObject.class.getMethod("undelete", Boolean.TYPE);
			GET_PROPERTY_CHANGE_SUPPORT = HasPropertyChangeSupport.class.getMethod("getPropertyChangeSupport");
			GET_DELETED_PROPERTY = HasPropertyChangeSupport.class.getMethod("getDeletedProperty");
			TO_STRING = Object.class.getMethod("toString");
			CLONE_OBJECT = CloneableProxyObject.class.getMethod("cloneObject");
			CLONE_OBJECT_WITH_CONTEXT = CloneableProxyObject.class.getMethod("cloneObject", Array.newInstance(Object.class, 0).getClass());
			IS_CREATED_BY_CLONING = CloneableProxyObject.class.getMethod("isCreatedByCloning");
			IS_BEING_CLONED = CloneableProxyObject.class.getMethod("isBeingCloned");
			EQUALS_OBJECT = AccessibleProxyObject.class.getMethod("equalsObject", Object.class);
			EQUALS_OBJECT_USING_FILTER = AccessibleProxyObject.class.getMethod("equalsObject", Object.class, Function.class);
			UPDATE_WITH_OBJECT = AccessibleProxyObject.class.getMethod("updateWith", Object.class);
			GET_EMBEDDED = AccessibleProxyObject.class.getMethod("getEmbeddedObjects");
			GET_EMBEDDED_VALIDABLE = Validable.class.getMethod("getEmbeddedValidableObjects");
			GET_REFERENCED = AccessibleProxyObject.class.getMethod("getReferencedObjects");
			ACCEPT_VISITOR = AccessibleProxyObject.class.getMethod("accept", PAMELAVisitor.class);
			ACCEPT_WITH_STRATEGY_VISITOR = AccessibleProxyObject.class.getMethod("accept", PAMELAVisitor.class,
					PAMELAVisitor.VisitingStrategy.class);
			DESTROY = AccessibleProxyObject.class.getMethod("destroy");
			HAS_KEY = KeyValueCoding.class.getMethod("hasKey", String.class);
			OBJECT_FOR_KEY = KeyValueCoding.class.getMethod("objectForKey", String.class);
			SET_OBJECT_FOR_KEY = KeyValueCoding.class.getMethod("setObjectForKey", Object.class, String.class);
			GET_TYPE_FOR_KEY = KeyValueCoding.class.getMethod("getTypeForKey", String.class);
			ENABLE_ASSERTION_CHECKING = SpecifiableProxyObject.class.getMethod("enableAssertionChecking");
			DISABLE_ASSERTION_CHECKING = SpecifiableProxyObject.class.getMethod("disableAssertionChecking");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static boolean isEqual(Object oldValue, Object newValue) {
		if (oldValue == null) {
			return newValue == null;
		}
		if (oldValue == newValue) {
			return true;
		}
		if (oldValue instanceof AccessibleProxyObject && newValue instanceof AccessibleProxyObject) {
			return ((AccessibleProxyObject) oldValue).equalsObject(newValue);
		}
		if (oldValue instanceof List && newValue instanceof List) {
			List<Object> l1 = (List<Object>) oldValue;
			List<Object> l2 = (List<Object>) newValue;
			if (l1.size() != l2.size()) {
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				Object v1 = l1.get(i);
				Object v2 = l2.get(i);
				if (!isEqual(v1, v2)) {
					return false;
				}
			}
			return true;
		}
		return oldValue.equals(newValue);

	}

}
