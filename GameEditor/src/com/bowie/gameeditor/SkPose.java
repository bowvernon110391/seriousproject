package com.bowie.gameeditor;

import com.bowie.gameeditor.SkAnim.Action;
import com.bowie.gameeditor.SkAnim.Keyframe;
import com.bowie.gameeditor.SkAnim.PoseBone;

/**
 * @author Bow
 * this class holds a single pose data
 */
public class SkPose {
	Skeleton refData = null;
	
	// local data
	Quaternion [] rot;
	Vector3 [] head;
	Vector3 [] tail;
	
	public SkPose(Skeleton refSkel) {
		refData = refSkel;
		// now allocate data
		int dataLen = refData.bones.size();
		
		rot = new Quaternion[dataLen];
		head = new Vector3[dataLen];
		tail = new Vector3[dataLen];
		
		// init each data
		for (int i=0; i<dataLen; i++) {
			rot[i] = new Quaternion();
			head[i] = new Vector3();
			tail[i] = new Vector3();
		}
	}
	
	public boolean isValid() {
		return refData != null;
	}
	
	/**
	 * MUST HAVE VALID REF FIRST
	 * @param actionId the action id
	 * @param time the time
	 */
	public void calculateLinear(int actionId, float time) {
		if (!isValid())
			return;
		// grab action
		Action ac = refData.getAnimation().getActionById(actionId);
		if (ac == null)
			return;
		// got action, let's grab keyframe
		// the keyframe pointer
		Keyframe [] kfs = {null, null};
		ac.getKeyframeLinear(time, kfs);
		// then, interpolate
		if (kfs[0] == null || kfs[1] == null)
			return;
		float interpFactor = (time - kfs[0].time) / (kfs[1].time - kfs[0].time);
		
		interpolateLinear(interpFactor, kfs[0], kfs[1]);
	}
	
	public void calculateCubic(int actionId, float time) {
		if (!isValid())
			return;
		// grab action
		Action ac = refData.getAnimation().getActionById(actionId);
		if (ac == null)
			return;
		// got action, grab keyframes
		Keyframe [] kfs = {null, null, null, null};
		ac.getKeyfameCubic(time, kfs);
		// then interpolate
		if (kfs[0] == null || kfs[1] == null)
			return;
		float interpFactor = (time - kfs[0].time) / (kfs[1].time - kfs[0].time);
		
		interpolateCubic(interpFactor, kfs[0], kfs[1], kfs[2], kfs[3]);
	}
	
	/**
	 * @param factor between [0..1]. 0 means a, 1 means b
	 * @param a the start keyframe
	 * @param b the end keyframe
	 */
	private void interpolateLinear(float factor, Keyframe a, Keyframe b) {
		// sanity check
		if (a == null || b == null)
			return;
		
		// safety
		if (factor < 0.0f) factor = 0.0f;
		if (factor > 1.0f) factor = 1.0f;
		
		// another safety
		if (a.pbones.size() < rot.length) return;
		if (b.pbones.size() < rot.length) return;
		
		// do the thing
		for (int i=0; i<rot.length; i++) {
			PoseBone pa = a.pbones.get(i);
			PoseBone pb = b.pbones.get(i);
			
			// rotation
			Quaternion.slerp(pa.rot, pb.rot, factor, rot[i]);
			// head and tail
			Vector3.lerp(pa.head, pb.head, factor, head[i]);
			Vector3.lerp(pa.tail, pb.tail, factor, tail[i]);
		}
	}
	
	/**
	 * This will do cubic interpolation, although only for the position LOL
	 * @param factor: [0..1] 0 means keyframeA, 1 means keyframeB
	 * @param a keyframe 0
	 * @param b keyframe 1
	 * @param c keyframe 2
	 * @param d keyframe 3
	 */
	private void interpolateCubic(float factor, Keyframe a, Keyframe b, Keyframe c, Keyframe d) {
		if (a==null || b==null || c==null || d==null)
			return;
		// safety
		if (factor < 0.0f) factor = 0.0f;
		if (factor > 1.0f) factor = 1.0f;
		
		// another safety
		if (a.pbones.size() < rot.length) return;
		if (b.pbones.size() < rot.length) return;
		if (c.pbones.size() < rot.length) return;
		if (d.pbones.size() < rot.length) return;
		
		// do the thing
		// do the thing
		for (int i=0; i<rot.length; i++) {
			PoseBone pa = a.pbones.get(i);
			PoseBone pb = b.pbones.get(i);
			PoseBone pc = c.pbones.get(i);
			PoseBone pd = d.pbones.get(i);
			
			// rotation
			Quaternion.slerp(pa.rot, pb.rot, factor, rot[i]);
			// head and tail CUBIIC
//			Vector3.lerp(pa.head, pb.head, factor, head[i]);
//			Vector3.lerp(pa.tail, pb.tail, factor, tail[i]);
			Vector3.cubicInterp(pa.head, pb.head, pc.head, pd.head, factor, head[i]);
			Vector3.cubicInterp(pa.tail, pb.tail, pc.tail, pd.tail, factor, tail[i]);
			
		}
	}
}
