package com.bowie.gameeditor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import com.jogamp.opengl.GL2;

public class MeshView extends Screen {
	
	//rotation data
	private float rot_x = 0.0f;
	private float rot_y = 0.0f;
	private float rot_z = 0.0f;
	
	private float cam_dist = 5.0f;	//means -z * cam_dist
	
	//cam status
	public static final int CAM_NONE = -1;
	public static final int CAM_PAN = 0;
	public static final int CAM_ROT = 1;
	
	private int cam_status = CAM_NONE;
	private float cam_max_rot_x = 85;	//max 85 degree
	private float cam_step = 0.1f;	//0.1 unit per scroll
	private float cam_fast_step_mult = 4.0f;	//4 time faster
	private float cam_min_dist = 0.1f;	//0.1 is closest
	
	//track cursor pos
	private int cx = 0;
	private int cy = 0;
	
	private Trackball tracker = new Trackball();
	
	//current mesh to be viewed
	private Mesh curMesh = null;
	private boolean meshReloading = false;
	private String meshFilename = "";
	
	//default cube mesh
	private Mesh cubeMesh = null;
	
	public MeshView(Editor p) {
		super(p);
	}
	
	public void drawSimpleCube() {
		//different format, so need different approach
	}
	
	public void setMesh(String filename) {
		meshFilename = filename;
		meshReloading = true;
	}
	
	@Override
	public void onResize(GL2 gl, int x, int y, int w, int h) {
		parent.getLogger().log("Mesh View resized!! " + x + ", " + y + ", " + w + " , " + h);
		tracker.setViewport(x, y, w, h);
		
		//reset gl status
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	@Override
	public void onDraw(GL2 gl, float dt) {
		if (meshReloading) {
			meshReloading = false;
			parent.getLogger().log("Here we should be loading shit: " + meshFilename);
			
			curMesh = new Mesh(meshFilename, gl);
			
//			System.out.println("Well shit");
			parent.getLogger().log(curMesh.toString());
		}
		
		if (cubeMesh == null) {
			//let's change to cylinder
			float cRad = 1.0f;
			int cStep = 8;
			
			//generate vertices
			
			//generate indices
			
			//color first, then vertex
			float [] vertices = {
					//uv
					0, 0,
					1, 0,
					1, 1,
					0, 1,
					1, 0,
					0, 0,
					0, 1,
					1, 1,
					
					//colors
					1, 0, 0,
					0, 1, 0,
					0, 0, 1,
					1, 1, 0,
					1, 0, 1,
					0, 1, 1,
					1, 0.5f,0,
					0.5f, 1, 0.25f,
					
					//vertices
					-1, -1, 1,
					1, -1, 1,
					1, 1, 1,
					-1, 1, 1,
					
					-1, -1, -1,
					1, -1, -1,
					1, 1, -1,
					-1, 1, -1
			};
			
			short [] indices = {
				0, 1, 2, 0, 2, 3,	//front
				1, 5, 6, 1, 6, 2,	//right
				3, 2, 6, 3, 6, 7,	//top
				4, 0, 3, 4, 3, 7,	//left
				5, 4, 7, 5, 7, 6,	//back
				0, 4, 5, 0, 5, 1	//bottom
			};
			
			cubeMesh = Mesh.buildSimpleCube(parent.getContext(), vertices, indices);
			parent.getLogger().log(cubeMesh.toString());
		}
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();
		
		Quaternion rot = tracker.getRotation();
		Matrix4 mat = new Matrix4(rot, new Vector3(0, 0, -cam_dist));
		
		gl.glMultMatrixf(mat.m, 0);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		if (curMesh != null) {
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
			
			curMesh.renderSimple(gl);
		} else if (cubeMesh != null) {
			gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, cubeMesh.getVBO());
			
			gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
			gl.glColorPointer(3, GL2.GL_FLOAT, 0, 8*2*4);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 8*2*4 + 8*3*4);
			
			
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, cubeMesh.getIBO());
			gl.glDrawElements(GL2.GL_TRIANGLES, 36, GL2.GL_UNSIGNED_SHORT, 0);
		}
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
	
	@Override
	public void onActive() {
		//it's safe to create shit here since gl context is valid
		
			
	}
	
	@Override
	public void onDeactive() {
		
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {
		int btn = arg0.getButton();
		if (btn == MouseEvent.BUTTON2) {
			//mid button mean rotating
			cam_status = CAM_ROT;
			
			tracker.beginTrack(arg0.getX(), arg0.getY());
		} else if(btn == MouseEvent.BUTTON3) {
			//right button mean panning
			cam_status = CAM_PAN;
		}
		
		//start tracking cursor
		cx = arg0.getX();
		cy = arg0.getY();
		
//		parent.getLogger().log("Cam stat: " + cam_status);
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		cam_status = CAM_NONE;
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		int curx = arg0.getX();
		int cury = arg0.getY();
		
		int dx = curx - cx;
		int dy = cury - cy;
		
		switch (cam_status) {
		case CAM_PAN:
			break;
		case CAM_ROT:
			rot_y += dx;
			rot_x += dy;
			cx = curx;
			cy = cury;
			
			//limit rotation
			rot_x = rot_x < -cam_max_rot_x ? -cam_max_rot_x : rot_x > cam_max_rot_x ? cam_max_rot_x : rot_x;
			
			tracker.track(arg0.getX(), arg0.getY());
			
			/*Vector3 tmp;
			
			tmp = tracker.vA;
			parent.getLogger().log("scA : " + tmp.x + ", " + tmp.y + ", " + tmp.z);
			
			tmp = tracker.vB;
			parent.getLogger().log("scB : " + tmp.x + ", " + tmp.y + ", " + tmp.z);
			
			float dp = Vector3.dot(tracker.vA, tracker.vB);
			parent.getLogger().log("DP: " + dp + " angle : " + Math.toDegrees(Math.acos(dp)));
			
			Quaternion q = tracker.rot;
			parent.getLogger().log("qrot: " + q.x + ", " + q.y + ", " + q.z + ", " + q.w);*/

			break;
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
//		parent.getLogger().log("wheel: " + arg0.getWheelRotation());
		float speed= 1.0f;
		if (arg0.isShiftDown())
			speed = cam_fast_step_mult;
		
		cam_dist += arg0.getWheelRotation() * cam_step * speed;
		//limit distance
		cam_dist = cam_dist < cam_min_dist ? cam_min_dist : cam_dist;
	}
}
