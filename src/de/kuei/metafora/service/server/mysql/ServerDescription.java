package de.kuei.metafora.service.server.mysql;

import java.net.MalformedURLException;
import java.net.URL;

public class ServerDescription {

	private String serverUrl;
	private String user;
	private String password;
	private String device;
	private String modul;
	private String connectionname;

	public ServerDescription(String serverUrl, String user, String password,
			String device, String modul, String connectionname) {
		this.serverUrl = serverUrl;
		this.user = user;
		this.password = password;
		this.device = device;
		this.modul = modul;
		this.connectionname = connectionname;
	}

	public boolean requiresLogin() {
		if (user != null) {
			return true;
		}
		return false;
	}

	public String getServer() {
		return serverUrl;
	}

	public URL getServerUrl() {
		URL url = null;
		try {
			url = new URL(serverUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getDevice() {
		return device;
	}

	public String getModul() {
		return modul;
	}

	public String getConnectionName() {
		return connectionname;
	}

}
