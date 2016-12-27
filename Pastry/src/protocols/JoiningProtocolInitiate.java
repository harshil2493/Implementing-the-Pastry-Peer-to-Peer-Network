package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class JoiningProtocolInitiate {
	String myID;
	String peerIP;
	int peerPort;
	int hopeTravel;

	public JoiningProtocolInitiate(String myID, String myIPInString,
			int myPortInInt, int hope) {
		// TODO Auto-generated constructor stub
		this.myID = myID;
		this.peerIP = myIPInString;
		this.peerPort = myPortInInt;
		this.hopeTravel = hope;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.JOINING_PROTOCOL_INITIATE);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		byte[] byteLocalIP = peerIP.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = peerPort;
		dout.writeInt(localPortNumber);

		dout.writeInt(hopeTravel);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
