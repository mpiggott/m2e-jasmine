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
import org.eclipse.swt.layout.FillLayout;
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
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class JasmineBrowserEditorPart extends EditorPart {

	private static final String ID = JasmineBrowserEditorPart.class.getCanonicalName();

	private FXCanvas canvas;

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
		parent.setLayout(new FillLayout());

		canvas = new FXCanvas(parent, SWT.NONE);
		WebView view = new WebView();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		scrollPane.getStyleClass().add("noborder-scroll-pane");
		Scene scene = new Scene(scrollPane);
		scrollPane.setContent(view);

		canvas.setScene(scene);

		WebEngine engine = view.getEngine();

		try {
			engine.load(((JasmineEditorInput)getEditorInput()).getSpecRunner().toURI().toURL().toString());

			// TODO
			// new JasmineListener(browser);
		} catch (MalformedURLException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", e), StatusManager.SHOW);
		}
		setPartName(getEditorInput().getName());
	}

	@Override
	public void setFocus() {
		if (canvas != null) {
			canvas.setFocus();
		}
	}

	public static IEditorPart open(final File specRunner, final String name) throws Exception {
		final List<Exception> exception = new LinkedList<>();
		final List<IEditorPart> part = new LinkedList<>();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					IWorkbenchPage page = getPage();
					JasmineEditorInput input = new JasmineEditorInput(specRunner, name);
					part.add(page.openEditor(input, ID));
				} catch (Exception e) {
					exception.add(e);
				}
			}});
		if (!exception.isEmpty()) {
			throw exception.get(0);
		}
		return part.get(0);
	}

	private static IWorkbenchPage getPage() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win != null) {
			IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				return page;
			}
		}
		for (IWorkbenchWindow wbw : wb.getWorkbenchWindows()) {
			IWorkbenchPage page = wbw.getActivePage();
			if (page != null) {
				return page;
			}
		}
		throw new IllegalStateException("No workbench pages?");
	}

	public static class JasmineEditorInput implements IEditorInput {

		private final File specRunner;

		private final String name;

		public JasmineEditorInput(File specRunner, String name) {
			this.specRunner = specRunner;
			this.name = name;
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


		@Override
		public <T> T getAdapter(Class<T> adapter) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
