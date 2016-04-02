package com.bowie.gameeditor;

/**
 * this would track an object's physics prop
 * @author Bowie
 *
 */
public class MThirdPersonCamera extends BaseModel {
	// perspective data
	private float fov = 55.0f;
	private float aspect = 1.0f;
	private float znear = 0.1f;
	private float zfar = 1000.0f;
	
	private Vector3 up = new Vector3(0, 1, 0);
	private Vector3 right = new Vector3();
	private Vector3 front = new Vector3();
	
	private Vector3 camPos = new Vector3();		// where the camera at
	private Vector3 camLookAt = new Vector3();	// where it's looking at
	
	private Vector3 targetCamPos = new Vector3();
	private Vector3 targetCamLookAt = new Vector3();
	
	// it's calculated
	private Vector3 camOffset = new Vector3();
	
	// these are in radians
	private float xzRot = 0;
	private float yRot = (float) Math.toRadians(40);
	private float zoomDist = 4.0f;
	
	// these are the targets
	private float targetXZRot = xzRot;
	private float targetYRot = yRot;
	private float targetZoomDist = zoomDist;
	
	private float minZoomDist = 0.2f;
	private float maxZoomDist = 10.0f;
	
	private float minYRot = (float) Math.toRadians(5.0f);
	private float maxYRot = (float) Math.toRadians(80.0f);
	
	private float rotPerPixel = (float) Math.toRadians(0.5f);	// 2/180 radian / pixel
	private float zoomPerPixel = 0.2f;
	
	private float elasticity = 4.0f;	// 90% each time
	private float minHeight = 2.0f;		// minimum of 2 in y direction (between lookat and pos)
	private Vector3 lookAtOffset = new Vector3(0, 0.5f, 0);	// offset in object's local space
	
	private MActorState ref = null;
	
	public MThirdPersonCamera(GameObject o) {
		trackObject(o);
	}
	
	public void trackObject(GameObject o) {
		if (o == null)
			return;
		// not null, get its model and hope it's physics
		if (o.getModel() instanceof MActorState) {
			ref = (MActorState) o.getModel();
		}
	}
	
	private Vector3 calcOffsetVector(float xzRot, float yRot, float zoomDist) {
		Vector3 v = new Vector3();
		
		v.x = (float) (Math.sin(xzRot) * Math.cos(yRot) * zoomDist);
		v.z = (float) (Math.cos(xzRot) * Math.cos(yRot) * zoomDist);
		v.y = (float) (Math.sin(yRot) * zoomDist);
		return v;
	}
	
	private void updateCamOffset(float dt) {
		// update offset vector
		xzRot += (targetXZRot - xzRot) * elasticity * dt;
		yRot += (targetYRot - yRot) * elasticity * dt;
		zoomDist += (targetZoomDist - zoomDist) * elasticity * dt;
		
		camOffset = calcOffsetVector(xzRot, yRot, zoomDist);
	}
	
	public Vector3 getRightVector() {
		return new Vector3((float) Math.cos(xzRot), 0, (float) -Math.sin(xzRot));
	}
	
	public Vector3 getFrontVector() {
		return new Vector3((float) Math.sin(xzRot), 0, (float) Math.cos(xzRot));
	}
	
	@Override
	public void update(float dt) {
		if (ref != null) {
			updateCamOffset(dt);
			
			// we have something to track on
			// calculate our lookat to target
			ref.rot.transformVector(lookAtOffset, targetCamLookAt);
			Vector3.add(targetCamLookAt, ref.pos, targetCamLookAt);
			// move it
			camLookAt.x += (targetCamLookAt.x - camLookAt.x) * elasticity * dt;
			camLookAt.y += (targetCamLookAt.y - camLookAt.y) * elasticity * dt;
			camLookAt.z += (targetCamLookAt.z - camLookAt.z) * elasticity * dt;
			
			// calculate our pos target
			Vector3.add(camLookAt, camOffset, camPos);
		}
		
//		System.out.println("l: " + camLookAt.x + ", " + camLookAt.y + ", " + camLookAt.z);
	}
	
	public Matrix4 getCamMatrix(float dt) {
		Vector3 tmp0 = new Vector3();
		
		tmp0.x = camLookAt.x + (targetCamLookAt.x - camLookAt.x) * elasticity * dt;
		tmp0.y = camLookAt.y + (targetCamLookAt.y - camLookAt.y) * elasticity * dt;
		tmp0.z = camLookAt.z + (targetCamLookAt.z - camLookAt.z) * elasticity * dt;
//		tmp0 = camLookAt;
		
		// calculate temporary offset
		float i_xzRot = xzRot + (targetXZRot - xzRot) * elasticity * dt;
		float i_yRot = yRot + (targetYRot - yRot) * elasticity * dt;
		float i_zoomDist = zoomDist + (targetZoomDist - zoomDist) * elasticity * dt;
		
		Vector3 tmpOffset = calcOffsetVector(i_xzRot, i_yRot, i_zoomDist);
		
		Vector3 tmp1 = new Vector3();
		Vector3.add(tmp0, tmpOffset, tmp1);
		
		// calculate front
		Vector3.sub(tmp1, tmp0, front);
		// reset up
		up.x = up.z = 0;
		up.y =1;
		// calculate right
		Vector3.cross(up, front, right);
		// calculate up
		Vector3.cross(front, right, up);
		// normalize all
		front.normalize();
		up.normalize();
		right.normalize();
		
		// make matrix
		Matrix4 view = new Matrix4();
		view.setPosition(tmp1);
		view.setRotation(right, up, front);
		// simply invert
		view.fastInvert();
		
		// make projection
		Matrix4 proj = new Matrix4();
		Matrix4.perspective(fov, aspect, znear, zfar, proj);
		
		// return the projview shit
		Matrix4.mul(proj, view, proj);
		return proj;
	}

	public float getFov() {
		return fov;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public float getAspect() {
		return aspect;
	}

	public void setAspect(float aspect) {
		this.aspect = aspect;
	}

	public float getZnear() {
		return znear;
	}

	public void setZnear(float znear) {
		this.znear = znear;
	}

	public float getZfar() {
		return zfar;
	}

	public void setZfar(float zfar) {
		this.zfar = zfar;
	}

	public float getMinZoomDist() {
		return minZoomDist;
	}

	public void setMinZoomDist(float minZoomDist) {
		this.minZoomDist = minZoomDist;
	}

	public float getMaxZoomDist() {
		return maxZoomDist;
	}

	public void setMaxZoomDist(float maxZoomDist) {
		this.maxZoomDist = maxZoomDist;
	}

	public float getRotPerPixel() {
		return rotPerPixel;
	}

	public void setRotPerPixel(float rotPerPixel) {
		this.rotPerPixel = rotPerPixel;
	}

	public float getMinYRot() {
		return minYRot;
	}

	public void setMinYRot(float minYRot) {
		this.minYRot = minYRot;
	}

	public float getMaxYRot() {
		return maxYRot;
	}

	public void setMaxYRot(float maxYRot) {
		this.maxYRot = maxYRot;
	}

	public float getTargetXZRot() {
		return targetXZRot;
	}

	public void setTargetXZRot(float targetXZRot) {
		this.targetXZRot = targetXZRot;
	}

	public float getTargetYRot() {
		return targetYRot;
	}

	public void setTargetYRot(float targetYRot) {
		this.targetYRot = targetYRot;
		// clamp
		if (this.targetYRot > this.maxYRot)
			this.targetYRot = this.maxYRot;
		if (this.targetYRot < this.minYRot)
			this.targetYRot = this.minYRot;
	}

	public float getTargetZoomDist() {
		return targetZoomDist;
	}

	public void setTargetZoomDist(float targetZoomDist) {
		this.targetZoomDist = targetZoomDist > maxZoomDist ? maxZoomDist : targetZoomDist < minZoomDist ?
				minZoomDist : targetZoomDist;
	}

	public float getZoomPerPixel() {
		return zoomPerPixel;
	}

	public void setZoomPerPixel(float zoomPerPixel) {
		this.zoomPerPixel = zoomPerPixel;
	}

	public float getZoomDist() {
		return zoomDist;
	}

	public void setZoomDist(float zoomDist) {
		this.zoomDist = zoomDist;
	}

	public Vector3 getLookAtOffset() {
		return lookAtOffset;
	}

	public void setLookAtOffset(Vector3 lookAtOffset) {
		this.lookAtOffset = lookAtOffset;
	}

	public float getElasticity() {
		return elasticity;
	}

	public void setElasticity(float elasticity) {
		this.elasticity = elasticity;
	}
}
