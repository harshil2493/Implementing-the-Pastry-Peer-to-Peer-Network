package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerSendsDataToPeer {
	String fileName;
	String metaData;
	byte[] fileData;
	String ID;

	public PeerSendsDataToPeer(String f, String m, byte[] data, String myID) {
		this.fileName = f;
		this.metaData = m;
		this.fileData = data;
		this.ID = myID;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_DATA_TO_PEER);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		dout.writeInt(metaData.getBytes().length);
		dout.write(metaData.getBytes());

		dout.writeInt(fileData.length);
		dout.write(fileData);
		dout.writeInt(ID.getBytes().length);
		dout.write(ID.getBytes());
		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
