package org.jacp.demo.main;

import org.jacp.javafx.rcp.util.ClassFinder;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 19.07.13
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */
public class ClassFindTest {
   public static void main(final String[] args) throws ClassNotFoundException {
         ClassFinder finder = new ClassFinder();
       ClassFinderOrig finderOrig = new ClassFinderOrig();
       long start = System.currentTimeMillis();
      /* Class[] result = finder.getAll("org.jacp.demo.callbacks");
       for(Class c : result) {
           System.out.println(c);
       }*/
       long end = System.currentTimeMillis();
       System.out.println("Execution time was "+(end-start)+" ms.");
       start = System.currentTimeMillis();
       Class[] result2 = finderOrig.getAll("org.jacp.demo.callbacks");
       for(Class c : result2) {
           System.out.println(c);
       }
      end = System.currentTimeMillis();

       System.out.println("Execution time was "+(end-start)+" ms.");

       start = System.currentTimeMillis();
       Class[] result = finder.getAll("org.jacp.demo");
       for(Class c : result) {
           System.out.println("--"+c);
       }
       end = System.currentTimeMillis();
       System.out.println("Execution time was "+(end-start)+" ms.");

   }
}
