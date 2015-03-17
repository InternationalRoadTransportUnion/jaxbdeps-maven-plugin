package org.iru.maven.plugins.jaxbdeps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.FileUtils;

@Mojo( name = "unpack", requiresDependencyResolution = ResolutionScope.COMPILE, 
defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = false )
public class UnpackMojo extends AbstractJaxbDepMojo {

	private static final String META_INF_SUN_JAXB_EPISODE = "META-INF/sun-jaxb.episode";

	/**
	 * The dependencies whose episode file should not be extracted
	 */
	@Parameter
	protected Dependency[] skipEpisodeDependencies;


	
	/**
	 * The episode file extension
	 */
	@Parameter(defaultValue = "xml")
	protected String episodeFileExtension;
	

	@Override
	protected ArtifactProcessor getArtifactProcessor() {
		return new ArtifactProcessor() {

			File episodeTmpDir = null;
			Transformer transformer = null;
			File extractedFilesFile;
			List<String> extractedFiles;
			List<String> episodeFiles;
			File episodeFilesFile;
			
			Log log;
			
			public void setup(Log log) throws MojoFailureException {
				this.log = log;
				
				rundir.mkdirs();
				try {
					FileUtils.cleanDirectory(rundir);
					extractedFilesFile = new File(rundir, EXTRACTED_FILES);
				} catch (IOException e) {
					throw new MojoFailureException(e.getMessage(), e);
				}

				extractedFiles = new ArrayList<String>();
				
				if (bindingDirectory != null) {
					try {
						bindingDirectory.mkdirs();
						episodeTmpDir = Files.createTempDirectory("jaxbdeps").toFile();
						Source xsl = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("ifexists.xsl"));

						transformer = TransformerFactory.newInstance().newTransformer(xsl);
						episodeFiles = new ArrayList<String>();
						episodeFilesFile = new File(rundir, EPISODE_FILES);
					} catch (TransformerException e) {
						throw new MojoFailureException(e.getMessage(), e);
					} catch (IOException e) {
						throw new MojoFailureException(e.getMessage(), e);
					}
				}
			}

			@Override
			public void processArtifact(Artifact depArtifact, Dependency dep) throws MojoExecutionException {
				String artifactKey = getKey(dep);

				{
					unpack(depArtifact, rundir, includes, excludes);
					
					List<String> newFiles;
					try {
						newFiles = FileUtils.getFileNames(rundir, null, null, false);
					} catch (IOException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					}
					
					for (String newFile : newFiles) {
						File dest = new File(outputDirectory, newFile);
						dest.getParentFile().mkdirs();
						if (new File(rundir, newFile).renameTo(dest)) 
							extractedFiles.add(newFile);
					}
					
				}
				
				if (bindingDirectory != null) {
					if (skipEpisodeDependencies != null) {
						getLog().info("Evaluating skipEpisodeDependencies for: " + artifactKey);
						for (Dependency skipEpisodeDependency : skipEpisodeDependencies) {
							if (skipEpisodeDependency.getGroupId().equals(dep.getGroupId()) &&
									skipEpisodeDependency.getArtifactId().equals(dep.getArtifactId())) {
								log.info("Skipping episode file in "+artifactKey);
								return;
							}
						}
						log.info("Continuing with " + artifactKey);
					}

					unpack(depArtifact, episodeTmpDir, META_INF_SUN_JAXB_EPISODE, null);
					File ef = new File(episodeTmpDir, META_INF_SUN_JAXB_EPISODE);
					if (ef.exists()) {
						File df = new File(bindingDirectory, dep.getArtifactId()+"-episode."+ episodeFileExtension);
						Source input = new StreamSource(ef);
						Result output = new StreamResult(df);
						try {
							transformer.transform(input, output);
						} catch (TransformerException e) {
							log.warn(e);
							try {
								FileUtils.rename(ef, df);
							} catch (IOException ioe) {
								throw new MojoExecutionException(ioe.getMessage(), ioe);
							}
						} finally {
							episodeFiles.add(df.getName());
						}
					}

				}
			}

			public void shutdown() {
				try {
					{
						FileOutputStream out = new FileOutputStream(extractedFilesFile);
						IOUtils.writeLines(extractedFiles, null, out);
					}
					if (episodeFilesFile != null) {
						FileOutputStream out = new FileOutputStream(episodeFilesFile);
						IOUtils.writeLines(episodeFiles, null, out);
					}
					
					if (episodeTmpDir != null) {
						FileUtils.deleteDirectory(episodeTmpDir);
					} 
				} catch (IOException e) {
					log.warn(e);
				}
			}
		};

	}
}
