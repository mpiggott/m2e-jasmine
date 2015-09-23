package ca.piggott.m2e.jasmine.internal.launch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import ca.piggott.m2e.jasmine.internal.Activator;
import ca.piggott.m2e.jasmine.internal.JasmineConfigurationConstants;
import ca.piggott.m2e.jasmine.internal.JasminePluginConfiguration;
import ca.piggott.m2e.jasmine.internal.MavenHelper;
import ca.piggott.m2e.jasmine.internal.ui.JasmineBrowserEditorPart;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class JasmineLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate {
	
	private Template template;
	private static final List<String> FILES;
	
	static {
		List<String> files = new LinkedList<>();
		files.add("boot.js");
		files.add("jasmine-html.js");
		files.add("jasmine.js");
		
		FILES = Collections.unmodifiableList(files);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, "Launching jasmine test " + configuration.getName(), 5);
		File specRunner = getSpecRunner(createModel(configuration, mon.newChild(4)), mon.newChild(1));

		// Create Browser editor part
		try {
			JasmineBrowserEditorPart.open(specRunner, configuration.getName());
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to open editor", e));
		}
		// TODO monitor browser part
	}

	private Map<String, Object> createModel(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		SubMonitor submon = SubMonitor.convert(monitor, "", 3);
		String projectName = configuration.getAttribute(JasmineConfigurationConstants.PROJECT_NAME, (String) null);
		String executionId = configuration.getAttribute(JasmineConfigurationConstants.EXECUTION_ID, (String) null);

		JasminePluginConfiguration config = MavenHelper.getConfiguration(projectName, executionId, monitor);

		Map<String,Object> model = new HashMap<>();
		model.put("preloadSources", MavenHelper.getPreloadSources(projectName, config));
		model.put("sources", MavenHelper.getSources(projectName, config));
		model.put("specs", MavenHelper.getSpecs(projectName, config));

		submon.done();

		return model;
	}

	private File getSpecRunner(Map<String, Object> model, IProgressMonitor subMonitor) throws CoreException {
		try {
			Path testPath = Files.createTempDirectory("jasmine");
			Path specRunner = testPath.resolve("index.html");
			
			getTemplate().process(model, Files.newBufferedWriter(specRunner, StandardOpenOption.CREATE));
			
			for (String file : FILES) {
				copy(testPath.resolve(file), file);
			}
			return specRunner.toFile();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create temporary file", e));
		} catch (TemplateException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create spec runner", e));
		}
	}

	private Template getTemplate() throws CoreException {
		if (template == null) {
			Configuration cfg = new Configuration();
			cfg.setClassForTemplateLoading(getClass(), "/jasmine/");
			try {
				template = cfg.getTemplate("SpecRunner.ftl");
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to load spec runner template", e));
			}
		}
		return template;
	}
	
	private static void copy(Path destination, String resource) throws IOException {
		InputStream in = null;
		try {
			in = JasmineLaunchConfigurationDelegate.class.getResourceAsStream("/jasmine/" + resource);
			Files.copy(in, destination);
		} finally {
			closeQuietly(in);
		}
		
	}

	private static void closeQuietly(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
}
