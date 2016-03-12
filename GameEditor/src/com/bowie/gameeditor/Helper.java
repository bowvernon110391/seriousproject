package com.bowie.gameeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class Helper {
	
	private static boolean mustReverse = ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN;

	public Helper() {
	}
	
	public static int readInt(int data) {
		return mustReverse?Integer.reverseBytes(data):data;
	}
	
	public static short readShort(short data) {
		return mustReverse?Short.reverseBytes(data):data;
	}
	
	public static float readFloat(float data) {
		int bits = Float.floatToIntBits(data);
		if (mustReverse)
			bits = Integer.reverseBytes(bits);
		return Float.intBitsToFloat(bits);
	}
	
	public static int convertUShort(short data) {
		return data >= 0 ? data : 65536+data;
	}
	
	public static byte [] getBytesFromFile(File handle) {
		if (handle.exists()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(handle);
				byte [] buf = new byte[fis.available()];
				
				fis.read(buf);
				fis.close();
				
				return buf;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return null;
	}
	
	public static byte [] getBytesFromFile(String fname) {
		return getBytesFromFile(new File(fname));
	}
}
