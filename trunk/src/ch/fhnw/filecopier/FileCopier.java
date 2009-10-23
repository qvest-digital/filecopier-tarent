/*
 * FileCopier.java
 *
 * Created on 19.09.2008, 15:37:49
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A class for copying files and directories. It can be used headless.
 * This class is NOT threadsafe!
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class FileCopier {

    /**
     * the string used for the file property
     */
    public final static String FILE_PROPERTY = "file";
    /**
     * the string used for the byte counter property
     */
    public final static String BYTE_COUNTER_PROPERTY = "byte_counter";
    /**
     * the string used for the state property
     */
    public final static String STATE_PROPERTY = "state";

    /**
     * the state of the FileCopier
     */
    public enum State {

        /**
         * the initial state
         */
        START,
        /**
         * the FileCopier is checking the source directory (determining the
         * number of files and directories and the file size sum)
         */
        CHECKING_SOURCE,
        /**
         * the FileCopier is copying files and directories
         */
        COPYING,
        /**
         * the FileCopier finished copying files and directories
         */
        END
    }
    private State state = State.START;
    private final static Logger LOGGER =
            Logger.getLogger(FileCopier.class.getName());
    private final PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(this);
    private long byteCount;
    private long oldCopiedBytes;
    private long copiedBytes;
    private final char[] regExChars = new char[]{
        '+', '(', ')', '^', '$', '.', '{', '}', '[', ']', '|', '\\'
    };
    private int updateTime = 1000; // the intervall we want to get
    private final static NumberFormat NUMBER_FORMAT =
            NumberFormat.getInstance();
    private FileChannel sourceChannel;
    private long position;
    private long sourceLength;
    private long slice = 1048576; // 1 MiB
    private long transferVolume;
    private long sliceStartTime;
    private CyclicBarrier barrier;

    /**
     * Add a listener for property changes.
     * @param property the property that changes
     * @param listener the listener for the change
     */
    public void addPropertyChangeListener(
            String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(
                property, listener);
    }

    /**
     * Remove a listener for property changes.
     * @param property the property that changes
     * @param listener the listener for property changes
     */
    public void removePropertyChangeListener(
            String property, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(
                property, listener);
    }

    /**
     * returns the byte count of all source files
     * @return the byte count of all source files
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * returns the sum of all bytes copied so far
     * @return the sum of all bytes copied so far
     */
    public long getCopiedBytes() {
        return copiedBytes;
    }

    /**
     * resets the copier so that another copy operation can be started
     */
    public void reset() {
        byteCount = 0;
        copiedBytes = 0;
        State previousState = state;
        state = State.START;
        propertyChangeSupport.firePropertyChange(
                STATE_PROPERTY, previousState, state);
    }

    /**
     * copies source files to a given destination
     * @param copyJobs all copyjobs to execute
     * @throws java.io.IOException if an I/O exception occurs
     */
    public void copy(CopyJob... copyJobs) throws IOException {

        // feed our property change listeners
        State previousState = state;
        state = State.CHECKING_SOURCE;
        propertyChangeSupport.firePropertyChange(
                STATE_PROPERTY, previousState, state);

        // scan all sources of all copyJobs and store the directoryInfos
        int fileCount = 0;
        for (CopyJob copyJob : copyJobs) {
            if (copyJob == null) {
                continue;
            }
            String[] sources = copyJob.getSources();
            List<DirectoryInfo> directoryInfos = new ArrayList<DirectoryInfo>();
            for (String source : sources) {
                DirectoryInfo tmpInfo =
                        evaluateSource(source, copyJob.isRecursive());
                if (tmpInfo != null) {
                    directoryInfos.add(tmpInfo);
                    byteCount += tmpInfo.getByteCount();
                    fileCount += tmpInfo.getFiles().size();
                }
            }
            copyJob.setDirectoryInfos(directoryInfos);
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("\n\nsource files:");
                for (DirectoryInfo directoryInfo : directoryInfos) {
                    LOGGER.info("source files in base directory " +
                            directoryInfo.getBaseDirectory() + ':');
                    for (File sourceFile : directoryInfo.getFiles()) {
                        LOGGER.info((sourceFile.isFile() ? "f " : "d ") +
                                sourceFile.getPath());
                    }
                }
            }
        }

        if (fileCount == 0) {
            LOGGER.info("there are no files to copy");
            return;
        }

        // feed our property change listeners
        previousState = state;
        state = State.COPYING;
        propertyChangeSupport.firePropertyChange(
                STATE_PROPERTY, previousState, state);

        // execute all copy jobs
        for (CopyJob copyJob : copyJobs) {
            if (copyJob == null) {
                continue;
            }
            List<DirectoryInfo> directoryInfos = copyJob.getDirectoryInfos();
            String[] destinations = copyJob.getDestination();
            // get number of source files in this job
            int sourceCount = 0;
            for (DirectoryInfo directoryInfo : directoryInfos) {
                sourceCount += directoryInfo.getFiles().size();
            }
            switch (sourceCount) {
                case 0:
                    // skip empty jobs
                    continue;

                case 1:
                    // we only have a single source file in this job
                    File sourceFile = directoryInfos.get(0).getFiles().get(0);
                    for (String destination : destinations) {
                        File destinationFile = new File(destination);

                        if (destinationFile.exists()) {
                            if (destinationFile.isDirectory()) {
                                // copy sourceFile to destination directory
                                copyFilesToDirectory(directoryInfos, destinationFile);
                            } else {
                                if (sourceFile.isDirectory()) {
                                    throw new IOException(
                                            "can not overwrite file \"" + destinationFile +
                                            "\" with directory \"" + sourceFile + "\"");
                                } else {
                                    // overwrite destinationFile with sourceFile
                                    copyFile(sourceFile, destinationFile);
                                }
                            }
                        } else {
                            // create copy of sourceFile at destinationFile
                            copyFile(sourceFile, destinationFile);
                        }
                    }
                    break;

                default:
                    // we have several source files (the target MUST be a directory)
                    for (String destination : destinations) {
                        File destinationFile = new File(destination);
                        if (destinationFile.exists()) {
                            if (destinationFile.isDirectory()) {
                                // copy all files into target directory
                                copyFilesToDirectory(directoryInfos, destinationFile);
                            } else {
                                StringBuilder errorMessage = new StringBuilder(
                                        "can not copy several files to another file\n" +
                                        " sources:");
                                for (DirectoryInfo directoryInfo : directoryInfos) {
                                    List<File> files = directoryInfo.getFiles();
                                    for (File file : files) {
                                        errorMessage.append("  " + file.getPath());
                                    }
                                }
                                errorMessage.append(
                                        " destination: " + destinationFile.getPath());
                                throw new IOException(errorMessage.toString());
                            }
                        } else {
                            // destination does not exist
                            // we create it as the target directory...
                            if (destinationFile.mkdirs()) {
                                // copy all files into target directory
                                copyFilesToDirectory(directoryInfos, destinationFile);
                            } else {
                                throw new IOException(
                                        "can not create target directory " +
                                        destination);
                            }
                        }
                    }
            }
        }

        if (oldCopiedBytes != copiedBytes) {
            // need to fire one last time...
            // (last slice was not fully used)
            propertyChangeSupport.firePropertyChange(
                    BYTE_COUNTER_PROPERTY, oldCopiedBytes, copiedBytes);
        }
        previousState = state;
        state = State.END;
        propertyChangeSupport.firePropertyChange(
                STATE_PROPERTY, previousState, state);
    }

    private void copyFilesToDirectory(List<DirectoryInfo> directoryInfos,
            File destinationDir) throws IOException {
        for (DirectoryInfo directoryInfo : directoryInfos) {
            File baseDirectory = directoryInfo.getBaseDirectory();
            String basePath = baseDirectory.getPath();
            int baseLength = basePath.length();
            for (File sourceFile : directoryInfo.getFiles()) {
                String filePath = sourceFile.getPath();
                String destinationPath = filePath.substring(baseLength);
                File destinationFile =
                        new File(destinationDir, destinationPath);
                if (sourceFile.isDirectory()) {
                    if (destinationFile.exists()) {
                        LOGGER.info("Directory \"" +
                                destinationFile + "\" already exists");
                    } else {
                        LOGGER.info("Creating directory \"" +
                                destinationFile + "\"");
                        if (!destinationFile.mkdirs()) {
                            throw new IOException(
                                    "Could not create directory \"" +
                                    destinationFile + "\"");
                        }
                    }
                } else {
                    copyFile(sourceFile, destinationFile);
                }
            }
        }
    }

    private DirectoryInfo evaluateSource(
            String sourceExpression, boolean recursive) {

        // check expression for regex
        boolean regExFount = false;
        int firstRegExIndex = Integer.MAX_VALUE;
        for (char regExChar : regExChars) {
            int index = sourceExpression.indexOf(regExChar);
            if (index != -1) {
                // The expression contains a regex char.
                // We have to check, if it is escaped.
                char previousChar = sourceExpression.charAt(index - 1);
                if (previousChar != '\\') {
                    // no, it is really a regex here...
                    regExFount = true;
                    firstRegExIndex =
                            Math.min(firstRegExIndex, index);
                }

            }
        }

        // determine base directory and search pattern
        File baseDirectory = null;
        Pattern pattern = null;
        if (regExFount) {
            String substring = sourceExpression.substring(0, firstRegExIndex);
            int lastSeparatorChar = substring.lastIndexOf(File.separatorChar);
            String dirName = sourceExpression.substring(0, lastSeparatorChar);
            baseDirectory = new File(dirName);
            pattern = Pattern.compile(sourceExpression);
        } else {
            // no regEx, just a file or directory
            File file = new File(sourceExpression);
            baseDirectory = file.getParentFile();
            // use file.getPath() instead of sourceExpression so that all
            // path separators at the end of the expression are removed...
            String path = file.getPath();
            if (recursive && file.isDirectory()) {
                pattern = Pattern.compile(path + ".*");
            } else {
                pattern = Pattern.compile(path);
            }
        }

        // traverse base directory and apply pattern
        return expand(baseDirectory, pattern, recursive);
    }

    private DirectoryInfo expand(File baseDirectory,
            Pattern pattern, boolean recursive) {

        LOGGER.info("base directory: " + baseDirectory +
                " pattern: \"" + pattern + "\"");

        // feed the listeners
        propertyChangeSupport.firePropertyChange(
                FILE_PROPERTY, null, baseDirectory);

        if (!baseDirectory.exists()) {
            LOGGER.warning(baseDirectory + " does not exist");
            return null;
        }

        if (!baseDirectory.isDirectory()) {
            LOGGER.warning(baseDirectory + " is no directory");
            return null;
        }

        if (!baseDirectory.canRead()) {
            LOGGER.warning("can not read " + baseDirectory);
            return null;
        }

        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }

        LOGGER.fine("recursing directory " + baseDirectory);
        long tmpByteCount = 0;
        List<File> files = new ArrayList<File>();
        for (File subFile : baseDirectory.listFiles()) {
            if (pattern.matcher(subFile.getPath()).matches()) {
                LOGGER.fine(subFile + " matches");
                if (subFile.isDirectory()) {
                    if (recursive) {
                        files.add(subFile);
                        tmpByteCount += subFile.length();
                        DirectoryInfo tmpInfo =
                                expand(subFile, pattern, recursive);
                        if (tmpInfo != null) {
                            files.addAll(tmpInfo.getFiles());
                            tmpByteCount += tmpInfo.getByteCount();
                        }
                    } else {
                        LOGGER.fine("skipping directory " + subFile);
                    }
                } else {
                    files.add(subFile);
                    tmpByteCount += subFile.length();
                }
            } else {
                LOGGER.fine(subFile + " does not match");
            }
        }
        return new DirectoryInfo(baseDirectory, files, tmpByteCount);
    }

    private void copyFile(File source, File... destinations)
            throws IOException {

        // some initial logging
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Copying file \"");
            stringBuilder.append(source.toString());
            stringBuilder.append("\" to the following destinations:\n");
            for (File destination : destinations) {
                stringBuilder.append(destination.getPath());
                stringBuilder.append('\n');
            }
            LOGGER.info(stringBuilder.toString());
        }

        // ensure that all destination files exist before starting the transfer
        // processing
        for (File destination : destinations) {
            if (!destination.exists()) {
                destination.createNewFile();
            }
        }

        // quick return when source is an empty file
        sourceLength = source.length();
        if (sourceLength == 0) {
            return;
        }

        // create a Transferrer thread for every destination
        int destinationCount = destinations.length;
        sourceChannel = new FileInputStream(source).getChannel();
        final Transferrer[] transferrers = new Transferrer[destinationCount];
        for (int i = 0; i < destinationCount; i++) {
            transferrers[i] = new Transferrer(
                    new FileOutputStream(destinations[i]).getChannel());
        }

        barrier = new CyclicBarrier(destinationCount, new Runnable() {

            @Override
            public void run() {

                // inform property listeners about copied data volume
                position += transferVolume;
                copiedBytes += transferVolume;
                propertyChangeSupport.firePropertyChange(
                        BYTE_COUNTER_PROPERTY, oldCopiedBytes, copiedBytes);
                oldCopiedBytes = copiedBytes;

                // determine next slice size before releasing the barrier
                long stop = System.currentTimeMillis();
                long time = stop - sliceStartTime;
                LOGGER.finest("time = " + NUMBER_FORMAT.format(time) + " ms");
                if (time != 0) {
                    long newSlice = (slice * slice * updateTime) / (time * transferVolume);
                    // just using newSlice here leads to overmodulation
                    // doubling or halving is the slower (and probably better)
                    // approach
                    long doubleSlice = 2 * slice;
                    long halfSlice = slice / 2;
                    if (newSlice > doubleSlice) {
                        slice = doubleSlice;
                    } else if (newSlice < halfSlice) {
                        slice = halfSlice;
                    }
                    transferVolume = Math.min(slice, sourceLength - position);
                    LOGGER.finest("slice = " + NUMBER_FORMAT.format(slice) +
                            " Byte\n" + "transferVolume = " +
                            NUMBER_FORMAT.format(transferVolume) + " Byte");
                }
            }
        });

        // start the transfer process
        position = 0;
        transferVolume = Math.min(slice, sourceLength);
        LOGGER.finest("slice = " + NUMBER_FORMAT.format(slice) + " Byte\n" +
                "transferVolume = " + NUMBER_FORMAT.format(transferVolume) +
                " Byte");
        sliceStartTime = System.currentTimeMillis();

        ExecutorCompletionService<Void> completionService =
                new ExecutorCompletionService<Void>(
                Executors.newCachedThreadPool());
        for (Transferrer transferrer : transferrers) {
            completionService.submit(transferrer, null);
        }

        // wait until all transferrers completed their execution
        for (int i = 0; i < destinationCount; i++) {
            try {
                completionService.take();
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        if (sourceChannel != null) {
            sourceChannel.close();
        }
    }

    private class Transferrer extends Thread {

        private final FileChannel destinationChannel;

        public Transferrer(FileChannel destinationChannel) {
            this.destinationChannel = destinationChannel;
        }

        @Override
        public void run() {
            try {
                while (position < sourceLength) {
                    // transfer the current slice
                    long transferredBytes = 0;
                    while (transferredBytes < transferVolume) {
                        transferredBytes += destinationChannel.transferFrom(
                                sourceChannel, position,
                                transferVolume - transferredBytes);
                    }
                    // wait for all other Transferrers to finish their slice
                    barrier.await();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "could not transfer data", ex);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (BrokenBarrierException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    destinationChannel.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE,
                            "could not close destination channel", ex);
                }
            }
        }
    }

    // maybe used later to convert globs into regex?
    private static String convertWildCardsToRegEx(String string) {
        char[] chars = string.toCharArray();
        StringBuilder builder = new StringBuilder(chars.length + 5);
        for (char character : chars) {
            if (character == '*') {
                builder.append(".*");
            } else if (character == '?') {
                builder.append(".");
            } else if ("+()^$.{}[]|\\".indexOf(character) != -1) {
                builder.append('\\').append(character);
            } else {
                builder.append(character);
            }

        }
        return builder.toString();
    }
}
