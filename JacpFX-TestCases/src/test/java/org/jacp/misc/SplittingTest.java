package org.jacp.misc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

/**
 * Created by Andy Moncsek on 11.12.15.
 */
public class SplittingTest {
    @Test
    public void testSimpleSplit() {
        split(); // warmup
        long start = System.currentTimeMillis();
        split();
        long end = System.currentTimeMillis();

        System.out.println("Execution time split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplit() {
        splitComplex(); // warmup
        long start = System.currentTimeMillis();
        splitComplex();
        long end = System.currentTimeMillis();

        System.out.println("Execution time complex split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplit2() {
        splitComplex2(); // warmup
        long start = System.currentTimeMillis();
        splitComplex2();
        long end = System.currentTimeMillis();

        System.out.println("Execution time complex 2 split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplit3() {
        splitComplex3(); // warmup
        long start = System.currentTimeMillis();
        splitComplex3();
        long end = System.currentTimeMillis();

        System.out.println("Execution time complex 3 split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplit4() {
        splitComplex4(); // warmup
        long start = System.currentTimeMillis();
        splitComplex4();
        long end = System.currentTimeMillis();

        System.out.println("Execution time complex 4 split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplit5() {
        splitComplex5(); // warmup
        long start = System.currentTimeMillis();
        splitComplex5();
        long end = System.currentTimeMillis();

        System.out.println("Execution time complex 5 split " + (end - start) + " ms.");
    }

    @Test
    public void testComplexSplitCorrect() {
        String a = "a.b";
        String b = "a.b.c";
        String c = "ab";
        char PATTERN_SPLIT = '.';

        String[] r1 = splitBySingleCharExtended(a.toCharArray(), PATTERN_SPLIT);
        assertTrue(r1.length == 2);
        assertTrue(r1[0].equals("a"));
        assertTrue(r1[1].equals("b"));


        String[] r2 = splitBySingleCharExtended(b.toCharArray(), PATTERN_SPLIT);
        assertTrue(r2.length == 3);
        assertTrue(r2[0].equals("a"));
        assertTrue(r2[1].equals("b"));
        assertTrue(r2[2].equals("c"));

        String[] r3 = splitBySingleCharExtended(c.toCharArray(), PATTERN_SPLIT);
        assertTrue(r3.length == 1);
        assertTrue(r3[0].equals("ab"));

    }

    @Test
    public void testComplexSplitCorrect2() {
        String a = "a.b";
        String b = "a.b.c";
        String c = "ab";
        char PATTERN_SPLIT = '.';

        String[] r1 = splitBySingleCharExtended(a.toCharArray(), PATTERN_SPLIT);
        assertTrue(r1.length == 2);
        assertTrue(r1[0].equals("a"));
        assertTrue(r1[1].equals("b"));


        String[] r2 = splitBySingleCharExtended(b.toCharArray(), PATTERN_SPLIT);
        assertTrue(r2.length == 3);
        assertTrue(r2[0].equals("a"));
        assertTrue(r2[1].equals("b"));
        assertTrue(r2[2].equals("c"));

        String[] r3 = splitBySingleCharExtended(c.toCharArray(), PATTERN_SPLIT);
        assertTrue(r3.length == 1);
        assertTrue(r3[0].equals("ab"));

    }

    private void split() {
        final String value = "a.b";
        String PATTERN_SPLIT = "\\.";
        IntStream.range(0, 10000000).forEach(i -> {
            value.split(PATTERN_SPLIT);
        });
    }

    private void splitComplex() {
        final String value = "a.b";
        char PATTERN_SPLIT = '.';
        IntStream.range(0, 10000000).forEach(i -> {
            String val = splitBySingleChar(value.toCharArray(), PATTERN_SPLIT).get(0);
        });
    }

    private void splitComplex2() {
        final String value = "a.b";
        char PATTERN_SPLIT = '.';
        IntStream.range(0, 10000000).forEach(i -> {
            String val = splitBySingleCharExtended(value.toCharArray(), PATTERN_SPLIT)[0];
        });
    }

    private void splitComplex3() {
        final String value = "a.b";
        char PATTERN_SPLIT = '.';
        IntStream.range(0, 10000000).forEach(i -> {
            String val = splitBySingleChar3(value.toCharArray(), PATTERN_SPLIT).get(0);
        });
    }

    private void splitComplex4() {
        final String value = "a.b";
        char PATTERN_SPLIT = '.';
        IntStream.range(0, 10000000).forEach(i -> {
            String val = splitBySingleCharExtended2(value.toCharArray(), PATTERN_SPLIT)[0];
        });
    }

    private void splitComplex5() {
        final String value = "a.b";
        char PATTERN_SPLIT = '.';
        IntStream.range(0, 10000000).forEach(i -> {
            char[] val = splitBySingleCharExtended3(value.toCharArray(), PATTERN_SPLIT)[0];
            new String(val);
        });
    }

    public static ArrayList<String> splitBySingleChar(final char[] s,
                                                      final char splitChar) {
        final ArrayList<String> result = new ArrayList<>();
        final int length = s.length;
        int offset = 0;
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    result.add(new String(s, offset, count));
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            result.add(new String(s, offset, count));
        }
        return result;
    }

    public static ArrayList<String> splitBySingleChar3(final char[] s,
                                                       final char splitChar) {
        final ArrayList<String> result = new ArrayList<>(3);
        final int length = s.length;
        int offset = 0;
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    result.add(new String(s, offset, count));
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            result.add(new String(s, offset, count));
        }
        return result;
    }


    public static String[] splitBySingleCharExtended(final char[] s,
                                                     final char splitChar) {
        String[] result = new String[0];
        final int length = s.length;
        int offset = 0;
        int count = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    result = Arrays.copyOf(result, result.length + 1);
                    result[result.length - 1] = new String(s, offset, count);
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            result = Arrays.copyOf(result, result.length + 1);
            result[result.length - 1] = new String(s, offset, count);
        }
        return result;
    }

    public static String[] splitBySingleCharExtended2(final char[] s,
                                                      final char splitChar) {
        String[] result = new String[3];
        final int length = s.length;
        int offset = 0;
        int count = 0;
        int matchCount = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    if (matchCount == 2) return result;
                    result[matchCount] = new String(s, offset, count);
                    matchCount++;
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            if (matchCount == 2) return result;
            result[matchCount] = new String(s, offset, count);
        }
        return result;
    }

    public static char[][] splitBySingleCharExtended3(final char[] s,
                                                      final char splitChar) {
        char[][] result = new char[3][];
        final int length = s.length;
        int offset = 0;
        int count = 0;
        int matchCount = 0;
        for (int i = 0; i < length; i++) {
            if (s[i] == splitChar) {
                if (count > 0) {
                    if (matchCount == 2) return result;
                    result[matchCount] = Arrays.copyOfRange(s, offset, offset + count);
                    matchCount++;
                }
                offset = i + 1;
                count = 0;
            } else {
                count++;
            }
        }
        if (count > 0) {
            if (matchCount == 2) return result;
            result[matchCount] = Arrays.copyOfRange(s, offset, offset + count);
        }
        return result;
    }
}
