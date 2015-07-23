/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [SpringJavaConfigLauncher.java]
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
import org.jacpfx.spring.processor.StatelessScopedPostProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by amo on 28.01.14.
 */
public class SpringJavaConfigLauncher implements Launcher<AnnotationConfigApplicationContext> {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext();
    private final ConfigurableListableBeanFactory factory;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SpringJavaConfigLauncher(java.lang.Class<?>... annotatedClasses) {
        context.register(StatelessScopedPostProcessor.class);
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
        lock.readLock().lock();
        try {
            return this.factory.getBean(clazz);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <P> P getBean(final String qualifier) {
        lock.readLock().lock();
        try {
            return (P) this.factory.getBean(qualifier);
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean contains(final String id) {
        lock.readLock().lock();
        try {
            return this.factory.containsBean(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public <P> P registerAndGetBean(Class<? extends P> type, String id, Scope scope) {
        if (contains(id))
            return getBean(id);
        final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(type);
        if (scope != null) beanDefinition.setScope(scope.getType());
        beanDefinition.setAutowireCandidate(true);
        lock.writeLock().lock();
        try {
            final AutowireCapableBeanFactory factory = getContext()
                    .getAutowireCapableBeanFactory();
            final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
            registry.registerBeanDefinition(id, beanDefinition);
            factory.autowireBeanProperties(this,
                    AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        } finally {
            lock.writeLock().unlock();
        }
        return getBean(id);
    }
}
