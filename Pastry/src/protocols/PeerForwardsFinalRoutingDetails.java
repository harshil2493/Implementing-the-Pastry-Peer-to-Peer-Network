package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PeerForwardsFinalRoutingDetails {
	LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>> routingTable;
	ArrayList<String> leftLeaves;
	ArrayList<String> rightLeaves;
	LinkedHashMap<String, ArrayList<String>> leafNodeInfo = new LinkedHashMap<String, ArrayList<String>>();
	int myPortNumber;
	String myHostName;
	String myID;
	int hopeTravel;
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

	public void setLeafNodeInfo(
			LinkedHashMap<String, ArrayList<String>> leafNodeInfo) {
		this.leafNodeInfo = leafNodeInfo;
	}

	public void setLeftLeaves(ArrayList<String> leftLeaves) {
		this.leftLeaves = leftLeaves;
	}

	public void setRightLeaves(ArrayList<String> rightLeaves) {
		this.rightLeaves = rightLeaves;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_FORWARDS_FINAL_ROUTING_DETAILS);
		// Sending Routing Size
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

		// Sending LeftNode
		// System.out.println("Sent Left Node:" + leftLeaves);
		int sizeOfLeftNodeElementSize = leftLeaves.size();
		dout.writeInt(sizeOfLeftNodeElementSize);
		for (String leftNode : leftLeaves) {
			byte[] leftNodeInByte = HexBytes.convertHexToBytes(leftNode);
			dout.writeInt(leftNodeInByte.length);
			dout.write(leftNodeInByte);

			ArrayList<String> myInfo = leafNodeInfo.get(leftNode);
			byte[] leftNodeIP = myInfo.get(0).getBytes();
			dout.writeInt(leftNodeIP.length);
			dout.write(leftNodeIP);

			dout.writeInt(Integer.parseInt(myInfo.get(1)));

		}
		// System.out.println("Sent Right Node:" + rightLeaves);
		// Sending RightNode
		int sizeOfRightNodeElementSize = rightLeaves.size();
		dout.writeInt(sizeOfRightNodeElementSize);
		for (String rightNode : rightLeaves) {
			byte[] rightNodeInByte = HexBytes.convertHexToBytes(rightNode);
			dout.writeInt(rightNodeInByte.length);
			dout.write(rightNodeInByte);

			ArrayList<String> myInfo = leafNodeInfo.get(rightNode);
			byte[] rightNodeIP = myInfo.get(0).getBytes();
			dout.writeInt(rightNodeIP.length);
			dout.write(rightNodeIP);

			dout.writeInt(Integer.parseInt(myInfo.get(1)));

		}

		byte[] IDInB = HexBytes.convertHexToBytes(myID);
		int myIDSize = IDInB.length;
		dout.writeInt(myIDSize);
		dout.write(IDInB);

		byte[] IDIPInB = myHostName.getBytes();
		dout.writeInt(IDIPInB.length);
		dout.write(IDIPInB);

		dout.writeInt(myPortNumber);

		dout.writeInt(hopeTravel);
		for (String ID : travelList) {
			byte[] IDTravel = HexBytes.convertHexToBytes(ID);
			dout.writeInt(IDTravel.length);
			dout.write(IDTravel);
		}
		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}
	// LinkedHashMap<String, ArrayList<String>> leafNodeInfo = new
	// LinkedHashMap<String, ArrayList<String>>();
}
