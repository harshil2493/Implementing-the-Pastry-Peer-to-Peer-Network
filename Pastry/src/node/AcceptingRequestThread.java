package node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocols.DiscoveryRespondsToRegistration;
import connection.TCPSender;

public class AcceptingRequestThread implements Runnable {

	private DiscoveryNode node;
	public Object lockingObject = new Object();
	private static final Logger logger = Logger
			.getLogger(AcceptingRequestThread.class.getName());

	public AcceptingRequestThread(DiscoveryNode discoveryNode) {
		// TODO Auto-gnerated constructor stub
		this.node = discoveryNode;
		//
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (true) {
			ArrayList<String> processingList;
			synchronized (node.requestQueue) {
				processingList = node.requestQueue.poll();

				if (processingList == null) {
					try {
						// System.out.println();

						// logger.log(Level.INFO,
						System.out
								.println("\nINFO: Discovery Has No Pending Request. It Is Waiting For Peer's Request");

						node.requestQueue.wait();
						// System.out.println();
						System.out
								.println("\nINFO: Discovery Has Been Notified Regarding Joining Request Of Peer!!.");
						synchronized (node.requestQueue) {
							processingList = node.requestQueue.poll();

						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();

						System.err.println("Exception: " + e.getMessage());
					}
				}
				// Process Will Start
				// System.out.println(processingList);
				String possibleID = processingList.get(0);
				String IPOfClient = processingList.get(1);
				String portOfClient = processingList.get(2);
				System.out.println("\nINFO: Discovery Node Is Processing: " + possibleID + " Peer Request");
				int success = 0;
				DiscoveryRespondsToRegistration discoveryRespondsToRegistration = new DiscoveryRespondsToRegistration();
				if (node.registeredNodes.contains(possibleID)) {
					// System.out.println();

					System.err
							.println("WARNING: Discovery Got Request For The Already Registered ID");
					success = 0;
					discoveryRespondsToRegistration.setSuccess(success);

					try {
						Socket socketToClient = new Socket(IPOfClient,
								Integer.parseInt(portOfClient));
						TCPSender sender = new TCPSender(socketToClient);
						sender.sendData(discoveryRespondsToRegistration
								.getByte());
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}

				} else {
					int randomNumber = node.registeredNodes.size();
					success = 1;
					discoveryRespondsToRegistration.setSuccess(success);
					discoveryRespondsToRegistration
							.setContactNodePossible(randomNumber);
					discoveryRespondsToRegistration.setID(possibleID);
					discoveryRespondsToRegistration
							.setLeaves(node.numberOfLeaves);
					if (randomNumber == 0) {
						// First Member To Overlay

						System.out
								.println("\nINFO: First Member In Overlay (May Be): "
										+ possibleID);

					} else {
						int contactingNode = new Random().nextInt(randomNumber);
						String contactingNodeID = node.registeredNodes
								.get(contactingNode);

						ArrayList<String> infoToSend = node.nodeInfo
								.get(contactingNodeID);
						String IPOfContact = infoToSend.get(0);
						int PortOfContact = Integer.parseInt(infoToSend.get(1));
						discoveryRespondsToRegistration
								.setContactID(contactingNodeID);
						discoveryRespondsToRegistration
								.setContactIP(IPOfContact);
						discoveryRespondsToRegistration
								.setContactPort(PortOfContact);
					}

					discoveryRespondsToRegistration.setSuccess(success);
					try {
						Socket socketToClient = new Socket(IPOfClient,
								Integer.parseInt(portOfClient));
						TCPSender sender = new TCPSender(socketToClient);
						sender.sendData(discoveryRespondsToRegistration
								.getByte());

					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception exception) {
						// TODO Auto-generated catch block
						exception.printStackTrace();
					}

					try {
						synchronized (lockingObject) {

							System.out.println("\nINFO: Waiting For ACK From "
									+ possibleID);

							lockingObject.wait();

							System.out
									.println("\nINFO: ACK Has Been Received From "
											+ possibleID +
											 "\nNow Discovery Can Execute Further Requests, IFF There Are Any!");
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}
	}

}
