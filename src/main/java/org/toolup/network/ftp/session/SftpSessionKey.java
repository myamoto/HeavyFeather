package org.toolup.network.ftp.session;

public  final class SftpSessionKey{
		private String server;
		private int port;
		private String login;
		
		public SftpSessionKey(String server, int port, String login) {
			super();
			this.server = server;
			this.port = port;
			this.login = login;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((login == null) ? 0 : login.hashCode());
			result = prime * result + port;
			result = prime * result + ((server == null) ? 0 : server.hashCode());
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
			SftpSessionKey other = (SftpSessionKey) obj;
			if (login == null) {
				if (other.login != null)
					return false;
			} else if (!login.equals(other.login))
				return false;
			if (port != other.port)
				return false;
			if (server == null) {
				if (other.server != null)
					return false;
			} else if (!server.equals(other.server))
				return false;
			return true;
		}

	}