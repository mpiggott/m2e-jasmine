package ca.piggott.m2e.jasmine.internal.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.statushandlers.StatusManager;

import ca.piggott.m2e.jasmine.internal.Activator;

public class JasmineBrowserEditorPart extends EditorPart {

	private static final String ID = JasmineBrowserEditorPart.class.getCanonicalName();

	private Browser browser;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	@Override
	public void doSaveAs() {
		// do nothing
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof JasmineEditorInput) {
			setSite(site);
			setInput(input);
			return;
		}
		throw new PartInitException("Wrong editor input:" + input.getClass()); //$NON-NLS-1$

	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);

		browser = new Browser(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browser.setLayoutData(gridData);
		try {
			browser.setUrl(((JasmineEditorInput)getEditorInput()).getSpecRunner().toURI().toURL().toString());
			browser.addProgressListener(new ProgressAdapter() {
				public void completed(ProgressEvent event) {
					new JasmineListener(browser);
				}
			});
		} catch (MalformedURLException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e), StatusManager.SHOW);
		}
	}

	@Override
	public void setFocus() {
		if (browser != null) {
			browser.setFocus();
		}
	}

	public static IEditorPart open(File specRunner, String name) throws PartInitException {
		List<PartInitException> exception = new LinkedList<>();
		List<IEditorPart> part = new LinkedList<>();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				if (win != null) {
					IWorkbenchPage page = win.getActivePage();
					JasmineEditorInput input = new JasmineEditorInput(specRunner, name);
					if (page != null) {
						try {
							part.add(page.openEditor(input, ID));
						} catch (PartInitException e) {
							exception.add(e);
						}
					}
				}
			}});
		if (!exception.isEmpty()) {
			throw exception.get(0);
		}
		return part.get(0);
	}

	public static class JasmineEditorInput implements IEditorInput {

		private final File specRunner;

		private final String name;

		public JasmineEditorInput(File specRunner, String name) {
			this.specRunner = specRunner;
			this.name = name;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapter) {
			return null;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return name;
		}

		public File getSpecRunner() {
			return specRunner;
		}
	}
}
