package org.jboss.tools.maven.ui.internal.project.facet;

import java.util.Map;
import java.util.SortedSet;

import javax.swing.JButton;

import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.ui.IFacetWizardPage;
import org.eclipse.wst.common.project.facet.ui.IWizardContext;
import org.jboss.tools.maven.core.IJBossMavenConstants;

public class MavenFacetInstallPage extends DataModelWizardPage implements
IFacetWizardPage {

	private static final String SEAM_FACET_ID = "jst.seam"; //$NON-NLS-1$
	private Text groupId;
	private Text artifactId;
	private Text version;
	private Combo packaging;
	public static final IProjectFacet SEAM_FACET = ProjectFacetsManager.getProjectFacet(SEAM_FACET_ID);
	
	public MavenFacetInstallPage() {
		super(DataModelFactory.createDataModel(new AbstractDataModelProvider() {
		}), "jboss.m2.facet.install.page"); //$NON-NLS-1$
		setTitle("JBoss M2 capabilities");
		setDescription("Add M2 capabilities to this Web Project");
		
	}

	@Override
	protected Composite createTopLevelComposite(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		
		groupId = createField(composite,"Group Id:",IJBossMavenConstants.GROUP_ID);
		artifactId = createField(composite, "Artifact Id:", IJBossMavenConstants.ARTIFACT_ID);
		String projectName = getDataModel().getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
		artifactId.setText(projectName);
		
		version = createField(composite, "Version:", IJBossMavenConstants.VERSION);
		
		Label packagingLabel = new Label(composite, SWT.NONE);
		packagingLabel.setText("Packaging:");
		packaging = new Combo(composite, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		packaging.setLayoutData(gd);
		synchHelper.synchCombo(packaging, IJBossMavenConstants.PACKAGING, null);
		// FIXME
		String[] items = { "war","ear", "ejb", "jar" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		packaging.setItems(items);
		IFacetedProjectWorkingCopy fpwc = (IFacetedProjectWorkingCopy) getDataModel().getProperty(IFacetDataModelProperties.FACETED_PROJECT_WORKING_COPY);
		if (fpwc.hasProjectFacet(WebFacetUtils.WEB_FACET)) {
			packaging.select(0);
		} else if (fpwc.hasProjectFacet(IJ2EEFacetConstants.ENTERPRISE_APPLICATION_FACET)) {
			packaging.select(1);
		} else if (fpwc.hasProjectFacet(IJ2EEFacetConstants.EJB_FACET)) {
			packaging.select(2);
		} else {
			packaging.select(3);
		}
		
		Text name = createField(composite, "Name:", IJBossMavenConstants.NAME);
		name.setText(projectName);
		createField(composite, "Description", IJBossMavenConstants.DESCRIPTION);
	
		if (fpwc.hasProjectFacet(SEAM_FACET)) {
			Text seamVersion = createField(composite, "Seam Maven version:", IJBossMavenConstants.SEAM_MAVEN_VERSION);
			IProjectFacetVersion seamFacetVersion = fpwc.getProjectFacetVersion(SEAM_FACET);
			if ("2.0".equals(seamFacetVersion.getVersionString())) { //$NON-NLS-1$
				seamVersion.setText("2.0.2.SP1"); //$NON-NLS-1$
			} else {
				seamVersion.setText("2.1.1.GA"); //$NON-NLS-1$
			}
			Button removeWTPContainers = new Button(composite,SWT.CHECK);
			removeWTPContainers.setText("Remove WTP Classpath containers");
			synchHelper.synchCheckbox(removeWTPContainers, IJBossMavenConstants.REMOVE_WTP_CLASSPATH_CONTAINERS, null);
			// FIXME add the Validate button
		}
		
        validatePage();
		return composite;
	}

	@Override
	protected void validatePage() {
		setErrorMessage(null);
		if (groupId.getText().trim().length() <= 0) {
			setErrorMessage("The groupId field is required.");
		} else if (artifactId.getText().trim().length() <= 0) {
			setErrorMessage("The artifactId field is required.");
		} else if (version.getText().trim().length() <= 0) {
			setErrorMessage("The version field is required.");
		} else if (packaging.getText().trim().length() <= 0) {
			setErrorMessage("The packaging field is required.");
		}
		
	}

	private Text createField(Composite composite, String labelText,String property) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		text.setLayoutData(gd);
		synchHelper.synchText(text, property, null);
		return text;
	}

	@Override
	protected String[] getValidationPropertyNames() {
		return new String[0];
	}

	public void setConfig(Object config) {
		model.removeListener(this);
		synchHelper.dispose();

		model = (IDataModel) config;
		model.addListener(this);
		synchHelper = initializeSynchHelper(model);
	}

	@Override
	public void dispose() {
		model.removeListener(this);
		super.dispose();
	}

	public void setWizardContext(IWizardContext context) {

	}
	public void transferStateToConfig() {
		
	}

}
