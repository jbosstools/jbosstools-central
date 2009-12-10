package org.jboss.tools.maven.ui.internal.libprov;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
import org.eclipse.jst.common.project.facet.ui.libprov.LibraryProviderOperationPanel;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.jboss.tools.maven.core.MavenCoreActivator;
import org.jboss.tools.maven.core.libprov.MavenLibraryProviderInstallOperationConfig;
import org.jboss.tools.maven.ui.Messages;
import org.maven.ide.components.pom.Dependency;
import org.maven.ide.components.pom.Exclusion;
import org.maven.ide.components.pom.Model;
import org.maven.ide.components.pom.PomFactory;
import org.maven.ide.components.pom.util.PomResourceImpl;

public class MavenUserLibraryProviderInstallPanel extends
		LibraryProviderOperationPanel {

	private TableViewer dependencyViewer;
	protected ColumnLayoutData[] dependencyTableColumnLayouts= {
			new ColumnWeightData(80,80),
			new ColumnWeightData(80,80),
			new ColumnWeightData(60,60),
			new ColumnWeightData(60,60),
			new ColumnWeightData(60,60)
		};
	protected ColumnLayoutData[] exclusionTableColumnLayouts= {
			new ColumnWeightData(80,80),
			new ColumnWeightData(60,60)
		};
	private List<Dependency> dependencies;
	private TableViewer exclusionViewer;
	private List exclusions;
	private PomResourceImpl resource;
	
	@Override
	public Control createControl(final Composite parent) {
		resource = MavenCoreActivator.getResource();
		if (resource != null) {
			resource.unload();
			MavenCoreActivator.setResource(null);
		}
		MavenLibraryProviderInstallOperationConfig config = (MavenLibraryProviderInstallOperationConfig) getOperationConfig();
		ILibraryProvider provider = config.getLibraryProvider();
		File providerFile = MavenCoreActivator.getProviderFile(provider);
		URL url = null;
		try {
			if (providerFile.exists()) {
				url = providerFile.toURL();
			} else {
				Map<String, String> params = provider.getParams();
				String pomURLString = params.get("template"); //$NON-NLS-1$
				URL platformURL = new URL(pomURLString);
				url = FileLocator.resolve(platformURL);
			}
		
			resource = MavenCoreActivator.loadResource(url);
			MavenCoreActivator.setResource(resource);
			Model model = resource.getModel();
			dependencies = model.getDependencies();
			config.setModel(model);
			parent.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					parent.removeDisposeListener(this);
					resource.unload();
					MavenCoreActivator.setResource(null);
				}
			});
		} catch (CoreException e) {
			MavenCoreActivator.log(e);
		} catch (IOException e) {
			MavenCoreActivator.log(e);
		}
		
		if (dependencies == null) {
			dependencies = new ArrayList<Dependency>();
		}
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label dependencyLabel = new Label(composite, SWT.NONE);
		dependencyLabel.setText(Messages.MavenUserLibraryProviderInstallPanel_Dependencies);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan=2;
		dependencyLabel.setLayoutData(gd);
		
		createDependencyViewer(composite);
		dependencyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof Dependency) {
						Dependency dependency = (Dependency) object;
						exclusions = dependency.getExclusions();
						exclusionViewer.setInput(exclusions);
					}
				}
			}
		});
		
		Label exclusionLabel = new Label(composite, SWT.NONE);
		exclusionLabel.setText(Messages.MavenUserLibraryProviderInstallPanel_Exclusions);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan=2;
		exclusionLabel.setLayoutData(gd);
		
		createExclusionViewer(composite);
		
		if (dependencies.size() > 0) {
			dependencyViewer.getTable().select(0);
			exclusions = dependencies.get(0).getExclusions();
			exclusionViewer.setInput(exclusions);
		}
		return composite;
	}

	private void createExclusionViewer(Composite parent) {
		exclusionViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 60;
		exclusionViewer.getTable().setLayoutData(gd);
		//viewer.setLabelProvider(new TableLabelProvider());
		exclusionViewer.setContentProvider(new TableContentProvider());
		Table table = exclusionViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		
		String[] columnHeaders = {Messages.MavenUserLibraryProviderInstallPanel_GroupId,Messages.MavenUserLibraryProviderInstallPanel_ArtifactId};

		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn column = new TableViewerColumn(exclusionViewer, SWT.NONE);
			column.setLabelProvider(new ExclusionColumnLabelProvider(i));
			column.getColumn().setText(columnHeaders[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			column.setEditingSupport(new ExclusionEditingSupport(exclusionViewer, i));

		}
		
		
		TableLayout layout = new AutoResizeTableLayout(table);
		for (int i = 0; i < exclusionTableColumnLayouts.length; i++) {
			layout.addColumnData(exclusionTableColumnLayouts[i]);
		}
		exclusionViewer.getTable().setLayout(layout);
		
		createExclusionButtons(parent);

	}

	private void createDependencyViewer(Composite parent) {
		dependencyViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 80;
		dependencyViewer.getTable().setLayoutData(gd);
		//viewer.setLabelProvider(new TableLabelProvider());
		dependencyViewer.setContentProvider(new TableContentProvider());
		Table table = dependencyViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		
		String[] columnHeaders = {Messages.MavenUserLibraryProviderInstallPanel_GroupId,Messages.MavenUserLibraryProviderInstallPanel_ArtifactId,Messages.MavenUserLibraryProviderInstallPanel_Version,Messages.MavenUserLibraryProviderInstallPanel_Scope,Messages.MavenUserLibraryProviderInstallPanel_Type};
		
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn column = new TableViewerColumn(dependencyViewer, SWT.NONE);
			column.setLabelProvider(new DependencyColumnLabelProvider(i));
			column.getColumn().setText(columnHeaders[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			column.setEditingSupport(new DependencyEditingSupport(dependencyViewer, i));
		
		}
		
		
		TableLayout layout = new AutoResizeTableLayout(table);
		for (int i = 0; i < dependencyTableColumnLayouts.length; i++) {
			layout.addColumnData(dependencyTableColumnLayouts[i]);
		}
		
		dependencyViewer.getTable().setLayout(layout);
		dependencyViewer.setInput(dependencies);
		createDependencyButtons(parent);
	}

	private void createDependencyButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1,false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(Messages.MavenUserLibraryProviderInstallPanel_Add);
		addButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				Dependency dependency = PomFactory.eINSTANCE.createDependency();
				dependency.setGroupId("?"); //$NON-NLS-1$
				dependency.setArtifactId("?"); //$NON-NLS-1$
				dependencies.add(dependency);
				dependencyViewer.refresh();
			}
		
		});
		
		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(Messages.MavenUserLibraryProviderInstallPanel_Remove);
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = dependencyViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof Dependency) {
						Dependency dependency = (Dependency) object;
						boolean ok = MessageDialog.openQuestion(getShell(), Messages.MavenUserLibraryProviderInstallPanel_Remove_dependency, NLS.bind(Messages.MavenUserLibraryProviderInstallPanel_Are_you_sure_you_want_to_remove_the_artifact, dependency.getGroupId(), dependency.getArtifactId()));
						if (ok) {
							dependencies.remove(object);
							dependencyViewer.refresh();
						}
					}
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		final Button restoreDefaults = new Button(buttonComposite, SWT.PUSH);
		restoreDefaults.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		restoreDefaults.setText(Messages.MavenUserLibraryProviderInstallPanel_Restore_Defaults);
		
		restoreDefaults.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				resource.unload();
				MavenLibraryProviderInstallOperationConfig config = (MavenLibraryProviderInstallOperationConfig) getOperationConfig();
				ILibraryProvider provider = config.getLibraryProvider();
				Map<String, String> params = provider.getParams();
				String pomURLString = params.get("template"); //$NON-NLS-1$
				try {
					URL platformURL = new URL(pomURLString);
					URL url = FileLocator.resolve(platformURL);
					resource = MavenCoreActivator.loadResource(url);
					Model model = resource.getModel();
					dependencies = model.getDependencies();
					dependencyViewer.setInput(dependencies);
					config.setModel(model);
					dependencyViewer.refresh();
					if (dependencies.size() > 0) {
						dependencyViewer.getTable().select(0);
						exclusions = dependencies.get(0).getExclusions();
					} else {
						exclusions = null;
					}
					exclusionViewer.setInput(exclusions);
					exclusionViewer.refresh();
				} catch (MalformedURLException e1) {
					MavenCoreActivator.log(e1);
				} catch (IOException e1) {
					MavenCoreActivator.log(e1);
				} catch (CoreException e1) {
					MavenCoreActivator.log(e1);
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		dependencyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = dependencyViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					removeButton.setEnabled(object instanceof Dependency);
				} else {
					removeButton.setEnabled(false);
				}
			}
		});
	}

	protected Shell getShell() {
		// TODO Auto-generated method stub
		return null;
	}

	private void createExclusionButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(1,false));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(Messages.MavenUserLibraryProviderInstallPanel_Add);
		addButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				Exclusion exclusion = PomFactory.eINSTANCE.createExclusion();
				exclusion.setGroupId("?"); //$NON-NLS-1$
				exclusion.setArtifactId("?"); //$NON-NLS-1$
				if (exclusions == null) {
					ISelection sel = dependencyViewer.getSelection();
					if (sel instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) sel;
						Object object = selection.getFirstElement();
						if (object instanceof Dependency) {
							exclusions = ((Dependency) object).getExclusions();
							exclusionViewer.setInput(exclusions);
						}
					}
				}
				if (exclusions != null) {
					exclusions.add(exclusion);
				}
				exclusionViewer.refresh();
			}
		
		});
		
		final Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(Messages.MavenUserLibraryProviderInstallPanel_Remove);
		removeButton.setEnabled(false);
		
		removeButton.addSelectionListener(new SelectionListener(){
		
			public void widgetSelected(SelectionEvent e) {
				ISelection sel = exclusionViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					if (object instanceof Exclusion) {
						Exclusion exclusion = (Exclusion) object;
						boolean ok = MessageDialog.openQuestion(getShell(), Messages.MavenUserLibraryProviderInstallPanel_Remove_exclusion, NLS.bind(Messages.MavenUserLibraryProviderInstallPanel_Are_you_sure_you_want_to_remove_the_artifact, exclusion.getGroupId(), exclusion.getArtifactId())); 
						if (ok && exclusions != null) {
							exclusions.remove(object);
						}
					}
					exclusionViewer.refresh();
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		exclusionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = exclusionViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) sel;
					Object object = selection.getFirstElement();
					removeButton.setEnabled(object instanceof Exclusion);
				} else {
					removeButton.setEnabled(false);
				}
			}
		});
	}
	
	private static class TableContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Object[]) {
				return (Object[]) inputElement;
			}
			if (inputElement instanceof Collection) {
				return ((Collection) inputElement).toArray();
			}
			return new Object[0];
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}
	}

	private static class DependencyColumnLabelProvider extends ColumnLabelProvider  {

		private int columnIndex;

		public DependencyColumnLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public String getText(Object element) {
			if (element instanceof Dependency) {
				Dependency dependency = (Dependency) element;
				switch (columnIndex) {
				case 0:
					return dependency.getGroupId();
				case 1:
					return dependency.getArtifactId();
				case 2:
					return dependency.getVersion();
				case 3:
					return dependency.getScope();
				case 4:
					return dependency.getType();
				}
			}
			return null;
		}
	}
	
	private static class ExclusionColumnLabelProvider extends ColumnLabelProvider  {

		private int columnIndex;

		public ExclusionColumnLabelProvider(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		public String getText(Object element) {
			if (element instanceof Exclusion) {
				Exclusion exclusion = (Exclusion) element;
				switch (columnIndex) {
				case 0:
					return exclusion.getGroupId();
				case 1:
					return exclusion.getArtifactId();
				}
			}
			return null;
		}
	}
}
