package org.jacpfx.rcp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String CLASS_DOT=".";
    private static final String CLASS_DOLLAR="$";
    private static final String CLASS_FILE=".class";
    private static final String CLASS_PROJECT_SEPERATOR="classes";
    private static final int CLASS_PROJECT_SEPERATOR_LENGTH=8;
    /**
     * List with the jar files on the classpath
     */
    //  private static String[] jarFiles;
    /**
     * List with the directories on the classpath (containing .class files)
     */
    //  private static String[] binDirs;
    private final List<Path> binDirs;

    private final PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:*.class");

    private static final String FILE_SEPERATOR;

    static{
        FILE_SEPERATOR=isWindows()? File.separator + File.separator :File.separator;
    }

    private static boolean isWindows() {

        return (OS.contains("win"));

    }

    /**
     * Default constructur initializes the directories indicated by the
     * CLASSPATH, if they are not yet initialized.
     */
    public ClassFinder() {
        binDirs = initClassPathDir();
    }

    /**
     * Initialize the directories based on the classpath
     */
    private List<Path> initClassPathDir() {
        final String[] cs = CLASSPATH.split(File.pathSeparator);
        final Stream<String> entries = Stream.of(cs);
        return entries
                .map(s -> FileSystems.getDefault().getPath(s))
                .filter(s -> Files.isDirectory(s, LinkOption.NOFOLLOW_LINKS)).collect(Collectors.toList());

    }

    /**
     * Retrive all classes of the indicated package. The package is searched in
     * all classpath directories that are directories
     *
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return Array of found classes
     * @throws ClassNotFoundException no class was found in classpath
     */
    public Class[] getAll(final String packageName) throws ClassNotFoundException {

        final String packageDir = convertPackege(packageName);
        final List<String> files = new CopyOnWriteArrayList<>();
        final PathMatcher folderMatcher =
                FileSystems.getDefault().getPathMatcher("glob:**"+convertPackageToRegex(packageName)+"**");
        final SimpleFileVisitor <Path> visitor =   new CollectingFileVisitor(files,folderMatcher);
        binDirs.parallelStream().forEach(dir -> {
            try {
                Files.walkFileTree(dir, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        final List<Class> result = exctractClasses(packageDir, files);
        return result.toArray(new Class[result.size()]);



    }

    private List<Class> exctractClasses(final String packageDir, List<String> files) {
        final String seperator= CLASS_PROJECT_SEPERATOR.concat(FILE_SEPERATOR);
        return files.parallelStream()
                .map(dir -> dir.substring((dir.lastIndexOf(seperator)+CLASS_PROJECT_SEPERATOR_LENGTH), dir.length()))
                .filter(classDir->classDir.contains(packageDir))
                .map(subDir -> subDir.replace(File.separator, CLASS_DOT))
                .map(className -> className.substring(0, className
                        .lastIndexOf(CLASS_FILE)))
                .filter(classFile -> !classFile.contains(CLASS_DOLLAR))
                .map(cFile -> {
                    try {
                        return ClassLoader.getSystemClassLoader().loadClass(cFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(clazz -> clazz != null)
                .collect(Collectors.toList());

    }


    /**
     * Convert the package name into a relative directory path
     *
     * @param packageName name of the package as 'ch.sahits.civ'
     * @return relativ directory to the package
     */
    private String convertPackege(String packageName) {

        return packageName.replace(CLASS_DOT, File.separator);
    }

    private String convertPackageToRegex(String packageName){
        return packageName.replace(CLASS_DOT, FILE_SEPERATOR);
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
