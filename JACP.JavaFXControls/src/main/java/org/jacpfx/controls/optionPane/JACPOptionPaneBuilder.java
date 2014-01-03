/************************************************************************
 * 
 * Copyright (C) 2010 - 2014
 *
 * [JACPOptionPaneBuilder.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
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
package org.jacpfx.controls.optionPane;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * The Class JACPOptionPaneBuilder.
 * 
 * @author Patrick Symmangk
 *
 */
public class JACPOptionPaneBuilder {

	/** The on ok message. */
	private EventHandler<ActionEvent> onOkAction;

	/** The on cancel message. */
	private EventHandler<ActionEvent> onCancelAction;

	/** The on yes message. */
	private EventHandler<ActionEvent> onYesAction;

	/** The on no message. */
	private EventHandler<ActionEvent> onNoAction;

	/** The default button. */
	private JACPDialogButton defaultButton;

	/** The title. */
	private String title;

	/** The content. */
	private String content;
	
	private boolean autoHide;


	/**
	 * Gets the on ok message.
	 * 
	 * @return the on ok message
	 */
	private EventHandler<ActionEvent> getOnOkAction() {
		return this.onOkAction;
	}

	/**
	 * Sets the on ok message.
	 * 
	 * @param onOkAction
	 *            the on ok message
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setOnOkAction(
			final EventHandler<ActionEvent> onOkAction) {
		this.onOkAction = onOkAction;
		return this;
	}

	/**
	 * Gets the on cancel message.
	 * 
	 * @return the on cancel message
	 */
	private EventHandler<ActionEvent> getOnCancelAction() {
		return this.onCancelAction;
	}

	/**
	 * Sets the on cancel message.
	 * 
	 * @param onCancelAction
	 *            the on cancel message
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setOnCancelAction(
			final EventHandler<ActionEvent> onCancelAction) {
		this.onCancelAction = onCancelAction;
		return this;
	}

	/**
	 * Gets the on yes message.
	 * 
	 * @return the on yes message
	 */
	private EventHandler<ActionEvent> getOnYesAction() {
		return this.onYesAction;
	}

	/**
	 * Sets the on yes message.
	 * 
	 * @param onYesAction
	 *            the on yes message
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setOnYesAction(
			final EventHandler<ActionEvent> onYesAction) {
		this.onYesAction = onYesAction;
		return this;
	}

	/**
	 * Gets the on no message.
	 * 
	 * @return the on no message
	 */
	private EventHandler<ActionEvent> getOnNoAction() {
		return this.onNoAction;
	}

	/**
	 * Sets the on no message.
	 * 
	 * @param onNoAction
	 *            the on no message
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setOnNoAction(
			final EventHandler<ActionEvent> onNoAction) {
		this.onNoAction = onNoAction;
		return this;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	private String getTitle() {
		return this.title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the title
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setTitle(final String title) {
		this.title = title;
		return this;
	}

	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	private String getContent() {
		return this.content;
	}

	/**
	 * Sets the content.
	 * 
	 * @param content
	 *            the content
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setContent(final String content) {
		this.content = content;
		return this;
	}

	/**
	 * Gets the default button.
	 * 
	 * @return the default button
	 */
	private JACPDialogButton getDefaultButton() {
		return this.defaultButton;
	}

	/**
	 * Sets the default button.
	 * 
	 * @param defaultButton
	 *            the default button
	 * @return the jACP option pane builder
	 */
	public JACPOptionPaneBuilder setDefaultButton(
			final JACPDialogButton defaultButton) {
		this.defaultButton = defaultButton;
		return this;
	}


    private boolean isAutoHide() {
        return autoHide;
    }

    public JACPOptionPaneBuilder setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
        return this;
    }

    /**
     * Builds the.
     * 
     * @return the jACP option pane
     */
    public JACPOptionPane build() {
        // build OptionPane!
        final JACPOptionPane pane = JACPDialogUtil.createOptionPane(
                this.title, this.content);
        if (this.onCancelAction != null) {
            pane.setOnCancelAction(this.onCancelAction);
        }
        if (this.onOkAction != null) {
            pane.setOnOkAction(this.onOkAction);
        }
        if (this.onYesAction != null) {
            pane.setOnYesAction(this.onYesAction);
        }
        if (this.onNoAction != null) {
            pane.setOnNoAction(this.onNoAction);
        }
        pane.setDefaultButton(this.defaultButton);
        pane.setAutoHide(this.autoHide);
        return pane;
    }
}
