/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.pp.msk.maven;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.maven.artifact.repository.ArtifactRepository;
import ua.pp.msk.maven.exceptions.ArtifactPromotingException;

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
    private String username;
    private String password;

    public MavenHttpClient(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url.contains("/nexus/service/local/artifact/maven/content")) {
            this.url = url;
        } else {
            this.url = url.concat("/nexus/service/local/artifact/maven/content");
        }
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private int execute(File file) throws ArtifactPromotingException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        int status = -1;
        try {
            getLog().debug("Connecting to URL: " + url);
            HttpPost post = new HttpPost(url);

            post.setHeader("User-Agent", userAgent);

            if (username != null && username.length() != 0 && password != null) {
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
                post.addHeader(new BasicScheme().authenticate(creds, post, null));
            }
            if (file == null) {
                if (!urlParams.isEmpty()) {
                    post.setEntity(new UrlEncodedFormEntity(urlParams));
                }
            } else {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                if (!urlParams.isEmpty()) {
                    for (NameValuePair nvp : urlParams) {
                        builder.addPart(nvp.getName(), new StringBody(nvp.getValue(), ContentType.MULTIPART_FORM_DATA));
                    }
                }
                FileBody fb = new FileBody(file);
                //Not used because of form submission
                //builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
                builder.addPart("file", fb);
                HttpEntity sendEntity = builder.build();
                post.setEntity(sendEntity);
            }

            CloseableHttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            status = statusLine.getStatusCode();
            getLog().info("Response status code: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
//Perhaps I need to parse html           
//String html = EntityUtils.toString(entity);

        } catch (Exception ex) {
            throw new ArtifactPromotingException(ex);
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
                throw new ArtifactPromotingException("Cannot close the http client", ex);
            }
        }
        return status;
    }

    private int execute() throws ArtifactPromotingException {
        return execute(null);
    }

    public void promote(Artifact artifact) throws ArtifactPromotingException {
        //Example of url http://localhost:8081/nexus/service/local/artifact/maven/content
        if (log != null) {
            getLog().debug("Trying to promote the artifact...");
        }
        File af = artifact.getFile();

        if (af == null) {
            if (log != null) {
                getLog().debug("Trying to get the artifact repository...");
            }
            ArtifactRepository ar = artifact.getRepository();
            if (ar != null) {
                String pathOfArtifact = ar.pathOf(artifact);
                getLog().debug("Path of artifact is " + pathOfArtifact);
                af = Paths.get(pathOfArtifact).toFile();
                if (af == null) {
                    throw new ArtifactPromotingException("Artifact " + artifact.getArtifactId() + " has no file");
                }
            } else {
                throw new ArtifactPromotingException("It is impossible to get the filesystem location of artiifact, because artifact repository field is null value");
            }

        } else if (log != null) {
            getLog().debug("Artifact file " + af.getAbsolutePath());
        }
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        if (repository == null || repository.length() == 0) {
            throw new ArtifactPromotingException("Repository cannot be null value");
        } else {
            addUrlParameter("r", repository);
        }
        addUrlParameter("hasPom", "false");
        String fileName = af.getName();
        getLog().debug("Processing artifact file " + fileName);
        String[] splitName = fileName.toLowerCase().split("\\.");
        getLog().debug("Split name: " + Arrays.toString(splitName));
        String extension = splitName[splitName.length - 1].trim();
        if (!extension.equals("jar") && !extension.equals("war") && !extension.equals("ear")) {
            throw new ArtifactPromotingException(extension + " is not supported. Currently only jar, war, ear file extensions are supported");
        }
        addUrlParameter("e", extension);
        addUrlParameter("g", groupId);
        addUrlParameter("a", artifactId);
        addUrlParameter("v", version);
        //Packaging should be 
        addUrlParameter("p", extension);
        int ec = execute(af);
        switch (ec) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
                if (log != null) {
                    getLog().info("Artifact has been promoted successfully");
                }
                break;
            case HttpStatus.SC_BAD_REQUEST:
                throw new ArtifactPromotingException("Bad request. Perhaps artifact already exists. Artifact promotion failed");
            default:
                throw new ArtifactPromotingException("Artifact promotion failed");
        }

    }

}
