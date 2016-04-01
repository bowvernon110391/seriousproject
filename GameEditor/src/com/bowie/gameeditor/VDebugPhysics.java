package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

public class VDebugPhysics extends BaseView {
	private MActorState ref;
	public VDebugPhysics(MActorState refData) {
		ref = refData;
	}
	
	// here we simply draw shit
	@Override
	public void render(GL2 gl, float dt) {
		// draw shit here
		ref.debugDraw(gl, dt);
	}
}
