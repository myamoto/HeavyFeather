package org.toolup.network.ftp.session;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

public final class SftpSession{
	private ChannelSftp channel;
	private Session session;
	public SftpSession(ChannelSftp channel, Session session) {
		super();
		this.channel = channel;
		this.session = session;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SftpSession other = (SftpSession) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		return true;
	}

	public void close() {
		if(channel != null)channel.exit();
		if(session != null)session.disconnect();
	}
	public ChannelSftp getChannel() {
		return channel;
	}

}