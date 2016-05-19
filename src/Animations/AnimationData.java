package Animations;

public class AnimationData {
	AnimationDef def;
	int curFrame;
	float secsUntilNextFrame;

	public AnimationData(AnimationDef def) {
		this.def = def;
		curFrame = 1;
		secsUntilNextFrame = def.getFrames(curFrame).frameTimeSecs;
	}

	public void update(float deltaTime) {
		secsUntilNextFrame -= deltaTime;
		if (curFrame >= def.getNumFrames()) {
			curFrame = 1;
		}
		if (secsUntilNextFrame <= 0) {
			curFrame++;
			secsUntilNextFrame = def.getFrames(curFrame - 1).frameTimeSecs;
		}
	}

	public int getCurFrame() {
		return def.getFrames(curFrame - 1).getFrame();
	}
}