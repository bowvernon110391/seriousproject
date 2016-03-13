attribute vec3 pos;
attribute vec3 col;

uniform mat4 matprojview;
uniform mat4 matmodel;

varying vec3 color;

void main() {
	color = col;
	gl_Position = matprojview * (matmodel * vec4(pos, 1.0) );	
}
