/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven;

import org.apache.maven.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResolutionException;

/**
 *
 * @author maskimko
 */
public interface PublishResolver {
    
    Artifact resolve(Artifact artifact) throws ArtifactResolutionException;
    
}
