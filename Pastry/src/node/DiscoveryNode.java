package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

import protocols.DiscoveryNodeRespondingToStoreData;
import protocols.HexBytes;
import protocols.Protocol;
import util.InterActiveCommandParser;
import connection.TCPSender;
import connection.TCPServerThread;

public class DiscoveryNode implements Node {

	private final static Logger logger = Logger.getLogger(DiscoveryNode.class
			.getName());

	public int myPortNumber = 0;
	public String myHostName;

	TCPServerThread serverThread;

	public Queue<ArrayList<String>> requestQueue = new LinkedList<ArrayList<String>>();
	AcceptingRequestThread requestProcess;

	public ArrayList<String> registeredNodes = new ArrayList<String>();
	public HashMap<String, ArrayList<String>> nodeInfo = new HashMap<String, ArrayList<String>>();

	public int numberOfLeaves = 1;

	public DiscoveryNode(int portNumber) {
		// TODO Auto-generated constructor stub
		// this.myPortNumber = portNumber;

		String startingMessage = "\nINFO: Starting Discovery Node's Listing Thread.";
		// System.out.println("\nINFO: Starting Discovery Node's Listing Thread.");

		serverThread = new TCPServerThread(portNumber, this);
		Thread threadForFirstTimeConnection = new Thread(serverThread);
		// System.out.println();

		// System.out.println("\nINFO: Starting Discovery Node's Listing Thread."
		// + this.myPortNumber);
		// System.out.println();
		startingMessage = startingMessage
				+ "\nINFO: Starting Peer's Joining Request Protocol.";
		// System.out.println("\nINFO: Starting Peer's Joining Request Protocol");
		requestProcess = new AcceptingRequestThread(this);
		Thread processingThread = new Thread(requestProcess);

		startingMessage = startingMessage
				+ "\nINFO: Starting InterActiveCommandParser";
		// System.out.println("\nINFO: Starting InterActiveCommandParser");
		InterActiveCommandParser activeCommandParser = new InterActiveCommandParser(
				this);
		Thread parserThread = new Thread(activeCommandParser);

		parserThread.start();
		threadForFirstTimeConnection.start();
		processingThread.start();
		System.out.println("INFO: Node StartUp Messages\n" + startingMessage);
	}

	@Override
	public void onEvent(byte[] data, Socket socket) {
		// TODO Auto-generated method stub
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(
					baInputStream));
			byte type = din.readByte();
			switch (type) {
			case Protocol.PEER_SENDS_REGISTRATION:
				int lengthOfID = din.readInt();
				byte[] IDOfPeerByte = new byte[lengthOfID];
				din.readFully(IDOfPeerByte);

				int lengthOfIP = din.readInt();
				byte[] IPOfPeerByte = new byte[lengthOfIP];
				din.readFully(IPOfPeerByte);

				int portOfPeer = din.readInt();

				// System.out.println(HexBytes.convertBytesToHex(IDOfPeerByte));
				// System.out.println(new String(IPOfPeerByte));
				// System.out.println(portOfPeer);
				ArrayList<String> listToProcess = new ArrayList<String>();
				listToProcess.add(HexBytes.convertBytesToHex(IDOfPeerByte));
				listToProcess.add(new String(IPOfPeerByte));
				listToProcess.add(String.valueOf(portOfPeer));
				synchronized (requestQueue) {
					requestQueue.add(listToProcess);

					requestQueue.notifyAll();
					System.out.println("\nINFO: Discovery Has Received Joining Request Of Node " + HexBytes.convertBytesToHex(IDOfPeerByte) + " \nRequest Will Be Added To Queue And When Discovery Is Free To Process The Request, Request Will Be Executed!");
				}

				break;

			case Protocol.STOREDATA_REQUESTING_TO_DISCOVERY:
				int lengthOfIPStore = din.readInt();
				byte[] IPOfStore = new byte[lengthOfIPStore];
				din.readFully(IPOfStore);

				int portOfStore = din.readInt();

				byte[] fileName = new byte[din.readInt()];
				din.readFully(fileName);

				byte[] key = new byte[din.readInt()];
				din.readFully(key);

				byte typeOfRequest = din.readByte();
				System.out
						.println("\nINFO: StoreData Is Requesting Random Peer To Join WithIn The Overlay\nReason Behind Joining Can Be Storing Or Retrieving Of File WIthIn Overlay");

				int successMessage = registeredNodes.size();
				if (successMessage != 0) {
					int randomNumber = new Random().nextInt(registeredNodes
							.size());
					String IDOfRandomPeer = registeredNodes.get(randomNumber);
					String peerIP = nodeInfo.get(IDOfRandomPeer).get(0);
					int peerPort = Integer.parseInt(nodeInfo
							.get(IDOfRandomPeer).get(1));

					DiscoveryNodeRespondingToStoreData respondingToStoreData = new DiscoveryNodeRespondingToStoreData(
							successMessage, IDOfRandomPeer, peerIP, peerPort,
							fileName, key, typeOfRequest);

					Socket tempSocketToStoreData = new Socket(new String(
							IPOfStore), portOfStore);
					try {
						TCPSender sender = new TCPSender(tempSocketToStoreData);
						sender.sendData(respondingToStoreData.getByte());
						System.out
								.println("\nINFO: Discovery Has Sent Details To Store Data");
						tempSocketToStoreData.close();
					} catch (Exception e) {
						System.out
								.println("WARNING: Some Problem Occurred While Returning Random Peer's Detail To StoreData");
					}

				} else {
					// System.out.println(new String(IPOfStore)+""+
					// portOfStore);
					DiscoveryNodeRespondingToStoreData respondingToStoreData = new DiscoveryNodeRespondingToStoreData(
							successMessage, "", "", 0, fileName, key,
							typeOfRequest);
					Socket tempSocketToStoreData = new Socket(new String(
							IPOfStore), portOfStore);

					try {
						TCPSender sender = new TCPSender(tempSocketToStoreData);
						sender.sendData(respondingToStoreData.getByte());
						// System.out.println("\nINFO: Discovery Has Sent Details To Store Data");
						tempSocketToStoreData.close();
					} catch (Exception e) {
						System.out
								.println("WARNING: Some Problem Occurred While Returning Random Peer's Detail To StoreData");
					}
				}
				break;
				
			case Protocol.PEER_SENDS_REMOVE_ME_TO_DISCOVERY:
				int removeIDLength = din.readInt();
				byte[] removeIDBytes = new byte[removeIDLength];
				din.readFully(removeIDBytes);

				String IDToRemoval = HexBytes.convertBytesToHex(removeIDBytes);
				System.out.println("INFO: Exit Request Of Node : " + IDToRemoval + " Is Coming To Discovery!");
				
				registeredNodes.remove(registeredNodes.indexOf(IDToRemoval));
				nodeInfo.remove(IDToRemoval);
				
				System.out
				.println("\nINFO: DataStructure At Discovery After Removal Of Node\nINFO: Total Number Of Registered Nodes: "
						+ registeredNodes.size() + "\nINFO: Nodes: "
						+ registeredNodes + "\nINFO: Node Details: "
						+ nodeInfo);
				
				break;
			case Protocol.PEER_SENDS_ACK:
				int ackIDLength = din.readInt();
				byte[] ackIDBytes = new byte[ackIDLength];
				din.readFully(ackIDBytes);

				int lengthOfIPACK = din.readInt();
				byte[] IPOfACKByte = new byte[lengthOfIPACK];
				din.readFully(IPOfACKByte);

				int portOfACK = din.readInt();

				registeredNodes.add(HexBytes.convertBytesToHex(ackIDBytes));

				// System.out.println("\nINFO: Total Registered Nodes: "
				// + registeredNodes);

				ArrayList<String> infoToPut = new ArrayList<String>();
				infoToPut.add(new String(IPOfACKByte));
				infoToPut.add(String.valueOf(portOfACK));

				nodeInfo.put(HexBytes.convertBytesToHex(ackIDBytes), infoToPut);

				System.out
						.println("\nINFO: After ACK Of Peer: "+ HexBytes.convertBytesToHex(ackIDBytes) +", Peer's Information Storage Structure At Discovery Node\nINFO: Total Number Of Registered Nodes: "
								+ registeredNodes.size() + "\nINFO: Nodes: "
								+ registeredNodes + "\nINFO: Node Details: "
								+ nodeInfo);

				// System.out.println("NODES: " + registeredNodes);

				// System.out.println(HexBytes.convertBytesToHex(ackIDBytes));
				synchronized (requestProcess.lockingObject) {
					requestProcess.lockingObject.notify();
				}

				break;
			default:

				break;
			}
			din.close();
			baInputStream.close();
		} catch (IOException exception) {
			// System.out.println();

			System.err.println("\nDiscovery Failed To Complete Query");
			System.err.println("Exception: " + exception.getMessage());
		}

	}

	public static void main(String[] arguments) {
		try {
			if (arguments.length != 1) {
				// System.out.println();

				System.err
						.println("\nWARNING: Please Enter Valid Number Of Arguments.");

			} else {
				int portNumber = Integer.parseInt(arguments[0]);
				DiscoveryNode discoveryNode = new DiscoveryNode(portNumber);
				// startNode(portNumber);
			}
		} catch (Exception e) {
			// System.out.println();

			System.err
					.println("\nWARNING: Some Exception Happened For Converting Argument To Port Number.");
			System.err.println("Exception: " + e.getMessage());
		}
	}

	@Override
	public void setAddress(String hostName) {
		// TODO Auto-generated method stub
		this.myHostName = hostName;
	}

	@Override
	public void setPort(int localPort) {
		// TODO Auto-generated method stub
		this.myPortNumber = localPort;
	}

}
