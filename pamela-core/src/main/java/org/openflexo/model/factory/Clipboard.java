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

package org.openflexo.model.factory;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;

/**
 * Represent the clipbord of a PAMELA model<br>
 * 
 * This is the serialization of objects beeing pasted or cut.<br>
 * <code>originalContents</code> are the copied contents, in their original context<br>
 * <code>lastReferenceContents</code> are the last reference contents, in multiple paste context (if you copy, then paste and paste again)<br>
 * Note that for the first copy operation, originalContents are the referenceContents
 */
public class Clipboard {

	private final ModelFactory modelFactory;
	private final Object[] originalContents;
	private Object[] lastReferenceContents;
	private Object contents;
	private final boolean isSingleObject;

	private Object copyContext;

	protected Clipboard(ModelFactory modelFactory, Object... objects) throws ModelExecutionException, ModelDefinitionException,
			CloneNotSupportedException {
		this.modelFactory = modelFactory;

		this.originalContents = objects;
		this.lastReferenceContents = objects;

		if (objects == null || objects.length == 0) {
			throw new ClipboardOperationException("Cannot build an empty Clipboard");
		}
		isSingleObject = objects.length == 1;
		// TODO: This should rather be done when pasting instead of cloning immediately
		if (isSingleObject) {
			Object object = objects[0];

			if (modelFactory.getHandler(object) == null) {
				throw new ModelExecutionException("Object has no handler in supplied ModelFactory, object=" + object + " modelFactory="
						+ modelFactory);
			}

			contents = modelFactory.getHandler(object).cloneObject(objects);
		} else {
			contents = modelFactory.getHandler(objects[0]).cloneObjects(objects);
		}
	}

	/**
	 * Return the copied object corresponding to the last reference object
	 * 
	 * @param lastReferenceObject
	 * @return
	 */
	public Object getCopiedObject(Object lastReferenceObject) {
		if (isSingleObject()) {
			if (lastReferenceObject == lastReferenceContents[0]) {
				return getSingleContents();
			} else {
				return null;
			}
		}
		for (int index = 0; index < lastReferenceContents.length; index++) {
			if (lastReferenceContents[index] == lastReferenceObject) {
				return getMultipleContents().get(index);
			}
		}
		return null;
	}

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public Object[] getOriginalContents() {
		return originalContents;
	}

	public Object[] getLastReferenceContents() {
		return lastReferenceContents;
	}

	public boolean doesOriginalContentsContains(Object o) {
		for (Object oc : originalContents) {
			if (o == oc) {
				return true;
			}
		}
		return false;
	}

	public Object getSingleContents() {
		return contents;
	}

	public List<Object> getMultipleContents() {
		return (List<Object>) contents;
	}

	public boolean isSingleObject() {
		return isSingleObject;
	}

	/**
	 * Return an array storing all types involved as root elements in current Clipboard
	 * 
	 * @return
	 */
	public Class<?>[] getTypes() {
		Class<?>[] returned;
		if (isSingleObject()) {
			returned = new Class[1];
			returned[0] = getSingleContents().getClass();
		} else {
			List<Class<?>> allTypes = new ArrayList<Class<?>>();
			for (Object o : getMultipleContents()) {
				Class<?> type = o.getClass();
				if (!allTypes.contains(type)) {
					allTypes.add(type);
				}
			}
			returned = new Class[allTypes.size()];
			allTypes.toArray(returned);
		}
		return returned;
	}

	public String debug() {
		return debug("Clipboard");
	}

	public String debug(String clipboardName) {
		StringBuffer returned = new StringBuffer();
		returned.append("*************** " + clipboardName + " ****************\n");
		returned.append("Single object: " + isSingleObject() + "\n");
		returned.append("Original contents:\n");
		for (Object o : originalContents) {
			returned.append(" > " + o + "\n");
		}
		if (isSingleObject()) {
			returned.append("------------------- " + contents + " -------------------\n");
			List<Object> embeddedList = modelFactory.getEmbeddedObjects(contents, EmbeddingType.CLOSURE);
			for (Object e : embeddedList) {
				returned.append(Integer.toHexString(e.hashCode()) + " Embedded: " + e + "\n");
			}
		} else {
			List contentsList = (List) contents;
			for (Object object : contentsList) {
				returned.append("------------------- " + object + " -------------------\n");
				List<Object> embeddedList = modelFactory.getEmbeddedObjects(object, EmbeddingType.CLOSURE, contentsList.toArray());
				for (Object e : embeddedList) {
					returned.append(Integer.toHexString(e.hashCode()) + " Embedded: " + e + "\n");
				}
			}
		}
		return returned.toString();
	}

	/**
	 * Called when clipboard has been used somewhere. Copy again contents for a future use<br>
	 * lastReferenceContents are set to the actual value of contents (which has just been used)
	 * 
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public void consume() throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (isSingleObject) {
			lastReferenceContents = new Object[1];
			lastReferenceContents[0] = contents;
			contents = modelFactory.getHandler(contents).cloneObject(contents);
		} else {
			lastReferenceContents = new Object[((List) contents).size()];
			for (int i = 0; i < ((List) contents).size(); i++) {
				lastReferenceContents[i] = ((List) contents).get(i);
			}
			contents = modelFactory.getHandler(((List) contents).get(0)).cloneObjects(((List) contents).toArray());
		}
	}

	public Object getCopyContext() {
		return copyContext;
	}

	public void setCopyContext(Object copyContext) {
		this.copyContext = copyContext;
	}

}
