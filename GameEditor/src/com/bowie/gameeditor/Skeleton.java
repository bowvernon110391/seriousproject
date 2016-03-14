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
		public String name = "", parentName = "null";
		public int parentId = -1;
		public Vector3 head = new Vector3();
		public Vector3 tail = new Vector3();
		public Quaternion localRot = new Quaternion();
		
		//will be build by the skeleton
		public Vector3 absHead = new Vector3();
		public Vector3 absTail = new Vector3();
		public Quaternion absRot = new Quaternion();
	}
	
	
	public List<Bone> bones = new ArrayList<>();
	
	public Skeleton() {
		//do nothing
	}
	
	public int findBoneId(String name) {
		for (int i=0; i<bones.size(); i++) {
			if (bones.get(i).name.equals(name))
				return i;
		}
		return -1;	//not found
	}
	
	public void addBone(String name, String parentName, Vector3 head, Vector3 tail, Quaternion rot) {
		Bone b = new Bone();
		b.name = name;
		b.parentName = parentName;
		b.parentId = findBoneId(parentName);
		b.head = head;
		b.tail = tail;
		b.localRot = rot;
		b.localRot.normalize();	//just to be safe
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
				b.absHead = new Vector3(b.head);
				b.absTail = new Vector3(b.tail);
				b.absRot = new Quaternion(b.localRot); 			//so abs rot = localrot
			} else {
				// it's relative
				Bone p = bones.get(b.parentId);		//grab parent
				
				//rotation
				Quaternion.mul(p.absRot, b.localRot, b.absRot);
				
				p.absRot.transformVector(b.head, b.absHead);
				Vector3.add(b.absHead, p.absTail, b.absHead);
				
				p.absRot.transformVector(b.tail, b.absTail);
				Vector3.add(b.absTail, p.absTail, b.absTail);
				
				/*b.localRot.transformVector(b.head, b.absHead);	//head
				b.localRot.transformVector(b.tail, b.absTail); 	//tail
				b.absRot = new Quaternion(b.localRot); 			//so abs rot = localrot
*/			}
		}
	}
	
	@Override
	public String toString() {
		String ret = "[ABSOLUTE DATA]\r\n";
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
		}
		
		return ret;
	}
}
