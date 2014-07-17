package org.jboss.tools.maven.ui.wizard;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Settings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.jboss.tools.maven.core.IArtifactResolutionService;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.ui.Activator;

public class AddRepositoryDialog extends TitleAreaDialog {

	private static final String INTERVAL_PREFIX = "interval:"; //$NON-NLS-1$
	private static final String URL_ALREADY_EXISTS = "URL already exists"; //$NON-NLS-1$
	private static final String URL_IS_NOT_VALID = "URL isn't valid."; //$NON-NLS-1$
	private static final String REPOSITORY_NAME_IS_EMPTY = "The use of an empty repository name is discouraged."; //$NON-NLS-1$
	private static final String REPOSITORY_URL_IS_REQUIRED = "Repository URL is required."; //$NON-NLS-1$
	private static final String REPOSITORY_ID_IS_REQUIRED = "Repository ID is required."; //$NON-NLS-1$
	private static final String PROFILE_ID_IS_REQUIRED = "Profile ID is required."; //$NON-NLS-1$
	private static final String ADD_MAVEN_REPOSITORY_TITLE = "Add Maven Repository"; //$NON-NLS-1$
	private static final String EDIT_MAVEN_REPOSITORY_TITLE = "Edit Maven Repository"; //$NON-NLS-1$
	private static final String INVALID_SNAPSHOTS_POLICY = "Invalid snapshots update policy"; //$NON-NLS-1$
	private static final String INVALID_RELEASES_POLICY = "Invalid releases update policy"; //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String CONFIGURE_MAVEN_REPOSITORIES = "ConfigureMavenRepositories"; //$NON-NLS-1$
	private static final String LASTPATH = "lastPath"; //$NON-NLS-1$
	private static final String JSF_IMPL = "com" + File.separator + "sun" + File.separator + "faces" + File.separator + "jsf-impl";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String WFK_BOMS = "com" + File.separator + "redhat" + File.separator + "jboss" + File.separator + "wfk" + File.separator + "boms";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String JBOSS_EAP_MAVEN_REPOSITORY = "JBoss EAP Maven Repository"; //$NON-NLS-1$
	private static final String JBOSS_EAP_MAVEN_REPOSITORY_ID = "jboss-eap-maven-repository";; //$NON-NLS-1$
	private static final String JBOSS_WFK_MAVEN_REPOSITORY_ID = "jboss-wfk-maven-repository";; //$NON-NLS-1$
	private static final String[] UPDATE_POLICIES = new String[] {"never","always","daily","interval:XXX"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] REPO_LAYOUTS = new String[] {"default","p2","legacy"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private Set<RepositoryWrapper> availableRepositories;
	private Set<RepositoryWrapper> includedRepositories;
	private IMaven maven;
	private Combo profileCombo;
	private Button activeByDefaultButton;
	private boolean activeByDefault;
	private Button snapshotsButton;
	private Button releasesButton;
	private Combo snapshotsPolicyCombo;
	private Combo releasesPolicyCombo;
	private Combo repositoryLayout;
	private Text idText;
	private Text urlText;
	private Text nameText;
	private Image jbossImage, resolvedImage, unresolvedImage;
	private IDialogSettings dialogSettings;
	private String localRepository;
	private ControlDecoration profileComboDecoration;
	private ControlDecoration idTextDecoration;
	private ControlDecoration nameTextDecoration;
	private ControlDecoration urlTextDecoration;
	private ControlDecoration urlValidTextDecoration;
	private ControlDecoration urlExistsTextDecoration;
	private ControlDecoration snapshotsPolicyDecoration;
	private ControlDecoration releasesPolicyDecoration;
	
	private RepositoryWrapper repositoryWrapper;
	private ArtifactKey artifactKey;
	private Label artifactLabel;
	private String coords;
	private Label artifactImageLabel;
	private String preSelectedProfile;
	private RepositoryWrapper editWrapper;
	private boolean isEditing;
	private boolean isActive;

	public AddRepositoryDialog(Shell parentShell,
			Set<RepositoryWrapper> availableRepositories,
			Set<RepositoryWrapper> includedRepositories, 
			IMaven maven, ArtifactKey artifactKey) {
		this(parentShell, availableRepositories, 
				includedRepositories, maven, artifactKey, null, false);
	}
	
	public AddRepositoryDialog(Shell parentShell,
			Set<RepositoryWrapper> availableRepositories,
			Set<RepositoryWrapper> includedRepositories, 
			IMaven maven, ArtifactKey artifactKey,
			RepositoryWrapper editWrapper, boolean isActive) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.RESIZE
				| getDefaultOrientation());
		this.availableRepositories = availableRepositories;
		this.includedRepositories = includedRepositories;
		this.maven = maven;
		this.artifactKey = artifactKey;
		this.editWrapper = editWrapper;
		this.isEditing = editWrapper != null;
		this.isActive = isActive;
		ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				"icons/MavenRepositoryWizBan.png"); //$NON-NLS-1$
		setTitleImage(desc.createImage());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (!isEditing) {
			getShell().setText(ADD_MAVEN_REPOSITORY_TITLE);
			setTitle(ADD_MAVEN_REPOSITORY_TITLE);
			setMessage("Enter a new repository"); //$NON-NLS-1$
		} else {
			getShell().setText(EDIT_MAVEN_REPOSITORY_TITLE);
			setTitle(EDIT_MAVEN_REPOSITORY_TITLE);
		}
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		gd.heightHint = 400;
		gd.widthHint = 500;
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout(1, false));
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Group profileGroup = new Group(contents, SWT.NONE);
		profileGroup.setText("Profile"); //$NON-NLS-1$
		profileGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		profileGroup.setLayout(new GridLayout(3, false));

		createLabel(profileGroup, "Profile ID:"); //$NON-NLS-1$

		profileCombo = new Combo(profileGroup, SWT.NONE);
		profileCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		String[] profileIDs = getProfileIds();
		profileCombo.setItems(profileIDs);
		profileCombo.setText(EMPTY_STRING);
		profileCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		profileCombo.setEnabled(!isEditing);
		profileComboDecoration = addDecoration(profileCombo, FieldDecorationRegistry.DEC_REQUIRED, PROFILE_ID_IS_REQUIRED);
		
		activeByDefaultButton = new Button(profileGroup, SWT.CHECK);
		activeByDefaultButton.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, false, false));
		activeByDefaultButton.setText("Active by default"); //$NON-NLS-1$

		profileCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				selectProfile();
			}

		});
		
		Group repositoryGroup = new Group(contents, SWT.NONE);
		repositoryGroup.setText("Repository"); //$NON-NLS-1$
		repositoryGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		repositoryGroup.setLayout(new GridLayout(2, false));
		
		createLabel(repositoryGroup, "ID:"); //$NON-NLS-1$
		idText = createText(repositoryGroup);
		idTextDecoration = addDecoration(idText, FieldDecorationRegistry.DEC_REQUIRED, REPOSITORY_ID_IS_REQUIRED);
		
		createLabel(repositoryGroup, "Name:"); //$NON-NLS-1$
		nameText = createText(repositoryGroup);
		nameTextDecoration = addDecoration(nameText, FieldDecorationRegistry.DEC_WARNING, REPOSITORY_NAME_IS_EMPTY);
		
		createLabel(repositoryGroup, "URL:"); //$NON-NLS-1$
		urlText = createText(repositoryGroup);
		urlTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_REQUIRED, REPOSITORY_URL_IS_REQUIRED);
		urlValidTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_ERROR, URL_IS_NOT_VALID);
		urlExistsTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_ERROR, URL_ALREADY_EXISTS);
		
		createAdvancedComposite(repositoryGroup);
		
		profileCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				selectProfile();
			}

		});
		
		if (!isEditing) {
			createRecognizeButton(contents);
		}
		
		if (artifactKey != null) {
			String message = "The '" + getCoords() + "' artifact"; //$NON-NLS-1$ //$NON-NLS-2$
			final String unresolvedMessage = message + " is not resolved."; //$NON-NLS-1$
			final String resolvedMessage = message + " is resolved."; //$NON-NLS-1$
			Composite labelComposite = new Composite(contents, SWT.NONE);
			labelComposite.setLayout(new GridLayout(2, false));
			labelComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
			artifactImageLabel = createLabel(labelComposite, ""); //$NON-NLS-1$
			artifactLabel = createLabel(labelComposite, unresolvedMessage );
			artifactImageLabel.setImage(getUnresolvedImage());
			urlText.addModifyListener(new ModifyListener() {
				
				public void modifyText(ModifyEvent e) {
					if (getErrorMessage() != null) {
						return;
					}
					final String id = idText.getText().trim();
					final String url = urlText.getText().trim();
					Job job = new Job("Resolving artifact ...") { //$NON-NLS-1$

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							final boolean resolved = resolveArtifact(id, url);
							Display.getDefault().asyncExec(new Runnable() {
								
								public void run() {
									if (resolved) {
										artifactImageLabel.setImage(getResolvedImage());
										artifactLabel.setText(resolvedMessage);
									} else {
										artifactImageLabel.setImage(getUnresolvedImage());
										artifactLabel.setText(unresolvedMessage);
									}
								}
							});
							
							return Status.OK_STATUS;
						}
						
					};
					job.setUser(true);
					job.schedule();
				}
			});
		}

		if (preSelectedProfile != null) {
			for (int i = 0; i < profileIDs.length; i++) {
				if (preSelectedProfile.equals(profileIDs[i])){
					profileCombo.select(i);
					selectProfile();
					break;
				}
			}
		}
		return area;
	}

	public void createRecognizeButton(Composite contents) {
		Button recognizeButton = new Button(contents, SWT.PUSH);
		recognizeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
		recognizeButton.setText("Recognize JBoss Maven Enterprise Repositories..."); //$NON-NLS-1$
		recognizeButton.setImage(getJBossImage());
		
		recognizeButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select the directory in which to search for JBoss Maven Enterprise Repositories:"); //$NON-NLS-1$
				directoryDialog.setText("Search for JBoss Maven Enterprise Repositories"); //$NON-NLS-1$

				dialogSettings = Activator.getDefault().getDialogSettings();
				IDialogSettings configureMavenRepositories = dialogSettings.getSection(CONFIGURE_MAVEN_REPOSITORIES);
				if (configureMavenRepositories == null) {
					configureMavenRepositories = dialogSettings.addNewSection(CONFIGURE_MAVEN_REPOSITORIES);
				}
				String filterPath = configureMavenRepositories.get(LASTPATH);
				if (filterPath != null) {
					directoryDialog.setFilterPath(filterPath);
				}
				String pathStr = directoryDialog.open();
				if (pathStr == null)
					return;
				
				configureMavenRepositories.put(LASTPATH, pathStr);
				final IPath path = new Path(pathStr);
				
				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.setBlockOnOpen(false);
				dialog.setCancelable(true);
				dialog.open();
				final IProgressMonitor monitor = dialog.getProgressMonitor();
				monitor.beginTask("Searching...", 110); //$NON-NLS-1$
				final Set<RepositoryWrapper> repos = new HashSet<RepositoryWrapper>();
				
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor2) {
						searchForRepositories(path, repos, monitor2);
					}
				};
				try {
					dialog.run(true, true, runnable);
				} catch (Exception e1) {
					Activator.log(e1);
				} 
				
				if (monitor.isCanceled()) {
					return;
				}
				if (repos.size() == 0) {
					String url = new File(pathStr).toURI().toString();
					url = url.trim();
					if (!url.endsWith(RepositoryWrapper.SEPARATOR)) {
						url = url + RepositoryWrapper.SEPARATOR;
					}
					Set<RepositoryWrapper> allRepositories = new HashSet<RepositoryWrapper>();
					allRepositories.addAll(includedRepositories);
					allRepositories.addAll(availableRepositories);
					boolean found = false;
					for (RepositoryWrapper wrapper:allRepositories) {
						if (url.equals(wrapper.getRepository().getUrl())) {
							found = true;
							break;
						}
					}
					if (found) {
						MessageDialog.openInformation(getShell(), "Information", "No new repository found."); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					} else {
						boolean ok = MessageDialog.openQuestion(getShell(), "Confirm Add Repository", "No new repository found. Would you like me to add the '" + url + "' repository."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (ok) {
							Repository repository = ConfigureMavenRepositoriesWizardPage.getDefaultRepository();
							repository.setId(getUniqueId(new File(pathStr), "id", allRepositories)); //$NON-NLS-1$
							repository.setName(new File(pathStr).getName());
							repository.setUrl(url);
							RepositoryWrapper wrapper = new RepositoryWrapper(repository, repository.getId());
							repos.add(wrapper);
						}
					}
				}
				for (RepositoryWrapper wrapper:repos) {
					if (!includedRepositories.contains(wrapper)) {
						availableRepositories.add(wrapper);
					}
				}
				if (repos.size() > 0) {
					String[] profileIDs = getProfileIds();
					profileCombo.setItems(profileIDs);
					RepositoryWrapper wrapper = repos.iterator().next();
					profileCombo.setText(wrapper.getProfileId());
					if (wrapper.getRepository() != null) {
						updateRepository(wrapper.getRepository());
						activeByDefaultButton.setSelection(true);
					}
				}
			}
		});
	}

	private String getCoords() {
		if (coords == null && artifactKey != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(artifactKey.getGroupId());
			builder.append(":"); //$NON-NLS-1$
			builder.append(artifactKey.getArtifactId());
			builder.append(":"); //$NON-NLS-1$
			if (artifactKey.getClassifier() != null) {
				builder.append(artifactKey.getClassifier());
				builder.append(":"); //$NON-NLS-1$
			}
			builder.append(artifactKey.getVersion());
			coords = builder.toString();
		}
		return coords;
	}

	private boolean resolveArtifact(String id, String url) {
		try {
			List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
			repos.add(MavenPlugin.getMaven().createArtifactRepository(id, url));
			repos.addAll(MavenPlugin.getMaven().getArtifactRepositories());
			IArtifactResolutionService artifactResolutionService = MavenCoreActivator.getDefault().getArtifactResolutionService();
			return artifactResolutionService.isResolved(coords, repos, new NullProgressMonitor());
		} catch (CoreException e) {
			Activator.log(e);
		}
		return false;
	}

	private Text createText(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE|SWT.BORDER);
		text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		text.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		return text;
	}

	protected void validate() {
		idTextDecoration.hide();
		urlTextDecoration.hide();
		nameTextDecoration.hide();
		profileComboDecoration.hide();
		urlValidTextDecoration.hide();
		urlExistsTextDecoration.hide();
		snapshotsPolicyDecoration.hide();
		releasesPolicyDecoration.hide();
		enableOkButton(true);
		
		setMessage(null);
		if (profileCombo.getText().trim().isEmpty()) {
			setMessage(PROFILE_ID_IS_REQUIRED, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		if (idText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_ID_IS_REQUIRED, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		if (urlText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_URL_IS_REQUIRED, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		String urlString;
		try {
			urlString = new URL(urlText.getText().trim()).toString();
		} catch (MalformedURLException e) {
			setMessage(URL_IS_NOT_VALID, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		if (!urlString.endsWith(RepositoryWrapper.SEPARATOR)) {
			urlString = urlString + RepositoryWrapper.SEPARATOR;
		}
		if (!isEditing) {
			for (RepositoryWrapper wrapper : includedRepositories) {
				if (urlString.equals(wrapper.getRepository().getUrl())) {
					setMessage(URL_ALREADY_EXISTS, IMessageProvider.ERROR);
					enableOkButton(false);
					showDecoration();
					return;
				}
			}
		}
		
		if(!validatePolicy(releasesButton, releasesPolicyCombo)){
			setMessage(INVALID_RELEASES_POLICY, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		
		if(!validatePolicy(snapshotsButton, snapshotsPolicyCombo)){
			setMessage(INVALID_SNAPSHOTS_POLICY, IMessageProvider.ERROR);
			enableOkButton(false);
			showDecoration();
			return;
		}
		
		if (nameText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_NAME_IS_EMPTY, IMessageProvider.WARNING);
			showDecoration();
			return;
		}
	}

	private void enableOkButton(boolean enabled) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(enabled);		
		}
	}

	private void showDecoration() {
		if (profileCombo.getText().trim().isEmpty()) {
			profileComboDecoration.show();
		}
		if (idText.getText().trim().isEmpty()) {
			idTextDecoration.show();
		}
		if (urlText.getText().trim().isEmpty()) {
			urlTextDecoration.show();
		} else {
			String urlString;
			try {
				urlString = new URL(urlText.getText().trim()).toString();
				if (!urlString.endsWith(RepositoryWrapper.SEPARATOR)) {
					urlString = urlString + RepositoryWrapper.SEPARATOR;
				}
				for (RepositoryWrapper wrapper:includedRepositories) {
					if (urlString.equals(wrapper.getRepository().getUrl()) && editWrapper != null && !wrapper.getRepository().equals(editWrapper.getRepository())) {
						urlExistsTextDecoration.show();
					}
				}
			} catch (MalformedURLException e) {
				urlValidTextDecoration.show();
			}
		}
		if (nameText.getText().trim().isEmpty()) {
			nameTextDecoration.show();
		}
		if(!validatePolicy(releasesButton, releasesPolicyCombo)){
			releasesPolicyDecoration.show();
		}
		if(!validatePolicy(snapshotsButton, snapshotsPolicyCombo)){
			snapshotsPolicyDecoration.show();
		}
	}
	
	private boolean validatePolicy(Button policyButton, Combo comboToValidate){
		if(!policyButton.getSelection()){
			return true;
		}
		if (comboToValidate.getText().trim().startsWith(INTERVAL_PREFIX)) {
			String[] interval = comboToValidate.getText().trim().split(INTERVAL_PREFIX);
			if(interval.length != 2){
				return false;
			} else {
				try{
					Integer.parseInt(interval[1]);
				} catch (NumberFormatException ex){
					return false;
				}
			}
		} else if(!Arrays.asList(UPDATE_POLICIES).contains(comboToValidate.getText().trim())){
			return false;
		}
		return true;
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
				false));
		label.setText(text);
		return label;
	}

	private String[] getProfileIds() {
		Set<String> ids = new TreeSet<String>();
		if (!isEditing) {
			ids.add(EMPTY_STRING);
			for (RepositoryWrapper wrapper : availableRepositories) {
				if (wrapper.getProfileId() != null
						&& !wrapper.getProfileId().isEmpty()) {
					ids.add(wrapper.getProfileId());
				}
			}
		} else {
			ids.add(preSelectedProfile);
		}
		return ids.toArray(new String[0]);
	}

	protected void updateRepository(Repository repository) {
		idText.setText(repository.getId() == null ? "" : repository.getId()); //$NON-NLS-1$
		nameText.setText(repository.getName() == null ? "" : repository.getName()); //$NON-NLS-1$
		urlText.setText(repository.getUrl() == null ? "" : repository.getUrl()); //$NON-NLS-1$
		snapshotsButton.setSelection(repository.getSnapshots().isEnabled());
		snapshotsButton.notifyListeners(SWT.Selection, new Event());
		snapshotsPolicyCombo.setText(repository.getSnapshots().getUpdatePolicy());
		releasesButton.setSelection(repository.getReleases().isEnabled());
		releasesButton.notifyListeners(SWT.Selection, new Event());
		releasesPolicyCombo.setText(repository.getReleases().getUpdatePolicy());
		repositoryLayout.setText(repository.getLayout());
	}

	private Image getJBossImage() {
		if (jbossImage == null) {
			ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/jboss.png"); //$NON-NLS-1$
			jbossImage = desc.createImage();
		}
		return jbossImage;
	}
	
	private Image getResolvedImage() {
		if (resolvedImage == null) {
			ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/resolved.gif"); //$NON-NLS-1$
			resolvedImage = desc.createImage();
		}
		return resolvedImage;
	}
	
	private Image getUnresolvedImage() {
		if (unresolvedImage == null) {
			ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/unresolved.gif"); //$NON-NLS-1$
			unresolvedImage = desc.createImage();
		}
		return unresolvedImage;
	}
	
	private void searchForRepositories(IPath path,
			Set<RepositoryWrapper> repos, IProgressMonitor monitor) {
		File[] files = null;
		if (path != null) {
			File f = path.toFile();
			if (f.isDirectory()) {
				files = new File[1];
				files[0] = f;
			}
			else
				return;
		} else
			files = File.listRoots();

		if (files != null) {
			int size = files.length;
			int work = 100 / size;
			int workLeft = 100 - (work * size);
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				if (files[i] != null && files[i].isDirectory()) {
					searchDir(repos, files[i], 4, monitor);
				}
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		} else
			monitor.worked(100);
	}
	
	private void searchDir(Set<RepositoryWrapper> repos, File directory, int depth,
			IProgressMonitor monitor) {
		
		String localRepository = getLocalRepository();
		if (localRepository != null && localRepository.trim().equals(directory.getAbsolutePath())) {
			return;
		}
		monitor.setTaskName("Searching " + directory.getAbsolutePath()); //$NON-NLS-1$
		File comFile = new File(directory, "com"); //$NON-NLS-1$
		if (comFile.isDirectory()) {
			if (getRepositoryFromDir(directory, repos, monitor)) {
				return;
			}
		}
		
		if (depth == 0)
			return;
		
		File[] files = directory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		if (files != null) {
			int size = files.length;
			for (int i = 0; i < size; i++) {
				if (monitor.isCanceled())
					return;
				searchDir(repos, files[i], depth - 1, monitor);
			}
		}
	}

	private boolean getRepositoryFromDir(File directory, Set<RepositoryWrapper> repos, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		
		File file = new File(directory, JSF_IMPL);
		if (file.isDirectory()) {
			File[] list = file.listFiles(new FileFilter() {
				
				public boolean accept(File pathname) {
					if (pathname != null && pathname.getName() != null && pathname.getName().contains("redhat")) { //$NON-NLS-1$
						return true;
					}
					return false;
				}
			});
			if (list != null && list.length >= 1) {
				// JBoss EAP Maven Repository
				Repository repository = ConfigureMavenRepositoriesWizardPage.getDefaultRepository();
				Set<RepositoryWrapper> allRepositories = new HashSet<RepositoryWrapper>();
				allRepositories.addAll(repos);
				allRepositories.addAll(includedRepositories);
				allRepositories.addAll(availableRepositories);
				String url = getUrl(directory);
				if (url == null) {
					return false;
				}
				for (RepositoryWrapper wrapper:allRepositories) {
					if (url.equals(wrapper.getRepository().getUrl())) {
						return true;
					}
				}
				repository.setId(getUniqueId(directory, JBOSS_EAP_MAVEN_REPOSITORY_ID, allRepositories));
				repository.setName(JBOSS_EAP_MAVEN_REPOSITORY);
				repository.setUrl(url);
				RepositoryWrapper wrapper = new RepositoryWrapper(repository, repository.getId());
				repos.add(wrapper);
				return true;
			}
		}
		file = new File(directory, WFK_BOMS);
		if (file.isDirectory()) {
			// JBoss WFK Maven Repository
			Repository repository = ConfigureMavenRepositoriesWizardPage.getDefaultRepository();
			Set<RepositoryWrapper> allRepositories = new HashSet<RepositoryWrapper>();
			allRepositories.addAll(repos);
			allRepositories.addAll(includedRepositories);
			allRepositories.addAll(availableRepositories);
			String url = getUrl(directory);
			if (url == null) {
				return false;
			}
			for (RepositoryWrapper wrapper:allRepositories) {
				if (url.equals(wrapper.getRepository().getUrl())) {
					return true;
				}
			}
			repository.setId(getUniqueId(directory, JBOSS_WFK_MAVEN_REPOSITORY_ID, allRepositories));
			repository.setName("JBoss WFK Maven Repository"); //$NON-NLS-1$
			repository.setUrl(url);
			RepositoryWrapper wrapper = new RepositoryWrapper(repository, repository.getId());
			repos.add(wrapper);
			return true;
		}
		return false;
	}

	protected String getUrl(File directory) {
		String url;
		try {
			url = directory.toURI().toURL().toString();
		} catch (MalformedURLException e1) {
			Activator.log(e1);
			return null;
		}
		url = url.trim();
		if (!url.endsWith(RepositoryWrapper.SEPARATOR)) {
			url = url + RepositoryWrapper.SEPARATOR;
		}
		return url;
	}

	private String getUniqueId(File directory, String simpleId, Set<RepositoryWrapper> allRepositories) {
		int i = 0;
		String id = simpleId;
		try {
			id = directory.toURI().toURL().toString();
		} catch (MalformedURLException e1) {
			Activator.log(e1);
		}
		id = new Path(id).lastSegment();
		id = id.replace(" ", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		id = id.replace("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		id = id.replace(".", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String startId = id;
		while (true) {
			boolean found = false;
			for (RepositoryWrapper wrapper:allRepositories) {
				if (id.equals(wrapper.getRepository().getId())) {
					id = startId + "-" + i++; //$NON-NLS-1$
					found = true;
					break;
				}
			}
			if (!found) {
				return id;
			}
		}
	}

	private String getLocalRepository() {
		if (localRepository == null) {
			String userSettings = ConfigureMavenRepositoriesWizardPage.getUserSettings();
			String globalSettings = MavenPlugin.getMavenConfiguration()
					.getGlobalSettingsFile();
			try {
				Settings settings = maven.buildSettings(globalSettings,
						userSettings);
				localRepository = settings.getLocalRepository();
				if (localRepository == null) {
					localRepository = RepositorySystem.defaultUserLocalRepository
							.getAbsolutePath();
				}
			} catch (CoreException e) {
				Activator.log(e);
			} 
		}
		return localRepository;
	}
	
	@Override
	public boolean close() {
		if (jbossImage != null) {
			jbossImage.dispose();
		}
		if (resolvedImage != null) {
			resolvedImage.dispose();
		}
		if (unresolvedImage != null) {
			unresolvedImage.dispose();
		}
		return super.close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	protected ControlDecoration addDecoration(Control control, String id, String description) {
		final ControlDecoration decPath = new ControlDecoration(control, SWT.TOP
				| SWT.LEFT);
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration fd = registry.getFieldDecoration(id);
		decPath.setImage(fd.getImage());
		fd.setDescription(description);
	
		decPath.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(
				id).getImage());

		decPath.setShowOnlyOnFocus(false);
		decPath.setShowHover(true);
		decPath.setDescriptionText(description);
		return decPath;
	}

	public RepositoryWrapper getRepositoryWrapper() {
		return repositoryWrapper;
	}

	@Override
	protected void okPressed() {
		if (editWrapper != null) {
			repositoryWrapper = editWrapper;
			populateRepository(repositoryWrapper.getRepository());
		} else {
			Repository repository = ConfigureMavenRepositoriesWizardPage
					.getDefaultRepository();
			populateRepository(repository);
			repositoryWrapper = new RepositoryWrapper(repository, profileCombo
					.getText().trim());
		}
		activeByDefault = activeByDefaultButton.getSelection();
		super.okPressed();
	}

	private void populateRepository(Repository repository) {
		repository.setId(idText.getText().trim());
		repository.setName(nameText.getText().trim());
		repository.setUrl(urlText.getText().trim());
		
		RepositoryPolicy snapshotsPolicy = createRepositoryPolicy(snapshotsButton, snapshotsPolicyCombo);
		repository.setSnapshots(snapshotsPolicy);
		RepositoryPolicy releasesPolicy = createRepositoryPolicy(releasesButton, releasesPolicyCombo);
		repository.setReleases(releasesPolicy);
		
		repository.setLayout(repositoryLayout.getText());
	}
	
	private RepositoryPolicy createRepositoryPolicy(Button policyButton, Combo policyCombo){
		RepositoryPolicy policy = new RepositoryPolicy();
		policy.setEnabled(policyButton.getSelection());
		policy.setUpdatePolicy(policyCombo.getText());
		return policy;
	}

	public boolean isActiveByDefault() {
		return activeByDefault;
	}

	public void setPreSelectedProfile(String profileId) {
		this.preSelectedProfile = profileId;
	}

	private void selectProfile() {
		Settings settings;
		String id = profileCombo.getText();
		if (id == null || id.trim().isEmpty()) {
			return;
		}
		if (!isEditing) {
			for (RepositoryWrapper wrapper : availableRepositories) {
				if (wrapper.getProfileId() != null
						&& wrapper.getRepository() != null
						&& id.equals(wrapper.getProfileId())) {
					updateRepository(wrapper.getRepository());
					activeByDefaultButton.setSelection(true);
					return;
				}
			}
		} else {
			updateRepository(editWrapper.getRepository());
			activeByDefaultButton.setSelection(isActive);
			return;
		}
		try {
			settings = maven.getSettings();
			for (Profile profile : settings.getProfiles()) {
				if (id.equals(profile.getId())) {
					if (profile.getActivation() == null) {
						activeByDefaultButton.setSelection(false);
					}
					if (profile.getActivation() != null) {
						activeByDefaultButton.setSelection(profile
							.getActivation().isActiveByDefault());
					}
					List<Repository> repositories = profile
							.getRepositories();
					if (repositories != null
							&& repositories.size() == 1) {
						Repository repository = repositories.get(0);
						updateRepository(repository);
					}
					break;
				}
			}
		} catch (CoreException e1) {
			Activator.log(e1);
		}
	}
	
	private void createAdvancedComposite(final Composite parent){
		
		final ExpandableComposite ex = new ExpandableComposite(parent,ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		ex.setText("Advanced"); //$NON-NLS-1$

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		ex.setLayoutData(gridData);
		
		Composite advancedComposite = new Composite(ex, SWT.NONE);
		advancedComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		advancedComposite.setLayout(new GridLayout(2, false));
		
		createLabel(advancedComposite, "Repository layout: "); //$NON-NLS-1$
		repositoryLayout = new Combo(advancedComposite, SWT.DROP_DOWN);
		populateCombo(repositoryLayout, REPO_LAYOUTS);
		
		Group srGroup = new Group(advancedComposite,SWT.NONE);
		srGroup.setText("Snapshots && Releases"); //$NON-NLS-1$
		srGroup.setLayoutData(gridData);
		srGroup.setLayout(new GridLayout(3, false));
		
		releasesButton = new Button(srGroup, SWT.CHECK);
		releasesButton.setText("Enable releases"); //$NON-NLS-1$
		releasesButton.setSelection(true);
		releasesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(releasesButton.getSelection()){
					releasesPolicyCombo.setEnabled(true);
				} else {
					releasesPolicyCombo.setEnabled(false);
				}
				validate();
			}
		});
		
		createLabel(srGroup, "Update policy: "); //$NON-NLS-1$
		releasesPolicyCombo = new Combo(srGroup, SWT.DROP_DOWN);
		populateCombo(releasesPolicyCombo,UPDATE_POLICIES);
		releasesPolicyCombo.select(0);//Default set to never update releases
		releasesPolicyDecoration = addDecoration(releasesPolicyCombo, FieldDecorationRegistry.DEC_ERROR, INVALID_RELEASES_POLICY);
		releasesPolicyCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		snapshotsButton = new Button(srGroup, SWT.CHECK);
		snapshotsButton.setText("Enable snapshots"); //$NON-NLS-1$
		snapshotsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(snapshotsButton.getSelection()){
					snapshotsPolicyCombo.setEnabled(true);
					validate();
				} else {
					snapshotsPolicyCombo.setEnabled(false);
				}
				validate();
			}
		});

		createLabel(srGroup, "Update policy: "); //$NON-NLS-1$
		snapshotsPolicyCombo = new Combo(srGroup, SWT.DROP_DOWN);
		populateCombo(snapshotsPolicyCombo,UPDATE_POLICIES);
		snapshotsPolicyCombo.select(2);//Default set to update snapshots daily
		snapshotsPolicyDecoration = addDecoration(snapshotsPolicyCombo, FieldDecorationRegistry.DEC_ERROR, INVALID_SNAPSHOTS_POLICY);
		snapshotsPolicyCombo.setEnabled(false);
		snapshotsPolicyCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		ex.setClient(advancedComposite);
		
		ex.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				parent.getParent().layout();
			}
		});
		
	}

	private void populateCombo(Combo combo, String[] values){
		for(String value : values){
			combo.add(value);
		}
	}
}

