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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Some tests for the file copier
 * @author ronny
 */
public class FileCopierTest {

    private final File tmpDir = new File(System.getProperty("java.io.tmpdir") +
            File.separatorChar + "filecopiertest");
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
        sourceDir = new File(tmpDir, "testSourceDir");
        if (!sourceDir.exists() && !sourceDir.mkdirs()) {
            fail("could not create source dir " + sourceDir);
        }
        destinationDir = new File(tmpDir, "testDestinationDir");
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            fail("could not create destination dir " + destinationDir);
        }
    }

    /**
     * test, if we correctly handle empty copy jobs
     * @throws Exception if an exception occurs
     */
    @Test
    public void testEmptyJob() throws Exception {

        System.out.println("************** testEmptyJob() **************");

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
        CopyJob copyJob = new CopyJob(
                new Source[]{
                    new Source(sourceDir.getPath(), ".*")
                },
                new String[]{
                    destinationDir.getPath()
                },
                true);
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
     * test, if we correctly copy&rename a single file
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyAndRename() throws Exception {
        System.out.println("************** " +
                "testCopyAndRename() **************");

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
                new Source[]{
                    new Source(singleFile.getParent(), singleFile.getName())
                },
                new String[]{
                    destinationDir.getPath() + File.separatorChar + NEW_NAME
                },
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
        System.out.println("************** " +
                "testCopyingEmptySourceDirRecursive() **************");

        // try copying the empty directory
        CopyJob copyJob = new CopyJob(
                new Source[]{
                    new Source(sourceDir.getParent(), sourceDir.getName())
                },
                new String[]{
                    destinationDir.getPath()
                },
                true);
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
     * test, if we correctly copy a single file to a target directory
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyingSingleFile() throws Exception {
        System.out.println("************** " +
                "testCopyingSingleFile() **************");
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
                new Source[]{
                    new Source(singleFile.getParent(), singleFile.getName())
                },
                new String[]{
                    destinationDir.getPath()
                },
                recursive);
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
        System.out.println("************** " +
                "testDir2File() **************");
        File destinationFile = new File(destinationDir, "destinationFile");
        if (!destinationFile.createNewFile()) {
            fail("could not create test file " + destinationFile);
        }
        try {
            // try copying the test file
            CopyJob copyJob = new CopyJob(
                    new Source[]{
                        new Source(sourceDir.getParent(), sourceDir.getName())
                    },
                    new String[]{
                        destinationFile.getPath()
                    },
                    true);
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
     * test, if we correctly copy an empty directory (recursively) when
     * appending path separators at the end of the path
     * @throws Exception if an exception occurs
     */
    @Test
    public void testPathSeparator() throws Exception {
        System.out.println("************** " +
                "testPathSeparator() **************");

        // try copying the empty directory
        CopyJob copyJob = new CopyJob(
                new Source[]{
                    new Source(sourceDir.getParent(), sourceDir.getName())
                },
                new String[]{
                    destinationDir.getPath()
                },
                true);
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
