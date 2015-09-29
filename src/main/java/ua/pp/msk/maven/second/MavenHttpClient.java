/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven.second;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Maksym Shkolnyi aka maskimko
 */
public class MavenHttpClient {

    private String url;
    private String userAgent = "Maven Dependency pushing plugin";
    private List<NameValuePair> urlParams = new ArrayList<NameValuePair>();
    private Log log;
    private String repository;

    public MavenHttpClient(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void addUrlParameter(String key, String value) {
        urlParams.add(new BasicNameValuePair(key, value));
    }

    public void addUrlParameters(Map<String, String> params) {
        for (Map.Entry<String, String> me : params.entrySet()) {
            addUrlParameter(me.getKey(), me.getValue());
        }
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public void execute() {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        try {
            HttpPost post = new HttpPost(url);

            post.setHeader("User-Agent", userAgent);

            if (!urlParams.isEmpty()) {
                post.setEntity(new UrlEncodedFormEntity(urlParams));
            }
            CloseableHttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            getLog().info("Response status code: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
//Perhaps I need to parse html           
//String html = EntityUtils.toString(entity);

        } catch (UnsupportedEncodingException ex) {
            if (log != null) {
                getLog().error(ex.getMessage());
            }
        } catch (IOException ex) {
            getLog().error(ex.getMessage());
        }
    }
    
    public void promote(Artifact artifact){
        File af = artifact.getFile();
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        if( repository == null || repository.length() == 0) {
            getLog().error("Repository cannot be null value");
            //TODO throw exception here
            return;
        } else {        addUrlParameter("r", repository); }
        addUrlParameter("hasPom", "false");
        String[] splitName = af.getName().toLowerCase().split(".");
                String extension = splitName[splitName.length-1];
                if (!extension.equals("jar") || !extension.equals("war") ||  !extension.equals("ear")){
                    getLog().error(extension + " isnot supported. Currently only jar, war, ear file extensions are supported");
                    //TODO throw exception here
                    return;
                }
                addUrlParameter("e", extension);
                addUrlParameter("g", groupId);
                addUrlParameter("a", artifactId);
                addUrlParameter("v", version);
                //Packaging should be 
                addUrlParameter("p", extension);
                //AUTHENTICATE here
    }

}
