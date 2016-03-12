/*
 * this is opengl 4x4 Matrix
 * used only for opengl transformation
 */

package com.bowie.gameeditor;

public class Matrix4 {
	public float [] m = new float[16];
	
	private static Matrix3 tmpMat0 = new Matrix3();
	private static Vector3 tmpV0 = new Vector3();
	
	public Matrix4() {
		this.setToIdentity();
	}
	
	public Matrix4(Quaternion q, Vector3 v) {
		//construct from quaternion and a vector
		this.fromQuatVec3(q, v);
	}
	
	public void transformVector(Vector3 v, Vector3 res) {
		tmpV0.x = m[0]*v.x+m[4]*v.y+m[8]*v.z+m[12];
        tmpV0.y = m[1]*v.x+m[5]*v.y+m[9]*v.z+m[13];
        tmpV0.z = m[2]*v.x+m[6]*v.y+m[10]*v.z+m[14];
        
        res.x = tmpV0.x;
        res.y = tmpV0.y;
        res.z = tmpV0.z;
	}
	
	public void fromQuatVec3(Quaternion q, Vector3 v) {
		//construct from quaternion and a vector
		q.toMatrix3(tmpMat0);
				
		m[0]=tmpMat0.m[0];    m[4]=tmpMat0.m[3];    m[8]=tmpMat0.m[6];    m[12]=v.x;
        m[1]=tmpMat0.m[1];    m[5]=tmpMat0.m[4];    m[9]=tmpMat0.m[7];    m[13]=v.y;
        m[2]=tmpMat0.m[2];    m[6]=tmpMat0.m[5];    m[10]=tmpMat0.m[8];    m[14]=v.z;
        m[3]=0.0f;      m[7]=0.0f;    m[11]=0.0f;    m[15]=1.0f;
	}
	
	public void quickInvert() {
		//for rendering purpose, the matrix needs to be correct tho
		float f;
		//swap 1 - 4
		f=m[1];	m[1]=m[4];	m[4]=f;
		//swap 2 - 8
		f=m[2];	m[2]=m[8];	m[8]=f;
		//swap 6 - 9
		f=m[6];	m[6]=m[9];	m[9]=f;
		
		//transform vertex
		tmpV0.x = m[0]*m[12] + m[4]*m[13] + m[8]*m[14];
        tmpV0.y = m[1]*m[12] + m[5]*m[13] + m[9]*m[14];
        tmpV0.z = m[2]*m[12] + m[6]*m[13] + m[10]*m[14];
        
        //set it
        m[12] = tmpV0.x;
        m[13] = tmpV0.x;
        m[14] = tmpV0.x;
	}
	
	public float[] getValues() {
		return m;
	}
	
	//simple mult
	public static void mult(Matrix4 a, Matrix4 b, Matrix4 c) {
		
	}
	
	//simple ortho matrix calculation
	public static void ortho(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax, Matrix4 m) {
		m.m[0] = 2.0f/(xmax-xmin);
		m.m[5] = 2.0f/(ymax-ymin);
		m.m[10] = -2.0f/(zmax-zmin);
		
		m.m[12] = -(xmax+xmin)/(xmax-xmin);
		m.m[13] = -(ymax+ymin)/(ymax-ymin);
		m.m[14] = -(zmax+zmin)/(zmax-zmin);
		
		m.m[1]=m.m[2]=m.m[3]=m.m[4]=m.m[6]=m.m[7]=m.m[8]=m.m[9]=m.m[11]=0.0f;
		m.m[15]=1.0f;
	}
	
	//simple perspective matrix calculation
	public static void perspective(float fov, float aspect, float zmin, float zmax, Matrix4 m) {
		float onepertanfovper2 = (float) Math.tan(Math.toRadians(fov/2.0f));
		
		m.m[0] = onepertanfovper2/aspect;
		m.m[1] = 0;
		m.m[2] = 0;
		m.m[3] = 0;
		
		m.m[4] = 0;
		m.m[5] = onepertanfovper2;
		m.m[6] = 0;
		m.m[7] = 0;
		
		m.m[8] = 0;
		m.m[9] = 0;
		m.m[10] = -(zmax+zmin)/(zmax-zmin);
		m.m[11] = -1.0f;
		
		m.m[12] = 0;
		m.m[13] = 0;
		m.m[14] = -(2.0f*zmax*zmin)/(zmax-zmin);
		m.m[15] = 0;
	}
	
	public void setToIdentity() {
		m[1]=m[2]=m[3]=m[4]=m[6]=m[7]=m[8]=m[9]=m[11]=m[12]=m[13]=m[14]=0.0f;
		m[0]=m[5]=m[10]=m[15]=1.0f;
	}
	
	//a.transformBy(b) is a = b * a
	public void transformBy(Matrix4 t) {
		float [] nm = new float[16];
		
		nm[0] = t.m[0] * m[0] + t.m[4] * m[1] + t.m[8] * m[2] + t.m[12] * m[3];
		nm[4] = t.m[0] * m[4] + t.m[4] * m[5] + t.m[8] * m[6] + t.m[12] * m[7];
		nm[8] = t.m[0] * m[8] + t.m[4] * m[9] + t.m[8] * m[10] + t.m[12] * m[11];
		nm[12] = t.m[0] * m[12] + t.m[4] * m[13] + t.m[8] * m[14] + t.m[12] * m[15];
		
		nm[1] = t.m[1] * m[0] + t.m[5] * m[1] + t.m[9] * m[2] + t.m[13] * m[3];
		nm[5] = t.m[1] * m[4] + t.m[5] * m[5] + t.m[9] * m[6] + t.m[13] * m[7];
		nm[9] = t.m[1] * m[8] + t.m[5] * m[9] + t.m[9] * m[10] + t.m[13] * m[11];
		nm[13] = t.m[1] * m[12] + t.m[5] * m[13] + t.m[9] * m[14] + t.m[13] * m[15];
		
		nm[2] = t.m[2] * m[0] + t.m[6] * m[1] + t.m[10] * m[2] + t.m[14] * m[3];
		nm[6] = t.m[2] * m[4] + t.m[6] * m[5] + t.m[10] * m[6] + t.m[14] * m[7];
		nm[10] = t.m[2] * m[8] + t.m[6] * m[9] + t.m[10] * m[10] + t.m[14] * m[11];
		nm[14] = t.m[2] * m[12] + t.m[6] * m[13] + t.m[10] * m[14] + t.m[14] * m[15];
		
		nm[3] = t.m[3] * m[0] + t.m[7] * m[1] + t.m[11] * m[2] + t.m[15] * m[3];
		nm[7] = t.m[3] * m[4] + t.m[7] * m[5] + t.m[11] * m[6] + t.m[15] * m[7];
		nm[11] = t.m[3] * m[8] + t.m[7] * m[9] + t.m[11] * m[10] + t.m[15] * m[11];
		nm[15] = t.m[3] * m[12] + t.m[7] * m[13] + t.m[11] * m[14] + t.m[15] * m[15];
		
		m[0]=nm[0];
		m[1]=nm[1];
		m[2]=nm[2];
		m[3]=nm[3];
		
		m[4]=nm[4];
		m[5]=nm[5];
		m[6]=nm[6];
		m[7]=nm[7];
		
		m[8]=nm[8];
		m[9]=nm[9];
		m[10]=nm[10];
		m[11]=nm[11];
		
		m[12]=nm[12];
		m[13]=nm[13];
		m[14]=nm[14];
		m[15]=nm[15];
	}
	
	//this make a translation matrix
	static public Matrix4 makeTranslate(float x, float y, float z) {
		Matrix4 t = new Matrix4();
		t.m[12] = x;
		t.m[13] = y;
		t.m[14] = z;
		return t;
	}
}
