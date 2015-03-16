package org.iru.maven.plugins.jaxbdeps;

import java.io.File;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.AbstractDependencyMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractJaxbDepMojo extends AbstractDependencyMojo {

	/**
	 * The dependencies to look into
	 */
	@Parameter
	protected Dependency[] dependencies;
	/**
	 * The files to include
	 */
	@Parameter(defaultValue = "*.xsd")
	protected String includes;
	/**
	 * The files to exclude
	 */
	@Parameter(defaultValue = "")
	protected String excludes;
	/**
	 * The directory where to write schema
	 */
	@Parameter
	protected File outputDirectory;
	
	@Parameter(defaultValue = "${project.build.directory}/jaxbdeps")
	protected File rundir;
	
	protected static final String EXTRACTED_FILES = "extracted-files";

	public AbstractJaxbDepMojo() {
		super();
	}
	
	protected abstract ArtifactProcessor getArtifactProcessor();
	
	protected String getKey(Dependency dep) {
		return dep.getGroupId()+":"+dep.getArtifactId();
	}
	
	@Override
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		
		if (outputDirectory == null) {
			File wsdlDirectory = new File(project.getBasedir(), "src/wsdl");
			if (wsdlDirectory.exists()) {
				outputDirectory = wsdlDirectory;
			} else {
				outputDirectory = new File(project.getBasedir(), "src/main/resources");
			}
		}
		
		if (dependencies == null) {
			@SuppressWarnings("unchecked")
			Object[] depsArray = project.getCompileDependencies().toArray(new Dependency[0]);
			dependencies = (Dependency[]) depsArray;
		}
		
		ArtifactProcessor ap = getArtifactProcessor();
		
		
		@SuppressWarnings("unchecked")
		Map<String, Artifact> artifactMap = project.getArtifactMap();
		
		
		ap.setup(log);
		
		for (Dependency dep : dependencies) {
			String artifactKey = getKey(dep);
			Artifact depArtifact = artifactMap.get(artifactKey);
			if (depArtifact == null) {
				log.warn("Unresolved artifact: "+artifactKey);
				continue;
			}
			
			ap.processArtifact(depArtifact, dep);
		}
		
		ap.shutdown();
		
	}
}