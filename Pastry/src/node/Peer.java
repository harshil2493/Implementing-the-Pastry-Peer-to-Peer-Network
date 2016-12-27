package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import protocols.HexBytes;
import protocols.JoiningProtocolInitiate;
import protocols.NewPeerRequestFileTransfer;
import protocols.PeerACKAboutStoreFile;
import protocols.PeerCompletingRequest;
import protocols.PeerForwardsFinalRoutingDetails;
import protocols.PeerForwardsRoutingDetailsToPeer;
import protocols.PeerSendsAck;
import protocols.PeerSendsDataToPeer;
import protocols.PeerSendsDeleteUpdate;
import protocols.PeerSendsLeafDelete;
import protocols.PeerSendsLeafUpdate;
import protocols.PeerSendsReadData;
import protocols.PeerSendsRegistration;
import protocols.PeerSendsRemoveMeToDiscovery;
import protocols.PeerSendsRoutingUpdate;
import protocols.Protocol;
import protocols.StoreDataRequestingPeer;
import util.InterActiveCommandParser;
import connection.TCPSender;
import connection.TCPServerThread;

public class Peer implements Node {
	private final static Logger logger = Logger.getLogger(DiscoveryNode.class
			.getName());

	public int myPortNumber = 0;
	public String myHostName;

	public int discoveryPort = 0;
	public String discoveryAddress;
	public String myID;

	public LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>> routingTable = new LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>>();
	public HashMap<String, ArrayList<String>> backUpEntries = new HashMap<String, ArrayList<String>>();

	public int numberOfLeaves = 1;
	public ArrayList<String> leftLeaves = new ArrayList<String>();
	public ArrayList<String> rightLeaves = new ArrayList<String>();
	LinkedHashMap<String, ArrayList<String>> leafNodeInfo = new LinkedHashMap<String, ArrayList<String>>();
	TCPServerThread serverThread;
	Thread threadParser;

	String defaultLocation = Protocol.defaultLocationToStoreFiles;
	public ArrayList<String> listOfFiles = new ArrayList<String>();
	public HashMap<String, String> metaDataInfo = new HashMap<String, String>();

	public Object accessFiles = new Object();

	public Peer(String addressOfDiscovery, int portOfDiscovery, int needPort,
			String possibleID) {
		// TODO Auto-generated constructor stub
		this.myPortNumber = needPort;
		this.discoveryAddress = addressOfDiscovery;
		this.discoveryPort = portOfDiscovery;
		this.myID = possibleID;

		File f = new File(defaultLocation);
		if (f.exists()) {
			System.out
					.println("INFO: Old File Structure Seems To Be Existed.. Peer Startup Is Deleting Old Files.");
			deleteDirectory(f);
		}
		f.mkdirs();
		System.out
				.println("INFO: See "
						+ defaultLocation
						+ " For Viewing All Files In Peers"
						+ " You Can Change Location From InterActiveCommandParser Before New Files Are Getting Added.");
		serverThread = new TCPServerThread(needPort, this);
		Thread peerThread = new Thread(serverThread);

		InterActiveCommandParser activeCommandParser = new InterActiveCommandParser(
				this);
		threadParser = new Thread(activeCommandParser);

		peerThread.start();
	}

	public static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
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

			case Protocol.STOREDATA_SENDS_READING_REQUEST:
				byte[] fileInByteRead = new byte[din.readInt()];
				din.readFully(fileInByteRead);
				String fileToRead = new String(fileInByteRead);

				byte[] keyReadInByte = new byte[din.readInt()];
				din.readFully(keyReadInByte);
				String keyRead = new String(keyReadInByte);

				byte[] hostInByte = new byte[din.readInt()];
				din.readFully(hostInByte);
				String hostSD = new String(hostInByte);

				int portSD = din.readInt();

				// System.out.println(fileToRead + keyRead + hostSD + portSD);

				if (fileToRead.startsWith("/"))
					fileToRead = fileToRead.substring(1);
				// file = defaultLocation + file;
				String finalString = defaultLocation + fileToRead;

				String metaDetails = metaDataInfo.get(finalString);
				File fileRead = new File(finalString);
				if (fileRead.exists()) {
					byte[] fileInBytes = new byte[(int) fileRead.length()];

					FileInputStream fileInputStream = new FileInputStream(
							fileRead);
					// convert file into array of bytes
					// fileInputStream
					fileInputStream.read(fileInBytes);
					fileInputStream.close();

					try {
						Socket socketToReadFile = new Socket(hostSD, portSD);

						TCPSender senderToReadFile = new TCPSender(
								socketToReadFile);

						PeerSendsReadData peerSendsReadData = new PeerSendsReadData(
								fileToRead, myID, fileInBytes, metaDetails, 1);

						senderToReadFile.sendData(peerSendsReadData.getByte());

						socketToReadFile.close();
					} catch (Exception e) {
						System.out
								.println("WARNING: Exception While Sending File "
										+ fileToRead + " To StoreData");
						e.printStackTrace();
					}
				} else {
					System.out
							.println("WARNING: File Does Not Exist On Node..! Sending Failure Message To StoreData");
					byte[] fileInBytes = "No File".getBytes();
					try {
						Socket socketToReadFile = new Socket(hostSD, portSD);

						TCPSender senderToReadFile = new TCPSender(
								socketToReadFile);
						metaDetails = "No MetaData";

						PeerSendsReadData peerSendsReadData = new PeerSendsReadData(
								fileToRead, myID, fileInBytes, metaDetails, 0);

						senderToReadFile.sendData(peerSendsReadData.getByte());

						socketToReadFile.close();
					} catch (Exception e) {
						System.out
								.println("WARNING: Exception While Sending File "
										+ fileToRead + " To StoreData");
						e.printStackTrace();
					}
				}
				break;
			case Protocol.STOREDATA_SENDS_STORING_DATA:
				// dout.writeInt(fileName.getBytes().length);
				// dout.write(fileName.getBytes());
				//
				// dout.writeInt(keyFile.getBytes().length);
				// dout.write(keyFile.getBytes());
				//
				// dout.writeInt(userName.getBytes().length);
				// dout.write(userName.getBytes());
				//
				// dout.writeInt(fileData.length);
				// dout.write(fileData);
				byte[] fileInByte = new byte[din.readInt()];
				din.readFully(fileInByte);
				String file = new String(fileInByte);

				byte[] keyFileInByte = new byte[din.readInt()];
				din.readFully(keyFileInByte);
				String keyFile = new String(keyFileInByte);

				byte[] userInByte = new byte[din.readInt()];
				din.readFully(userInByte);
				String user = new String(userInByte);

				byte[] dataInByte = new byte[din.readInt()];
				din.readFully(dataInByte);
				// System.out.println("INFO: " + file + keyFile + user);
				if (file.startsWith("/"))
					file = file.substring(1);
				String actualFileName = file;
				file = defaultLocation + file;
				int lastIndex = file.lastIndexOf("/");
				String directory = file.substring(0, lastIndex);
				// String fileNameGot = file.substring(lastIndex).split("\\.");
				String metaDataFile = file + Protocol.defaultKeyWordForMetaData;
				// if(fileNameGot.length == 2) metaDataFile = metaDataFile +
				// fileNameGot[1];

				// System.out.println("Directory: " + directory);
				// System.out.println(fileNameGot);
				if (!new File(directory).exists())
					new File(directory).mkdirs();
				// System.out.println("FILE:" + file);
				// System.out.println("Meta: " + metaDataFile);
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(dataInByte);
				fos.close();
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				FileOutputStream fileOutputStream = new FileOutputStream(
						metaDataFile);
				String metaInfo = "FileLocation:\n" + file
						+ "\nVersion:\n1.0\nOwner:\n" + user + "\nKeyUsed:\n"
						+ keyFile + "\nTimeStamp:\n" + timestamp.toString();
				fileOutputStream.write(metaInfo.getBytes());
				fileOutputStream.close();

				listOfFiles.add(file);
				metaDataInfo.put(file, metaInfo);

				PeerACKAboutStoreFile aboutStoreFile = new PeerACKAboutStoreFile(
						actualFileName, myID);
				try {

					user = user.substring(5);
					// user.replace("_PORT_", " ");
					String[] userInfo = user.split("_PORT_");
					Socket s = new Socket(userInfo[0],
							Integer.parseInt(userInfo[1]));
					TCPSender senderTemp = new TCPSender(s);
					senderTemp.sendData(aboutStoreFile.getByte());
					System.out
							.println("\nINFO: Storing Has Been Done Successfully.. \nINFO: ACK Regarding Storing Of File Has Been Sent..");
					s.close();
				} catch (Exception e) {
					System.out
							.println("WARNING: Exception While Sending Acknoledgement Regarding Success Of Storing File "
									+ actualFileName + " To StoreData");
					e.printStackTrace();
				}
				// System.out.println(listOfFiles);
				// System.out.println(metaDataInfo);
				break;
			case Protocol.DISCOVERY_RESPONDS_TO_REGISTRATION:
				int success = din.readInt();
				if (success != 0) {
					threadParser.start();
					int firstNode = din.readInt();
					int lengthOfMyID = din.readInt();
					byte[] IDOfContactByteMy = new byte[lengthOfMyID];
					din.readFully(IDOfContactByteMy);
					this.numberOfLeaves = din.readInt();
					String startUpMessages = "INFO: Number Of Leaves Set To "
							+ this.numberOfLeaves;
					// System.out.println("INFO: Number Of Leaves Set To " +
					// this.numberOfLeaves);
					this.myID = HexBytes.convertBytesToHex(IDOfContactByteMy);

					initializingTables(this.myID);
					startUpMessages = startUpMessages
							+ "\nINFO: Before Calling Joining Protocol\nINFO: Routing Table\n";
					// System.out.println("\nINFO: Before Calling Joining Protocol");
					// System.out.println("INFO: Routing Table");
					for (Integer rows : routingTable.keySet()) {
						// System.out.println(rows + " - "
						// + routingTable.get(rows));

						startUpMessages = startUpMessages + rows + " - "
								+ routingTable.get(rows) + "\n";
					}
					startUpMessages = startUpMessages + "\nINFO: Left Leaves: "
							+ leftLeaves + "\nINFO: Right Leaves: "
							+ rightLeaves;
					// System.out.println("INFO: Left Leaves: " + leftLeaves);
					// System.out.println("INFO: Right Leaves: " + rightLeaves);

					// System.out.println("Official It Is: " + myID);
					if (firstNode == 0) {
						// System.out.println();
						// System.out
						// .println("\nINFO: Congratulations!. This Peer Is Registered First In Overlay With ID Of: "
						// + myID);

						startUpMessages = startUpMessages
								+ "\nINFO: Congratulations!. This Peer Is Registered First In Overlay With ID Of: "
								+ myID;
						System.out.println(startUpMessages);
						byte[] rawData = { Protocol.JOINING_PROTOCOL_COMPLETED };
						this.onEvent(rawData, socket);
					} else {
						// Start Joining Protocol
						// System.out.println();
						// System.out
						// .println("\nINFO: This Peer Can Register In Overlay With ID Of: "
						// + myID);

						startUpMessages = startUpMessages
								+ "\nINFO: This Peer Can Register In Overlay With ID Of: "
								+ myID;

						System.out.println(startUpMessages);
						int lengthOfID = din.readInt();
						byte[] IDOfContactByte = new byte[lengthOfID];
						din.readFully(IDOfContactByte);

						int lengthOfContactIP = din.readInt();
						byte[] IPOfContactByte = new byte[lengthOfContactIP];
						din.readFully(IPOfContactByte);

						int portOfContact = din.readInt();

						startJoiningProtocol(
								HexBytes.convertBytesToHex(IDOfContactByte),
								new String(IPOfContactByte), portOfContact);
					}
				} else {

					System.err
							.println("\nWARNING: Peer Failed To Complete Registartion."
									+ "\nRejection Message Came From Discovery."
									+ "\nProblem Happened Because Of Peer Is Requesting Already Registered ID.");
					System.out
							.println("\nPlease Enter New ID In Hexadecimals Only: ");
					// System.out.println(myHostName);
					// System.out.println(myPortNumber);
					Scanner reader = new Scanner(System.in);

					String newID = reader.nextLine();
					if (newID.length() == 4) {
						myID = newID;
						startConnectionToDiscovery(discoveryAddress,
								discoveryPort);
					}
				}
				break;
			case Protocol.JOINING_PROTOCOL_COMPLETED:
				String afterJoiningProtocol = "\nINFO: After Calling Joining Protocol\nINFO: Routing Table\n";
				// System.out.println("\nINFO: After Calling Joining Protocol");
				// System.out.println("INFO: Routing Table");
				for (Integer rows : routingTable.keySet()) {
					// System.out.println(rows + " - " +
					// routingTable.get(rows));
					afterJoiningProtocol = afterJoiningProtocol + rows + " - "
							+ routingTable.get(rows) + "\n";
				}

				afterJoiningProtocol = afterJoiningProtocol
						+ "\nINFO: Leaves Set\nINFO: Left Leaves : "
						+ leftLeaves + "\nINFO: Right Leaves : " + rightLeaves;
				// System.out.println("INFO: Leaves Set");
				// System.out.println("INFO: Left Leaves : " + leftLeaves);
				// System.out.println("INFO: Right Leaves : " + rightLeaves);

				PeerSendsAck ack = new PeerSendsAck(myID, myHostName,
						myPortNumber);

				try {
					Socket socketTemporaryToDiscovery = new Socket(
							discoveryAddress, discoveryPort);
					TCPSender senderToDiscovery = new TCPSender(
							socketTemporaryToDiscovery);
					senderToDiscovery.sendData(ack.getByte());
					// System.out.println();

					System.out
							.println(afterJoiningProtocol
									+ "\nINFO: Joining Protocol Has Been Successfully Completed.\nINFO: Peer Is Acknoledging To Discovery Node");
					socketTemporaryToDiscovery.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.err.println("\nException: " + e.getMessage());

					// e.printStackTrace();
				}

				break;

			case Protocol.PEER_FORWARDS_FINAL_ROUTING_DETAILS:
				// int sizeOfTable = routingTable.size();
				// dout.writeInt(sizeOfTable);
				// for(int i = 0 ; i < sizeOfTable ; i++)
				// {
				// LinkedHashMap<String, ArrayList<String>> entryInRow =
				// routingTable.get(i);
				// int sizeOfRow = entryInRow.size();
				// dout.writeInt(sizeOfRow);
				// for(String keyOfColumn : entryInRow.keySet())
				// {
				// // String keyC = keyOfColumn;
				// byte[] stringKeyInBytes = keyOfColumn.getBytes();
				// int sizeOfKey = stringKeyInBytes.length;
				// dout.writeInt(sizeOfKey);
				// dout.write(stringKeyInBytes);
				//
				// ArrayList<String> listInCell = entryInRow.get(keyOfColumn);
				// int sizeOfCell = listInCell.size();
				// for(String toSend : listInCell)
				// {
				// byte[] dataInCell = toSend.getBytes();
				// int sizeOfDataInCell = dataInCell.length;
				// dout.writeInt(sizeOfDataInCell);
				// dout.write(dataInCell);
				// }
				// }
				// }
				try {
					int sizeOfTable = din.readInt();
					// System.out.println("Size Of Table: " + sizeOfTable);
					for (int i = 0; i < sizeOfTable; i++) {
						LinkedHashMap<String, ArrayList<String>> entryToRow = routingTable
								.get(i);
						int numberOfElement = din.readInt();
						// System.out.println("Number Of Elements In Row " + i +
						// " :" + numberOfElement);
						for (int j = 0; j < numberOfElement; j++) {
							int sizeOfKey = din.readInt();
							byte[] keyInByte = new byte[sizeOfKey];
							din.readFully(keyInByte);
							String keyInString = new String(keyInByte);
							// System.out.println("Key: " + keyInString);

							ArrayList<String> listInCell = new ArrayList<String>();
							int sizeOfCell = din.readInt();
							// System.out.println("Number Of Details In Cell" +
							// sizeOfCell);
							for (int k = 0; k < sizeOfCell; k++) {
								int sizeOfelement = din.readInt();
								byte[] elementInByte = new byte[sizeOfelement];
								din.readFully(elementInByte);
								String element = new String(elementInByte);
								listInCell.add(element);

							}
							if (entryToRow.get(keyInString).size() == 0) {
								// if (!entryToRow.get(keyInString).get(0)
								// .equals("Reserved")) {
								entryToRow.put(keyInString, listInCell);
								// }
							} else {
								if (!entryToRow.get(keyInString).get(0)
										.equals(Protocol.reservedKeyWord)) {
									entryToRow.put(keyInString, listInCell);
								}
							}
						}
					}
					// System.out.println(routingTable);

					// Sending LeftNode
					// int sizeOfLeftNodeElementSize = leftLeaves.size();
					// dout.writeInt(sizeOfLeftNodeElementSize);
					// for (String leftNode : leftLeaves) {
					// byte[] leftNodeInByte =
					// HexBytes.convertHexToBytes(leftNode);
					// dout.writeInt(leftNodeInByte.length);
					// dout.write(leftNodeInByte);
					//
					// ArrayList<String> myInfo = leafNodeInfo.get(leftNode);
					// byte[] leftNodeIP = myInfo.get(0).getBytes();
					// dout.writeInt(leftNodeIP.length);
					// dout.write(leftNodeIP);
					//
					// dout.writeInt(Integer.parseInt(myInfo.get(1)));
					//
					// }
					//
					// // Sending RightNode
					// int sizeOfRightNodeElementSize = rightLeaves.size();
					// dout.writeInt(sizeOfRightNodeElementSize);
					// for (String rightNode : rightLeaves) {
					// byte[] rightNodeInByte =
					// HexBytes.convertHexToBytes(rightNode);
					// dout.writeInt(rightNodeInByte.length);
					// dout.write(rightNodeInByte);
					//
					// ArrayList<String> myInfo = leafNodeInfo.get(rightNode);
					// byte[] rightNodeIP = myInfo.get(0).getBytes();
					// dout.writeInt(rightNodeIP.length);
					// dout.write(rightNodeIP);
					//
					// dout.writeInt(Integer.parseInt(myInfo.get(1)));
					//
					// }
					int leftLeaveSize = din.readInt();
					for (int i = 0; i < leftLeaveSize; i++) {
						int sizeOfID = din.readInt();
						byte[] byteID = new byte[sizeOfID];
						din.readFully(byteID);

						String IDOfL = HexBytes.convertBytesToHex(byteID);

						leftLeaves.add(IDOfL);

						int IPSize = din.readInt();
						byte[] IPByte = new byte[IPSize];
						din.readFully(IPByte);
						ArrayList<String> nodeInfoOfLeaves = new ArrayList<String>();
						nodeInfoOfLeaves.add(new String(IPByte));
						nodeInfoOfLeaves.add(String.valueOf(din.readInt()));
						leafNodeInfo.put(IDOfL, nodeInfoOfLeaves);

					}
					// System.out.println("Left Recieved: " + leftLeaves);
					int rightLeaveSize = din.readInt();
					for (int i = 0; i < rightLeaveSize; i++) {
						int sizeOfID = din.readInt();
						byte[] byteID = new byte[sizeOfID];
						din.readFully(byteID);

						String IDOfL = HexBytes.convertBytesToHex(byteID);

						rightLeaves.add(IDOfL);

						int IPSize = din.readInt();
						byte[] IPByte = new byte[IPSize];
						din.readFully(IPByte);
						ArrayList<String> nodeInfoOfLeaves = new ArrayList<String>();
						nodeInfoOfLeaves.add(new String(IPByte));
						nodeInfoOfLeaves.add(String.valueOf(din.readInt()));

						leafNodeInfo.put(IDOfL, nodeInfoOfLeaves);

					}
					// System.out.println("Right Recieved: " + rightLeaves);
					// System.out.println(leftLeaves);
					// System.out.println(rightLeaves);
					// byte[] IDInB = HexBytes.convertHexToBytes(myID);
					// int myIDSize = IDInB.length;
					// dout.writeInt(myIDSize);
					// dout.write(IDInB);
					//
					// byte[] IDIPInB = myHostName.getBytes();
					// dout.writeInt(IDIPInB.length);
					// dout.write(IDIPInB);
					//
					// dout.writeInt(myPortNumber);
					//
					// dout.writeInt(hopeTravel);

					int finalPeersIDSize = din.readInt();
					byte[] finalPeersIDInB = new byte[finalPeersIDSize];
					din.readFully(finalPeersIDInB);
					String finalPeer = HexBytes
							.convertBytesToHex(finalPeersIDInB);

					int finalIP = din.readInt();
					byte[] finalIPB = new byte[finalIP];
					din.readFully(finalIPB);
					String IPFinal = new String(finalIPB);

					int port = din.readInt();
					ArrayList<String> finalInfo = new ArrayList<String>();
					finalInfo.add(IPFinal);
					finalInfo.add(String.valueOf(port));
					leafNodeInfo.put(finalPeer, finalInfo);
					int distance = compareHope(myID, finalPeer);
					if (distance > 0) {
						rightLeaves.add(0, finalPeer);
						if (rightLeaves.size() > numberOfLeaves) {
							rightLeaves.remove(rightLeaves.size() - 1);
						}
						if (leftLeaves.size() < numberOfLeaves) {
							// System.out
							// .println("Add To Right.. But Left Not Full");
							leftLeaves.add(leftLeaves.size(), finalPeer);
						}
						// if(rightLeaves.size() > numberOfLeaves)
						// {
						// rightLeaves.remove(rightLeaves.size() - 1);
						// }
					} else {
						leftLeaves.add(0, finalPeer);
						if (leftLeaves.size() > numberOfLeaves) {
							leftLeaves.remove(leftLeaves.size() - 1);
						}
						if (rightLeaves.size() < numberOfLeaves) {
							rightLeaves.add(rightLeaves.size(), finalPeer);
							// System.out
							// .println("Add To Left.. But Right Not Full");
						}

					}

					// System.out.println("Right: " + rightLeaves);
					// System.out.println("Left: " + leftLeaves);
					// System.out.println("Left: " + leftLeaves);
					// System.out.println("Right: " + rightLeaves);
					// System.out.println("Final Info: " + leafNodeInfo);
					int hopeTraveled = din.readInt();

					ArrayList<String> travelList = new ArrayList<String>();
					for (int i = 0; i < (hopeTraveled); i++) {
						int l = din.readInt();
						byte[] byteID = new byte[l];
						din.readFully(byteID);
						travelList.add(HexBytes.convertBytesToHex(byteID));
					}
					System.out
							.println("\nNode Can Officially Join.. \nFor Finishing Joining Protocol.. \nHope Travel: "
									+ hopeTraveled
									+ "\nTravel List: "
									+ travelList);
					// System.out.println();
					int offset = 0;
					for (String leftNodes : leftLeaves) {

						try {
							ArrayList<String> leftInfo = leafNodeInfo
									.get(leftNodes);
							Socket temporarySocket = new Socket(
									leftInfo.get(0), Integer.parseInt(leftInfo
											.get(1)));
							TCPSender sender = new TCPSender(temporarySocket);

							PeerSendsLeafUpdate leafUpdate = new PeerSendsLeafUpdate(
									myID, myHostName, myPortNumber, 1, offset);
							sender.sendData(leafUpdate.getByte());
							offset++;
							temporarySocket.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					offset = 0;
					for (String rightNodes : rightLeaves) {

						try {
							ArrayList<String> rightInfo = leafNodeInfo
									.get(rightNodes);
							Socket temporarySocket = new Socket(
									rightInfo.get(0),
									Integer.parseInt(rightInfo.get(1)));
							TCPSender sender = new TCPSender(temporarySocket);

							PeerSendsLeafUpdate leafUpdate = new PeerSendsLeafUpdate(
									myID, myHostName, myPortNumber, -1, offset);
							sender.sendData(leafUpdate.getByte());
							offset++;
							temporarySocket.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					offset = 0;

					String appendingUpdate = "";
					for (int i = 0; i < routingTable.size(); i++) {
						// System.out.println("Called---" + i);
						char foundChar = myID.charAt(i);
						LinkedHashMap<String, ArrayList<String>> getList = routingTable
								.get(i);
						// System.out.println("List--" + i + " List" + getList);
						for (int j = 0; j <= 15; j++) {
							String keyPut = appendingUpdate
									+ Integer.toHexString(j).toUpperCase();

							// System.out.println("key" + keyPut);

							ArrayList<String> listOnCell = getList.get(keyPut);
							// System.out.println(listOnCell);
							if (!(listOnCell.size() == 0)) {
								if (!listOnCell.get(0).equals(
										Protocol.reservedKeyWord)) {
									// System.out.println("Sending To" +
									// listOnCell.get(0));
									try {

										Socket temporaryUpdate = new Socket(
												listOnCell.get(1),
												Integer.parseInt(listOnCell
														.get(2)));

										TCPSender sender = new TCPSender(
												temporaryUpdate);
										PeerSendsRoutingUpdate routingUpdate = new PeerSendsRoutingUpdate(
												myID, myHostName, myPortNumber);

										sender.sendData(routingUpdate.getByte());
										temporaryUpdate.close();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}

						}

						appendingUpdate = appendingUpdate + foundChar;
					}

					System.out.println("INFO: Requesting File Transfer!");
					String leftNode = leftLeaves.get(0);
					String leftNodeHost = leafNodeInfo.get(leftNode).get(0);
					int leftNodePort = Integer.parseInt(leafNodeInfo.get(
							leftNode).get(1));

					String rightNode = rightLeaves.get(0);
					String rightNodeHost = leafNodeInfo.get(rightNode).get(0);
					int rightNodePort = Integer.parseInt(leafNodeInfo.get(
							rightNode).get(1));

					// System.out.println(leftNode + leftNodeHost + leftNodePort
					// + rightNode + rightNodeHost + rightNodePort);

					Socket socketToLeft = new Socket(leftNodeHost, leftNodePort);
					TCPSender senderToLeft = new TCPSender(socketToLeft);
					NewPeerRequestFileTransfer fileTransfer = new NewPeerRequestFileTransfer(
							myID, myHostName, myPortNumber);
					senderToLeft.sendData(fileTransfer.getByte());
					socketToLeft.close();
					if (!rightNode.equals(leftNode)) {
						Socket socketToRight = new Socket(rightNodeHost,
								rightNodePort);
						TCPSender senderToRight = new TCPSender(socketToRight);
						NewPeerRequestFileTransfer fileTransferToR = new NewPeerRequestFileTransfer(
								myID, myHostName, myPortNumber);
						senderToRight.sendData(fileTransferToR.getByte());
						socketToRight.close();
					}
					System.out
							.println("INFO: Request File Transfer Has Been Sent");
					// System.out.println("Called?");
					byte[] rawData = { Protocol.JOINING_PROTOCOL_COMPLETED };
					this.onEvent(rawData, socket);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				break;

			case Protocol.PEER_SENDS_DATA_TO_PEER:

				// dout.writeInt(fileName.getBytes().length);
				// dout.write(fileName.getBytes());
				//
				// dout.writeInt(metaData.getBytes().length);
				// dout.write(metaData.getBytes());
				//
				// dout.writeInt(fileData.length);
				// dout.write(fileData);
				synchronized (accessFiles) {
					byte[] fileTransfer = new byte[din.readInt()];
					din.readFully(fileTransfer);

					byte[] metaTrasfer = new byte[din.readInt()];
					din.readFully(metaTrasfer);

					byte[] dataTransfer = new byte[din.readInt()];
					din.readFully(dataTransfer);

					byte[] sendID = new byte[din.readInt()];
					din.readFully(sendID);

					String fileT = new String(fileTransfer);
					int lastIndexT = fileT.lastIndexOf("/");
					String directoryT = fileT.substring(0, lastIndexT);
					// String fileNameGot =
					// file.substring(lastIndex).split("\\.");
					String metaDataFileT = fileT
							+ Protocol.defaultKeyWordForMetaData;
					// if(fileNameGot.length == 2) metaDataFile = metaDataFile +
					// fileNameGot[1];

					// System.out.println("Directory: " + directory);
					// System.out.println(fileNameGot);
					if (!new File(directoryT).exists())
						new File(directoryT).mkdirs();
					// System.out.println("FILE:" + file);
					// System.out.println("Meta: " + metaDataFile);
					FileOutputStream fosT = new FileOutputStream(fileT);
					fosT.write(dataTransfer);
					fosT.close();

					FileOutputStream fileOutputStreamT = new FileOutputStream(
							metaDataFileT);
					String metaInfoT = new String(metaTrasfer);
					fileOutputStreamT.write(metaInfoT.getBytes());
					fileOutputStreamT.close();

					listOfFiles.add(fileT);
					metaDataInfo.put(fileT, metaInfoT);

					System.out
							.println("\nINFO: File Has Been Received From Neighbor.. File: "
									+ fileT
									+ " Neighbor: "
									+ new String(sendID));

					// System.out.println(listOfFiles);
					// System.out.println(metaDataInfo);

				}

				break;
			case Protocol.NEW_PEER_REQUEST_FILE_TRANSFER:
				int newIDLength = din.readInt();
				byte[] newIDBytes = new byte[newIDLength];
				din.readFully(newIDBytes);

				int lengthOfIPNew = din.readInt();
				byte[] IPOfNewByte = new byte[lengthOfIPNew];
				din.readFully(IPOfNewByte);

				int portOfNew = din.readInt();

				String newPeer = (HexBytes.convertBytesToHex(newIDBytes));
				ArrayList<String> needToRemove = new ArrayList<String>();
				for (String fileNameStored : listOfFiles) {
					String[] metaString = metaDataInfo.get(fileNameStored)
							.split("\n");

					String keyOfFile = (metaString[7]);

					int fileToMyDistance = compareHope(myID, keyOfFile);
					int fileToNewNodeDistance = compareHope(newPeer, keyOfFile);

					if (fileToMyDistance < 0)
						fileToMyDistance = fileToMyDistance * -1;
					if (fileToNewNodeDistance < 0)
						fileToNewNodeDistance = fileToNewNodeDistance * -1;

					if (fileToNewNodeDistance < fileToMyDistance) {
						System.out.println("\nINFO: Need To Transfer File .."
								+ fileNameStored + " To New Peer " + newPeer);

						File fileToSend = new File(new String(fileNameStored));

						byte[] fileInBytes = new byte[(int) fileToSend.length()];

						FileInputStream fileInputStream = new FileInputStream(
								fileToSend);
						// convert file into array of bytes
						// fileInputStream
						fileInputStream.read(fileInBytes);
						fileInputStream.close();

						PeerSendsDataToPeer dataToPeer = new PeerSendsDataToPeer(
								fileNameStored,
								metaDataInfo.get(fileNameStored), fileInBytes,
								myID);
						try {
							Socket sTOp = new Socket(new String(IPOfNewByte),
									portOfNew);
							TCPSender senderToPeer = new TCPSender(sTOp);
							senderToPeer.sendData(dataToPeer.getByte());
							sTOp.close();
							needToRemove.add(fileNameStored);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (fileToMyDistance == fileToNewNodeDistance) {
						if (myID.compareTo(newPeer) < 0) {
							System.out
									.println("\nINFO: Need To Transfer File .."
											+ fileNameStored
											+ " To New Peer "
											+ newPeer
											+ " Even Though Distance To Both Nodes Are Same..");

							File fileToSend = new File(new String(
									fileNameStored));

							byte[] fileInBytes = new byte[(int) fileToSend
									.length()];

							FileInputStream fileInputStream = new FileInputStream(
									fileToSend);
							// convert file into array of bytes
							// fileInputStreamneedToRemove.add(fileNameStored);
							fileInputStream.read(fileInBytes);
							fileInputStream.close();

							PeerSendsDataToPeer dataToPeer = new PeerSendsDataToPeer(
									fileNameStored,
									metaDataInfo.get(fileNameStored),
									fileInBytes, myID);
							try {
								Socket sTOp = new Socket(
										new String(IPOfNewByte), portOfNew);
								TCPSender senderToPeer = new TCPSender(sTOp);
								senderToPeer.sendData(dataToPeer.getByte());
								sTOp.close();
								needToRemove.add(fileNameStored);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

				}
				for (String removeIt : needToRemove) {
					System.out.println("\nINFO: Removing File " + removeIt
							+ " From File System");
					listOfFiles.remove(listOfFiles.indexOf(removeIt));
					metaDataInfo.remove(removeIt);

					File fileR = new File(removeIt);
					File fileM = new File(removeIt
							+ Protocol.defaultKeyWordForMetaData);
					if (fileR.exists()) {
						fileR.delete();
						fileM.delete();
					}
				}
				break;
			case Protocol.PEER_SENDS_DELETE_LEAF:
				int leafDeleteIDLength = din.readInt();
				byte[] leafDeleteIDB = new byte[leafDeleteIDLength];
				din.readFully(leafDeleteIDB);
				String deleteLeaf = HexBytes.convertBytesToHex(leafDeleteIDB);

				int sideToDelete = din.readInt();
				int offsetToDelete = din.readInt();

				if (sideToDelete > 0) {
					// rightLeaves.add(offsetToPut, leaf);
					// if (rightLeaves.size() > numberOfLeaves) {
					// rightLeaves.remove(rightLeaves.size() - 1);
					// }
					int startingSizeOfR = rightLeaves.size();
					rightLeaves.subList(offsetToDelete, rightLeaves.size())
							.clear();

					// System.out.println("Left Recieved: " + leftLeaves);
					boolean allowedToPut = true;

					int rightLeaveSize = din.readInt();
					for (int i = 0; i < rightLeaveSize; i++) {
						int sizeOfID = din.readInt();
						byte[] byteID = new byte[sizeOfID];
						din.readFully(byteID);

						String IDOfL = HexBytes.convertBytesToHex(byteID);

						int IPSize = din.readInt();
						byte[] IPByte = new byte[IPSize];
						din.readFully(IPByte);
						ArrayList<String> nodeInfoOfLeaves = new ArrayList<String>();
						nodeInfoOfLeaves.add(new String(IPByte));
						nodeInfoOfLeaves.add(String.valueOf(din.readInt()));

						if (allowedToPut) {
							if (!IDOfL.equals(myID)) {
								rightLeaves.add(offsetToDelete + i, IDOfL);
								leafNodeInfo.put(IDOfL, nodeInfoOfLeaves);
							} else {
								allowedToPut = false;
							}
						}
						if (rightLeaves.size() == startingSizeOfR) {
							allowedToPut = false;
						}

					}

				} else if (sideToDelete < 0) {
					// leftLeaves.add(offsetToPut, leaf);
					// if (leftLeaves.size() > numberOfLeaves) {
					// leftLeaves.remove(leftLeaves.size() - 1);
					// }

					int startingSizeOfL = leftLeaves.size();
					leftLeaves.subList(offsetToDelete, leftLeaves.size())
							.clear();

					boolean allowedToPut = true;

					int leftLeaveSize = din.readInt();
					for (int i = 0; i < leftLeaveSize; i++) {
						int sizeOfID = din.readInt();
						byte[] byteID = new byte[sizeOfID];
						din.readFully(byteID);

						String IDOfL = HexBytes.convertBytesToHex(byteID);

						int IPSize = din.readInt();
						byte[] IPByte = new byte[IPSize];
						din.readFully(IPByte);
						ArrayList<String> nodeInfoOfLeaves = new ArrayList<String>();
						nodeInfoOfLeaves.add(new String(IPByte));
						nodeInfoOfLeaves.add(String.valueOf(din.readInt()));

						if (allowedToPut) {
							if (!IDOfL.equals(myID)) {
								leftLeaves.add(offsetToDelete + i, IDOfL);
								leafNodeInfo.put(IDOfL, nodeInfoOfLeaves);
							} else {
								allowedToPut = false;
							}
						}
						if (leftLeaves.size() == startingSizeOfL) {
							allowedToPut = false;
						}

					}
				}
				String leafDelete = "\nINFO: Leaf Update Request Is Received From "
						+ deleteLeaf
						+ "\nINFO: Left Leaves: "
						+ leftLeaves
						+ "\nINFO: Right Leaves: "
						+ rightLeaves
						+ "\nINFO: Leaves Information: " + leafNodeInfo;
				// System.out
				// .println("\nINFO: Leaf Update Request Is Received From "
				// + leaf);
				//
				// System.out.println("INFO: Left Leaves: " + leftLeaves);
				// System.out.println("INFO: Right Leaves: " + rightLeaves);
				// System.out.println("INFO: Leaves Information: " +
				// leafNodeInfo);
				System.out.println(leafDelete);

				break;
			case Protocol.PEER_SENDS_LEAF_UPDATE:
				// byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
				// dout.writeInt(IDInBytes.length);
				// dout.write(IDInBytes);
				//
				// byte[] byteLocalIP = peerIP.getBytes();
				// int addressLength = byteLocalIP.length;
				// dout.writeInt(addressLength);
				// dout.write(byteLocalIP);
				//
				// int localPortNumber = peerPort;
				// dout.writeInt(localPortNumber);
				//
				// dout.writeInt(side);
				// dout.writeInt(offset);

				int leafIDLength = din.readInt();
				byte[] leafIDBytes = new byte[leafIDLength];
				din.readFully(leafIDBytes);
				String leaf = HexBytes.convertBytesToHex(leafIDBytes);
				ArrayList<String> infoOfLeafToPut = new ArrayList<String>();
				int lengthOfIPLeaf = din.readInt();
				byte[] IPOfLeafByte = new byte[lengthOfIPLeaf];
				din.readFully(IPOfLeafByte);
				infoOfLeafToPut.add(new String(IPOfLeafByte));
				int portOfLeaf = din.readInt();
				infoOfLeafToPut.add(String.valueOf(portOfLeaf));
				int side = din.readInt();
				int offsetToPut = din.readInt();

				leafNodeInfo.put(leaf, infoOfLeafToPut);
				if (side > 0) {
					rightLeaves.add(offsetToPut, leaf);
					if (rightLeaves.size() > numberOfLeaves) {
						rightLeaves.remove(rightLeaves.size() - 1);
					}
				} else if (side < 0) {
					leftLeaves.add(offsetToPut, leaf);
					if (leftLeaves.size() > numberOfLeaves) {
						leftLeaves.remove(leftLeaves.size() - 1);
					}
				}
				String leafUpdate = "\nINFO: Leaf Update Request Is Received From "
						+ leaf
						+ "\nINFO: Left Leaves: "
						+ leftLeaves
						+ "\nINFO: Right Leaves: "
						+ rightLeaves
						+ "\nINFO: Leaves Information: " + leafNodeInfo;
				// System.out
				// .println("\nINFO: Leaf Update Request Is Received From "
				// + leaf);
				//
				// System.out.println("INFO: Left Leaves: " + leftLeaves);
				// System.out.println("INFO: Right Leaves: " + rightLeaves);
				// System.out.println("INFO: Leaves Information: " +
				// leafNodeInfo);
				System.out.println(leafUpdate);

				break;
			case Protocol.JOINING_PROTOCOL_INITIATE:
				int peerIDLength = din.readInt();
				byte[] peerIDBytes = new byte[peerIDLength];
				din.readFully(peerIDBytes);

				int lengthOfIPPeer = din.readInt();
				byte[] IPOfPeerByte = new byte[lengthOfIPPeer];
				din.readFully(IPOfPeerByte);

				int portOfPeer = din.readInt();

				int hopeTravel = din.readInt() + 1;

				ArrayList<String> travelList = new ArrayList<String>();
				for (int i = 0; i < (hopeTravel - 1); i++) {
					int l = din.readInt();
					byte[] byteID = new byte[l];
					din.readFully(byteID);
					travelList.add(HexBytes.convertBytesToHex(byteID));
				}
				travelList.add(myID);
				String request = HexBytes.convertBytesToHex(peerIDBytes);

				String joiningProtocol = "\nINFO: Joining Request Is Coming \nsFor Node: "
						+ request
						+ "\nBelow Details Will Include My Appended Details Too\nHope Travel Of Request: "
						+ hopeTravel + "\nTravel List: " + travelList;
				// System.out
				// .println("\nINFO: Joining Request Is Coming For Node: "
				// + request + "\nHope Travel Of Request: "
				// + hopeTravel);
				System.out.println(joiningProtocol);
				ArrayList<String> nextHopeDetails = lookUp(request);

				int position = 0;
				boolean foundProperPosition = false;
				// String searchingIndex = "";
				while (!foundProperPosition) {

					if (myID.charAt(position) != request.charAt(position)) {
						foundProperPosition = true;
					} else {
						// searchingIndex += myID.charAt(position);
						position++;
					}
				}
				LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>> routingTableToSend = new LinkedHashMap<Integer, LinkedHashMap<String, ArrayList<String>>>();
				String appendingString = "";
				for (int i = 0; i <= position; i++) {
					char found = myID.charAt(i);
					LinkedHashMap<String, ArrayList<String>> getList = routingTable
							.get(i);
					LinkedHashMap<String, ArrayList<String>> listToSend = new LinkedHashMap<String, ArrayList<String>>();
					for (int j = 0; j <= 15; j++) {
						String keyPut = appendingString
								+ Integer.toHexString(j).toUpperCase();

						ArrayList<String> listOnCell = getList.get(keyPut);
						if (!(listOnCell.size() == 0)) {
							if (!listOnCell.get(0).equals(
									Protocol.reservedKeyWord)) {
								listToSend.put(keyPut, listOnCell);
							} else {
								if (i == position) {
									ArrayList<String> myList = new ArrayList<String>();
									myList.add(myID);
									myList.add(myHostName);
									myList.add(String.valueOf(myPortNumber));
									listToSend.put(keyPut, myList);
								}
							}
						}

					}
					// if(!(listToSend.size() == 0))
					routingTableToSend.put(i, listToSend);
					appendingString = appendingString + found;
				}

				appendingString = "";
				if (hopeTravel != 1) {
					try {
						// System.out
						// .println("--------------In If Condition-----------------------");
						// System.out.println("Here?");
						// System.out.println("Need To Append So Many Things.");
						int sizeOfTableRec = din.readInt();
						// System.out.println("------------Size Rec: -----------"
						// + sizeOfTableRec);
						// System.out.println("SizeOfTable" + sizeOfTableRec);
						// System.out.println("Size Of Table: " + sizeOfTable);
						for (int i = 0; i < sizeOfTableRec; i++) {
							LinkedHashMap<String, ArrayList<String>> entryToRow = routingTableToSend
									.get(i);

							// System.out
							// .println("------------Entry Row For: -----------"
							// + i + " " + entryToRow);
							int numberOfElement = din.readInt();
							// System.out.println("Number Of Entry ------------- "
							// + numberOfElement);
							// System.out.println("Number Of Elements In Row " +
							// i +
							// " :" + numberOfElement);
							for (int j = 0; j < numberOfElement; j++) {
								int sizeOfKey = din.readInt();

								byte[] keyInByte = new byte[sizeOfKey];
								din.readFully(keyInByte);
								String keyInString = new String(keyInByte);

								// System.out.println("Key----------"
								// + keyInString);
								// System.out.println("Key: " + keyInString);

								ArrayList<String> listInCell = new ArrayList<String>();
								int sizeOfCell = din.readInt();
								// System.out.println();
								// System.out.println("Number Of Details In Cell"
								// +
								// sizeOfCell);
								for (int k = 0; k < sizeOfCell; k++) {
									int sizeOfelement = din.readInt();
									byte[] elementInByte = new byte[sizeOfelement];
									din.readFully(elementInByte);
									String element = new String(elementInByte);
									listInCell.add(element);

								}
								if (entryToRow.containsKey(keyInString)) {
									// if (entryToRow.get(keyInString).size() ==
									// 0) {
									// System.out
									// .println("Entry Found --------"
									// + keyInString
									// + listInCell);
									//
									// } else {
									if (entryToRow.get(keyInString).get(0)
											.equals(listInCell.get(0))) {
										// System.out.println("Same Entry Found");
									} else {
										// System.out
										// .println("Already Exist Entry.. Need To Write Swapping Logic.");
										// if()
										System.out.println("INFO: Comparing "
												+ listInCell.get(0)
												+ " And "
												+ entryToRow.get(keyInString)
														.get(0));
										int myOldDistance = compareHope(
												request, listInCell.get(0));
										if (myOldDistance < 0)
											myOldDistance = myOldDistance * -1;

										int newDistance = compareHope(request,
												entryToRow.get(keyInString)
														.get(0));
										if (newDistance < 0)
											newDistance = newDistance * -1;

										if (myOldDistance > newDistance) {
											entryToRow.put(keyInString,
													listInCell);
										}
										System.out
												.println("INFO: After Comparision Best Detail To Put In Routing Table Of Requested Peer: "
														+ entryToRow
																.get(keyInString));

									}
									// Write Union Logic
									// System.out.println("Here?");
									// }
								} else {
									entryToRow.put(keyInString, listInCell);
								}
							}
						}

						// System.out
						// .println("--------------Out If Condition-----------------------");
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				String furtherSending = "\nINFO: Routing Table To Send Further: "
						+ routingTableToSend;
				// System.out.println("\nINFO: Routing Table To Send Further: "
				// + routingTableToSend);
				if (nextHopeDetails.get(0).equals(myID)) {
					furtherSending = furtherSending
							+ "\nINFO: Hola! I Am Final Node..!";
					System.out.println(furtherSending);
					// System.out.println("\nINFO: Hola! I Am Final Node..!");
					// System.out.println("Hola! I Am Final Node..!");

					// System.out
					// .println("\nINFO: My Details: " + nextHopeDetails);

					PeerForwardsFinalRoutingDetails finalRoutingDetails = new PeerForwardsFinalRoutingDetails();
					finalRoutingDetails.setRoutingTable(routingTableToSend);
					finalRoutingDetails.setLeftLeaves(leftLeaves);
					finalRoutingDetails.setRightLeaves(rightLeaves);
					finalRoutingDetails.setLeafNodeInfo(leafNodeInfo);
					finalRoutingDetails.setMyID(myID);
					finalRoutingDetails.setMyHostName(myHostName);
					finalRoutingDetails.setMyPortNumber(myPortNumber);
					finalRoutingDetails.setHopeTravel(hopeTravel);
					finalRoutingDetails.setTravelList(travelList);
					// if(hopeTravel == 1)
					// {
					try {
						Socket temporarySocketToPeer = new Socket(new String(
								IPOfPeerByte), portOfPeer);
						TCPSender sender = new TCPSender(temporarySocketToPeer);

						sender.sendData(finalRoutingDetails.getByte());
						System.out
								.println("Final Routing Table And LeafSet Has Been Sent Successfully.");
						temporarySocketToPeer.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// }
					// else
					// {
					// System.out.println("Hope" + hopeTravel);
					// System.out.println("Routing Table: " +
					// routingTableToSend);
					// }
				} else {
					// System.out.println("\nINFO: Sorry! Need To Forward Request To "
					// + nextHopeDetails.get(0));
					furtherSending = furtherSending
							+ "\nINFO: Sorry! Need To Forward Request To "
							+ nextHopeDetails.get(0) + "\n"
							+ "INFO: Next Hope Details" + nextHopeDetails;
					System.out.println(furtherSending);
					// System.out
					// .println("\nINFO: Sorry! Need To Forward Request To "
					// + nextHopeDetails.get(0)
					// + "\n"
					// + "INFO: Next Hope Details"
					// + nextHopeDetails);

					PeerForwardsRoutingDetailsToPeer forwardsRoutingDetailsToPeer = new PeerForwardsRoutingDetailsToPeer();
					forwardsRoutingDetailsToPeer
							.setRoutingTable(routingTableToSend);

					forwardsRoutingDetailsToPeer.setMyID(request);
					forwardsRoutingDetailsToPeer.setMyHostName(new String(
							IPOfPeerByte));
					forwardsRoutingDetailsToPeer.setMyPortNumber(portOfPeer);
					forwardsRoutingDetailsToPeer.setHopeTravel(hopeTravel);
					forwardsRoutingDetailsToPeer.setTravelList(travelList);

					try {
						Socket temporarySocketToPeer = new Socket(
								nextHopeDetails.get(1),
								Integer.parseInt(nextHopeDetails.get(2)));
						TCPSender sender = new TCPSender(temporarySocketToPeer);

						sender.sendData(forwardsRoutingDetailsToPeer.getByte());
						temporarySocketToPeer.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block

						System.out
								.println("\nWARNING: May Be Selected Node Is Deleted..\nINFO: Removing Of Node Is Initiated..");
						// System.out
						// .println("\nINFO: Removing Of Node Is Initiated..");

						String appendingUpdate = "";
						for (int i = 0; i < routingTable.size(); i++) {
							// System.out.println("Called---" + i);
							char foundChar = myID.charAt(i);
							LinkedHashMap<String, ArrayList<String>> getList = routingTable
									.get(i);
							// System.out.println("List--" + i + " List" +
							// getList);
							for (int j = 0; j <= 15; j++) {
								String keyPut = appendingUpdate
										+ Integer.toHexString(j).toUpperCase();

								// System.out.println("key" + keyPut);

								ArrayList<String> listOnCell = getList
										.get(keyPut);
								// System.out.println(listOnCell);
								if (!(listOnCell.size() == 0)) {
									if (listOnCell.get(0).equals(
											nextHopeDetails.get(0))) {
										// System.out.println("Sending To" +
										// listOnCell.get(0));
										listOnCell.clear();

									}
								}

							}

							appendingUpdate = appendingUpdate + foundChar;
						}
						appendingUpdate = "";

						System.out
								.println("\nINFO: Removing Of Node Is Completed..");
						this.onEvent(data, socket);
						// e.printStackTrace();
					}
					// System.out.println();
				}

				break;

			case Protocol.PEER_SENDS_DELETE_ROUTING_UPDATE:

				int deletedPeerIDLength = din.readInt();
				byte[] deletedPeerID = new byte[deletedPeerIDLength];
				din.readFully(deletedPeerID);
				String deletedPeer = HexBytes.convertBytesToHex(deletedPeerID);

				int positionToLookForD = 0;
				boolean foundPositionForD = false;
				String searchingStringForD = "";

				while (!foundPositionForD) {

					if (myID.charAt(positionToLookForD) != deletedPeer
							.charAt(positionToLookForD)) {
						foundPositionForD = true;
					} else {
						searchingStringForD += myID.charAt(positionToLookForD);
						positionToLookForD++;
					}
				}

				char columnToAppendForD = deletedPeer
						.charAt(positionToLookForD);

				String lookingStringForD = searchingStringForD
						+ columnToAppendForD;

				ArrayList<String> listNeedForD = routingTable.get(
						positionToLookForD).get(lookingStringForD);
				System.out.println("\nINFO: Position Details Before Deletion: "
						+ listNeedForD);
				if (listNeedForD.size() == 0) {
					System.out.println("INFO: Element Not Found!");
				} else {
					// Replacement Logic Needs To Be Written
					if (!listNeedForD.get(0).equals(Protocol.reservedKeyWord)) {

						// int myDistanceToOldPeer = compareHope(myID,
						// listNeed.get(0));
						// int myDistanceToNewPeer = compareHope(myID,
						// requestingPeer);
						//
						// if(myDistanceToNewPeer < 0) myDistanceToNewPeer =
						// myDistanceToNewPeer * -1;
						// if(myDistanceToOldPeer < 0) myDistanceToOldPeer =
						// myDistanceToOldPeer * -1;
						String keyInBackUp = positionToLookForD
								+ lookingStringForD;
						if (listNeedForD.get(0).equals(deletedPeer)) {
							listNeedForD.clear();
							if (backUpEntries.containsKey(keyInBackUp)) {

								try {
									// System.err.println();
									String backUpID = backUpEntries.get(
											keyInBackUp).get(0);
									String backUpHost = backUpEntries.get(
											keyInBackUp).get(1);
									String backUpPort = backUpEntries.get(
											keyInBackUp).get(2);
									listNeedForD.add(backUpID);
									listNeedForD.add(backUpHost);

									listNeedForD.add(backUpPort);
									System.out
											.println("INFO: We Are In Luck.. We Found Back Up Entry Regarding The Postion..");

									backUpEntries.get(keyInBackUp).remove(0);
									backUpEntries.get(keyInBackUp).remove(0);
									backUpEntries.get(keyInBackUp).remove(0);

									if (backUpEntries.get(keyInBackUp).size() == 0) {
										backUpEntries.remove(keyInBackUp);
									}
									System.out.println(backUpEntries);
								} catch (Exception e) {
									e.printStackTrace();
								}

							} else {
								System.out
										.println("INFO: Back Up Node Regarding Position Is Empty.. So Position Will Remain Empty");

							}
						} else {
							System.out
									.println("\nINFO: Element Found Is Not Matching The Deletion Requested Peer\nMay Be Node Will Be In Back Up Node..");
							if (backUpEntries.containsKey(keyInBackUp)) {
								if (backUpEntries.get(keyInBackUp).contains(
										deletedPeer)) {
									int indexToDelete = backUpEntries.get(
											keyInBackUp).indexOf(deletedPeer);

									backUpEntries.get(keyInBackUp).remove(
											indexToDelete);
									backUpEntries.get(keyInBackUp).remove(
											indexToDelete);
									backUpEntries.get(keyInBackUp).remove(
											indexToDelete);

									if (backUpEntries.get(keyInBackUp).size() == 0) {
										backUpEntries.remove(keyInBackUp);
									}
									System.out
											.println("\nINFO: Updated Back Up Entry: "
													+ backUpEntries);

								} else {
									System.out
											.println("\nINFO: Back Entry Regarding Requested Position Is Not Empty But It Does Not Contains Deleted Peer1");

								}

							} else {
								System.out
										.println("\nINFO: Nopes.. Back Entry Is Also Empty Regarding Requested Position");
							}

						}
						// String compare =
						// "\nINFO: Newly Added Node Is Requesting Entry In This Peer Where Already Cell Contains Entry\nCell's Entry Will Be Stored In BackUp";

						// String keyInBackUp = positionToLook + lookingString;
						// ArrayList<String> updated = new ArrayList<String>();
						//
						// for (String list : listNeed) {
						// updated.add(list);
						// }
						// if (backUpEntries.containsKey(keyInBackUp)) {
						// // System.err.println();
						//
						// backUpEntries.get(keyInBackUp).addAll(updated);
						// } else {
						// backUpEntries.put(keyInBackUp, updated);
						// }
						// compare = compare + "\nBackUp RoutingTable: "
						// + backUpEntries + "\n";
						// System.out.println("\nINFO: Newly Added Node Is Requesting Entry In This Peer\nNeed To Compare: "
						// + listNeed.get(0) + "And " + requestingPeer);
						// if(myDistanceToNewPeer > myDistanceToOldPeer)
						// {
						// compare = compare + "\nAfter Comparision : " +
						// requestingPeer + " Seems Best Peer";
						// listNeed.clear();
						// listNeed.add(requestingPeer);
						// listNeed.add(new String(IPOfRequested));
						// listNeed.add(String.valueOf(portOfRequested));
						// // }
						// // else
						// // {
						// // compare = compare + "\nAfter Comparision : " +
						// // listNeed.get(0) + " Seems Best Peer";
						// // }
						// // System.out
						// //
						// .println("---Write Logic To Replace Old Entry---");
						// System.err.println(compare);

					}
				}
				String updateRoutingTableAfterD = "\nINFO:"
						+ "Routing Table Update Request Is Received From "
						+ deletedPeer;
				// System.out.println("\nINFO:"
				// + "Routing Table Update Request Is Received From "
				// + requestingPeer);
				updateRoutingTableAfterD = updateRoutingTableAfterD
						+ "\nINFO: Updated Routing Table\n";
				// System.out.println("INFO: Routing Table");
				for (Integer rows : routingTable.keySet()) {
					// System.out.println(rows + " - " +
					// routingTable.get(rows));

					updateRoutingTableAfterD = updateRoutingTableAfterD + rows
							+ " - " + routingTable.get(rows) + "\n";
				}
				System.out.println(updateRoutingTableAfterD);
				// System.out.println("");

				break;
			case Protocol.PEER_SENDS_ROUTING_UPDATE:

				int requestedPeerIDLength = din.readInt();
				byte[] requestedPeerIDBytes = new byte[requestedPeerIDLength];
				din.readFully(requestedPeerIDBytes);
				String requestingPeer = HexBytes
						.convertBytesToHex(requestedPeerIDBytes);

				int lengthOfIPRequested = din.readInt();
				byte[] IPOfRequested = new byte[lengthOfIPRequested];
				din.readFully(IPOfRequested);

				int portOfRequested = din.readInt();

				int positionToLook = 0;
				boolean foundPosition = false;
				String searchingString = "";

				while (!foundPosition) {

					if (myID.charAt(positionToLook) != requestingPeer
							.charAt(positionToLook)) {
						foundPosition = true;
					} else {
						searchingString += myID.charAt(positionToLook);
						positionToLook++;
					}
				}

				char columnToAppend = requestingPeer.charAt(positionToLook);

				String lookingString = searchingString + columnToAppend;

				ArrayList<String> listNeed = routingTable.get(positionToLook)
						.get(lookingString);
				if (listNeed.size() == 0) {
					listNeed.add(requestingPeer);
					listNeed.add(new String(IPOfRequested));
					listNeed.add(String.valueOf(portOfRequested));
				} else {
					// Replacement Logic Needs To Be Written
					if (!listNeed.get(0).equals(Protocol.reservedKeyWord)) {

						// int myDistanceToOldPeer = compareHope(myID,
						// listNeed.get(0));
						// int myDistanceToNewPeer = compareHope(myID,
						// requestingPeer);
						//
						// if(myDistanceToNewPeer < 0) myDistanceToNewPeer =
						// myDistanceToNewPeer * -1;
						// if(myDistanceToOldPeer < 0) myDistanceToOldPeer =
						// myDistanceToOldPeer * -1;
						String compare = "\nINFO: Newly Added Node Is Requesting Entry In This Peer Where Already Cell Contains Entry\nCell's Entry Will Be Stored In BackUp";

						String keyInBackUp = positionToLook + lookingString;
						ArrayList<String> updated = new ArrayList<String>();

						for (String list : listNeed) {
							updated.add(list);
						}
						if (backUpEntries.containsKey(keyInBackUp)) {
							// System.err.println();

							backUpEntries.get(keyInBackUp).addAll(updated);
						} else {
							backUpEntries.put(keyInBackUp, updated);
						}
						compare = compare + "\nBackUp RoutingTable: "
								+ backUpEntries + "\n";
						// System.out.println("\nINFO: Newly Added Node Is Requesting Entry In This Peer\nNeed To Compare: "
						// + listNeed.get(0) + "And " + requestingPeer);
						// if(myDistanceToNewPeer > myDistanceToOldPeer)
						// {
						// compare = compare + "\nAfter Comparision : " +
						// requestingPeer + " Seems Best Peer";
						listNeed.clear();
						listNeed.add(requestingPeer);
						listNeed.add(new String(IPOfRequested));
						listNeed.add(String.valueOf(portOfRequested));
						// }
						// else
						// {
						// compare = compare + "\nAfter Comparision : " +
						// listNeed.get(0) + " Seems Best Peer";
						// }
						// System.out
						// .println("---Write Logic To Replace Old Entry---");
						System.err.println(compare);

					}
				}
				String updateRoutingTable = "\nINFO:"
						+ "Routing Table Update Request Is Received From "
						+ requestingPeer;
				// System.out.println("\nINFO:"
				// + "Routing Table Update Request Is Received From "
				// + requestingPeer);
				updateRoutingTable = updateRoutingTable
						+ "\nINFO: Updated Routing Table\n";
				// System.out.println("INFO: Routing Table");
				for (Integer rows : routingTable.keySet()) {
					// System.out.println(rows + " - " +
					// routingTable.get(rows));

					updateRoutingTable = updateRoutingTable + rows + " - "
							+ routingTable.get(rows) + "\n";
				}
				System.out.println(updateRoutingTable);
				// System.out.println("");
				break;

			case Protocol.STOREDATA_REQUESTING_PEER:

				// byte[] IDInBytes = HexBytes.convertHexToBytes(keyOfFile);
				// dout.writeInt(IDInBytes.length);
				// dout.write(IDInBytes);
				//
				// byte[] byteLocalIP = hostName.getBytes();
				// int addressLength = byteLocalIP.length;
				// dout.writeInt(addressLength);
				// dout.write(byteLocalIP);
				//
				// int localPortNumber = portNumber;
				// dout.writeInt(localPortNumber);
				//
				// dout.writeInt(hopeTravel);
				//
				// for(String travel : travelList)
				// {
				// dout.writeInt(travel.getBytes().length);
				// dout.write(travel.getBytes());
				// }
				// dout.writeByte(requestType);

				byte[] keyID = new byte[din.readInt()];

				din.readFully(keyID);

				byte[] fileName = new byte[din.readInt()];

				din.readFully(fileName);

				byte[] storeDataIP = new byte[din.readInt()];

				din.readFully(storeDataIP);

				int storeDataPort = din.readInt();

				int hopeTraveled = din.readInt();

				ArrayList<String> sendingFurther = new ArrayList<String>();
				for (int i = 0; i < hopeTraveled; i++) {
					byte[] listTravelling = new byte[din.readInt()];

					din.readFully(listTravelling);

					sendingFurther.add(new String(listTravelling));
				}

				// System.out.println(HexBytes.convertBytesToHex(keyID) +
				// sendingFurther);

				byte requestingType = din.readByte();

				// System.out.println(requestingType);
				if (requestingType == Protocol.STORE_REQUEST) {
					System.out
							.println("\nINFO: Storing Request Of File "
									+ new String(fileName)
									+ " With Key "
									+ HexBytes.convertBytesToHex(keyID)
									+ " Is Coming To This Peer\nTill Now, Request Travel "
									+ hopeTraveled
									+ " Number Of Hopes\nTravelList Of Request Is : "
									+ sendingFurther);

				} else if (requestingType == Protocol.READ_REQUEST) {
					System.out
							.println("\nINFO: Reading Request Of File "
									+ new String(fileName)
									+ " With Key "
									+ HexBytes.convertBytesToHex(keyID)
									+ " Is Coming To This Peer\nTill Now, Request Travel "
									+ hopeTraveled
									+ " Number Of Hopes\nTravelList Of Request Is : "
									+ sendingFurther);
				}
				hopeTraveled++;
				sendingFurther.add(myID);
				// System.out.println("");
				ArrayList<String> nextHopeDetailsForRequest = lookUp(HexBytes
						.convertBytesToHex(keyID));

				if (nextHopeDetailsForRequest.get(0).equals(myID)) {
					if (requestingType == Protocol.STORE_REQUEST) {
						System.out
								.println("Hola! I am Final Node For Request Regarding Storing Of File : "
										+ new String(fileName)
										+ " With Key "
										+ HexBytes.convertBytesToHex(keyID));
					} else {
						System.out
								.println("Hola! I am Final Node For Request Regarding Reading Of File : "
										+ new String(fileName)
										+ " With Key "
										+ HexBytes.convertBytesToHex(keyID));
					}

					PeerCompletingRequest completingRequest = new PeerCompletingRequest(
							myID, myHostName, myPortNumber,
							HexBytes.convertBytesToHex(keyID), new String(
									fileName), sendingFurther, hopeTraveled,
							requestingType);
					try {
						Socket temporarySocketToStore = new Socket(new String(
								storeDataIP), storeDataPort);
						TCPSender senderToPeer = new TCPSender(
								temporarySocketToStore);
						senderToPeer.sendData(completingRequest.getByte());
						// System.out.println("Details Has Been Sent To Store Data");
						// sender.sendData(forwardsRoutingDetailsToPeer.getByte());
						temporarySocketToStore.close();
					} catch (Exception e) {
						System.out
								.println("WARNING: StoreData Is Not Listening");
					}
				} else {
					System.out
							.println("\nINFO: Sorry! Need To Forward Request Regarding File : "
									+ new String(fileName)
									+ " With Key "
									+ HexBytes.convertBytesToHex(keyID)
									+ "To "
									+ nextHopeDetailsForRequest.get(0)
									+ "\n"
									+ "INFO: Next Hope Details"
									+ nextHopeDetailsForRequest);

					StoreDataRequestingPeer dataRequestingPeer = new StoreDataRequestingPeer(
							new String(storeDataIP), storeDataPort,
							HexBytes.convertBytesToHex(keyID), new String(
									fileName), sendingFurther, hopeTraveled,
							requestingType);

					try {
						Socket temporarySocketToPeerForRequest = new Socket(
								nextHopeDetailsForRequest.get(1),
								Integer.parseInt(nextHopeDetailsForRequest
										.get(2)));
						TCPSender senderToPeer = new TCPSender(
								temporarySocketToPeerForRequest);
						senderToPeer.sendData(dataRequestingPeer.getByte());
						// sender.sendData(forwardsRoutingDetailsToPeer.getByte());
						temporarySocketToPeerForRequest.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block

						System.out
								.println("\nWARNING: May Be Selected Node Is Deleted..\nINFO: Removing Of Node Is Initiated..");
						// System.out
						// .println("\nINFO: Removing Of Node Is Initiated..");

						String appendingUpdate = "";
						for (int i = 0; i < routingTable.size(); i++) {
							// System.out.println("Called---" + i);
							char foundChar = myID.charAt(i);
							LinkedHashMap<String, ArrayList<String>> getList = routingTable
									.get(i);
							// System.out.println("List--" + i + " List" +
							// getList);
							for (int j = 0; j <= 15; j++) {
								String keyPut = appendingUpdate
										+ Integer.toHexString(j).toUpperCase();

								// System.out.println("key" + keyPut);

								ArrayList<String> listOnCell = getList
										.get(keyPut);
								// System.out.println(listOnCell);
								if (!(listOnCell.size() == 0)) {
									if (listOnCell.get(0).equals(
											nextHopeDetailsForRequest.get(0))) {
										// System.out.println("Sending To" +
										// listOnCell.get(0));
										listOnCell.clear();

									}
								}

							}

							appendingUpdate = appendingUpdate + foundChar;
						}
						appendingUpdate = "";

						System.out
								.println("\nINFO: Removing Of Node Is Completed..");
						this.onEvent(data, socket);
						// e.printStackTrace();
					}
					// System.out.println();
				}

				break;

			default:
				break;
			}
			din.close();
			baInputStream.close();
		} catch (IOException exception) {
			// System.out.println();

			System.err.println("\nPeer Failed To Complete Query");
			System.err.println("Exception: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	private ArrayList<String> lookUp(String requestedPeer) {
		// System.out.println("Look Up..");
		// System.out.println("LeafNodeInfo: " + leafNodeInfo);
		// System.out.println("Routing Table: " + routingTable);
		// TODO Auto-generated method stub
		try {
			boolean foundInLeaves = false;
			String nextHope = "";
			String nextHopeIP = "";
			String nextHopePort = "";
			ArrayList<String> nextHopeDetails = new ArrayList<String>();
			if (leftLeaves.size() != 0 && !foundInLeaves) {
				System.out
						.println("\nINFO: ( ROUTING INFO : "
								+ requestedPeer
								+ " ) Checking Inside Left Leaves Set! Requested Peer: "
								+ requestedPeer);
				String offsetString = myID;
				for (int i = 0; i < leftLeaves.size(); i++) {
					String lLeaf = leftLeaves.get(i);

					int antiDistanceOffsetToL = compareHopeWithDirection(
							Protocol.InAntiClockWiseDirect, offsetString, lLeaf);
					int clockDistanceLToP = compareHopeWithDirection(
							Protocol.InClockWiseDirect, lLeaf, requestedPeer);

					if (clockDistanceLToP < antiDistanceOffsetToL) {
						System.out
								.println("\n( ROUTING INFO : "
										+ requestedPeer
										+ " ) Between Left Leaf And Offset, Requested Peer Can Exist!! Leaf: "
										+ lLeaf + " Offset: " + offsetString
										+ " Requested Peer: " + requestedPeer);
						int antiDistanceOffsetToP = compareHopeWithDirection(
								Protocol.InAntiClockWiseDirect, offsetString,
								requestedPeer);

						if (antiDistanceOffsetToP < clockDistanceLToP) {
							nextHope = offsetString;
							foundInLeaves = true;

							break;
						} else if (antiDistanceOffsetToP > clockDistanceLToP) {
							nextHope = lLeaf;
							foundInLeaves = true;

							break;
						} else if (antiDistanceOffsetToP == clockDistanceLToP) {
							if (offsetString.compareTo(lLeaf) > 0) {
								nextHope = offsetString;

							} else {
								nextHope = lLeaf;
							}
							foundInLeaves = true;
							break;
						}
					}

					offsetString = lLeaf;
				}
				if (foundInLeaves) {
					System.out
							.println("\nINFO:( ROUTING INFO : "
									+ requestedPeer
									+ " ) Next Hope Has Been Found In Left Leaf Set Only!.. Next Hope: "
									+ nextHope + " Requesting Peer: "
									+ requestedPeer);
					if (!(nextHope == myID)) {
						nextHopeIP = leafNodeInfo.get(nextHope).get(0);
						nextHopePort = leafNodeInfo.get(nextHope).get(1);
					}
				}
			}
			// if (foundInLeaves) {
			// System.out
			// .println("\nINFO: Next Hope Has Been Found In Left Leaf Set Only!.."
			// + nextHope);
			// if (!(nextHope == myID)) {
			// nextHopeIP = leafNodeInfo.get(nextHope).get(0);
			// nextHopePort = leafNodeInfo.get(nextHope).get(1);
			// }
			// }
			if (rightLeaves.size() != 0 && !foundInLeaves) {
				System.out
						.println("\nINFO: ( ROUTING INFO : "
								+ requestedPeer
								+ " ) Checking Inside Right Leaves Set! Requested Peer: "
								+ requestedPeer);
				String offsetString = myID;
				for (int i = 0; i < rightLeaves.size(); i++) {
					String rLeaf = rightLeaves.get(i);

					int clockDistanceOffsetToL = compareHopeWithDirection(
							Protocol.InClockWiseDirect, offsetString, rLeaf);
					int antiDistanceLToP = compareHopeWithDirection(
							Protocol.InAntiClockWiseDirect, rLeaf,
							requestedPeer);

					if (antiDistanceLToP < clockDistanceOffsetToL) {
						System.out
								.println("\n( ROUTING INFO : "
										+ requestedPeer
										+ " ) Between Right Leaf And Offset, Requested Peer Can Exist! Leaf: "
										+ rLeaf + " Offset: " + offsetString
										+ " Requested Peer: " + requestedPeer);
						int clockDistanceOffsetToP = compareHopeWithDirection(
								Protocol.InClockWiseDirect, offsetString,
								requestedPeer);

						if (clockDistanceOffsetToP < antiDistanceLToP) {
							nextHope = offsetString;

							foundInLeaves = true;

							break;
						} else if (clockDistanceOffsetToP > antiDistanceLToP) {
							nextHope = rLeaf;
							foundInLeaves = true;

							break;
						} else if (clockDistanceOffsetToP == antiDistanceLToP) {
							if (offsetString.compareTo(rLeaf) > 0) {
								nextHope = offsetString;

							} else {
								nextHope = rLeaf;
							}
							foundInLeaves = true;
							break;
						}
					}

					offsetString = rLeaf;
				}

				if (foundInLeaves) {
					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " ) Next Hope Has Been Found In Right Leaf Set Only!.. Next Hope: "
									+ nextHope + "Requesting Peer: "
									+ requestedPeer);
					if (!(nextHope == myID)) {
						nextHopeIP = leafNodeInfo.get(nextHope).get(0);
						nextHopePort = leafNodeInfo.get(nextHope).get(1);
					}
				}
			}

			if (!foundInLeaves) {
				int position = 0;
				boolean foundProperPosition = false;
				String searchingIndex = "";
				while (!foundProperPosition) {

					if (myID.charAt(position) != requestedPeer.charAt(position)) {
						foundProperPosition = true;
					} else {
						searchingIndex += myID.charAt(position);
						position++;
					}
				}
				// System.out.println("Position: " + position);
				// System.out.println("Comparing With: " + requestedPeer);
				// System.out.println("My ID:" + myID);

				char column = requestedPeer.charAt(position);
				int columnPosition = Integer
						.valueOf(String.valueOf(column), 16);
				String lookingIndex = searchingIndex + column;
				System.err
						.println("\nINFO: ( ROUTING INFO : "
								+ requestedPeer
								+ " ) As Leaf Nodes Are Not Suitable For Requested Peer "
								+ requestedPeer
								+ " I Will Find Proper Match In My Routing Table At Position "
								+ position + " And At Index Of " + lookingIndex);

				ArrayList<String> listNeed = routingTable.get(position).get(
						lookingIndex);
				if (listNeed.size() != 0
						&& !listNeed.get(0).equals(Protocol.reservedKeyWord)) {
					nextHope = listNeed.get(0);
					nextHopeIP = listNeed.get(1);
					nextHopePort = listNeed.get(2);

					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " ) Proper Match In My Routing Table At Position "
									+ position + " And At Index Of "
									+ lookingIndex
									+ " Has Been Found For Requested Peer : "
									+ requestedPeer);

				} else {
					// Searching With L And R
					// System.out.println("Column Position" + columnPosition);
					// System.out.println("Searching Index" + searchingIndex);
					// int searchWithRight = columnPosition;
					// String stringInRight = "";
					// String IPInRight = "";
					// String portInRight = "";
					// boolean foundRightElement = false;
					// while (!foundRightElement) {
					// searchWithRight++;
					// if (searchWithRight > 15)
					// searchWithRight = searchWithRight - 16;
					// if (searchWithRight != columnPosition) {
					// // System.out.println(searchWithRight);
					// String temporaryRight = searchingIndex
					// + Integer.toHexString(searchWithRight)
					// .toUpperCase();
					// // System.out.println("Looking Into " + temporaryRight);
					// ArrayList<String> listNeedInR = routingTable.get(
					// position).get(temporaryRight);
					// if (listNeedInR.size() != 0) {
					// if (!listNeedInR.get(0).equals("Reserved")) {
					// stringInRight = listNeedInR.get(0);
					//
					// IPInRight = listNeedInR.get(1);
					// portInRight = listNeedInR.get(2);
					//
					//
					// foundRightElement = true;
					// }
					//
					// }
					// } else {
					// foundRightElement = true;
					// }
					//
					// }
					// // System.out.println("Break Right");
					//
					// int searchWithLeft = columnPosition;
					// String stringInLeft = "";
					// String IPInLeft = "";
					// String portInLeft = "";
					// boolean foundLeftElement = false;
					// while (!foundLeftElement) {
					// searchWithLeft--;
					// if (searchWithLeft < 0)
					// searchWithLeft = searchWithLeft + 16;
					// if (searchWithLeft != columnPosition) {
					// String temporaryLeft = searchingIndex
					// + Integer.toHexString(searchWithLeft)
					// .toUpperCase();
					// // System.out.println("Looking Into " + temporaryLeft);
					// ArrayList<String> listNeedInL = routingTable.get(
					// position).get(temporaryLeft);
					// if (listNeedInL.size() != 0) {
					// if (!listNeedInL.get(0).equals("Reserved")) {
					// stringInLeft = listNeedInL.get(0);
					// IPInLeft = listNeedInL.get(1);
					// portInLeft = listNeedInL.get(2);
					// foundLeftElement = true;
					// }
					//
					// }
					// } else {
					// foundLeftElement = true;
					// }
					//
					// }
					// String finalString = myID;
					// String finalIP = myHostName;
					// String finalPort = String.valueOf(myPortNumber);
					// int distanceToPeer = compareHope(finalString,
					// requestedPeer);
					// if (distanceToPeer < 0)
					// distanceToPeer = distanceToPeer * -1;
					// // System.out.println(distanceToPeer);
					// if (!stringInRight.equals("")) {
					// int distanceToPeerFromRight = compareHope(stringInRight,
					// requestedPeer);
					// if (distanceToPeerFromRight < 0)
					// distanceToPeerFromRight = distanceToPeerFromRight * -1;
					// if (distanceToPeerFromRight < distanceToPeer) {
					// finalString = stringInRight;
					// finalIP = IPInRight;
					// finalPort = portInRight;
					// distanceToPeer = distanceToPeerFromRight;
					// }
					// }
					// if (!stringInLeft.equals("")) {
					// int distanceToPeerFromLeft = compareHope(stringInLeft,
					// requestedPeer);
					// if (distanceToPeerFromLeft < 0)
					// distanceToPeerFromLeft = distanceToPeerFromLeft * -1;
					// if (distanceToPeerFromLeft < distanceToPeer) {
					// finalString = stringInLeft;
					// finalIP = IPInLeft;
					// finalPort = portInLeft;
					// distanceToPeer = distanceToPeerFromLeft;
					// }
					// }

					String finalString = myID;
					String finalIP = myHostName;
					String finalPort = String.valueOf(myPortNumber);

					// System.out.println("Searching"
					// + searchingIndex);
					// System.out.println("Column" + column);
					// System.out.println("Position" + position);

					int myDistance = compareHope(myID, requestedPeer);
					if (myDistance < 0)
						myDistance = myDistance * -1;
					int lowDistance = myDistance;
					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " )As No Leaf Nodes Are Good Enough.. \nAnd Entry At Specific Point Is Missing.. \nLookUp Is, At Last, Searching In Routing Table And Leaf Set For Best Node\nIn Leaf Set.. Searching Index (For Matching  At Least p Best Prefix): "
									+ searchingIndex);
					for (String leftSet : leftLeaves) {
						if (searchingIndex.equals("")) {
							int temporaryLeafDistance = compareHope(leftSet,
									requestedPeer);
							if (temporaryLeafDistance < 0)
								temporaryLeafDistance = temporaryLeafDistance
										* -1;
							if (temporaryLeafDistance < lowDistance) {
								finalString = leftSet;
								lowDistance = temporaryLeafDistance;
							} else if (temporaryLeafDistance == lowDistance) {
								if (leftSet.compareTo(finalString) > 0) {
									finalString = leftSet;
									lowDistance = temporaryLeafDistance;
								}
							}
						} else {
							if (leftSet.startsWith(searchingIndex)) {
								int temporaryLeafDistance = compareHope(
										leftSet, requestedPeer);
								if (temporaryLeafDistance < 0)
									temporaryLeafDistance = temporaryLeafDistance
											* -1;
								if (temporaryLeafDistance < lowDistance) {
									finalString = leftSet;
									lowDistance = temporaryLeafDistance;
								} else if (temporaryLeafDistance == lowDistance) {
									if (leftSet.compareTo(finalString) > 0) {
										finalString = leftSet;
										lowDistance = temporaryLeafDistance;
									}
								}
							}
						}
					}
					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " ) Searching In Left Leaves Is Done.. For Requested Node: "
									+ requestedPeer
									+ " Best Possible Node Till Now "
									+ finalString);
					for (String rightSet : rightLeaves) {
						if (searchingIndex.equals("")) {
							int temporaryLeafDistance = compareHope(rightSet,
									requestedPeer);
							if (temporaryLeafDistance < 0)
								temporaryLeafDistance = temporaryLeafDistance
										* -1;
							if (temporaryLeafDistance < lowDistance) {
								finalString = rightSet;
								lowDistance = temporaryLeafDistance;
							} else if (temporaryLeafDistance == lowDistance) {
								if (rightSet.compareTo(finalString) > 0) {
									finalString = rightSet;
									lowDistance = temporaryLeafDistance;
								}
							}
						} else {
							if (rightSet.startsWith(searchingIndex)) {
								int temporaryLeafDistance = compareHope(
										rightSet, requestedPeer);
								if (temporaryLeafDistance < 0)
									temporaryLeafDistance = temporaryLeafDistance
											* -1;
								if (temporaryLeafDistance < lowDistance) {
									finalString = rightSet;
									lowDistance = temporaryLeafDistance;
								} else if (temporaryLeafDistance == lowDistance) {
									if (rightSet.compareTo(finalString) > 0) {
										finalString = rightSet;
										lowDistance = temporaryLeafDistance;
									}
								}
							}
						}
					}

					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " ) Searching In Right And Left Leaves Is Done.. For Requested Node: "
									+ requestedPeer
									+ " Best Possible Node Till Now "
									+ finalString);
					if (!finalString.equals(myID)) {
						finalIP = leafNodeInfo.get(finalString).get(0);
						finalPort = leafNodeInfo.get(finalString).get(1);

					}

					String appendingString = searchingIndex;
					System.out
							.println("\nINFO: ( ROUTING INFO : "
									+ requestedPeer
									+ " ) Look Up Will Also Look Into Routing Table .. Starting From Row: "
									+ position);
					for (int i = position; i < routingTable.size(); i++) {
						char found = myID.charAt(i);
						LinkedHashMap<String, ArrayList<String>> getList = routingTable
								.get(i);

						for (int j = 0; j <= 15; j++) {
							String keyPut = appendingString
									+ Integer.toHexString(j).toUpperCase();

							ArrayList<String> listOnCell = getList.get(keyPut);
							if (!(listOnCell.size() == 0)) {
								if (!listOnCell.get(0).equals(
										Protocol.reservedKeyWord)) {
									String mayBeString = listOnCell.get(0);
									int maybeDistance = compareHope(
											mayBeString, requestedPeer);
									if (maybeDistance < 0)
										maybeDistance = maybeDistance * -1;
									if (maybeDistance < lowDistance) {
										lowDistance = maybeDistance;
										finalString = mayBeString;
										finalIP = listOnCell.get(1);
										finalPort = listOnCell.get(2);
									} else if (maybeDistance == lowDistance) {
										if (mayBeString.compareTo(finalString) > 0) {
											lowDistance = maybeDistance;
											finalString = mayBeString;
											finalIP = listOnCell.get(1);
											finalPort = listOnCell.get(2);
										}

									}

								}
							}

						}
						// if(!(listToSend.size() == 0))

						appendingString = appendingString + found;
					}
					System.out
							.println("\nINFO: Searching In Routing Table Is Also Done.. For Requested Node: "
									+ requestedPeer
									+ "\nAfter Searching In Routing Table, And Leaf Sets, Best Possible Node Till Now: "
									+ finalString
									+ " For Requested Peer: "
									+ requestedPeer);
					nextHope = finalString;
					nextHopeIP = finalIP;
					nextHopePort = finalPort;

				}
			}
			nextHopeDetails.add(nextHope);
			if (nextHope.equals(myID)) {
				nextHopeDetails.add(myHostName);
				nextHopeDetails.add(String.valueOf(myPortNumber));
			} else {
				nextHopeDetails.add(nextHopeIP);
				nextHopeDetails.add(nextHopePort);
			}
			System.out
					.println("\nINFO: Whole Look Up Is Done.. \nImportant Details\nRequested Peer: "
							+ requestedPeer
							+ " Next Hope Details: "
							+ nextHopeDetails);
			return nextHopeDetails;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
		// while()
	}

	private void initializingTables(String myIDToPlay) {
		// TODO Auto-generated method stub
		String appendingString = "";
		for (int i = 0; i <= 3; i++) {
			char found = myIDToPlay.charAt(i);

			LinkedHashMap<String, ArrayList<String>> temporary = new LinkedHashMap<String, ArrayList<String>>();
			for (int j = 0; j <= 15; j++) {
				String keyToPut = appendingString
						+ Integer.toHexString(j).toUpperCase();
				ArrayList<String> temporaryArray = new ArrayList<String>();
				if (Integer.toHexString(j).toUpperCase()
						.equals(String.valueOf(found))) {
					temporaryArray.add(Protocol.reservedKeyWord);
				}
				temporary.put(keyToPut, temporaryArray);

			}
			routingTable.put(i, temporary);
			appendingString = appendingString + found;
		}
		// System.out.println(routingTable);
	}

	private int compareHope(String point, String neededPoint) {
		int decimalDistance = (Integer.parseInt(neededPoint, 16) - Integer
				.parseInt(point, 16));

		if (decimalDistance >= (65536 / 2)) {
			// System.out.println("Left");
			// System.out.println(decimalDistance - 65536);
			return (decimalDistance - 65536);
		} else if (decimalDistance <= -(65536 / 2)) {
			return (65536 + decimalDistance);
		} else {
			// System.out.println("Right");
			// System.out.println(decimalDistance);
			return (decimalDistance);

		}
	}

	private int compareHopeWithDirection(char direction, String point,
			String neededPoint) {
		int decimalDistance = (Integer.parseInt(neededPoint, 16) - Integer
				.parseInt(point, 16));
		if (direction == Protocol.InClockWiseDirect) {
			if (decimalDistance > 0) {
				return decimalDistance;
			} else {
				return 65536 + decimalDistance;
			}
		} else if (direction == Protocol.InAntiClockWiseDirect) {
			if (decimalDistance < 0) {
				return decimalDistance * -1;
			} else {
				return 65536 - decimalDistance;
			}
		} else {
			return 0;
		}
		// if (decimalDistance >= (65536 / 2)) {
		// // System.out.println("Left");
		// // System.out.println(decimalDistance - 65536);
		// return (decimalDistance - 65536);
		// } else if (decimalDistance <= -(65536 / 2)) {
		// return (65536 + decimalDistance);
		// } else{
		// // System.out.println("Right");
		// // System.out.println(decimalDistance);
		// return (decimalDistance);
		//
		// }
	}

	private void startJoiningProtocol(String ID, String IP, int portOfContact) {
		// TODO Auto-generated method stub
		System.out.println("\nINFO: Joining Protocol Has Been Called.");
		try {
			Socket socketForJoining = new Socket(IP, portOfContact);

			TCPSender sender = new TCPSender(socketForJoining);

			JoiningProtocolInitiate initiate = new JoiningProtocolInitiate(
					myID, myHostName, myPortNumber, 0);
			sender.sendData(initiate.getByte());

			System.out.println("\nINFO: Peer Will Forward Request To " + ID);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		// System.out.println("Joining Protocol");
		// System.out.println("Contact: " + ID);
		// System.out.println("IP: " + IP);
		// System.out.println("Port: " + portOfContact);

	}

	public static void main(String[] args) {
		try {
			String startingPeer = "";
			if (args.length == 3) {
				String addressOfDiscovery = args[0];
				int portOfDiscovery = Integer.parseInt(args[1]);
				int needPort = Integer.parseInt(args[2]);

				startingPeer = "\nINFO: As Fourth Argument Is Missing, ID Will Be Generated Randomly";
				// System.out.println("\nINFO: As Fourth Argument Is Missing, ID Will Be Generated Randomly");

				Date date = new Date();
				// System.out.println(date.getTime());

				long timeStamp = date.getTime();
				// System.out.println(date.getSeconds());

				startingPeer = startingPeer + "\nINFO: Local TimeStamp: "
						+ timeStamp;
				// System.out.println("\nINFO: Local TimeStamp: " + timeStamp);
				// System.out.println(timeStamp);
				// long firstDigit = (timeStamp % 10);
				// long secondDigit = (timeStamp / 10) % 10000;
				// long thirdDigit = (timeStamp / 100000) % 10000;
				// long fourthDigit = (timeStamp / 100000) / 10000;
				//
				// ArrayList<String> IDDigits = new ArrayList<String>();
				// IDDigits.add(generateSum(firstDigit));
				// IDDigits.add(generateSum(secondDigit));
				// IDDigits.add(generateSum(thirdDigit));
				// IDDigits.add(generateSum(fourthDigit));
				// Collections.shuffle(IDDigits);
				// String possibleID = IDDigits.get(0) + IDDigits.get(1)
				// + IDDigits.get(2) + IDDigits.get(3);
				String possibleID = generateID(String.valueOf(timeStamp));
				System.out.println(startingPeer + "\nINFO: Possible Peer ID: "
						+ possibleID.toUpperCase());
				Peer peerInOverlay = new Peer(addressOfDiscovery,
						portOfDiscovery, needPort, possibleID);
				peerInOverlay.myID = possibleID;

			} else if (args.length == 4) {

				String addressOfDiscovery = args[0];
				int portOfDiscovery = Integer.parseInt(args[1]);
				int needPort = Integer.parseInt(args[2]);
				String possibleID = args[3];
				Peer peerInOverlay = new Peer(addressOfDiscovery,
						portOfDiscovery, needPort, possibleID);
				peerInOverlay.myID = possibleID;

			} else {

				System.err
						.println("\nWARNING: Please Enter Valid Number Of Arguments. Atleast 3 Arguments Are Needed");
			}
		} catch (Exception e) {

			System.err
					.println("\nWARNING: Some Exception Happened For Converting Argument To Appropriate Datatypes.");
			System.err.println("Exception: " + e.getMessage());
		}

	}

	private static String generateID(String timeStamp)
			throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		// System.out.println(fileName);
		MessageDigest cript = MessageDigest.getInstance("SHA-1");
		cript.reset();
		cript.update(timeStamp.getBytes());

		StringBuffer sb = new StringBuffer("");

		byte[] mdbytes = cript.digest();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16));
		}
		// System.out.println(sb.length());
		ArrayList<String> needToShuffle = new ArrayList<String>();
		needToShuffle
				.add(String.valueOf(sb.toString().charAt(sb.length() - 2)));
		needToShuffle
				.add(String.valueOf(sb.toString().charAt(sb.length() - 3)));
		needToShuffle
				.add(String.valueOf(sb.toString().charAt(sb.length() - 4)));
		needToShuffle
				.add(String.valueOf(sb.toString().charAt(sb.length() - 5)));

		Collections.shuffle(needToShuffle);
		String finalReturn = "" + needToShuffle.get(0) + needToShuffle.get(1)
				+ needToShuffle.get(2) + needToShuffle.get(3);

		return finalReturn.toUpperCase();

	}

	private static String generateSum(long digit) {
		// TODO Auto-generated method stub
		int sum = 0;
		while (digit != 0) {

			// add last digit to the sum

			sum += digit % 10;

			// cut last digit

			digit /= 10;

		}
		if (sum > 15)
			sum = sum % 15;
		return Integer.toHexString(sum);
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

	public void startConnectionToDiscovery(String hostName, int localPort) {
		// TODO Auto-generated method stub

		PeerSendsRegistration peerSendsRegistration = new PeerSendsRegistration(
				myHostName, myPortNumber, myID);
		try {
			Socket connectingToDiscovery = new Socket(discoveryAddress,
					discoveryPort);
			TCPSender sender = new TCPSender(connectingToDiscovery);
			sender.sendData(peerSendsRegistration.getByte());
			connectingToDiscovery.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// System.out.println();

			System.err
					.println("\nWARNING: Peer Failed To Send Registration Request To Discovery Node");
			System.err.println("Exception: " + e.getMessage());
		}
	}

	public void exit() throws Exception {
		// TODO Auto-generated method stub

		PeerSendsRemoveMeToDiscovery discovery = new PeerSendsRemoveMeToDiscovery(
				myID);
		Socket socketToDiscoveryForRemoval = new Socket(discoveryAddress,
				discoveryPort);
		TCPSender senderToDForRemoval = new TCPSender(
				socketToDiscoveryForRemoval);
		senderToDForRemoval.sendData(discovery.getByte());
		socketToDiscoveryForRemoval.close();

		int offset = 0;
		for (String leftNodes : leftLeaves) {

			try {
				ArrayList<String> leftInfo = leafNodeInfo.get(leftNodes);
				Socket temporarySocket = new Socket(leftInfo.get(0),
						Integer.parseInt(leftInfo.get(1)));
				TCPSender sender = new TCPSender(temporarySocket);

				PeerSendsLeafDelete leafDelete = new PeerSendsLeafDelete(myID,
						1, offset, leafNodeInfo, leftLeaves, rightLeaves);
				sender.sendData(leafDelete.getByte());
				offset++;
				temporarySocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		offset = 0;
		for (String rightNodes : rightLeaves) {

			try {
				ArrayList<String> rightInfo = leafNodeInfo.get(rightNodes);
				Socket temporarySocket = new Socket(rightInfo.get(0),
						Integer.parseInt(rightInfo.get(1)));
				TCPSender sender = new TCPSender(temporarySocket);

				PeerSendsLeafDelete leafDelete = new PeerSendsLeafDelete(myID,
						-1, offset, leafNodeInfo, leftLeaves, rightLeaves);
				sender.sendData(leafDelete.getByte());
				offset++;
				temporarySocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		offset = 0;

		System.out
				.println("INFO: Sending Files To Neighbors!\nAs Node Is About To Exit... Files Must Be Sent To Best Possible Node!..");
		String leftNode = leftLeaves.get(0);
		String leftNodeHost = leafNodeInfo.get(leftNode).get(0);
		int leftNodePort = Integer.parseInt(leafNodeInfo.get(leftNode).get(1));

		String rightNode = rightLeaves.get(0);
		String rightNodeHost = leafNodeInfo.get(rightNode).get(0);
		int rightNodePort = Integer
				.parseInt(leafNodeInfo.get(rightNode).get(1));

		// System.out.println(leftNode + leftNodeHost + leftNodePort
		// + rightNode + rightNodeHost + rightNodePort);

		Socket socketToLeft = new Socket(leftNodeHost, leftNodePort);
		TCPSender senderToLeft = new TCPSender(socketToLeft);

		if (!rightNode.equals(leftNode)) {
			Socket socketToRight = new Socket(rightNodeHost, rightNodePort);
			TCPSender senderToRight = new TCPSender(socketToRight);

			for (String fileNameStored : listOfFiles) {
				String[] metaString = metaDataInfo.get(fileNameStored).split(
						"\n");

				String keyOfFile = (metaString[7]);

				int fileToLeft = compareHope(leftNode, keyOfFile);
				int fileToRight = compareHope(rightNode, keyOfFile);

				if (fileToLeft < 0)
					fileToLeft = fileToLeft * -1;
				if (fileToRight < 0)
					fileToRight = fileToRight * -1;
				File fileToSend = new File(new String(fileNameStored));

				byte[] fileInBytes = new byte[(int) fileToSend.length()];

				FileInputStream fileInputStream = new FileInputStream(
						fileToSend);
				// convert file into array of bytes
				// fileInputStream
				fileInputStream.read(fileInBytes);
				fileInputStream.close();

				PeerSendsDataToPeer dataToPeer = new PeerSendsDataToPeer(
						fileNameStored, metaDataInfo.get(fileNameStored),
						fileInBytes, myID);

				if (fileToLeft < fileToRight) {
					System.out.println("\nINFO: Need To Transfer File .."
							+ fileNameStored + " To New Peer " + leftNode);

					try {
						senderToLeft.sendData(dataToPeer.getByte());

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (fileToLeft > fileToRight) {
					System.out.println("\nINFO: Need To Transfer File .."
							+ fileNameStored + " To New Peer " + rightNode);

					try {
						senderToRight.sendData(dataToPeer.getByte());

					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (leftNode.compareTo(rightNode) > 0) {
						System.out.println("\nINFO: Need To Transfer File .."
								+ fileNameStored + " To New Peer " + leftNode
								+ " Even Though File Is At Same Distance");

						try {
							senderToLeft.sendData(dataToPeer.getByte());

						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("\nINFO: Need To Transfer File .."
								+ fileNameStored + " To New Peer " + rightNode);

						try {
							senderToRight.sendData(dataToPeer.getByte());

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}

			}

			socketToRight.close();

		} else {
			System.out
					.println("INFO: As There Is Only One Node In OverLay.. Need To Send All Files To One Node Only.. Node: "
							+ leftNode);

			for (String fileNameStored : listOfFiles) {

				File fileToSend = new File(new String(fileNameStored));

				byte[] fileInBytes = new byte[(int) fileToSend.length()];

				FileInputStream fileInputStream = new FileInputStream(
						fileToSend);
				// convert file into array of bytes
				// fileInputStream
				fileInputStream.read(fileInBytes);
				fileInputStream.close();

				PeerSendsDataToPeer dataToPeer = new PeerSendsDataToPeer(
						fileNameStored, metaDataInfo.get(fileNameStored),
						fileInBytes, myID);

				try {
					senderToLeft.sendData(dataToPeer.getByte());

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
		socketToLeft.close();
		System.out.println("INFO: Request File Transfer Has Been Sent");

		if (!backUpEntries.isEmpty()) {
			for (String s : backUpEntries.keySet()) {
				ArrayList<String> backUpInfos = backUpEntries.get(s);
				int size = backUpInfos.size() / 3;
				for (int i = 0; i < size; i++) {
					try {
						System.out.println("\nINFO: Notifing Node "
								+ backUpInfos.get(i * 3) + " In BackUp Entry");
						Socket temporaryUpdate = new Socket(
								backUpInfos.get(i * 3 + 1),
								Integer.parseInt(backUpInfos.get(i * 3 + 2)));

						TCPSender sender = new TCPSender(temporaryUpdate);
						PeerSendsDeleteUpdate routingUpdate = new PeerSendsDeleteUpdate(
								myID);

						sender.sendData(routingUpdate.getByte());
						temporaryUpdate.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("\nINFO: No Node To Notify In BackUp Entry");
		}
		String appendingUpdate = "";
		for (int i = 0; i < routingTable.size(); i++) {
			// System.out.println("Called---" + i);
			char foundChar = myID.charAt(i);
			LinkedHashMap<String, ArrayList<String>> getList = routingTable
					.get(i);
			// System.out.println("List--" + i + " List" + getList);
			for (int j = 0; j <= 15; j++) {
				String keyPut = appendingUpdate
						+ Integer.toHexString(j).toUpperCase();

				// System.out.println("key" + keyPut);

				ArrayList<String> listOnCell = getList.get(keyPut);
				// System.out.println(listOnCell);
				if (!(listOnCell.size() == 0)) {
					if (!listOnCell.get(0).equals(Protocol.reservedKeyWord)) {
						// System.out.println("Sending To" +
						// listOnCell.get(0));
						try {

							Socket temporaryUpdate = new Socket(
									listOnCell.get(1),
									Integer.parseInt(listOnCell.get(2)));

							TCPSender sender = new TCPSender(temporaryUpdate);
							PeerSendsDeleteUpdate routingUpdate = new PeerSendsDeleteUpdate(
									myID);

							sender.sendData(routingUpdate.getByte());
							temporaryUpdate.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}

			appendingUpdate = appendingUpdate + foundChar;
		}
	}

}
