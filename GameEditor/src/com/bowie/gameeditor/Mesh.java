package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

public class Mesh {
	//=================SHARED================================
	public static int [] vertexElemCount = {8, 12};
	
	//=================REAL DATA=============================
	private int [] vbo = {-1};			//the vertex buffer object
	private int [] ibo = {-1};			//the index buffer object
	private int [] indexSize;
	private int [] indexByteOffset;		//the byte offset
	
	//=================REFERENCE=============================
	private short vertexFormat = 0;
	private short vertexCount = 0;
	private int elemPerVertex = 8;
	private int bytesPerVertex = 32;
	private String [] meshMaterialName;
	
	public boolean hasData = false;

	public Mesh(String filename, GL2 context) {
		byte [] b = Helper.getBytesFromFile(filename);
		loadFromBytes(b, context);
	}
	
	public Mesh(byte [] b, GL2 context) {
		loadFromBytes(b, context);
	}
	
	public Mesh() {
	}

	private boolean loadFromBytes(byte [] b, GL2 context) {
		//temporary data
		ByteBuffer vertexData;
		
		short meshCount = 0;
		List<ShortBuffer> meshIndices = new ArrayList<>();
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
		
		for (int i=0; i<vCount; i++) {
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
		//allocate offset
		indexSize = new int[meshCount];
		indexByteOffset = new int[meshCount];
		
		List<Short> tmpIndexBuffer = new ArrayList<>();
		
		int totalIndices = 0;	//for tracking and globbing index data
		
		byte [] tmpBuf = new byte[32];
		Charset utf8 = Charset.forName("UTF-8");
		
		for (int i=0; i<meshCount; i++) {
			//1st, read material name
			buffer.get(tmpBuf, 0, 32);
			meshMaterialName[i] = new String(tmpBuf, utf8);
			
			//2nd, read index count
			int indexCount = buffer.getShort();
			
			//set byte offset and count
			indexSize[i] = indexCount;
			indexByteOffset[i] = totalIndices * 2;
			totalIndices += indexCount;
			
			//3rd, allocate indices and read em
//			ByteBuffer bb = ByteBuffer.allocateDirect(2 * indexCount).order(ByteOrder.nativeOrder());
//			ShortBuffer sb = bb.asShortBuffer(); 
//			/*meshIndices[i] = new short[meshIndexCount[i]];*/
//			
//			for (int j=0; j<indexCount; j++) {
//				sb.put(buffer.getShort());
//			}
//			//flip and store
//			sb.flip();
//			meshIndices.add(sb);
			
			for (int j=0; j<indexCount; j++) {
				tmpIndexBuffer.add(buffer.getShort());
			}
		}
		
		//join all indices into one
		ByteBuffer indexData = ByteBuffer.allocateDirect(2 * tmpIndexBuffer.size()).order(ByteOrder.nativeOrder());
		indexData.clear();
		
		//copy data
		short [] tmpBuffer = new short[tmpIndexBuffer.size()];
		for(int i=0; i<tmpIndexBuffer.size(); i++) {
			tmpBuffer[i] = tmpIndexBuffer.get(i);
		}
		
		indexData.asShortBuffer().put(tmpBuffer);
		indexData.clear();
		
		hasData =  createVertexBuffer(context, vertexData, indexData.asShortBuffer());
		return hasData;
	}
	
//	public int lastErr = GL2.GL_NO_ERROR;
	
	private boolean createVertexBuffer(GL2 gl, ByteBuffer vertexData, ShortBuffer indexData) {
		System.out.println("Creating vb: " + vertexData.capacity() + " , " + indexData.capacity());
		//check for sanity
		if (vertexData.limit() == 0 || indexData.limit() == 0) {
			return false;
		}

		//VBO
		int [] tmp = {-1};
		gl.glGenBuffers(1, tmp, 0);
		
		gl.glGenBuffers(1, vbo, 0);
		System.out.println("Generated VBO: " + vbo[0]);
		/*if (!gl.glIsBuffer(vbo[0])) {
			return false;
		}*/
		//check
		/*if ( (lastErr=gl.glGetError()) != GL2.GL_NO_ERROR) {
			return false;
		}*/
		//now supply data as static
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertexData.limit(), vertexData, GL2.GL_STATIC_DRAW);
		
		//IBO
		gl.glGenBuffers(1, ibo, 0);
		System.out.println("Generated IBO: " + ibo[0]);
		/*if (!gl.glIsBuffer(ibo[0])) {
			return false;
		}*/
		//check
		/*if ( (lastErr=gl.glGetError()) != GL2.GL_NO_ERROR) {
			return false;
		}*/
		//now supply data as static
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexData.limit() * 2, indexData, GL2.GL_STATIC_DRAW);
		
		/*//now for the index buffer
		for (int i=0; i<indexData.size(); i++) {
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
			gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexData.get(i).limit()*2, indexData.get(i), GL2.GL_STATIC_DRAW);
			indexCount[i] = indexData.get(i).limit();
		}*/
		
		return true;
	}
	
	//this shit draw itself
	public void renderSimple(GL2 gl) {
		
		//teh position
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
		
//		gl.glVertexPointer(3, GL2.GL_FLOAT, bytesPerVertex, vertexData);
		gl.glVertexPointer(3, GL2.GL_FLOAT, bytesPerVertex, 0);
		//teh normal
//		vertexData.position(12);
		gl.glNormalPointer(GL2.GL_FLOAT, bytesPerVertex, 12);
		//the texcoord
//		vertexData.position(24);
		gl.glTexCoordPointer(2, GL2.GL_FLOAT, bytesPerVertex, 24);
		
		//now for the index
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
		for (int i=0; i<indexSize.length; i++) {
			gl.glDrawElements(GL2.GL_TRIANGLES, indexSize[i], GL2.GL_UNSIGNED_SHORT, indexByteOffset[i]);
		}
		/*for (ShortBuffer sb : meshIndices) {
			gl.glDrawElements(GL2.GL_TRIANGLES, sb.limit(), GL2.GL_UNSIGNED_SHORT, sb);
		}*/
		
		
	}
	
	public int getVBO() {
		return vbo[0];
	}
	
	public int getIBO() {
		return ibo[0];
	}
	
	 static public Mesh buildSimpleCube(GL2 gl, float [] vertex, short [] indices) {
		 Mesh m = new Mesh();
		 
		 ByteBuffer vData = ByteBuffer.allocateDirect(4 * vertex.length).order(ByteOrder.nativeOrder());
		 vData.asFloatBuffer().put(vertex);
		 vData.clear();
		 
		 ShortBuffer iData = ShortBuffer.wrap(indices);
		 iData.clear();
		 
		 m.createVertexBuffer(gl, vData, iData);
		 
		 //set auxiliary data
		 m.indexSize = new int []{0};
		 m.indexByteOffset = new int []{0};
		 
		 return m;
	 }
	 
	 @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("vbo: " + vbo[0] + " , " + "ibo: " + ibo[0] + " meshCount: " + indexSize.length + "\r\n ");
		for (int i=0; i<indexSize.length; i++) {
			sb.append(" " + i + ": indexCount: " + indexSize[i] + " , offset(bytes): " + indexByteOffset[i] + "\r\n");
		}
		
//		sb.append("lastError: " + lastErr);
		
		return sb.toString();
	}
}
