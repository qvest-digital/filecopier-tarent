/*
 * FileCopierTest.java
 *
 * Created on 22. April 2008, 14:21
 *
 * This file is part of the Java File Copy Library.
 *
 * The Java File Copy Libraryis free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Java File Copy Libraryis distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.fhnw.filecopier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * test, if we correctly handle empty copy jobs
     * @throws Exception if an exception occurs
     */
    @Test
    public void testMultipleDestinations() throws Exception {

        // log everything to console
        Logger logger = Logger.getLogger(FileCopier.class.getName());
        logger.setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        logger.addHandler(consoleHandler);
        consoleHandler.setLevel(Level.ALL);

        // create a large (3 MiB) random source file
        int testSize = 3 * 1024 * 1024;
        byte[] data = new byte[testSize];
        Random random = new Random();
        random.nextBytes(data);
        File sourceFile = new File(sourceDir, "sourceFile");
        FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
        fileOutputStream.write(data);
        fileOutputStream.close();

        // create a second destination directory
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File destinationDir2 = new File(tmpDir, "testDestinationDir2");
        if (!destinationDir2.exists() && !destinationDir2.mkdir()) {
            fail("could not create destination dir " + destinationDir2);
        }

        // copy source file in both destination directories
        CopyJob copyJob = new CopyJob(
                new String[]{sourceFile.getPath()},
                new String[]{
                    destinationDir.getPath(),
                    destinationDir2.getPath()},
                true);
        fileCopier.copy(copyJob);

        // check both destination files
        File expected1 = new File(destinationDir, "sourceFile");
        boolean exists1 = expected1.exists();
        boolean isFile1 = expected1.isFile();
        BufferedInputStream bufferedInputStream =
                new BufferedInputStream(new FileInputStream(expected1));
        byte[] expectedData1 = new byte[testSize];
        bufferedInputStream.read(expectedData1);
        boolean contentMatch1 = Arrays.equals(data, expectedData1);
        File expected2 = new File(destinationDir2, "sourceFile");
        boolean exists2 = expected2.exists();
        boolean isFile2 = expected2.isFile();
        bufferedInputStream =
                new BufferedInputStream(new FileInputStream(expected2));
        byte[] expectedData2 = new byte[testSize];
        bufferedInputStream.read(expectedData2);
        boolean contentMatch2 = Arrays.equals(data, expectedData2);

        // cleanup
        if (!sourceFile.delete()) {
            fail("could not delete source file " + sourceFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expected1.delete()) {
            fail("could not delete destination file " + expected1);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }
        if (!expected2.delete()) {
            fail("could not delete destination file " + expected2);
        }
        if (!destinationDir2.delete()) {
            fail("could not delete destination dir " + destinationDir2);
        }

        // final check
        assertTrue("first destination file was not created", exists1);
        assertTrue("first destination file is no file", isFile1);
        assertTrue("first destination file content does not match source file",
                contentMatch1);
        assertTrue("second destination file was not created", exists2);
        assertTrue("second destination file is no file", isFile2);
        assertTrue("second destination file content does not match source file",
                contentMatch2);
    }

    /**
     * test, if we correctly handle empty copy jobs
     * @throws Exception if an exception occurs
     */
    @Test
    public void testEmptyJob() throws Exception {

        // try a single empty job
        fileCopier.copy((CopyJob) null);

        // try a normal and an empty job
        File singleFile = new File(sourceDir, "singleFile");
        try {
            if (!singleFile.createNewFile()) {
                fail("could not create test file " + singleFile);
            }
            FileWriter fileWriter = new FileWriter(singleFile);
            fileWriter.write("something not important");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + singleFile);
            throw ex;
        }
        CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath() + "/.*"},
                new String[]{destinationDir.getPath()}, true);
        fileCopier.copy(copyJob, (CopyJob) null);

        File expectedFile = new File(destinationDir, singleFile.getName());
        if (!singleFile.delete()) {
            fail("could not delete single file " + singleFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expectedFile.delete()) {
            fail("could not delete destination file " + expectedFile);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }
    }

    /**
     * test, if we correctly copy symlinks
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopySymlink() throws Exception {
        // create a source file with some content
        String content = "Please link here.";
        File normalFile = new File(sourceDir, "normalFile");
        try {
            if (!normalFile.createNewFile()) {
                fail("could not create test file " + normalFile);
            }
            FileWriter fileWriter = new FileWriter(normalFile);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            System.out.println("Could not create " + normalFile);
            throw ex;
        }

        // create symlink to file
        String symlinkFileName = "symlink";
        File symlinkFile = new File(normalFile.getParentFile().getPath() +
                File.separator + symlinkFileName);
        ProcessExecutor executor = new ProcessExecutor();
        int exitValue = executor.executeProcess(
                "ln", "-s", normalFile.getName(), symlinkFile.getPath());
        assertEquals("could not create symlink \"" + symlinkFile.getPath() +
                '\"', 0, exitValue);

        // copy file and symlink
        CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath() + "/.*"},
                new String[]{destinationDir.getPath()}, true);
        fileCopier.copy(copyJob);

        // check
        File expectedFile = new File(destinationDir, normalFile.getName());
        boolean fileExists = expectedFile.exists();
        boolean fileIsFile = expectedFile.isFile();

        File expectedSymlink = new File(destinationDir, symlinkFileName);
        boolean symlinkExists = expectedSymlink.exists();
        boolean symlinkIsFile = expectedSymlink.isFile();

        // cleanup
        if (!normalFile.delete()) {
            fail("could not delete source file " + normalFile);
        }
        if (!symlinkFile.delete()) {
            fail("could not delete source symlink " + symlinkFile);
        }
        if (!sourceDir.delete()) {
            fail("could not delete source dir " + sourceDir);
        }
        if (!expectedFile.delete()) {
            fail("could not delete destination file " + expectedFile);
        }
        if (!expectedSymlink.delete()) {
            fail("could not delete destination symlink " + expectedSymlink);
        }
        if (!destinationDir.delete()) {
            fail("could not delete destination dir " + destinationDir);
        }

        // final check
        assertTrue("destination file was not created", fileExists);
        assertTrue("destination file is no file", fileIsFile);
        assertTrue("destination symlink was not created", symlinkExists);
        assertFalse("destination symlink is no symlink", symlinkIsFile);
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
        CopyJob copyJob1 = new CopyJob(new String[]{singleFile.getPath()},
                new String[]{existingDestinationFile1.getPath()}, false);
        CopyJob copyJob2 = new CopyJob(new String[]{singleFile.getPath()},
                new String[]{existingDestinationFile2.getPath()}, false);
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
        CopyJob copyJob = new CopyJob(new String[]{singleFile.getPath()},
                new String[]{existingDestinationFile.getPath()}, false);
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
        CopyJob copyJob = new CopyJob(
                new String[]{singleFile.getPath()},
                new String[]{
                    destinationDir.getPath() + File.separatorChar + NEW_NAME},
                false);
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
        CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath()},
                new String[]{destinationDir.getPath()}, true);
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
        CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath()},
                new String[]{destinationDir.getPath()}, false);
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
        CopyJob copyJob = new CopyJob(new String[]{singleFile.getPath()},
                new String[]{destinationDir.getPath()}, recursive);
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
            CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath()},
                    new String[]{destinationFile.getPath()}, true);
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
        CopyJob copyJob = new CopyJob(new String[]{sourceDir.getPath()},
                new String[]{destinationDir.getPath()}, true);
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
        CopyJob copyJob = new CopyJob(
                new String[]{sourceDir.getPath() + File.separatorChar},
                new String[]{destinationDir.getPath()}, true);
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
