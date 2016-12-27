package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class StoreDataRequestingToDiscovery {
	String storeIP;
	int storePort;
	String f;
	String k;
	byte messageType;

	public StoreDataRequestingToDiscovery(String IP, int port, String fileName,
			String key, byte storeRequest) {
		// TODO Auto-generated constructor stub
		this.storeIP = IP;
		this.storePort = port;
		this.f = fileName;
		this.k = key;
		this.messageType = storeRequest;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.STOREDATA_REQUESTING_TO_DISCOVERY);

		byte[] byteLocalIP = storeIP.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = storePort;
		dout.writeInt(localPortNumber);

		dout.writeInt(f.getBytes().length);
		dout.write(f.getBytes());

		dout.writeInt(k.getBytes().length);
		dout.write(k.getBytes());

		dout.writeByte(messageType);
		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
