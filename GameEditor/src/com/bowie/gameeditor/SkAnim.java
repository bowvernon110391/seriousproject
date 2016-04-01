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
		
		public float getFrameCount() {
			float [] frameId = {0,0};
			this.getTrackTime(frameId);
			
			return (frameId[1] - frameId[0])+1;
		}
		
		// get min and max time for this action
		public void getTrackTime(float [] trackTime) {
			if (trackTime.length < 2 || keyframes.size() == 0)
				return;
			
			// good to save
			// 1st keyframe and last keyframe
			trackTime[0] = keyframes.get(0).time;
			trackTime[1] = keyframes.get(keyframes.size()-1).time;
		}
		
		// get keyframe between time (only return 2 frame)
		public void getKeyframeLinear(float time, Keyframe [] kf) {
			if (kf.length < 2)
				return;
			// do we have enough data?
			if (keyframes.size() < 2)
				return;
			
			// yarp
			for (int i=0; i<keyframes.size()-1; i++) {
				Keyframe ka = keyframes.get(i);
				Keyframe kb = keyframes.get(i+1);
				// check time. one of them must be set inclusive
				if (ka.time <= time && kb.time >= time) {
					// this is it
					kf[0] = ka;
					kf[1] = kb;
					return; // stop
				}
			}
		}
		
		// get keyframe between time, and return the next 2 frame, wrapping if necessary
		public void getKeyfameCubic(float time, Keyframe [] kf) {
			// cubic needs 4 data
			if (kf.length < 4)
				return;
			// can we?
			if (keyframes.size() < 2)
				return;
			
			// yaarp
			int ka_id = 0;	// the first keyframe
			int kb_id = 0;	// the last keyframe
			
			for (int i=0; i<keyframes.size()-1; i++) {
				Keyframe ka = keyframes.get(i);
				Keyframe kb = keyframes.get(i+1);
				
				// check time. one of them must be set inclusive
				if (ka.time <= time && kb.time >= time) {
					// this is it
					kf[0] = ka;
					kf[1] = kb;
					// store keyframe id
					ka_id = i;
					kb_id = i+1;
					// the next frame and beyond
					int kfCount = keyframes.size();
					// normally, the 3rd and 4th frames are next to last frame
					int kc_id = kb_id+1;
					int kd_id = kc_id+1;
					
					// but if we exceed, we need to wrap
					if (kc_id >= kfCount) {
						kc_id = (kc_id+1) % kfCount; // +1, because 1st and last frame are similar
					}
					if (kd_id >= kfCount) {
						kd_id = (kd_id+1) % kfCount; // +1, because 1st and last frame are similar
					}
					
//					System.out.println("cubic_frame: " + ka_id + ", " + kb_id + ", " + kc_id + ", " + kd_id);
					// store additional keyframe
					kf[2] = keyframes.get(kc_id);
					kf[3] = keyframes.get(kd_id);
					return; // stop
				}
			}
		}
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
	
	public int getActionId(String name) {
		for (int i=0; i<actions.size(); i++) {
			if (actions.get(i).name.equals(name))
				return i;
		}
		return -1;
	}
	
	public Action getActionById(int id) {
		if (id < 0)
			return null;
		if (id >= actions.size())
			return null;
		return actions.get(id);
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
