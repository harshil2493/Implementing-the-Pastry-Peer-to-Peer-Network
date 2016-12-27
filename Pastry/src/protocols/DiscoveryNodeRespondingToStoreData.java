package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DiscoveryNodeRespondingToStoreData {
	int success;
	String peerID;
	String peerIP;
	int peerPort;
	byte[] f;
	byte[] k;
	byte type;

	public DiscoveryNodeRespondingToStoreData(int successMessage,
			String IDOfRandomPeer, String peerIP, int peerPort,
			byte[] fileName, byte[] key, byte typeOfRequest) {
		// TODO Auto-generated constructor stub

		this.success = successMessage;
		this.peerID = IDOfRandomPeer;
		this.peerIP = peerIP;
		this.peerPort = peerPort;
		this.f = fileName;
		this.k = key;
		this.type = typeOfRequest;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.DISCOVERYNODE_RESPONDING_TO_STOREDATA);
		dout.writeInt(success);
		if (success != 0) {
			byte[] IDInBytes = HexBytes.convertHexToBytes(peerID);
			dout.writeInt(IDInBytes.length);
			dout.write(IDInBytes);

			byte[] byteLocalIP = peerIP.getBytes();
			int addressLength = byteLocalIP.length;
			dout.writeInt(addressLength);
			dout.write(byteLocalIP);

			int PortNumber = peerPort;
			dout.writeInt(PortNumber);

			dout.writeInt(f.length);
			dout.write(f);

			dout.writeInt(k.length);
			dout.write(k);

			dout.writeByte(type);
		}

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
