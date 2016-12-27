package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class StoreDataRequestingPeer {
	int portNumber;
	String hostName;
	String keyOfFile;
	String fileName;
	int hopeTravel;
	ArrayList<String> travelList;
	byte requestType;

	public StoreDataRequestingPeer(String host, int port, String key,
			String file, ArrayList<String> list, int hopeTravel,
			byte typeOfRequest) {
		// TODO Auto-generated constructor stub
		this.hostName = host;
		this.portNumber = port;
		this.keyOfFile = key;
		this.fileName = file;
		this.travelList = list;
		this.requestType = typeOfRequest;
		this.hopeTravel = hopeTravel;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.STOREDATA_REQUESTING_PEER);

		byte[] IDInBytes = HexBytes.convertHexToBytes(keyOfFile);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		dout.writeInt(fileName.getBytes().length);
		dout.write(fileName.getBytes());

		byte[] byteLocalIP = hostName.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = portNumber;
		dout.writeInt(localPortNumber);

		dout.writeInt(hopeTravel);

		for (String travel : travelList) {
			dout.writeInt(travel.getBytes().length);
			dout.write(travel.getBytes());
		}
		dout.writeByte(requestType);

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
}
