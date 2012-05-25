package org.jboss.tools.maven.ui.wizard;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.maven.ui.Activator;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class AddRepositoryDialog extends TitleAreaDialog {

	private static final String URL_ALREADY_EXISTS = "URL already exists";
	private static final String URL_IS_NOT_VALID = "URL isn't valid.";
	private static final String REPOSITORY_NAME_IS_EMPTY = "The use of an empty repository name is discouraged.";
	private static final String REPOSITORY_URL_IS_REQUIRED = "Repository URL is required.";
	private static final String REPOSITORY_ID_IS_REQUIRED = "Repository ID is required.";
	private static final String PROFILE_ID_IS_REQUIRED = "Profile ID is required.";
	private static final String ADD_MAVEN_REPOSITORY_TITLE = "Add Maven Repository";
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String CONFIGURE_MAVEN_REPOSITORIES = "ConfigureMavenRepositories"; //$NON-NLS-1$
	private static final String LASTPATH = "lastPath"; //$NON-NLS-1$
	private static final String JSF_IMPL = "com" + File.separator + "sun" + File.separator + "faces" + File.separator + "jsf-impl";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String WFK_BOMS = "com" + File.separator + "redhat" + File.separator + "jboss" + File.separator + "wfk" + File.separator + "boms";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String JBOSS_EAP_MAVEN_REPOSITORY = "JBoss EAP Maven Repository"; //$NON-NLS-1$
	private static final String JBOSS_EAP_MAVEN_REPOSITORY_ID = "jboss-eap-maven-repository";; //$NON-NLS-1$
	private static final String JBOSS_WFK_MAVEN_REPOSITORY_ID = "jboss-wfk-maven-repository";; //$NON-NLS-1$

	private Set<RepositoryWrapper> availableRepositories;
	private Set<RepositoryWrapper> includedRepositories;
	private IMaven maven;
	private Combo profileCombo;
	private Button activeByDefaultButton;
	private boolean activeByDefault;
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
	
	private RepositoryWrapper repositoryWrapper;
	private ArtifactKey artifactKey;
	private Label artifactLabel;
	private String coords;
	private Label artifactImageLabel;

	public AddRepositoryDialog(Shell parentShell,
			Set<RepositoryWrapper> availableRepositories,
			Set<RepositoryWrapper> includedRepositories, IMaven maven, ArtifactKey artifactKey) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.RESIZE
				| getDefaultOrientation());
		this.availableRepositories = availableRepositories;
		this.includedRepositories = includedRepositories;
		this.maven = maven;
		this.artifactKey = artifactKey;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(ADD_MAVEN_REPOSITORY_TITLE);
		setTitle(ADD_MAVEN_REPOSITORY_TITLE);
		setMessage("Enter a new repository");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		gd.heightHint = 300;
		gd.widthHint = 500;
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout(1, false));
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Group profileGroup = new Group(contents, SWT.NONE);
		profileGroup.setText("Profile");
		profileGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		profileGroup.setLayout(new GridLayout(3, false));

		createLabel(profileGroup, "Profile ID:");

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
		profileComboDecoration = addDecoration(profileCombo, FieldDecorationRegistry.DEC_REQUIRED, PROFILE_ID_IS_REQUIRED);
		
		activeByDefaultButton = new Button(profileGroup, SWT.CHECK);
		activeByDefaultButton.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, false, false));
		activeByDefaultButton.setText("Active by default");

		profileCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Settings settings;
				String id = profileCombo.getText();
				if (id == null || id.trim().isEmpty()) {
					return;
				}
				for (RepositoryWrapper wrapper:availableRepositories) {
					if (wrapper.getProfileId() != null && wrapper.getRepository() != null && id.equals(wrapper.getProfileId())) {
						updateRepository(wrapper.getRepository());
						activeByDefaultButton.setSelection(true);
						return;
					}
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
						}
						break;
					}
				} catch (CoreException e1) {
					Activator.log(e1);
				}
				
			}

		});
		
		Group repositoryGroup = new Group(contents, SWT.NONE);
		repositoryGroup.setText("Repository");
		repositoryGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		repositoryGroup.setLayout(new GridLayout(2, false));
		
		createLabel(repositoryGroup, "ID:");
		idText = createText(repositoryGroup);
		idTextDecoration = addDecoration(idText, FieldDecorationRegistry.DEC_REQUIRED, REPOSITORY_ID_IS_REQUIRED);
		
		createLabel(repositoryGroup, "Name:");
		nameText = createText(repositoryGroup);
		nameTextDecoration = addDecoration(nameText, FieldDecorationRegistry.DEC_WARNING, REPOSITORY_NAME_IS_EMPTY);
		
		createLabel(repositoryGroup, "URL:");
		urlText = createText(repositoryGroup);
		urlTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_REQUIRED, REPOSITORY_URL_IS_REQUIRED);
		urlValidTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_ERROR, URL_IS_NOT_VALID);
		urlExistsTextDecoration = addDecoration(urlText, FieldDecorationRegistry.DEC_ERROR, URL_ALREADY_EXISTS);
		Button recognizeButton = new Button(contents, SWT.PUSH);
		recognizeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
		recognizeButton.setText("Recognize JBoss Maven Enterprise Repositories...");
		recognizeButton.setImage(getJBossImage());
		
		recognizeButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select the directory in which to search for JBoss Maven Enterprise Repositories:");
				directoryDialog.setText("Search for JBoss Maven Enterprise Repositories");

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
				monitor.beginTask("Searching...", 110);
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
						MessageDialog.openInformation(getShell(), "Information", "No new repository found.");
						return;
					} else {
						boolean ok = MessageDialog.openQuestion(getShell(), "Confirm Add Repository", "No new repository found. Would you like me to add the '" + url + "' repository.");
						if (ok) {
							Repository repository = ConfigureMavenRepositoriesWizardPage.getDefaultRepository();
							repository.setId(getUniqueId(new File(pathStr), "id", allRepositories));
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
		
		if (artifactKey != null) {
			String message = "The '" + getCoords() + "' artifact";
			final String unresolvedMessage = message + " is not resolved.";
			final String resolvedMessage = message + " is resolved.";
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
					Job job = new Job("Resolving artifact ...") {

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
		
		return area;
	}

	private String getCoords() {
		if (coords == null && artifactKey != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(artifactKey.getGroupId());
			builder.append(":");
			builder.append(artifactKey.getArtifactId());
			builder.append(":");
			if (artifactKey.getClassifier() != null) {
				builder.append(artifactKey.getClassifier());
				builder.append(":");
			}
			builder.append(artifactKey.getVersion());
			coords = builder.toString();
		}
		return coords;
	}

	private boolean resolveArtifact(String id, String url) {
		
		org.sonatype.aether.RepositorySystem system;
		try {
			system = new DefaultPlexusContainer()
					.lookup(org.sonatype.aether.RepositorySystem.class);
		} catch (Exception e) {
			Activator.log(e);
			return false;
		}
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		IMaven maven = MavenPlugin.getMaven();
		String localRepoHome = maven.getLocalRepositoryPath();
		LocalRepository localRepo = new LocalRepository(localRepoHome);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(new DefaultArtifact(getCoords()));

		RemoteRepository centralRepo = new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		artifactRequest.addRepository(centralRepo);
		RemoteRepository remoteRepo = new RemoteRepository(id,
				"default", url); //$NON-NLS-1$
		artifactRequest.addRepository(remoteRepo);
		for (RepositoryWrapper wrapper : includedRepositories) {
			Repository repo = wrapper.getRepository();
			if (repo == null) {
				continue;
			}
			remoteRepo = new RemoteRepository(repo.getId(),
					"default", repo.getUrl()); //$NON-NLS-1$
			artifactRequest.addRepository(remoteRepo);
		}

		try {
			ArtifactResult artifactResult = system.resolveArtifact(session,
					artifactRequest);
			Artifact artifact = artifactResult.getArtifact();
			return artifact != null;
		} catch (ArtifactResolutionException e) {
			if (Activator.getDefault().isDebugging()) {
				Activator.log(e);
			}
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
		getButton(IDialogConstants.OK_ID).setEnabled(true);
		setMessage(null);
		if (profileCombo.getText().trim().isEmpty()) {
			setMessage(PROFILE_ID_IS_REQUIRED, IMessageProvider.ERROR);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			showDecoration();
			return;
		}
		if (idText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_ID_IS_REQUIRED, IMessageProvider.ERROR);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			showDecoration();
			return;
		}
		if (urlText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_URL_IS_REQUIRED, IMessageProvider.ERROR);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			showDecoration();
			return;
		}
		String urlString;
		try {
			urlString = new URL(urlText.getText().trim()).toString();
		} catch (MalformedURLException e) {
			setMessage(URL_IS_NOT_VALID, IMessageProvider.ERROR);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			showDecoration();
			return;
		}
		if (!urlString.endsWith(RepositoryWrapper.SEPARATOR)) {
			urlString = urlString + RepositoryWrapper.SEPARATOR;
		}
		for (RepositoryWrapper wrapper:includedRepositories) {
			if (urlString.equals(wrapper.getRepository().getUrl())) {
				setMessage(URL_ALREADY_EXISTS, IMessageProvider.ERROR);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				showDecoration();
				return;
			}
		}
		if (nameText.getText().trim().isEmpty()) {
			setMessage(REPOSITORY_NAME_IS_EMPTY, IMessageProvider.WARNING);
			showDecoration();
			return;
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
					if (urlString.equals(wrapper.getRepository().getUrl())) {
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
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false,
				false));
		label.setText(text);
		return label;
	}

	private String[] getProfileIds() {
		Set<String> ids = new TreeSet<String>();
		ids.add(EMPTY_STRING);
		for (RepositoryWrapper wrapper:availableRepositories) {
			if (wrapper.getProfileId() != null && !wrapper.getProfileId().isEmpty()) {
				ids.add(wrapper.getProfileId());
			}
		}
//		Settings settings;
//		try {
//			settings = maven.getSettings();
//		} catch (CoreException e) {
//			return ids.toArray(new String[0]);
//		}
//		for (Profile profile : settings.getProfiles()) {
//			if (profile.getId() != null) {
//				ids.add(profile.getId());
//			}
//		}
//		for (RepositoryWrapper wrapper:availableRepositories) {
//			if (wrapper.getProfileId() != null && !wrapper.getProfileId().isEmpty()) {
//				ids.add(wrapper.getProfileId());
//			}
//		}
		return ids.toArray(new String[0]);
	}

	protected void updateRepository(Repository repository) {
		idText.setText(repository.getId());
		nameText.setText(repository.getName());
		urlText.setText(repository.getUrl());
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
		monitor.setTaskName("Searching " + directory.getAbsolutePath());
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
					if (pathname != null && pathname.getName() != null && pathname.getName().contains("redhat")) {
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
			repository.setName("JBoss WFK Maven Repository");
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
			String globalSettings = MavenPlugin.getMavenRuntimeManager()
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
		Repository repository = ConfigureMavenRepositoriesWizardPage.getDefaultRepository();
		repository.setId(idText.getText().trim());
		repository.setName(nameText.getText().trim());
		repository.setUrl(urlText.getText().trim());
		repositoryWrapper = new RepositoryWrapper(repository, profileCombo.getText().trim());
		activeByDefault = activeByDefaultButton.getSelection();
		super.okPressed();
	}

	public boolean isActiveByDefault() {
		return activeByDefault;
	}
}
