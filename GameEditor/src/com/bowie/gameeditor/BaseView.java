package com.bowie.gameeditor;

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
	
	public void preRender(float dt) {
	}
	
	public void render(float dt) {
		
	}
}
