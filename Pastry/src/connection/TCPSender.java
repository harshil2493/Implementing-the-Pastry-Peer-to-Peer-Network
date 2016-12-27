package connection;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPSender {

	public Socket socket;
	private DataOutputStream dataOutputStream;

	private static final Logger logger = Logger.getLogger(TCPSender.class
			.getName());

	public TCPSender(Socket socket) throws Exception {
		this.socket = socket;
		this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
	}

	public void sendData(byte[] data) {
		// System.out.println("Called> sendData");
		int dataLength = data.length;
		try {
			dataOutputStream.writeInt(dataLength);
			dataOutputStream.write(data);
			dataOutputStream.flush();

		} catch (Exception e) {
			// System.out.println();
			// logger.log(Level.WARNING,
			// "Sender Has Encountered Problem In Sending Data");
			System.err
					.println("WARNING: Sender Has Encountered Problem In Sending Data");
			System.err.println("Exception: " + e.getMessage());

		}
	}
}
