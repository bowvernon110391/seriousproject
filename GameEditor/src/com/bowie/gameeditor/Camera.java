package com.bowie.gameeditor;

public class Camera {
	//properties of camera
	private Vector3 pos = new Vector3();
	private Quaternion ori = new Quaternion();
	
	//for perspective camera
	private float fov = 55.0f;		//default FOV
	private float znear = 0.1f;		//default znear
	private float zfar = 1000.0f; 	//default zmax
	private float aspect = 1.0f;	//default aspect
	
	//for orthogonal camera
	private float xmin = -1.0f;
	private float xmax =  1.0f;
	private float ymin = -1.0f;
	private float ymax =  1.0f;
	private float zmin = -1.0f;
	private float zmax =  1.0f;
	
	private boolean isPerspective = true;

	public Matrix4 projMat = new Matrix4();	//projection
	public Matrix4 viewMat = new Matrix4();	//view (camera)
	
	private boolean viewDirty = false;
	private boolean projDirty = false;
	
	public Camera() {
	}
	
	public void setPos(Vector3 v) {
		pos = new Vector3(v);
		//dirty, recalculate camView
		viewDirty = true;
	}
	
	public void setRot(Quaternion q) {
		ori = new Quaternion(q);
		//dirty, recalculate camView
		viewDirty = true;
	}
	
	public void setAspect(float a) {
		aspect = a;
		projDirty = true;
	}
	
	public void setFOV(float a) {
		fov = a;
		projDirty = true;
	}
	
	public void setClipping(float n, float f) {
		znear = n;
		zfar = f;
		projDirty = true;
	}
	
	public void setModeToPerspective() {
		isPerspective = true;
		projDirty = true;
	}
	
	public void setModeToOrthogonal() {
		isPerspective = false;
		projDirty = true;
	}
	
	public boolean isPerspective() {
		return isPerspective;
	}
}
