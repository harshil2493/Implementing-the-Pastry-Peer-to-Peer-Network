package connection;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.Node;
import node.Peer;

public class TCPServerThread implements Runnable {
	int nodePortNumber;
	Node node;
	private static final Logger logger = Logger.getLogger(TCPServerThread.class
			.getName());

	public TCPServerThread(int portNumber, Node requestingNode) {
		// TODO Auto-generated constructor stub
		this.nodePortNumber = portNumber;
		this.node = requestingNode;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket serverSocket = new ServerSocket(nodePortNumber);
			node.setAddress(InetAddress.getLocalHost().getHostName());
			node.setPort(serverSocket.getLocalPort());
			// System.out.println();
			System.out.println("\nINFO: Node Is Listening HOST: "
					+ InetAddress.getLocalHost().getHostName() + " PORT: "
					+ serverSocket.getLocalPort());
			if (node instanceof Peer) {
				// System.out.println();
				System.out
						.println("\nINFO: Connection To Discovery Is Initiated.");
				((Peer) node).startConnectionToDiscovery(InetAddress
						.getLocalHost().getHostName(), serverSocket
						.getLocalPort());
			}
			while (true) {
				Socket socket = serverSocket.accept();

				TCPConnection tcpConnection = new TCPConnection(node, socket);

				boolean connectionSuccess = tcpConnection.startConnection();
				if (connectionSuccess) {
					// System.out.println();
					// logger.log(Level.INFO,
					// "Connection Successfully Established.");
				} else {
					// System.out.println();
					System.out.println("\nINFO: Connection Did Not Establish.");
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// System.out.println();
			System.err
					.println("WARNING: Discovery Cannot Run. Port Number Is Conflicting.");
			System.err.println("Exception: " + e.getMessage());

		}
	}

}
