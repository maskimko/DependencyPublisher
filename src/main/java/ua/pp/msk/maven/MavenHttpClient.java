/*
 * Copyright 2015 Maksym Shkolnyi (aka maskimko)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package ua.pp.msk.maven;

import edu.emory.mathcs.backport.java.util.Arrays;
import ua.pp.msk.maven.exceptions.ArtifactPromotingException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthenticationException;
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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

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
	private ArtifactRepository artifactRepository;

	public MavenHttpClient(String url, ArtifactRepository artifactRepository) {
		this.url = url;
		this.artifactRepository = artifactRepository;
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
		for (NameValuePair bsv: urlParams){
			if (bsv.getName().equals(key)){
				urlParams.remove(bsv);
			}
		}
		urlParams.add(new BasicNameValuePair(key, value));
	}

	public void addUrlParameters(Map<String, String> params) {
		for (Map.Entry<String, String> me : params.entrySet()) {
			addUrlParameter(me.getKey(), me.getValue());
		}
	}

	public ArtifactRepository getArtifactRepository() {
		return artifactRepository;
	}

	public void setArtifactRepository(ArtifactRepository artifactRepository) {
		this.artifactRepository = artifactRepository;
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
				// Not used because of form submission
				// builder.addBinaryBody("file", file,
				// ContentType.DEFAULT_BINARY, file.getName());
				builder.addPart("file", fb);
				HttpEntity sendEntity = builder.build();
				post.setEntity(sendEntity);
			}

			CloseableHttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			StatusLine statusLine = response.getStatusLine();
			status = statusLine.getStatusCode();
			getLog().info("Response status code: " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			// Perhaps I need to parse html
			// String html = EntityUtils.toString(entity);

		} catch (AuthenticationException ex) {
			throw new ArtifactPromotingException(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new ArtifactPromotingException(ex);
		} catch (IOException ex) {
			throw new ArtifactPromotingException(ex);
		} finally {
			try {
				client.close();
			} catch (IOException ex) {
				throw new ArtifactPromotingException("Cannot close http client", ex);
			}
		}
		return status;
	}

	private int execute() throws ArtifactPromotingException {
		return execute(null);
	}

	public void promote(Artifact artifact) throws ArtifactPromotingException {
		// Example of url
		// http://localhost:8081/nexus/service/local/artifact/maven/content
		List<File> artifactFiles = new LinkedList<File>();
		if (artifact.getFile() != null) {
			artifactFiles.add(artifact.getFile());
		}

		final String groupId = artifact.getGroupId();
		String[] groupIdComponents = groupId.split("\\.");
		final String artifactId = artifact.getArtifactId();
		final String version = artifact.getVersion();
		if (artifactFiles.isEmpty()) {
			StringBuilder sb = new StringBuilder(artifactRepository.getBasedir());
			for (int i = 0; i < groupIdComponents.length; i++) {
				sb.append(File.separator);
				sb.append(groupIdComponents[i]);
			}
			sb.append(File.separator);
			sb.append(artifactId);
			sb.append(File.separator);
			sb.append(version);
			File directory = new File(sb.toString());
			if (directory.isDirectory()) {
				File[] artifacts = directory.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						if (name.endsWith("jar") || name.endsWith("war") || name.endsWith("ear")) {
							if (name.contains(version)) {
								if (name.startsWith(artifactId))
									return true;
							}
						}
						return false;
					}
				});
				artifactFiles.addAll(Arrays.asList(artifacts));
			} else {
				throw new ArtifactPromotingException(String.format(
						"Artifact file is null of %s artifact. Cannot locate it in the %s directory, because it is not a directory"));
			}

		}
		if (repository == null || repository.length() == 0) {
			getLog().error("Repository cannot be null value");
			throw new ArtifactPromotingException("Repository cannot be null value");
		} else {
			addUrlParameter("r", repository);
		}
		addUrlParameter("hasPom", "false");

		
		addUrlParameter("g", groupId);
		addUrlParameter("a", artifactId);
		addUrlParameter("v", version);
		// Packaging should be
		for (File af : artifactFiles) {
			String fileName = af.getName();
			getLog().debug("Processing artifact file " + fileName);
			String[] splitName = fileName.toLowerCase().split("\\.");
			getLog().debug("Split name: " + Arrays.toString(splitName));
			String extension = splitName[splitName.length - 1].trim();
			if (!extension.equals("jar") && !extension.equals("war") && !extension.equals("ear")) {
				throw new ArtifactPromotingException(
						extension + " is not supported. Currently only jar, war, ear file extensions are supported");
			}
			addUrlParameter("p", extension);
			addUrlParameter("e", extension);
			int ec = execute(af);
			if (ec == HttpStatus.SC_OK || ec == HttpStatus.SC_CREATED) {
				if (log != null) {
					getLog().info("Artifact has been promoted successfully");
				}
			} else if (log != null) {
				throw new ArtifactPromotingException("Artifact promotion failed");
			}
		}
	}

}
