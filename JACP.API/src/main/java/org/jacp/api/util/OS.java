package org.jacp.api.util;

/**
 * Specifies the OS
 * 
 * @author Andy Moncsek
 * 
 */
public enum OS {
	MAC("MAC"), UNIX("UNIX"), SOLARIS("SOLARIS"), WINDOWS("WINDOWS"), UNKNOWN(
			"UNKNOWN");
	private static final String os = System.getProperty("os.name").toLowerCase();
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
		return (os.indexOf("win") >= 0);

	}

	private static boolean isMac() {
		// Mac
		return (os.indexOf("mac") >= 0);

	}

	private static boolean isUnix() {
		// linux or unix
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

	}

	private static boolean isSolaris() {
		// Solaris
		return (os.indexOf("sunos") >= 0);

	}
}
