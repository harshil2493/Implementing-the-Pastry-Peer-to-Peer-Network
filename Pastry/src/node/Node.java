package node;

import java.net.Socket;

public interface Node {
	void onEvent(byte[] data, Socket socket);

	void setAddress(String hostName);

	void setPort(int localPort);
}
