# DependencyPublisher
A simple Maven plugin to push all of the resolved dependencies into the artifact storage like Sonatype Nexus
Curently Nexus is only supported artifact storage.


Basic usage: 
As an example add the following to your pom.xml file
<pre>
<code>
&lt;build&gt;
&nbsp;&lt;plugins&gt;
&nbsp;&nbsp;&lt;plugin&gt;
&nbsp;&nbsp;&nbsp;&lt;groupId&gt;ua.pp.msk.maven&lt;/groupId&gt;
&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;dependency-publish-maven-plugin&lt;/artifactId&gt;
&nbsp;&nbsp;&nbsp;&lt;version&gt;0.7&lt;/version&gt;
&nbsp;&nbsp;&nbsp;&lt;executions&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;execution&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;phase&gt;install&lt;/phase&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;goals&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;goal&gt;publish&lt;/goal&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/goals&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;configuration&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url&gt;....http://&lt;host&gt;[:port]/nexus/service/local/artifact/maven/content....&lt;/url&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;username&gt;...&lt;/username&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;password&gt;...&lt;/password&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;repositoryId&gt;...Nexus repository id...&lt;/repositoryId&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;promote&gt;...true...&lt;/promote&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/configuration&gt;
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/execution&gt;
&nbsp;&nbsp;&nbsp;&lt;/executions&gt;
&nbsp;&nbsp;&lt;/plugin&gt;
&nbsp;&lt;/plugins&gt;
&lt;/build&gt;
</code>
</pre>

Copyright 2015 Maksym Shkolnyi (aka maskimko)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
