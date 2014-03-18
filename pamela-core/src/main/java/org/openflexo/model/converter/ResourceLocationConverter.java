package org.openflexo.model.converter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;

public class ResourceLocationConverter extends Converter<Resource> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger.getLogger(ResourceLocationConverter.class
			.getPackage().getName());
	private static final ResourceLocator rl = ResourceLocator.getResourceLocator();


	public ResourceLocationConverter() {
		super(Resource.class);
	}

	@Override
	public Resource convertFromString(String value, ModelFactory factory) {
		Resource resourceloc = ResourceLocator.locateResource(value);
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
		return value.getRelativePath();
	}
}