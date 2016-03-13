package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

public abstract class Camera {
	public Camera() {
	}
	
	//Must return projection
	abstract public Matrix4 getProjView();
	
	//when resizing
	abstract public void setViewport(GL2 gl, int x, int y, int w, int h);
}
