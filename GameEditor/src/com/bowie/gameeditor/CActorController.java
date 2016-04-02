package com.bowie.gameeditor;

import java.awt.event.KeyEvent;

public class CActorController extends BaseController {
	private MThirdPersonCamera cam;
	private MActorState actorState;
	
	private int keyUp = KeyEvent.VK_W;
	private int keyDown = KeyEvent.VK_S;
	private int keyLeft = KeyEvent.VK_A;
	private int keyRight = KeyEvent.VK_D;
	private int keyRun = KeyEvent.VK_SPACE;
	
	private int [] yMov = {0, 0};
	private int [] xMov = {0, 0};
	private int sprint = 0;
	
	public CActorController(MThirdPersonCamera cam, GameObject actor) {
		this.cam = cam;
		if (actor.getModel() instanceof MActorState) {
			this.actorState = (MActorState)actor.getModel();
		}
	}
	
	// do something when update
	@Override
	public void update(float dt) {
		// here we calculate our target velocity
		// based on keypress
		float rightMov = xMov[1] - xMov[0];
		float frontMov = yMov[1] - yMov[0];
		
		// get front and right vector
		if (cam != null && actorState != null) {
			Vector3 right = cam.getRightVector();
			Vector3 front = cam.getFrontVector();
			float targetSpeed = actorState.getMaxVel() * 0.25f;	// walkSpeed
			if (sprint > 0) {
				targetSpeed = actorState.getMaxVel();
			}
			
			Vector3 targetVel = new Vector3();
			
			targetVel.x += right.x * rightMov;
			targetVel.y += right.y * rightMov;
			targetVel.z += right.z * rightMov;
			
			targetVel.x += front.x * frontMov;
			targetVel.y += front.y * frontMov;
			targetVel.z += front.z * frontMov;
			// normalize and scale
			targetVel.normalize();
			targetVel.scale(targetSpeed);
			// set
			actorState.setTargetVel(targetVel);
		}
	}
	
	//handle key
	void keyDown(KeyEvent e) {
		final int kc = e.getKeyCode();
		
		if (kc == keyUp) yMov[0] = 1;
		if (kc == keyDown) yMov[1] = 1;
		
		if (kc == keyLeft) xMov[0] = 1;
		if (kc == keyRight) xMov[1] = 1;
		
		if (kc == keyRun) sprint = 1;
	}
	
	void keyUp(KeyEvent e) {
		final int kc = e.getKeyCode();	
		
		if (kc == keyUp) yMov[0] = 0;
		if (kc == keyDown) yMov[1] = 0;
		
		if (kc == keyLeft) xMov[0] = 0;
		if (kc == keyRight) xMov[1] = 0;
		
		if (kc == keyRun) sprint = 0;
	}
	
}
