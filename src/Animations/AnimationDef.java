package Animations;

import Animations.FrameDef;

public class AnimationDef {
	String name;
	FrameDef[] frames;

	public AnimationDef(String name, FrameDef[] frames) {
		this.name = name;
		this.frames = frames;
	}

	public FrameDef getFrames(int x) {
		return frames[x];
	}

	public String getName() {
		return this.name;
	}

	public void setFrames(FrameDef[] frames) {
		this.frames = frames;
	}

	public int getNumFrames() {
		return frames.length;
	}
}