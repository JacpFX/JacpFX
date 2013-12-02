package org.jacpfx.rcp.util;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 02.12.13
 * Time: 22:32
 * Util method to check if access is allowed
 */
public class AccessUtil {


    public static boolean hasAccess(final String callerClass, Class ...clazz) {
        final Class<?> caller = getClassByString(callerClass);
        if(caller==null) return false;
         for(final Class c : clazz) {
             if(c.isAssignableFrom(caller)) return true;
         }
        return false;

    }



    private static Class<?> getClassByString(final String className) {
        if(className==null) return null;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
