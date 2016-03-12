package com.bowie.gameeditor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * @author Bow
 * The texture class. it's the parent class
 * it uses JOGL as helper, so for android please use
 * or create another class. wont be difficult
 */
public class Texture2D {
	
	private Texture texture = null;
	
	public Texture2D (String filename, boolean mipmap) {
		loadTexture(Helper.getBytesFromFile(filename), mipmap);
	}
	
	/**
	 * this create a texture object, with the data is from
	 * a stream of bytes
	 * @param b: the stream of bytes
	 */
	public Texture2D (byte [] b, boolean mipmap) {
		loadTexture(b, mipmap);
	}
	
	/**
	 * this bind the texture
	 */
	public Texture getTexture() {
		return texture;
	}
	
	public boolean loadTexture(byte [] b, boolean mipmap) {		
		try {
			InputStream is = new ByteArrayInputStream(b);
			//let's load			
			texture = TextureIO.newTexture(is, mipmap, null);
			//close
			is.close();
			
			return true;	//yeash!!
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
