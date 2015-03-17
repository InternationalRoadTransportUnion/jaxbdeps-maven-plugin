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
defaultPhase = LifecyclePhase.PRE_CLEAN, threadSafe = false )
public class CleanMojo extends AbstractJaxbDepMojo {

	@Override
	protected ArtifactProcessor getArtifactProcessor() {
		return new ArtifactProcessor() {

			Log log;
			File extractedFilesFile;
			File episodeFilesFile;
			
			@Override
			public void setup(Log log) throws MojoFailureException {
				
				this.log = log;
				
				extractedFilesFile = deleteFileFiles(new File(rundir, EXTRACTED_FILES), outputDirectory);
				
				if (bindingDirectory != null)
					episodeFilesFile = deleteFileFiles(new File(rundir, EPISODE_FILES), bindingDirectory);
				
			}

			private File deleteFileFiles(File extractedFilesFile, File directory)
					throws MojoFailureException {
				if (extractedFilesFile.exists()) {
					try {
						@SuppressWarnings("unchecked")
						List<String> files = IOUtils.readLines(new FileInputStream(extractedFilesFile));
						for (String file : files) {
							if (! new File(directory, file).delete()) {
								this.log.warn("Could not delete: "+file);
							} else {
								this.log.debug("Cleaning extracted file: "+file);
							}
						}
						
					} catch (IOException e) {
						throw new MojoFailureException(e.getMessage(), e);
					}
				} else {
					extractedFilesFile = null;
				}
				return extractedFilesFile;
			}

			@Override
			public void processArtifact(Artifact artifact, Dependency dependency)
					throws MojoExecutionException {
				throw new MojoExecutionException("Unexpected artifact in clean: " + getKey(dependency));
			}

			@Override
			public void shutdown() throws MojoFailureException {
				deleteFile(extractedFilesFile);
				deleteFile(episodeFilesFile);
			}

			private void deleteFile(File file) {
				if (file != null) {
					if (! file.delete()) {
						this.log.warn("Could not delete: "+file);
					} else { 
						this.log.debug("Cleaning temporary file: "+file);
					}
				}
			}
			
			
		};
	
	}

}
