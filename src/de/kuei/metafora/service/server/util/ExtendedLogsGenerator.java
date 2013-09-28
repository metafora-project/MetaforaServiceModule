package de.kuei.metafora.service.server.util;

import java.util.Vector;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;
import de.kuei.metafora.service.server.xmpp.ExtendedLogDataParser;

public class ExtendedLogsGenerator {

	public static void main(String[] args) {
		//generateExtendedLogs("jdbc:mysql://metafora.ku-eichstaett.de/logger?useUnicode=true&characterEncoding=UTF-8");
		generateExtendedLogs("jdbc:mysql://metaforaserver.ku.de/logger?useUnicode=true&characterEncoding=UTF-8");
	}

	public static void generateExtendedLogs(String databaseurl) {
		MysqlConnectorLogger.url = databaseurl;
		
		Vector<String[]> logs = MysqlConnectorLogger.getInstance()
				.getUnextendedLogs();

		System.err.println(logs.size() + " logs found.");
		
		
		ExtendedLogDataParser parser = new ExtendedLogDataParser();

		// for (String[] log : logs) {
		for (int i = 0; i < 500; i++) {
			String[] log = logs.get(i);

			if (log[1]
					.matches(".*[aA][cC][tT][iI][oO][nN].*[tT][iI][mM][eE].*")) {
				try {
					long id = Long.parseLong(log[0]);

					parser.parseMessage(log[1], id);

				} catch (Exception e) {
					System.err.println(e.getClass().getName() + " Exception: "
							+ e.getMessage());
				}
			}
		}

	}

}
