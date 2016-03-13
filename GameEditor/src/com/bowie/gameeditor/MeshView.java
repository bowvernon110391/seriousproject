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
	
	//current mesh to be viewed
	private Mesh curMesh = null;
	private boolean meshReloading = false;
	private String meshFilename = "";
	
	private Texture2D curTex = null;
	private boolean texReloading = false;
	private String texFilename = "";

	int curFaceCull = GL2.GL_BACK;
	boolean cullModeChanged = false;
	
	//default cube mesh
	private Mesh cubeMesh = null;
	
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
	
	public void drawSimpleCube() {
		//different format, so need different approach
	}
	
	public void setMesh(String filename) {
		meshFilename = filename;
		meshReloading = true;
	}
	
	public void setTexture(String filename) {
		texFilename = filename;
		texReloading = true;
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
		
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		gl.glFrontFace(GL2.GL_CCW);
		gl.glShadeModel(GL2.GL_SMOOTH);
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		
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
		
		if (arg0.getKeyCode() == 'R') {
			//open up readme files
			GameDataFiles gdf = parent.getDataFile();
			if (gdf != null) {
				byte [] tmpBuf = gdf.getBytes("readme.txt");
				if (tmpBuf != null) {
					parent.getLogger().log(new String(tmpBuf, Charset.forName("UTF-8")));
				}
			}
		}
		
		if (arg0.getKeyCode() == 'M') {
			//model test
			meshReloading = true;
			meshFilename = "mesh/male_char_proto.bmf";
		}
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
			parent.getLogger().log(testShader.toString());
		}
		
		if (meshReloading) {
			meshReloading = false;
//			parent.getLogger().log("Here we should be loading shit: " + meshFilename);
//			
//			curMesh = new Mesh(meshFilename, gl);
//			
//			parent.getLogger().log(curMesh.toString());
			if (parent.getLogger() != null) {
				curMesh = new Mesh(parent.getDataFile().getBytes(meshFilename), gl);
			} else {
				parent.getLogger().log("Datafile not set");
			}
		}
		
		if (texReloading) {
			texReloading = false;
			parent.getLogger().log("reloading texture: " + texFilename);
			
			curTex = new Texture2D(texFilename, true);
			curTex.getTexture().setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			curTex.getTexture().setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
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
//			parent.getLogger().log(cubeMesh.toString());
		}
		
		if (cullModeChanged) {
			cullModeChanged = false;
			gl.glCullFace(curFaceCull);
		}
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		Quaternion rot = tracker.getRotation();
		Matrix4 mat = new Matrix4(rot, new Vector3());
		
		/*//set projection for camera
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getProjView().m, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		gl.glLoadMatrixf(mat.m, 0);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		//enable texturing if possible
		if (curTex != null) {
			gl.glEnable(GL2.GL_TEXTURE_2D);
			curTex.getTexture().bind(gl);
			gl.glEnable(GL2.GL_BLEND);;
		}
		
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
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_BLEND);
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);*/
		
		//use shader instead
		if (curShader != null && cubeMesh != null) {
			curShader.useShader(gl);
		
			//send uniform
			gl.glUniformMatrix4fv(curShader.getUniformLoc(Shader.MAT_PROJVIEW), 1, false, camera.getProjView().m, 0);
			gl.glUniformMatrix4fv(curShader.getUniformLoc(Shader.MAT_MODEL), 1, false, mat.m, 0);
			
			gl.glEnableVertexAttribArray(Shader.ATTRIB_POS);
			gl.glEnableVertexAttribArray(Shader.ATTRIB_COL);
			
			//draw here
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, cubeMesh.getVBO());
			gl.glVertexAttribPointer(Shader.ATTRIB_POS, 3, GL2.GL_FLOAT, false, 0, 8*2*4 + 8*3*4);
			gl.glVertexAttribPointer(Shader.ATTRIB_COL, 3, GL2.GL_FLOAT, false, 0, 8*2*4);
			
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, cubeMesh.getIBO());
			gl.glDrawElements(GL2.GL_TRIANGLES, 36, GL2.GL_UNSIGNED_SHORT, 0);
			
			gl.glDisableVertexAttribArray(Shader.ATTRIB_POS);
			gl.glDisableVertexAttribArray(Shader.ATTRIB_COL);
		}
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
