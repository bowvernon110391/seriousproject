package com.bowie.gameeditor;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Bow
 * this class will load base skeleton data
 * right now it only support ascii though
 */
public class SkeletonLoader {
	public SkeletonLoader() {
	}
	
	Skeleton loadSkeleton(String filename) {
		return loadFromByte(Helper.getBytesFromFile(filename));
	}
	
	Skeleton loadFromByte(byte [] data) {
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
