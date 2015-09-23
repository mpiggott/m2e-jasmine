package ca.piggott.m2e.jasmine.internal;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class JasminePluginConfiguration {

	private final String jsSourceDir;

	private final String jsSpecDir;

	private final List<String> preloadSources;

	private final List<String> sourceExcludes;

	private final List<String> sourceIncludes;

	private final List<String> specExcludes;

	private final List<String> specIncludes;

	public JasminePluginConfiguration(List<String> preloadSources, List<String> specIncludes,
			List<String> sourceIncludes, List<String> specExcludes,
			List<String> sourceExcludes, String jsSourceDir, String jsSpecDir) {
		this.preloadSources = preloadSources;
		this.specIncludes = specIncludes;
		this.sourceIncludes = sourceIncludes;
		this.specExcludes = specExcludes;
		this.sourceExcludes = sourceExcludes;

		this.jsSourceDir = jsSourceDir == null ? "src/main/javascript" : jsSourceDir;
		this.jsSpecDir = jsSpecDir == null ? "src/test/javascript" : jsSpecDir;
	}

	public String getJsSourceDir() {
		return jsSourceDir;
	}

	public String getJsSpecDir() {
		return jsSpecDir;
	}

	public List<String> getPreloadSources() {
		return preloadSources;
	}
	
	public List<String> getSourceExcludes() {
		return sourceExcludes;
	}
	
	public List<String> getSourceIncludes() {
		return sourceIncludes;
	}

	public List<String> getSpecExcludes() {
		return specExcludes;
	}

	public List<String> getSpecIncludes() {
		return specIncludes;
	}

	public static  JasminePluginConfiguration from(Xpp3Dom pluginConfiguration) {
		return new JasminePluginConfiguration(getList(pluginConfiguration, "preloadSources", Collections.emptyList()),
				getList(pluginConfiguration, "specIncludes", Collections.singletonList("**/*.js")),
				getList(pluginConfiguration, "sourceIncludes", Collections.singletonList("**/*.js")),
				getList(pluginConfiguration, "specExcludes", Collections.emptyList()),
				getList(pluginConfiguration, "sourceExcludes", Collections.emptyList()),
				get(pluginConfiguration, "jsSrcDir", "src/main/javascript"),
				get(pluginConfiguration, "jsTestSrcDir", "src/test/javascript"));
	}

	private static String get(Xpp3Dom parent, String name, String defaultValue) {
		Xpp3Dom child = parent.getChild(name);
		return child == null ? defaultValue : child.getValue();
	}

	private static List<String> getList(Xpp3Dom parent, String name, List<String> defaults) {
		Xpp3Dom dom = parent.getChild(name);
		if (dom == null || dom.getChildCount() == 0) {
			return defaults;
		}

		List<String> values = new LinkedList<>();
		for (Xpp3Dom child : dom.getChildren()) {
			values.add(child.getValue());
		}
		return values;
	}
}
