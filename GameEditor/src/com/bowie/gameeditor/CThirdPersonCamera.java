package com.bowie.gameeditor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class CThirdPersonCamera extends BaseController {
	
	private MThirdPersonCamera ref;
	private int lastX, lastY;
	
	public CThirdPersonCamera(MThirdPersonCamera refData) {
		ref = refData;
	}
	
	public void mouseDown(MouseEvent e) {
		lastX = e.getX();
		lastY = e.getY();
	}
	
	public void mouseDrag(MouseEvent e) {
		int cX = e.getX();
		int cY = e.getY();
		
		int dX = cX - lastX;
		int dY = cY - lastY;
		
		lastX = cX;
		lastY = cY;
		
		// calculate rotation here
		if (ref != null) {
			float xzRot = ref.getTargetXZRot();
			float yRot = ref.getTargetYRot();
			float rotRate = ref.getRotPerPixel();
			
			xzRot += -dX * rotRate;
			yRot += dY * rotRate;
			
			ref.setTargetXZRot(xzRot);
			ref.setTargetYRot(yRot);
		}
	}
	
	public void mouseUp(MouseEvent e) {
		// do nothing
		lastX = e.getX();
		lastY = e.getY();
	}
	
	public void mouseWheel(MouseWheelEvent e) {
		if (ref != null) {
			float targetZoomDist = ref.getZoomDist() + e.getWheelRotation() * ref.getZoomPerPixel();
			ref.setTargetZoomDist(targetZoomDist);
		}
	}
}
