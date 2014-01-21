package org.jacp.test.context;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Andy Moncsek on 04.12.13.
 */
public class SimpleArrayTest {

    @Test
    public void testArraySort() {
        String[] ids ={"id01","id02","id30","id25","id11"};
        Arrays.parallelSort(ids);
       int result =  Arrays.binarySearch(ids,"id022");
        System.out.println(result+"   :: "+ids);
    }
}
