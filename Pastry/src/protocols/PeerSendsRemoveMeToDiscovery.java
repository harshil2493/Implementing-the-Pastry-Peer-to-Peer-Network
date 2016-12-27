package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class PeerSendsRemoveMeToDiscovery {
	String myID;
	public PeerSendsRemoveMeToDiscovery(String ID) {
		// TODO Auto-generated constructor stub
		this.myID = ID;
	}
	
	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_REMOVE_ME_TO_DISCOVERY);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
