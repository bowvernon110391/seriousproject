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
	
	public void setTo(Vector3 v) {
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
	
	public static void lerp(Vector3 v1, Vector3 v2, float u, Vector3 res) {
		float tx, ty, tz;
		
		tx = v1.x * (1.0f-u) + v2.x * u;
		ty = v1.y * (1.0f-u) + v2.y * u;
		tz = v1.z * (1.0f-u) + v2.z * u;
		
		res.x = tx;
		res.y = ty;
		res.z = tz;
	}
	
	public static float cubic(float y0, float y1, float y2, float y3, float u) {
		float a0, a1, a2, a3, u2;
		
		u2 = u * u;
		a0 = y3 - y2 - y0 + y1;
		a1 = y0 - y1 - a0;
		a2 = y2 - y0;
		a3 = y1;
		
		return (a0*u*u2 + a1*u2 + a2*u + a3);
	}
	
	public static float catmullRom(float y0, float y1, float y2, float y3, float u) {
		float a0, a1, a2, a3, u2;
		a0 = (float) (-0.5 * y0 + 1.5 * y1 - 1.5 * y2 + 0.5 * y3);
		a1 = (float) (y0 - 2.5 * y1 + 2 * y2 - 0.5 * y3);
		a2 = (float) (-0.5 * y0 + 0.5 * y2);
		a3 = y1;
		u2 = u*u;
		return (a0 * u * u2 + a1 * u2 + a2 * u + a3);
	}
	
	public static void cubicInterp(Vector3 v0, Vector3 v1, Vector3 v2, Vector3 v3, float u, Vector3 res) {
		float x = cubic(v0.x, v1.x, v2.x, v3.x, u);
		float y = cubic(v0.y, v1.y, v2.y, v3.y, u);
		float z = cubic(v0.z, v1.z, v2.z, v3.z, u);
//
//		float x = catmullRom(v0.x, v1.x, v2.x, v3.x, u);
//		float y = catmullRom(v0.y, v1.y, v2.y, v3.y, u);
//		float z = catmullRom(v0.z, v1.z, v2.z, v3.z, u);
		
		res.x = x;
		res.y = y;
		res.z = z;
	}
}
