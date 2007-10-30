/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin (Polarion Software) - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.ui.panel.view.property;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.svn.core.IStateFilter;
import org.eclipse.team.svn.core.client.PropertyData;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.local.RefreshResourcesOperation;
import org.eclipse.team.svn.core.operation.local.property.IPropertyProvider;
import org.eclipse.team.svn.core.operation.local.property.SetMultiPropertiesOperation;
import org.eclipse.team.svn.core.resource.IResourceProvider;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.StringMatcher;
import org.eclipse.team.svn.ui.SVNTeamUIPlugin;
import org.eclipse.team.svn.ui.dialog.DefaultDialog;
import org.eclipse.team.svn.ui.panel.AbstractDialogPanel;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;
import org.eclipse.team.svn.ui.utility.UserInputHistory;
import org.eclipse.team.svn.ui.verifier.AbstractVerifierProxy;
import org.eclipse.team.svn.ui.verifier.NonEmptyFieldVerifier;

/**
 * Keyword property edit panel
 * 
 * @author Sergiy Logvin
 */
public class PropertyKeywordEditPanel extends AbstractDialogPanel {
	
	protected CheckboxTableViewer checkboxViewer;
	protected Button setRecursivelyCheckbox;
	protected Button useMaskCheckbox;
	protected Combo maskText;
	
	protected IResource []selectedResources;
	protected IResource []alreadyWithProperties;
	protected IPropertyProvider properties;
	protected boolean recursionEnabled;
	
	protected boolean setRecursively;
	protected String mask;
	protected boolean useMask;
	protected boolean computeStates;
	
	protected KeywordTableElement dateElement;
	protected KeywordTableElement revisionElement;
	protected KeywordTableElement lastChangedByElement;
	protected KeywordTableElement headUrlElement;
	protected KeywordTableElement idElement;
	
	protected UserInputHistory maskHistory;

	public PropertyKeywordEditPanel(IResource []selection, IResourceProvider alreadyWithProperties, IPropertyProvider properties) {
		super();
		this.selectedResources = selection;
		this.properties = properties;
		this.alreadyWithProperties = alreadyWithProperties == null ? new IResource[0] : alreadyWithProperties.getResources();
		this.recursionEnabled = FileUtility.checkForResourcesPresence(selection, new IStateFilter() {
			public boolean allowsRecursion(IResource resource, String state, int mask) {
				return false;
			}
			public boolean accept(IResource resource, String state, int mask) {
				return resource instanceof IContainer;
			}
		}, IResource.DEPTH_ZERO);
		this.dialogTitle = SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Title");
		this.dialogDescription = SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Description");
		this.defaultMessage = SVNTeamUIPlugin.instance().getResource(this.alreadyWithProperties.length > 1? "PropertyKeywordEditPanel.Message.Single" : "PropertyKeywordEditPanel.Message.Multi");
		
		this.initializeKeywordElements();
	}
	
	public void createControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		   
		this.checkboxViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.checkboxViewer.getTable().setLayoutData(data);
		   
		TableLayout tlayout = new TableLayout();
		this.checkboxViewer.getTable().setLayout(tlayout);
		   
		TableColumn column = new TableColumn(this.checkboxViewer.getTable(), SWT.LEFT);
		column.setText(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Keyword"));
		tlayout.addColumnData(new ColumnWeightData(20, true));
	       
		column = new TableColumn(this.checkboxViewer.getTable(), SWT.LEFT);
		column.setText(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Description1"));
		tlayout.addColumnData(new ColumnWeightData(50, true));
		
		column = new TableColumn(this.checkboxViewer.getTable(), SWT.LEFT);
		column.setText(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Sample"));
		tlayout.addColumnData(new ColumnWeightData(30, true));
		
		final KeywordTableElement[] elements = new KeywordTableElement[] {this.dateElement, this.revisionElement, this.lastChangedByElement, this.headUrlElement, this.idElement};
		
		this.checkboxViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return elements;
			}	
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		this.checkboxViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event){
				KeywordTableElement element = (KeywordTableElement)event.getElement();
	    		   
				if (element.getCurrentState() == KeywordTableElement.DESELECTED) {
					element.setCurrentState(KeywordTableElement.SELECTED);
				}
				else if ((element.getCurrentState() == KeywordTableElement.SELECTED) &&
						(element.getInitialState() == KeywordTableElement.MIXED)) {
					element.setCurrentState(KeywordTableElement.MIXED);
				}
				else {
					element.setCurrentState(KeywordTableElement.DESELECTED);
				}
				PropertyKeywordEditPanel.this.refreshCheckboxState(element);
			}
		});
	       
		this.checkboxViewer.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				KeywordTableElement keyElement = (KeywordTableElement)element;
				switch (columnIndex) {
					case 0: {
						return keyElement.getName();
					}
					case 1: {
						return keyElement.getDescription();
					}
					case 2: {
						return keyElement.getSample();
					}
					default: {
						return "";
					}
				}
			}
			
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {           
			}
		});
		
		this.checkboxViewer.setInput(elements);
		this.checkboxViewer.getTable().setHeaderVisible(true);
		
		this.addSelectionButtons(composite);
		
		if (this.recursionEnabled || this.selectedResources.length > 1) {
			Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			separator.setVisible(false);
			
			Composite subComposite = new Composite (composite, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			layout.numColumns = 2;
			subComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			subComposite.setLayout(layout);
			
			Composite maskComposite = new Composite (subComposite, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 0;
			layout.numColumns = 2;
			maskComposite.setLayout(layout);
			maskComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.useMaskCheckbox = new Button(maskComposite, SWT.CHECK);
			this.useMaskCheckbox.setText(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.UseMask"));
			this.maskHistory = new UserInputHistory("keywordsEditPanel");
			this.maskText = new Combo(maskComposite, SWT.BORDER);
			this.maskText.setItems(this.maskHistory.getHistory());
			if (this.maskText.getItemCount() == 0) {
				this.maskText.setText("*");
			}
			else {
				this.maskText.select(0);
			}
			this.maskText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					PropertyKeywordEditPanel.this.checkboxViewer.setAllGrayed(false);
					PropertyKeywordEditPanel.this.changeMixedElementsToChecked();
				}
			});
			this.attachTo(this.maskText,
					new AbstractVerifierProxy(new NonEmptyFieldVerifier(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Mask.Verifier"))) {
						protected boolean isVerificationEnabled(Control input) {
							return PropertyKeywordEditPanel.this.useMaskCheckbox.getSelection();
						}
			});
			data = new GridData();
			data.widthHint = 170;
			this.maskText.setLayoutData(data);
			
			this.maskText.setEnabled(false);
			
			this.useMaskCheckbox.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					PropertyKeywordEditPanel.this.maskText.setEnabled(PropertyKeywordEditPanel.this.useMaskCheckbox.getSelection());
					PropertyKeywordEditPanel.this.checkboxViewer.setAllGrayed(false);
					PropertyKeywordEditPanel.this.changeMixedElementsToChecked();
					PropertyKeywordEditPanel.this.validateContent();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			if (this.recursionEnabled) {
				this.setRecursivelyCheckbox = new Button(subComposite, SWT.CHECK);
				this.setRecursivelyCheckbox.setText(SVNTeamUIPlugin.instance().getResource("PropertyKeywordEditPanel.Recursively"));
				this.setRecursivelyCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
				this.setRecursivelyCheckbox.setSelection(true);
				this.setRecursivelyCheckbox.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						PropertyKeywordEditPanel.this.checkboxViewer.setAllGrayed(false);
						PropertyKeywordEditPanel.this.changeMixedElementsToChecked();
					}
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
	       
		for (int i = 0; i < elements.length;i++) {
			this.refreshCheckboxState(elements[i]);
		}
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.setKeysDialogContext";
    }
	
	public void performKeywordChanges() {
		//if filtration by mask is enabled - remove all non-matching resources from the operable map
		IStateFilter filter = IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES;
		if (this.useMask) {
			filter = new IStateFilter() {
				private StringMatcher fileNameMatcher = new StringMatcher(PropertyKeywordEditPanel.this.mask);
				
				public boolean allowsRecursion(IResource resource, String state, int mask) {
					return IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED.allowsRecursion(resource, state, mask);
				}
				public boolean accept(IResource resource, String state, int mask) {
					if (!IStateFilter.SF_EXCLUDE_PREREPLACED_AND_DELETED_FILES.accept(resource, state, mask)) {
						return false;
					}
					return this.fileNameMatcher.match(resource.getName());
				}
			};
		}
		
		IResourceProvider resourceProvider = new IResourceProvider() {
			public IResource []getResources() {
				return PropertyKeywordEditPanel.this.selectedResources;
			}
		};
		
		IPropertyProvider propertyProvider = new IPropertyProvider() {
			public PropertyData []getProperties(IResource resource) {
				PropertyData []retVal = PropertyKeywordEditPanel.this.properties == null ? null : PropertyKeywordEditPanel.this.properties.getProperties(resource);
				if (retVal == null) {
					retVal = new PropertyData[1];
				}
				SVNKeywordProperty keyProperty = new SVNKeywordProperty(retVal[0] == null ? null : retVal[0].value);
				PropertyKeywordEditPanel.this.configureProperty(keyProperty);
				retVal[0] = new PropertyData(PropertyData.KEYWORDS, keyProperty.toString(), null);
				return retVal;
			}
		};
		   
		CompositeOperation composite = new CompositeOperation("Operation.SetKeywords");
		composite.add(new SetMultiPropertiesOperation(resourceProvider, propertyProvider, filter, this.recursionEnabled && this.setRecursively ? IResource.DEPTH_INFINITE : IResource.DEPTH_ONE));
		composite.add(new RefreshResourcesOperation(resourceProvider));
		UIMonitorUtility.doTaskScheduledActive(composite);
	}
	
	protected void configureProperty(SVNKeywordProperty keyProperty) {
		keyProperty.setDateEnabled(this.dateElement.getCurrentState() == KeywordTableElement.SELECTED ? true : 
			(this.dateElement.getCurrentState() == KeywordTableElement.DESELECTED ? false : keyProperty.isDateEnabled()));
		
		keyProperty.setRevisionEnabled(this.revisionElement.getCurrentState() == KeywordTableElement.SELECTED ? true :
			(this.revisionElement.getCurrentState() == KeywordTableElement.DESELECTED ? false : keyProperty.isLastChangedByEnabled()));
           
		keyProperty.setLastChangedByEnabled(this.lastChangedByElement.getCurrentState() == KeywordTableElement.SELECTED ? true :
			(this.lastChangedByElement.getCurrentState() == KeywordTableElement.DESELECTED ? false : keyProperty.isLastChangedByEnabled()));
		
		keyProperty.setHeadUrlEnabled(this.headUrlElement.getCurrentState() == KeywordTableElement.SELECTED ? true :
			(this.headUrlElement.getCurrentState() == KeywordTableElement.DESELECTED ? false : keyProperty.isHeadUrlEnabled()));
           
		keyProperty.setIdEnabled(this.idElement.getCurrentState() == KeywordTableElement.SELECTED ? true :
			(this.idElement.getCurrentState() == KeywordTableElement.DESELECTED ? false : keyProperty.isIdEnabled()));
	}
	
	protected void applyCurrentKeywordValuesOnTableElement(KeywordTableElement tableElement, boolean propertyPresent) {
		tableElement.setInitialState(tableElement.getInitialState() == KeywordTableElement.INITIAL ? (propertyPresent ? KeywordTableElement.SELECTED : KeywordTableElement.DESELECTED) :
			(((propertyPresent && tableElement.getInitialState() == KeywordTableElement.DESELECTED) || (!propertyPresent && tableElement.getInitialState() == KeywordTableElement.SELECTED)) ? KeywordTableElement.MIXED : tableElement.getInitialState()));

		tableElement.setCurrentState(tableElement.getInitialState());
	}
	
	protected void initializeKeywordElements() {
		this.dateElement = new KeywordTableElement(SVNKeywordProperty.DATE_NAMES[0], SVNKeywordProperty.DATE_DESCR(), SVNKeywordProperty.DATE_SAMPLE, KeywordTableElement.INITIAL);
		this.revisionElement = new KeywordTableElement(SVNKeywordProperty.REVISION_NAMES[0], SVNKeywordProperty.REVISION_DESCR(), SVNKeywordProperty.REVISION_SAMPLE, KeywordTableElement.INITIAL);
		this.lastChangedByElement = new KeywordTableElement(SVNKeywordProperty.AUTHOR_NAMES[0], SVNKeywordProperty.AUTHOR_DESCR(), SVNKeywordProperty.AUTHOR_SAMPLE, KeywordTableElement.INITIAL);
		this.headUrlElement = new KeywordTableElement(SVNKeywordProperty.HEAD_URL_NAMES[0], SVNKeywordProperty.HEAD_URL_DESCR(), SVNKeywordProperty.HEAD_URL_SAMPLE, KeywordTableElement.INITIAL);
		this.idElement = new KeywordTableElement(SVNKeywordProperty.ID_NAMES[0], SVNKeywordProperty.ID_DESCR(), SVNKeywordProperty.ID_SAMPLE, KeywordTableElement.INITIAL);

		List alreadyWithPropertiesList = Arrays.asList(this.alreadyWithProperties);
		for (int i = 0; i < this.selectedResources.length; i++) {
			PropertyData[] data;
			SVNKeywordProperty keywordPropertyValue = new SVNKeywordProperty(null);
			if (alreadyWithPropertiesList.contains(this.selectedResources[i]) &&
				this.properties != null &&
				(data = this.properties.getProperties(this.selectedResources[i])) != null) {
				keywordPropertyValue = new SVNKeywordProperty(data[0].value);
			}
	    	this.applyCurrentKeywordValuesOnTableElement(this.dateElement, keywordPropertyValue.isDateEnabled());
	    	this.applyCurrentKeywordValuesOnTableElement(this.revisionElement, keywordPropertyValue.isRevisionEnabled());            
	    	this.applyCurrentKeywordValuesOnTableElement(this.lastChangedByElement, keywordPropertyValue.isLastChangedByEnabled());
	    	this.applyCurrentKeywordValuesOnTableElement(this.headUrlElement, keywordPropertyValue.isHeadUrlEnabled());
	    	this.applyCurrentKeywordValuesOnTableElement(this.idElement, keywordPropertyValue.isIdEnabled());          
		}
	}
	      
	protected void refreshCheckboxState(KeywordTableElement element) {
		this.checkboxViewer.setChecked(element, element.getCurrentState() == KeywordTableElement.MIXED || element.getCurrentState() == KeywordTableElement.SELECTED);
		this.checkboxViewer.setGrayed(element, element.getCurrentState() == KeywordTableElement.MIXED);
	}
	
	protected void changeMixedElementsToChecked() {
		Object []elements = this.checkboxViewer.getCheckedElements();
		for (int i = 0; i < elements.length; i++) {
			((KeywordTableElement)elements[i]).setCurrentState(KeywordTableElement.SELECTED);
		}
	}
	   
	protected void addSelectionButtons(Composite composite) {
		   
		Composite tComposite = new Composite(composite, SWT.RIGHT);
		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 2;
		gLayout.marginWidth = 0;
		tComposite.setLayout(gLayout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		tComposite.setData(data);
		   
		Button selectButton = new Button(tComposite, SWT.PUSH);
		selectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.SelectAll"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(selectButton);
		selectButton.setLayoutData(data);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropertyKeywordEditPanel.this.refreshKeywordElements(true);
			}
		};
		selectButton.addSelectionListener(listener);
		
		Button deselectButton = new Button(tComposite, SWT.PUSH);
		deselectButton.setText(SVNTeamUIPlugin.instance().getResource("Button.ClearSelection"));
		data = new GridData();
		data.widthHint = DefaultDialog.computeButtonWidth(deselectButton);
		deselectButton.setLayoutData(data);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropertyKeywordEditPanel.this.refreshKeywordElements(false);
			   }
		   };
		   deselectButton.addSelectionListener(listener);
	   }
	   
	   protected void refreshKeywordElements(boolean selected) {
		   int state = selected ? KeywordTableElement.SELECTED : KeywordTableElement.DESELECTED;
		   this.dateElement.setCurrentState(state);
		   this.revisionElement.setCurrentState(state);
		   this.lastChangedByElement.setCurrentState(state);
		   this.headUrlElement.setCurrentState(state);
		   this.idElement.setCurrentState(state);
           this.checkboxViewer.setAllChecked(selected);
           this.checkboxViewer.setAllGrayed(false);
	   }
	   
	   protected void cancelChanges() {
		   
	   }
	   
	   protected void saveChanges() {
		   this.useMask = this.useMaskCheckbox == null ? false : this.useMaskCheckbox.getSelection();
		   this.mask = this.maskText == null ? "*" : this.maskText.getText().trim();
		   this.setRecursively = this.setRecursivelyCheckbox == null ? false : this.setRecursivelyCheckbox.getSelection();
		   if (this.useMask) {
			   this.maskHistory.addLine(this.maskText.getText());
		   }
	   }
	   
	   public Point getPrefferedSize() {
		   return new Point(670, SWT.DEFAULT);
	   }
		
}
