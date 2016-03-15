package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import com.bowie.gameeditor.Skeleton.Bone;

/**
 * @author Bow
 * this class will load base skeleton data
 * right now it only support ascii though
 */
public class SkeletonLoader {
	public SkeletonLoader() {
	}
	
	//Ascii
	Skeleton loadSkeletonASCII(String filename) {
		return loadFromByteASCII(Helper.getBytesFromFile(filename));
	}
	
	//Binary
	Skeleton loadSkeleton(String filename) {
		return loadFromBytes(Helper.getBytesFromFile(filename));
	}
	
	Skeleton loadFromBytes(byte [] data) {
		if (data == null) {
			System.out.println("Skeleton: null data");
			return null;
		}
		
		if (data.length < 4) {
			System.out.println("Skeleton: not enough data");
			return null;
		}
		
		Skeleton skel = new Skeleton();
		
		ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		
		//let's read header
		int version = bb.get();		//1 byte
		int boneCount = bb.get();	//1 byte
		
		//only version 1 supported for now
		if (version != 1) {
			System.out.println("Skeleton: version unsupported : " + version);
			return null;
		}
		
		//maybe no bone
		if (boneCount == 0) {
			System.out.println("Skeleton: zero bones");
			return null;
		}
		
		//now read them all
		for (int i=0; i<boneCount; i++) {
			byte [] boneName = new byte[32];		//32 bytes
			byte [] boneParentName = new byte[32];	//32 bytes
			
			bb.get(boneName, 0, 32);		//32 bytes
			bb.get(boneParentName, 0, 32);	//32 bytes
			
			int boneParentId = bb.get();	//1 byte
			
			//12 bytes
			Vector3 head = new Vector3(
					bb.getFloat(), bb.getFloat(), bb.getFloat()
					);
			//12 bytes
			Vector3 tail = new Vector3(
					bb.getFloat(), bb.getFloat(), bb.getFloat()
					);
			//16 bytes
			Quaternion rot = new Quaternion(
					bb.getFloat(), bb.getFloat(), bb.getFloat(), bb.getFloat()
					);
			//add em
			skel.addBone(new String(boneName), new String(boneParentName), head, tail, rot);
		}
		skel.buildTransform();
		return skel;
	}
	
	Skeleton loadFromByteASCII(byte [] data) {
		String bigText = new String(data);
//		System.out.print(bigText);
		//now we slice per line
		String [] lines = bigText.split("\\n");
//		System.out.println("Got " + lines.length + " lines");
		
		//create temp skeleton data
		Skeleton skel = new Skeleton();
		
		for (String line : lines) {
			line = line.trim();
			//now the data is split by | 
			String [] boneData = line.split(Pattern.quote("|"));
			
			if (boneData.length >= 6) {
				String boneName = boneData[0];
				String parentName = boneData[1];
				int parentId = Integer.parseInt(boneData[2]);
				
				String [] headPos = boneData[3].split("\\s");
				String [] tailPos = boneData[4].split("\\s");
				String [] rotData = boneData[5].split("\\s");
				
				Vector3 head = new Vector3(Float.parseFloat(headPos[0]), Float.parseFloat(headPos[1]), Float.parseFloat(headPos[2]));
				Vector3 tail = new Vector3(Float.parseFloat(tailPos[0]), Float.parseFloat(tailPos[1]), Float.parseFloat(tailPos[2]));
				Quaternion rot = new Quaternion(Float.parseFloat(rotData[1]), Float.parseFloat(rotData[2]), Float.parseFloat(rotData[3]), Float.parseFloat(rotData[0]));
				
				skel.addBone(boneName, parentName, head, tail, rot);
				System.out.println("bone: " + boneName + ", parent: " + parentName);
			}
		}
		skel.buildTransform();
		return skel;	//return ready to use skeleton
	}
}
