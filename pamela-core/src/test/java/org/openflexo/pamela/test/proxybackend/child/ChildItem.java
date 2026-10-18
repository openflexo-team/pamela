package org.openflexo.pamela.test.proxybackend.child;

/**
 * Loaded by a child class loader in the tests (as an entity of a technology adapter jar)
 */
public interface ChildItem {

	public String getName();

	public void setName(String name);

	public String base();
}
