/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [EmbeddedFXComponent.java]
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

package org.jacpfx.rcp.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.jacpfx.api.component.ComponentView;
import org.jacpfx.api.component.UIComponent;
import org.jacpfx.api.util.UIType;
import org.jacpfx.rcp.context.InternalContext;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Andy Moncsek
 * Date: 02.07.13
 * Time: 21:30
 * This is an implementation of an EmbeddedFXComponent which will be used to encapsulate handles on application startup.
 */
public class EmbeddedFXComponent extends ASubComponent implements
        UIComponent<Node, EventHandler<Event>, Event, Object>,
        Initializable {

    private volatile Node root;
    /**
     * will be set on init
     */
    private String viewLocation;
    /**
     * will be set on init
     */
    private URL documentURL;
    /**
     * will be set on init
     */
    private UIType type = UIType.PROGRAMMATIC;


    public EmbeddedFXComponent(ComponentView handle) {
        this.setComponent(handle);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final Node getRoot() {
        return this.root;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRoot(Node root) {
        this.root = root;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final String getViewLocation() {
        if(type.equals(UIType.PROGRAMMATIC))throw new UnsupportedOperationException("Only supported when @Declarative" +
                " annotation is used");
        return viewLocation;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final void setViewLocation(String document){
        this.viewLocation = document;
        this.type = UIType.DECLARATIVE;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        this.documentURL = url;
        InternalContext.class.cast(this.getContext()).setResourceBundle(resourceBundle);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public final URL getDocumentURL() {
        if(this.type.equals(UIType.PROGRAMMATIC))throw new UnsupportedOperationException("Only supported when @Declarative annotation is used");
        return documentURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final UIType getType() {
        return type;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setUIType(UIType type) {
        this.type = type;
    }



    @Override
    public String toString() {
        return this.getContext() != null ? this.getContext().getId() : this.getComponent().toString();
    }

}

