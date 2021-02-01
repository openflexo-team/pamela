package {$plainPackageName};

import java.util.*;

import java.beans.PropertyChangeSupport;

/**
  * Automatically generated
  */
public class Main {

	public static int entitiesCreated = 0;

	public static void main(String[] args) {
		{$entities.get(0).name}();
		System.out.println("Created " + entitiesCreated + " entities");
	}

{$internalCode}

}

