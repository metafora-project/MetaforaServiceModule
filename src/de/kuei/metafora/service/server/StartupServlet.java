package de.kuei.metafora.service.server;

import java.util.Vector;

import javax.servlet.http.HttpServlet;

import de.kuei.metafora.service.server.couchdb.DocUploadServlet;
import de.kuei.metafora.service.server.mysql.ChannelDescription;
import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;
import de.kuei.metafora.service.server.mysql.MysqlConnectorToolState;
import de.kuei.metafora.service.server.mysql.MysqlInitConnector;
import de.kuei.metafora.service.server.mysql.ServerDescription;
import de.kuei.metafora.service.server.xmpp.XMPPChatForwarder;
import de.kuei.metafora.service.server.xmpp.XMPPMessageHandler;
import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUCManager;

public class StartupServlet extends HttpServlet {

	public static boolean logged = true;
	public static String metafora = "METAFORA";
	public static String apache = "http://metafora-project.de";
	public static String tomcat = "https://metafora-project.de";

	public static XmppMUC logger;
	public static XmppMUC analysis;
	public static XmppMUC command;

	@Override
	public void init() {

		MysqlInitConnector.getInstance().loadData("MetaforaServiceModul");

		if (MysqlInitConnector.getInstance().getParameter("logged")
				.toLowerCase().equals("false")) {
			logged = false;
		}

		metafora = MysqlInitConnector.getInstance().getParameter("METAFORA");

		ServerDescription apacheServer = MysqlInitConnector.getInstance()
				.getAServer("apache");
		apache = apacheServer.getServer();

		ServerDescription tomcatServer = MysqlInitConnector.getInstance()
				.getAServer("tomcat");
		tomcat = tomcatServer.getServer();

		ServerDescription couchDBServer = MysqlInitConnector.getInstance()
				.getAServer("couchdb");

		// config DocUploadService
		DocUploadServlet.server = couchDBServer.getServer();
		System.err.println("ServiceModul: StartupServlet: CouchDB: "
				+ couchDBServer.getServer());
		DocUploadServlet.user = couchDBServer.getUser();
		DocUploadServlet.password = couchDBServer.getPassword();

		System.err.println("Metafora Service Modul: Apache: "+apache);
		System.err.println("Metafora Service Modul: Apache: "+tomcat);
		System.err.println("Metafora Service Modul: CouchDB: "+DocUploadServlet.user+"@"+DocUploadServlet.server);
		
		// init mysql
		Vector<ServerDescription> mysqlServers = MysqlInitConnector
				.getInstance().getServer("mysql");

		for (ServerDescription mysqlServer : mysqlServers) {
			if (mysqlServer.getConnectionName().equals("LoggerDatabase")) {
				System.err.println(mysqlServer.getConnectionName()
						+ " configured.");
				MysqlConnectorLogger.url = "jdbc:mysql://"
						+ mysqlServer.getServer()
						+ "/logger?useUnicode=true&characterEncoding=UTF-8";
				MysqlConnectorLogger.user = mysqlServer.getUser();
				MysqlConnectorLogger.password = mysqlServer.getPassword();
				System.err.println("Metafora Service Modul: MySQL: "+MysqlConnectorLogger.url);
			} else if (mysqlServer.getConnectionName().equals("ToolsDatabase")) {
				System.err.println(mysqlServer.getConnectionName()
						+ " configured.");
				MysqlConnectorToolState.url = "jdbc:mysql://"
						+ mysqlServer.getServer()
						+ "/toolstate?useUnicode=true&characterEncoding=UTF-8";
				MysqlConnectorToolState.user = mysqlServer.getUser();
				MysqlConnectorToolState.password = mysqlServer.getPassword();
				System.err.println("Metafora Service Modul: MySQL: "+MysqlConnectorToolState.url);
			}
		}

		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		System.err.println("Metafora Service Modul: XMPP server : #"+xmppServers.size());
		
		for (ServerDescription xmppServer : xmppServers) {
			System.err.println("Metafora Service Modul: XMPP server: " + xmppServer.getServer());
			System.err.println("Metafora Service Modul: XMPP user: " + xmppServer.getUser());
			System.err.println("Metafora Service Modul: XMPP password: " + xmppServer.getPassword());
			System.err.println("Metafora Service Modul: XMPP device: " + xmppServer.getDevice());
			System.err.println("Metafora Service Modul: Modul: " + xmppServer.getModul());
			System.err.println("Metafora Service Modul: Connection name: "
					+ xmppServer.getConnectionName());

			System.err.println("Starting XMPP connection...");

			NameConnectionMapper.getInstance().createConnection(
					xmppServer.getConnectionName(), xmppServer.getServer(),
					xmppServer.getUser(), xmppServer.getPassword(),
					xmppServer.getDevice());
			
			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).login();
		}

		ServerConnection connection = NameConnectionMapper.getInstance()
				.getConnection("MetaforaServiceModul");
		if (connection != null) {
			connection.addPacketListener(new XMPPMessageHandler());
		} else {
			System.err.println("StartupServlet: MetaforaService not found!");
		}

		Vector<ChannelDescription> channels = MysqlInitConnector.getInstance()
				.getXMPPChannels();

		System.err.println("MetaforaServiceModul: StartupServlet: "
				+ channels.size() + " XMPP multi user chats found.");

		for (ChannelDescription channeldesc : channels) {
			connection = NameConnectionMapper.getInstance().getConnection(
					channeldesc.getConnectionName());

			if (connection == null) {
				System.err.println("StartupServlet: Unknown connection: "
						+ channeldesc.getUser());
				continue;
			}

			System.err.println("Joining channel " + channeldesc.getChannel()
					+ " as " + channeldesc.getAlias());

			XmppMUC muc = XmppMUCManager.getInstance().getMultiUserChat(
					channeldesc.getChannel(), channeldesc.getAlias(),
					connection);
			muc.join(0);

			if (channeldesc.getConnectionName().equals("MetaforaServiceModul")) {
				if (channeldesc.getChannel().equals("logger")) {
					System.err.println("StartupServlet: logger configured.");
					logger = muc;
				} else if (channeldesc.getChannel().equals("analysis")) {
					System.err.println("StartupServlet: analysis configured.");
					analysis = muc;
				} else if (channeldesc.getChannel().equals("command")) {
					System.err.println("StartupServlet: command configured.");
					command = muc;
				}
			}
		}

		connection = NameConnectionMapper.getInstance().getConnection(
				"MetaforaCommandMessenger");
		if (connection != null && command != null) {
			connection.addPacketListener(new XMPPChatForwarder(command));
		} else {
			System.err
					.println("StartupServlet: MetaforaCommandMessenger not found!");
		}

		connection = NameConnectionMapper.getInstance().getConnection(
				"MetaforaAnalysisMessenger");
		if (connection != null && analysis != null) {
			connection.addPacketListener(new XMPPChatForwarder(analysis));
		} else {
			System.err
					.println("StartupServlet: MetaforaAnalysisMessenger not found!");
		}

		connection = NameConnectionMapper.getInstance().getConnection(
				"MetaforaLoggerMessenger");
		if (connection != null && logger != null) {
			connection.addPacketListener(new XMPPChatForwarder(logger));
		} else {
			System.err
					.println("StartupServlet: MetaforaLoggerMessenger not found!");
		}
	}

	@Override
	public void destroy() {
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		for (ServerDescription xmppServer : xmppServers) {
			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).disconnect();
		}
	}
}
