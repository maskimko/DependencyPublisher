package ua.pp.msk.maven;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class RepositoryInspector {

	private MavenProject mavenProject;
	private Log log;

	public RepositoryInspector(MavenProject mavenProject, Log log) {
		super();
		this.mavenProject = mavenProject;
		this.log = log;
	}

	public void inspect() {
		List remoteArtifactRepositories = mavenProject.getRemoteArtifactRepositories();
		List repositories = mavenProject.getRepositories();
		log.debug("Remote repositories");
		for (Object o : remoteArtifactRepositories) {

			if (o instanceof ArtifactRepository) {
				ArtifactRepository ar = (ArtifactRepository) o;
				log.debug(ar.getBasedir());
				log.debug(ar.getUrl());
				log.debug(ar.getKey());
			}

		}
		log.debug("Repositories");
		for (Object o : repositories) {
			if (o instanceof ArtifactRepository) {
				ArtifactRepository ar = (ArtifactRepository) o;
				log.debug(ar.getBasedir());
				log.debug(ar.getUrl());
				log.debug(ar.getKey());
			}
		}
	}

}
