package ca.piggott.m2e.jasmine.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.osgi.util.NLS;

public class MavenHelper {

	public static MavenProject getProjectByName(String projectName, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Loading Maven Project", 1);
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				createCoreException(NLS.bind("Failed to locate workspace project {0}",  project));
			}
			return MavenPlugin.getMavenProjectRegistry().getProject(project).getMavenProject(monitor);
		} finally {
			monitor.done();
		}
	}
	
	public static List<String> getExecutionIds(String projectName, IProgressMonitor monitor) throws CoreException {
		MavenProject project = getProjectByName(projectName, monitor);
		List<String> executionIds = new LinkedList<String>();
		for (PluginExecution exe : getExecutions(project)) {
			executionIds.add(exe.getId());
		}
		return executionIds;
	}

	public static List<PluginExecution> getExecutions(MavenProject project) throws CoreException {
		for (Plugin plugin : project.getBuild().getPlugins()) {
			if ("com.github.searls".equals(plugin.getGroupId()) && "jasmine-maven-plugin".equals(plugin.getArtifactId())) {
				return plugin.getExecutions();
			}
		}
		throw createCoreException("Failed to find jasmine configuration");
	}

	public static JasminePluginConfiguration getConfiguration(String projectName, String executionId, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(monitor);
		try {
			MavenProject project = getProjectByName(projectName, subMon.newChild(9));
			if (project == null) {
				throw createCoreException(NLS.bind("Failed to load maven project: {0}", projectName));
			}
			if (subMon.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			return getConfiguration(project, executionId, subMon.newChild(1));
		} finally {
			subMon.done();
		}
	}

	public static List<String> getPreloadSources(String projectName, JasminePluginConfiguration config) throws CoreException {
		return getFiles(projectName, config.getJsSourceDir(), config.getPreloadSources(), Collections.<String>emptyList());
	}

	public static List<String> getSources(String projectName, JasminePluginConfiguration config) throws CoreException {
		return getFiles(projectName, config.getJsSourceDir(), config.getSourceIncludes(), config.getSourceExcludes());
	}

	public static List<String> getSpecs(String projectName, JasminePluginConfiguration config) throws CoreException {
		return getFiles(projectName, config.getJsSpecDir(), config.getSpecIncludes(), config.getSpecExcludes());
	}
	
	private static List<String> getFiles(String projectName, String path, List<String> includes, List<String> excludes) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IFolder folder = project.getFolder(Path.fromOSString(path));
		if (!folder.exists()) {
			return Collections.<String>emptyList();
		}
		File root = new File(folder.getRawLocationURI());
		try {
			List<File> files = FileUtils.getFiles(root, toCommaSeparated(includes), toCommaSeparated(excludes));
			List<String> urls = new ArrayList<>(files.size());

			for (File file : files) {
				urls.add(file.toURI().toURL().toString());
			}

			return urls;
		} catch (IOException e) {
			throw createCoreException(NLS.bind("Failed while matching files in {0}", root), e);
		}
	}

	private static String toCommaSeparated(List<String> patterns) {
		if (patterns.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String pattern : patterns) {
			sb.append(pattern).append(',');
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	private static JasminePluginConfiguration getConfiguration(MavenProject project, String executionId, IProgressMonitor monitor) throws CoreException {
		for (PluginExecution exe : getExecutions(project)) {
			if (executionId.equals(exe.getId())) {
				Xpp3Dom config = (Xpp3Dom) exe.getConfiguration();
				return JasminePluginConfiguration.from(config);
			}
		}
		throw createCoreException(NLS.bind("Failed to find executionId {0} in {1}", executionId, project.getName()));
	}

	private static CoreException createCoreException(String message) {
		return createCoreException(message, null);
	}
	
	private static CoreException createCoreException(String message, Exception e) {
		return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e));
	}
}
