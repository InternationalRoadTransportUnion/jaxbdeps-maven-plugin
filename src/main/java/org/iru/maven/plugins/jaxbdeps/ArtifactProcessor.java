package org.iru.maven.plugins.jaxbdeps;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public interface ArtifactProcessor {

	void setup(Log log) throws MojoFailureException;
	void processArtifact(Artifact artifact, Dependency dependency) throws MojoExecutionException;
	void shutdown() throws MojoFailureException;
}
