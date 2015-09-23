package ca.piggott.m2e.jasmine.internal.launch;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import ca.piggott.m2e.jasmine.internal.JasmineConfigurationConstants;
import ca.piggott.m2e.jasmine.internal.MavenHelper;

public class JasmineTab extends AbstractLaunchConfigurationTab {

	private Text projectText;

	private Text executionText;

	private ILaunchConfiguration configuration;

	@Override
	public void createControl(Composite parent) {
		Composite selector = new Composite(parent, SWT.NONE);
		setControl(selector);
		selector.setLayout(new GridLayout(3, false));
		createProjectGroup(selector);
		createExecutionGroup(selector);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			this.configuration = configuration;
			projectText.setText(configuration.getAttribute(JasmineConfigurationConstants.PROJECT_NAME, ""));
			executionText.setText(configuration.getAttribute(JasmineConfigurationConstants.EXECUTION_ID, ""));
		} catch (CoreException e) {
			// TODO StatusHandler
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(JasmineConfigurationConstants.PROJECT_NAME, projectText.getText());
		configuration.setAttribute(JasmineConfigurationConstants.EXECUTION_ID, executionText.getText());
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	protected boolean isDirty() {
		try {
			return projectText.getText().equals(configuration.getAttribute(JasmineConfigurationConstants.PROJECT_NAME, ""));
		} catch (CoreException e) {
		}
		return true;
	}

	private void createProjectGroup(Composite selector) {
		Label label = new Label(selector, SWT.NONE);
		label.setText("Project:");

		projectText = new Text(selector, SWT.NONE);
		projectText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = new Button(selector, SWT.NONE);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IProject project = chooseProject();
				if (project != null) {
					projectText.setText(project.getName());
				}
			}
		});
	}

	private IProject getProject() {
		String name = projectText.getText().trim();
		if (name.length() == 0) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
	
	private IProject chooseProject() {
		ILabelProvider labelProvider = new JavaElementLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);

		List<IProject> projects = new LinkedList<>();
		for (IMavenProjectFacade facade : MavenPlugin.getMavenProjectRegistry().getProjects()) {
			projects.add(facade.getProject());
		}
		dialog.setElements(projects.toArray());

		IProject project = getProject();
		if (project != null) {
			dialog.setInitialSelections(new Object[] { getProject() });
		}
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return project;
	}

	private void createExecutionGroup(Composite selector) {
		Label label = new Label(selector, SWT.NONE);
		label.setText("Exection ID:");

		executionText = new Text(selector, SWT.NONE);
		executionText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = new Button(selector, SWT.NONE);
		button.setText("Select");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String executionId = chooseExecution();
				if (executionId != null) {
					executionText.setText(executionId);
				}
			}
		});
	}
	
	private String chooseExecution() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		try {
			// TODO doing this on the UI thread is not a good idea
			dialog.setElements(MavenHelper.getExecutionIds(projectText.getText(), new NullProgressMonitor()).toArray());
		} catch (CoreException e) {
			// TODO StatusHandler
		}
		if (dialog.open() == Window.OK){
			return (String) dialog.getFirstResult();
		}
		return null;
	}
}
