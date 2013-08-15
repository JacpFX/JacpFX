package org.jacp.javafx.rcp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 19.07.13
 * Time: 10:43
 * Find classes for defined packages.
 */
public class ClassFinder {
    /**
     * Defined classpath
     */
    private static final String CLASSPATH = System.getProperty("java.class.path");
    /**
     * List with the jar files on the classpath
     */
    //  private static String[] jarFiles;
    /**
     * List with the directories on the classpath (containing .class files)
     */
    //  private static String[] binDirs;
    private List<Path> binDirs;
    /**
     * All Classpath elements
     */
    private static Path[] classPathDirs = null;
    private final PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:*.class");



    /**
     * Default constructur initializes the directories indicated by the
     * CLASSPATH, if they are not yet initialized.
     */
    public ClassFinder() {
        if (classPathDirs == null) {
            binDirs = initClassPathDir();
        }
    }

    /**
     * Initialize the directories based on the classpath
     */
    private List<Path> initClassPathDir() {
        final String[] cs = CLASSPATH.split(File.pathSeparator);
        final List<String> entries = Arrays.asList(cs);
        return entries.parallelStream()
                .map(s -> FileSystems.getDefault().getPath(s))
                .filter(s -> Files.isDirectory(s, LinkOption.NOFOLLOW_LINKS)).collect(Collectors.toList());

    }

    /**
     * Retrive all classes of the indicated package. The package is searched in
     * all classpath directories that are directories
     *
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException
     */
    public Class[] getAll(final String packageName) throws ClassNotFoundException {

        final String packageDir = convertPackege(packageName);
        final List<String> files = new ArrayList<>();
        final PathMatcher folderMatcher =
                FileSystems.getDefault().getPathMatcher("glob:**"+packageDir+"**");
        final SimpleFileVisitor <Path> visitor =   new CollectingFileVisitor(files,folderMatcher);
        binDirs.parallelStream().forEach(dir -> {
            try {
                Files.walkFileTree(dir, visitor);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        });

        final List<Class> result = exctractClasses(packageDir, packageName, files);

        return result.toArray(new Class[result.size()]);



    }

    private List<Class> exctractClasses(final String packageDir, final String packageName, List<String> files) {

        return files.parallelStream()
                .map(dir -> dir.substring(dir.lastIndexOf(packageDir), dir.length()))
                .map(subdir -> subdir.replace(File.separator, "."))
                .map(className -> className.substring(0, className
                        .lastIndexOf(".class")))
                .filter(classFile -> !classFile.contains("$"))
                .map(cFile -> {
                    try {
                        return ClassLoader.getSystemClassLoader().loadClass(cFile);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());

    }


    /**
     * Convert the package name into a relative directory path
     *
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return relativ directory to the package
     */
    private String convertPackege(String packageName) {
        String sep = File.separator;
        return packageName.replace(".", sep);
    }

    private class CollectingFileVisitor extends SimpleFileVisitor<Path>{
        private final List<String> files;
        private final PathMatcher folderMatcher;
        public CollectingFileVisitor(final List<String> files,final PathMatcher folderMatcher) {
            this.files=files;
            this.folderMatcher = folderMatcher;
        }
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            final boolean matchesFolder =  folderMatcher.matches(file);
            if ( matchesFolder && matcher.matches(file.getFileName())) {
                files.add(file.toString());
            }
            return matchesFolder ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            return FileVisitResult.CONTINUE;
        }
    }


}
