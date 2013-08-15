#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.spring.services;

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
