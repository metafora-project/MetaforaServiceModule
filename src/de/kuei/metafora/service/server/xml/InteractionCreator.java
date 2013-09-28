package de.kuei.metafora.service.server.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.service.server.StartupServlet;

public class InteractionCreator {

	private String id = null;

	private Document allAnswers;
	private Element actions = null;
	private File answerFile = null;
	private String channel = null;

	public InteractionCreator(String id, String channel) {
		this.id = id;
		this.channel = channel;

		answerFile = new File("/var/www/metafora/logs/" + channel + "Request"
				+ id + ".xml");
		try {
			answerFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			allAnswers = XMLUtils.createDocument();
			Element interactiondata = allAnswers
					.createElement("interactiondata");
			interactiondata.setAttribute("id", id);
			allAnswers.appendChild(interactiondata);

			actions = allAnswers.createElement("actions");
			interactiondata.appendChild(actions);

		} catch (XMLException ex) {
			ex.printStackTrace();
		}

	}

	public void addAction(String xml) {
		if (xml == null)
			return;
		else {
			xml = xml.trim();
			xml = xml.replace("\n", "");
		}

		Document actiondoc;
		try {
			actiondoc = XMLUtils.parseXMLString(xml, false);
			NodeList actionList = actiondoc.getElementsByTagName("action");
			for (int i = 0; i < actionList.getLength(); i++) {
				Node actionNode = actionList.item(i);
				actionNode = allAnswers.importNode(actionNode, true);
				this.actions.appendChild(actionNode);
			}
		} catch (XMLException e) {
			System.err
					.println("MetaforaServiceModul: InteractionCreator.addAction(): invalid XML: "
							+ xml);
			e.printStackTrace();
		}
	}

	public String getXmlString() {
		String answer = null;

		try {
			String allAnsXml = XMLUtils.documentToString(allAnswers);
			try {
				FileWriter fw = new FileWriter(answerFile, false);
				fw.write(allAnsXml);
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}

		Document doc;
		try {
			doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");

			if (channel.toLowerCase().equals("analysis")) {
				actiontype.setAttribute("type", "REQUEST_ANALYSIS_ANSWER");
			} else {
				actiontype.setAttribute("type", "REQUEST_HISTORY_ANSWER");
			}
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			Element user = doc.createElement("user");
			user.setAttribute("id", "");
			user.setAttribute("role", "");
			action.appendChild(user);

			Element object = doc.createElement("object");
			object.setAttribute("id", "0");
			object.setAttribute("type", "ELEMENT");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property = doc.createElement("property");
			property.setAttribute("name", "REQUEST_ID");
			property.setAttribute("value", this.id);
			properties.appendChild(property);

			property = doc.createElement("property");
			property.setAttribute("name", "URL");

			property.setAttribute("value", StartupServlet.apache + "/logs/"
					+ channel + "Request" + this.id + ".xml");
			properties.appendChild(property);

			property = doc.createElement("property");
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.metafora);
			properties.appendChild(property);

			property = doc.createElement("property");
			property.setAttribute("name", "RECEIVING_TOOL");
			property.setAttribute("value", "VISUALIZER");
			properties.appendChild(property);

			answer = XMLUtils.documentToString(doc);
		} catch (XMLException e) {
			e.printStackTrace();
		}

		return answer;
	}

}
