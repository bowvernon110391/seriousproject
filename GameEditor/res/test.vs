attribute vec3 pos;
attribute vec3 col;

uniform mat4 matprojview;
uniform mat4 matmodel;

varying vec3 color;
varying vec3 posMult;

void main() {
	color = col;
	posMult = pos * 0.5 + 0.5;
	gl_Position = matprojview * matmodel * vec4(pos, 1.0);	
}
