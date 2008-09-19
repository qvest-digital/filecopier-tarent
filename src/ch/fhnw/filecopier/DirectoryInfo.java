package ch.fhnw.filecopier;

import java.io.File;
import java.util.List;

/**
 * Some informations about a directory.
 * @author ronny
 */
public class DirectoryInfo {

    private final File baseDirectory;
    private final List<File> files;
    private final long byteCount;
    
    /**
     * creates a new DirectoryInfo
     * @param baseDirectory the base directory
     * @param files the list of all files and subdirectories (relative paths)
     * @param byteCount the sum of all file sizes
     * (recursively)
     */
    public DirectoryInfo(File baseDirectory, List<File> files, long byteCount) {
        this.baseDirectory = baseDirectory;
        this.files = files;
        this.byteCount = byteCount;
    }
    
    /**
     * returns the base directory
     * @return the base directory
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * returns the list of all files and directories
     * @return the list of all files and directories
     */
    public List<File> getFiles() {
        return files;
    }

    /**
     * returns the sum of all file sizes
     * @return the sum of all file sizes
     */
    public long getByteCount() {
        return byteCount;
    }
}
