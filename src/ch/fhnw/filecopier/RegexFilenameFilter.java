package ch.fhnw.filecopier;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * 
 * @author ronny
 */
public class RegexFilenameFilter implements FilenameFilter {

    private static final Logger _logger = Logger.getLogger(RegexFilenameFilter.class.getPackage().getName());
    /**
     * Only file name that match this regex are accepted by this filter
     */
    String _regex = null; // setting the filter regex to null causes any name to be accepted (same as ".*")


    public RegexFilenameFilter() {
    }

    /**
     * Set the filter from a wildcard expression as known from the windows command line
     * ("?" = "any character", "*" = zero or more occurances of any character")
     *
     * @param sWild the wildcard pattern
     *
     * @return this
     */
    public RegexFilenameFilter setWildcard(String sWild) {
        _regex = wildcardToRegex(sWild);

        _logger.fine("RegexFilenameFilter.setWildcard(" + sWild + ") -> '" + _regex + "'");

        Pattern.compile(_regex); // throw PatternSyntaxException if the pattern is not valid
// this should never happen if wildcardToRegex works as intended,
// so thiw method does not declare PatternSyntaxException to be thrown

        return this;
    }

    /**
     * Set the regular expression of the filter
     *
     * @param regex the regular expression of the filter
     *
     * @return this
     */
    public RegexFilenameFilter setRegex(String regex)
            throws java.util.regex.PatternSyntaxException {
        _regex = regex.toLowerCase();

        _logger.fine("RegexFilenameFilter.setRegex(" + regex + ") -> '" + _regex + "'");

        Pattern.compile(_regex); // throw PatternSyntaxException if the pattern is not valid

        return this;
    }

    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     *
     * @param name the name of the file.
     *
     * @return true if and only if the name should be included in the file list; false otherwise.
     */
    public boolean accept(File dir, String name) {
        boolean bAccept;

        if (_regex == null) {
            bAccept = true;
        } else {
            bAccept = name.toLowerCase().matches(_regex);
        }
        _logger.fine("RegexFilenameFilter.accept(" + name + ") = " + bAccept);

        return bAccept;
    }

    /**
     * Converts a windows wildcard pattern to a regex pattern
     *
     * @param wild - Wildcard patter containing * and ?
     *
     * @return - a regex pattern that is equivalent to the windows wildcard pattern
     */
    private static String wildcardToRegex(String wild) {
        if (wild == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();

        char[] chars = wild.toCharArray();

        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == '*') {
                buffer.append(".*");
            } else if (chars[i] == '?') {
                buffer.append(".");
            } else if ("+()^$.{}[]|{}".indexOf(chars[i]) != -1) {
                // prefix all metacharacters with backslash
                buffer.append('\\').append(chars[i]);
            } else {
                buffer.append(chars[i]);
            }
        }
        return buffer.toString().toLowerCase();
    }
} 