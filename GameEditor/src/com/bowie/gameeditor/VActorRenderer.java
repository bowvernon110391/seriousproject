package com.bowie.gameeditor;

import com.bowie.gameeditor.Mesh.MeshGroup;
import com.jogamp.opengl.GL2;

public class VActorRenderer extends BaseView {
	private MActorState ref;
	private Mesh mesh;
	private TextureList texture;
	private Matrix4 renderMatrix;
	
	public VActorRenderer(MActorState refData, Mesh m, TextureList t) {
		ref = refData;
		mesh = m;
		texture = t;
		renderMatrix = new Matrix4();
	}
	
	@Override
	public void preRender(GL2 gl, float dt) {
		AnimStateManager animState = ref.getAnimState();
		// calculate render pose
		if (animState != null)
			animState.preRender(dt);
		// grab render matrix from actor physics data
		ref.calculateRenderMatrix(dt, renderMatrix);
	}
	
	// here we simply draw shit
	@Override
	public void render(GL2 gl, float dt) {		
		gl.glPushMatrix();
			gl.glMultMatrixf(renderMatrix.m, 0);
			
			ref.debugDraw(gl, dt);
			
			if (mesh != null) {
				// if has material, bind
				
				AnimStateManager animState = ref.getAnimState();
				
				// skin it
				if (animState != null) {
					SkPose p = animState.getRenderPose(dt);
					p.CPUSkin(gl, mesh);
				}
				
				// bind texture too
				if (texture != null) {
					gl.glEnable(GL2.GL_TEXTURE_2D);
					gl.glColor3f(1, 1, 1);
				}
				
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, mesh.tmpVBO);
				
				gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
				
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, mesh.bufferObjs[0]);
				
//				gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
//				gl.glColorPointer(3, GL2.GL_FLOAT, mesh.vertSizeInBytes, Mesh.OFFSET_NORMAL);
				
				gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
				gl.glTexCoordPointer(2, GL2.GL_FLOAT, mesh.vertSizeInBytes, Mesh.OFFSET_UV);
				
				gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, mesh.bufferObjs[1]);
				
				int i=0;
				for (MeshGroup g : mesh.groups) {
					if (texture != null) {
						texture.textures[i++].getTexture().bind(gl);
						// enable blending for the last
						if (i == 5) {
//							gl.glDepthMask(false);
							gl.glEnable(GL2.GL_BLEND);
						}
					}
					gl.glDrawElements(GL2.GL_TRIANGLES, g.indexCount, GL2.GL_UNSIGNED_SHORT, g.indexStart * 2);
					
					if (i == 5) {
						gl.glDisable(GL2.GL_BLEND);
					}
				}
				
				gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
//				gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
				gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
				
				gl.glDisable(GL2.GL_TEXTURE_2D);
			}
		gl.glPopMatrix();
	}
}
