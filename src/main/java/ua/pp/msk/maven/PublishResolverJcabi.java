/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven;

import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 *
 * @author Maksym Shkolnyi aka maskimko
 */
public class PublishResolverJcabi implements PublishResolver {

    private RepositorySystemSession repoSession;

    private RepositorySystem repoSystem;

    private List<RemoteRepository> remoteRepos;

    private Log log;

    public RepositorySystemSession getRepoSession() {
        return repoSession;
    }

    public void setRepoSession(RepositorySystemSession repoSession) {
        this.repoSession = repoSession;
    }

    public RepositorySystem getRepoSystem() {
        return repoSystem;
    }

    public void setRepoSystem(RepositorySystem repoSystem) {
        this.repoSystem = repoSystem;
    }

    public List<RemoteRepository> getRemoteRepos() {
        return remoteRepos;
    }

    public void setRemoteRepos(List<RemoteRepository> remoteRepos) {
        this.remoteRepos = remoteRepos;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    
    public PublishResolverJcabi(){
    }
    
    public PublishResolverJcabi(RepositorySystemSession repoSession, RepositorySystem repoSystem, List<RemoteRepository> remoteRepos, Log log) {
        this.repoSession = repoSession;
        this.repoSystem = repoSystem;
        this.remoteRepos = remoteRepos;
        this.log = log;
    }

    
    
    public Artifact resolve(Artifact artifact) throws ArtifactResolutionException {

        ArtifactRequest request = new ArtifactRequest();
      
        request.setArtifact(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), null, artifact.getVersion()));
        request.setRepositories(remoteRepos);

        getLog().info("Resolving artifact " + artifact
                + " from " + remoteRepos);

        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
        if (artifact.getFile() == null) {
            artifact.setFile(result.getArtifact().getFile());
        }
        getLog().info(String.format("Resolved artifact %s:%s:%s to %s form %s", 
                artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), 
                result.getArtifact().getFile().getAbsolutePath() , result.getRepository().toString()));
        return artifact;
    }
}
