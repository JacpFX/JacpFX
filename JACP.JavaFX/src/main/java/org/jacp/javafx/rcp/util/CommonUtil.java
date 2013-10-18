package org.jacp.javafx.rcp.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 20.09.13
 * Time: 15:37
 * All common util methods
 */
class CommonUtil {


    /**
     * Returns a stream of strings from a string array
     * @param ids
     * @return
     */
    public static Stream<String> getStringStreamFromArray(String[] ids) {
        return Stream.of(ids);
    }
}
