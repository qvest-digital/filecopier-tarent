/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.fhnw.filecopier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ronny
 */
public class FileCopierTest {

    private FileCopier fileCopier;
    private File sourceDir;
    private File destinationDir;

    /**
     * sets up some things before a test runs
     */
    @Before
    public void setUp() {
        // create a copier instance
        fileCopier = new FileCopier();

        // create all test directories
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        sourceDir = new File(tmpDir, "testSourceDir");
        if (!sourceDir.exists() && !sourceDir.mkdir()) {
            fail("could not create source dir " + sourceDir);
        }
        destinationDir = new File(tmpDir, "testDestinationDir");
        if (!destinationDir.exists() && !destinationDir.mkdir()) {
            fail("could not create destination dir " + destinationDir);
        }
    }

    /**
     * test, if we correctly overwrite a single file if it is the second job
     * @throws Exception if an exception occurs
     */
    @Test
    public void testOverwriteSingleFileAsSecondJob() throws Exception {
        // create a single source file with some content
        String content = "We can overwrite single files.";
        File singleFile = new File(sourceDir, "singleFile");
        try {
            if (!singleFile.createNewFile()) {
                fail("could not create test file " + singleFile);
            }
            FileWriter fileWriter = new FileWriter(singleFile);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + singleFile);
            throw ex;
        }

        // create first destination file
        File existingDestinationFile1 =
                new File(destinationDir, "existingFile1");
        try {
            if (!existingDestinationFile1.createNewFile()) {
                fail("could not create test file " + existingDestinationFile1);
            }
            FileWriter fileWriter = new FileWriter(existingDestinationFile1);
            fileWriter.write("something completely different");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + existingDestinationFile1);
            throw ex;
        }

        // create second destination file
        File existingDestinationFile2 =
                new File(destinationDir, "existingFile2");
        try {
            if (!existingDestinationFile2.createNewFile()) {
                fail("could not create test file " + existingDestinationFile2);
            }
            FileWriter fileWriter = new FileWriter(existingDestinationFile2);
            fileWriter.write("and even more different...");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + existingDestinationFile2);
            throw ex;
        }

        // try copying the test files
        CopyJob copyJob1 = new CopyJob(false,
                existingDestinationFile1.getPath(), singleFile.getPath());
        CopyJob copyJob2 = new CopyJob(false,
                existingDestinationFile2.getPath(), singleFile.getPath());
        fileCopier.copy(copyJob1, copyJob2);

        // check
        File expected = new File(destinationDir, "existingFile2");
        boolean exists = expected.exists();
        boolean isFile = expected.isFile();
        BufferedReader bufferedReader =
                new BufferedReader(new FileReader(expected));
        String line = bufferedReader.readLine();
        boolean contentMatch = content.equals(line);

        // cleanup
        if (!singleFile.delete()) {
            fail("could not delete source file " + singleFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!existingDestinationFile1.delete()) {
            fail("could not delete destination file " +
                    existingDestinationFile1);
        }
        if (!expected.delete()) {
            fail("could not delete destination file " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no file", isFile);
        assertTrue("the file was not copied", contentMatch);
    }

    /**
     * test, if we correctly overwrite a single file
     * @throws Exception if an exception occurs
     */
    @Test
    public void testOverwriteSingleFile() throws Exception {
        // create a single source file with some content
        String content = "We can overwrite single files.";
        File singleFile = new File(sourceDir, "singleFile");
        try {
            if (!singleFile.createNewFile()) {
                fail("could not create test file " + singleFile);
            }
            FileWriter fileWriter = new FileWriter(singleFile);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + singleFile);
            throw ex;
        }

        // create a single destination file
        File existingDestinationFile = new File(destinationDir, "existingFile");
        try {
            if (!existingDestinationFile.createNewFile()) {
                fail("could not create test file " + existingDestinationFile);
            }
            FileWriter fileWriter = new FileWriter(existingDestinationFile);
            fileWriter.write("something completely different");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + existingDestinationFile);
            throw ex;
        }

        // try copying the test file
        CopyJob copyJob = new CopyJob(
                false, existingDestinationFile.getPath(), singleFile.getPath());
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, "existingFile");
        boolean exists = expected.exists();
        boolean isFile = expected.isFile();
        BufferedReader bufferedReader =
                new BufferedReader(new FileReader(expected));
        String line = bufferedReader.readLine();
        boolean contentMatch = content.equals(line);

        // cleanup
        if (!singleFile.delete()) {
            fail("could not delete source file " + singleFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expected.delete()) {
            fail("could not delete destination file " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no file", isFile);
        assertTrue("the file was not copied", contentMatch);
    }

    /**
     * test, if we correctly copy&rename a single file
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyAndRename() throws Exception {
        // create a single file
        File singleFile = new File(sourceDir, "singleFile");
        try {
            if (!singleFile.createNewFile()) {
                fail("could not create test file " + singleFile);
            }
        } catch (IOException ex) {
            System.out.println("Could not create " + singleFile);
            throw ex;
        }

        // try copying the test file
        final String NEW_NAME = "newName";
        CopyJob copyJob = new CopyJob(false,
                destinationDir.getPath() + File.separatorChar + NEW_NAME,
                singleFile.getPath());
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, NEW_NAME);
        boolean exists = expected.exists();
        boolean isFile = expected.isFile();

        // cleanup
        if (!singleFile.delete()) {
            fail("could not delete source file " + singleFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expected.delete()) {
            fail("could not delete destination file " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no file", isFile);

    }

    /**
     * test, if we correctly copy an empty directory (recursively)
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyingEmptySourceDirRecursive() throws Exception {

        // try copying the empty directory
        CopyJob copyJob = new CopyJob(
                true, destinationDir.getPath(), sourceDir.getPath());
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, sourceDir.getName());
        boolean exists = expected.exists();
        boolean isDirectory = expected.isDirectory();

        // cleanup
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expected.delete()) {
            fail("could not delete destination dir " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no directory", isDirectory);
    }

    /**
     * test, if we correctly copy an empty directory (recursively)
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyingEmptySourceDirNonRecursive() throws Exception {
        // try copying the empty directory
        CopyJob copyJob = new CopyJob(
                false, destinationDir.getPath(), sourceDir.getPath());
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, sourceDir.getName());
        boolean exists = expected.exists();

        // cleanup
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertFalse("destination was created", exists);
    }

    /**
     * test, if we correctly copy a single file to a target directory
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyingSingleFile() throws Exception {
        // should work identically in both the recursive and non-recursive case
        testSingleFile(true/*recursive*/);
        setUp();
        testSingleFile(false/*recursive*/);
    }

    private void testSingleFile(boolean recursive) throws IOException {
        // create a single file
        File singleFile = new File(sourceDir, "singleFile");
        try {
            if (!singleFile.createNewFile()) {
                fail("could not create test file " + singleFile);
            }
        } catch (IOException ex) {
            System.out.println("Could not create " + singleFile);
            throw ex;
        }

        // try copying the test file
        CopyJob copyJob = new CopyJob(
                recursive, destinationDir.getPath(), singleFile.getPath());
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, singleFile.getName());
        boolean exists = expected.exists();
        boolean isFile = expected.isFile();

        // cleanup
        if (!singleFile.delete()) {
            fail("could not delete source file " + singleFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expected.delete()) {
            fail("could not delete destination file " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no file", isFile);

    }

    /**
     * test, if we correctly handle the situation of copying an empty directory
     * to an existing file
     * (do nothing, print warning)
     * @throws IOException if an I/O exception occurs
     */
    @Test
    public void testDir2File() throws IOException {
        File destinationFile = new File(destinationDir, "destinationFile");
        if (!destinationFile.createNewFile()) {
            fail("could not create test file " + destinationFile);
        }
        try {
            // try copying the test file
            CopyJob copyJob = new CopyJob(
                    true, destinationFile.getPath(), sourceDir.getPath());
            fileCopier.copy(copyJob);
            fail("this test must throw an exception");
        } catch (IOException ex) {
            ex.printStackTrace();
        // yes, we want an exception here!
        }

        // test
        destinationFile = new File(destinationDir, "destinationFile");
        boolean exists = destinationFile.exists();
        boolean isFile = destinationFile.isFile();

        // cleanup
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!destinationFile.delete()) {
            fail("could not delete destination file " + destinationFile);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no file", isFile);
    }

    /**
     * test, if we correctly handle the situation of copying a full directory
     * to an existing directory
     * @throws IOException if an I/O exception occurs
     */
    @Test
    public void testFullDir2Dir() throws IOException {
        // create a test file in the source directory
        File testFile = new File(sourceDir, "testFile");
        try {
            if (!testFile.createNewFile()) {
                fail("could not create test file " + testFile);
            }
        } catch (IOException ex) {
            System.out.println("Could not create " + testFile);
            throw ex;
        }

        // try copying the source directory (recursive)
        CopyJob copyJob = new CopyJob(
                true, destinationDir.getPath(), sourceDir.getPath());
        fileCopier.copy(copyJob);

        // check
        File expectedDir = new File(destinationDir, sourceDir.getName());
        boolean dirExists = expectedDir.exists();
        boolean dirIsDir = expectedDir.isDirectory();
        File expectedFile = new File(expectedDir, testFile.getName());
        boolean fileExists = expectedFile.exists();
        boolean fileIsFile = expectedFile.isFile();

        // cleanup
        if (!testFile.delete()) {
            fail("could not delete test file " + testFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (expectedFile.exists() && !expectedFile.delete()) {
            fail("could not delete destination file " + expectedFile);
        }
        if (!expectedDir.delete()) {
            fail("could not delete destination dir " + expectedDir);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination directory was not created", dirExists);
        assertTrue("destination directory is no directory", dirIsDir);
        assertTrue("destination file was not created", fileExists);
        assertTrue("destination file is no file", fileIsFile);
    }

    /**
     * test, if we correctly copy an empty directory (recursively) when
     * appending path separators at the end of the path
     * @throws Exception if an exception occurs
     */
    @Test
    public void testPathSeparator() throws Exception {

        // try copying the empty directory
        CopyJob copyJob = new CopyJob(true, destinationDir.getPath(),
                sourceDir.getPath() + File.separatorChar);
        fileCopier.copy(copyJob);

        // check
        File expected = new File(destinationDir, sourceDir.getName());
        boolean exists = expected.exists();
        boolean isDirectory = expected.isDirectory();

        // cleanup
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (expected.exists() && !expected.delete()) {
            fail("could not delete destination dir " + expected);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination was not created", exists);
        assertTrue("destination is no directory", isDirectory);
    }
}