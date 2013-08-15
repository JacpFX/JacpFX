package org.jacp.demo.components;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.DeclarativeComponent;
import org.jacp.api.annotations.PostConstruct;
import org.jacp.api.annotations.PreDestroy;
import org.jacp.demo.common.GenderType;
import org.jacp.demo.constants.GlobalConstants;
import org.jacp.demo.entity.Contact;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.component.FXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;

@DeclarativeComponent(defaultExecutionTarget = "PdetailComponent", id = GlobalConstants.ComponentConstants.COMPONENT_DETAIL_VIEW, name = "XMlDetailView", active = true, viewLocation = "/fxml/UserDetail.fxml", resourceBundleLocation = "bundles.languageBundle")
// , localeID="en_US")
public class XMLDetailView implements FXComponent {
	private final static Log LOGGER = LogFactory
			.getLog(XMLDetailView.class);
	@FXML
	private Label lblFirstname;
	@FXML
	private Label lblLastname;
	@FXML
	private Label lblStreet;
	@FXML
	private Label lblZip;
	@FXML
	private Label lblCountry;

	@FXML
	private Pane imagePanel;


	@FXML
	private void handleSubmit(ActionEvent event) {
	}

	@Override
	public Node handle(IAction<Event, Object> action) {
		LOGGER.debug("XMLDetailView handleAction message: "+action.getMessage());
		return null;
	}

	private void fillView(Contact contact) {
		imagePanel.getStyleClass().clear();
		String styleClass = GenderType.FEMALE.getLabel().equals(
				contact.getGender()) ? "female" : "male";
		imagePanel.getStyleClass().add(styleClass);
		lblFirstname.setText(contact.getFirstName());
		lblLastname.setText(contact.getLastName());
		lblZip.setText(contact.getZip());
		lblStreet.setText(contact.getAddress());
		lblCountry.setText(contact.getCountry());
	}

	@Override
	public Node postHandle(final Node node,
			final IAction<Event, Object> action) {
		if (action.isMessageType(Contact.class)) {
			// contact selected
			final Contact contact = (Contact) action.getMessage();
			if (contact != null) {
				fillView(contact);
			}
		}

		return null;

	}

	@PostConstruct
	public void start(FXComponentLayout layout, URL url,
			ResourceBundle resourceBundle) {
        System.out.println("STAR XML: "+layout+"  "+url+"  "+resourceBundle);
	}

	@PreDestroy
	public void stop(FXComponentLayout layout) {
        System.out.println("STOP XML: "+layout);
    }

}
