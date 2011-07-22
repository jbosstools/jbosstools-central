/*************************************************************************************
 * Copyright (c) 2009-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.maven.ui.internal.profiles;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.maven.core.profiles.ProfileState;
import org.jboss.tools.maven.ui.Messages;

public class SelectProfilesDialog extends TitleAreaDialog implements
		IMenuListener {

	private static int PROFILE_ID_COLUMN = 0;
	private static int SOURCE_COLUMN = 1;

	private CheckboxTableViewer profileTableViewer;
	private Button offlineModeBtn;
	private Button forceUpdateBtn;
    //private ITextViewer profilesTextViewer;
    private Text profilesText;
	
	private boolean offlineMode ;
	private boolean forceUpdate;

	List<ProfileSelection> sharedProfiles;
	Set<IMavenProjectFacade> facades;
	IMavenProjectFacade facade;
	
	
	
	

	final Action activationAction = new ChangeProfileStateAction(ProfileState.Active);
	
	final Action deActivationAction = new ChangeProfileStateAction(ProfileState.Disabled);

	public SelectProfilesDialog(Shell parentShell, Set<IMavenProjectFacade> facades,
			List<ProfileSelection> sharedProfiles) {
		super(parentShell);
		setShellStyle(super.getShellStyle() | SWT.RESIZE | SWT.MODELESS);
		offlineMode = MavenPlugin.getMavenConfiguration().isOffline();
		this.facades = facades;
		if(facades.size() == 1){
			facade = facades.iterator().next();
		}
		this.sharedProfiles = sharedProfiles; 
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.SelectProfilesDialog_Select_Maven_profiles);
	}
	


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
        container.setEnabled(true);
        
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 12;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setTitle(Messages.SelectProfilesDialog_Maven_profile_selection);
		String text;
		if (facade == null) {
			text = Messages.SelectProfilesDialog_Select_active_profiles_for_selected_projects;
		} else {
			text = NLS.bind(
					Messages.SelectProfilesDialog_Select_the_active_Maven_profiles,
					facade.getProject().getName());
		}
		setMessage(text);

		if (facade != null) {
			
		    Label profilesLabel = new Label(container, SWT.NONE);
		    profilesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		    profilesLabel.setText(NLS.bind(Messages.SelectProfilesDialog_Active_Profiles_for_Project, facade.getProject().getName()));
			
			profilesText = new Text(container, SWT.BORDER);
			profilesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			profilesText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			profilesText.setEditable(false);
			profilesText.setToolTipText(Messages.SelectProfilesDialog_Read_Only_profiles);
			updateProfilesText();
		}

		boolean hasProfiles = !sharedProfiles.isEmpty();
		Label warningLabel = new Label(container,  SWT.CENTER); 
		warningLabel.setVisible(false); 
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(warningLabel);
		Label lblAvailable = new Label(container, SWT.NONE);
		String textLabel;
		
		if (hasProfiles) {
			if (facade == null) {
				textLabel = Messages.SelectProfilesDialog_Common_profiles;
				warningLabel.setImage(JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING));
				warningLabel.setVisible(true);
			} else {
				textLabel = Messages.SelectProfilesDialog_Available_profiles;
			}
		} else {
			if (facade == null) {
				textLabel = Messages.SelectProfilesDialog_No_Common_Profiles;
			} else {
				textLabel = 
				NLS.bind(Messages.SelectProfilesDialog_Project_has_no_available_profiles, facade.getProject().getName());
			}
		}
		lblAvailable.setText(textLabel);
		lblAvailable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		if (hasProfiles) {

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4);
			gd.heightHint = 200;
			gd.widthHint = 500;

			profileTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
			Table table = profileTableViewer.getTable();
			table.setFocus();
			table.setLayoutData(gd);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);

			TableColumn profileColumn = new TableColumn(table, SWT.NONE);
			profileColumn.setText(Messages.SelectProfilesDialog_Profile_id_header);
			profileColumn.setWidth(350);
			
			TableColumn sourceColumn = new TableColumn(table, SWT.NONE);
			sourceColumn.setText(Messages.SelectProfilesDialog_Profile_source_header);
			sourceColumn.setWidth(120);
			

			profileTableViewer.setContentProvider(ArrayContentProvider.getInstance());

			profileTableViewer.setLabelProvider(new ProfileLabelProvider(parent
					.getFont()));

			
			profileTableViewer.addCheckStateListener(new ICheckStateListener() {
				
				public void checkStateChanged(CheckStateChangedEvent event) {
					ProfileSelection profile = (ProfileSelection) event.getElement();
					if (profileTableViewer.getGrayed(profile)) {
						profileTableViewer.setGrayed(profile, false);
					}
					profile.setSelected(profileTableViewer.getChecked(profile));
					if (Boolean.FALSE.equals(profile.getSelected()) 
							|| profile.getActivationState() == null) {
						profile.setActivationState(ProfileState.Active);
					}
					
					updateProfilesText();
					profileTableViewer.refresh();
				}
			});
			
			profileTableViewer.setInput(sharedProfiles);

			addSelectionButton(container, Messages.SelectProfilesDialog_SelectAll, true);

			addSelectionButton(container, Messages.SelectProfilesDialog_DeselectAll, false);
			
			offlineModeBtn = addCheckButton(container, Messages.SelectProfilesDialog_Offline, offlineMode);

			forceUpdateBtn = addCheckButton(container, Messages.SelectProfilesDialog_Force_update, forceUpdate);

			createMenu();
		}

		return area;
	}

	private void updateProfilesText() {
		if (profilesText != null) {
			profilesText.setText(ProfileUtil.toString(sharedProfiles));
		}
	}

	private Button addCheckButton(Composite container, String label, boolean selected) {
		Button checkBtn = new Button(container, SWT.CHECK);
		checkBtn.setText(label);
		checkBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				true, false, 2, 1));
		checkBtn.setSelection(selected);
		return checkBtn;
	}

	private Button addSelectionButton(Composite container, String label, final boolean ischecked) {
		Button button = new Button(container, SWT.NONE);
		button.setLayoutData(new GridData(SWT.FILL, SWT.UP,
				false, false, 1, 1));
		button.setText(label);
		button.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				profileTableViewer.setAllGrayed(false);
				for (ProfileSelection profile : sharedProfiles) {
					profileTableViewer.setChecked(profile, ischecked);
					
					profile.setSelected(profileTableViewer.getChecked(profile));
					if (profile.getActivationState() == null) {
 						profile.setActivationState(ProfileState.Active);
					}
				}
				updateProfilesText();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		return button;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (profileTableViewer != null) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		if (profileTableViewer != null) {
			offlineMode = offlineModeBtn.getSelection();
			forceUpdate = forceUpdateBtn.getSelection();
		}
		super.okPressed();
	}

	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		Menu contextMenu = menuMgr.createContextMenu(profileTableViewer
				.getControl());
		menuMgr.addMenuListener(this);
		profileTableViewer.getControl().setMenu(contextMenu);
		menuMgr.setRemoveAllWhenShown(true);

		for (ProfileSelection p : sharedProfiles) {
			Boolean selected = p.getSelected();
			if (selected ==null || p.getActivationState() == null) {
				profileTableViewer.setGrayed(p, true);
				profileTableViewer.setChecked(p, true);
			} else if(selected != null) {
				profileTableViewer.setChecked(p, selected );
			}
		}
	}

	public void menuAboutToShow(IMenuManager manager) {

		IStructuredSelection selection = (IStructuredSelection) profileTableViewer
				.getSelection();
		if (!selection.isEmpty()) {
			final ProfileSelection entry = (ProfileSelection) selection.getFirstElement();
			String text = ""; //$NON-NLS-1$
			ProfileState state = entry.getActivationState();
			if ( state == null || state.equals(ProfileState.Disabled)) {
				text = Messages.SelectProfilesDialog_Activate_menu;
				activationAction.setText(NLS.bind(text, entry.getId()));
				manager.add(activationAction);
			} 
			if( !ProfileState.Disabled.equals(state)) {
				text = Messages.SelectProfilesDialog_Deactivate_menu;
				deActivationAction.setText(NLS.bind(text, entry.getId()));
				manager.add(deActivationAction);
			}
		}
	}

	public boolean isOffline() {
		return offlineMode;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}
	
	
	/* TODO add content assist to the profiles input text
	private ITextViewer createTextInput(Composite container) {
		ITextViewer textViewer = new TextViewer(container, SWT.BORDER | SWT.FILL);
		textViewer.setDocument(new Document());
		final ContentAssistant contentAssistant = new ContentAssistant();
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(100);
		contentAssistant.enableAutoInsert(true);
		contentAssistant.setContentAssistProcessor(new ProfileContentAssistProcessor(sharedProfiles), 
												                     IDocument.DEFAULT_CONTENT_TYPE);
		
	    textViewer.getTextWidget().addKeyListener(new KeyAdapter() {
	    	@Override
	    	public void keyPressed(KeyEvent e) {
	            if ( (e.character == ' ') && ((e.stateMask & SWT.CTRL) != 0) ) {
	              contentAssistant.showPossibleCompletions();
	            }
	    	}
	    }); 
	    
	    
		contentAssistant.install(textViewer );
		return textViewer;
	}
	
	private final class ProfileContentAssistProcessor implements
			IContentAssistProcessor {
		
		private List<String> profileIds;
		
		public ProfileContentAssistProcessor(
				List<ProfileSelection> possibleProfiles) {
			assert possibleProfiles != null;
			profileIds = new ArrayList<String>(possibleProfiles.size());
			for (ProfileSelection ps : possibleProfiles) {
				profileIds.add(ps.getId());
			}
		}

		public String getErrorMessage() {
			return null;
		}

		public IContextInformationValidator getContextInformationValidator() {
			return null;
		}

		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		public char[] getCompletionProposalAutoActivationCharacters() {
			return new char[]{','};
		}

		public IContextInformation[] computeContextInformation(ITextViewer viewer,
				int offset) {
			// TODO Auto-generated method stub
			return null;
		}

		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
				int documentOffset) {
		    IDocument document = viewer.getDocument();
		    int currOffset = documentOffset -1;
		    ICompletionProposal[] proposals = null;
		    try {
		      String currentWord = getCurrentWord(document, currOffset);
		      System.err.println(currOffset + " - Current word "+currentWord); //$NON-NLS-1$
				
		      int currentWordLength = currentWord.length();
		      List<ICompletionProposal> suggestions = new ArrayList<ICompletionProposal>(profileIds.size());
		      for (String id : profileIds) {
		    	  if (!id.startsWith(currentWord)) {
		    		  continue;
		    	  }
		    	  
		    	  ICompletionProposal proposal = new CompletionProposal(id,  
		    			  documentOffset - currentWordLength,
		    			  currentWordLength, 
		    			  id.length());		    	  
		    	  
		    	  suggestions.add(proposal);
		      }
		      if (!suggestions.isEmpty()) {
		    	  proposals = new ICompletionProposal[suggestions.size()];
		    	  suggestions.toArray(proposals);
		      }
		    } catch (BadLocationException e) {
		    	System.err.println(e);
		    }			
		    return proposals;
		}


		private String getCurrentWord(IDocument document, int currOffset)
				throws BadLocationException {
			StringBuilder currWord = new StringBuilder();
		      char currChar;
		      
		      while (currOffset > -1
		          && !Character.isWhitespace(currChar = document
		              .getChar(currOffset))
		          && ',' != currChar) {
		        currWord.insert(0, currChar);
		        currOffset--;
		      }
			return currWord.toString();
		}
	}
   */
	
	private class ChangeProfileStateAction extends Action {
		
		private final ProfileState state;

		public ChangeProfileStateAction(ProfileState state) {
			this.state = state;
		}

		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) profileTableViewer
					.getSelection();
			if (!selection.isEmpty()) {
				final ProfileSelection entry = (ProfileSelection) selection.getFirstElement();
				entry.setActivationState(state);
				profileTableViewer.setChecked(entry, true);
				profileTableViewer.setGrayed(entry, false);
				if (ProfileState.Disabled.equals(state)) {
					entry.setSelected(true);
				}
				updateProfilesText();
				profileTableViewer.refresh();
			}
			super.run();
		}
	}

	private class ProfileLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		
		private Font implicitActivationFont;
		
		private Color inactiveColor;
		
		public ProfileLabelProvider(Font defaultFont) {
			FontData[] fds = defaultFont.getFontData();
			for (FontData fd : fds) {
				fd.setStyle(SWT.ITALIC);
			}
			implicitActivationFont = new Font(defaultFont.getDevice(), fds);
			inactiveColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		}
		
		public Font getFont(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			Font font = null;
			if (Boolean.TRUE.equals(entry.getAutoActive())
					&& PROFILE_ID_COLUMN == columnIndex) {
				font = implicitActivationFont;
			}
			return font;
		}
		
		public Color getForeground(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			if (ProfileState.Disabled.equals(entry.getActivationState())) {
				return inactiveColor;
			}
			return null;
		}
		
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			ProfileSelection entry = (ProfileSelection) element;
			StringBuilder text = new StringBuilder();
			if (entry != null) {
				if (columnIndex == PROFILE_ID_COLUMN) {
					text.append(entry.getId());

					ProfileState state = entry.getActivationState();
					if (Boolean.TRUE.equals(entry.getSelected()) && state == ProfileState.Disabled) {
						text.append(Messages.SelectProfilesDialog_deactivated);
					} else if (Boolean.TRUE.equals(entry.getAutoActive())) {
						text.append(Messages.SelectProfilesDialog_autoactivated);
					}
				} else if (columnIndex == SOURCE_COLUMN) {
					text.append(entry.getSource());
				}
			}
			return text.toString();
		}
	}
}
