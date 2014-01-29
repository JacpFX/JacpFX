package org.jacp.test.util;

/**
 *
 * @author: Patrick Symmangk (pete.jacp@gmail.com)
 *
 */
public class PathUtil {

    static StringBuilder sb = new StringBuilder();

    public static String createPath(final String... chunks) {
        for (final String chunk : chunks) {
            if (sb.length() != 0) {
                sb.append(".");
            }
            sb.append(chunk);
        }
        return sb.toString();
    }

}
