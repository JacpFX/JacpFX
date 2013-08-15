package org.jacp.demo.main;

import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: ady
 * Date: 19.07.13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public class ClassFinderOrig {
    /**
     * Defined classpath
     */
    private static final String CLASSPATH = System.getProperty("java.class.path");
    /**
     * List with the jar files on the classpath
     */
    private static String[] jarFiles;
    /**
     * List with the directories on the classpath (containing .class files)
     */
    private static String[] binDirs;
    /**
     * All Classpath elements
     */
    private static File[] classPathDirs = null;
    /**
     * Default constructur initializes the directories indicated by the
     * CLASSPATH, if they are not yet initialized.
     */
    public ClassFinderOrig() {
        if (classPathDirs == null) {
            initClassPathDir();
        }
    }
    /**
     * Initialize the directories based on the classpath
     */
    private void initClassPathDir() {
        StringTokenizer st = new StringTokenizer(CLASSPATH, File.pathSeparator);
        int count = st.countTokens();
        classPathDirs = new File[count];
        Vector jar = new Vector();
        Vector bin = new Vector();
        for (int i = 0; i < count; i++) {
            classPathDirs[i] = new File(st.nextToken());
            if (classPathDirs[i].isDirectory()) {
                bin.add(classPathDirs[i].getAbsolutePath());
            } else {
                jar.add(classPathDirs[i].getAbsolutePath());
            }
        }
        jarFiles = new String[jar.size()];
        binDirs = new String[bin.size()];
        jar.copyInto(jarFiles);
        bin.copyInto(binDirs);
    }

    /**
     * Retrive all classes of the indicated package. The package is searched in
     * all classpath directories that are directories
     *
     * @param packageName
     *            name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException
     */
    public Class[] getAll(String packageName) throws ClassNotFoundException {
        String packageDir = convertPackege(packageName);
        Vector classes = new Vector();
        for (int i = 0; i < binDirs.length; i++) {
            packageDir = binDirs[i] + File.separator + packageDir;
           // System.out.println("DIR: "+packageDir);
            File dir = new File(packageDir);
           // System.out.println("DIR exists: "+dir.exists());
            classes.addAll(extractClasses(packageName, dir));
        }
        Class[] result = new Class[classes.size()];
        classes.copyInto(result);
        return result;
    }
    /**
     * Extract all the classes from a directory
     * @param packageName name of the package as 'ch.sahits.civ'
     * @param dir Package as directory
     * @return Vector with all found directories
     * @throws ClassNotFoundException
     */
    private Vector extractClasses(String packageName, File dir) throws ClassNotFoundException {
        Vector classes = new Vector();
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".class");
            }
        });
        if (files!=null) {	// directories without .class files may exist
            for (int j = 0; j < files.length; j++) {
                String className = packageName + "." + files[j].getName();
                className = className.substring(0, className
                        .lastIndexOf(".class"));
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
    /**
     * Convert the package name into a relative directory path
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return relativ directory to the package
     */
    private String convertPackege(String packageName) {
        String sep = File.separator;
        return packageName.replace(".", sep);
    }
    /**
     * Retrive all classes of the indicated package and all subpackages. The package is searched in
     * all classpath directories that are directories
     *
     * @param packageName
     *            name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException
     */
    public Class[] getAllRecursive(String packageName) throws ClassNotFoundException {
        String packageDir = convertPackege(packageName);
        Vector classes = new Vector();
        for (int i = 0; i < binDirs.length; i++) {
            packageDir = binDirs[i] + File.separator + packageDir;
            File dir = new File(packageDir);
            classes.addAll(extractClasses(packageName, dir));
            if (dir.isDirectory()) {
                File[] sub = dir.listFiles();
                for (int j = 0; j < sub.length; j++) {
                    if (sub[j].isDirectory()) {
                        Class[] rec = getAllRecursive(packageName + "."
                                + sub[j].getName());
                        Vector temp = new Vector(rec.length);
                        for (int k = 0; k < rec.length; k++) {
                            temp.add(rec[k]);
                        }
                        classes.addAll(temp);
                    }
                }
            }
        }
        Class[] result = new Class[classes.size()];
        classes.copyInto(result);
        return result;
    }
}
