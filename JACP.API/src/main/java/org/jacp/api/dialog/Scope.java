package org.jacp.api.dialog;
/**
 * Defines the scope of a dialog
 * @author Andy Moncsek
 *
 */
public enum Scope {
	SINGLETON("singleton"),PROTOTYPE("prototype");
	
	private final String type;
	
	private Scope(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
