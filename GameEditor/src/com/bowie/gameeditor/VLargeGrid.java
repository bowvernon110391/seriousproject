package com.bowie.gameeditor;

import com.jogamp.opengl.GL2;

public class VLargeGrid extends BaseView {
	private float size = 10.0f;
	private float texScale = 10.0f;
	private Vector3 pos = new Vector3();
	private Texture2D tex;
	
	public VLargeGrid(float s, float texScale, Texture2D texture) {
		size = s;
		this.texScale = texScale;
		this.tex = texture;
	}
	
	
	
	@Override
	public void render(GL2 gl, float dt) {
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		if (tex != null)
			tex.getTexture().bind(gl);
		
		gl.glBegin(GL2.GL_QUADS);
			gl.glColor3f(1, 0.92f, 0.94f);
			gl.glTexCoord2f(0, texScale);
			gl.glVertex3f(-size, 0, -size);
			
			gl.glColor3f(0.81f, 0.92f, 0.94f);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(-size, 0,  size);
			
			gl.glColor3f(0.71f, 0.92f, 0.94f);
			gl.glTexCoord2f(texScale, 0);
			gl.glVertex3f( size, 0,  size);
			
			gl.glColor3f( 0.990f, 0.92f, 0.974f);
			gl.glTexCoord2f(texScale, texScale);
			gl.glVertex3f( size, 0, -size);
		gl.glEnd();
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}
}
