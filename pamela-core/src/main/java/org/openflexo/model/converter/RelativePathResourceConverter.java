package org.openflexo.model.converter;

import java.util.logging.Level;

import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

public class RelativePathResourceConverter extends Converter<Resource> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger.getLogger(RelativePathResourceConverter.class
			.getPackage().getName());

	private final String relativePath;

	public RelativePathResourceConverter(String aRelativePath) {
		super(Resource.class);
		relativePath = aRelativePath;
	}

	@Override
	public Resource convertFromString(String value, ModelFactory factory) {
		Resource resourceloc = ResourceLocator.locateResource(relativePath+"/"+value);
		if (resourceloc == null) {
			logger.warning("Cannot find Resource: " + value );
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("********* convertFromString " + value + " return " + resourceloc.toString());
		}
		return resourceloc;
	}
	
	@Override
	public String convertToString(Resource value) {
		return value.makePathRelativeToString(relativePath);
	}
}