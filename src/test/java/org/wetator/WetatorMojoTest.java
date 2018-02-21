package org.wetator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class WetatorMojoTest {

    /**
     * Unit test for the method that scans a directory for wetator test files.
     * <p>
     * Wetator supports different file formats for their test files which can be
     * found here: https://www.wetator.org/wetator-documentation/test-case-files
     * <p>
     * This method checks whether the matching works correctly and all valid test
     * files are found while the invalid ones are ignored.
     */
    @Test
    public void scanForWetatorTestFiles_DefaultPatterns() {
        // ASSEMBLE
        final String testDirectory = "src/test/resources";
        WetatorMojo wetatorMojo = new WetatorMojo();

        // ACT
        String[] foundTestFiles = wetatorMojo.scanForWetatorTestFiles(testDirectory, WetatorMojo.DEFAULT_INCLUDE_PATTERN, WetatorMojo.DEFAULT_EXCLUDE_PATTERN);
        List<String> foundTestFilesList = Arrays.asList(foundTestFiles);

        // ASSERT
        System.out.println(foundTestFilesList);
        assertTrue(foundTestFilesList.contains("Valid1.wet"));
        assertTrue(foundTestFilesList.contains("Valid2.wett"));
        assertTrue(foundTestFilesList.contains("Valid3.xls"));
        assertTrue(foundTestFilesList.contains("subFolder" + File.separator + "Valid4.wet"));
        assertEquals(4, foundTestFilesList.size());
    }

    /**
     * Unit test for the method that scans a directory for wetator test files using
     * custom include and exclude patterns.
     */
    @Test
    public void scanForWetatorTestFiles_OnlyWetAndExcludeSubmodule() {
        // ASSEMBLE
        final String testDirectory = "src/test/resources";
        WetatorMojo wetatorMojo = new WetatorMojo();

        // ACT
        String[] foundTestFiles = wetatorMojo.scanForWetatorTestFiles(testDirectory, new String[] { "**/*.wet" }, new String[] { "**/subFolder/" });
        List<String> foundTestFilesList = Arrays.asList(foundTestFiles);

        // ASSERT
        System.out.println(foundTestFilesList);
        assertTrue(foundTestFilesList.contains("Valid1.wet"));
        assertEquals(1, foundTestFilesList.size());
    }

}
