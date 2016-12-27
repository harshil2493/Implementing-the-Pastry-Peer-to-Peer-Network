package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DiscoveryRespondsToRegistration {
	int success;
	int contactNodePossible;
	String contactID;
	String contactIP;
	int contactPort;
	String IDFinal;
	int leaves;

	public void setLeaves(int leaves) {
		this.leaves = leaves;
	}

	public void setContactNodePossible(int contactNodePossible) {
		this.contactNodePossible = contactNodePossible;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public void setContactID(String contactID) {
		this.contactID = contactID;
	}

	public void setContactIP(String contactIP) {
		this.contactIP = contactIP;
	}

	public void setContactPort(int contactPort) {
		this.contactPort = contactPort;
	}

	public void setID(String possibleID) {
		// TODO Auto-generated method stub
		this.IDFinal = possibleID;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.DISCOVERY_RESPONDS_TO_REGISTRATION);
		dout.writeInt(success);
		if (success != 0) {
			dout.writeInt(contactNodePossible);
			byte[] IDInBytesPeer = HexBytes.convertHexToBytes(IDFinal);
			dout.writeInt(IDInBytesPeer.length);
			dout.write(IDInBytesPeer);

			dout.writeInt(leaves);

			if (contactNodePossible != 0) {
				byte[] IDInBytes = HexBytes.convertHexToBytes(contactID);
				dout.writeInt(IDInBytes.length);
				dout.write(IDInBytes);

				byte[] byteLocalIP = contactIP.getBytes();
				int addressLength = byteLocalIP.length;
				dout.writeInt(addressLength);
				dout.write(byteLocalIP);

				int PortNumber = contactPort;
				dout.writeInt(PortNumber);
			}
		}

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
