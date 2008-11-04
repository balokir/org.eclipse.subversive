/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.synchronize.merge;

import java.util.Collection;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSubscriber;
import org.eclipse.team.svn.core.synchronize.AbstractSVNSyncInfo;
import org.eclipse.team.svn.core.synchronize.MergeSubscriber;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.operation.MergeScope;
import org.eclipse.team.svn.ui.synchronize.AbstractSVNParticipant;
import org.eclipse.team.svn.ui.synchronize.AbstractSynchronizeActionGroup;
import org.eclipse.team.svn.ui.synchronize.SynchronizeLabelDecorator;
import org.eclipse.team.svn.ui.utility.OverlayedImageDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;

/**
 * Merge view participant
 * 
 * @author Alexander Gurov
 */
public class MergeParticipant extends AbstractSVNParticipant {
	public static final String PARTICIPANT_ID = MergeParticipant.class.getName();
	public static final int SUPPORTED_MODES = ISynchronizePageConfiguration.ALL_MODES;

	protected IPropertyChangeListener configurationListener;
	
    public MergeParticipant() {
        super();
    }

    public MergeParticipant(ISynchronizeScope scope) {
        super(scope);
    }
    
    public AbstractSVNSubscriber getMatchingSubscriber() {
        MergeSubscriber subscriber = MergeSubscriber.instance();
        MergeScope scope = (MergeScope) this.getScope();        
        
        subscriber.setMergeScopeHelper(scope.getMergeScopeHelper());
        return subscriber;
    }

    protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
    	super.initializeConfiguration(configuration);
    }
    
    public void dispose() {
    	super.dispose();
    }
    
    protected String getParticipantId() {
        return MergeParticipant.PARTICIPANT_ID;
    }

	protected Collection<AbstractSynchronizeActionGroup> getActionGroups() {
		return ExtensionsManager.getInstance().getCurrentSynchronizeActionContributor().getMergeContributions();
	}

    protected int getSupportedModes() {
        return MergeParticipant.SUPPORTED_MODES;
    }

    protected int getDefaultMode() {
        return ISynchronizePageConfiguration.BOTH_MODE;
    }

    protected String getShortTaskName() {
        return SVNTeamUIPlugin.instance().getResource("MergeView.TaskName");
    }
    
    protected ILabelDecorator createLabelDecorator() {
    	return new MergeLabelDecorator();
    }
    
	protected class MergeLabelDecorator extends SynchronizeLabelDecorator {
	    public MergeLabelDecorator() {
	        super();
	    }
	    
		public Image decorateImage(Image image, Object element) {
		    AbstractSVNSyncInfo info = this.getSyncInfo(element);
			if (info != null && (info.getKind() & SynchronizeLabelDecorator.CONFLICTING_REPLACEMENT_MASK) == SynchronizeLabelDecorator.CONFLICTING_REPLACEMENT_MASK) {
				ILocalResource local = info.getLocalResource();
		        if (IStateFilter.SF_PREREPLACEDREPLACED.accept(local)) {
				    return this.registerImageDescriptor(new OverlayedImageDescriptor(image, AbstractSVNParticipant.OVR_REPLACED_CONF, new Point(22, 16), OverlayedImageDescriptor.RIGHT | OverlayedImageDescriptor.CENTER_V));
		        }
			}
			return super.decorateImage(image, element);
		}
		
	}

}
