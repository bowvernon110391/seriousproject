package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.common.nio.ByteBufferInputStream;
import com.jogamp.opengl.GL2;

public class MeshFile {
	
	private short vertexFormat = 0;
	private short vertexCount = 0;
	private int elemPerVertex = 8;
	private int bytesPerVertex = 32;
//	private float [] vertexData;
	private ByteBuffer vertexData;
	
	private short meshCount = 0;
	private String [] meshMaterialName;
	private List<ShortBuffer> meshIndices = new ArrayList<>();
	
	public static int [] vertexElemCount = {8, 12};

	public MeshFile(String filename) {
		byte [] b = Helper.getBytesFromFile(filename);
		loadFromBytes(b);
	}
	
	public MeshFile(byte [] b) {
		loadFromBytes(b);
	}
	
	@Override
	public String toString() {
		//generate string containing information
		StringBuilder sb = new StringBuilder();
		
		sb.append("vertexCount: " + vertexCount + " , in bytes: " + vertexData.limit() + "\r\n");
		
		for (int c=0; c<vertexCount; c++) {
			sb.append("v" + c + ": ");
			
			sb.append(vertexData.getFloat() + ", ");
			sb.append(vertexData.getFloat() + ", ");
			sb.append(vertexData.getFloat() + " | ");
			
			sb.append(vertexData.getFloat() + ", ");
			sb.append(vertexData.getFloat() + ", ");
			sb.append(vertexData.getFloat() + " | ");
			
			sb.append(vertexData.getFloat() + ", ");
			sb.append(vertexData.getFloat() + " | ");
			
			if (elemPerVertex > 8) {
				
			} else {
				sb.append("\r\n");
			}
		}
		vertexData.flip();
		
		sb.append("meshCount: " + meshIndices.size() + "\r\n");
		
		int i=0;
		for (ShortBuffer ib : meshIndices) {
			sb.append("mesh " + i++ + ": " + ib.limit());
			for (int c=0; c<ib.limit()/3; c++) {
				sb.append(" " + c + ": " + ib.get() + ", " + ib.get() + ", " + ib.get() + "\r\n");
			}
			ib.flip();
		}
		
		return sb.toString();
	}
	
	private boolean loadFromBytes(byte [] b) {
//		System.out.println("Reading bytes..." + b.length);
		
		if (b.length <= 0)
			return false;
		
		//read from bytes by default
		ByteBuffer buffer = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		
//		System.out.println("Position at: " + buffer.position() + "remaining: " + buffer.remaining());
		
		//first, read magic number
		short magicNumber = buffer.getShort();	//11391
		
		if (magicNumber != 11391)
			return false;
		
		vertexFormat = buffer.getShort();	//0 (non weighted) or 1 (weighted)
		vertexCount = buffer.getShort();	//unsigned though!!
		meshCount = buffer.getShort();	//unsigned too, but don't worry
		
//		System.out.println("magicnumber: " + magicNumber + " vertexFormat: " + vertexFormat);
		
		//for looping (avoiding negative shit)
		//because the value wrap around
		int vCount = Helper.convertUShort(vertexCount);
		
		//how many float per vertex?
		// 0 = x y z nx ny nz u v (32 bytes)
		// 1 = x y z nx ny nz u v bi0 bw0 bi1 bw1 bi2 bw2 bi3 bw3 (52 bytes)
		int numElems = vertexFormat == 0 ? 8 : vertexFormat == 1 ? 12 : 8;
		int bytesPerElem = vertexFormat == 0 ? 32 : vertexFormat == 1 ? 52 : 32;
		
		int vertexDataSize = bytesPerElem * vertexCount;
		
		elemPerVertex = numElems;
		bytesPerVertex = bytesPerElem;
		
		vertexData = ByteBuffer.allocateDirect(vertexDataSize).order(ByteOrder.nativeOrder());
		vertexData.clear();
		
//		System.out.println("float count: " + numElems * vertexCount);
		
		for (int i=0; i<vertexCount; i++) {
			vertexData.putFloat( buffer.getFloat() );	//x
			vertexData.putFloat( buffer.getFloat() );	//y
			vertexData.putFloat( buffer.getFloat() );	//z
			
			vertexData.putFloat( buffer.getFloat() );	//nx
			vertexData.putFloat( buffer.getFloat() );	//ny
			vertexData.putFloat( buffer.getFloat() );	//nz
			
			vertexData.putFloat( buffer.getFloat() );	//u
			vertexData.putFloat( buffer.getFloat() );	//v
			
			if (numElems > 8) {
				// read bone data here....
			}
		}
		//done, flip it
		vertexData.flip();
		
		//now read meshes
		//allocate all data
		meshMaterialName = new String[meshCount];
		
		byte [] tmpBuf = new byte[32];
		Charset utf8 = Charset.forName("UTF-8");
		
		for (int i=0; i<meshCount; i++) {
			//1st, read material name
			buffer.get(tmpBuf, 0, 32);
			meshMaterialName[i] = new String(tmpBuf, utf8);
			
			//2nd, read index count
			int indexCount = buffer.getShort();
			
			//3rd, allocate indices and read em
			ByteBuffer bb = ByteBuffer.allocateDirect(2 * indexCount).order(ByteOrder.nativeOrder());
			ShortBuffer sb = bb.asShortBuffer(); 
			/*meshIndices[i] = new short[meshIndexCount[i]];*/
			
			for (int j=0; j<indexCount; j++) {
				sb.put(buffer.getShort());
			}
			//flip and store
			sb.flip();
			meshIndices.add(sb);
		}
		
		return true;
	}
	
	//this shit draw itself
	public void renderSimple(GL2 gl) {
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		//teh position
		vertexData.position(0);
		gl.glVertexPointer(3, GL2.GL_FLOAT, bytesPerVertex, vertexData);
		//teh normal
		vertexData.position(12);
		gl.glNormalPointer(GL2.GL_FLOAT, bytesPerVertex, vertexData);
		//the texcoord
		vertexData.position(24);
		gl.glTexCoordPointer(2, GL2.GL_FLOAT, bytesPerVertex, vertexData);
		
		//now for the index
		for (ShortBuffer sb : meshIndices) {
			gl.glDrawElements(GL2.GL_TRIANGLES, sb.limit(), GL2.GL_UNSIGNED_SHORT, sb);
		}
		
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
}
