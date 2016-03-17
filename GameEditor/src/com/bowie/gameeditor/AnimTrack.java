package com.bowie.gameeditor;

public class AnimTrack {
	
	public int curTrack = 0;			// current track or action
	public float curTime = 0.0f;	// current track time
	public float renderTime = 0.0f;	// the time at render
	public float minTime = 0.0f;
	public float maxTime = 1.0f;
	
	public float fps = 24.0f;	// standard	
	
	public static final int PLAY_LOOP = 0;
	public static final int PLAY_CLAMP = 1;
	
	public int playMode = PLAY_LOOP;
	
	public void setTrack(int trackId) {
		curTrack = trackId;
	}
	
	public void setPlayMode(int mode) {
		playMode = mode;
	}
	
	public void setTrackTime(float cur) {
		curTime = cur;
	}
	
	public void setFPS(float fps_) {
		fps = fps_;
	}
	
	public void setTrackTimeSet(float min, float max) {
		minTime = min;
		maxTime = max;
	}
	
	public void prepRender(float dt) {
		renderTime = curTime + fps * dt;
		renderTime = correctTime(renderTime);
	}
	
	public void update(float dt) {
		curTime += fps * dt;
		curTime = correctTime(curTime);
	}
	
	private float correctTime(float time) {
		// must clamp or otherwise wrap around
		switch (playMode) {
		case PLAY_LOOP:
			// wrap around. both ways I says
			float delta = maxTime - minTime;
			if (time > maxTime) {
				while (time > maxTime)
					time -= delta;
			}
			if (time < minTime) {
				while (time < minTime)
					time += delta;
			}
			break;
		case PLAY_CLAMP:
			time = time > maxTime ? maxTime : time < minTime ? minTime : time;
			break;
		}
		return time;
	}
}
