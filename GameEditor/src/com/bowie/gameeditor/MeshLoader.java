package com.bowie.gameeditor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.bowie.gameeditor.Mesh.MeshGroup;
import com.bowie.gameeditor.Mesh.MeshVertex_v2;

/**
 * @author Bow
 * this thing here will load our
 * specialized mesh file format
 */
public class MeshLoader {
	
	public Mesh loadMesh(String filename) {
		return loadFromBytes(Helper.getBytesFromFile(filename));
	}

	/**
	 * @param data
	 * @return
	 */
	public Mesh loadFromBytes(byte [] data) {
		if (data == null) {
			System.out.println("Mesh: null data");
			return null;
		}
		
		if (data.length < 4) {
			System.out.println("Mesh: not enough data");
			return null;
		}
		
		// good to go
		// our file is lil endian
		ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		
		// only support version 1, and if the vertex format version is [0..1]
		byte version = bb.get();
		byte vformatId = bb.get();
		
		if (version != 1) {
			System.out.println("Mesh: Version unsupported");
			return null;
		}
		
		if (vformatId > 1) {
			System.out.println("Mesh: VFormat unsupported: " + vformatId);
			return null;
		}
		
		// next comes vcount, icount, gcount (2 bytes each)
		int vertCount = bb.getShort();
		int idxCount = bb.getShort();
		int grpCount = bb.getShort();
		
		// safe to spawn mesh
		Mesh m = new Mesh();
		
		m.allocateVBuffer(vertCount, vformatId);
		
		// next vertex data, read em all
		for (int i=0; i<vertCount; i++) {
			m.vertices[i].pos[0] = bb.getFloat();
			m.vertices[i].pos[1] = bb.getFloat();
			m.vertices[i].pos[2] = bb.getFloat();
			
			m.vertices[i].normal[0] = bb.getFloat();
			m.vertices[i].normal[1] = bb.getFloat();
			m.vertices[i].normal[2] = bb.getFloat();
			
			m.vertices[i].uv[0] = bb.getFloat();
			m.vertices[i].uv[1] = bb.getFloat();
			
			// next comes special data if it's version 1
			if (vformatId == 1) {
				// cast it
				MeshVertex_v2 v2 = (MeshVertex_v2) m.vertices[i];
				v2.bone_ids[0] = bb.get();
				v2.bone_ids[1] = bb.get();
				v2.bone_ids[2] = bb.get();
				v2.bone_ids[3] = bb.get();
				
				v2.bone_ws[0] = bb.getFloat();
				v2.bone_ws[1] = bb.getFloat();
				v2.bone_ws[2] = bb.getFloat();
				v2.bone_ws[3] = bb.getFloat();
			}
		}
		
		// now comes group data
		byte [] grpName = new byte[32];
		for (int i=0; i<grpCount; i++) {
			// first there's group name -> 32 bytes
			bb.get(grpName, 0, 32);
			
			String groupName = new String(grpName, Charset.forName("UTF-8")).trim();
			short groupIdxStart = bb.getShort();
			short groupIdxCount = bb.getShort();
			
			m.addGroup(groupName, groupIdxStart, groupIdxCount);
		}
		
		// last is index data, just read the rest of it
		m.indices = new short[idxCount];
		bb.asShortBuffer().get(m.indices, 0, idxCount);
		
		return m;
	}
}
