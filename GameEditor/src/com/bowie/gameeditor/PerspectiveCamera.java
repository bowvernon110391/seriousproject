package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

public class PerspectiveCamera extends Camera {
	private float fov = 55.0f;
	private float aspect = 1.0f;
	private float znear = 0.05f;
	private float zfar = 150.0f;
	
	private Matrix4 projMat = new Matrix4();	//projection matrix
	private Matrix4 viewMat = new Matrix4();	//view matrix
	
	private Matrix4 projViewMat = new Matrix4();
	
	private boolean projDirty = true;
	private boolean viewDirty = true;
	
	private Vector3 pos = new Vector3();
	private Quaternion rot = new Quaternion();
	
	public float getNear() {
		return znear;
	}
	
	public float getFar() {
		return zfar;
	}
	
	public void setClip(float zn, float zf) {
		znear = zn;
		zf = zf;
		
		projDirty = true;
	}
	
	public void setPos(float x, float y, float z) {
		pos.x = x;
		pos.y = y;
		pos.z = z;
		
		viewDirty = true;
	}
	
	public void setRot(Quaternion newRot) {
		rot = newRot;
	}
	
	public Vector3 getPos() {
		return pos;
	}
	
	public Quaternion getRot() {
		return rot;
	}
	
	public float getFOV() {
		return fov;
	}
	
	public void setFOV(float f) {
		fov = f;
		projDirty = true;
	}
	
	public float getAspect() {
		return aspect;
	}
	
	public void setAspect(float f) {
		aspect = f;
		projDirty = true;
	}
	
	@Override
	public	Matrix4 getProjView() {
		boolean recalcProjView = false;
		if (projDirty) {
			recalcProjView = true;
			projDirty = false;
			//well recalculate projection
			Matrix4.perspective(fov, aspect, znear, zfar, projMat);
		}
		
		if (viewDirty) {
			recalcProjView = true;
			viewDirty = false;
			
			Matrix4 invTransMat = new Matrix4();
			invTransMat.m[12] = -pos.x;
			invTransMat.m[13] = -pos.y;
			invTransMat.m[14] = -pos.z;
			
			Matrix4 invRotMat = new Matrix4(rot, new Vector3(0,0,0));
			invRotMat.fastInvert();
			
			Matrix4.mul(invTransMat, invRotMat, viewMat);
		}
		
		if (recalcProjView) {
			//ok, do new mult
			Matrix4.mul(projMat, viewMat, projViewMat);
		}
		
		return projViewMat;
	}
	
	@Override
	public void setViewport(GL2 gl, int x, int y, int w, int h) {
		gl.glViewport(x, y, w, h);
		setAspect((float)w/(float)h);
	}
	
}
