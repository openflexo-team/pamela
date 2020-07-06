/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

package org.openflexo.pamela.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openflexo.pamela.model.ModelProperty;

/**
 * Indicates that related {@link ModelEntity} or {@link ModelProperty} should be serialized as a XML element in the context of XML
 * serialization<br>
 * 
 * This annotation should be placed either on a {@link ModelEntity} or on the method where getter is declared
 * 
 * @author sylvain
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface XMLElement {

	String DEFAULT_XML_TAG = "";
	String NO_CONTEXT = "";
	String NO_NAME_SPACE = "";

	/**
	 * Name to be used in XMLElement serialized data
	 * 
	 * @return
	 */
	String xmlTag() default DEFAULT_XML_TAG;

	/**
	 * Comma separated deprecated XML tags (used to deserialize only, with the aim of migrating data from old format)
	 * 
	 * @return
	 */
	String deprecatedXMLTags() default DEFAULT_XML_TAG;

	/**
	 * String context to be appended at the beginning of XML tag of element to deserialize for this property
	 * 
	 * @return
	 */
	String context() default NO_CONTEXT;

	/**
	 * String context to be appended at the beginning of XML tag of element to deserialize for this property (used to deserialize only, with
	 * the aim of migrating data from old format)
	 * 
	 * @return
	 */
	String deprecatedContext() default NO_CONTEXT;

	String namespace() default NO_NAME_SPACE;

	boolean primary() default false;

	String NO_ID_FACTORY = "";

	String idFactory() default NO_ID_FACTORY;

	class XMLElementImpl implements XMLElement {
		private String xmlTag;
		private String deprecatedXMLTags;
		private String context;
		private String deprecatedContext;
		private String namespace;
		private boolean primary;
		private String idFactory;

		public XMLElementImpl(String xmlTag, String context, String namespace, boolean primary, String idFactory) {
			this.xmlTag = xmlTag;
			this.context = context;
			this.namespace = namespace;
			this.primary = primary;
			this.idFactory = idFactory;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return XMLElement.class;
		}

		@Override
		public String xmlTag() {
			return xmlTag;
		}

		@Override
		public String deprecatedXMLTags() {
			return deprecatedXMLTags;
		}

		@Override
		public String context() {
			return context;
		}

		@Override
		public String deprecatedContext() {
			return deprecatedContext;
		}

		@Override
		public String namespace() {
			return namespace;
		}

		@Override
		public boolean primary() {
			return primary;
		}

		@Override
		public String idFactory() {
			return idFactory;
		}

	}
}
