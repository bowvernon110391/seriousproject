package com.bowie.gameeditor;

public class Quaternion {

	public float x,y,z,w;
	
	static private Vector3 tmpV0 = new Vector3();
	static private Vector3 tmpV1 = new Vector3();
	static private Vector3 tmpV2 = new Vector3();
	static private Vector3 tmpV3 = new Vector3();
	static private Vector3 tmpV4 = new Vector3();
	static private Vector3 tmpV5 = new Vector3();
	
	static private Quaternion tmpQ0 = new Quaternion();
	static private Quaternion tmpQ1 = new Quaternion();
	static private Quaternion tmpQ2 = new Quaternion();
	static private Quaternion tmpQ3 = new Quaternion();
	
	public Quaternion() {
		//identity
		x=y=z=0;
		w=1;
	}
	
	public Quaternion(float x_, float y_, float z_, float w_) {
		x = x_;
		y = y_;
		z = z_;
		w = w_;
	}
	
	public Quaternion(Quaternion q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
	}
	
	public Quaternion(Vector3 v, float s) {
		x = v.x;
		y = v.y;
		z = v.z;
		w = s;
	}
	
	public void setToIdentity() {
		x=y=z=0;
		w=1;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	public void normalize() {
		float l = this.length();
		if (l > Vector3.EPSILON) {
			l = 1.0f/l;
			
			x*= l;
			y*= l;
			z*= l;
			w*= l;
		}
	}
	
	public static void add(Quaternion a, Quaternion b, Quaternion c) {
		c.x = a.x + b.x;
		c.y = a.y + b.y;
		c.z = a.z + b.z;
		c.w = a.w + b.w;
	}
	
	public static void mul(Quaternion a, Quaternion b, Quaternion c) {
		//tmp0 = vA
		tmpV0.x = a.x;
		tmpV0.y = a.y;
		tmpV0.z = a.z;
		//tmp1 = vB
		tmpV1.x = b.x;
		tmpV1.y = b.y;
		tmpV1.z = b.z;
		//dp = vA . vB
		float dp = Vector3.dot(tmpV0, tmpV1);		
		//vA x vB
		Vector3.cross(tmpV0, tmpV1, tmpV2);
		//vA * b.w
		tmpV0.scale(b.w);
		//vB * a.w
		tmpV1.scale(a.w);
		//tmp0 = vA + vB
		Vector3.add(tmpV0, tmpV1, tmpV0);
		//now we add aka vRes
		Vector3.add(tmpV0, tmpV2, tmpV0);
		//store
		c.x = tmpV0.x;
		c.y = tmpV0.y;
		c.z = tmpV0.z;
		c.w = a.w*b.w - dp;
	}
	
	public void toMatrix3(Matrix3 res) {
		float xx=x*x, xy=x*y, xz=x*z, xw=x*w, yy=y*y, yz=y*z, yw=y*w, zz=z*z, zw=z*w, ww=w*w;

		res.m[0]	= 1-2*(yy+zz);	res.m[3]	= 2*(xy-zw);	res.m[6]	= 2*(xz+yw);
		res.m[1]	= 2*(xy+zw);	res.m[4]	= 1-2*(xx+zz);	res.m[7]	= 2*(yz-xw);
		res.m[2]	= 2*(xz-yw);	res.m[5]	= 2*(yz+xw);	res.m[8]	= 1-2*(xx+yy);
	}
	
	public void conjugate() {
		x=-x;
		y=-y;
		z=-z;
	}
	
	public Quaternion conjugated() {
		//return conjugated version
		Quaternion ret = new Quaternion(this);
		ret.conjugate();
		return ret;
	}
	
	public static Quaternion makeAxisRot(Vector3 axis, float radRot) {
		radRot *= 0.5f;
		float sinA = (float) Math.sin(radRot);
		float cosA = (float) Math.cos(radRot);
		
		Vector3 newAxis = new Vector3(axis.normalized());
		newAxis.scale(sinA);
		
		Quaternion ret = new Quaternion(newAxis, cosA);
		ret.normalize();
		return ret;
	}
	
	public void transformVector(Vector3 v, Vector3 res) {
		//make q0(v, 0.0)
		tmpQ0.x = v.x;
		tmpQ0.y = v.y;
		tmpQ0.z = v.z;
		tmpQ0.w = 0;
		
		//q1 = this * q0
		Quaternion.mul(this, tmpQ0, tmpQ1);
		
		//q0 = q1 * this.conjugated()
		Quaternion.mul(tmpQ1, this.conjugated(), tmpQ0);
		
		//copy result
		res.x = tmpQ0.x;
		res.y = tmpQ0.y;
		res.z = tmpQ0.z;
	}
}
