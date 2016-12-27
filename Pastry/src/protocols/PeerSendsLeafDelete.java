package protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import node.Peer;

public class PeerSendsLeafDelete {
	String myID;

	int side;
	int offset;
	LinkedHashMap<String, ArrayList<String>> leafNodeInfo;
	ArrayList<String> leftLeaves;
	ArrayList<String> rightLeaves;

	public PeerSendsLeafDelete(String myID, int side, int offset,
			LinkedHashMap<String, ArrayList<String>> leafNodeInfo,
			ArrayList<String> leftLeaves, ArrayList<String> rightLeaves) {
		// TODO Auto-generated constructor stub
		this.myID = myID;

		this.side = side;
		this.offset = offset;

		this.leafNodeInfo = leafNodeInfo;
		this.leftLeaves = leftLeaves;
		this.rightLeaves = rightLeaves;
	}

	public byte[] getByte() throws Exception {
		// TODO Auto-generated method stub

		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(
				baOutputStream));
		dout.write(Protocol.PEER_SENDS_DELETE_LEAF);

		byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
		dout.writeInt(IDInBytes.length);
		dout.write(IDInBytes);

		dout.writeInt(side);
		dout.writeInt(offset);

		if (side < 0) {
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
		} else {
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
		}
		dout.flush();

		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();

		return marshalledBytes;

	}

}
