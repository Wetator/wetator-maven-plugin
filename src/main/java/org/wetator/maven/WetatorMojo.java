package org.wetator.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.wetator.core.IProgressListener;
import org.wetator.core.WetatorConfiguration;
import org.wetator.core.WetatorEngine;
import org.wetator.progresslistener.StdOutProgressListener;

/**
 * This is the main Mojo for executing wetator tests.
 */
@Mojo(name = "execute", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class WetatorMojo extends AbstractMojo {

    private static final String WET_FILE_PATTERN = "**\\*.wet";
    private static final String WETT_FILE_PATTERN = "**\\*.wett";
    private static final String XLS_FILE_PATTERN = "**\\*.xls";
    private static final String XLSX_FILE_PATTERN = "**\\*.xlsx";

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    /**
     * File patterns for the wetator test files.
     */
    static final String[] DEFAULT_INCLUDE_PATTERN = new String[] { WET_FILE_PATTERN, WETT_FILE_PATTERN, XLS_FILE_PATTERN, XLSX_FILE_PATTERN };
    static final String[] DEFAULT_EXCLUDE_PATTERN = new String[] {};

    /**
     * Path with filename to the config file in file system.
     */
    @Parameter(property = "execute.configFile")
    private String configFile;

    /**
     * Directory where the test files resides.
     */
    @Parameter(property = "execute.testFileDir")
    private String testFileDir;

    /**
     * Filename patterns for the test files that shall be included.
     * <p>
     * Per default (when this is left empty) the following patterns are included:
     * {@value #WET_FILE_PATTERN}, {@value #WETT_FILE_PATTERN},
     * {@value #XLS_FILE_PATTERN}, {@value #XLSX_FILE_PATTERN}
     */
    @Parameter
    private String[] includes;

    /**
     * Filename patterns for the test files that shall be excluded.
     * <p>
     * Per default nothing is excluded.
     */
    @Parameter
    private String[] excludes;

    /**
     * Can be used to include additional artifacts/directories to the classpath as
     * dependencies of this plugin.
     * <p>
     * <dl>
     * <dt>(leave empty>
     * <dd>no additional artifacts or directories added to the classpath</dd>
     * <dt>runtime</dt>
     * <dd>all elements from the runtime classpath of the project</dd>
     * <dt>compile</dt>
     * <dd>all elements from the compile classpath of the project</dd>
     * <dt>test</dt>
     * <dd>all elements from the test classpath of the project</dd>
     * </dl>
     */
    @Parameter
    private String additionalClasspathScope;

    /**
     * The URL to the website under test.
     * <p>
     * Overwrites the wetator.baseUrl parameter from the wetator.config file!
     */
    @Parameter(property = "execute.baseUrl")
    private String baseUrl;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        includeAdditionalDependenciesInClasspath(additionalClasspathScope);

        // Need to set the default values of array/list parameter manually as this is
        // not possible via the annotations.
        // See:
        // https://stackoverflow.com/questions/1659087/how-to-configure-defaults-for-a-parameter-with-multiple-values-for-a-maven-plugi
        if (includes == null || includes.length == 0) {
            includes = DEFAULT_INCLUDE_PATTERN;
        }
        if (excludes == null || excludes.length == 0) {
            excludes = DEFAULT_EXCLUDE_PATTERN;
        }

        // this map contains the configuration parameter for the wetator engine that
        // overwrites the parameter from the configuration file
        Map<String, String> externalConfigurations = new TreeMap<>();
        if (baseUrl != null) {
            externalConfigurations.put(WetatorConfiguration.PROPERTY_BASE_URL, baseUrl);
        }

        getLog().info("start running wetator tests ...");
        getLog().info("using config file: " + configFile);
        getLog().info("using wetator test file directory: " + testFileDir);
        getLog().info("using include pattern: " + Arrays.toString(includes));
        getLog().info("using exclude pattern: " + Arrays.toString(excludes));
        getLog().info("using base URL: " + externalConfigurations.get(WetatorConfiguration.PROPERTY_BASE_URL));

        final WetatorEngine wetatorEngine = new WetatorEngine();
        try {
            wetatorEngine.setConfigFileName(configFile);
            wetatorEngine.setExternalProperties(externalConfigurations);

            final IProgressListener tmpProgressListener = new StdOutProgressListener();
            wetatorEngine.addProgressListener(tmpProgressListener);

            wetatorEngine.init();

            final String[] wetatorTestFilenames = scanForWetatorTestFiles(testFileDir, includes, excludes);
            getLog().info(wetatorTestFilenames.length + " test files were found!");
            for (String weatorTestFilename : wetatorTestFilenames) {
                getLog().info("adding test file: " + weatorTestFilename);
                wetatorEngine.addTestCase(weatorTestFilename, new File(testFileDir, weatorTestFilename));
            }

            getLog().info("Executing tests...");

            wetatorEngine.executeTests();

            getLog().info("find wetator test results in: " + wetatorEngine.getConfiguration().getOutputDir().getCanonicalPath());
            getLog().info("wetator test execution complete!");

        } catch (final Exception e) {
            System.out.println("Wetator execution failed: " + e.getMessage());
            getLog().error("Wetator execution failed:", e);

            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            wetatorEngine.shutdown();
        }

    }

    /**
     * Adds all classpath elements from the given scope to the classpath of this
     * plugin.
     *
     * @param aClasspathScope
     *            scope
     */
    private void includeAdditionalDependenciesInClasspath(String aClasspathScope) {
        if (aClasspathScope == null || aClasspathScope.length() == 0) {
            return;
        }

        try {
            switch (aClasspathScope) {
            case Artifact.SCOPE_RUNTIME: {
                addElementsToClasspath(project.getRuntimeClasspathElements());
                break;
            }
            case Artifact.SCOPE_COMPILE: {
                addElementsToClasspath(project.getCompileClasspathElements());
                break;
            }
            case Artifact.SCOPE_TEST: {
                addElementsToClasspath(project.getTestClasspathElements());
                break;
            }
            default: {
                throw new IllegalArgumentException("You passed the following invalid value as a classpath scope: '" + aClasspathScope + "'. Only values that are allowed are: "
                        + Artifact.SCOPE_RUNTIME + ", " + Artifact.SCOPE_COMPILE + ", " + Artifact.SCOPE_TEST);
            }
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalStateException("Have you forgotten to annotate the Mojo with 'requiresDependencyResolution'?", e);
        }

    }

    /**
     * Treats the given list of strings as paths to artifacts or directories and
     * tries to add those to the classpath of this plugin.
     *
     * @param someDependencyPaths
     *            paths to artifacts (.jar files) or directories (with .class files)
     */
    private void addElementsToClasspath(List<String> someDependencyPaths) {
        someDependencyPaths.forEach(element -> {
            try {
                URL tmpUrl = new File(element).toURI().toURL();
                pluginDescriptor.getClassRealm().addURL(tmpUrl);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot convert the given path " + element + " to a valid URL.", e);
            }
        });
    }

    /**
     * Scans the given directory and finds all files that match the given include
     * and exclude patterns.
     *
     * @param pTestFileDir
     *            directory of the test files
     * @param pIncludePattern
     *            pattern of files that shall be included
     * @param pExcludePattern
     *            pattern of files that shall be excluded
     * @param includeFilePattern
     *            filename pattern
     * @return the name of all test files that were found
     */
    String[] scanForWetatorTestFiles(String pTestFileDir, String[] pIncludePattern, String[] pExcludePattern) {
        final DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir(pTestFileDir);
        directoryScanner.setIncludes(pIncludePattern);
        directoryScanner.setExcludes(pExcludePattern);
        directoryScanner.scan();

        return directoryScanner.getIncludedFiles();
    }

    public String getAdditionalClasspathScope() {
        return additionalClasspathScope;
    }

    public void setAdditionalClasspathScope(String aAdditionalClasspathScope) {
        additionalClasspathScope = aAdditionalClasspathScope;
    }
}
