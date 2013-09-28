package de.kuei.metafora.service.server.mysql;

public class ChannelDescription {

	private String channel;
	private String alias;
	private String user;
	private String modul;
	private String connectionname;

	public ChannelDescription(String channel, String alias, String user,
			String modul, String connectionname) {
		this.channel = channel;
		this.alias = alias;
		this.user = user;
		this.modul = modul;
		this.connectionname = connectionname;
	}

	public String getChannel() {
		return channel;
	}

	public String getAlias() {
		return alias;
	}

	public String getUser() {
		return user;
	}

	public String getModul() {
		return modul;
	}

	public String getConnectionName() {
		return connectionname;
	}

}
