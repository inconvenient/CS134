package Background;

public class Tile {

	int image;
	boolean collision;

	public Tile(int image, boolean c) {
		this.image = image;
		this.collision = c;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public boolean isCollision() {
		return collision;
	}

	public void setCollision(boolean collision) {
		this.collision = collision;
	}
}
