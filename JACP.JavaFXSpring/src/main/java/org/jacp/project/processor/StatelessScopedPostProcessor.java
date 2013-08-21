/************************************************************************
 * 
 * Copyright (C) 2010 - 2012
 *
 * [StatelessScopedPostProcessor.java]
 * AHCP Project (http://jacp.googlecode.com/)
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
package org.jacp.project.processor;


import org.jacp.api.annotations.component.Stateless;
import org.jacp.javafx.rcp.component.CallbackComponent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
/**
 * This PostProcessr declares all beans with type AStatelessCallbackComponent to scope prototype
 * @author Andy Moncsek
 *
 */
public final class StatelessScopedPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory factory) throws BeansException {
    	final String[] stateless =factory.getBeanNamesForType(CallbackComponent.class);
        for(final String beanName: stateless) {
            final BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
            final String className = beanDefinition.getBeanClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(Stateless.class)) beanDefinition.setScope("prototype");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}