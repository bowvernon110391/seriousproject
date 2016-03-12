package com.bowie.gameeditor;



/**
 * @author Bow
 * Trackball
 * -
 * a class for tracking rotation in 3D space
 * from a 2D viewport
 * map it from 2D coordinate to 3D sphere coord
 */
public class Trackball {
	private float sphereRadius = 1.0f;
	public Quaternion rot = new Quaternion();
	
	public int [] screen = {0, 0, 200, 200};
	
	//track cursor
	public int [] lastCursor = {0,0};
	public int [] curCursor = {0,0};
	
	public Vector3 vA = new Vector3();
	public Vector3 vB = new Vector3();
	
	/**
	 * empty ctor
	 */
	public Trackball() {
		
	}
	
	/**
	 * will map our clicks based on these screen prop
	 * @param x	: position of screenx
	 * @param y	: position of screeny
	 * @param w	
	 * @param h
	 */
	public void setViewport(int x, int y, int w, int h) {
		
		screen[0] = x;
		screen[1] = y;
		screen[2] = w;
		screen[3] = h;
	}
	
	
	public void beginTrack(int x, int y) {
		lastCursor[0] = curCursor[0] = x;
		lastCursor[1] = curCursor[1] = y;
	}
	
	public void track(int x, int y) {
		curCursor[0] = x;
		curCursor[1] = y;
		
		//only generate quaternion delta if it's different
		if (curCursor[0] == lastCursor[0] && curCursor[1] == lastCursor[1]) {
			return; //do nothing
		}
		
		//calculate sphere coordinate for start and end
		MathHelper.calcSphereCoord(lastCursor[0], lastCursor[1], screen, vA);
		MathHelper.calcSphereCoord(curCursor[0], curCursor[1], screen, vB);
		//flip the Y component tho
		vA.y = -vA.y;
		vB.y = -vB.y;
		//calculate difference of rotation between those two points
		Quaternion qRot = MathHelper.calcQuatDifference(vA, vB);
		//apply as rotation
		Quaternion.mul(qRot, rot, rot);
		rot.normalize();
		//update tracker
		lastCursor[0] = curCursor[0];
		lastCursor[1] = curCursor[1];
	}
	
	public void endTrack(int x, int y) {
	}
	
	public Quaternion getRotation() {
		return rot;
	}
}
