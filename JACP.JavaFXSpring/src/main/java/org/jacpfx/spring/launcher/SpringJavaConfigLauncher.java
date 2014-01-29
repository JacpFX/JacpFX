/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2014
 *
 *  [Component.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacpfx.spring.launcher;

import org.jacpfx.api.fragment.Scope;
import org.jacpfx.api.launcher.Launcher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by amo on 28.01.14.
 */
public class SpringJavaConfigLauncher implements Launcher<AnnotationConfigApplicationContext> {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext();
    private final ConfigurableListableBeanFactory factory;

    public SpringJavaConfigLauncher(java.lang.Class<?>... annotatedClasses) {
        context.register(annotatedClasses);
        context.refresh();
        this.factory = this.context.getBeanFactory();
    }

    @Override
    public AnnotationConfigApplicationContext getContext() {
        return this.context;
    }

    @Override
    public <P> P getBean(Class<P> clazz) {
        return this.factory.getBean(clazz);
    }

    @Override
    public <P> P registerAndGetBean(Class<? extends P> type, String id, Scope scope) {
        if (this.factory.containsBean(id))
            return getBean(type);
        final AutowireCapableBeanFactory factory = getContext()
                .getAutowireCapableBeanFactory();
        final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(type);
        if (scope != null) beanDefinition.setScope(scope.getType());
        beanDefinition.setAutowireCandidate(true);
        registry.registerBeanDefinition(id, beanDefinition);
        factory.autowireBeanProperties(this,
                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        return getBean(type);
    }
}
