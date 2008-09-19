/*
 * CopyJob.java
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

import java.util.List;

/**
 * A class that contains information about files to copy
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
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
