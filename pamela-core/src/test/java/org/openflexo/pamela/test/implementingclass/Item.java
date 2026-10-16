package org.openflexo.pamela.test.implementingclass;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;

/**
 * An entity declared without implementation class: its implementation is set at run time, from another package
 */
@ModelEntity
public interface Item extends AccessibleProxyObject {

	public static final String NAME = "name";

	@Getter(value = NAME)
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	public String describe();
}
