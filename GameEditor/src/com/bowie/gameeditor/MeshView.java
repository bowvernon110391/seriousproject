package com.bowie.gameeditor;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
	
	//current state
	int curFaceCull = GL2.GL_BACK;
	boolean cullModeChanged = false;
	
	//skeleton
	public Skeleton skel = null;
	boolean skelDrawMatrix = false;
	boolean skelSelChange = false;	//selection has changed
	int skelBoneId = -1;	//none selected
	
	//teh shader
	boolean shaderReloading = true;
	Shader curShader = null;
	
	//shit camera
	PerspectiveCamera camera = new PerspectiveCamera();
	
	public MeshView(Editor p) {
		super(p);
		
		//set default props
		camera.setPos(0, 0, 5);
	}
	
	@Override
	public void onResize(GL2 gl, int x, int y, int w, int h) {
//		parent.getLogger().log("Mesh View resized!! " + x + ", " + y + ", " + w + " , " + h);
		tracker.setViewport(x, y, w, h);
		camera.setViewport(gl, x, y, w, h);
		
		//reset gl status
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glClearDepth(1.0);
		gl.glClearColor(0.2f, 0.5f, 0.1f, 0.0f);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		gl.glFrontFace(GL2.GL_CCW);
		gl.glShadeModel(GL2.GL_SMOOTH);
		
		gl.glLineWidth(3.0f);
		
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
//		parent.getLogger().log("glError: " + gl.glGetError());
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		super.keyPressed(arg0);
//		parent.getLogger().log("Key pressed: " + arg0.getKeyCode());
		if (arg0.getKeyCode() == 'F') {
			curFaceCull = curFaceCull == GL2.GL_BACK ? GL2.GL_FRONT : GL2.GL_BACK;
			cullModeChanged = true;
		}
		
		//bone data traversal
		if (arg0.getKeyCode() == 'M') {
			skelDrawMatrix = !skelDrawMatrix;
		}
		
		if (arg0.getKeyCode() == KeyEvent.VK_LEFT) {
			skelBoneId --;
			skelSelChange = true;
		}
		
		if (arg0.getKeyCode() == KeyEvent.VK_RIGHT) {
			skelBoneId ++;
			skelSelChange = true;
		}
	}
	
	private void drawTestShader(GL2 gl, float dt) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		Quaternion rot = tracker.getRotation();
		Matrix4 mat = new Matrix4(rot, new Vector3());

		// use shader instead
		if (curShader == null)
			return;
		
		curShader.useShader(gl);
	}
	
	void drawTestCube(GL2 gl, float dt) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		Quaternion rot = tracker.getRotation();
		Matrix4 mat = new Matrix4(rot, new Vector3());
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getProjView().m, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		gl.glLoadMatrixf(mat.m, 0);
		 
	}
	
	void drawTestSkeleton(GL2 gl, float dt) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		Quaternion rot = tracker.getRotation();
		Matrix4 mat = new Matrix4(rot, new Vector3());
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getProjView().m, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		gl.glLoadMatrixf(mat.m, 0);
		
		if (skel == null)
			return;
		
		gl.glBegin(GL2.GL_LINES);
			for (int i=0; i<skel.bones.size(); i++) {
				Skeleton.Bone b = skel.bones.get(i);
				
				gl.glColor3f(1, 0, 0);
				gl.glVertex3f(b.abs.head.x, b.abs.head.y, b.abs.head.z);
				
				gl.glColor3f(0, 0, 1);
				gl.glVertex3f(b.abs.tail.x, b.abs.tail.y, b.abs.tail.z);
			}
			//now we draw bone matrix
			float matScale = 1.05f;
			
			//Draw matrix
			if (skelDrawMatrix && skel.bones.size() > 0) {
				//correct the bone id first
				skelBoneId = skelBoneId >= skel.bones.size() ? skel.bones.size()-1 : skelBoneId < 0 ? 0 : skelBoneId;
				
				//log
				if (skelSelChange) {
					skelSelChange = false;
					parent.getLogger().log("bone selected: " + skel.bones.get(skelBoneId).name);
				}
				
				//root point is absolute head
				Vector3 matP = skel.bones.get(skelBoneId).abs.head;
				//rotation is absolute rotation
				Quaternion matR = skel.bones.get(skelBoneId).abs.rot;
				
				Matrix3 boneMat = new Matrix3();
				matR.toMatrix3(boneMat);
				
				float [] colors = {
						1, 0.2f, 0.2f,
						0.2f, 1, 0.2f,
						0.2f, 0.2f, 1
				};
				
				//draw em
				Vector3 vTmp = new Vector3();
				for (int i=0; i<3; i++) {
					vTmp.x = matP.x + boneMat.m[i*3] * matScale;
					vTmp.y = matP.y + boneMat.m[i*3+1] * matScale;
					vTmp.z = matP.z + boneMat.m[i*3+2] * matScale;
					
					//issue draw call
					gl.glColor3fv(colors, i*3);
					gl.glVertex3f(matP.x, matP.y, matP.z);
					gl.glVertex3f(vTmp.x, vTmp.y, vTmp.z);
				}
			}
		gl.glEnd();
	}

	@Override
	public void onDraw(GL2 gl, float dt) {
		if (shaderReloading) {
			shaderReloading = false;
			// now attempt to load shader
			
//			InputStream vs = Helper.getResourceFromJAR("/test.vs");
//			parent.getLogger().log("vs loaded? "+(vs!=null));
//			byte [] vsData = Helper.getBytesFromInputStream(vs);
//			String vsSource = new String(vsData);
//			parent.getLogger().log(vsSource);
			
			Shader testShader = new Shader(gl, Helper.getBytesFromInputStream(Helper.getResourceFromJAR("/test.vs")),
					Helper.getBytesFromInputStream(Helper.getResourceFromJAR("/test.fs")));
			curShader = testShader;
			/*if (!testShader.isError()) {
				parent.getLogger().log("Shader loaded!!");
				curShader = testShader;
				
				//let's see what our shader got
				parent.getLogger().log(curShader.toString());
			}*/
//			parent.getLogger().log(testShader.toString());
		}
		
		if (cullModeChanged) {
			cullModeChanged = false;
			gl.glCullFace(curFaceCull);
		}
		
//		drawTestCube(gl, dt);
		drawTestSkeleton(gl, dt);
		
	}
	
	@Override
	public void onActive() {
		//it's safe to create shit here since gl context is valid
		SkeletonLoader skelLoader = new SkeletonLoader();
		SkAnimLoader animLoader = new SkAnimLoader();
		
		skel = skelLoader.loadSkeleton("D:\\bone_experiment.skel");
		SkAnim skanim = animLoader.loadSkAnim("D:\\bone_experiment.skanim");
		
		if (skanim == null) {
			parent.getLogger().log("Failed loading skeletal animation");
		} else {
			parent.getLogger().log("SKANIM DATA\n");
			parent.getLogger().log(skanim.toString());
		}
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
		
		Vector3 camPos = camera.getPos();
		cam_dist = camPos.z;
		
		cam_dist += arg0.getWheelRotation() * cam_step * speed;
		//limit distance
		cam_dist = cam_dist < cam_min_dist ? cam_min_dist : cam_dist;
		
		camPos.z = cam_dist;
		camera.setPos(camPos.x, camPos.y, camPos.z);
	}
}
