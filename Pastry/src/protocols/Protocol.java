package protocols;

public class Protocol {
	public static final byte PEER_SENDS_REGISTRATION = 2;

	public static final byte DISCOVERY_RESPONDS_TO_REGISTRATION = 3;

	public static final byte PEER_SENDS_ACK = 4;

	public static final byte JOINING_PROTOCOL_INITIATE = 5;

	public static final byte PEER_FORWARDS_REQUEST = 6;

	public static final byte PEER_FORWARDS_FINAL_ROUTING_DETAILS = 7;

	public static final byte JOINING_PROTOCOL_COMPLETED = 8;

	public static final byte PEER_SENDS_LEAF_UPDATE = 9;

	public static final byte PEER_SENDS_ROUTING_UPDATE = 10;

	public static final byte STOREDATA_REQUESTING_TO_DISCOVERY = 11;

	public static final byte DISCOVERYNODE_RESPONDING_TO_STOREDATA = 12;

	public static final byte STOREDATA_REQUESTING_PEER = 13;

	public static final byte STORE_REQUEST = 14;

	public static final byte READ_REQUEST = 15;

	public static final byte PEER_COMPLETING_REQUEST = 16;

	public static final byte STOREDATA_SENDS_STORING_DATA = 17;

	public static final byte STOREDATA_SENDS_READING_REQUEST = 18;

	public static final byte PEER_ACK_ABOUT_STORE_FILE = 19;

	public static final byte PEER_SENDS_READ_DATA = 20;

	public static final byte PEER_SENDS_DELETE_ROUTING_UPDATE = 21;

	public static final byte PEER_SENDS_DELETE_LEAF = 22;

	public static final byte NEW_PEER_REQUEST_FILE_TRANSFER = 23;

	public static final byte PEER_SENDS_DATA_TO_PEER = 24;

	public static final byte PEER_SENDS_REMOVE_ME_TO_DISCOVERY = 25;

	
	public static final String defaultLocationToStoreFiles = "/tmp/StoreData/";

	public static final String defaultLocationToReadFiles = "/tmp/ReadData/";

	public static final String defaultKeyWordForMetaData = "_metaData";

	public static final String reservedKeyWord = "Reserved";

	public static final char InClockWiseDirect = 'c';
	
	public static final char InAntiClockWiseDirect = 'a';
}
