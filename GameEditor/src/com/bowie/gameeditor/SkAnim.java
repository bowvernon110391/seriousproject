package com.bowie.gameeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bow
 * this class holds skeletal animation data
 * it has to be attached to a skeleton thoigh, and
 * accessed via said skeleton
 */
public class SkAnim {
	
	/**
	 * @author Bow
	 * this holds a single pose bone data
	 * id = bone id in the skeleton
	 * head = head pos in local space
	 * tail = tail pos in local space
	 * rot = rotation in local space
	 * as for parent-children data, we must refer to the skeleton
	 */
	public class PoseBone {
		
		int bone_id;
		Vector3 head = new Vector3();
		Vector3 tail = new Vector3();
		Quaternion rot = new Quaternion();
	}
	
	/**
	 * @author Bow
	 * this class holds a single keyframe data
	 * keyframe data consists of a time stamp,
	 * and a list of pose bone data
	 */
	public class Keyframe {
		
		public float time = 0;	// the key time
		// here be arrays of bones
		// the posebone data is stored from 0 index to bone_len -1
		// so use of array is all right
		List<PoseBone> pbones = new ArrayList<>();
	}
	
	/**
	 * @author Bow
	 * this holds a single action animation data
	 * an action has a name, and a list of keyframes
	 */
	public class Action {
		
		public String name = "";
		List<Keyframe> keyframes = new ArrayList<>();
	}
	
	public int bone_per_kf = 0;
	public int num_action = 0;
	List<Action> actions = new ArrayList<>();
	
	// API to add / modify data
	public Keyframe makeNewKeyframe() {
		return new Keyframe();
	}
	
	public Action makeNewAction() {
		return new Action();
	}
	
	public PoseBone makeNewPoseBone() {
		return new PoseBone();
	}
	
	public void setBonePerFrame(int bpf) {
		this.bone_per_kf = bpf;	// for future reference
		for (Action ac : actions) {
			// resize em
			for (Keyframe kf : ac.keyframes) {
				while (kf.pbones.size() < bpf)
					kf.pbones.add(new PoseBone());
			}
		}
	}
	
	public void addAction(Action a) {
		actions.add(a);
	}
	
	public Action getActionByName(String name) {
		for (Action ac : actions) {
			if (ac.name.equals(name))
				return ac;
		}
		return null;
	}
	
	public int getActionIdByName(String name) {
		for (int i=0; i<actions.size(); i++) {
			if (actions.get(i).name.equals( name ))
				return i;
		}
		
		return -1;
	}
	
	@Override
	public String toString() {
		// log all data here
		String ret = "num_actions: " + actions.size() + " , bone_per_keyframe: " + bone_per_kf + "\n";
		for (int i=0; i<actions.size(); i++) {
			Action ac_data = actions.get(i);
			ret += "\t" + ac_data.name + ", kf: " + ac_data.keyframes.size() + "\n"; 
		}
		return ret;
	}
}
