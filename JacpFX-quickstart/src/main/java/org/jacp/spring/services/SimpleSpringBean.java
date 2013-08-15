package org.jacp.spring.services;

import org.springframework.stereotype.Service;

@Service("simpleSpringBean")
/**
 * This is a simple Spring service to show JacpFX - Spring integration. 
 * @author <a href="mailto:amo.ahcp@gmail.com"> Andy Moncsek</a>
 *
 */
public class SimpleSpringBean {
	/**
	 * A simple service method.
	 * @return String
	 */
	public String sayHello() {
		return "hello";
	}
}
