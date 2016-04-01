package com.bowie.gameeditor;

import java.nio.FloatBuffer;

import com.bowie.gameeditor.Mesh.MeshVertex_v2;
import com.bowie.gameeditor.SkAnim.Action;
import com.bowie.gameeditor.SkAnim.Keyframe;
import com.bowie.gameeditor.SkAnim.PoseBone;
import com.bowie.gameeditor.Skeleton.Bone;
import com.jogamp.opengl.GL2;

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
	
	// for skinning
	Quaternion [] skinRot;
	Vector3 [] skinHead;
	
	public SkPose(Skeleton refSkel) {
		refData = refSkel;
		// now allocate data
		int dataLen = refData.bones.size();
		
		rot = new Quaternion[dataLen];
		head = new Vector3[dataLen];
		tail = new Vector3[dataLen];
		
		skinRot = new Quaternion[dataLen];
		skinHead= new Vector3[dataLen];
		
		// init each data
		for (int i=0; i<dataLen; i++) {
			rot[i] = new Quaternion();
			head[i] = new Vector3();
			tail[i] = new Vector3();
			
			skinRot[i] = new Quaternion();
			skinHead[i] = new Vector3();
		}
	}
	
	/**
	 * This will calculate blended pose
	 * THIS ONLY CALCULATE SKINNING DATA!!!!
	 * DO NOT WASTE CPU FOR BLENDING DEBUG DATA
	 * @param pA	poseA [from]
	 * @param pB	poseB [to]
	 * @param interp	interpolation amount [0..1]
	 * @param pRes	where to store the pose
	 */
	public static void blendPose(SkPose pA, SkPose pB, float interp, SkPose pRes) {
		// heavily check for stupid configuration
		if (pA == null || pB == null || pRes == null)
			return;
		if (pA.skinHead.length != pB.skinHead.length || pA.skinHead.length != pRes.skinHead.length)
			return;
		
		// now correct interpolation
		if (interp < 0.0f) interp = 0.0f;
		if (interp > 1.0f) interp = 1.0f;
		
		// now we can continue
		for (int i=0; i<pA.skinHead.length; i++) {
			// interpolate rotation
			Quaternion.slerp(pA.skinRot[i], pB.skinRot[i], interp, pRes.skinRot[i]);
			// interpolate head
			Vector3.lerp(pA.skinHead[i], pB.skinHead[i], interp, pRes.skinHead[i]);
		}
	}
	
	public boolean isValid() {
		return refData != null;
	}
	
	public void setTo(SkPose ref) {
		if (ref.skinHead.length == skinHead.length) {
			for (int i=0; i<ref.skinHead.length; i++) {
				skinHead[i].setTo(ref.skinHead[i]);
				skinRot[i].setTo(ref.skinRot[i]);
			}
		}
	}
	
	public void CPUSkin(GL2 gl, Mesh m) {
		// will CPU Skin to mesh's temp vbuffer
		if (m == null)
			return;
		//makes sense?
		if (m.vertFormatId != 1)
			return;
		// do it for all vertices
		if (m.vertices == null)
			return;
		
		if (this.refData == null)
			return;
		
		FloatBuffer fb = m.getTempBuffer();
		
		if (fb == null)
			return;
		// clear
		fb.clear();
		
		Vector3 tmpV = new Vector3();	// for temporary calculation
		Vector3 vl = new Vector3();
		
		
		int boneId = 0;
		float boneW = 0;
		for (int i=0; i<m.vertices.length; i++) {
			tmpV.x = tmpV.y = tmpV.z = 0.0f;
			MeshVertex_v2 v = (MeshVertex_v2) m.vertices[i];
			// do it for each bones
			for (int j=0; j<4; j++) {
				boneId = v.bone_ids[j];
				boneW = v.bone_ws[j];
				
				Bone b = this.refData.bones.get(boneId);
				// sandwich multiplication
				if (b == null)
					continue;
				
				// skip if insignificant. if it's very small then
				// big chance that after this one it's null bone
				vl.x = v.pos[0];
				vl.y = v.pos[1];
				vl.z = v.pos[2];
								
				
				// ---------------------START--------------------------------------
//				Vector3.sub(vl, b.abs.head, vl);
//				b.abs.rot.conjugated().transformVector(vl, vl);
//				
//				rot[boneId].transformVector(vl, vl);
//				Vector3.add(vl, head[boneId], vl);
				// ---------------------END--------------------------------------
				skinRot[boneId].transformVector(vl, vl);
				Vector3.add(vl, skinHead[boneId], vl);
				
				
				// add based on weight
				tmpV.x += vl.x * boneW;
				tmpV.y += vl.y * boneW;
				tmpV.z += vl.z * boneW;
			}
			// got it, store it
			fb.put(tmpV.x);
			fb.put(tmpV.y);
			fb.put(tmpV.z);
		}
		// done, flip it
		fb.flip();
		
		// rebuffer data
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, m.tmpVBO);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, m.vertices.length * 12, fb, GL2.GL_STATIC_DRAW);
	}
	
	public void calculatePhaseLinear(int actionId, float phase) {
		// simply convert phase to time
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
			
			// the difference
			// -rot
			Quaternion bindRot = refData.bones.get(i).abs.rot;
			Quaternion fwdRot = rot[i];
			Quaternion.mul(fwdRot, bindRot.conjugated(), this.skinRot[i]);
			
			// -trans
			Vector3 bindPos = refData.bones.get(i).abs.head;
			Vector3 fwdPos = head[i];
			
			skinHead[i].x = -bindPos.x;
			skinHead[i].y = -bindPos.y;
			skinHead[i].z = -bindPos.z;
			
			skinRot[i].transformVector(skinHead[i], skinHead[i]);
			
			skinHead[i].x += fwdPos.x;
			skinHead[i].y += fwdPos.y;
			skinHead[i].z += fwdPos.z;
			
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
		Vector3 vTmp = new Vector3();
		Vector3 vUp = new Vector3(0,1,0);
		Quaternion qTmp;
		Vector3 vRotAxis = new Vector3();
		for (int i=0; i<rot.length; i++) {
			PoseBone pa = a.pbones.get(i);
			PoseBone pb = b.pbones.get(i);
			PoseBone pc = c.pbones.get(i);
			PoseBone pd = d.pbones.get(i);
			
			// doing slerp is not enough, we must use cubic for
			// rotation also. But I dont know how, so, match
			// rotation with joint head-tail
			Quaternion.slerp(pa.rot, pb.rot, factor, rot[i]);

			// head and tail CUBIIC
			Vector3.cubicInterp(pa.head, pb.head, pc.head, pd.head, factor, head[i]);
			Vector3.cubicInterp(pa.tail, pb.tail, pc.tail, pd.tail, factor, tail[i]);
			
			// next we match it with head and tail direction (ON ITS Y)
			Vector3.sub(tail[i], head[i], vTmp);
			// normalize, next we find quaternion's y
			vTmp.normalize();
			vUp.x = vUp.z = 0;
			vUp.y = 1;
			rot[i].transformVector(vUp, vUp);
			
//			vRotAxis.normalize();
			// grab the angle difference
			float angleDiff = Vector3.dot(vUp, vTmp);
			// now, only do that thing if the angle difference > EPSILON
			
			// grab cross product
			Vector3.cross(vUp, vTmp, vRotAxis);
			
			if (Vector3.dot(vRotAxis, vRotAxis) > Vector3.EPSILON) {
				float angleRot = (float) Math.acos(angleDiff);
				// get the arccos
				// THIS FUNCTION IS A SAVIOUR!!!!!!
				qTmp = Quaternion.makeAxisRot(vRotAxis, angleRot);
				// multiply it
				Quaternion.mul(qTmp, rot[i], rot[i]);
			}
			
			
			// now we calculate skin rot
			//===============================================================================
			// the difference
			// -rot
			Quaternion bindRot = refData.bones.get(i).abs.rot;
			Quaternion fwdRot = rot[i];
			Quaternion.mul(fwdRot, bindRot.conjugated(), this.skinRot[i]);
			
			// -trans
			Vector3 bindPos = refData.bones.get(i).abs.head;
			Vector3 fwdPos = head[i];
			
			skinHead[i].x = -bindPos.x;
			skinHead[i].y = -bindPos.y;
			skinHead[i].z = -bindPos.z;
			
			skinRot[i].transformVector(skinHead[i], skinHead[i]);
			
			skinHead[i].x += fwdPos.x;
			skinHead[i].y += fwdPos.y;
			skinHead[i].z += fwdPos.z;
		}
	}
}
