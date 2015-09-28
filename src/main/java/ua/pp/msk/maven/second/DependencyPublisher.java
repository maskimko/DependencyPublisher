/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven.second;

import com.jcabi.aether.Aether;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Repository;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 *
 * @author maskimko
 */
@Mojo(name = "publish", defaultPhase = LifecyclePhase.INSTALL)
public class DependencyPublisher extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;
    /*
     * jcabi Aether from Sonatype requires lots of dependencies
     * I think it should be removed in future.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String artifactId = mavenProject.getArtifactId();
        String groupId = mavenProject.getGroupId();
        String version = mavenProject.getVersion();
        getLog().info(String.format("groupId: %s, artifactId: %s, version: %s", artifactId, groupId, version));
        List repositories = mavenProject.getRepositories();
        if (repositories != null && repositories.size() > 0) {
            for (Object o : repositories) {
                getLog().info(String.format("Repository object class: %s, toString: %s", o.getClass().getCanonicalName(), o.toString()));
                if (o instanceof Repository) {
                    Repository r = (Repository) o;
                    getLog().info(String.format("Repository id: %s name: %s layout %s url: %s", r.getId(), r.getName(), r.getLayout(), r.getUrl()));
                }
            }
        }
        /*
         * jcabi Aether from Sonatype requires lots of dependencies
         * I think it should be removed in future.
         */
       try {
        File repo = repositorySystemSession.getLocalRepository().getBasedir();
        Aether a = new Aether(mavenProject, repo);
            List<Artifact> artifacts = a.resolve(new DefaultArtifact(groupId, artifactId, "", version), JavaScopes.RUNTIME);
       } catch (DependencyResolutionException ex){
           getLog().error("Cannot resolve artifacts: " + ex.getMessage());
       }
    }
}
