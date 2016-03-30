package com.bowie.gameeditor;

/**
 * @author Bowie
 * a game object is a consolidation of:
 * - controller (player or ai)
 * - model (state machine)
 * - view (renderable + anim state machine)
 */

public class GameObject {
	protected BaseController controller;
	protected BaseModel model;
	protected BaseView view;
	
	public BaseController getController() {
		return controller;
	}

	public void setController(BaseController controller) {
		this.controller = controller;
	}

	public BaseModel getModel() {
		return model;
	}

	public void setModel(BaseModel model) {
		this.model = model;
	}

	public BaseView getView() {
		return view;
	}

	public void setView(BaseView view) {
		this.view = view;
	}
	
	public GameObject() {
		// nothing for now
	}
	
	public GameObject(BaseController c, BaseModel m, BaseView v) {
		// directly set data
		this.setController(c);
		this.setModel(m);
		this.setView(v);
	}
	
	
}
