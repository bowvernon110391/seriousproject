package com.bowie.gameeditor;

/**
 * @author Bowie
 * this is the base of controller object
 * must refer to a game object
 * 
 * the PlayerController and AIController 
 * derives from this
 * 
 * store controller data and state here
 */
public class BaseController {
	protected GameObject parent = null;
	
	public BaseController() {
	}
	
	public void setObject(GameObject o) {
		this.parent = o;
	}
	
	public void update(float dt) {
	}
	
	public boolean hasObject() {
		return parent != null;
	}
}
