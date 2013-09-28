package de.kuei.metafora.service.server.xmpp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;

public class ExtendedLogDataParser extends DefaultHandler {

	private String actiontime = null;
	private String actiontype = null;
	private String activitytype = null;
	private String group = null;
	private String challenge = null;
	private long id = -1;

	public ExtendedLogDataParser() {
	}

	public synchronized void parseMessage(String message, long id) {
		final Lock lock = new ReentrantLock();
		lock.lock();
		this.id = id;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			ByteArrayInputStream is;
			try {
				is = new ByteArrayInputStream(message.getBytes("UTF-8"));
				try {
					parser.parse(is, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		this.id = -1;
		
		lock.unlock();
	}

	@Override
	public void endDocument() throws SAXException {
		MysqlConnectorLogger.getInstance().logExtendedDataToDatabase(id,
				actiontime, actiontype, activitytype, challenge, group);

		actiontime = null;
		actiontype = null;
		activitytype = null;
		challenge = null;
		group = null;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
		if (actiontime != null || actiontype != null || activitytype != null
				|| challenge != null || group != null) {
			System.err
					.println("ExtendedLogDataParser: There was a variable not null. Something is wrong!");
			actiontime = null;
			actiontype = null;
			activitytype = null;
			challenge = null;
			group = null;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (qName.equals("action")) {
			actiontime = atts.getValue("time");
		} else if (qName.equals("actiontype")) {
			actiontype = atts.getValue("type");
		} else if (qName.equals("property")) {
			String name = atts.getValue("name");
			if (name != null) {
				if (name.toLowerCase().equals("activity_type")) {
					activitytype = atts.getValue("value");
				} else if (name.toLowerCase().equals("group_id")) {
					group = atts.getValue("value");
				} else if (name.toLowerCase().equals("challenge_id")) {
					challenge = atts.getValue("value");
				}
			}
		}
	}
}
