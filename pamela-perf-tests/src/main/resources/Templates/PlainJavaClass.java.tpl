package {$plainPackageName};

import java.util.*;

import java.beans.PropertyChangeSupport;

/**
  * Automatically generated
  */
public class {$name} {

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

{$propertiesInternalCode}

	public {$name}() {
		Main.entitiesCreated++;
{$constructorInternalCode}
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

{$propertiesCode}

}

