package util;

import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import node.DiscoveryNode;
import node.Node;
import node.Peer;
import node.StoreData;
import protocols.Protocol;
import protocols.StoreDataRequestingToDiscovery;
import connection.TCPSender;

public class InterActiveCommandParser implements Runnable {
	Node node;

	public InterActiveCommandParser(Node requestedNode) {
		// TODO Auto-generated constructor stub
		this.node = requestedNode;
	}

	@Override
	public void run() {

		// TODO Auto-generated method stub
		while (true) {

			try {

				if (node instanceof DiscoveryNode) {
					DiscoveryNode discoveryNode = (DiscoveryNode) node;
					System.out
							.println("\nINFO: Some Useful Commands In DiscoveryNode"
									+ "\n1. To Change Number Of Leaves In LeafSet Of Peer : set-number-of-leaf NUMBER (Right Now Number Is Set To "
									+ discoveryNode.numberOfLeaves
									+ ") \n2. To View Overlay Details : list-nodes");
					Scanner reader = new Scanner(System.in);

					String command = reader.nextLine();

					if (command.contains("set-number-of-leaf")) {
						String arguments[] = command.split(" ");
						if (arguments.length == 2) {
							if (discoveryNode.registeredNodes.size() == 0) {
								int numberToChange = Integer
										.parseInt(arguments[1]);
								discoveryNode.numberOfLeaves = numberToChange;
								System.out
										.println("\nINFO: Number Of LeafSet Is Changed To "
												+ discoveryNode.numberOfLeaves);
							} else {
								System.out
										.println("\nWARNING: Sorry.. Your Request To Change Number Of LeafSet Cannot Be Executed As Some Peers Are Already Registered With Old Number.. ");
							}
						} else {
							System.err
									.println("\nWARNING: Some Problem With Argument To Change Number Of LeafSet\nTry Again");

						}
					} else if (command.equals("list-nodes")) {
						String toPrint = "List Of All Active Nodes In System\nID\tPeer Details\n";
						for (String ID : discoveryNode.nodeInfo.keySet()) {
							toPrint = toPrint + ID + "\t"
									+ discoveryNode.nodeInfo.get(ID) + "\n";
						}
						System.out.println(toPrint);

					} else {
						System.out
								.println("\nWARNING: Command Is Not Applicable For DiscoveryNode");
					}

				} else if (node instanceof Peer) {
					Peer peer = (Peer) node;
					System.out
							.println("\nINFO: Some Useful Commands In Peer"
									+ "\n1. To See My Routing Table : show-me-routing-table\n2. To See My Leaf Set : show-me-leaf-set\n3. To See Files I Have : show-me-files\n4. To Leave Overlay : exit-me");
					Scanner reader = new Scanner(System.in);

					String command = reader.nextLine();
					if (command.equals("show-me-routing-table")) {
						String routingDetails = "";
						routingDetails = routingDetails
								+ "\nINFO: My Routing Table\n";
						// System.out.println("\nINFO: Before Calling Joining Protocol");
						// System.out.println("INFO: Routing Table");
						for (Integer rows : peer.routingTable.keySet()) {
							// System.out.println(rows + " - "
							// + routingTable.get(rows));

							routingDetails = routingDetails + rows + " - "
									+ peer.routingTable.get(rows) + "\n";
						}
						System.out.println(routingDetails);

					} else if (command.equals("show-me-leaf-set")) {
						String leafDetails = "\nINFO: Leaf Set Details";
						leafDetails = leafDetails + "\nINFO: Left Leaves: "
								+ peer.leftLeaves + "\nINFO: Right Leaves: "
								+ peer.rightLeaves;
						System.out.println(leafDetails);
					} else if (command.equals("show-me-files")) {
						// System.out.println("INFO: Under Construction");
						String filesName = "\nINFO: List Of Files Stored On This Peer\nTotal Number Of Files: "
								+ peer.listOfFiles.size() + "\n";

						int i = 1;
						for (String file : peer.listOfFiles) {
							filesName = filesName + "\nFile Number: " + i
									+ "\nFile Name: ";

							filesName = filesName + file + "\n";

							filesName = filesName + "MetaData Info\n";

							filesName = filesName + peer.metaDataInfo.get(file)
									+ "\n";
							i++;
						}
						System.out.println(filesName);
					} else if (command.equals("exit-me")) {
						System.out
								.println("\nINFO: Node Leaving Protocol has been Initialized...");
						peer.exit();
					} else {
						System.out
								.println("\nWARNING: Command Is Not Applicable For PeerNode");
					}

				} else if (node instanceof StoreData) {

					StoreData data = (StoreData) node;
					System.out
							.println("\nINFO: Some Useful Commands In StoreData"
									+ "\n1. To Store File : store FILENAME\n2. To Retrieve File : read FILENAME");

					Scanner reader = new Scanner(System.in);

					String command = reader.nextLine();

					if (command.startsWith("store")) {

						String fileName = command.split(" ")[1];
						String key;
						if (command.split(" ").length == 3) {
							key = command.split(" ")[2];
						} else {
							key = generateKey(fileName);
							System.out
									.println("\nINFO: Automatic Generation Of Key : "
											+ key);
						}
						// System.out.println(key);

						Socket socketToDiscovery = new Socket(
								data.discoveryHostName, data.discoveryPort);
						TCPSender sender = new TCPSender(socketToDiscovery);

						StoreDataRequestingToDiscovery dataRequestingToDiscovery = new StoreDataRequestingToDiscovery(
								data.myHostName, data.myPortNumber, fileName,
								key, Protocol.STORE_REQUEST);

						sender.sendData(dataRequestingToDiscovery.getByte());

						socketToDiscovery.close();

					} else if (command.startsWith("read")) {
						String fileName = command.split(" ")[1];
						String key;
						if (command.split(" ").length == 3) {
							key = command.split(" ")[2];
						} else {
							key = generateKey(fileName);
							System.out
									.println("\nINFO: Automatic Generation Of Key : "
											+ key);
						}
						// System.out.println(key);

						Socket socketToDiscovery = new Socket(
								data.discoveryHostName, data.discoveryPort);
						TCPSender sender = new TCPSender(socketToDiscovery);

						StoreDataRequestingToDiscovery dataRequestingToDiscovery = new StoreDataRequestingToDiscovery(
								data.myHostName, data.myPortNumber, fileName,
								key, Protocol.READ_REQUEST);

						sender.sendData(dataRequestingToDiscovery.getByte());

						socketToDiscovery.close();
					} else {
						System.out
								.println("\nWARNING: Command Is Not Applicable For StoreData");
					}

				}

			} catch (Exception e) {
				if (!(e instanceof NoSuchElementException)) {
					System.err
							.println("\nWARNING: Some Problem Happened In InterActiveCommandParser");
					System.out.println("Exception: " + e.getMessage());
				}
				// e.printStackTrace();

			}
		}
	}

	private String generateKey(String fileName) throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		// System.out.println(fileName);
		MessageDigest cript = MessageDigest.getInstance("SHA-1");
		cript.reset();
		cript.update(fileName.getBytes());

		StringBuffer sb = new StringBuffer("");

		byte[] mdbytes = cript.digest();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16));
		}
		String finalReturn = "" + sb.toString().charAt(1)
				+ sb.toString().charAt(2) + sb.toString().charAt(4)
				+ sb.toString().charAt(8);
		return finalReturn.toUpperCase();
	}

}
