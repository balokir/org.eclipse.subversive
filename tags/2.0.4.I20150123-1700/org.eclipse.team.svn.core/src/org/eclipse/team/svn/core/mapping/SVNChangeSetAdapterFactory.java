package org.eclipse.team.svn.core.mapping;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdapterFactory;

public class SVNChangeSetAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof SVNActiveChangeSet && adapterType == ResourceMapping.class) {
			SVNActiveChangeSet cs = (SVNActiveChangeSet) adaptableObject;
			return new SVNChangeSetResourceMapping(cs);
		}
		if (adaptableObject instanceof SVNIncomingChangeSet && adapterType == ResourceMapping.class) {
			SVNIncomingChangeSet cs = (SVNIncomingChangeSet) adaptableObject;
			return new SVNChangeSetResourceMapping(cs);
		}
		if (adaptableObject instanceof SVNUnassignedChangeSet && adapterType == ResourceMapping.class) {
			SVNUnassignedChangeSet cs = (SVNUnassignedChangeSet) adaptableObject;
			return new SVNChangeSetResourceMapping(cs);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { ResourceMapping.class };
	}

}
