package com.bowie.gameeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bowie
 * This class represents a game world
 * game world consists of:
 * -GameObjects (logic)
 * -Renderables (render)
 */
public class GameWorld {
	
	// here be list of
	private List<BaseController> controllers;
	private List<BaseModel> models;
	private List<BaseView> views;
	private List<GameObject> objects;	// for bookkeeping only
	
	public GameWorld() {
		// do something nifty here
		controllers = new ArrayList<>();
		models = new ArrayList<>();
		views = new ArrayList<>();
		objects = new ArrayList<>();
	}
	
	public void preRender(float dt) {
		// do something to all renderables here
		for (BaseView v : views) {
			v.preRender(dt);
		}
	}
	
	public void update(float dt) {
		// update game object state here
		// some sequences:
		// - update controller
		// - update model (states)
		//---------------------IN RENDER PHASE--------------------------------
		// - update view (preRender)
		// - render!! 
		
		// controllers first
		for (BaseController c : controllers) {
			c.update(dt);
		}
		
		// models next
		for (BaseModel m : models) {
			m.update(dt);
		}
	}
	
	public void render(float dt) {
		// do real render here
		for (BaseView v : views) {
			v.render(dt);
		}
	}
	
	// when adding object, we simply hold its data, not the object itself?
	public boolean addObject(GameObject o) {
		if (!objects.contains(o)) {
			// no duplicate, heheh
			// add to object
			objects.add(o);
			controllers.add(o.getController());
			models.add(o.getModel());
			views.add(o.getView());
			return true;
		}
		// already there. nothing to do
		return false;
	}
}
