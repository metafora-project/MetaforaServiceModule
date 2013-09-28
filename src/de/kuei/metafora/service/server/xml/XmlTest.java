package de.kuei.metafora.service.server.xml;

import java.util.Vector;

import org.xml.sax.SAXParseException;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;

public class XmlTest {
	
	private static int messageNr = -1;
	private static int lineNr = -1;
	private static int colNr = -1;
	

	public static void main(String[] args) {
		/*
		 * try { Document doc = XMLUtils.createDocument();
		 * 
		 * Element action = doc.createElement("action");
		 * action.setAttribute("time", "1302691416192");
		 * doc.appendChild(action);
		 * 
		 * Element actiontype = doc.createElement("actiontype");
		 * actiontype.setAttribute("classification", "create");
		 * actiontype.setAttribute("type", "createNode");
		 * actiontype.setAttribute("logged", "true");
		 * action.appendChild(actiontype);
		 * 
		 * Element user = doc.createElement("user"); user.setAttribute("id",
		 * "John"); user.setAttribute("ip", "141.78.96.123");
		 * user.setAttribute("role", "originator"); action.appendChild(user);
		 * 
		 * user = doc.createElement("user"); user.setAttribute("id", "Anna");
		 * user.setAttribute("ip", "141.78.96.123"); user.setAttribute("role",
		 * "originator"); action.appendChild(user);
		 * 
		 * user = doc.createElement("user"); user.setAttribute("id", "Simon");
		 * user.setAttribute("ip", "141.78.96.123"); user.setAttribute("role",
		 * "originator"); action.appendChild(user);
		 * 
		 * Element object = doc.createElement("object");
		 * object.setAttribute("id", "default_node_1");
		 * object.setAttribute("type", "<unser xml>");
		 * action.appendChild(object);
		 * 
		 * String xml = XMLUtils.documentToString(doc,
		 * "http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");
		 * System.out.println(xml);
		 * 
		 * Document doc2 = XMLUtils.parseXMLString(xml, false); NodeList nodes =
		 * doc2.getElementsByTagName("action");
		 * System.out.println(nodes.getLength()); if(nodes.getLength() > 0){
		 * Node node = nodes.item(0);
		 * System.out.println(node.getAttributes().getLength()); Node time =
		 * node.getAttributes().getNamedItem("time"); if(time != null)
		 * System.out.println(time.getTextContent()); }
		 * 
		 * System.err.println("doc3:"); Document doc3 =
		 * XMLUtils.parseXMLString(xml, true);
		 * 
		 * System.err.println("doc4:"); String xml2 =
		 * XMLUtils.documentToString(doc); Document doc4 =
		 * XMLUtils.parseXMLString(xml2, true);
		 * 
		 * 
		 * 
		 * Document content = XMLUtils.createDocument();
		 * 
		 * Element obj = content.createElement("object");
		 * content.appendChild(obj);
		 * 
		 * Element node = content.createElement("node"); node.setAttribute("id",
		 * "default_node_1"); obj.appendChild(node);
		 * 
		 * 
		 * Element graphics = content.createElement("graphics");
		 * node.appendChild(graphics);
		 * 
		 * Element bordercolor = content.createElement("bordercolor");
		 * bordercolor.setAttribute("value", "#FFFFFF");
		 * graphics.appendChild(bordercolor);
		 * 
		 * Element position = content.createElement("position");
		 * position.setAttribute("x", "123"); position.setAttribute("y", "456");
		 * graphics.appendChild(position);
		 * 
		 * Element text = content.createElement("text");
		 * text.setAttribute("value", "Hallo Welt!"); node.appendChild(text);
		 * 
		 * Element properties = content.createElement("properties");
		 * node.appendChild(properties);
		 * 
		 * Element pictureurl = content.createElement("pictureurl");
		 * pictureurl.setAttribute("value", "visualcards/Agreement01.jpg");
		 * properties.appendChild(pictureurl);
		 * 
		 * Element tool = content.createElement("tool");
		 * tool.setAttribute("value",
		 * "http://www.example.com:8080/lasad?autologin=true,...");
		 * properties.appendChild(tool);
		 * 
		 * Element categorie = content.createElement("categorie");
		 * categorie.setAttribute("value", "aCategorie");
		 * properties.appendChild(categorie);
		 * 
		 * Element name = content.createElement("name");
		 * name.setAttribute("value", "aName"); properties.appendChild(name);
		 * 
		 * String contentxml = XMLUtils.documentToString(content,
		 * "http://metafora.ku-eichstaett.de/dtd/planningtoolelement.dtd");
		 * System.out.println(contentxml);
		 * 
		 * System.err.println("vor parse");
		 * 
		 * Document content2 = XMLUtils.parseXMLString(contentxml, true);
		 * 
		 * System.err.println("nach parse");
		 * 
		 * NodeList list = doc.getElementsByTagName("object");
		 * System.out.println(list.getLength());
		 * 
		 * Node o = list.item(0); Element elem = (Element)o;
		 * elem.setAttribute("type", contentxml);
		 * 
		 * String alles = XMLUtils.documentToString(doc,
		 * "http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");
		 * System.out.println(alles);
		 * 
		 * System.err.println("vor parse");
		 * 
		 * Document allesDoc = XMLUtils.parseXMLString(alles, true);
		 * 
		 * System.err.println("nach parse");
		 * 
		 * String innerXml =
		 * allesDoc.getElementsByTagName("object").item(0).getAttributes
		 * ().getNamedItem("type").getNodeValue(); System.out.println(innerXml);
		 * 
		 * System.err.println("vor parse");
		 * 
		 * XMLUtils.parseXMLString(contentxml, true);
		 * 
		 * System.err.println("nach parse");
		 * 
		 * } catch (XMLException e) { e.printStackTrace(); }
		 */

		falscheNachrichten();
	}

	public static void falscheNachrichten() {
		Vector<String> messages = MysqlConnectorLogger.getInstance()
				.getTimeLogs();

		for (int i = 0; i < messages.size(); i++) {
			try {
				XMLUtils.parseXMLString(messages.get(i), false);
			} catch (XMLException e) {
				SAXParseException saxex = (SAXParseException) e.getCause();
				String[] lines = messages.get(i).split("\n");
				char c = 0;
				int pos = -1;
				try{
					String lp = lines[saxex.getLineNumber()-1].substring(0, saxex.getColumnNumber());
					int pos1 = lp.lastIndexOf("&");
					int pos2 = lp.lastIndexOf("<");
					int pos3 = lp.lastIndexOf(">");
					int pos4 = lp.lastIndexOf("\"");
					int pos5 = lp.lastIndexOf("\'");
					pos = -1;
					if(pos1 > pos)
						pos = pos1;
					if(pos2 > pos)
						pos = pos2;
					if(pos3 > pos)
						pos = pos3;
					if(pos4 > pos)
						pos = pos4;
					if(pos5 > pos)
						pos = pos5;
					
					if(pos > -1){
						c = lp.charAt(pos);
					}
				}catch(Exception ex){
					System.out.println(ex.getMessage()+"\n"+e.getMessage()+"\n"+messages.get(i)+"\n\n");
				}
				
				String replace = null;
				if(c != 0){
					replace = maskChar(c);
				}
				
				if (replace != null && !(i == messageNr && lineNr == saxex.getLineNumber() && colNr == saxex.getColumnNumber())) {
					messageNr = i;
					lineNr = saxex.getLineNumber();
					colNr = saxex.getColumnNumber();
					
					System.out.println("repair message "+i+" at line "+saxex.getLineNumber()+" column "+saxex.getColumnNumber()+" ...");
					String line = lines[saxex.getLineNumber()-1];
					String start = line.substring(0, pos);
					String end = line.substring(pos+1,
							line.length());
					line = start + replace + end;
					lines[saxex.getLineNumber()-1] = line;
					String msg = "";
					for (int j = 0; j < lines.length; j++) {
						msg += lines[j] + "\n";
					}
					messages.remove(i);
					messages.insertElementAt(msg, i);
					i--;
				} else if(c != 0){
					System.err.println("Repair failed for char "+c+": \n" + e.getMessage()
							+ "\n" + messages.get(i) + "\n\n");
				}

			}
		}
	}

	public static String maskChar(char c) {
		if (c == '<') {
			return "&lt;";
		} else if (c == '>') {
			return "&gt;";
		} else if (c == '&') {
			return "&amp;";
		} else if (c == '\"') {
			return "&quot;";
		} else if (c == '\'') {
			return "&apos;";
		} else {
			return null;
		}
	}
}
