wetator-maven-plugin
====================

This is a Maven plugin for executing Wetator tests.

You can find the Wetator homepage at [http://www.wetator.org/](http://www.wetator.org/).

## Build Status ##
[![Build Status] TODO


## Usage ##
To use the plugin you have to add this to your Maven pom.xml plugins section:

    <plugin>
        <groupId>org.wetator.maven</groupId>
        <artifactId>wetator-maven-plugin</artifactId>
        <version>1.0.3</version>
        <configuration>
            <configFile>src/test/resources/wetator.config</configFile>
            <testFileDir>src/test/resources/wetator</testFileDir>
            <includePattern>
              <includePattern>**/*.wet</includePattern>
              <includePattern>**/*.wett</includePattern>
            </includePattern>
            <excludePattern>
              <excludePattern>**/modules/**</excludePattern>
            </excludePattern>
        </configuration>
    </plugin>

By default the plugin includes all \*.wet, \*.wett, \*.xls, \*.xlsx files within the 'testFileDir' folder.

To run the tests run:

    mvn org.wetator.maven:wetator-maven-plugin:execute

## Remarks ##
The wetator-maven-plugin is using the Wetator dependency:

    <dependency>
        <groupId>org.wetator</groupId>
        <artifactId>wetator</artifactId>
        <version>1.8.0</version>
    </dependency>
