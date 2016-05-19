package Animations;

public class FrameDef {
	int image;
	float frameTimeSecs;

	public FrameDef(int img, float frameTS) {
		this.image = img;
		this.frameTimeSecs = frameTS;
	}

	public int getFrame() {
		return image;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public float getFrameTimeSecs() {
		return frameTimeSecs;
	}

	public void setFrameTimeSecs(float frameTimeSecs) {
		this.frameTimeSecs = frameTimeSecs;
	}
}
