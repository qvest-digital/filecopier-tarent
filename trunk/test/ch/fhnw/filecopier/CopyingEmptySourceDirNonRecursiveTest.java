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
 * Some tests for the file copier
 * @author ronny
 */
public class CopyingEmptySourceDirNonRecursiveTest {

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
     * test, if we correctly copy an empty directory (recursively)
     * @throws Exception if an exception occurs
     */
    @Test
    public void testCopyingEmptySourceDirNonRecursive() throws Exception {
        
        // try copying the empty directory
        CopyJob copyJob = new CopyJob(
                new Source[]{
                    new Source(sourceDir.getParent(), sourceDir.getName())
                },
                new String[]{
                    destinationDir.getPath()
                },
                false);
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
}
