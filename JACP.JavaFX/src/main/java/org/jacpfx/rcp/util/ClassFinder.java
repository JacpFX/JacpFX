package org.jacpfx.rcp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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
    private static final String CLASS_DOT = ".";
    private static final String CLASS_BACKSLASH_DOT = "\\.";
    private static final String CLASS_SLASH = "/";
    private static final String CLASS_DOLLAR = "$";
    private static final String CLASS_FILE = ".class";
    private static final String CLASS_PROJECT_SEPERATOR = "classes";
    private static final int CLASS_PROJECT_SEPERATOR_LENGTH = 8;
    /**
     * List with the directories on the classpath (containing .class files)
     */
    private final List<Path> binDirs;
    private final List<Path> jarsFiles;

    private final PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:*.class");

    private static final String FILE_SEPERATOR;

    static {
        FILE_SEPERATOR = isWindows() ? File.separator+File.separator: File.separator;
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
        jarsFiles = initClassPathJars();
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

    private List<Path> initClassPathJars() {
        final String[] cs = CLASSPATH.split(File.pathSeparator);
        final Stream<String> entries = Stream.of(cs);
        return entries
                .map(s -> FileSystems.getDefault().getPath(s))
                .filter(s -> Files.isRegularFile(s, LinkOption.NOFOLLOW_LINKS)).collect(Collectors.toList());

    }

    public List<String> getClasseNamesInPackage
            (Path jar, String packageName) {
        final List<String> classes = new ArrayList();
        packageName = packageName.replaceAll(CLASS_BACKSLASH_DOT, CLASS_SLASH);
        try {
            final JarInputStream jarFile = new JarInputStream
                    (Files.newInputStream(jar));
            while (true) {
                JarEntry jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().startsWith(packageName)) &&
                        (jarEntry.getName().endsWith(CLASS_FILE))) {
                    classes.add(jarEntry.getName().replaceAll(CLASS_SLASH, CLASS_BACKSLASH_DOT).replace(CLASS_FILE,""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
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
                FileSystems.getDefault().getPathMatcher("glob:**" + convertPackageToRegex(packageName) + "**");
        final SimpleFileVisitor<Path> visitor = new CollectingFileVisitor(files, folderMatcher);
        binDirs.parallelStream().forEach(dir -> {
            try {
                Files.walkFileTree(dir, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        final List<Class> result = exctractClasses(packageDir, files);
        if(!result.isEmpty())return result.toArray(new Class[result.size()]);

        return findClassesInJar(packageName);

    }

    private Class[] findClassesInJar(final String packageName) {
        // scan jar files
        final List<List<String>> res = jarsFiles.
                stream().
                map(path -> getClasseNamesInPackage(path, packageName)).
                collect(Collectors.toList());
        final Optional<List<Class>> classes = res.
                stream().
                reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                }).
                map(this::loadClasses);
        if(!classes.isPresent()) return null;
        final List<Class> resultTmp = classes.get();
        return resultTmp.toArray(new Class[resultTmp.size()]);
    }

    private List<Class> loadClasses(List<String> files) {
        return files
                .stream()
                .map(this::loadClass)
                .filter(clazz -> clazz != null)
                .collect(Collectors.toList());
    }

    private List<Class> exctractClasses(final String packageDir, List<String> files) {
        final String seperator = CLASS_PROJECT_SEPERATOR.concat(File.separator);
        return files.parallelStream()
                .filter(classDir -> classDir.contains(packageDir))
                .map(dir -> dir.substring(dir.indexOf(packageDir)))
                .map(subDir -> subDir.replace(File.separator, CLASS_DOT))
                .map(className -> className.substring(0, className
                        .lastIndexOf(CLASS_FILE)))
                .filter(classFile -> !classFile.contains(CLASS_DOLLAR))
                .map(this::loadClass)
                .filter(clazz -> clazz != null)
                .collect(Collectors.toList());

    }

    private Class<?> loadClass(String file) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private String convertPackageToRegex(String packageName) {
        return packageName.replace(CLASS_DOT, FILE_SEPERATOR);
    }

    private class CollectingFileVisitor extends SimpleFileVisitor<Path> {
        private final List<String> files;
        private final PathMatcher folderMatcher;

        public CollectingFileVisitor(final List<String> files, final PathMatcher folderMatcher) {
            this.files = files;
            this.folderMatcher = folderMatcher;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            final boolean matchesFolder = folderMatcher.matches(file);
            if (matchesFolder && matcher.matches(file.getFileName())) {
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
