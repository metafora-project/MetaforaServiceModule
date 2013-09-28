package de.kuei.metafora.service.server.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Vector;

import de.kuei.metafora.service.server.xml.InteractionCreator;

public class MysqlConnectorLogger {

	public static String url = "jdbc:mysql://metaforaserver.ku.de/logger?useUnicode=true&characterEncoding=UTF-8";
	public static String user = "logwatcher";
	public static String password = "didPfLW";

	private static MysqlConnectorLogger instance = null;

	public static synchronized MysqlConnectorLogger getInstance() {
		if (instance == null) {
			instance = new MysqlConnectorLogger();
		}
		return instance;
	}

	private Connection connection;

	private MysqlConnectorLogger() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			try {
				connection = DriverManager.getConnection(
						MysqlConnectorLogger.url, MysqlConnectorLogger.user,
						MysqlConnectorLogger.password);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private String buildSQL(long id, long idend, String user, String chat,
			long actiontime, long actiontimeend, String actiontype,
			String activitytye, String group, long challenge, long limit,
			long limitstart, boolean actionlogsOnly, boolean messageOnly) {

		String sql = "SELECT ";

		if (messageOnly) {
			sql += "`message` FROM ";
		} else {
			sql += "`id`, `user`, `chat`, `time`, `message`, `actiontime`, `actiontype`, `activitytype`, `group`, `challenge` FROM ";
		}

		if (actionlogsOnly) {
			sql += " actionlogs ";
		} else {
			sql += " fulllogs ";
		}

		if (id != -1 && idend == -1) {
			sql += "WHERE id = " + id + " ";
		} else {
			boolean first = true;

			if (id != -1 && idend != -1) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "id >= " + id + " AND id <= " + idend + " ";
				first = false;
			}

			if (user != null) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "user LIKE '" + user + "' ";
				first = false;
			}

			if (chat != null) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "chat LIKE '" + chat + "' ";
				first = false;
			}

			if (actiontime != -1 && actiontimeend != -1) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "actiontime >= " + actiontime + " AND actiontime <= "
						+ actiontimeend + " ";
				first = false;
			} else if (actiontime != -1 && actiontimeend == -1) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "actiontime = " + actiontime + " ";
				first = false;
			}

			if (actiontype != null) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "actiontype LIKE '" + actiontype + "' ";
				first = false;
			}

			if (activitytye != null) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "activitytype LIKE '" + activitytye + "' ";
				first = false;
			}

			if (group != null) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "`group` LIKE '" + group + "' ";
				first = false;
			}

			if (challenge != -1) {
				if (!first)
					sql += "AND ";
				else
					sql += "WHERE ";
				sql += "challenge = " + challenge + " ";
				first = false;
			}

			if (limitstart != -1 && limit != -1) {
				sql += "LIMIT " + limitstart;
				sql += ", " + limit + " ";
			} else if (limit != -1) {
				sql += "LIMIT " + limit;
			}
		}

		return sql;
	}

	public Vector<String[]> queryLogs(long id, long idend, String user,
			String chat, long actiontime, long actiontimeend,
			String actiontype, String activitytye, String group,
			long challenge, long limit, long limitstart,
			boolean actionlogsOnly, boolean messageOnly) {

		Vector<String[]> result = new Vector<String[]>();

		String sql = buildSQL(id, idend, user, chat, actiontime, actiontimeend,
				actiontype, activitytye, group, challenge, limit, limitstart,
				actionlogsOnly, messageOnly);

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.first()) {
				do {
					if (messageOnly) {
						String[] message = new String[1];
						message[0] = rs.getString("message");
						result.add(message);
					} else {
						String[] message = new String[10];
						message[0] = rs.getString("id");
						message[1] = rs.getString("user");
						message[2] = rs.getString("chat");
						message[3] = rs.getString("time");
						message[4] = rs.getString("message");
						message[5] = rs.getString("actiontime");
						message[6] = rs.getString("actiontype");
						message[7] = rs.getString("activitytype");
						message[8] = rs.getString("group");
						message[9] = rs.getString("challenge");
						result.add(message);
					}
				} while (rs.next());
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public Vector<String[]> getUnextendedLogs() {
		String sql = "SELECT id, message FROM logs WHERE id NOT IN (SELECT id FROM extendedlogs)";

		Vector<String[]> messages = new Vector<String[]>();

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.first()) {
				do {
					String[] message = new String[2];
					message[0] = rs.getString("id");
					message[1] = rs.getString("message");
					messages.add(message);
				} while (rs.next());
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return messages;
	}

	public Vector<String> getTimeLogs() {
		String sql = "SELECT message FROM timelogs LIMIT 3000";

		Vector<String> messages = new Vector<String>();

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.first()) {
				do {
					String message = rs.getString("message");
					messages.add(message);
				} while (rs.next());
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return messages;
	}

	public String getHistoryMessages(String channel, String starttime,
			String endtime, InteractionCreator creator) {

		String sql = null;

		if (channel == null) {
			if (endtime == null) {
				sql = "SELECT * FROM timelogs WHERE time >= " + starttime
						+ " AND chat REGEXP '.*';";
			} else {
				sql = "SELECT * FROM timelogs WHERE time >= " + starttime
						+ " AND time <= " + endtime + " AND chat REGEXP '.*';";
			}
		} else {
			if (endtime == null) {
				sql = "SELECT * FROM timelogs WHERE time >= " + starttime
						+ " AND chat REGEXP '.*" + channel + ".*';";
			} else {
				sql = "SELECT * FROM timelogs WHERE time >= " + starttime
						+ " AND time <= " + endtime + " AND chat REGEXP '.*"
						+ channel + ".*';";
			}
		}

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.first()) {
				do {
					String message = rs.getString("message");
					creator.addAction(message);
				} while (rs.next());
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return creator.getXmlString();
	}

	public long logMessageToDatabase(String user, String message, String chat,
			Date time) {

		Statement stmt;
		long id = -1;
		try {
			stmt = getConnection().createStatement();

			String sql = "INSERT INTO logs(user, chat, time, message) VALUES ('"
					+ user
					+ "','"
					+ chat
					+ "','"
					+ time
					+ "',\""
					+ message.replaceAll("\"", "'") + "\")";

			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

			try {
				ResultSet rs = stmt.getGeneratedKeys();
				rs.first();
				id = rs.getLong(1);
				rs.close();
			} catch (Exception e) {
				System.err
						.println("MetaforaServiceModul: MysqlConncetorLogger: "
								+ e.getClass().getName() + ": "
								+ e.getMessage() + " for SQL: " + sql);
			}

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public void logExtendedDataToDatabase(long id, String actiontime,
			String actiontype, String activitytype, String challenge,
			String group) {
		if (id < 0) {
			System.err
					.println("MefaforaServiceModul: MysqlConnectorLogger: Invalid id "
							+ id);
			return;
		}

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			String sql = "INSERT INTO extendedlogs(`id`, `actiontime`, `actiontype`, `activitytype`, `group`, `challenge`) VALUES ("
					+ id
					+ ", "
					+ ((actiontime != null) ? actiontime : "NULL")
					+ ", "
					+ ((actiontype != null) ? "'" + actiontype + "'" : "NULL")
					+ ", "
					+ ((activitytype != null) ? "'" + activitytype + "'"
							: "NULL")
					+ ", "
					+ ((group != null) ? "'" + group + "'" : "NULL")
					+ ", "
					+ ((challenge != null) ? challenge : "NULL") + ")";

			stmt.execute(sql);

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private Connection getConnection() {
		try {
			if (connection == null || !connection.isValid(5)) {
				connection = DriverManager.getConnection(
						MysqlConnectorLogger.url, MysqlConnectorLogger.user,
						MysqlConnectorLogger.password);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

}
