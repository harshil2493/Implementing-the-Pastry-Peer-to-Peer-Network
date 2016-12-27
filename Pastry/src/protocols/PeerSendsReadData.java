package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerSendsReadData {
	String fileName;
	String myID;
	byte[] data;
	String meta;
	int success;

	public PeerSendsReadData(String file, String ID, byte[] dataRead,
			String metaDetails, int i) {
		// TODO Auto-generated constructor stub
		this.fileName = file;
		this.myID = ID;
		this.data = dataRead;
		this.meta = metaDetails;
		this.success = i;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_READ_DATA);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		dout.writeInt(success);
		if (success == 1) {
			dout.writeInt(data.length);
			dout.write(data);

			dout.writeInt(meta.getBytes().length);
			dout.write(meta.getBytes());
		}
		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
