/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ua.pp.msk.maven.exceptions;

/**
 *
 * @author Maksym Shkolnyi aka maskimko
 */
public class ArtifactPromotingException extends Exception{

    public ArtifactPromotingException() {
    }

    public ArtifactPromotingException(String string) {
        super(string);
    }

    public ArtifactPromotingException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ArtifactPromotingException(Throwable thrwbl) {
        super(thrwbl);
    }

}
