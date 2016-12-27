package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerSendsRoutingUpdate {
	String myID;
	String peerIP;
	int peerPort;

	public PeerSendsRoutingUpdate(String myID, String myIPInString,
			int myPortInInt) {
		// TODO Auto-generated constructor stub
		this.myID = myID;
		this.peerIP = myIPInString;
		this.peerPort = myPortInInt;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_ROUTING_UPDATE);

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
