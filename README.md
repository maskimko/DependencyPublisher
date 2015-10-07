# DependencyPublisher
A simple Maven plugin to push all of the resolved dependencies into the artifact storage like Sonatype Nexus
Curently Nexus is only supported artifact storage.


Basic usage: 
Add the following to your pom.xml file
<build>
<plugins>
....
    <plugin>
                <groupId>ua.pp.msk.maven</groupId>
                <artifactId>dependency-publish-maven-plugin</artifactId>
                <version>0.7</version>
                <executions>
                    <execution>
                        <phase>install</phase>
                        <goals>
                            <goal>publish</goal>
                        </goals>
                        <configuration>
                            <url>....http://<host>[:port]/nexus/service/local/artifact/maven/content....</url>
                            <username>...</username>
                            <password>...</password>
                            <repositoryId>...Nexus repository id...</repositoryId>
                            <promote>...true...</promote>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
      ...
  </plugins>
</build>
