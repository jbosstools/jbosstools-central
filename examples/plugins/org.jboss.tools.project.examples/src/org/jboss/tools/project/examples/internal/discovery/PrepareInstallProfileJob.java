/*******************************************************************************
 * Copyright (c) 2009, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.project.examples.internal.discovery;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.IProvHelpContextIds;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.dialogs.InstallWizard;
import org.eclipse.equinox.internal.p2.ui.dialogs.PreselectedIUInstallWizard;
import org.eclipse.equinox.internal.p2.ui.dialogs.ProvisioningWizardDialog;
import org.eclipse.equinox.internal.p2.ui.dialogs.RemediationPage;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.RemediationOperation;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.discovery.core.model.ConnectorDescriptor;
import org.eclipse.mylyn.internal.discovery.ui.AbstractInstallJob;
import org.eclipse.mylyn.internal.discovery.ui.DiscoveryUi;
import org.eclipse.mylyn.internal.discovery.ui.InstalledItem;
import org.eclipse.mylyn.internal.discovery.ui.UninstallRequest;
import org.eclipse.mylyn.internal.discovery.ui.util.DiscoveryUiUtil;
import org.eclipse.mylyn.internal.discovery.ui.wizards.Messages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * A job that configures a p2 {@link #getInstallAction() install action} for installing one or more
 * {@link ConnectorDescriptor connectors}. The bulk of the installation work is done by p2; this class just sets up the
 * p2 repository meta-data and selects the appropriate features to install. After running the job the
 * {@link #getInstallAction() install action} must be run to perform the installation.
 * 
 * Based on org.eclipse.mylyn.internal.discovery.ui.PrepareInstallProfileJob_e_3_6
 * 
 * @author David Green
 * @author Steffen Pingel
 * @author snjeza - adding the P2 remediation operation
 * 
 * @since 1.5.3
 */
public class PrepareInstallProfileJob extends AbstractInstallJob {

	protected final List<ConnectorDescriptor> installableConnectors;

	protected final ProvisioningUI provisioningUI;

	protected Set<URI> repositoryLocations;

	public PrepareInstallProfileJob(List<ConnectorDescriptor> installableConnectors) {
		if (installableConnectors == null) {
			throw new IllegalArgumentException();
		}
		this.installableConnectors = new ArrayList<ConnectorDescriptor>(installableConnectors);
		this.provisioningUI = ProvisioningUI.getDefaultUI();
	}

	@Override
	public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
		if (installableConnectors.isEmpty()) {
			throw new IllegalArgumentException("There are no connectors to install");
		}

		try {
			final SubMonitor monitor = SubMonitor.convert(progressMonitor, Messages.InstallConnectorsJob_task_configuring,
					100);
			try {
				final IInstallableUnit[] ius = computeInstallableUnits(monitor.newChild(50));

				checkCancelled(monitor);

				final InstallOperation installOperation = resolveInstall(monitor.newChild(50), ius,
						repositoryLocations.toArray(new URI[0]));
				
				checkCancelled(monitor);
				
				final RemediationOperation[] rops = new RemediationOperation[1];
				rops[0] = null;
				if (installOperation != null && installOperation.getResolutionResult().getSeverity() == IStatus.ERROR) {
					rops[0] = new RemediationOperation(ProvisioningUI.getDefaultUI()
							.getSession(), installOperation.getProfileChangeRequest());
					rops[0].resolveModal(monitor.newChild(500));
				}

				checkCancelled(monitor);

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (installOperation != null) {
							IProvisioningPlan plan = installOperation.getProvisioningPlan();
							ProvisioningContext context = installOperation.getProvisioningContext();
							ProvUI.getSize(ProvUI.getEngine(ProvisioningUI.getDefaultUI()
									.getSession()), plan, context, monitor);
						}
						openInstallWizard(Arrays.asList(ius), installOperation, rops[0], null);
					}
				});
			} finally {
				monitor.done();
			}
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	private int openInstallWizard(Collection<IInstallableUnit> initialSelections, InstallOperation operation, 
			final RemediationOperation rop, LoadMetadataRepositoryJob job) {
		if (operation == null) {
			InstallWizard wizard = new InstallWizard(provisioningUI, operation, initialSelections, job);
			WizardDialog dialog = new ProvisioningWizardDialog(ProvUI.getDefaultParentShell(), wizard);
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IProvHelpContextIds.INSTALL_WIZARD);
			return dialog.open();
		}
		PreselectedIUInstallWizard wizard = new PreselectedIUInstallWizard(provisioningUI, operation, initialSelections, job) {

			@Override
			protected RemediationPage createRemediationPage() {
				if (rop == null) {
					return null;
				}
				return super.createRemediationPage();
			}
			
		};
		wizard.setRemediationOperation(rop);
		WizardDialog dialog = new ProvisioningWizardDialog(ProvUI.getDefaultParentShell(), wizard);
		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IProvHelpContextIds.INSTALL_WIZARD);
		return dialog.open();
	}
	
	@Override
	public IStatus uninstall(UninstallRequest request, IProgressMonitor progressMonitor)
			throws InvocationTargetException, InterruptedException {
		IProfile profile = ProvUI.getProfileRegistry(ProvisioningUI.getDefaultUI().getSession()).getProfile(
				ProvisioningUI.getDefaultUI().getProfileId());
		if (profile == null) {
			throw new IllegalStateException("No valid profile defined");
		}

		try {
			SubMonitor monitor = SubMonitor.convert(progressMonitor, Messages.InstallConnectorsJob_task_configuring,
					100);
			try {
				repositoryLocations = new HashSet<URI>(Arrays.asList(provisioningUI.getRepositoryTracker()
						.getKnownRepositories(provisioningUI.getSession())));

				final List<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
				IQueryResult<IInstallableUnit> result = profile.available(QueryUtil.createIUGroupQuery(), monitor);
				for (Iterator<IInstallableUnit> it = result.iterator(); it.hasNext();) {
					IInstallableUnit iu = it.next();
					try {
						org.osgi.framework.Version version = new org.osgi.framework.Version(iu.getVersion()
								.getOriginal());
						InstalledItem<IInstallableUnit> item = new InstalledItem<IInstallableUnit>(iu, iu.getId(),
								version);
						if (request.select(item)) {
							ius.add(iu);
						}
					} catch (IllegalArgumentException e) {
						// ignore
					}
				}

				checkCancelled(monitor);

				if (ius.size() == 0) {
					return Status.CANCEL_STATUS;
				}

				final UninstallOperation uninstallOperation = resolveUninstall(monitor.newChild(50),
						ius.toArray(new IInstallableUnit[0]), repositoryLocations.toArray(new URI[0]));

				checkCancelled(monitor);

				return uninstallOperation.getProvisioningJob(null).runModal(monitor);
			} finally {
				monitor.done();
			}
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	protected void checkCancelled(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	protected UninstallOperation resolveUninstall(IProgressMonitor monitor, final IInstallableUnit[] ius,
			URI[] repositories) throws CoreException {
		final UninstallOperation uninstallOperation = provisioningUI.getUninstallOperation(Arrays.asList(ius),
				repositories);
		IStatus operationStatus = uninstallOperation.resolveModal(new SubProgressMonitor(monitor,
				installableConnectors.size()));
		if (operationStatus.getSeverity() > IStatus.WARNING) {
			throw new CoreException(operationStatus);
		}
		return uninstallOperation;
	}

	private InstallOperation resolveInstall(IProgressMonitor monitor, final IInstallableUnit[] ius, URI[] repositories)
			throws CoreException {
		final InstallOperation installOperation = provisioningUI.getInstallOperation(Arrays.asList(ius), repositories);
		installOperation.resolveModal(new SubProgressMonitor(monitor,
				installableConnectors.size()));
//		if (operationStatus.getSeverity() > IStatus.WARNING) {
//			throw new CoreException(operationStatus);
//		}
		return installOperation;
	}

	public IInstallableUnit[] computeInstallableUnits(SubMonitor monitor) throws CoreException {
		try {
			monitor.setWorkRemaining(100);
			// add repository urls and load meta data
			List<IMetadataRepository> repositories = addRepositories(monitor.newChild(50));
			final List<IInstallableUnit> installableUnits = queryInstallableUnits(monitor.newChild(50), repositories);
			removeOldVersions(installableUnits);
			checkForUnavailable(installableUnits);
			return installableUnits.toArray(new IInstallableUnit[installableUnits.size()]);

//			MultiStatus status = new MultiStatus(DiscoveryUi.ID_PLUGIN, 0, Messages.PrepareInstallProfileJob_ok, null);
//			ius = installableUnits.toArray(new IInstallableUnit[installableUnits.size()]);
//			ProfileChangeRequest profileChangeRequest = InstallAction.computeProfileChangeRequest(ius, profileId,
//					status, new SubProgressMonitor(monitor, installableConnectors.size()));
//			if (status.getSeverity() > IStatus.WARNING) {
//				throw new CoreException(status);
//			}
//			if (profileChangeRequest == null) {
//				// failed but no indication as to why
//				throw new CoreException(new Status(IStatus.ERROR, DiscoveryUi.ID_PLUGIN,
//						Messages.PrepareInstallProfileJob_computeProfileChangeRequestFailed, null));
//			}
//			PlannerResolutionOperation operation = new PlannerResolutionOperation(
//					Messages.PrepareInstallProfileJob_calculatingRequirements, profileId, profileChangeRequest, null,
//					status, true);
//			IStatus operationStatus = operation.execute(new SubProgressMonitor(monitor, installableConnectors.size()));
//			if (operationStatus.getSeverity() > IStatus.WARNING) {
//				throw new CoreException(operationStatus);
//			}
//
//			plannerResolutionOperation = operation;

		} catch (URISyntaxException e) {
			// should never happen, since we already validated URLs.
			throw new CoreException(new Status(IStatus.ERROR, DiscoveryUi.ID_PLUGIN,
					Messages.InstallConnectorsJob_unexpectedError_url, e));
		} catch (MalformedURLException e) {
			// should never happen, since we already validated URLs.
			throw new CoreException(new Status(IStatus.ERROR, DiscoveryUi.ID_PLUGIN,
					Messages.InstallConnectorsJob_unexpectedError_url, e));
		} finally {
			monitor.done();
		}
	}

	/**
	 * Verifies that we found what we were looking for: it's possible that we have connector descriptors that are no
	 * longer available on their respective sites. In that case we must inform the user. Unfortunately this is the
	 * earliest point at which we can know.
	 */
	private void checkForUnavailable(final List<IInstallableUnit> installableUnits) throws CoreException {
		// at least one selected connector could not be found in a repository
		Set<String> foundIds = new HashSet<String>();
		for (IInstallableUnit unit : installableUnits) {
			foundIds.add(unit.getId());
		}

		StringBuilder message = new StringBuilder(); //$NON-NLS-1$
		StringBuilder detailedMessage = new StringBuilder(); //$NON-NLS-1$
		for (ConnectorDescriptor descriptor : installableConnectors) {
			StringBuilder unavailableIds = null;
			for (String id : descriptor.getInstallableUnits()) {
				if (!foundIds.contains(id)) {
					if (unavailableIds == null) {
						unavailableIds = new StringBuilder();
					} else {
						unavailableIds.append(Messages.InstallConnectorsJob_commaSeparator);
					}
					unavailableIds.append(id);
				}
			}
			if (unavailableIds != null) {
				if (message.length() > 0) {
					message.append( Messages.InstallConnectorsJob_commaSeparator );
				}
				message.append( descriptor.getName() );

				if (detailedMessage.length() > 0) {
					detailedMessage.append( Messages.InstallConnectorsJob_commaSeparator );
				}
				detailedMessage.append( NLS.bind(Messages.PrepareInstallProfileJob_notFoundDescriptorDetail, new Object[] {
						descriptor.getName(), unavailableIds.toString(), descriptor.getSiteUrl() }) );
			}
		}

		if (message.length() > 0) {
			// instead of aborting here we ask the user if they wish to proceed anyways
			final boolean[] okayToProceed = new boolean[1];
			final String finalMessage = message.toString();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					okayToProceed[0] = MessageDialog.openQuestion(DiscoveryUiUtil.getShell(),
							Messages.InstallConnectorsJob_questionProceed,
							NLS.bind(Messages.InstallConnectorsJob_questionProceed_long, new Object[] { finalMessage }));
				}
			});
			if (!okayToProceed[0]) {
				throw new CoreException(new Status(IStatus.ERROR, DiscoveryUi.ID_PLUGIN, NLS.bind(
						Messages.InstallConnectorsJob_connectorsNotAvailable, detailedMessage.toString()), null));
			}
		}
	}

	/**
	 * Filters those installable units that have a duplicate in the list with a higher version number. it's possible
	 * that some repositories will host multiple versions of a particular feature. we assume that the user wants the
	 * highest version.
	 */
	private void removeOldVersions(final List<IInstallableUnit> installableUnits) {
		Map<String, Version> symbolicNameToVersion = new HashMap<String, Version>();
		for (IInstallableUnit unit : installableUnits) {
			Version version = symbolicNameToVersion.get(unit.getId());
			if (version == null || version.compareTo(unit.getVersion()) < 0) {
				symbolicNameToVersion.put(unit.getId(), unit.getVersion());
			}
		}
		if (symbolicNameToVersion.size() != installableUnits.size()) {
			for (IInstallableUnit unit : new ArrayList<IInstallableUnit>(installableUnits)) {
				Version version = symbolicNameToVersion.get(unit.getId());
				if (!version.equals(unit.getVersion())) {
					installableUnits.remove(unit);
				}
			}
		}
	}

	/**
	 * Perform a query to get the installable units. This causes p2 to determine what features are available in each
	 * repository. We select installable units by matching both the feature id and the repository; it is possible though
	 * unlikely that the same feature id is available from more than one of the selected repositories, and we must
	 * ensure that the user gets the one that they asked for.
	 */
	private List<IInstallableUnit> queryInstallableUnits(SubMonitor monitor, List<IMetadataRepository> repositories)
			throws URISyntaxException {
		final List<IInstallableUnit> installableUnits = new ArrayList<IInstallableUnit>();

		monitor.setWorkRemaining(repositories.size());
		for (final IMetadataRepository repository : repositories) {
			checkCancelled(monitor);
			final Set<String> installableUnitIdsThisRepository = getDescriptorIds(repository);
			IQuery<IInstallableUnit> query = QueryUtil.createIUGroupQuery();
			IQueryResult<IInstallableUnit> result = repository.query(query, monitor.newChild(1));
			for (Iterator<IInstallableUnit> iter = result.iterator(); iter.hasNext();) {
				IInstallableUnit iu = iter.next();
				String id = iu.getId();
				if (installableUnitIdsThisRepository.contains(id)) {
					installableUnits.add(iu);
				}
			}
		}
		return installableUnits;
	}

	private List<IMetadataRepository> addRepositories(SubMonitor monitor) throws MalformedURLException,
			URISyntaxException, ProvisionException {
		// tell p2 that it's okay to use these repositories
		ProvisioningSession session = ProvisioningUI.getDefaultUI().getSession();
		RepositoryTracker repositoryTracker = ProvisioningUI.getDefaultUI().getRepositoryTracker();
		repositoryLocations = new HashSet<URI>();
		monitor.setWorkRemaining(installableConnectors.size() * 5);
		for (ConnectorDescriptor descriptor : installableConnectors) {
			URI uri = new URL(descriptor.getSiteUrl()).toURI();
			if (repositoryLocations.add(uri)) {
				checkCancelled(monitor);
				repositoryTracker.addRepository(uri, null, session);
			}
			monitor.worked(1);
		}

		// add selected repositories to resolve dependencies 
		URI[] knownRepositories = repositoryTracker.getKnownRepositories(session);
		if (knownRepositories != null) {
			for (URI uri : knownRepositories) {
				if (Pattern.matches("http://download.eclipse.org/releases/.*", uri.toString())) {
					repositoryLocations.add(uri);
				}
			}
		}

		// fetch meta-data for these repositories
		ArrayList<IMetadataRepository> repositories = new ArrayList<IMetadataRepository>();
		monitor.setWorkRemaining(repositories.size());
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) session.getProvisioningAgent().getService(
				IMetadataRepositoryManager.SERVICE_NAME);
		for (URI uri : repositoryLocations) {
			checkCancelled(monitor);
			IMetadataRepository repository = manager.loadRepository(uri, monitor.newChild(1));
			repositories.add(repository);
		}
		return repositories;
	}

	private Set<String> getDescriptorIds(final IMetadataRepository repository) throws URISyntaxException {
		final Set<String> installableUnitIdsThisRepository = new HashSet<String>();
		// determine all installable units for this repository
		for (ConnectorDescriptor descriptor : installableConnectors) {
			try {
				if (repository.getLocation().equals(new URL(descriptor.getSiteUrl()).toURI())) {
					installableUnitIdsThisRepository.addAll(descriptor.getInstallableUnits());
				}
			} catch (MalformedURLException e) {
				// will never happen, ignore
			}
		}
		return installableUnitIdsThisRepository;
	}

	@Override
	public Set<String> getInstalledFeatures(IProgressMonitor monitor) {
		Set<String> features = new HashSet<String>();
		IProfile profile = ProvUI.getProfileRegistry(ProvisioningUI.getDefaultUI().getSession()).getProfile(
				ProvisioningUI.getDefaultUI().getProfileId());
		if (profile != null) {
			IQueryResult<IInstallableUnit> result = profile.available(QueryUtil.createIUGroupQuery(), monitor);
			for (Iterator<IInstallableUnit> it = result.iterator(); it.hasNext();) {
				IInstallableUnit unit = it.next();
				features.add(unit.getId());
			}
		}
		return features;
	}

}
