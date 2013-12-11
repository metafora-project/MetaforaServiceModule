package de.kuei.metafora.service.server.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class MysqlConnectorToolState {

	public static String url = "jdbc:mysql://metafora-project.info/toolstate?useUnicode=true&characterEncoding=UTF-8";
	public static String user = "toolstate";
	public static String password = "didPfTS";

	private static MysqlConnectorToolState instance = null;

	public static synchronized MysqlConnectorToolState getInstance() {
		if (instance == null) {
			instance = new MysqlConnectorToolState();
		}
		return instance;
	}

	private Connection connection;

	private MysqlConnectorToolState() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			try {
				connection = DriverManager.getConnection(
						MysqlConnectorToolState.url,
						MysqlConnectorToolState.user,
						MysqlConnectorToolState.password);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Vector<String> getToolStates(String time, String tool) {
		String sql;
		if (time != null) {
			sql = "SELECT id FROM toolstates WHERE time >= " + time
					+ " AND tool LIKE '" + tool + "';";
		} else {
			sql = "SELECT id FROM toolstates WHERE tool LIKE '" + tool + "';";
		}

		Vector<String> ids = new Vector<String>();

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			if (rs.first()) {
				do {
					String message = rs.getString("id");
					ids.add(message);
				} while (rs.next());
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ids;
	}

	public String getToolStateById(int id) {
		String sql;

		sql = "SELECT * FROM toolstates WHERE id = " + id + ";";

		String message = null;

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			rs.last();
			int cnt = rs.getRow();

			if (cnt > 1) {
				System.out
						.println("MysqlConnector.getToolStateById(): multiple Rows found!");
			}

			rs.first();
			message = rs.getString("xml");

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return message;
	}

	public void saveToolStateToDatabase(String tool, String time, String xml) {

		Statement stmt;
		try {
			stmt = getConnection().createStatement();

			String sql = "INSERT INTO toolstates(time, tool, xml) VALUES ('"
					+ time + "','" + tool + "',\"" + xml.replaceAll("\"", "'")
					+ "\")";

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
						MysqlConnectorToolState.url,
						MysqlConnectorToolState.user,
						MysqlConnectorToolState.password);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

}
