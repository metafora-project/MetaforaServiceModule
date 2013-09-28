package de.kuei.metafora.service.server;

import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.kuei.metafora.service.server.mysql.MysqlConnectorLogger;

public class LogRequestServlet extends HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String query = req.getQueryString();

		HashMap<String, String> parameter = new HashMap<String, String>();

		String[] parts = null;
		if (query != null) {
			parts = query.split("&");

			for (String part : parts) {
				String[] kv = part.split("=");
				if (kv.length == 2) {
					kv[0] = URLDecoder.decode(kv[0], "UTF-8");
					kv[1] = URLDecoder.decode(kv[1], "UTF-8");
					parameter.put(kv[0], kv[1]);
				} else {
					System.err.println("Invalid query: " + part);
				}
			}
		}

		String response = null;
		try {
			response = queryData(parameter);
		} catch (Exception e) {
			response = "Java Exception: " + e.getClass().getName() + ": "
					+ e.getMessage();
			System.err.println("Java Exception: " + e.getClass().getName()
					+ ": " + e.getMessage());
		}

		res.setCharacterEncoding("UTF-8");
		res.setContentType("text/plain");

		Writer writer = res.getWriter();
		writer.write(response);
		writer.flush();
		writer.close();
	}

	private String queryData(HashMap<String, String> parameter) {
		String result = "";

		if (parameter.isEmpty()) {
			return "This servlet querys the log database.\n"
					+ "\n"
					+ "Parameters:\n"
					+ "id\t\t\t\t\tlong\t\tThe logs id into the log table.\n"
					+ "idend\t\t\t\tlong\t\tIf this parameter is used with log id a range can be requested.\n"
					+ "user\t\t\t\tString\t\tThe user name of the xmpp user.\n"
					+ "chat\t\t\t\tString\t\tThe name of the chat. Use 'analysis%' for analysis channel.\n"
					+ "actiontime\t\t\tlong\t\tThe time of the action.\n"
					+ "actiontimeend\t\tlong\t\tIf this parameter is used with actiontime a range can be requested.\n"
					+ "actiontype\t\t\tString\t\tThe actiontype if available.\n"
					+ "activitytype\t\tString\t\tThe actiontype if available.\n"
					+ "group\t\t\t\tString\t\tThe group name if available.\n"
					+ "challenge\t\t\tlong\t\tThe id of the callenge.\n"
					+ "actionsonly\t\tboolean\t\tDefault: true. If this parameter is true only xml action will be returned.\n"
					+ "messageonly\t\tboolean\t\tDefault: true. If this parameter is true only the message will be returned.\n"
					+ "limit\t\t\t\tlong\t\tThis parameter limits the count of the returned messages.\n"
					+ "limitstart\t\t\tlong\t\tIf this parameter can be used with limit. It returns the records starting with the given position.";
		}

		long id = -1;
		long idend = -1;
		String user = null;
		String chat = null;
		long actiontime = -1;
		long actiontimeend = -1;
		String actiontype = null;
		String activitytype = null;
		String group = null;
		long challenge = -1;
		boolean actionlogsOnly = true;
		boolean messageOnly = true;
		long limit = -1;
		long limitstart = -1;

		user = parameter.get("user");
		chat = parameter.get("chat");
		actiontype = parameter.get("actiontype");
		activitytype = parameter.get("activitytype");
		group = parameter.get("group");

		String idText = parameter.get("id");
		if (idText != null) {
			try {
				id = Long.parseLong(idText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				id = -1;
			}
		}

		idText = parameter.get("idend");
		if (idText != null) {
			try {
				idend = Long.parseLong(idText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				idend = -1;
			}
		}

		String timeText = parameter.get("actiontime");
		if (timeText != null) {
			try {
				actiontime = Long.parseLong(timeText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				actiontime = -1;
			}
		}

		timeText = parameter.get("actiontimeend");
		if (timeText != null) {
			try {
				actiontimeend = Long.parseLong(timeText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				actiontimeend = -1;
			}
		}

		String challengeText = parameter.get("challenge");
		if (challengeText != null) {
			try {
				challenge = Long.parseLong(challengeText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				challenge = -1;
			}
		}

		String limitText = parameter.get("limit");
		if (limitText != null) {
			try {
				limit = Long.parseLong(limitText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				limit = -1;
			}
		}

		limitText = parameter.get("limitstart");
		if (limitText != null) {
			try {
				limitstart = Long.parseLong(limitText);
			} catch (Exception e) {
				System.err.println("MetaforaServiceModul: LogRequestServlet: "
						+ e.getMessage());
				limitstart = -1;
			}
		}

		String actionText = parameter.get("actionsonly");
		if (actionText != null) {
			if (actionText.toLowerCase().equals("false")) {
				actionlogsOnly = false;
			} else {
				actionlogsOnly = true;
			}
		}

		String messageText = parameter.get("messageonly");
		if (messageText != null) {
			if (messageText.toLowerCase().equals("false")) {
				messageOnly = false;
			} else {
				messageOnly = true;
			}
		}

		Vector<String[]> queryResult = MysqlConnectorLogger.getInstance()
				.queryLogs(id, idend, user, chat, actiontime, actiontimeend,
						actiontype, activitytype, group, challenge, limit,
						limitstart, actionlogsOnly, messageOnly);

		for (int i = 0; i < queryResult.size(); i++) {
			String[] qr = queryResult.get(i);
			if (qr.length == 1) {
				result += qr[0].replaceAll("\n", "") + "\n";
			} else {
				for (int j = 0; j < qr.length; j++) {
					if (qr[j] == null) {
						result += "NULL\n";
					} else {
						result += qr[j].replaceAll("\n", "") + "\n";
					}
				}
				result += "\n";
			}
		}

		return result;
	}
}
