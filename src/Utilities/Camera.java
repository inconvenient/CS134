package Utilities;

public class Camera {

	public int x;
	public int y;
	public AABB cameraAABB;

	public Camera(int x, int y) {
		this.x = x;
		this.y = y;
		cameraAABB = new AABB(x, y, 1280, 1024);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
		this.cameraAABB.setX(x);
	}

	public void setY(int y) {
		this.y = y;
		this.cameraAABB.setY(y);
	}
	
	public AABB getAABB() {
		return this.cameraAABB;
	}
}
