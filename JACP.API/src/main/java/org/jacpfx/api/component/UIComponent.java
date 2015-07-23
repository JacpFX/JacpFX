/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [UIComponent.java]
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
package org.jacpfx.api.component;

import org.jacpfx.api.util.UIType;

import java.net.URL;

/**
 * Represents an basic UI component handled by a perspective. A UIComponent is
 * an // * visible UI component displayed in a defined area of perspective.
 *
 * @param <C> defines the base component where others extend from
 * @param <L> defines the message listener type
 * @param <A> defines the basic event type
 * @param <M> defines the basic message type
 * @author Andy Moncsek
 */
public interface UIComponent<C, L, A, M> extends SubComponent<L, A, M> {

    /**
     * Returns the 'root' ui component created by the handle method.
     *
     * @return the root component
     */
    C getRoot();


    /**
     * Set the 'root' ui component created by the handle method.
     *
     * @param root, the UI root
     */
    void setRoot(C root);

    /**
     * Returns the component handle class, this is the users implementation of the component.
     *
     * @param <X>, X extends an ComponentView
     * @return ComponentHandle, the component handle.
     */
    public default <X extends ComponentView<C, A, M>> X getComponentViewHandle() {
        //noinspection unchecked
        return (X) this.getComponent();
    }

    /**
     * Contains the document url describing the UI.
     *
     * @return the document url
     */
    String getViewLocation();

    /**
     * Set the viewLocation location on component start.
     *
     * @param documentURL , the url of the FXML document
     */
    void setViewLocation(final String documentURL);

    /**
     * The document URL describing the UI.
     *
     * @return the document url
     */
    URL getDocumentURL();


    /**
     * Distinguish component types.
     *
     * @return the type of the component.
     */
    UIType getType();

    /**
     * Set the UI type to distinguish component types
     *
     * @param type, PROGRAMMATIC / DECLARATIVE type
     */
    void setUIType(final UIType type);


}
