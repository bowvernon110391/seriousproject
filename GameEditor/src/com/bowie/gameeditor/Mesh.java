package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

/**
 * @author Bow
 * this thing here holds a mesh data
 * it can hold data, or store it as VBO
 * and then remove it
 * 
 * it has:
 * -vertex array
 * -several mesh group divided per material (to be rendered separately)
 */
public class Mesh {
	/**
	 * @author Bow
	 * This is a single vertex data
	 */
	public class MeshVertex {
		
		public float [] pos = {0,0,0};
		public float [] normal = {0,0,0};
		public float [] uv = {0,0};
	}
	
	/**
	 * @author Bow
	 * this is extensification of our format.
	 * for weighted mesh
	 */
	public class MeshVertex_v2 extends MeshVertex {
		
		public byte [] bone_ids = {0,0,0,0};
		public float [] bone_ws = {0,0,0,0};
	}
	
	/**
	 * @author Bow
	 * this hold a single mesh group
	 */
	public class MeshGroup {
		String name;
		int indexStart = 0, indexCount = 0;
	}
	
	// this is this mesh vertices
	public MeshVertex [] vertices = null;
	// this is a list of vertex indices
	public short [] indices = null;
	// this holds our mesh groups
	public List<MeshGroup> groups = new ArrayList<>();
	// the vertex format id
	public int vertFormatId = 0;	// default to no weight
	public int vertSizeInBytes = 32;	// default
	// the VBO + IBO
	public int[] bufferObjs = {-1, -1};	// no VBO, no IBO
	// this is this instance's temporary buffer
	// used for weighted mesh to store some temporary
	// calculation (CPU SKINNING)
	public FloatBuffer tmpBuffer = null;
	public int tmpVBO = -1;
	
	
	// return various data offset
	public static final int OFFSET_POS = 0;
	public static final int OFFSET_NORMAL = 12;
	public static final int OFFSET_UV = 24;
	public static final int OFFSET_BONE_IDS = 32;
	public static final int OFFSET_BONE_WS = 36;
	
	public boolean hasVBO() {
		return bufferObjs[0] != -1;
	}
	
	public boolean hasIBO() {
		return bufferObjs[1] != -1;
	}
	
	// return group by its name
	public int getGroupId(String grpName) {
		for (int i=0; i<groups.size(); i++) {
			if (groups.get(i).name.equals(grpName))
				return i;
		}
		return -1;
	}
	
	// return our group
	public MeshGroup getGroup(int id) {
		if (id < 0 || id >= groups.size())
			return null;
		return groups.get(id);
	}
	
	public FloatBuffer getTempBuffer() {
		return tmpBuffer;
	}
	
	// would allocate enough vertices data. 
	// may only be called once, for any previous data is gonna be rewritten 
	public void allocateVBuffer(int size, int version) {
		vertFormatId = version;
		
		if (version == 0) {
			vertSizeInBytes = 32;
			vertices = new MeshVertex[size];
			for (int i=0; i<size; i++)
				vertices[i] = new MeshVertex();
		} else if (version == 1) {
			// weighted
			vertSizeInBytes = 52;
			vertices = new MeshVertex_v2[size];
			for (int i=0; i<size; i++)
				vertices[i] = new MeshVertex_v2();
			
			// also, spawn temporary buffer
			ByteBuffer bb = ByteBuffer.allocateDirect(size * 12).order(ByteOrder.nativeOrder());
			this.tmpBuffer = bb.asFloatBuffer();
			this.tmpBuffer.clear();
		}
	}
	
	public void addGroup(String name, int indexStart, int indexCount) {
		// allocate single group
		MeshGroup grp = new MeshGroup();
		grp.name = new String(name);
		grp.indexStart = indexStart;
		grp.indexCount = indexCount;
		
		// add it
		groups.add(grp);
	}
	
	@Override
	public String toString() {
		String ret = "vcount: " + vertices.length + ", icount: " + indices.length + ", group: " + groups.size() + "\n";
		
		for (int i=0; i<vertices.length; i++) {
			// log vertices data
			MeshVertex_v2 v = (MeshVertex_v2) vertices[i];
			ret += "\t" + i + ": " + v.pos[0] + ", " + v.pos[1] + ", " + v.pos[2] + " | ";
			ret += v.normal[0] + ", " + v.normal[1] + ", " + v.normal[2] + " | ";
			ret += v.uv[0] + ", " + v.uv[1];
			
			if (vertFormatId == 1) {
				// waaiit there's MORE!!
				ret += "| " + v.bone_ids[0] + ", " + v.bone_ids[1] + ", " + v.bone_ids[2] + ", " + v.bone_ids[3];
				ret += "| " + v.bone_ws[0] + ", " + v.bone_ws[1] + ", " + v.bone_ws[2] + ", " + v.bone_ws[3];
			}
			ret += "\n";
		}
		ret += "\n";
		
		// log group data
		for (MeshGroup g : groups) {
			ret += g.name + " : " + g.indexStart + " ~ " + g.indexCount + "\n";
		}
		
		// log indices
		for (int i=0; i<indices.length; i++) {
			ret += i + " : " +  indices[i] + " \n";
		}
		
		return ret;
	}
	
	private ByteBuffer buildVBuffer() {
		int vertSize = 32;	// v0
		if (vertFormatId == 1)
			vertSize = 52;	// v1 (with weights)
		
		// allocate bytes
		ByteBuffer bb = ByteBuffer.allocateDirect(vertSize * vertices.length).order(ByteOrder.nativeOrder());
		bb.clear();
		
		// fill em
		for (int i=0; i<vertices.length; i++) {
			// safe to upper_cast, with check
			MeshVertex_v2 v = (MeshVertex_v2) vertices[i];
			
			bb.putFloat(v.pos[0]);
			bb.putFloat(v.pos[1]);
			bb.putFloat(v.pos[2]);
			
			bb.putFloat(v.normal[0]);
			bb.putFloat(v.normal[1]);
			bb.putFloat(v.normal[2]);
			
			bb.putFloat(v.uv[0]);
			bb.putFloat(v.uv[1]);
			
			// version 1
			if (vertFormatId == 1) {
				bb.put(v.bone_ids[0]);
				bb.put(v.bone_ids[1]);
				bb.put(v.bone_ids[2]);
				bb.put(v.bone_ids[3]);
				
				bb.putFloat(v.bone_ws[0]);
				bb.putFloat(v.bone_ws[1]);
				bb.putFloat(v.bone_ws[2]);
				bb.putFloat(v.bone_ws[3]);
			}
		}
		
		// flip em
		bb.flip();
		return bb;
	}
	
	private ShortBuffer buildIBuffer() {
		ByteBuffer bb = ByteBuffer.allocateDirect(2 * indices.length).order(ByteOrder.nativeOrder());
		ShortBuffer sb = bb.asShortBuffer();
		
		sb.clear();
		sb.put(indices);
		sb.flip();
		
		return sb;
	}
	
	public boolean buildBufferObjects(GL2 gl) {
		// must already have data
		if (vertices.length < 3)
			return false;
		if (indices.length < 3)
			return false;
		// good to go
		gl.glGenBuffers(2, bufferObjs, 0);
		
		// also, request one for temp buffer
		if (tmpBuffer != null) {
			int [] id = {-1};
			gl.glGenBuffers(1, id, 0);
			tmpVBO = id[0];
		}
		
		// build VBO first
		ByteBuffer vBuffer = this.buildVBuffer();
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferObjs[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, vBuffer.limit(), vBuffer, GL2.GL_STATIC_DRAW);
		
		// next, IBO
		ShortBuffer iBuffer = this.buildIBuffer();
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferObjs[1]);
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, iBuffer.limit() * 2, iBuffer, GL2.GL_STATIC_DRAW);
				
		return true;
	}
	
	public void freeData() {
		vertices = null;
		indices = null;
	}
}
