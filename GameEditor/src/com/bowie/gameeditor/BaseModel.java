package com.bowie.gameeditor;

/**
 * @author Bowie
 * this is the base model of a GameObject
 * it represents a state machine actually
 * 
 * store game object data and state here
 */
public class BaseModel {
	
	protected GameObject parent = null;
	
	public BaseModel() {
		// do something
	}
	
	public void setObject(GameObject o) {
		this.parent = o;
	}
	
	public boolean hasObject() {
		return parent != null;
	}
	
	public void update(float dt) {
		// do something here...
	}
}
