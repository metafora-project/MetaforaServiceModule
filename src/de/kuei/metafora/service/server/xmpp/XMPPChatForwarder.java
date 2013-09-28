package de.kuei.metafora.service.server.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;

public class XMPPChatForwarder implements PacketListener {

	private XmppMUC chat;

	public XMPPChatForwarder(XmppMUC chat) {
		this.chat = chat;
	}

	@Override
	public void processPacket(Packet packet) {
		if (!packet.getFrom().contains("conference")) {
			if (packet instanceof Message) {
				chat.sendMessage(((Message) packet).getBody());
			}
		}
	}
}
