package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerSendsRegistration {
	String peerIP;
	int peerPort;
	String myID;

	public PeerSendsRegistration(String myIPInString, int myPortInInt,
			String myIDInString) {
		// TODO Auto-generated constructor stub
		this.peerIP = myIPInString;
		this.peerPort = myPortInInt;
		this.myID = myIDInString;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_REGISTRATION);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		byte[] byteLocalIP = peerIP.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = peerPort;
		dout.writeInt(localPortNumber);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
