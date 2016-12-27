package connection;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import node.Node;

public class TCPConnection {
	public TCPReceiverThread receiver;
	public TCPSender sender;
	public Socket s;
	public Node assignedNode;

	private static final Logger logger = Logger.getLogger(TCPConnection.class
			.getName());

	public TCPConnection(Node node, Socket socket) throws Exception {
		// TODO Auto-generated constructor stub

		this.assignedNode = node;
		this.s = socket;

	}

	public boolean startConnection() {
		try {
			receiver = new TCPReceiverThread(assignedNode, s);
			sender = new TCPSender(s);

			Thread threadReceiver = new Thread(receiver);

			threadReceiver.start();
			return true;
		} catch (Exception e) {
			// System.out.println();
			// logger.log(Level.WARNING,
			// "Start Connection Did Not Work Properly");
			System.err
					.println("\nWARNING: Start Connection Did Not Work Properly");
			System.err.println("Exception: " + e.getMessage());
			return false;
		}
	}

}
