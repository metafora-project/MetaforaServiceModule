package de.kuei.metafora.service.server.xml;

import java.util.Vector;

import org.xml.sax.SAXParseException;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;

public class XmlTest {
	
	private static int messageNr = -1;
	private static int lineNr = -1;
	private static int colNr = -1;
	

	public static void main(String[] args) {
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
