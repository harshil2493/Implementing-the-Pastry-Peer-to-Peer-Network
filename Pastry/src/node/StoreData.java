package node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import protocols.HexBytes;
import protocols.Protocol;
import protocols.StoreDataRequestingPeer;
import protocols.StoreDataSendsReadingRequest;
import protocols.StoreDataSendsStoringData;
import util.InterActiveCommandParser;
import connection.TCPSender;
import connection.TCPServerThread;

public class StoreData implements Node {
	public String discoveryHostName;
	public int discoveryPort;

	public int myPortNumber;
	public String myHostName;

	public String defaultLocationToViewFiles = Protocol.defaultLocationToReadFiles;

	public StoreData(String host, int port, int requestedPort) {
		// TODO Auto-generated constructor stub
		this.discoveryHostName = host;
		this.discoveryPort = port;

		File f = new File(defaultLocationToViewFiles);
		if (f.exists()) {
			System.out
					.println("INFO: Old File Structure Seems To Be Existed.. Peer Startup Is Deleting Old Files.");
			deleteDirectory(f);
		}
		f.mkdirs();

		TCPServerThread serverThread = new TCPServerThread(requestedPort, this);
		Thread listening = new Thread(serverThread);
		listening.start();

		InterActiveCommandParser activeCommandParser = new InterActiveCommandParser(
				this);
		Thread parser = new Thread(activeCommandParser);
		parser.start();

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

			case Protocol.PEER_SENDS_READ_DATA:
				// System.out.println("Hello!");

				// byte[] IDInBytes = HexBytes.convertHexToBytes(myID);
				// dout.writeInt(IDInBytes.length);
				// dout.write(IDInBytes);
				//
				// dout.writeInt(fileName.getBytes().length);
				// dout.write(fileName.getBytes());
				//
				// dout.writeInt(data.length);
				// dout.write(data);

				byte[] IDOfFileLocation = new byte[din.readInt()];
				din.readFully(IDOfFileLocation);

				byte[] FileReadName = new byte[din.readInt()];
				din.readFully(FileReadName);

				int s = din.readInt();
				if (s == 1) {

					byte[] FileDataRead = new byte[din.readInt()];
					din.readFully(FileDataRead);

					byte[] meta = new byte[din.readInt()];
					din.readFully(meta);

					String fileLoc = defaultLocationToViewFiles
							+ new String(FileReadName);
					// System.out.println(fileLoc);

					// file = defaultLocation + file;
					int lastIndex = fileLoc.lastIndexOf("/");
					String directory = fileLoc.substring(0, lastIndex);
					// String fileNameGot =
					// file.substring(lastIndex).split("\\.");
					// String metaDataFile = file + "_metaData";
					// if(fileNameGot.length == 2) metaDataFile = metaDataFile +
					// fileNameGot[1];

					// System.out.println("Directory: " + directory);
					// System.out.println(fileNameGot);
					if (!new File(directory).exists())
						new File(directory).mkdirs();

					FileOutputStream fos = new FileOutputStream(fileLoc);
					fos.write(FileDataRead);
					fos.close();

					System.out
							.println("\nINFO: File Has Been Retrieved Successfully.. You Can View File At :"
									+ fileLoc
									+ " \nData Has Been Retrieved From "
									+ HexBytes
											.convertBytesToHex(IDOfFileLocation));
					System.out
							.println("\nMetaData Info Of File .. This Details Are Stored In Peer"
									+ new String(meta));
				} else if (s == 0) {
					System.out.println("WARNING: File Does Not Exist On "
							+ HexBytes.convertBytesToHex(IDOfFileLocation)
							+ "\nRequested File: " + new String(FileReadName));
				}
				break;

			case Protocol.PEER_ACK_ABOUT_STORE_FILE:
				// System.out.println("Here?");
				byte[] fileStoredInByte = new byte[din.readInt()];
				din.readFully(fileStoredInByte);

				byte[] storedLocation = new byte[din.readInt()];
				din.readFully(storedLocation);

				System.out
						.println("INFO: File Has Been Stored On Peer Successfully! File: "
								+ new String(fileStoredInByte)
								+ " Location: "
								+ HexBytes.convertBytesToHex(storedLocation));
				break;

			case Protocol.DISCOVERYNODE_RESPONDING_TO_STOREDATA:
				int success = din.readInt();
				if (success != 0) {
					int lengthOfID = din.readInt();
					byte[] IDOfContactByte = new byte[lengthOfID];
					din.readFully(IDOfContactByte);

					int lengthOfContactIP = din.readInt();
					byte[] IPOfContactByte = new byte[lengthOfContactIP];
					din.readFully(IPOfContactByte);

					int portOfContact = din.readInt();

					byte[] fileNameInByte = new byte[din.readInt()];
					din.readFully(fileNameInByte);

					byte[] keyInByte = new byte[din.readInt()];
					din.readFully(keyInByte);

					byte typeOfRequest = din.readByte();
					System.out.println("\nINFO: For File "
							+ new String(fileNameInByte)
							+ ", With Having Key Regarding That As "
							+ new String(keyInByte)
							+ ", StoreData Must First Contact "
							+ HexBytes.convertBytesToHex(IDOfContactByte));

					ArrayList<String> list = new ArrayList<String>();

					StoreDataRequestingPeer dataRequestingPeer = new StoreDataRequestingPeer(
							myHostName, myPortNumber, new String(keyInByte),
							new String(fileNameInByte), list, 0, typeOfRequest);

					Socket temporarySocketToPeerForRequest = new Socket(
							new String(IPOfContactByte), portOfContact);

					TCPSender sender = new TCPSender(
							temporarySocketToPeerForRequest);

					sender.sendData(dataRequestingPeer.getByte());

					temporarySocketToPeerForRequest.close();

					// System.out.println("Contact: ");
					// System.out.println("IP: " + new String(IPOfContactByte));
					// System.out.println("Port: " + portOfContact);

				} else {
					System.out
							.println("WARNING: Overlay Has No Enough Nodes To Store Your Content..");
					System.out
							.println("WARNING: Can You Try In Again In A While?");
				}
				break;

			case Protocol.PEER_COMPLETING_REQUEST:

				// yte[] IDInBytes = HexBytes.convertHexToBytes(keyOfFile);
				// dout.writeInt(IDInBytes.length);
				// dout.write(IDInBytes);
				//
				// dout.writeInt(fileName.getBytes().length);
				// dout.write(fileName.getBytes());
				//
				// dout.writeInt(myNick.getBytes().length);
				// dout.write(myNick.getBytes());
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
				// System.out.println("Coming?");

				byte[] keyID = new byte[din.readInt()];

				din.readFully(keyID);

				byte[] fileName = new byte[din.readInt()];

				din.readFully(fileName);

				byte[] nickName = new byte[din.readInt()];

				din.readFully(nickName);

				byte[] DataIP = new byte[din.readInt()];

				din.readFully(DataIP);

				int DataPort = din.readInt();

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

				if (requestingType == Protocol.STORE_REQUEST) {
					System.out.println("\nINFO: Storing Request Of File "
							+ new String(fileName) + " With Key "
							+ HexBytes.convertBytesToHex(keyID)
							+ " Is Accepted By Peer " + new String(nickName)
							+ "\nTill Now, Request Travel " + hopeTraveled
							+ " Number Of Hopes\nTravelList Of Request Is : "
							+ sendingFurther);

					String myNickName = "HOST_"
							+ String.valueOf(myHostName.charAt(0))
									.toUpperCase() + myHostName.substring(1)
							+ "_PORT_" + myPortNumber;

					File file = new File(new String(fileName));

					byte[] fileInBytes = new byte[(int) file.length()];

					FileInputStream fileInputStream = new FileInputStream(file);
					// convert file into array of bytes
					// fileInputStream
					fileInputStream.read(fileInBytes);
					fileInputStream.close();

					StoreDataSendsStoringData dataSendsStoringData = new StoreDataSendsStoringData(
							new String(fileName),
							HexBytes.convertBytesToHex(keyID), myNickName,
							fileInBytes);
					Socket socketToData = new Socket(new String(DataIP),
							DataPort);

					TCPSender tcpSender = new TCPSender(socketToData);
					tcpSender.sendData(dataSendsStoringData.getByte());
					System.out.println("\nINFO: Data Has Been Sent To Peer!..");
					socketToData.close();

				} else if (requestingType == Protocol.READ_REQUEST) {
					System.out.println("\nINFO: Reading Request Of File "
							+ new String(fileName) + " With Key "
							+ HexBytes.convertBytesToHex(keyID)
							+ " Is Accepted By Peer " + new String(nickName)
							+ "\nTill Now, Request Travel " + hopeTraveled
							+ " Number Of Hopes\nTravelList Of Request Is : "
							+ sendingFurther);

					StoreDataSendsReadingRequest dataSendsReadingRequest = new StoreDataSendsReadingRequest(
							new String(fileName),
							HexBytes.convertBytesToHex(keyID), myHostName,
							myPortNumber);
					Socket socketToData = new Socket(new String(DataIP),
							DataPort);

					TCPSender tcpSender = new TCPSender(socketToData);
					tcpSender.sendData(dataSendsReadingRequest.getByte());
					System.out
							.println("\nINFO: Reading Request Has Been Sent To Peer!..");
					socketToData.close();
				}
				break;
			}

			din.close();
			baInputStream.close();
		} catch (Exception e) {
			System.out
					.println("WARNING: Some Problem Happened While Processing Query");
			e.printStackTrace();
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

	public static void main(String[] args) {
		if (args.length == 2) {
			StoreData storeData = new StoreData(args[0],
					Integer.parseInt(args[1]), 0);

		} else if (args.length == 3) {
			StoreData storeData = new StoreData(args[0],
					Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else {
			System.out
					.println("WARNING: Some Problem In Number Of Argument Provided.");
		}
	}

}
