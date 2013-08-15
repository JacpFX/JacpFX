/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [Tupel.java]
 * AHCP Project (http://jacp.googlecode.com)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacp.api.util;

/**
 * helper class... TODO remove!!!
 * 
 * @author Andy Moncsek
 */
public class Tupel<X, Y> {

	X x;
	Y y;

	public void setX(final X x) {
		this.x = x;
	}

	public void setY(final Y y) {
		this.y = y;
	}

	public X getX() {
		return this.x;
	}

	public Y getY() {
		return this.y;
	}

}
