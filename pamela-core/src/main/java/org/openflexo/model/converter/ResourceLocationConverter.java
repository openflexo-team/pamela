package org.openflexo.model.converter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.ResourceLocation;
import org.openflexo.toolbox.ResourceLocator;

public class ResourceLocationConverter extends Converter<ResourceLocation> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger.getLogger(ResourceLocationConverter.class
			.getPackage().getName());
	private static final ResourceLocator rl = ResourceLocator.getResourceLocator();


	public ResourceLocationConverter() {
		super(ResourceLocation.class);
	}

	@Override
	public ResourceLocation convertFromString(String value, ModelFactory factory) {
		ResourceLocation resourceloc = rl.locateResource(value);
		if (resourceloc == null) {
			logger.warning("Cannot find Resource: " + value );
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("********* convertFromString " + value + " return " + resourceloc.toString());
		}
		return resourceloc;
	}

	@Override
	public String convertToString(ResourceLocation value) {
		return value.getInitialPath();
	}
}