package org.iru.maven.plugins.jaxbdeps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo( name = "clean", requiresDependencyResolution = ResolutionScope.NONE, 
defaultPhase = LifecyclePhase.PRE_CLEAN, threadSafe = true )
public class CleanMojo extends AbstractJaxbDepMojo {

	@Override
	protected ArtifactProcessor getArtifactProcessor() {
		return new ArtifactProcessor() {

			Log log;
			File extractedFilesFile;
			
			@Override
			public void setup(Log log) throws MojoFailureException {
				
				this.log = log;
				
				File extractedFilesFile = new File(rundir, EXTRACTED_FILES);
				
				if (extractedFilesFile.exists()) {
					try {
						@SuppressWarnings("unchecked")
						List<String> files = IOUtils.readLines(new FileInputStream(extractedFilesFile));
						for (String file : files) {
							if (! new File(outputDirectory, file).delete()) {
								this.log.warn("Could not delete: "+file);
							}
						}
						
					} catch (IOException e) {
						throw new MojoFailureException(e.getMessage(), e);
					}
				} else {
					extractedFilesFile = null;
				}
				
			}

			@Override
			public void processArtifact(Artifact artifact, Dependency dependency)
					throws MojoExecutionException {
				throw new MojoExecutionException("Unexpected artifact in clean: " + getKey(dependency));
			}

			@Override
			public void shutdown() throws MojoFailureException {
				if (extractedFilesFile != null) {
					if (! extractedFilesFile.delete())
						this.log.warn("Could not delete: "+extractedFilesFile);
				}
			}
			
			
		};
	
	}

}
