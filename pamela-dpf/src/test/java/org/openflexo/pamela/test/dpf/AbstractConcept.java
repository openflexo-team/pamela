/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.pamela.test.dpf;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.monitoring.Monitored;
import org.openflexo.pamela.annotations.monitoring.MonitoredEntity;
import org.openflexo.pamela.annotations.monitoring.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.test.dpf.AbstractConcept.AbstractConceptImpl;

@ModelEntity
@MonitoredEntity(MonitoringStrategy.CheckMonitoredMethodsOnly)
@ImplementationClass(AbstractConceptImpl.class)
public interface AbstractConcept extends AccessibleProxyObject {

	static final String NAME = "name";

	@Initializer
	void create(@Parameter(NAME) String aName);

	@Getter(NAME)
	String getName();

	@Setter(NAME)
	public void setName(String aName);

	@Monitored
	public void aMonitoredMethod();

	public static abstract class AbstractConceptImpl implements AbstractConcept {

		protected Class getImplementedInterface() {
			return (Class) getClass().getGenericInterfaces()[0];
		}

		@Override
		public String toString() {
			return getImplementedInterface().getSimpleName() + "[" + getName() + "]";
		}

		@Override
		public void aMonitoredMethod() {
			System.out.println("Hello world !");
		}

	}

}
