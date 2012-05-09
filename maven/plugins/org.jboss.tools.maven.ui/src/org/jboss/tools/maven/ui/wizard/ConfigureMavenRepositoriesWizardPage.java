package org.jboss.tools.maven.ui.wizard;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.cli.MavenCli;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Activation;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Settings;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jboss.tools.maven.ui.Activator;

public class ConfigureMavenRepositoriesWizardPage extends WizardPage {

	private static final String SEPARATOR = "/"; //$NON-NLS-1$
	
	private static final String JSF_IMPL = "com" + File.separator + "sun" + File.separator + "faces" + File.separator + "jsf-impl";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String WFK_BOMS = "com" + File.separator + "redhat" + File.separator + "jboss" + File.separator + "wfk" + File.separator + "boms";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	private static final String PAGE_NAME = "org.jboss.tools.maven.ui.wizard.page"; //$NON-NLS-1$
	private static final String ADD_ALL = " Add All>> ";
	private static final String ADD = " Add>> ";
	private static final String REMOVE_ALL = " <<Remove All ";
	private static final String REMOVE = " <Remove ";

	private Button removeButton;
	private Button removeAllButton;
	private Button addButton;
	private Button addAllButton;
	private IMavenConfiguration mavenConfiguration;
	private IMaven maven;
	private Image jbossImage;
	private ListViewer includedRepositoriesViewer;
	private List<Repository> includedRepositories;
	private List<Repository> availableRepositories;
	private List<Repository> selectedIncludedRepositories = new ArrayList<Repository>();
	private List<Repository> selectedAvailableRepositories = new ArrayList<Repository>();
	private ListViewer availableRepositoriesViewer;

	private String localRepository;
	
	public ConfigureMavenRepositoriesWizardPage() {
		super(PAGE_NAME);
		setTitle("Configure Maven Repositories");
		mavenConfiguration = MavenPlugin.getMavenConfiguration();
		maven = MavenPlugin.getMaven();
		try {
			maven.reloadSettings();
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private String getLocalRepository() {
		if (localRepository == null) {
			String userSettings = getUserSettings();
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
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		Dialog.applyDialogFont(composite);
		setControl(composite);
		
		Label userSettingsLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		userSettingsLabel.setLayoutData(gd);
		
		String userSettings = getUserSettings();
	    userSettingsLabel.setText("User settings: " + userSettings);
	    
	    Group repositoriesGroup = new Group(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridLayout layout = new GridLayout(3, false);
        repositoriesGroup.setLayout(layout);
        repositoriesGroup.setLayoutData(gd);
        repositoriesGroup.setText("Repositories");
	
        Composite availableRepositoriesComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        availableRepositoriesComposite.setLayoutData(gd);
        availableRepositoriesComposite.setLayout(new GridLayout());
        
        Label availableRepositoriesLabel = new Label(availableRepositoriesComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        availableRepositoriesLabel.setLayoutData(gd);
        availableRepositoriesLabel.setText("Available Repositories:");
        
        availableRepositoriesViewer = new ListViewer(availableRepositoriesComposite, SWT.BORDER | SWT.MULTI |SWT.H_SCROLL|SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 300;
        gd.widthHint = 300;
        availableRepositoriesViewer.getList().setLayoutData(gd);
        availableRepositoriesViewer.setContentProvider(new ArrayContentProvider());
        availableRepositoriesViewer.setLabelProvider(new RepositoryLabelProvider());
        
        Composite buttonsComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonsComposite.setLayoutData(gd);
        buttonsComposite.setLayout(new GridLayout(1, false));
        
        Label buttonsLabel = new Label(buttonsComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonsLabel.setLayoutData(gd);
        
        Composite buttonsComp = new Composite(buttonsComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, false, true);
        buttonsComp.setLayoutData(gd);
        buttonsComp.setLayout(new GridLayout());
        
        GC gc = new GC(buttonsComp);
        int maxAddRemoveButtonsWidth = computeMaxAddRemoveButtonsWidth(gc);
        gc.dispose();
        
        removeButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, REMOVE);
        removeAllButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, REMOVE_ALL);
        addButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, ADD);
        addAllButton = createButton(buttonsComp, maxAddRemoveButtonsWidth, ADD_ALL);
   
        Composite includedRepositoriesComposite = new Composite(repositoriesGroup, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        includedRepositoriesComposite.setLayoutData(gd);
        includedRepositoriesComposite.setLayout(new GridLayout(1, false));
        
        Label includedRepositoriesLabel = new Label(includedRepositoriesComposite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        includedRepositoriesLabel.setLayoutData(gd);
        includedRepositoriesLabel.setText("Maven Repositories:");
	    
	    includedRepositoriesViewer = new ListViewer(includedRepositoriesComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL|SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 300;
        gd.widthHint = 300;
        
        includedRepositoriesViewer.getList().setLayoutData(gd);
        includedRepositoriesViewer.setContentProvider(new ArrayContentProvider());
        includedRepositoriesViewer.setLabelProvider(new RepositoryLabelProvider());
        
        
        Button recognizeButton = new Button(composite, SWT.PUSH);
		recognizeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));
		recognizeButton.setText("Recognize JBoss Maven Enterprise Repositories...");
		recognizeButton.setImage(getJBossImage());
		
		recognizeButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
				directoryDialog.setMessage("Select the directory in which to search for JBoss Maven Enterprise Repositories:");
				directoryDialog.setText("Search for JBoss Maven Enterprise Repositories");

				String pathStr = directoryDialog.open();
				if (pathStr == null)
					return;
				
				final IPath path = new Path(pathStr);
				
				final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.setBlockOnOpen(false);
				dialog.setCancelable(true);
				dialog.open();
				final IProgressMonitor monitor = dialog.getProgressMonitor();
				monitor.beginTask("Searching...", 110);
				final List<Repository> list = new ArrayList<Repository>();
				
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor2) {
						searchForRepositories(path, list, monitor2);
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
				List<Repository> newRepositories = new ArrayList<Repository>();
				if (list.size() > 0) {
					for (Repository repository:list) {
						String url = repository.getUrl();
						if (url == null) {
							continue;
						}
						url = changeUrl(url);
						boolean included = false;
						for (Repository rep:availableRepositories) {
							String url1 = rep.getUrl();
							if (url1 == null) {
								continue;
							}
							url1 = changeUrl(url1);
							if (url1.equals(url)) {
								included = true;
								break;
							}
						}
						for (Repository rep:includedRepositories) {
							String url1 = rep.getUrl();
							if (url1 == null) {
								continue;
							}
							url1 = changeUrl(url1);
							if (url1.equals(url)) {
								included = true;
								break;
							}
						}
						if (!included) {
							newRepositories.add(repository);
						}
					}
				}
				if (newRepositories.size() > 0) {
					availableRepositories.addAll(newRepositories);
					refreshRepositories();
				}
			}
		});
		
		includedRepositories = getIncludedRepositories();
		availableRepositories = getAvailableRepositories();
		List<Repository> remove = new ArrayList<Repository>();
		for (Repository availableRepository:availableRepositories) {
			String url = availableRepository.getUrl();
			if (url == null) {
				continue;
			}
			url = changeUrl(url);
			for (Repository includedRepository:includedRepositories) {
				String url1 = includedRepository.getUrl();
				if (url1 == null) {
					continue;
				}
				url1 = changeUrl(url1);
				if (url1.equals(url)) {
					remove.add(availableRepository);
				}
			}
		}
		for (Repository repository:remove) {
			availableRepositories.remove(repository);
		}
		
		availableRepositoriesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				selectedAvailableRepositories.clear();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Iterator iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object object = iterator.next();
						if (object instanceof Repository) {
							selectedAvailableRepositories.add((Repository) object);
						}
					}
				}
				configureButtons();
			}
		});
		includedRepositoriesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				selectedIncludedRepositories.clear();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Iterator iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object object = iterator.next();
						if (object instanceof Repository) {
							selectedIncludedRepositories.add((Repository) object);
						}
					}
				}
				configureButtons();
			}
		});
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				includedRepositories.removeAll(selectedIncludedRepositories);
				availableRepositories.addAll(selectedIncludedRepositories);
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		removeAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				availableRepositories.addAll(includedRepositories);
				includedRepositories.clear();
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				includedRepositories.addAll(selectedAvailableRepositories);
				availableRepositories.removeAll(selectedAvailableRepositories);
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		addAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				includedRepositories.addAll(availableRepositories);
				availableRepositories.clear();
				setPageComplete(true);
				refreshRepositories();
			}
		
        });
		refreshRepositories();
		setPageComplete(false);
	}

	private void searchForRepositories(IPath path,
			List<Repository> list, IProgressMonitor monitor) {
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
					searchDir(list, files[i], 4, monitor);
				}
				monitor.worked(work);
			}
			monitor.worked(workLeft);
		} else
			monitor.worked(100);
	}
	
	private void searchDir(List<Repository> list, File directory, int depth,
			IProgressMonitor monitor) {
		
		String localRepository = getLocalRepository();
		if (localRepository != null && localRepository.trim().equals(directory.getAbsolutePath())) {
			return;
		}
		monitor.setTaskName("Searching " + directory.getAbsolutePath());
		File comFile = new File(directory, "com");
		if (comFile.isDirectory()) { //$NON-NLS-1$
			Repository repository = getRepositoryFromDir(directory, list, monitor);
			if (repository != null) {
				list.add(repository);
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
				searchDir(list, files[i], depth - 1, monitor);
			}
		}
	}

	private Repository getRepositoryFromDir(File directory,List<Repository> repositories, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return null;
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
				Repository repository = getDefaultRepository();
				String id = "jboss-eap-maven-repository";
				List<Repository> allRepositories = new ArrayList<Repository>();
				allRepositories.addAll(repositories);
				allRepositories.addAll(includedRepositories);
				allRepositories.addAll(availableRepositories);
				repository.setId(getUniqueId(id, allRepositories));
				repository.setName("JBoss EAP Maven Repository");
				try {
					repository.setUrl(directory.toURI().toURL().toString());
				} catch (MalformedURLException e) {
					Activator.log(e);
				}
				return repository;
			}
		}
		file = new File(directory, WFK_BOMS);
		if (file.isDirectory()) {
			// JBoss WFK Maven Repository
			Repository repository = getDefaultRepository();
			repository.setId("jboss-wfk-maven-repository");
			repository.setName("JBoss WFK Maven Repository");
			try {
				repository.setUrl(directory.toURI().toURL().toString());
			} catch (MalformedURLException e) {
				Activator.log(e);
			}
			return repository;
		}
		return null;
	}

	private String getUniqueId(String id, List<Repository> allRepositories) {
		int i = 0;
		String startId = id;
		while (true) {
			boolean found = false;
			for (Repository repository:allRepositories) {
				if (id.equals(repository.getId())) {
					id = startId + "." + i++;
					found = true;
					break;
				}
			}
			if (!found) {
				return id;
			}
		}
	}

	private String getUserSettings() {
		String userSettings = mavenConfiguration.getUserSettingsFile();
	    if(userSettings == null || userSettings.length() == 0) {
	    	userSettings = MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath();
	    }
		return userSettings;
	}
	
	private void configureButtons() {
		removeButton.setEnabled(selectedIncludedRepositories.size() > 0);
		removeAllButton.setEnabled(includedRepositories.size() > 0);
		addButton.setEnabled(selectedAvailableRepositories.size() > 0);
		addAllButton.setEnabled(availableRepositories.size() > 0);
	}

	private void refreshRepositories() {
		includedRepositoriesViewer.setInput(includedRepositories.toArray(new Repository[0]));
        availableRepositoriesViewer.setInput(availableRepositories.toArray(new Repository[0]));
		
        selectedIncludedRepositories.clear();
        selectedAvailableRepositories.clear();
        includedRepositoriesViewer.setSelection(new StructuredSelection(selectedIncludedRepositories.toArray(new Repository[0])));
        availableRepositoriesViewer.setSelection(new StructuredSelection(selectedAvailableRepositories.toArray(new Repository[0])));
		configureButtons();
	}
	
	private String changeUrl(String url) {
		url = url.trim();
		if (!url.endsWith(SEPARATOR)) {
			url = url + SEPARATOR;
		}
		return url;
	}

	private List<Repository> getIncludedRepositories() {
		List<Repository> repositories = new ArrayList<Repository>();
        try {
			List<Profile> activeProfiles = getActiveProfiles();
			for (Profile profile:activeProfiles) {
				repositories.addAll(profile.getRepositories());
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
        return repositories;
	}
	
	private List<Repository> getAvailableRepositories() {
		List<Repository> repositories = new ArrayList<Repository>();
        
		Repository repository = getDefaultRepository();
		
		repository.setId("jboss-public-repository");
		repository.setName("JBoss Public");
		repository.setUrl("https://repository.jboss.org/nexus/content/groups/public-jboss/");
		
		repositories.add(repository);
		
		repository = getDefaultRepository();
        repository.setId("java-net-public");
		repository.setName("Java Net Public");
		repository.setUrl("https://maven.java.net/content/groups/public/");
		
		repositories.add(repository);
		
		repository = getDefaultRepository();
        repository.setId("com.springsource.repository.bundles.release");
		repository.setName("EBR Spring Release");
		repository.setUrl("http://repository.springsource.com/maven/bundles/release/");
		
		repositories.add(repository);
		
		repository = getDefaultRepository();
        repository.setId("com.springsource.repository.bundles.external");
		repository.setName("EBR External Release");
		repository.setUrl("http://repository.springsource.com/maven/bundles/external/");
		
		repositories.add(repository);

		repository = getDefaultRepository();
        repository.setId("repository.apache.org");
		repository.setName("Apache Repository");
		repository.setUrl("https://repository.apache.org/content/groups/public/");
		
		repositories.add(repository);
		
		return repositories;
	}
	
	private Repository getDefaultRepository() {
		Repository repository = new Repository();
		repository.setLayout("default"); //$NON-NLS-1$
		RepositoryPolicy releases = new RepositoryPolicy();
		releases.setEnabled(true);
		releases.setUpdatePolicy("never"); //$NON-NLS-1$
		repository.setReleases(releases);
		RepositoryPolicy snapshots = new RepositoryPolicy();
		snapshots.setEnabled(false);
		snapshots.setUpdatePolicy("never"); //$NON-NLS-1$
		repository.setSnapshots(snapshots);
		return repository;
	}
	
	private Image getJBossImage() {
		if (jbossImage == null) {
			ImageDescriptor desc = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/jboss.png"); //$NON-NLS-1$
			jbossImage = desc.createImage();
		}
		return jbossImage;
	}

	private List<Profile> getActiveProfiles() throws CoreException {
		Settings settings = maven.getSettings();
		List<String> activeProfilesIds = settings.getActiveProfiles();
		List<Profile> activeProfiles = new ArrayList<Profile>();
		for (Profile profile : settings.getProfiles()) {
			if ((profile.getActivation() != null && profile.getActivation().isActiveByDefault())
					|| activeProfilesIds.contains(profile.getId())) {
				activeProfiles.add(profile);
			}
		}
		return activeProfiles;
	}
	
	private Button createButton(Composite buttonsComp,
			int maxAddRemoveButtonsWidth, String text) {
		GridData gd;
		Button button = new Button(buttonsComp, SWT.NONE | SWT.LEFT);
        gd = new GridData();
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        gd.widthHint = maxAddRemoveButtonsWidth;
        button.setLayoutData(gd);
        button.setText(text);
        return button;
	}
	
	private int computeMaxAddRemoveButtonsWidth(GC gc) {
		int maxWidth = 0;

		maxWidth = getGreaterWidth(gc,REMOVE, maxWidth);
		maxWidth = getGreaterWidth(gc,REMOVE_ALL, maxWidth);
		maxWidth = getGreaterWidth(gc,ADD, maxWidth);
		maxWidth = getGreaterWidth(gc,ADD_ALL, maxWidth);
		
		return maxWidth;
	}
	
	private int getGreaterWidth(GC gc, String str, int compareWidth) {
		int greaterWidth = compareWidth;

		Point strExtentPoint = gc.stringExtent(str);
		int strWidth = strExtentPoint.x;
		if (strWidth > compareWidth) {
			greaterWidth = strWidth;
		}

		return greaterWidth + 5;
	}
	
	@Override
	public void dispose() {
		if (jbossImage != null) {
			jbossImage.dispose();
		}
		super.dispose();
	}

	class RepositoryLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
	        return null;
	      }

	      public String getText(Object element) {
	    	  if (element instanceof Repository) {
	    		  Repository repository = (Repository) element;
	    		  String name = repository.getName() == null ? "<no-name>" : repository.getName(); //$NON-NLS-1$
	    		  return name + "-" + repository.getUrl(); //$NON-NLS-1$
	    	  }
	        return null;
	      }
	}

	public void finishPage() {
		try {
			List<Profile> profiles;
			profiles = getActiveProfiles();
			Profile profile;
			Settings settings = maven.getSettings();
			if (profiles.size() <= 0) {
				profile = new Profile();
				profile.setId("jbosstools-maven-profile");
				Activation activation = new Activation();
				activation.setActiveByDefault(true);
				profile.setActivation(activation);
				settings.addProfile(profile);
			} else {
				profile = profiles.get(0);
			}
			profile.setRepositories(includedRepositories);
			profile.setPluginRepositories(includedRepositories);
			String userSettings = getUserSettings();
			File file = new File(userSettings);
			OutputStream out = new FileOutputStream(file);
			maven.writeSettings(settings, out);
			maven.reloadSettings();
		} catch (Exception e) {
			// FIXME
			Activator.log(e);
		}
		
	}

}
