package protocols;

public class HexBytes {

	/**
	 * This method converts a set of bytes into a Hexadecimal representation.
	 * 
	 * @param buf
	 * @return
	 */
	public static final String convertBytesToHex(byte[] buffer) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < buffer.length; i++) {
			int byteValue = (int) buffer[i] & 0xff;
			if (byteValue <= 15) {
				stringBuffer.append("0");
			}
			stringBuffer.append(Integer.toString(byteValue, 16));
		}
		return stringBuffer.toString().toUpperCase();
	}

	/**
	 * This method converts a specified hexadecimal String into a set of bytes.
	 * 
	 * @param hexString
	 * @return
	 */
	public static final byte[] convertHexToBytes(String hexString) {
		int size = hexString.length();
		hexString = hexString.toUpperCase();
		byte[] buffer = new byte[size / 2];
		int element = 0;
		for (int i = 0; i < size; i++) {
			String sub = hexString.substring(i, i + 2);
			int valueOfSub = Integer.parseInt(sub, 16);
			i++;
			buffer[element] = (byte) valueOfSub;
			element++;
		}
		return buffer;
	}

}
