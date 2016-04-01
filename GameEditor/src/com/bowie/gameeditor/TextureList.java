package com.bowie.gameeditor;

import java.util.List;

public class TextureList {
	public Texture2D [] textures;	// list of textures
	
	public TextureList(Mesh m) {
		// should generate list of texture here
		initialize(m);
	}
	
	public void initialize(Mesh m) {
		textures = new Texture2D[m.groups.size()];
	}
}
