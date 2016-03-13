package com.bowie.gameeditor;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameDataFiles {
	private ZipFile masterFile = null;
	
	//random hash for all files
	private HashMap<String, ZipEntry> allData = new HashMap<>();
	
	//hash for all files enumerated
	private HashMap<String, ZipEntry> meshData = new HashMap<>();		//mesh data
	private HashMap<String, ZipEntry> textureData = new HashMap<>(); 	//texture data
	private HashMap<String, ZipEntry> objectData = new HashMap<>();		//object definition data
	
	static public final int TYPE_ALL = -1;
	static public final int TYPE_MESH = 0;
	static public final int TYPE_TEXTURE = 1;
	static public final int TYPE_OBJECT = 2;
	
	public GameDataFiles() {		
	}
	
	public GameDataFiles(String filename) {
		//read em
		loadGDF(filename);
	}
	
	public HashMap<String, ZipEntry> getFileMap(int type) {
		switch (type) {
		case TYPE_ALL:
			return allData;
		case TYPE_MESH:
			return meshData;
		case TYPE_TEXTURE:
			return textureData;
		case TYPE_OBJECT:
			return objectData;
		}
		return null;
	}
	
	public void loadGDF(String filename) {
		try {
			/*InputStream gdf = new FileInputStream(filename);
			ZipInputStream stream = new ZipInputStream(gdf);
			
			ZipEntry file;
			
			while ( (file = stream.getNextEntry()) != null) {
				//skip directory
				if (file.isDirectory())
					continue;
				
				filenames.add(file.getName());
				filesizes.add(file.getSize());
			}
			
			stream.close();*/
			
			masterFile = new ZipFile(filename);
			
			Enumeration<? extends ZipEntry> e = masterFile.entries();
			
			ZipEntry file;
			
			while (e.hasMoreElements()) {
				file = e.nextElement();
				
				//slash name
				if (!file.isDirectory()) {
					allData.put(file.getName().trim(), file);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isOpen() {
		return masterFile != null;
	}
	
	public byte [] getBytes(String filename) {
		filename = filename.trim();
		//can't do anything
		if (!isOpen()) {
			System.out.println("GDF: Well there's no master file opened");
			return null;
		}
		
//		//let's do extreme compare
//		System.out.println("GDF: do we have: " + filename + "? " + allData.containsKey(filename));
//		
//		Iterator<Entry<String, ZipEntry>> it = allData.entrySet().iterator();
//		
//		while (it.hasNext()) {
//			Map.Entry<?, ?> pair = it.next();
//			
//			System.out.println("GDF: '" + pair.getKey() + "' vs '" + filename + "' ? " + pair.getKey().equals(filename));
//			
//			it.remove();
//		}
		
		//let's see if we can find such files
		ZipEntry file = allData.get(filename);
		//we can't do shit then
		if (file == null) {
			System.out.println("GDF: cannot find such file: " + filename);
			return null;
		}
		//found, do the real thing
		try {
			InputStream is = masterFile.getInputStream(file);
			DataInputStream dis = new DataInputStream(is);
			//allocate enough bytes
			System.out.println("GDF: allocating for : " + filename + " for " + file.getSize() + " bytes");
			byte [] buffer = new byte[(int) file.getSize()];
			//can we do shit?
			if (buffer == null)
				throw new IOException("Sheeeiit we run out of memory!!");
			//yes we can
			dis.readFully(buffer);
			dis.close();
			
			//yaay we did it
			return buffer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//default shit
		return null;
	}
	
	public void close() {
		if (masterFile != null) {
			try {
				masterFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
