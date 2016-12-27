package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerACKAboutStoreFile {
	String fileName;
	String ID;

	public PeerACKAboutStoreFile(String file, String myID) {
		// TODO Auto-generated constructor stub
		this.fileName = file;
		this.ID = myID;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_ACK_ABOUT_STORE_FILE);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		dout.writeInt(HexBytes.convertHexToBytes(ID).length);
		dout.write(HexBytes.convertHexToBytes(ID));

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
