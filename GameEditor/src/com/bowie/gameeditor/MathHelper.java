package com.bowie.gameeditor;

public class MathHelper {

	public MathHelper() {
	}
	
	public static void calcSphereCoord(int x, int y, int [] screen, Vector3 res) {
		float sphereRadius = Math.min(screen[2], screen[3]);
		float vX = (float) (x-screen[0]) / (float) screen[2];
		float vY = (float) (y-screen[1]) / (float) screen[3];
		//map from [0..1] to [-1..1]
		
		vX = vX * 2.0f - 1.0f;
		vY = vY * 2.0f - 1.0f;
		
		//okay, z is simple
		float vZ = 1.0f - vX*vX - vY*vY;
		
		res.x = vX;
		res.y = vY;
		res.z = vZ;
		
		res.normalize();
	}
	
	/**
	 * Calculate quaternion rotation from 2 sphere coordinates
	 * @param scA the beginning point
	 * @param scB the end point
	 * @return a quaternion represent rotation from scA to scB
	 */
	public static Quaternion calcQuatDifference(Vector3 scA, Vector3 scB) {
		//get angle (in radians)
		float angle = (float) Math.acos(Vector3.dot(scA, scB));
		//get axis
		Vector3 axis = new Vector3();
		Vector3.cross(scA, scB, axis);
		axis.normalize();
		
		return Quaternion.makeAxisRot(axis, angle);
	}
	
	/**
	 * this wrap values to be between 0..1
	 * @param v
	 * @return value between 0..1
	 */
	public static float wrapFloat01(float v) {
		
		return (float) (v - Math.floor(v));
	}
	
	/**
	 * This converts phase [0..1] to appropriate frame time
	 * @param phase value
	 * @param trackTime [min..max] of frametime
	 * @return correct frametime
	 */
	public static float phaseToRenderTime(float phase, float [] trackTime) {
		if (trackTime.length >= 2) {
			return trackTime[0] * (1.0f-phase) + trackTime[1] * phase;
		}
		return 1.0f;
	}
	
	public static float clamp(float v, float min, float max) {
		return v < min ? min : v > max ? max : v;
	}
}
