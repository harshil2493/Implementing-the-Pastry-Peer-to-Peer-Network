package connection;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.Node;

public class TCPReceiverThread implements Runnable {

	private Socket socket;
	private DataInputStream dataInputStream;
	private Node node;

	private static final Logger logger = Logger
			.getLogger(TCPReceiverThread.class.getName());

	TCPReceiverThread(Node node, Socket socket) throws Exception {
		this.socket = socket;
		this.dataInputStream = new DataInputStream(socket.getInputStream());
		this.node = node;
	}

	public void run() {
		try {
			// int dataLenght;
			while (true) {
				// int dataLength;
				int dataLength = dataInputStream.readInt();
				byte[] data = new byte[dataLength];
				dataInputStream.readFully(data);
				node.onEvent(data, socket);

			}
		} catch (Exception e) {
			// e.printStackTrace();
			// logger.log(Level.WARNING,"Receiver Thread Has Encountered Problem In Receiving Data");
			//
			// System.err.println("Exception: " + e.getMessage());
			// e.printStackTrace();

		}
	}

}
