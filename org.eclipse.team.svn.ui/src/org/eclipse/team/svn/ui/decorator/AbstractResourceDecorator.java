/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Dann Martens - [patch] Text decorations 'ascendant' variable
 *    Thomas Champagne - Bug 217561 : additional date formats for label decorations
 *******************************************************************************/

package org.eclipse.team.svn.ui.decorator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.SVNTeamPlugin;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.resource.IRepositoryRoot;
import org.eclipse.team.svn.core.resource.events.IResourceStatesListener;
import org.eclipse.team.svn.core.resource.events.ResourceStatesChangedEvent;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.decorator.wrapper.ResourceDecoratorWrapper;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.preferences.SVNTeamPreferences;
import org.eclipse.team.svn.ui.utility.DateFormatter;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

/**
 * Abstract workspace resources decorator
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractResourceDecorator extends LabelProvider implements ILightweightLabelDecorator {
	protected static final ImageDescriptor OVR_VERSIONED = TeamImages.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR);

	protected static final ImageDescriptor OVR_ADDED = TeamImages.getImageDescriptor(ISharedImages.IMG_HOURGLASS_OVR);

	protected static ImageDescriptor OVR_NEW = TeamImages.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR);

	protected static ImageDescriptor OVR_MODIFIED = TeamImages.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR);
	
	protected static ImageDescriptor OVR_CONFLICTED;
	
	protected static ImageDescriptor OVR_OBSTRUCTED;
	
	protected static ImageDescriptor OVR_DELETED;
	
	protected static ImageDescriptor OVR_LOCKED;
	
	protected static ImageDescriptor OVR_NEEDS_LOCK;
	
	protected static ImageDescriptor OVR_SWITCHED;
		
	protected static IResourceStatesListener installedListener = null;
	protected static int instanceCounter = 0;
	
	protected IPropertyChangeListener configurationListener;

	protected boolean indicateConflicted;
	protected boolean indicateModified;
	protected boolean indicateDeleted;
	protected boolean indicateRemote;
	protected boolean indicateAdded;
	protected boolean indicateNew;
	protected boolean indicateLocked;
	protected boolean indicateNeedsLock;
	protected boolean indicateSwitched;

	protected String outgoingChars;
	protected String addedChars;
	protected String trunkPrefix;
	protected String branchPrefix;
	protected String tagPrefix;
	protected boolean useFonts;
	protected IDecorationFilter filter;
	
	protected Font ignoredFont;
	protected Font changedFont;
	protected Color ignoredForegroundColor;
	protected Color ignoredBackgroundColor;
	protected Color changedForegroundColor;
	protected Color changedBackgroundColor;
	
	protected IVariable []format;
	protected DecoratorVariables decorator;
	
	public AbstractResourceDecorator(IResourceStatesListener targetListener) {
		super();
		
		this.initStatic(this, targetListener);
		
		this.configurationListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().startsWith(SVNTeamPreferences.DECORATION_BASE) || 
					event.getProperty().startsWith(SVNTeamPreferences.DATE_FORMAT_BASE)) {
					AbstractResourceDecorator.this.loadConfiguration();
					String decoratorId = ResourceDecoratorWrapper.class.getName();
					SVNTeamUIPlugin.instance().getWorkbench().getDecoratorManager().update(decoratorId);
				}
			}
		};
		
		this.filter = ExtensionsManager.getInstance().getCurrentDecorationFilter();
		
		SVNTeamUIPlugin.instance().getPreferenceStore().addPropertyChangeListener(this.configurationListener);
		PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().addPropertyChangeListener(this.configurationListener);
		
		this.decorator = new DecoratorVariables(TextVariableSetProvider.instance);
	}
	
	public void dispose() {
		SVNTeamUIPlugin.instance().getPreferenceStore().removePropertyChangeListener(this.configurationListener);
		
		this.finiStatic();
		
		super.dispose();
	}
	
	public final void decorate(Object element, IDecoration decoration) {
		try {
			IResource resource = this.getResource(element);
			if (resource == null ||
				RepositoryProvider.getProvider(resource.getProject(), SVNTeamPlugin.NATURE_ID) == null ||
				!this.filter.isAcceptable(resource)) {
				return;
			}
			ILocalResource local = SVNRemoteStorage.instance().asLocalResource(resource);
			IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(resource);
			if (IStateFilter.SF_INTERNAL_INVALID.accept(local) || remote == null) {
			    return;
			}

			String localStatus = this.getStatus(local);
			int mask = local.getChangeMask();
			
			if (!IStateFilter.SF_NOTEXISTS.accept(resource, localStatus, mask)) {
				if (this.format == null) {
					this.loadConfiguration();
					localStatus = this.getStatus(local);
				}
				
				this.decorateImpl(remote, local, resource, localStatus, mask, decoration);
			}
		}
		catch (Throwable ex) {
			//LoggedOperation.reportError("SVN Decorator", ex);
		}
	}
	
	protected void decorateImpl(final IRepositoryResource remote, final ILocalResource local, final IResource resource, final String state, final int mask, IDecoration decoration) {
		if (local.isLocked() && this.indicateLocked) {
			decoration.addOverlay(AbstractResourceDecorator.OVR_LOCKED);
		} 
		else if (IStateFilter.SF_IGNORED.accept(resource, state, mask)) {
			if (this.useFonts) {
				decoration.setBackgroundColor(this.ignoredBackgroundColor);
				decoration.setForegroundColor(this.ignoredForegroundColor);
				decoration.setFont(this.ignoredFont);
			}
		}
		else if (IStateFilter.SF_NEW.accept(resource, state, mask)) {
			if (this.indicateNew) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_NEW);
			}
		}
		else if (this.indicateNeedsLock && IStateFilter.SF_NEEDS_LOCK.accept(resource, state, mask)) {
			decoration.addOverlay(AbstractResourceDecorator.OVR_NEEDS_LOCK);
		}
		else if (IStateFilter.SF_ADDED.accept(resource, state, mask)) {
//			new state also recognized as added, then it should be before added
			if (this.indicateAdded) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_ADDED);
			}
		}
		else if (IStateFilter.SF_DELETED.accept(resource, state, mask)) {
			decoration.addOverlay(AbstractResourceDecorator.OVR_DELETED);
		}
		else if (IStateFilter.SF_CONFLICTING.accept(resource, state, mask)) {
			if (this.indicateConflicted) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_CONFLICTED);
			}
			else if (this.indicateModified) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_MODIFIED);
			}
			else if (this.indicateSwitched && (local.getChangeMask() & ILocalResource.IS_SWITCHED) != 0) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_SWITCHED);
			}
			else if (this.indicateRemote) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_VERSIONED);
			}
		}
		else if (IStateFilter.SF_MODIFIED.accept(resource, state, mask)) {
			if (this.indicateModified) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_MODIFIED);
			}
			else if (this.indicateSwitched && (local.getChangeMask() & ILocalResource.IS_SWITCHED) != 0) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_SWITCHED);
			}
			else if (this.indicateRemote) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_VERSIONED);
			}
		}
		else if (IStateFilter.SF_OBSTRUCTED.accept(resource, state, mask)) {
			decoration.addOverlay(AbstractResourceDecorator.OVR_OBSTRUCTED);
		}
		else if (IStateFilter.SF_VERSIONED.accept(resource, state, mask)) {
			if (this.indicateSwitched && (local.getChangeMask() & ILocalResource.IS_SWITCHED) != 0) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_SWITCHED);
			}
			else if (this.indicateRemote) {
				decoration.addOverlay(AbstractResourceDecorator.OVR_VERSIONED);
			}
		}
		if (this.useFonts && IStateFilter.SF_MODIFIED.accept(resource, state, mask)) {
			decoration.setBackgroundColor(this.changedBackgroundColor);
			decoration.setForegroundColor(this.changedForegroundColor);
			decoration.setFont(this.changedFont);
		}
		
		this.decorator.decorateText(
			decoration, 
			this.format, 
			new IVariableContentProvider() {
				public String getValue(IVariable var) {
					if (var.equals(TextVariableSetProvider.VAR_ADDED_FLAG)) {
						return IStateFilter.SF_ADDED.accept(resource, state, mask) ? AbstractResourceDecorator.this.addedChars : "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_OUTGOING_FLAG)) {
						return (IStateFilter.SF_COMMITABLE.accept(resource, state, mask) || IStateFilter.SF_CONFLICTING.accept(resource, state, mask)) ? AbstractResourceDecorator.this.outgoingChars : "";
					}
					
					if (var.equals(TextVariableSetProvider.VAR_REVISION)) {
						return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) && !IStateFilter.SF_PREREPLACEDREPLACED.accept(resource, state, mask) ? String.valueOf(local.getRevision()) : "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_AUTHOR)) {
					    String author = local.getAuthor() == null ? "[no author]" : local.getAuthor(); 
						return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) ? author : "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_DATE)) {
						if (!IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							return "";
						}
						long date = local.getLastCommitDate();
						if (date == 0) {
							return SVNTeamPlugin.instance().getResource("SVNInfo.NoDate");
						}
						return DateFormatter.formatDate(date);
					}
					else if (var.equals(TextVariableSetProvider.VAR_RESOURCE_URL)) {
						return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) ? SVNUtility.decodeURL(remote.getUrl()) : "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_SHORT_RESOURCE_URL)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							String shortURL = SVNUtility.decodeURL(remote.getUrl()).substring(remote.getRepositoryLocation().getRepositoryRootUrl().length());
							return shortURL.startsWith("/") ? shortURL.substring(1) : shortURL;
						}
						return "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_LOCATION_URL)) {
						return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) ? remote.getRepositoryLocation().getUrlAsIs() : "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_LOCATION_LABEL)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							IRepositoryLocation location = remote.getRepositoryLocation();
							String label = location.getLabel();
							return label == null || label.length() == 0 ? location.getUrlAsIs() : label;
						}
						return "";
					}
					else if (var.equals(TextVariableSetProvider.VAR_ROOT_PREFIX)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							int kind = ((IRepositoryRoot)remote.getRoot()).getKind();
							return kind == IRepositoryRoot.KIND_TAGS ? AbstractResourceDecorator.this.tagPrefix : (
										kind == IRepositoryRoot.KIND_BRANCHES ? AbstractResourceDecorator.this.branchPrefix : (
											kind == IRepositoryRoot.KIND_TRUNK ? AbstractResourceDecorator.this.trunkPrefix : ""
										)
									);
						}
						return "";
					} 
					else if (var.equals(TextVariableSetProvider.VAR_ASCENDANT)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							return SVNUtility.getAscendant(remote);
						}
						return "";												
					}
					else if (var.equals(TextVariableSetProvider.VAR_DESCENDANT)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							return SVNUtility.getDescendant(remote);
						}
						return "";												
					}
					else if (var.equals(TextVariableSetProvider.VAR_FULLNAME)) {
						if (IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask)) {
							return SVNUtility.getPathUpToRoot(remote);
						}
						return "";												
					}
					else if (var.equals(TextVariableSetProvider.VAR_REMOTE_NAME)) {
						return IStateFilter.SF_ONREPOSITORY.accept(resource, state, mask) ? remote.getName() : "";
					}
					if (var.equals(TextVariableSetProvider.VAR_NAME)) {
						return local.getName();
					}
					return var.toString();
				}
			}
		);
	}
	
	protected abstract String getStatus(ILocalResource local);

	protected IResource getResource(Object object) {
		if (object instanceof IResource) {
			return (IResource) object;
		}
		if (object instanceof IAdaptable) {
			return (IResource) ((IAdaptable) object).getAdapter(
				IResource.class);
		}
		return null;
	}

	protected void loadConfiguration() {
		IPreferenceStore store = SVNTeamUIPlugin.instance().getPreferenceStore();
		
		this.indicateConflicted = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_CONFLICTED_NAME);
		this.indicateModified = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_MODIFIED_NAME);
		this.indicateRemote = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_REMOTE_NAME);
		this.indicateAdded = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_ADDED_NAME);
		this.indicateNew = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_NEW_NAME);
		this.indicateLocked = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_LOCKED_NAME);
		this.indicateNeedsLock = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_NEEDS_LOCK_NAME);
		this.indicateSwitched = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_ICON_SWITCHED_NAME);
		
		this.outgoingChars = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FLAG_OUTGOING_NAME);
		this.addedChars = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_FLAG_ADDED_NAME);
		
		this.trunkPrefix = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_TRUNK_PREFIX_NAME);
		this.branchPrefix = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_BRANCH_PREFIX_NAME);
		this.tagPrefix = SVNTeamPreferences.getDecorationString(store, SVNTeamPreferences.DECORATION_TAG_PREFIX_NAME);
		this.useFonts = SVNTeamPreferences.getDecorationBoolean(store, SVNTeamPreferences.DECORATION_USE_FONT_COLORS_DECOR_NAME);
		UIMonitorUtility.getDisplay().syncExec(new Runnable() {
			public void run() {
				ITheme current = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
				AbstractResourceDecorator.this.ignoredFont = current.getFontRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_IGNORED_FONT));
				AbstractResourceDecorator.this.changedFont = current.getFontRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_FONT));
				AbstractResourceDecorator.this.ignoredForegroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_IGNORED_FOREGROUND_COLOR));
				AbstractResourceDecorator.this.ignoredBackgroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_IGNORED_BACKGROUND_COLOR));
				AbstractResourceDecorator.this.changedForegroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_FOREGROUND_COLOR));
				AbstractResourceDecorator.this.changedBackgroundColor = current.getColorRegistry().get(SVNTeamPreferences.fullDecorationName(SVNTeamPreferences.NAME_OF_OUTGOING_BACKGROUND_COLOR));
			}
		});
	}

	protected synchronized void initStatic(final AbstractResourceDecorator self, final IResourceStatesListener targetListener) {
		if (AbstractResourceDecorator.instanceCounter++ == 0) {
			AbstractResourceDecorator.OVR_NEW = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/new_resource.gif");
			// we cannot use pencil icon for modified resources due to CVS uses it in order to show watch/edit states
//			AbstractResourceDecorator.OVR_MODIFIED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/modified_resource.gif");
			AbstractResourceDecorator.OVR_CONFLICTED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/conflicted_unresolved.gif");
			AbstractResourceDecorator.OVR_OBSTRUCTED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/obstructed.gif");
			AbstractResourceDecorator.OVR_DELETED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/deleted.gif");
			AbstractResourceDecorator.OVR_LOCKED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/lock.gif");
			AbstractResourceDecorator.OVR_NEEDS_LOCK = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/needs_lock.gif");
			AbstractResourceDecorator.OVR_SWITCHED = SVNTeamUIPlugin.instance().getImageDescriptor("icons/overlays/switched.gif");
			
			AbstractResourceDecorator.installedListener = new IResourceStatesListener() {
				public void resourcesStateChanged(ResourceStatesChangedEvent event) {
					if (targetListener != null) {
						targetListener.resourcesStateChanged(event);
					}
					else {
						self.fireLabelProviderChanged(new LabelProviderChangedEvent(self, event.getResourcesRecursivelly()));
					}
				}
			};
			SVNRemoteStorage.instance().addResourceStatesListener(ResourceStatesChangedEvent.class, AbstractResourceDecorator.installedListener);
		}
	}
	
	protected synchronized void finiStatic() {
		if (--AbstractResourceDecorator.instanceCounter == 0) {
			SVNRemoteStorage.instance().removeResourceStatesListener(ResourceStatesChangedEvent.class, AbstractResourceDecorator.installedListener);
			AbstractResourceDecorator.installedListener = null;
		}
	}
	
}
