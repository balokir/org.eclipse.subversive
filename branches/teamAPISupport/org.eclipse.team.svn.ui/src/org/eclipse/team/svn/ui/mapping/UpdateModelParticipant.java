/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Igor Burilo - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.mapping;

import java.util.Collection;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.MergeContext;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.IChangeSetProvider;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.mapping.UpdateSubscriberContext.ChangeSetSubscriberScopeManager;
import org.eclipse.team.svn.ui.operation.UILoggedOperation;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * @author Igor Burilo
 *
 */
public class UpdateModelParticipant extends AbstractSVNModelParticipant implements IChangeSetProvider {

	private static final String CTX_CONSULT_CHANGE_SETS = "consultChangeSets"; //$NON-NLS-1$
	
	protected ChangeSetCapability capability;
	
	protected boolean isConsultChangeSets;
	
	public UpdateModelParticipant() {
		super();
	}

	public UpdateModelParticipant(SynchronizationContext context) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.svn.ui.synchronize.update.SynchronizeModelParticipant")); //$NON-NLS-1$
		} catch (CoreException e) {			
			UILoggedOperation.reportError(this.getClass().getName(), e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
		this.isConsultChangeSets = isConsultChangeSets(context.getScopeManager());
	}
	
	protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
		return ExtensionsManager.getInstance().getCurrentSynchronizeActionContributor().getUpdateContributions();
	}

    protected int getSupportedModes() {
        return ISynchronizePageConfiguration.ALL_MODES;
    }

    protected int getDefaultMode() {
        return ISynchronizePageConfiguration.BOTH_MODE;
    }

	public ChangeSetCapability getChangeSetCapability() {
		if (this.capability == null) {
			this.capability = new SVNModelParticipantChangeSetCapability();
        }
        return this.capability;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelSynchronizeParticipant#restoreContext(org.eclipse.team.core.mapping.IResourceMappingScope, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected MergeContext restoreContext(ISynchronizationScopeManager manager) {		
		return UpdateSubscriberContext.createContext(manager, ISynchronizationContext.THREE_WAY);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelSynchronizeParticipant#createScopeManager(org.eclipse.core.resources.mapping.ResourceMapping[])
	 */
	protected ISynchronizationScopeManager createScopeManager(ResourceMapping[] mappings) {
		return UpdateSubscriberContext.createWorkspaceScopeManager(mappings, true, this.isConsultChangeSets);
	}
	
	public void saveState(IMemento memento) {
		super.saveState(memento);
	   	memento.putString(CTX_CONSULT_CHANGE_SETS, Boolean.toString(isConsultChangeSets));
	}
	
    public void init(String secondaryId, IMemento memento) throws PartInitException {
    	try {
    		String consult = memento.getString(CTX_CONSULT_CHANGE_SETS);
    		if (consult != null)
    			isConsultChangeSets = Boolean.valueOf(consult).booleanValue();
    	} finally {
    		super.init(secondaryId, memento);
    	}
    }
	    
	protected boolean isConsultChangeSets(ISynchronizationScopeManager manager) {
		if (manager instanceof ChangeSetSubscriberScopeManager) {
			ChangeSetSubscriberScopeManager man = (ChangeSetSubscriberScopeManager) manager;
			return man.isConsultSets();
		}
		return false;
	}
	
//	 TODO try it to add actions	
//	/* (non-Javadoc)
//	 * @see org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant#createMergeActionGroup()
//	 */
//	protected ModelSynchronizeParticipantActionGroup createMergeActionGroup() {
//		return new WorkspaceMergeActionGroup();
//	}

}
