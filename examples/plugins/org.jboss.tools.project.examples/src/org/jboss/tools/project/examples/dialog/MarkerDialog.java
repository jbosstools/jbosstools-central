/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.project.examples.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.jboss.tools.project.examples.Messages;
import org.jboss.tools.project.examples.ProjectExamplesActivator;
import org.jboss.tools.project.examples.dialog.xpl.QuickFixWizard;
import org.jboss.tools.project.examples.model.Project;

/**
 * @author snjeza
 * 
 */
public class MarkerDialog extends TitleAreaDialog {

	private static final String QUICK_FIX = Messages.MarkerDialog_Quick_Fix;
	private static final IMarkerResolution[] EMPTY_ARRAY = new IMarkerResolution[0];
	private List<Project> projects;
	private Image _dlgTitleImage;
	private Button quickFixButton;
	private Button finishButton;
	private TableViewer tableViewer;
	private IResourceChangeListener resourceChangeListener;

	private class QuickFixWizardDialog extends WizardDialog {

		/**
		 * @param parentShell
		 * @param newWizard
		 */
		public QuickFixWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
					| SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());
		}

	}

	public MarkerDialog(Shell parentShell, List<Project> projects) {
		super(parentShell);
		this.projects = projects;
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
				| SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());
		_dlgTitleImage = IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG)
				.createImage();
		setTitleImage(_dlgTitleImage);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(area, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 300;
		contents.setLayoutData(gd);
		contents.setLayout(new GridLayout());
		setTitle(QUICK_FIX);
		setMessage(Messages.MarkerDialog_Select_a_marker_and_click_the_Quick_Fix_button);
		getShell().setText(QUICK_FIX);
		applyDialogFont(contents);
		initializeDialogUnits(area);

		Label markersLabel = new Label(contents, SWT.NULL);
		markersLabel.setText(Messages.MarkerDialog_Markers);
		tableViewer = new TableViewer(contents, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.SINGLE);
		Table table = tableViewer.getTable();
		gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnNames = new String[] { Messages.MarkerDialog_Description, Messages.MarkerDialog_Resource, Messages.MarkerDialog_Type };
		int[] columnWidths = new int[] { 200, 150, 150 };

		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText(columnNames[i]);
			tc.setWidth(columnWidths[i]);
		}

		tableViewer.setLabelProvider(new MarkerLabelProvider());
		tableViewer.setContentProvider(new MarkerContentProvider(projects));
		tableViewer.setInput(projects);

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						ISelection source = event.getSelection();
						if (source instanceof IStructuredSelection) {
							IMarkerResolution[] resolutions = getMarkerResolutions(source);
							quickFixButton.setEnabled(resolutions.length > 0);
						}
					}

				});
		resourceChangeListener = new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (tableViewer != null
								&& !tableViewer.getTable().isDisposed()) {
							refreshTableViewer();
						}
					}

				});

			}

		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceChangeListener);
		return area;
	}

	@Override
	public boolean close() {
		if (_dlgTitleImage != null) {
			_dlgTitleImage.dispose();
		}
		if (resourceChangeListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(
					resourceChangeListener);
			resourceChangeListener = null;
		}
		return super.close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		quickFixButton = getButton(IDialogConstants.OK_ID);
		quickFixButton.setText(Messages.MarkerDialog_Quick_Fix);
		finishButton = getButton(IDialogConstants.CANCEL_ID);
		finishButton.setText(Messages.MarkerDialog_Finish);
		quickFixButton.setEnabled(false);
	}

	@Override
	protected void cancelPressed() {
		setReturnCode(OK);
		close();
	}

	@Override
	protected void okPressed() {
		ISelection source = tableViewer.getSelection();
		if (source instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) source;
			Object object = selection.getFirstElement();
			if (object instanceof IMarker) {
				IMarker selected = (IMarker) object;
				try {
					quickFixButton.setSelection(false);
					openQuickFixWizard(selected);
					for (Project project : projects) {
						if (project.getIncludedProjects() == null) {
							buildProject(project.getName());
						} else {
							List<String> includedProjects = project.getIncludedProjects();
							for (String projectName:includedProjects) {
								buildProject(projectName);
							}
						}
					}
					ProjectExamplesActivator.waitForBuildAndValidation
							.schedule();
					ProjectExamplesActivator.waitForBuildAndValidation.join();
				} catch (Exception e) {
					ProjectExamplesActivator.log(e);
				} finally {
					refreshTableViewer();
				}
			}
		}
	}

	private void buildProject(String projectName) throws CoreException {
		IProject eclipseProject = ResourcesPlugin
				.getWorkspace().getRoot().getProject(projectName);
		if (eclipseProject != null
				&& eclipseProject.isOpen()) {
			eclipseProject.build(
					IncrementalProjectBuilder.FULL_BUILD,
					null);
		}
	}

	private void refreshTableViewer() {
		tableViewer.setInput(projects);
		ISelection source = tableViewer.getSelection();
		if (source instanceof IStructuredSelection) {
			IMarkerResolution[] resolutions = getMarkerResolutions(source);
			quickFixButton.setEnabled(resolutions.length > 0);
		} else {
			quickFixButton.setSelection(false);
		}
	}

	private void openQuickFixWizard(final IMarker selected)
			throws ExecutionException {
		final Map resolutions = new LinkedHashMap();

		IRunnableWithProgress resolutionsRunnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor
						.beginTask(
								MarkerMessages.resolveMarkerAction_computationManyAction,
								100);

				IMarker[] allMarkers = (IMarker[]) ProjectExamplesActivator
						.getMarkers(projects).toArray(new IMarker[0]);
				monitor.worked(20);
				IMarkerResolution[] found = IDE.getMarkerHelpRegistry()
						.getResolutions(selected);
				int progressCount = 80;
				if (found.length > 1)
					progressCount = progressCount / found.length;
				for (int i = 0; i < found.length; i++) {
					IMarkerResolution markerResolution = found[i];
					if (markerResolution instanceof WorkbenchMarkerResolution) {
						IMarker[] other = ((WorkbenchMarkerResolution) markerResolution)
								.findOtherMarkers(allMarkers);
						Collection markers = new ArrayList();
						markers.add(selected);
						for (int j = 0; j < other.length; j++) {
							markers.add(other[j]);
						}
						resolutions.put(markerResolution, markers);
					} else {
						Collection markers = new ArrayList();
						markers.add(selected);
						resolutions.put(markerResolution, markers);
					}
					monitor.worked(progressCount);
				}
				monitor.done();
			}
		};

		Object service = null;

		IRunnableContext context = new ProgressMonitorDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());

		try {
			if (service == null) {
				PlatformUI.getWorkbench().getProgressService().runInUI(context,
						resolutionsRunnable, null);
			} else {
				((IWorkbenchSiteProgressService) service).runInUI(context,
						resolutionsRunnable, null);
			}
		} catch (InvocationTargetException exception) {
			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		} catch (InterruptedException exception) {

			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		}

		String markerDescription = selected.getAttribute(IMarker.MESSAGE,
				MarkerSupportInternalUtilities.EMPTY_STRING);
		if (resolutions.isEmpty()) {
			Status newStatus = new Status(
					IStatus.INFO,
					IDEWorkbenchPlugin.IDE_WORKBENCH,
					NLS
							.bind(
									MarkerMessages.MarkerResolutionDialog_NoResolutionsFound,
									new Object[] { markerDescription }));
			StatusAdapter adapter = new StatusAdapter(newStatus);
			adapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY,
					MarkerMessages.MarkerResolutionDialog_CannotFixTitle);
			StatusManager.getManager().handle(adapter, StatusManager.SHOW);
		} else {

			String description = NLS.bind(
					MarkerMessages.MarkerResolutionDialog_Description,
					markerDescription);

			Wizard wizard = new QuickFixWizard(description, resolutions);
			wizard
					.setWindowTitle(MarkerMessages.resolveMarkerAction_dialogTitle);
			WizardDialog dialog = new QuickFixWizardDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),
					wizard);
			dialog.open();
		}
	}

	private IMarkerResolution[] getMarkerResolutions(ISelection source) {
		IStructuredSelection selection = (IStructuredSelection) source;
		IMarker marker = (IMarker) selection.getFirstElement();
		if (marker == null) {
			return EMPTY_ARRAY;
		}
		IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry()
				.getResolutions(marker);
		return resolutions;
	}
}