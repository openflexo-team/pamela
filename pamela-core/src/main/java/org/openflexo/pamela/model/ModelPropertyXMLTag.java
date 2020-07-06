package org.openflexo.pamela.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openflexo.toolbox.StringUtils;

/**
 * Used in the context of XML serialization/deserialization
 * 
 * @author sylvain
 *
 * @param <I>
 */
public class ModelPropertyXMLTag<I> {
	private final String tag;
	private final ModelProperty<? super I> property;
	private final ModelEntity<?> accessedEntity;

	public ModelPropertyXMLTag(ModelProperty<? super I> property) {
		super();
		this.property = property;
		this.accessedEntity = null;
		this.tag = property.getXMLContext() + property.getXMLElement().xmlTag();
	}

	public ModelPropertyXMLTag(ModelProperty<? super I> property, ModelEntity<?> accessedEntity) {
		super();
		this.property = property;
		this.accessedEntity = accessedEntity;
		this.tag = property.getXMLContext() + accessedEntity.getXMLTag();
	}

	public String getTag() {
		return tag;
	}

	public List<String> getDeprecatedTags() {
		List<String> returned = new ArrayList<>();
		if (accessedEntity.getXMLElement() != null && StringUtils.isNotEmpty(accessedEntity.getXMLElement().deprecatedXMLTags())) {
			StringTokenizer st = new StringTokenizer(accessedEntity.getXMLElement().deprecatedXMLTags(), ",");
			while (st.hasMoreTokens()) {
				String nextTag = st.nextToken();
				returned.add(property.getXMLContext() + nextTag);
			}
		}
		if (getProperty().getXMLElement() != null && StringUtils.isNotEmpty(getProperty().getXMLElement().deprecatedContext())) {
			returned.add(getProperty().getXMLElement().deprecatedContext() + accessedEntity.getXMLTag());
		}
		if (returned.size() == 0) {
			return null;
		}
		return returned;
	}

	public ModelProperty<? super I> getProperty() {
		return property;
	}

	public ModelEntity<?> getAccessedEntity() {
		return accessedEntity;
	}

	@Override
	public String toString() {
		return "ModelPropertyXMLTag" + getAccessedEntity() + getProperty() + "/tag=" + getTag();
	}
}
