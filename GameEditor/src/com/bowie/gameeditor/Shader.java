package com.bowie.gameeditor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

public class Shader {
	//common shared shader properties
	
	public static final int ATTRIB_POS = 0;		//in shader: attrib vec3 pos;
	public static final int ATTRIB_COL = 1;		//in shader: attrib vec4 col;
	public static final int ATTRIB_UV = 2;		//in shader: attrib vec2 uv;
	public static final int ATTRIB_NOR = 3;		//in shader: attrib vec3 nor;
	
	public static final int [] attribId = {
		ATTRIB_POS, ATTRIB_COL, ATTRIB_UV, ATTRIB_NOR
	};
	
	public static final String [] attribNames = {
		"pos", "col", "uv", "nor"
	};
	
	//common uniform name (stored inside)
	public static final int UNIFORM_TEX0 = 0;	//uniform[0]
	public static final int UNIFORM_TEX1 = 1;	//uniform[1]
	public static final int UNIFORM_TEX2 = 2;	//uniform[2]
	public static final int UNIFORM_TEX3 = 3;	//uniform[3]
	public static final int MAT_PROJVIEW = 4;
	public static final int MAT_MODEL = 5;
	
	public static final int [] uniformId = {
		UNIFORM_TEX0, UNIFORM_TEX1, UNIFORM_TEX2, 
		UNIFORM_TEX3, MAT_PROJVIEW, MAT_MODEL
	};
	
	public static String [] uniformNames = {
		"tex0", "tex1", "tex2", "tex3",
		"matprojview", "matmodel"
	};
	
	//private properties
	private List<Integer> uniforms = new ArrayList<>(uniformId.length);
	private int progId = -1;	//each shader program has this
	private boolean loaded = false;	//not loaded
	private boolean errord = false;	//not error yet
	private String lastError = "";
	
	/**
	 * this construct a shader
	 * @param gl	the opengl object
	 * @param vsSource	the byte array containing vertex shader source
	 * @param fsSource	the byte array containing fragment shader source
	 */
	public Shader(GL2 gl, byte [] vsSource, byte [] fsSource) {
		loadShader(gl, vsSource, fsSource);
	}
	
	public Shader(GL2 gl, String vsFilename, String fsFilename) {
		
	}
	
	public boolean loadShader(GL2 gl, byte[] vsSource, byte[] fsSource) {
		//first, we compile each shader
		int vsId = compileShaderSource(gl, GL2.GL_VERTEX_SHADER, vsSource);
		if (vsId == 0 ) {
			return false;
		}
		int fsId = compileShaderSource(gl, GL2.GL_FRAGMENT_SHADER, fsSource);
		if (fsId == 0) {
			return false;
		}
		//then create a program
		progId = gl.glCreateProgram();
		if (progId == 0) {
			System.out.println("Shader: failed creating program");
			return false;
		}
		//attach each shader
		gl.glAttachShader(progId, vsId);
		gl.glAttachShader(progId, fsId);
		//bind common attributes
		bindCommonAttributes(gl);
		//link
		gl.glLinkProgram(progId);
		//check linking
		int [] params = new int[1];
		gl.glGetProgramiv(progId, GL2.GL_LINK_STATUS, params, 0);
		if (params[0] != GL2.GL_TRUE) {
			//shit
			System.out.println("Shader: linking failed");
			return false;
		}
		//alright now we query common uniforms
		//to cache it
		gl.glUseProgram(progId);
		queryCommonUniforms(gl);
		//return
		gl.glUseProgram(0);
		return true;		
	}
	
	/**
	 * this one load shader source and compile it
	 * @param gl the opengl object
	 * @param mode {GL_VERTEX_SHADER, GL_FRAGMENT_SHADER}
	 * @param source the byte array containing shader
	 * @return 0 if fails, otherwise return real id
	 */
	private int compileShaderSource(GL2 gl, int mode, byte[] source) {
		if (source == null) {
			System.out.println("Shader: null source");
			return 0;
		}
		
		if (source.length == 0) {
			System.out.println("Shader: empty source");
			return 0;
		}
		
		int sourceId = gl.glCreateShader(mode);
		if (sourceId == 0) {
			//we fail
			System.out.println("Shader: failed creating shader: " + mode);
			return 0;
		}
		
		//create utf8 string
		
		String [] src = { 
				new String(source, Charset.forName("UTF-8"))
		};
		
		int [] srcLen = {
				src[0].length()
		};
		
		gl.glShaderSource(sourceId, 1, src, srcLen, 0);
		gl.glCompileShader(sourceId);
		//check em out
		int [] compFlag = new int[1];
		gl.glGetShaderiv(sourceId, GL2.GL_COMPILE_STATUS, compFlag, 0);
		if (compFlag[0] != GL2.GL_TRUE) {
			//well sheeiiitt
			byte [] shaderLog = new byte[1024];
			int [] shaderLogLen = {1024};
			
			gl.glGetShaderInfoLog(sourceId, 1024, shaderLogLen, 0, shaderLog, 0);
			String logMessage = new String(shaderLog);
			
			System.out.println("Shader: failed to compile. reason:");
			System.out.println(logMessage);
			return 0;
		}
		
		return 0;
	}
	
	private void queryCommonUniforms(GL2 gl) {
		//this only query common uniforms
		//for more uniform, you have to add 
		//manually
		for (int i=0; i<uniformNames.length; i++) {
			int loc = gl.glGetUniformLocation(progId, uniformNames[i]);
			uniforms.set(i, loc);
		}
	}
	
	private void bindCommonAttributes(GL2 gl) {
		for (int i=0; i<attribId.length; i++) {
			gl.glBindAttribLocation(progId, attribId[i], attribNames[i]);
		}
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public boolean isError() {
		return errord;
	}
	
	public String getErrorMessage() {
		return lastError;
	}
	
	private void setError(String message) {
		lastError = message;
		errord = true;
	}
}
