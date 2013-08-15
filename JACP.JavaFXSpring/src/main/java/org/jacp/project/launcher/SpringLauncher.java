/************************************************************************
 * 
 * Copyright (C) 2010 - 2013
 *
 * [SpringLauncher.java]
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
package org.jacp.project.launcher;

import org.jacp.api.dialog.Scope;
import org.jacp.api.launcher.Launcher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The SpringLaucher class resolves the spring.xml file and handles access to
 * beans
 * 
 * @author Andy Moncsek
 * 
 */
public class SpringLauncher implements Launcher<ClassPathXmlApplicationContext> {
	private final ClassPathXmlApplicationContext context;
	private final ConfigurableListableBeanFactory factory;
	private final String BASIC_CONFIG_BEANS = "basic.xml";

	public SpringLauncher(final String resource) {
		this.context = new ClassPathXmlApplicationContext(new String[] {
				resource, this.BASIC_CONFIG_BEANS });
		this.factory = this.context.getBeanFactory();
	}

	@Override
	public ClassPathXmlApplicationContext getContext() {
		return this.context;
	}

	@Override
	public synchronized <E> E getBean(final Class<E> clazz) {
		return this.factory.getBean(clazz);
	}

	@Override
	public synchronized <T> T registerAndGetBean(Class<? extends T> type,
			final String id, final Scope scope) {
		if (this.factory.containsBean(id))
			return getBean(type);
		final AutowireCapableBeanFactory factory = getContext()
				.getAutowireCapableBeanFactory();
		final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
		final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(type);
		if(scope!=null)beanDefinition.setScope(scope.getType());
		beanDefinition.setAutowireCandidate(true);
		registry.registerBeanDefinition(id, beanDefinition);
		factory.autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		return getBean(type);
	}

}
