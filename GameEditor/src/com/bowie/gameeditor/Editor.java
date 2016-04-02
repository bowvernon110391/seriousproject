package com.bowie.gameeditor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

public class Editor implements ScriptCmdListener {	
	//local private variable
//	private GLJPanel canvas;
	private GLCanvas canvas;
	private FPSAnimator ticker;
	private GL2 context = null;
	private GLContext rawContext = null;
	
	//editor settings
	//--playback and timer
	private long timeLast = System.currentTimeMillis();
	private long fpsTimer = 0;
	private int fpsCount = 0;
	private float timeElapsed = 0.0f;
	
	private int tickPerSec = 30;	//30 FPS update rate
	private int renderPerSec = 60;	//50 FPS render rate
	
	//editor stat
	private boolean isPlaying = true;
	
	//--log & config
	private String logFilename = "log.txt";
	private String cfgFilename = "config.txt";
	
	//--all editor data here
	private GameDataFiles gdf = null;
	
	//--all screen are here
	private Screen [] views = {
			new MeshView(this)
	};
	
	//our active screen. -1 is nothing
	private int activeScreen = -1;
	//list of screens
	private int SCREEN_MESH_VIEW = 0;
	
	//--logger
	private Logger logger;
	
	//--test variable
	private float a = 0.0f;
	
	public Editor(Logger l) throws GLException {
		final Editor editor = this;
		
		//attach logger
		logger = l;
		
		//test
		logger.log("Editor spawned!");
//		logger.log("Byte order is: " + ByteOrder.nativeOrder());
		
		//create canvas
		GLProfile glp = GLProfile.getDefault();
		if (!glp.hasGLSL()) {
			throw new GLException("Fuck this device can't use shaders!!");
		}
		//set cap
//		GLProfile glp = GLProfile.getGL2ES2();
		GLCapabilities glcap = new GLCapabilities(glp);
		
		/*glcap.setHardwareAccelerated(true);
		glcap.setDepthBits(24);
		glcap.setDoubleBuffered(true);
		glcap.setRedBits(8);
		glcap.setGreenBits(8);
		glcap.setBlueBits(8);
		glcap.setAlphaBits(8);*/
		
//		canvas = new GLJPanel(glcap);
		canvas = new GLCanvas();
		
		canvas.addGLEventListener(new GLEventListener() {
			
			public void reshape(GLAutoDrawable drawable, int x, int y, int width,
					int height) {
				editor.canvasResize(drawable.getGL().getGL2(), x, y, width, height);
			}
			
			public void init(GLAutoDrawable drawable) {
				context = drawable.getGL().getGL2();
				editor.canvasInit(drawable.getGL().getGL2());				
			}
			
			public void dispose(GLAutoDrawable drawable) {
				editor.canvasDestroy(drawable.getGL().getGL2());
			}
			
			public void display(GLAutoDrawable drawable) {
				editor.canvasDraw(drawable.getGL().getGL2());				
			}
		});
		
		//also, start the fps animator
		ticker = new FPSAnimator(canvas, renderPerSec, true);
		ticker.start();
	}
	
	public GL2 getContext() {
		return context;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public GameDataFiles getDataFile() {
		return gdf;
	}
	
	//this initialize canvas
	public void canvasInit(GL2 gl) {	
		if (context == null) {
			context = gl;
		}		
		//start animating
//		ticker.start();
//		logger.log("editor initialized");
		//set default screen to mesh
		selectView(SCREEN_MESH_VIEW);
	}
	
	//this select screen
	public void selectView(int viewId) {
		if (viewId < 0 || viewId >= views.length)
			return;	//invalid viewId
		
		//valid viewId, switch current view to back
		if (activeScreen >= 0 && activeScreen < views.length) {
			//deactivate it
			views[activeScreen].onDeactive();
			
			//deattach listener
			canvas.removeMouseListener(views[activeScreen]);
			canvas.removeMouseMotionListener(views[activeScreen]);
			canvas.removeMouseWheelListener(views[activeScreen]);
			canvas.removeKeyListener(views[activeScreen]);
		}
		
		//activate our view
		views[viewId].onActive();
		activeScreen = viewId;		
		
		//attach listener
		canvas.addMouseListener(views[activeScreen]);
		canvas.addMouseMotionListener(views[activeScreen]);
		canvas.addMouseWheelListener(views[activeScreen]);
		canvas.addKeyListener(views[activeScreen]);
	}
	
	public Screen getCurrentView() {
		if (activeScreen >=0 )
			return views[activeScreen];
		return null;
	}
	
	//this do the drawing
	public void canvasDraw(GL2 gl) {
		//update first
		long timeCurrent = System.currentTimeMillis();
		long deltaTime = timeCurrent - timeLast;
		fpsTimer += deltaTime;
		timeElapsed += (float)deltaTime/1000.0f;
		float tickDt = 1.0f/(float)tickPerSec;
		timeLast = timeCurrent;
		
		if (isPlaying) {
			while(timeElapsed >= tickDt) {
				timeElapsed -= tickDt;
				if (activeScreen >= 0)
					views[activeScreen].onTick(tickDt);
			}
		} else {
			timeElapsed = 0.0f;
		}
		
		//draw
		if (activeScreen >= 0) {
			views[activeScreen].onDraw(gl, timeElapsed);
			fpsCount ++;
		}
		
		// display fps
		if (fpsTimer >= 1000) {
			fpsTimer = 0; // reset
			System.out.print("FPS: " + fpsCount + "\r");
			fpsCount = 0;
		}
	}
		
	//this resize te canvas
	public void canvasResize(GL2 gl, int x, int y, int w, int h) {
//		log("Resized: " + x + "," + y + "," + w + "," + h);
		
		// see that shit
		if (activeScreen >= 0)
			views[activeScreen].onResize(gl,x, y, w, h);
	}
	
	//this destroy canvas
	public void canvasDestroy(GL2 gl2) {
		logger.log("Well we're finished");
		logger.writeToFile(logFilename);
		
		//close master data file
		if (gdf != null) {
			gdf.close();
		}
	}
	
	//return canvas object
	public GLCanvas getCanvas() {
		return canvas;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public void playCanvas() {
		isPlaying = true;
	}
	
	public void stopCanvas() {
		isPlaying = false;
	}
	
	public void attachToPane(JPanel panel) {
		panel.add(canvas);
	}
	
	/*public void testMesh(String filename) {
		selectView(SCREEN_MESH_VIEW);
		
		MeshView m = (MeshView) getCurrentView();
		m.setMesh(filename);
	}*/
	
//	public void setupCamera(GL2 gl) {
//		gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
//		
//		Matrix4 m = new Matrix4();
//		Matrix4.perspective(camFOV, camAspect, camNear, camFar, m);
//		
//		gl.glMatrixMode(GL2.GL_PROJECTION);
//		//gl.glLoadMatrixf(m.getValues(), 0);
//		gl.glLoadMatrixf(m.getValues(), 0);
//		gl.glMatrixMode(GL2.GL_MODELVIEW);
//		gl.glLoadIdentity();
//		
//		//let's see the contents of modelview matrix
//		/*gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.m, 0);
//		
//		log("mv: "+m.m[0]+" "+m.m[4]+" "+m.m[8]+" "+m.m[12]);
//		log("mv: "+m.m[1]+" "+m.m[5]+" "+m.m[9]+" "+m.m[13]);
//		log("mv: "+m.m[2]+" "+m.m[6]+" "+m.m[10]+" "+m.m[14]);
//		log("mv: "+m.m[3]+" "+m.m[7]+" "+m.m[11]+" "+m.m[15]);*/
//	}
	
	public void setDataFile(String filename) {
		gdf = new GameDataFiles(filename);
		
		//log em
		Iterator<Entry<String, ZipEntry>> it = gdf.getFileMap(GameDataFiles.TYPE_ALL).entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> pair = it.next();
			
			ZipEntry f = (ZipEntry) pair.getValue();
			
			logger.log("data set: " + pair.getKey() + " : " + f.getCompressedSize() + " -> " + f.getSize());
			
//			it.remove();
		}
	}

	@Override
	public void commandEvent(String cmdString) {
		//user enter a command in console
		logger.log("You entered: " + cmdString);
		//1st, parse
		//2nd, execute
	}
}
