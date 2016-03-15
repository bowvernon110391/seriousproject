package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.bowie.gameeditor.SkAnim.Action;
import com.bowie.gameeditor.SkAnim.Keyframe;
import com.bowie.gameeditor.SkAnim.PoseBone;

public class SkAnimLoader {
	public SkAnimLoader() {
	}
	
	SkAnim loadSkAnim(String filename) {
		return loadFromBytes(filename);
	}
	
	SkAnim loadSkAnim(byte [] b) {
		return loadFromBytes(b);
	}
	
	SkAnim loadFromBytes(String filename) {
		return loadFromBytes(Helper.getBytesFromFile(filename));
	}
	
	SkAnim loadFromBytes(byte [] b) {
		if (b == null) {
			System.out.println("SkAnim: null data");
			return null;
		}
		if (b.length < 4) {
			System.out.println("SkAnim: not enough data");
			return null;
		}
		
		System.out.println("SkAnim: reading bytes: " + b.length);
		
		// okay, wrap em. file is Littel endian btw
		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		
		int version = bb.get();
		int bone_per_kf = bb.get();
		
		System.out.println("SkAnim: version: " + version + " bones per kf: " + bone_per_kf);
		
		// check version
		if (version != 1) {
			System.out.println("SkAnim: version unsupported: " + version);
			return null;
		}
		
		// check num of bones per keyframe, 
		if (bone_per_kf == 0) {
			System.out.println("SkAnim: zero bone per keyframe");
			return null;
		}
		
		// next, number of action
		short num_action = bb.getShort();
		
		System.out.println("SkAnim: action count: " + num_action);
		// check
		if (num_action == 0) {
			System.out.println("SkAnim: no action!!");
			return null;
		}
		
		// good, now spawn dummy anim
		SkAnim skanim = new SkAnim();
		
		// store basic data
		skanim.num_action = num_action;
		skanim.bone_per_kf = bone_per_kf;
		// for each action
		for (int i=0; i<num_action; i++) {
			Action ac_data = skanim.makeNewAction();
			
			// read em
			byte [] tmpBuf = new byte[32];
			bb.get(tmpBuf, 0, 32);
			// store name
			ac_data.name = new String(tmpBuf, Charset.forName("UTF-8")).trim();
			
			System.out.println("SkAnim: reading action: " + ac_data.name);
			
			// read num keyframe for this action
			short num_kf = bb.getShort();
			
			System.out.println("SkAnim: keyframe count for this action: " + num_kf);
			
			// for each keyframe
			for (int j=0; j<num_kf; j++) {
				// spawn keyframe
				Keyframe kf_data = skanim.makeNewKeyframe();
				
				// read time
				kf_data.time = bb.getFloat();
				
				System.out.println("\tkf_t[" + j + "]: " + kf_data.time);
				// now read pose bones
				// for each pose bone
				for (int k=0; k<bone_per_kf; k++) {
					// spawn new pose bone
					PoseBone pb = skanim.makeNewPoseBone();
					
					// read bone id
					pb.bone_id = bb.get();
					// read head data
					pb.head.x = bb.getFloat();
					pb.head.y = bb.getFloat();
					pb.head.z = bb.getFloat();
					// read tail data
					pb.tail.x = bb.getFloat();
					pb.tail.y = bb.getFloat();
					pb.tail.z = bb.getFloat();
					// read rot data
					pb.rot.x = bb.getFloat();
					pb.rot.y = bb.getFloat();
					pb.rot.z = bb.getFloat();
					pb.rot.w = bb.getFloat();
					
					System.out.println("\t\t" + pb.bone_id + " : " + pb.head.x + ", " + pb.head.y + ", " + pb.head.z+ ","+
							pb.tail.x + ", " + pb.tail.y + ", " + pb.tail.z + ", " + pb.rot.x + ", " + pb.rot.y + "," + pb.rot.w);
					
					// add pose bone to keyframe
					// it will automatically follow the index
					kf_data.pbones.add(pb);
				}
				// add keyframe to action
				// don't worry
				ac_data.keyframes.add(kf_data);
			}
			// finally, add action data 
			// to the skeletal animation structure
			skanim.actions.add(ac_data);
		}
		
		return skanim;
	}
}
