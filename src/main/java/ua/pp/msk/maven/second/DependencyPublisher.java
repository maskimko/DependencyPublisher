/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven.second;

//import com.jcabi.aether.Aether;
import java.io.File;
import java.util.List;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Repository;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
//import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
//import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
//import org.apache.maven.shared.dependency.graph.DependencyNode;
//import org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor;
//import org.eclipse.aether.util.artifact.JavaScopes;
//import org.sonatype.aether.RepositorySystemSession;
//import org.sonatype.aether.artifact.Artifact;
//import org.sonatype.aether.resolution.DependencyResolutionException;
//import org.sonatype.aether.util.artifact.DefaultArtifact;

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
//    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
//    private RepositorySystemSession repositorySystemSession;

    @Component
    private DependencyGraphBuilder dependencyGraphBuilder;
    private ArtifactFilter artifactFilter;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String artifactId = mavenProject.getArtifactId();
        String groupId = mavenProject.getGroupId();
        String version = mavenProject.getVersion();
        getLog().info("Publish goal of Dependency publisher plugin");
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
        if (artifactFilter == null) {
            getLog().debug("Artifact filter is null. Continue with scope artifact filter");
            ScopeArtifactFilter scopeArtifactFilter = new ScopeArtifactFilter();
            scopeArtifactFilter.setIncludeCompileScope(true);
            scopeArtifactFilter.setIncludeProvidedScope(true);
            scopeArtifactFilter.setIncludeRuntimeScope(true);
            scopeArtifactFilter.setIncludeSystemScope(true);
            scopeArtifactFilter.setIncludeTestScope(true);
            artifactFilter = scopeArtifactFilter;
            getLog().info("Filter compile scope: " + scopeArtifactFilter.isIncludeCompileScope());
            getLog().info("Filter provided scope: " + scopeArtifactFilter.isIncludeProvidedScope());
            getLog().info("Filter runtime scope: " + scopeArtifactFilter.isIncludeRuntimeScope());
            getLog().info("Filter system scope: " + scopeArtifactFilter.isIncludeSystemScope());
            getLog().info("Filter test scope: " + scopeArtifactFilter.isIncludeTestScope());
        }
        getLog().info("Got artifact filter class: " + artifactFilter.getClass().getCanonicalName());
        CollectingDependencyNodeVisitor dependencyNodeVisitor = new CollectingDependencyNodeVisitor();
        if (dependencyGraphBuilder != null) {
            getLog().info("Got dependency graph builder class: " + dependencyGraphBuilder.getClass().getCanonicalName());

            try {
                DependencyNode depNode = dependencyGraphBuilder.buildDependencyGraph(mavenProject, artifactFilter);
                depNode.accept(dependencyNodeVisitor);
                List<DependencyNode> nodes = dependencyNodeVisitor.getNodes();
                getLog().info("Listing the dependencies");
                for (DependencyNode node : nodes) {
                    getLog().info(String.format("groupId: %s artifactId: %s version %s", node.getArtifact().getGroupId(), node.getArtifact().getArtifactId(), node.getArtifact().getVersion()));
                }
                getLog().info("End of listing the dependencies");

            } catch (DependencyGraphBuilderException ex) {
                getLog().error("Got an error when building dependency graph: " + ex.getMessage());
            }
        } else {
            getLog().warn("Dependency graph builder is null");
        }
        /*
         * jcabi Aether from Sonatype requires lots of dependencies
         * I think it should be removed in future.
         */
//        try {
//            File repo = repositorySystemSession.getLocalRepository().getBasedir();
//            Aether a = new Aether(mavenProject, repo);
//            List<Artifact> artifacts = a.resolve(new DefaultArtifact(groupId, artifactId, "", version), JavaScopes.RUNTIME);
//        } catch (DependencyResolutionException ex) {
//            getLog().error("Cannot resolve artifacts: " + ex.getMessage());
//        }
    }
}
