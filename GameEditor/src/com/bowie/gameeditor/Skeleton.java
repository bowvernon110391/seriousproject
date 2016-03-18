package com.bowie.gameeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bow
 * this class represents the skeleton data
 */
public class Skeleton {
	
	/**
	 * @author Bow
	 * this class contains bone data
	 */
	public class Bone {
		public class Transform {
			public Vector3 head = new Vector3();
			public Vector3 tail = new Vector3();
			public Quaternion rot = new Quaternion();
		}
		
		public String name = "", parentName = "null";
		public int parentId = -1;
//		public Vector3 head = new Vector3();
//		public Vector3 tail = new Vector3();
//		public Quaternion localRot = new Quaternion();
		Transform local = new Transform();
		Transform abs = new Transform();
//		Transform invBind = new Transform();	// hold inverse bind pose
		
		//will be build by the skeleton
//		public Vector3 absHead = new Vector3();
//		public Vector3 absTail = new Vector3();
//		public Quaternion absRot = new Quaternion();
	}
	
	// SKELETON DATA
	public List<Bone> bones = new ArrayList<>();
	public SkAnim animation = null;	// no animation
	
	public Skeleton() {
		//do nothing
	}
	
	public boolean hasAnimation() {
		return animation != null;
	}
	
	public SkAnim getAnimation() {
		return animation;
	}
	
	public boolean attachAnimationData(SkAnim animData) {
		if (animation == null) {
			animation = animData;
		}
		return false; // We already have animation
	}
	
	public int findBoneId(String name) {
		for (int i=0; i<bones.size(); i++) {
			if (bones.get(i).name.equals(name))
				return i;
		}
		return -1;	//not found
	}
	
	public void addBone(String name, String parentName, Vector3 head, Vector3 tail, Quaternion rot) {
		Quaternion qRotX90 = Quaternion.makeAxisRot(new Vector3(1, 0, 0), (float) Math.toRadians(-90));
		
		Bone b = new Bone();
		b.name = name;
		b.parentName = parentName;
		b.parentId = findBoneId(parentName);
		
		//only tranform if no parent
		/*if (b.parentId < 0) {
			Quaternion rotFix = Quaternion.makeAxisRot(new Vector3(1, 0, 0), (float) Math.toRadians(-90));
			
			rotFix.transformVector(head, b.head);
			rotFix.transformVector(tail, b.tail);
			
			Quaternion.mul(rotFix, rot, b.localRot);
		} else {*/
			b.local.head = head;
			b.local.tail = tail;
			
			b.local.rot = rot;
//		}
				
		b.local.rot.normalize();	//just to be safe
		//add em
		bones.add(b);
	}
	
	public void buildTransform() {
		//here we build transformation data
		for (int i=0; i<bones.size(); i++) {
			Bone b = bones.get(i);
			
			if (b.parentId < 0) {
				// it's root
//				b.localRot.transformVector(b.head, b.absHead);	//head
//				b.localRot.transformVector(b.tail, b.absTail); 	//tail
				b.abs.head = new Vector3(b.local.head);
				b.abs.tail = new Vector3(b.local.tail);
				b.abs.rot = new Quaternion(b.local.rot); 			//so abs rot = localrot
			} else {
				// it's relative
				Bone p = bones.get(b.parentId);		//grab parent
				
				//rotation
				Quaternion.mul(p.abs.rot, b.local.rot, b.abs.rot);
				
				p.abs.rot.transformVector(b.local.head, b.abs.head);
				Vector3.add(b.abs.head, p.abs.tail, b.abs.head);
				
				p.abs.rot.transformVector(b.local.tail, b.abs.tail);
				Vector3.add(b.abs.tail, p.abs.tail, b.abs.tail);
				
				/*b.localRot.transformVector(b.head, b.absHead);	//head
				b.localRot.transformVector(b.tail, b.absTail); 	//tail
				b.absRot = new Quaternion(b.localRot); 			//so abs rot = localrot
*/			}
			// now store the inverse bind pose
		}
	}
	
	@Override
	public String toString() {
		/*String ret = "[ABSOLUTE DATA]\r\n";
		for (int i=0; i<bones.size(); i++) {
			Bone b = bones.get(i);
			ret += i + ": ";
			ret += "name(" + b.name + ") ";
			ret += "parent(" + b.parentName + ", " + b.parentId + ") ";
			ret += "absHead(" + b.absHead.x + ", " + b.absHead.y + ", " + b.absHead.z + ") ";
			ret += "absTail(" + b.absTail.x + ", " + b.absTail.y + ", " + b.absTail.z + ") ";
			ret += "rot(" + b.absRot.x + ", " + b.absRot.y + ", " + b.absRot.z + ", " + b.absRot.w + ")\r\n";
		}
		
		ret += "\r\n[LOCAL DATA]\r\n";
		for (int i=0; i<bones.size(); i++) {
			Bone b = bones.get(i);
			ret += i + ": ";
			ret += "name(" + b.name + ") ";
			ret += "parent(" + b.parentName + ", " + b.parentId + ") ";
			ret += "head(" + b.head.x + ", " + b.head.y + ", " + b.head.z + ") ";
			ret += "tail(" + b.tail.x + ", " + b.tail.y + ", " + b.tail.z + ") ";
			ret += "rot(" + b.localRot.x + ", " + b.localRot.y + ", " + b.localRot.z + ", " + b.localRot.w + ")\r\n";
		}*/
		String ret = "";
		
		for (int i=0; i<bones.size(); i++) {
			Bone b = bones.get(i);
			ret += i + ": " + b.name + "\n";
		}
		return ret;
	}
}
