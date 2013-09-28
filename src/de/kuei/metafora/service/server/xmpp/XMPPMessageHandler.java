package de.kuei.metafora.service.server.xmpp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.service.server.StartupServlet;
import de.kuei.metafora.service.server.couchdb.DocUploadServlet;
import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;
import de.kuei.metafora.service.server.mysql.MysqlConnectorToolState;
import de.kuei.metafora.service.server.xml.Classification;
import de.kuei.metafora.service.server.xml.CommonFormatCreator;
import de.kuei.metafora.service.server.xml.InteractionCreator;
import de.kuei.metafora.service.server.xml.Role;
import de.kuei.metafora.service.server.xml.XMLException;
import de.kuei.metafora.service.server.xml.XMLUtils;
import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.UserToUserChatManager;

public class XMPPMessageHandler implements PacketListener {

	public static final String available = "available";
	public static final String donotdisturb = "do not disturb";
	public static final String freetochat = "free to chat";
	public static final String away = "away";
	public static final String longtimeaway = "long time away";
	public static final String unknown = "unknown";
	public static final String offline = "offline";

	private File logFile;
	private File commandFile;
	private File analysisFile;

	private ExtendedLogDataParser parser;

	public XMPPMessageHandler() {
		parser = new ExtendedLogDataParser();

		try {
			this.logFile = new File("/var/www/metafora/logs/logfileMS.log");
			if (!logFile.exists()) {
				System.err.println(logFile.getAbsolutePath());
				System.err.println(logFile.getPath());
				this.logFile.createNewFile();
			}

			this.commandFile = new File("/var/www/metafora/logs/commandMS.log");
			if (!commandFile.exists())
				this.commandFile.createNewFile();

			this.analysisFile = new File(
					"/var/www/metafora/logs/analysisMS.log");
			if (!analysisFile.exists())
				this.analysisFile.createNewFile();

		} catch (Exception e) {
			System.err.println("XMPPMessageHandler: " + e.getClass().getName()
					+ ": " + e.getMessage());
		}
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message msg = (Message) packet;

			if (msg.getBody() == null) {
				System.err
						.println("MetaforaServiceModul: XMPPMessageHandler: packet dropped because message was null!");
				return;
			}

			String from = msg.getFrom();
			String name = "";
			String chat = from;

			int splitPos = from.indexOf('/');
			if (splitPos > 0) {
				name = from.substring(splitPos + 1, from.length());
				chat = from.substring(0, splitPos);
			}

			String subject = msg.getSubject();
			if (subject == null) {
				subject = "";
			}

			Date time = new Date();

			Collection<PacketExtension> extensions = packet.getExtensions();
			for (PacketExtension e : extensions) {
				if (e instanceof DelayInfo) {
					DelayInfo d = (DelayInfo) e;
					time = d.getStamp();
					break;
				} else if (e instanceof DelayInformation) {
					DelayInformation d = (DelayInformation) e;
					time = d.getStamp();
					break;
				}
			}

			newMessage(name, msg.getBody(), chat, time, msg.getLanguage(),
					subject);
		}
	}

	private void xmppHistoryRequest(String chat, String message, String user) {
		System.err.println("History request from " + chat + "\n" + message);
		try {
			String reqid = null;
			String reqtime = null;
			String endtime = null;
			String channel = "analysis";

			Document doc = XMLUtils.parseXMLString(message, true);
			NodeList properties = doc.getElementsByTagName("property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node property = properties.item(i);
				String name = property.getAttributes().getNamedItem("name")
						.getNodeValue();
				if (name.toLowerCase().equals("request_id")) {
					reqid = property.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (name.toLowerCase().equals("start_time")) {
					reqtime = property.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (name.toLowerCase().equals("end_time")) {
					endtime = property.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
				if (name.toLowerCase().equals("channel")) {
					channel = property.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
			}

			channel = channel.toLowerCase();

			InteractionCreator creator = new InteractionCreator(reqid, channel);

			String answer = MysqlConnectorLogger.getInstance()
					.getHistoryMessages(channel, reqtime, endtime, creator);

			sendAnswer(answer, chat);

		} catch (XMLException e) {
			logMessageToFile(user, e.getMessage(), chat, new Date());
			logMessageToDatabase(user, e.getMessage(), chat, new Date());
			System.err.println("MetaforaServiceModul: xmppHistoryRequst: "
					+ e.getMessage());
		}

	}

	private void pikiStateListRequest(String message, String user, String chat) {
		try {
			String reqtime = null;

			Document doc = XMLUtils.parseXMLString(message, true);
			NodeList properties = doc.getElementsByTagName("property");
			for (int i = 0; i < properties.getLength(); i++) {
				Node property = properties.item(i);
				String name = property.getAttributes().getNamedItem("name")
						.getNodeValue();
				if (name.toLowerCase().equals("time")) {
					reqtime = property.getAttributes().getNamedItem("value")
							.getNodeValue();
				}
			}

			Vector<String> statids = MysqlConnectorToolState.getInstance()
					.getToolStates(reqtime, "PIKI");

			CommonFormatCreator cfc = new CommonFormatCreator(
					System.currentTimeMillis(), Classification.other,
					"PIKI_STATE_LIST", StartupServlet.logged);
			cfc.addProperty("SENDING_TOOL", StartupServlet.metafora);

			cfc.addUser(user, "", Role.originator);
			cfc.setObject("PSL", "PIKI_STATE_LIST");

			for (String stateid : statids)
				cfc.addProperty("ID", stateid);

			cfc.addProperty("RECEIVING_TOOL", "PIKI");

			String answer = cfc.getDocument();

			sendAnswer(answer, chat);
		} catch (XMLException e) {
			logMessageToFile(user, e.getMessage(), chat, new Date());
			logMessageToDatabase(user, e.getMessage(), chat, new Date());
			System.err.println("MetaforaServiceModul: xmppHistoryRequst: "
					+ e.getMessage());
		}

	}

	private void sendAnswer(String answer, String chat) {
		if (chat.contains("conference")) {
			if (chat.startsWith("logger") && StartupServlet.logger != null) {
				StartupServlet.logger.sendMessage(answer);
			} else if (chat.startsWith("command")
					&& StartupServlet.command != null) {
				StartupServlet.command.sendMessage(answer);
			} else if (chat.startsWith("analysis")
					&& StartupServlet.analysis != null) {
				StartupServlet.analysis.sendMessage(answer);
			}
		} else {
			ServerConnection connection = NameConnectionMapper.getInstance()
					.getConnection("MetaforaService");
			if (connection != null) {
				Chat c = UserToUserChatManager.getInstance().openChatWithUser(
						chat, connection);
				try {
					c.sendMessage(answer);
				} catch (XMPPException e) {
					System.err
							.println("MetaforaServiceModul: xmppHistoryRequst: "
									+ e.getMessage());
				}
			}
		}
	}

	private void getPikiId(String message, String user, String chat) {
		try {
			String id = null;

			Document doc = XMLUtils.parseXMLString(message, true);
			NodeList objects = doc.getElementsByTagName("object");
			if (objects.getLength() > 0)
				id = objects.item(0).getAttributes().getNamedItem("id")
						.getNodeValue();
			else
				return;

			int idnum = 1;

			try {
				idnum = Integer.parseInt(id);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: getPikiId: "
						+ e.getMessage());
				return;
			}

			String xmlaction = MysqlConnectorToolState.getInstance()
					.getToolStateById(idnum);
			Document xmlActionDoc = XMLUtils.parseXMLString(xmlaction, false);
			NodeList properties = xmlActionDoc.getElementsByTagName("property");

			CommonFormatCreator cfc = new CommonFormatCreator(
					System.currentTimeMillis(), Classification.other,
					"SEND_PIKI_ID", StartupServlet.logged);
			cfc.addProperty("SENDING_TOOL", StartupServlet.metafora);

			cfc.addUser(user, "", Role.originator);
			cfc.setObject(id, "PIKI_STATE");

			for (int i = 0; i < properties.getLength(); i++) {
				Node property = properties.item(i);
				String name = property.getAttributes().getNamedItem("name")
						.getNodeValue();
				String value = property.getAttributes().getNamedItem("value")
						.getNodeValue();

				cfc.addProperty(name, value);
			}

			cfc.addProperty("RECEIVING_TOOL", "PIKI");

			String answer = cfc.getDocument();

			sendAnswer(answer, chat);
		} catch (XMLException e) {
			logMessageToFile(user, e.getMessage(), chat, new Date());
			logMessageToDatabase(user, e.getMessage(), chat, new Date());
			System.err.println("MetaforaServiceModul: getPikiId: "
					+ e.getMessage());
		}

	}

	private void getToolUploads(String chat, String user) {
		Vector<String[]> documents = DocUploadServlet.getInstance()
				.getToolDocuments();

		try {
			CommonFormatCreator cfc = new CommonFormatCreator(
					System.currentTimeMillis(), Classification.other,
					"TOOL_UPLOAD_LIST", StartupServlet.logged);
			cfc.addProperty("SENDING_TOOL", StartupServlet.metafora);

			cfc.addProperty("baseUrl", StartupServlet.tomcat
					+ "/metaforaservicemodul/metaforaservicemodul/fileupload");

			cfc.setObject("TUL", "TOOL_UPLOAD_LIST");

			for (String[] document : documents) {
				cfc.addProperty(document[0], document[1]);
			}
			cfc.addProperty("RECEIVING_TOOL", "METAFORA");

			String answer = cfc.getDocument();

			sendAnswer(answer, chat);
		} catch (XMLException e) {
			logMessageToFile(user, e.getMessage(), chat, new Date());
			logMessageToDatabase(user, e.getMessage(), chat, new Date());
			e.printStackTrace();
		}

	}

	public void newMessage(String user, String message, String chat, Date time,
			String language, String subject) {

		logMessageToFile(user, message, chat, time);

		logMessageToDatabase(user, message, chat, time);

		if (message
				.replaceAll("\n", "")
				.matches(
						".*[tT][yY][pP][eE][ ]*[=][ ]*[\"']REQUEST_ANALYSIS_HISTORY[\"'].*")
				|| message
						.replaceAll("\n", "")
						.matches(
								".*[tT][yY][pP][eE][ ]*[=][ ]*[\"']REQUEST_HISTORY[\"'].*")) {

			xmppHistoryRequest(chat, message, user);

		} else if (message.replaceAll("\n", "").matches(
				".*[tT][yY][pP][eE][ ]*[=][ ]*[\"]STORE_P[iI]K[iI]_STATE.*")) {

			MysqlConnectorToolState.getInstance().saveToolStateToDatabase(
					"PIKI", time.getTime() + "", message);

		} else if (message.replaceAll("\n", "").matches(
				".*[tT][yY][pP][eE][ ]*[=][ ]*[\"]GET_P[iI]K[iI]_STATE_LIST.*")) {

			pikiStateListRequest(message, user, chat);

		} else if (message.replaceAll("\n", "").matches(
				".*[tT][yY][pP][eE][ ]*[=][ ]*[\"]GET_P[iI]K[iI]_ID.*")) {

			getPikiId(message, user, chat);

		} else if (message.replaceAll("\n", "").matches(
				".*[tT][yY][pP][eE][ ]*[=][ ]*[\"]GET_TOOL_UPLOADS.*")) {

			getToolUploads(chat, user);

		}
	}

	private void logMessageToDatabase(String user, String message, String chat,
			Date time) {
		long id = MysqlConnectorLogger.getInstance().logMessageToDatabase(user,
				message, chat, time);

		message = message.replaceAll("\n", "");
		
		if (parser != null
				&& id >= 0
				&& message
						.matches(".*[aA][cC][tT][iI][oO][nN].*[tT][iI][mM][eE].*")) {

			parser.parseMessage(message, id);
		}
	}

	private void logMessageToFile(String user, String message, String chat,
			Date time) {
		if (chat.toLowerCase().contains("command") && commandFile != null
				&& commandFile.exists()) {
			try {
				FileWriter fw = new FileWriter(this.commandFile, true);
				fw.write(time.toString() + ": " + user + ", " + chat + "\n"
						+ message + "\n\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.err.println("XMPPMessageHandler: logMessageToFile: "
						+ e.getClass().getName() + ": " + e.getMessage());
			}
		} else if (chat.toLowerCase().contains("analysis")
				&& analysisFile != null && analysisFile.exists()) {
			try {
				FileWriter fw = new FileWriter(this.analysisFile, true);
				fw.write(time.toString() + ": " + user + ", " + chat + "\n"
						+ message + "\n\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.err.println("XMPPMessageHandler: logMessageToFile: "
						+ e.getClass().getName() + ": " + e.getMessage());
			}
		} else if (logFile != null && logFile.exists()) {
			try {
				if (!logFile.exists())
					logFile.createNewFile();
				FileWriter fw = new FileWriter(this.logFile, true);
				fw.write(time.toString() + ": " + user + ", " + chat + "\n"
						+ message + "\n\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.err.println("XMPPMessageHandler: logMessageToFile: "
						+ e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}
}
