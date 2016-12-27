package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class StoreDataSendsReadingRequest {

	String fileName;
	String keyFile;
	String hostName;
	int hostPort;

	public StoreDataSendsReadingRequest(String file, String key, String host,
			int port) {
		// TODO Auto-generated constructor stub
		this.fileName = file;
		this.keyFile = key;
		this.hostName = host;
		this.hostPort = port;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.STOREDATA_SENDS_READING_REQUEST);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		dout.writeInt(keyFile.getBytes().length);
		dout.write(keyFile.getBytes());

		dout.writeInt(hostName.getBytes().length);
		dout.write(hostName.getBytes());

		dout.writeInt(hostPort);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
