package org.openflexo.model.share;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Network service that manage Pamela model sharing.
 */
public class SharingService {

    private final Set<SharedEditingContext> registeredContext = new LinkedHashSet<>();

    public SharingService() {
    }

    public boolean register(SharedEditingContext sharedEditingContext) {
        return registeredContext.add(sharedEditingContext);
    }

    public boolean unregister(SharedEditingContext sharedEditingContext) {
        return registeredContext.remove(sharedEditingContext);
    }

}
