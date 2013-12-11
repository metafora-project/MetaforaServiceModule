package de.kuei.metafora.service.server.util;

import java.util.Date;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;
import de.kuei.metafora.service.server.xmpp.ExtendedLogDataParser;

public class Test {

	public static void main(String[] args) {
		String message = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE action SYSTEM \"http://data.metafora-project.de/dtd/commonformat.dtd\">\n<action time=\"1361364810681\">\n<actiontype classification=\"modify\" logged=\"false\" type=\"MODIFY_NODE\"/>\n<user id=\"RotemA\" ip=\"1361364555716\" role=\"originator\"/>\n<object id=\"דניאל עדן וטל ^_^_node_80\" type=\"&lt;object&gt;&lt;node id=&quot;דניאל עדן וטל ^_^_node_80&quot;&gt;&lt;graphics&gt;&lt;scalefactor value=&quot;50&quot;/&gt;&lt;bordercolor value=&quot;#AAAAAA&quot;/&gt;&lt;position x=&quot;1404&quot; y=&quot;273&quot;/&gt;&lt;/graphics&gt;&lt;text value=&quot;טל- מחשבת כמה קופסאות טונה ייכנסו בכלי בצורת קופסא.                             דני- מחשב כמה קופסאות טונה ייכנסו בכלי בצורת גליל.                 עדן- עוזרת לחיושבים ומציגה את התשובה    &quot;/&gt;&lt;properties&gt;&lt;pictureurl value=&quot;http://metafora-project.de/images/processes/allocate_roles.svg&quot;/&gt;&lt;tool value=&quot;null&quot;/&gt;&lt;categorie value=&quot;תהליכים&quot;/&gt;&lt;name value=&quot;חלוקת תפקידים&quot;/&gt;&lt;/properties&gt;&lt;/node&gt;&lt;/object&gt;\"/>\n<content>\n<properties>\n<property name=\"SENDING_TOOL\" value=\"PLANNING_TOOL_TEST\"/>\n<property name=\"GROUP_ID\" value=\"Metafora\"/>\n<property name=\"CHALLENGE_ID\" value=\"1\"/>\n<property name=\"CHALLENGE_NAME\" value=\"Demo Challenge\"/>\n<property name=\"ACTIVITY_TYPE\" value=\"MODIFY_POSITION\"/>\n</properties>\n</content>\n</action>";
		String database = "jdbc:mysql://metafora-project.de/logger?useUnicode=true&characterEncoding=UTF-8";
		long id = -1;

		ExtendedLogDataParser parser = new ExtendedLogDataParser();
		
		MysqlConnectorLogger.url = database;

		id = MysqlConnectorLogger.getInstance().logMessageToDatabase("test",
				message, "test", new Date());

		System.err.println("added with id " + id);

		message = message.replaceAll("\n", "");

		if (parser != null
				&& id >= 0
				&& message
						.matches(".*[aA][cC][tT][iI][oO][nN].*[tT][iI][mM][eE].*")) {

			parser.parseMessage(message, id);
		}

	}

}
