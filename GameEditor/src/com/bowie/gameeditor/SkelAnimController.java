package com.bowie.gameeditor;

import java.io.PrintStream;
import java.util.Formatter;

import com.bowie.gameeditor.SkAnim.Action;
import com.bowie.gameeditor.SkAnim.Keyframe;
import com.bowie.gameeditor.SkAnim.PoseBone;
import com.bowie.gameeditor.Skeleton.Bone;

/**
 * @author Bow
 * this class will hold animation data of an object
 * it needs to refer to a skeleton
 * you can set action index, set frame, play mode
 * each animated object should hold one instance of this
 */
public class SkelAnimController {
	
	Quaternion [] finalRot;
	Vector3 [] finalHead;
	Vector3 [] finalTail;
	Skeleton refSkeleton = null;
	
	int curActionId = -1;
	float curTime = 0;
	
	int curKeyframe = 0;	// 1st
	
	public SkelAnimController() {
		// nothing
	}
	
	public void setSkeleton(Skeleton skel) {
		refSkeleton = skel;
		// now we need to resize our data
		resizeData();
	}
	
	public void setAction(int actionId) {
		curActionId = actionId;
		curTime = 1;	// first frame
	}
	
	public void jumpToKeyframe(int jump) {
		if (refSkeleton == null)
			return;
		if (!refSkeleton.hasAnimation())
			return;
		if (curActionId < 0 || curActionId >= refSkeleton.animation.num_action)
			return;
		// ok, let's jum
		curKeyframe += jump;
		
		// now clamp data
		int maxKeyframe = refSkeleton.animation.actions.get(curActionId).keyframes.size()-1;
		int minKeyframe = 0;
		
		curKeyframe = curKeyframe < minKeyframe ? minKeyframe : curKeyframe > maxKeyframe ? maxKeyframe : curKeyframe;
	}
	
	public void recalcData() {
		PrintStream o = System.out;
		
		if (refSkeleton == null) {
			o.println("SkAnimController: no reference skeleton");
			return;
		}
		
		if (!refSkeleton.hasAnimation()) {
			o.println("SkAnimController: no animation");
			return;
		}
		
		o.println("recalc: " + curActionId + " @ " + curKeyframe);
		
		Action ac_data = refSkeleton.animation.actions.get(curActionId);
		Keyframe kf_data = ac_data.keyframes.get(curKeyframe);
		
//		o.println("SkAnimController: curAction: " + curActionId + " curKeyframe: " + curKeyframe);
//		o.println("SkAnimController: total bone: " + kf_data.pbones.size());
//		o.println("SkAnimController: total kf: " + ac_data.keyframes.size());
		
		for (int i=0; i<finalRot.length; i++) {
			// get bone reference
			Bone b = refSkeleton.bones.get(i);
			PoseBone pb = kf_data.pbones.get(i);
			
			// log neutral head, and so on
			// print bone first, then pose bone
			
//			o.printf("\tnbone: %d, %s | ", i, b.name.trim());
//			Vector3 v = b.local.head;
//			o.printf("%.4f %.4f %.4f  ", v.x, v.y, v.z);
//			v = b.local.tail;
//			o.printf("|%.4f %.4f %.4f  ", v.x, v.y, v.z);
//			Quaternion q = b.local.rot;
//			o.printf("|%.4f %.4f %.4f %.4f ", q.x, q.y, q.z, q.w);
//			
//			o.println("");
//			
//			o.printf("\tpbone: %d, %s | ", i, b.name.trim());
//			v = pb.head;
//			o.printf("%.4f %.4f %.4f  ", v.x, v.y, v.z);
//			v = pb.tail;
//			o.printf("|%.4f %.4f %.4f  ", v.x, v.y, v.z);
//			q = pb.rot;
//			o.printf("|%.4f %.4f %.4f %.4f ", q.x, q.y, q.z, q.w);
//			
//			o.println("\n");
			
			/*if (b.parentId < 0) {
				finalHead[i] = pb.head;
				finalTail[i] = pb.tail;
				finalRot[i] = pb.rot;
			} else {
				Quaternion.mul(finalRot[b.parentId], pb.rot, finalRot[i]);
				
				finalRot[b.parentId].transformVector(pb.head, finalHead[i]);
				Vector3.add(finalTail[b.parentId], finalHead[i], finalHead[i]);
				
				finalRot[b.parentId].transformVector(pb.tail, finalTail[i]);
				Vector3.add(finalTail[b.parentId], finalTail[i], finalTail[i]);
			}*/
			finalHead[i] = pb.head;
			finalTail[i] = pb.tail;
			finalRot[i] = pb.rot;
		}
	}
	
	private void resizeData() {
		if (refSkeleton != null) {
			int numBones = refSkeleton.bones.size();
			
			finalRot = new Quaternion[numBones];
			finalHead = new Vector3[numBones];
			finalTail = new Vector3[numBones];
			for (int i=0; i<numBones; i++) {
				finalRot[i] = new Quaternion();
				finalHead[i] = new Vector3();
				finalTail[i] = new Vector3();
			}
		}
	}
}
