varying vec3 color;
varying vec3 posMult;

void main() {		
	gl_FragColor = vec4(posMult + color, 1.0);
}
