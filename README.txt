DL JAVA CORE DYNAMIC WEB
------------------------
This project contains the core elements to deploy dynamic web services
in java using a standard java application.  The current list of dependencies
includin in this project include:

LIB: Embedded Tomcat (java servlet container server)
LIB: Freemarker (HTMP template engine)
RES: Twitter Bootstrap (CSS framework)
RES: JQuery (javascript framework)

Versions and details of how to fetch them follow.

==== EMBEDDED TOMCAT ====
Version: v9.0.16
Updating: External MAVEN download (see MAVEN dependeny in APPENDIX below)
Notes: Includes only subset of Tomcat, defined by maven
License: Released under APACHE 2.0

==== FREEMARKER ====
Version: v2.3.28
Updating: Download from https://freemarker.apache.org/
Notes: Self contained JAR with no dependencies
License: Released under APACHE 2.0

==== TWITTER BOOTSTRAP ====
Version: v3.0.3
Updating: Download from https://getbootstrap.com/
Notes: Older version.  Using bootstrap css/js and bootstrap-datepicker.
License: Released under APACHE 2.0

==== JQUERY ====
Version: v1.12.4
Updating: Download from https://jquery.com/
Notes: Older version.  Using base, confirm, loadtemplate, treetable
License: Released under MIT License


===============================
APPENDIX
===============================
HOW TO UPDATE FROM MAVEN

CREATE pom.xml with contents (updating version as needed):

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.heroku.sample</groupId>
  <artifactId>embeddedTomcatSample</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
  <name>embeddedTomcatSample Maven Webapp</name>
  <url>http://maven.apache.org</url>
  <properties>
    <tomcat.version>9.0.16</tomcat.version>
  </properties>
  <dependencies>
    <dependency>
        <groupId>org.apache.tomcat.embed</groupId>
        <artifactId>tomcat-embed-core</artifactId>
        <version>${tomcat.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.tomcat.embed</groupId>
        <artifactId>tomcat-embed-jasper</artifactId>
        <version>${tomcat.version}</version>
    </dependency>
  </dependencies>
  <build>
    <finalName>embeddedTomcatSample</finalName>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>appassembler-maven-plugin</artifactId>
            <version>2.0.0</version>
            <configuration>
                <assembleDirectory>target</assembleDirectory>
                <programs>
                    <program>
                        <mainClass>launch.Main</mainClass>
                        <name>webapp</name>
                    </program>
                </programs>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>assemble</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
  </build>
</project>

RUN MAVEN TO GET DEPENDENT LIBS

Run this command in the directory with the pom.xml above to have maven fetch
all of this projects dependencies (e.g. tomcat)

mvn dependency:copy-dependencies

COPY LIBS FROM target/dependency/*

