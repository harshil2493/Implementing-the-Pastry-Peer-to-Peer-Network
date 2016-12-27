package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PeerForwardsRoutingDetailsToPeer {
	LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>> routingTable;
	int hopeTravel;
	int myPortNumber;
	String myHostName;
	String myID;
	ArrayList<String> travelList;

	public void setTravelList(ArrayList<String> travelList) {
		this.travelList = travelList;
	}

	public void setMyPortNumber(int myPortNumber) {
		this.myPortNumber = myPortNumber;
	}

	public void setMyHostName(String myHostName) {
		this.myHostName = myHostName;
	}

	public void setHopeTravel(int hopeTravel) {
		this.hopeTravel = hopeTravel;
	}

	public void setMyID(String myID) {
		this.myID = myID;
	}

	public void setRoutingTable(
			LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>> routingTable) {
		this.routingTable = routingTable;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.JOINING_PROTOCOL_INITIATE);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		byte[] byteLocalIP = myHostName.getBytes();
		int addressLength = byteLocalIP.length;
		dout.writeInt(addressLength);
		dout.write(byteLocalIP);

		int localPortNumber = myPortNumber;
		dout.writeInt(localPortNumber);

		dout.writeInt(hopeTravel);
		for (String ID : travelList) {
			byte[] IDTravel = HexBytes.convertHexToBytes(ID);
			dout.writeInt(IDTravel.length);
			dout.write(IDTravel);
		}
		int sizeOfTable = routingTable.size();
		dout.writeInt(sizeOfTable);
		for (int i = 0; i < sizeOfTable; i++) {
			LinkedHashMap<String, ArrayList<String>> entryInRow = routingTable
					.get(i);
			int sizeOfRow = entryInRow.size();
			dout.writeInt(sizeOfRow);
			for (String keyOfColumn : entryInRow.keySet()) {
				// String keyC = keyOfColumn;
				byte[] stringKeyInBytes = keyOfColumn.getBytes();
				int sizeOfKey = stringKeyInBytes.length;
				dout.writeInt(sizeOfKey);
				dout.write(stringKeyInBytes);

				ArrayList<String> listInCell = entryInRow.get(keyOfColumn);
				int sizeOfCell = listInCell.size();
				dout.writeInt(sizeOfCell);
				// System.out.println("Size Of Cell" + sizeOfCell);
				for (String toSend : listInCell) {
					byte[] dataInCell = toSend.getBytes();
					int sizeOfDataInCell = dataInCell.length;
					dout.writeInt(sizeOfDataInCell);
					dout.write(dataInCell);
				}
			}
		}

		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
