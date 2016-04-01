package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

/**
 * @author Bowie
 * represent the View part of MVC pattern
 * of our game object
 * 
 * store render data and state here...
 */
public class BaseView {
	protected GameObject parent = null;
	
	public BaseView() {
		// do something here...
	}
	
	public void setObject(GameObject o) {
		parent = o;
	}
	
	public boolean hasObject() {
		return parent != null;
	}
	
	public void update(float dt) {
	}
	
	public void preRender(GL2 gl, float dt) {
	}
	
	public void render(GL2 gl, float dt) {
		
	}
}
