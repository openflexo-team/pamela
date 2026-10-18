package org.openflexo.pamela.test.proxybackend.model;

import java.io.IOException;

import org.openflexo.pamela.annotations.Initializer;

public interface Item {

	@Initializer
	public Item init(String name);

	@Initializer
	public Item initRedeclared(String name);

	public String getName();

	public void setName(String name);

	public String label();

	public String describe();

	public void fail() throws IOException;
}
