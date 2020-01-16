/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.pamela.validation;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Represents a validation issue containing many other validation issues. This is an artefact to express that some validation rules may
 * throw more than one error or warning
 * 
 * @author sguerin
 * 
 */
public class CompoundIssue<R extends ValidationRule<R, V>, V extends Validable> extends ValidationIssue<R, V> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CompoundIssue.class.getPackage().getName());

	private Vector<ValidationIssue<R, V>> _containedIssues;

	public CompoundIssue(V anObject) {
		super(anObject, null);
		_containedIssues = new Vector<>();
	}

	public CompoundIssue(V anObject, List<ValidationIssue<R, V>> issues) {
		this(anObject);
		for (ValidationIssue<R, V> issue : issues) {
			addToContainedIssues(issue);
		}
	}

	public List<ValidationIssue<R, V>> getContainedIssues() {
		return _containedIssues;
	}

	/*public void setContainedIssues(Vector<ValidationIssue<R, V>> containedIssues) {
		_containedIssues = containedIssues;
	}*/

	public void addToContainedIssues(ValidationIssue<R, V> issue) {
		_containedIssues.add(issue);
	}

	public void removeFromContainedIssues(ValidationIssue<R, V> issue) {
		_containedIssues.remove(issue);
	}

	@Override
	public void setCause(R rule) {
		super.setCause(rule);
		for (ValidationIssue<R, V> issue : _containedIssues) {
			if (issue != this) {
				issue.setCause(rule);
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ValidationIssue<?, ?> issue : getContainedIssues()) {
			sb.append(issue.toString() + "\n");
		}
		return sb.toString();
	}

}
