# wetator-maven-plugin

Version 4.21.0 / January 18, 2026

This is a Maven plugin for executing Wetator tests.

You can find the Wetator homepage at [http://www.wetator.org](http://www.wetator.org).

:heart: [Sponsor](https://github.com/sponsors/rbri)

## News

[HtmlUnit@Twitter](https://twitter.com/HtmlUnit)

## Build Status
[![Build Status](https://travis-ci.org/Wetator/wetator-maven-plugin.svg?branch=master)](https://travis-ci.org/Wetator/wetator-maven-plugin) [![Coverage Status](https://coveralls.io/repos/github/Wetator/wetator-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/Wetator/wetator-maven-plugin?branch=master)


## Usage
To use the plugin you have to add to the `<build><plugins>` section of your `pom.xml`. See the sample code below to have the wetator tests run during the `integration-test` phase.

```xml
<plugin>
    <groupId>org.wetator.maven</groupId>
    <artifactId>wetator-maven-plugin</artifactId>
    <version>4.21.0</version>
    <configuration>
        <configFile>src/test/resources/wetator.config</configFile>
        <testFileDir>src/test/resources/wetator</testFileDir>
        <includes>
            <include>**/*.wet</include>
            <include>**/*.wett</include>
        </includes>
        <excludes>
            <exclude>**/modules/**</exclude>
        </excludes>
    </configuration>
    <executions>
        <execution>
            <phase>integration-test</phase>
            <goals>
                <goal>execute</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

By default the plugin includes all \*.wet, \*.wett, \*.xls, \*.xlsx files within the 'testFileDir' folder.

To run the tests manually use:

```bash
mvn org.wetator.maven:wetator-maven-plugin:execute
```

## Remarks
The wetator-maven-plugin is using the Wetator dependency:

```xml
<dependency>
    <groupId>org.wetator</groupId>
    <artifactId>wetator</artifactId>
    <version>4.21.0</version>
</dependency>
```

## Release notes
### 4.21.0
* use Wetator 4.21.0

### 3.11.0
* use Wetator 3.11.0

### 3.5.0
* use Wetator 3.5.0

### 2.4.0
* use Wetator 2.4.0

### 2.3.0
* use Wetator 2.3.0

### 2.2.0
* use Wetator 2.2.0

### 2.1.0
* use Wetator 2.1.0

### 1.17.0
* moved to github
* use Wetator 1.17.0

### 1.1.5
* use Wetator 1.10.0
