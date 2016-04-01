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

import com.bowie.gameeditor.Mesh.MeshGroup;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class MeshView extends Screen {
	
	GameWorld world;
	MThirdPersonCamera cam;
	CThirdPersonCamera camControl;
	CActorController player;
	
	public MeshView(Editor p) {
		super(p);
	}
	
	@Override
	public void onResize(GL2 gl, int x, int y, int w, int h) {
		gl.glViewport(x, y, w, h);
		// set camera's aspect
		cam.setAspect((float)w/(float)h);
		
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
		
		gl.glLineWidth(1.0f);
		
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		
//		parent.getLogger().log("glError: " + gl.glGetError());
	}
	
	@Override
	public void onTick(float dt) {
		world.update(dt);
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		player.keyDown(arg0);
	}
	
	@Override
	public void keyReleased(KeyEvent arg0) {
		player.keyUp(arg0);
	}
	

	@Override
	public void onDraw(GL2 gl, float dt) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// active camera
		Matrix4 projView = cam.getCamMatrix(dt);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(projView.m, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		world.preRender(gl, dt);
		world.render(gl, dt);
	}
	
	@Override
	public void onActive() {
		//it's safe to create shit here since gl context is valid
		SkeletonLoader skelLoader = new SkeletonLoader();
		SkAnimLoader animLoader = new SkAnimLoader();
		MeshLoader meshLoader = new MeshLoader();
		
		world = new GameWorld();
		
		// add our actor
		MActorState o_model = new MActorState();
		VDebugPhysics o_view = new VDebugPhysics(o_model);
		GameObject o = new GameObject(null, o_model, o_view);
		
		world.addObject(o);
		
		// add tracking camera
		cam = new MThirdPersonCamera(o);		
		cam.setFov(60.0f);
		camControl = new CThirdPersonCamera(cam);
		world.addObject(new GameObject(camControl, cam, null));
		
		player = new CActorController(cam, o);
		world.addObject(new GameObject(player, null, null));
		
		Texture2D tex = new Texture2D("D:\\projects\\data\\grass.png", true);
		GL2 gl = parent.getContext();
		tex.getTexture().setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		tex.getTexture().setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		world.setWorldData(null, new VLargeGrid(40.0f, 30.0f, tex));
	}
	
	@Override
	public void onDeactive() {
		
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {
		camControl.mouseDown(arg0);
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		camControl.mouseUp(arg0);
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		camControl.mouseDrag(arg0);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		camControl.mouseWheel(arg0);
	}
}
