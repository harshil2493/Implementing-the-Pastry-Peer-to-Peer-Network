package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerSendsLeafUpdate {
	String myID;
	String peerIP;
	int peerPort;
	int side;
	int offset;

	public PeerSendsLeafUpdate(String myID, String myIPInString,
			int myPortInInt, int side, int offset) {
		// TODO Auto-generated constructor stub
		this.myID = myID;
		this.peerIP = myIPInString;
		this.peerPort = myPortInInt;
		this.side = side;
		this.offset = offset;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_LEAF_UPDATE);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		byte[] byteLocalIP = peerIP.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = peerPort;
		dout.writeInt(localPortNumber);

		dout.writeInt(side);
		dout.writeInt(offset);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
