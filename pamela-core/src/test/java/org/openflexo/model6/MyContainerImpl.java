package org.openflexo.model6;

import org.openflexo.model.factory.ModelFactory;

public abstract class MyContainerImpl implements MyContainer {

	private MyContents lecontenu = null;
	private ModelFactory factory = null;
	private String contentURI = null;

	public void setFactory(ModelFactory fact) {
		factory = fact;
	}

	@Override
	public String getContentURI() {
		if (lecontenu != null) {
			contentURI = new String("Content://" + lecontenu.toString());
			// If you uncomment this => infinite loop
			// setContentURI("Content://" + lecontenu.toString());
		}
		return contentURI;
	}

	@Override
	public void setContentURI(String anURI) {
		System.out.println("JE positionne l'URI " + anURI);
		contentURI = anURI;
	}

	@Override
	public String getContents() {
		if (lecontenu != null) {
			System.out.println("Getting something + " + lecontenu.getValue());
			return lecontenu.getValue();
		}
		else if (contentURI != null) {

			String anURi = getContentURI();
			System.out.println("Getting from URI: " + anURi);
			// If you uncomment this => infinite loop!
			// setContents(anURi.substring(10));
			lecontenu = MyContentsImpl.fromString(factory, anURi.substring(10));
			return lecontenu.getValue();
		}
		else {
			System.out.println("Getting Nothing ");
			return null;
		}
	}

	@Override
	public void setContents(String someContents) {
		if (getContents() == null) {
			System.out.println("Setting something (creation): " + someContents);
			lecontenu = MyContentsImpl.fromString(factory, someContents);
		}
		else {
			System.out.println("Setting something : " + someContents);
			lecontenu.setValue(someContents);
		}
		setContentURI("Content://" + lecontenu.getValue());
	}
}
