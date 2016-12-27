package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class StoreDataSendsStoringData {
	String fileName;
	String keyFile;
	String userName;
	byte[] fileData;

	public StoreDataSendsStoringData(String file, String key,
			String myNickName, byte[] fileInBytes) {
		// TODO Auto-generated constructor stub
		this.fileName = file;
		this.keyFile = key;
		this.userName = myNickName;
		this.fileData = fileInBytes;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.STOREDATA_SENDS_STORING_DATA);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		dout.writeInt(keyFile.getBytes().length);
		dout.write(keyFile.getBytes());

		dout.writeInt(userName.getBytes().length);
		dout.write(userName.getBytes());

		dout.writeInt(fileData.length);
		dout.write(fileData);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
