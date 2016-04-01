package com.bowie.gameeditor;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

/**
 * @author Bowie
 * This class represents a game world
 * game world consists of:
 * -GameObjects (logic)
 * -Renderables (render)
 */
public class GameWorld {
	// here be a more appropriate structure to contain renderables, etc
	
	// here be list of flat arrays
	private List<BaseController> controllers;
	private List<BaseModel> models;
	private List<BaseView> views;
	private List<GameObject> objects;	// for bookkeeping only
	
	// here be the game level data
	private BaseView levelView;		// level render data
	private BaseModel levelModel;	// level collision mesh, etc
	
	public GameWorld() {
		// do something nifty here
		controllers = new ArrayList<>();
		models = new ArrayList<>();
		views = new ArrayList<>();
		objects = new ArrayList<>();
	}
	
	public void setWorldData(BaseModel worldData, BaseView worldRender) {
		levelModel = worldData;
		levelView = worldRender;
	}
	
	public void preRender(GL2 gl, float dt) {
		// level
		if (levelView != null)
			levelView.preRender(gl, dt);
		
		// do something to all renderables here
		for (BaseView v : views) {
			v.preRender(gl, dt);
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
		
		// worlds of course
		if (levelModel != null)
			levelModel.update(dt);
		
		// controllers first
		for (BaseController c : controllers) {
			c.update(dt);
		}
		
		// models next
		for (BaseModel m : models) {
			m.update(dt);
		}
	}
	
	public void render(GL2 gl, float dt) {
		// render game level first
		if (levelView != null) {
			levelView.render(gl, dt);
		}
		// do real render here
		for (BaseView v : views) {
			v.render(gl, dt);
		}
	}
	
	// when adding object, we simply hold its data, not the object itself?
	public boolean addObject(GameObject o) {
		if (!objects.contains(o)) {
			// no duplicate, heheh
			// add to object
			objects.add(o);
			if (o.getController() != null)controllers.add(o.getController());
			if (o.getModel() != null)models.add(o.getModel());
			if (o.getView() != null)views.add(o.getView());
			return true;
		}
		// already there. nothing to do
		return false;
	}
}
