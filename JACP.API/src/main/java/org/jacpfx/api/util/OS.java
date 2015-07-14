package org.jacpfx.api.util;

/**
 * Specifies the OS
 *
 * @author Andy Moncsek
 */
public enum OS {
    MAC("MAC"), UNIX("UNIX"), SOLARIS("SOLARIS"), WINDOWS("WINDOWS"), UNKNOWN(
            "UNKNOWN");
    private static final String osType = System.getProperty("os.name").toLowerCase();
    private final String name;

    private OS(String name) {
        this.name = name;
    }

    public static OS getOS() {
        if (isWindows()) {
            return OS.WINDOWS;
        } else if (isMac()) {
            return OS.MAC;
        } else if (isUnix()) {
            return OS.UNIX;
        } else if (isSolaris()) {
            return OS.SOLARIS;
        } else {
            return OS.UNKNOWN;
        }
    }

    public String getName() {
        return name;
    }

    private static boolean isWindows() {
        // windows
        return osType.contains("win");

    }

    private static boolean isMac() {
        // Mac
        return osType.contains("mac");

    }

    private static boolean isUnix() {
        // linux or unix
        return osType.contains("nix") || osType.contains("nux");

    }

    private static boolean isSolaris() {
        // Solaris
        return osType.contains("sunos");

    }
}
