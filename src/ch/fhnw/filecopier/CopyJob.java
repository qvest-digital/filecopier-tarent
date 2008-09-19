package ch.fhnw.filecopier;

import java.util.List;

/**
 * A class that contains information about files to copy
 * @author ronny
 */
public class CopyJob {

    private final boolean recursive;
    private final String destination;
    private final String[] sources;
    private List<DirectoryInfo> directoryInfos;

    /**
     * A class representing a copy job.
     * @param recursive if the sources must be evaluated recursively
     * @param destination the destination file
     * @param sources the sources
     */
    public CopyJob(boolean recursive, String destination, String... sources) {
        this.recursive = recursive;
        this.destination = destination;
        this.sources = sources;
    }

    /**
     * returns true, if the job is recursive, false otherwise
     * @return true, if the job is recursive, false otherwise
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * returns the destination
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * returns the sources
     * @return the sources
     */
    public String[] getSources() {
        return sources;
    }

    /**
     * sets the directory infos
     * @param directoryInfos the directory infos
     */
    public void setDirectoryInfos(List<DirectoryInfo> directoryInfos) {
        this.directoryInfos = directoryInfos;
    }

    /**
     * returns the directory infos 
     * @return the directory infos
     */
    public List<DirectoryInfo> getDirectoryInfos() {
        return directoryInfos;
    }
}
