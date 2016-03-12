package com.bowie.gameeditor;

public class Vector3 {
	public static float EPSILON = 0.00001f;

	public float x,y,z;
	
	public Vector3() {
		x=y=z=0;
	}
	
	public Vector3(float x_, float y_, float z_) {
		x=x_;
		y=y_;
		z=z_;
	}
	
	public Vector3(Vector3 v) {
		//copy
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	//c = a + b
	public static void add(Vector3 a, Vector3 b, Vector3 c) {
		float cx, cy, cz;
		
		cx = a.x + b.x;
		cy = a.y + b.y;
		cz = a.z + b.z;
		
		c.x = cx;
		c.y = cy;
		c.z = cz;
	}
	
	//c = a - b
	public static void sub(Vector3 a, Vector3 b, Vector3 c) {
		float cx, cy, cz;
		
		cx = a.x - b.x;
		cy = a.y - b.y;
		cz = a.z - b.z;
		
		c.x = cx;
		c.y = cy;
		c.z = cz;
	}
	
	//scale vector
	public void scale(float s) {
		x *= s;
		y *= s;
		z *= s;
	}
	
	//dot product
	public static float dot(Vector3 a, Vector3 b) {
		return a.x*b.x + a.y*b.y + a.z*b.z;
	}
	
	//cross product
	//c = a x b
	public static void cross(Vector3 a, Vector3 b, Vector3 c) {
		float cx, cy, cz;
		
		cx = (a.y * b.z) - (a.z * b.y);
		cy = (a.z * b.x) - (a.x * b.z);
		cz = (a.x * b.y) - (a.y * b.x);
		
		c.x = cx;
		c.y = cy;
		c.z = cz;
	}
	
	//find out length
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	//will normalize our vector
	public void normalize() {
		float l = this.length();
		if (l > EPSILON) {
			l = 1.0f/l;
			
			x*= l;
			y*= l;
			z*= l;
		}
	}
	
	//will return normalized vector of this vector
	public Vector3 normalized() {
		Vector3 r = new Vector3(this);
		r.normalize();
		return r;
	}
}
