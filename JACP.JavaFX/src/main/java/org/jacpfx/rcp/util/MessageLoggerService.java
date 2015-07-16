/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
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

package org.jacpfx.rcp.util;

import org.jacpfx.api.message.Message;
import org.jacpfx.api.message.MessageLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Created by Andy Moncsek on 14.07.15.
 */
public class MessageLoggerService implements MessageLogger {
    private static MessageLoggerService service;
    private ServiceLoader<MessageLogger> loader;
    private final List<MessageLogger> logger = new ArrayList();

    private MessageLoggerService() {
        loader = ServiceLoader.load(MessageLogger.class);
        if (loader.iterator().hasNext()) {
            loader.forEach(logger::add);
        } else {
            logger.add(new EmptyLogger());
        }

    }

    public static synchronized MessageLoggerService getInstance() {
        if (service == null) {
            service = new MessageLoggerService();
        }
        return service;
    }

    @Override
    public void onSend(Message m) {
        execute((consumer -> consumer.onSend(m)));
    }


    @Override
    public void handleActive(Message m) {
        execute((consumer -> consumer.handleActive(m)));
    }

    @Override
    public void handleInactive(Message m) {
        execute((consumer -> consumer.handleInactive(m)));
    }

    @Override
    public void handleInCurrentPerspective(Message m) {
        execute((consumer -> consumer.handleInCurrentPerspective(m)));
    }

    @Override
    public void delegate(Message m) {
        execute((consumer -> consumer.delegate(m)));
    }

    @Override
    public void receive(Message m) {
        execute((consumer -> consumer.receive(m)));
    }

    private void execute(Consumer<MessageLogger> c) {
        logger.forEach(c::accept);
    }


    private static class EmptyLogger implements MessageLogger {

        @Override
        public void onSend(Message m) {

        }

        @Override
        public void handleActive(Message m) {

        }

        @Override
        public void handleInactive(Message m) {

        }

        @Override
        public void handleInCurrentPerspective(Message m) {

        }

        @Override
        public void delegate(Message m) {

        }

        @Override
        public void receive(Message m) {

        }
    }
}
