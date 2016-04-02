package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

/**
 * MC stands for Model Component
 * This is for containing a game object 
 * physical properties
 * @author Bowie
 *
 */
public class MActorState extends BaseModel {
	// some states
	public final int STATE_IDLE = 0;
	public final int STATE_MOVING = 1;
	public final int STATE_SLIDE_TO_STOP = 2;
	
	// some global shared data
	public float slideSpeedThreshold = 5.0f;
	
	// physics data
	public Vector3 pos = new Vector3();
	public Vector3 vel = new Vector3();
	public Vector3 targetVel = new Vector3();
	public float accel = 20.0f;
	public float maxVel = 8.0f;	// standard
	
	public float width = 0.6f;		
	public float height = 1.85f;
	public Quaternion rot = new Quaternion();	// to hold rotation data
	public Quaternion targetRot = new Quaternion();	// where should I look
	public float rotSpeed = 5.0f;			// how quick do we turn?
	
	// must be replaced by state machine!!
	public int state = STATE_IDLE;
	boolean onGround = true;
	
	// here be handle to animation states (for event trigger)
	private AnimStateManager animState;
	
	public void setState(int state) {
		this.state = state;
		
		// inform the animation state
		if (animState != null) {
			animState.changeTo(state);
		}
	}
	
	@Override
	public void update(float dt) {
		// some common things?
		if (state == STATE_IDLE) {
			// we're idling, stop immediately
			vel.x = 0;
			vel.z = 0;
			
			if (Vector3.dot(targetVel, targetVel) > Vector3.EPSILON)
				this.setState(STATE_MOVING);
		} else if (state == STATE_MOVING) {
			// move velocity to target as close as possible
			Vector3 targetDiff = Vector3.tmp0;
			Vector3.sub(targetVel, vel, targetDiff);
			
			// cap to max acceleration
			float validAccel = Math.min(accel, targetDiff.length());
			targetDiff.normalize();
			targetDiff.scale(validAccel * dt);
			
			// add em
			Vector3.add(targetDiff, vel, vel);
			
			// cap velocity
			float vLen = vel.length();
			if (vLen > maxVel) {
				vLen = maxVel / vLen;
				vel.scale(vLen);
			}
			
			// inform animstate
			if (animState != null) {
				animState.setMoveSpeed(vel.length());
			}
			
			// rotate based on shit
			if (Vector3.dot(vel, vel) > Vector3.EPSILON) {
				// set target Rotation
				Vector3 rotAxis = Vector3.tmp1;
				rotAxis.x = 0;
				rotAxis.z = 0;
				rotAxis.y = 1;
				targetRot = Quaternion.makeAxisRot(rotAxis, (float) Math.atan2(vel.x, vel.z));
			}
			
			// wait are we stopping?
			if (Vector3.dot(targetVel, targetVel) <= Vector3.EPSILON) {
				// how fast are we moving?
				float spdSqr = Vector3.dot(vel, vel);
				if (spdSqr >= slideSpeedThreshold * slideSpeedThreshold) {
//					state = STATE_SLIDE_TO_STOP;
					this.setState(STATE_SLIDE_TO_STOP);
				} else if (spdSqr >= 0.01f * maxVel * maxVel) {
					// still moving, but slow down
					vel.x *= 0.75f;
					vel.z *= 0.75f;
				} else {
//					state = STATE_IDLE;
					this.setState(STATE_IDLE);
				}
			}
		} else if (state == STATE_SLIDE_TO_STOP) {
			// tell
			if (animState != null)
				animState.setMoveSpeed(vel.length());
			// if we're too slow, idling
			if (Vector3.dot(vel, vel) < 0.01f * maxVel * maxVel) {
//				state = STATE_IDLE;
				this.setState(STATE_IDLE);
			}
			// half our speed to the stopping point
			vel.x *= 0.875f;
			vel.z *= 0.875f;
		}
		
		// position needs updating never the less
		pos.x += vel.x * dt;
		pos.y += vel.y * dt;
		pos.z += vel.z * dt;
		
		// same with rotation
		Quaternion.slerp(rot, targetRot, rotSpeed * dt, rot);
		
//		System.out.println("State: " + state);
		
		// update animation state
		if (animState != null) 
			animState.update(dt);
	}
	
	public void calculateRenderMatrix(float dt, Matrix4 res) {
		// calculate our render matrix and render
		Vector3 newPos = Vector3.tmp0;
		newPos.x = pos.x + vel.x * dt;
		newPos.y = pos.y + vel.y * dt;
		newPos.z = pos.z + vel.z * dt;
		
		Quaternion newRot = Quaternion.tmp0;
		Quaternion.slerp(rot, targetRot, rotSpeed * dt, newRot);
		
		res.fromQuatVec3(newRot, newPos);
	}
	
	// here be states though
	public void debugDraw(GL2 gl, float dt) {
		float halfw = width * 0.5f;
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(1, 1, 0);
			
			// front loop
			gl.glVertex3f(-halfw, 0, halfw);
			gl.glVertex3f( halfw, 0, halfw);
			gl.glVertex3f( halfw, height, halfw);
			gl.glVertex3f(-halfw, height, halfw);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glColor3f(0, 1, 0);
			
			// back loop
			gl.glVertex3f( halfw, 0, -halfw);
			gl.glVertex3f(-halfw, 0, -halfw);
			gl.glVertex3f(-halfw, height, -halfw);
			gl.glVertex3f( halfw, height, -halfw);
		gl.glEnd();
		
		gl.glBegin(GL2.GL_LINES);
			// connecting lines
			gl.glColor3f(1, 1, 0);
			gl.glVertex3f(-halfw, 0, halfw);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(-halfw, 0, -halfw);
			
			gl.glColor3f(1, 1, 0);
			gl.glVertex3f(halfw, 0, halfw);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(halfw, 0, -halfw);
			
			gl.glColor3f(1, 1, 0);
			gl.glVertex3f(halfw, height, halfw);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(halfw, height, -halfw);
			
			gl.glColor3f(1, 1, 0);
			gl.glVertex3f(-halfw, height, halfw);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(-halfw, height, -halfw);
		gl.glEnd();
	}

	public Vector3 getTargetVel() {
		return targetVel;
	}

	public void setTargetVel(Vector3 targetVel) {
		this.targetVel = targetVel;
	}

	public float getMaxVel() {
		return maxVel;
	}

	public void setMaxVel(float maxVel) {
		this.maxVel = maxVel;
	}

	public AnimStateManager getAnimState() {
		return animState;
	}

	public void setAnimState(AnimStateManager animState) {
		this.animState = animState;
	}
}
