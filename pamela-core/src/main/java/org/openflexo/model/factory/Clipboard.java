package org.openflexo.model.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;

public class Clipboard {

	private final ModelFactory modelFactory;
	private final Object[] originalContents;
	private Object contents;
	private final boolean isSingleObject;

	private Object copyContext;

	protected Clipboard(ModelFactory modelFactory, Object... objects) throws ModelExecutionException, ModelDefinitionException,
			CloneNotSupportedException {
		this.modelFactory = modelFactory;

		this.originalContents = objects;

		if (objects == null || objects.length == 0) {
			new ClipboardOperationException("Cannot build an empty Clipboard");
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

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public Object[] getOriginalContents() {
		return originalContents;
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
		StringBuffer returned = new StringBuffer();
		returned.append("*************** Clipboard ****************\n");
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
	 * Called when clipboard has been used somewhere. Copy again contents for a future use
	 * 
	 * @throws ModelExecutionException
	 * @throws ModelDefinitionException
	 * @throws CloneNotSupportedException
	 */
	public void consume() throws ModelExecutionException, ModelDefinitionException, CloneNotSupportedException {
		if (isSingleObject) {
			contents = modelFactory.getHandler(contents).cloneObject(contents);
		} else {
			contents = modelFactory.getHandler(((List) contents).get(0)).cloneObjects(((List) contents).toArray());
		}
	}

	public Object getCopyContext() {
		return copyContext;
	}

	public void setCopyContext(Object copyContext) {
		this.copyContext = copyContext;
	}

	private final Map<String, String> pasteProperties = new HashMap<String, String>();

	public String getPasteProperty(String key) {
		return pasteProperties.get(key);
	}

	public void setPasteProperty(String key, String value) {
		pasteProperties.put(key, value);
	}

}
