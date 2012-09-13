package org.jboss.tools.project.examples.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.model.ProjectExample;

public class NewProjectExamplesLocationPage extends WizardPage {

	private final static String RESOURCE= "org.eclipse.ui.resourceWorkingSetPage"; //$NON-NLS-1$
	private final static String JAVA= "org.eclipse.jdt.ui.JavaWorkingSetPage"; //$NON-NLS-1$
	private final static String OTHERS= "org.eclipse.jdt.internal.ui.OthersWorkingSet"; //$NON-NLS-1$
	
	private Text outputDirectoryText;
	private Button isWorkspace;
	private WorkingSetGroup fWorkingSetGroup;
	
	protected NewProjectExamplesLocationPage() {
		super("org.jboss.tools.project.examples.location"); //$NON-NLS-1$
        setTitle( "Location" );
        setDescription( "Project Example Location and Workspace" );
        fWorkingSetGroup= new WorkingSetGroup();
        setWorkingSets(new IWorkingSet[0]);
	}

	public void setWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null) {
			throw new IllegalArgumentException();
		}
		fWorkingSetGroup.setWorkingSets(workingSets);
	}
	
	public IWorkingSet[] getWorkingSets() {
		return fWorkingSetGroup.getSelectedWorkingSets();
	}
	
	protected Control createWorkingSetControl(Composite composite) {
		return fWorkingSetGroup.createControl(composite);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1,false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);
		Dialog.applyDialogFont(composite);
		setControl(composite);

		Group outputDirectoryGroup = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		outputDirectoryGroup.setLayout(layout);
		outputDirectoryGroup.setText(Messages.ProjectExamplesPreferencePage_Output_directory);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		outputDirectoryGroup.setLayoutData(gd);
		
		isWorkspace = new Button(outputDirectoryGroup, SWT.CHECK);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		isWorkspace.setLayoutData(gd);
		isWorkspace.setText(Messages.ProjectExamplesPreferencePage_Use_default_workspace_location);
		isWorkspace.setSelection(ProjectExamplesActivator.getDefault().getPreferenceStore().getBoolean(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT));
		
		outputDirectoryText = new Text(outputDirectoryGroup, SWT.SINGLE|SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.verticalAlignment = SWT.CENTER;
		outputDirectoryText.setLayoutData(gd);
		IPreferenceStore store = ProjectExamplesActivator.getDefault().getPreferenceStore();
		String outputDirectoryValue = store.getString(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY);
		if (outputDirectoryValue == null || outputDirectoryValue.isEmpty()) {
			final IPath path= Platform.getLocation();
			outputDirectoryValue = path.toOSString();
		}
		outputDirectoryText.setText(outputDirectoryValue == null ? "" : outputDirectoryValue); //$NON-NLS-1$
		final Button outputDirectoryBrowse = new Button(outputDirectoryGroup, SWT.PUSH);
		outputDirectoryBrowse.setText(Messages.Browse);
		outputDirectoryBrowse.addSelectionListener(new SelectionAdapter(){
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SINGLE);
				String value = outputDirectoryText.getText();
				if (value.trim().length() == 0) {
					value = Platform.getLocation().toOSString();
				}
				dialog.setFilterPath(value);
			
				String result = dialog.open();
				if (result == null || result.trim().length() == 0) {
					return;
				}
				outputDirectoryText.setText(result);
				
			}
		
		});
		enableControls(outputDirectoryBrowse);
		
		isWorkspace.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				enableControls(outputDirectoryBrowse);
				if (!isWorkspace.getSelection()) {
					String location = outputDirectoryText.getText().trim();
					if (!validateLocation(location)) {
						return;
					}
				}
				setPageComplete(true);
				setErrorMessage(null);
				setMessage(null);
				ProjectExamplesActivator.getDefault().getPreferenceStore().setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_DEFAULT, isWorkspace.getSelection());
			}
			
		});
		
		outputDirectoryText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!isWorkspace.getSelection()) {
					String location = outputDirectoryText.getText().trim();
					if (!validateLocation(location)) {
						return;
					}
				}
				ProjectExamplesActivator.getDefault().getPreferenceStore().setValue(ProjectExamplesActivator.PROJECT_EXAMPLES_OUTPUT_DIRECTORY, outputDirectoryText.getText());
				setPageComplete(true);
				setErrorMessage(null);
				setMessage(null);
			}
		});
		Control workingSetControl= createWorkingSetControl(composite);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setPageComplete(true);
	}
	
	private boolean canCreate(File file) {
		while (!file.exists()) {
			file= file.getParentFile();
			if (file == null)
				return false;
		}
		return file.canWrite();
	}

	public void init(IStructuredSelection selection, IWorkbenchPart activePart) {
		setWorkingSets(getSelectedWorkingSet(selection, activePart));
	}
	
	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection, IWorkbenchPart activePart) {
		IWorkingSet[] selected = getSelectedWorkingSet(selection);
		if (selected != null && selected.length > 0) {
			for (int i= 0; i < selected.length; i++) {
				if (!isValidWorkingSet(selected[i]))
					return EMPTY_WORKING_SET_ARRAY;
			}
			return selected;
		}

		if (!(activePart instanceof PackageExplorerPart))
			return EMPTY_WORKING_SET_ARRAY;

		PackageExplorerPart explorerPart= (PackageExplorerPart) activePart;
		if (explorerPart.getRootMode() == PackageExplorerPart.PROJECTS_AS_ROOTS) {
			//Get active filter
			IWorkingSet filterWorkingSet= explorerPart.getFilterWorkingSet();
			if (filterWorkingSet == null)
				return EMPTY_WORKING_SET_ARRAY;

			if (!isValidWorkingSet(filterWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			return new IWorkingSet[] {filterWorkingSet};
		} else {
			//If we have been gone into a working set return the working set
			Object input= explorerPart.getViewPartInput();
			if (!(input instanceof IWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			IWorkingSet workingSet= (IWorkingSet)input;
			if (!isValidWorkingSet(workingSet))
				return EMPTY_WORKING_SET_ARRAY;

			return new IWorkingSet[] {workingSet};
		}
	}
	
	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection))
			return EMPTY_WORKING_SET_ARRAY;

		ITreeSelection treeSelection= (ITreeSelection) selection;
		if (treeSelection.isEmpty())
			return EMPTY_WORKING_SET_ARRAY;

		List<?> elements= treeSelection.toList();
		if (elements.size() == 1) {
			Object element= elements.get(0);
			TreePath[] paths= treeSelection.getPathsFor(element);
			if (paths.length != 1)
				return EMPTY_WORKING_SET_ARRAY;

			TreePath path= paths[0];
			if (path.getSegmentCount() == 0)
				return EMPTY_WORKING_SET_ARRAY;

			Object candidate= path.getSegment(0);
			if (!(candidate instanceof IWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			IWorkingSet workingSetCandidate= (IWorkingSet) candidate;
			if (isValidWorkingSet(workingSetCandidate))
				return new IWorkingSet[] { workingSetCandidate };

			return EMPTY_WORKING_SET_ARRAY;
		}

		ArrayList<IWorkingSet> result= new ArrayList<IWorkingSet>();
		for (Iterator<?> iterator= elements.iterator(); iterator.hasNext();) {
			Object element= iterator.next();
			if (element instanceof IWorkingSet && isValidWorkingSet((IWorkingSet) element)) {
				result.add((IWorkingSet) element);
			}
		}
		return result.toArray(new IWorkingSet[result.size()]);
	}


	private static boolean isValidWorkingSet(IWorkingSet workingSet) {
		String id= workingSet.getId();
		if (!JAVA.equals(id) && !RESOURCE.equals(id))
			return false;

		if (workingSet.isAggregateWorkingSet())
			return false;

		return true;
	}
	
	protected void enableControls(Button outputDirectoryBrowse) {
		outputDirectoryText.setEnabled(!isWorkspace.getSelection());
		outputDirectoryBrowse.setEnabled(!isWorkspace.getSelection());
	}
	
	private final class WorkingSetGroup {

		private WorkingSetConfigurationBlock fWorkingSetBlock;

		public WorkingSetGroup() {
			String[] workingSetIds= new String[] { JAVA, RESOURCE };
			fWorkingSetBlock= new WorkingSetConfigurationBlock(workingSetIds, ProjectExamplesActivator.getDefault().getDialogSettings());
			//fWorkingSetBlock.setDialogMessage(NewWizardMessages.NewJavaProjectWizardPageOne_WorkingSetSelection_message);
		}

		public Control createControl(Composite composite) {
			Group workingSetGroup= new Group(composite, SWT.NONE);
			workingSetGroup.setFont(composite.getFont());
			workingSetGroup.setText("Working sets");
			workingSetGroup.setLayout(new GridLayout(1, false));

			fWorkingSetBlock.createContent(workingSetGroup);

			return workingSetGroup;
		}


		public void setWorkingSets(IWorkingSet[] workingSets) {
			fWorkingSetBlock.setWorkingSets(workingSets);
		}

		public IWorkingSet[] getSelectedWorkingSets() {
			return fWorkingSetBlock.getSelectedWorkingSets();
		}
	}

	@Override
	public IWizardPage getNextPage() {
		IWizard wizard = getWizard();
		if (wizard instanceof NewProjectExamplesWizard2) {
			ProjectExample projectExample = ((NewProjectExamplesWizard2)wizard).getSelectedProjectExample();
			if (projectExample != null && projectExample.getImportType() != null) {
				List<IProjectExamplesWizardPage> pages = ((NewProjectExamplesWizard2)wizard).getContributedPages("extra");
				for (IProjectExamplesWizardPage page:pages) {
					if (projectExample.getImportType().equals(page.getProjectExampleType())) {
						return page;
					}
				}
			} 
			//return ((NewProjectExamplesWizard2)wizard).getReadyPage();
		}
		return null;
	}

	private boolean validateLocation(String location) {
		if (location.length() == 0) {
			setErrorMessage(null);
			setMessage("Enter a location for the project");
			setPageComplete(false);
			return false;
		}
		// check whether the location is a syntactically correct path
		if (!Path.EMPTY.isValidPath(location)) {
			setErrorMessage("Invalid project contents directory");
			setPageComplete(false);
			return false;
		}
		IPath projectPath = Path.fromOSString(location);
		if (!projectPath.toFile().exists()) {
			// check non-existing external location
			if (!canCreate(projectPath.toFile())) {
				setErrorMessage("Cannot create project content at the given external location.");
				setPageComplete(false);
				return false;
			}
		}
		return true;
	}

}
