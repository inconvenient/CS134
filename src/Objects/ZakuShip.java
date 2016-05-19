package Objects;

import Utilities.AABB;

public class ZakuShip {

	private int x;
	private int y;
	private int health;
	public AABB hitbox;

	public ZakuShip(int x, int y) {
		this.x = x;
		this.y = y;
		this.health = 100;
		this.hitbox = new AABB(x, y, 500, 300);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public AABB getHitbox() {
		return hitbox;
	}

	public void setHitbox(AABB hitbox) {
		this.hitbox = hitbox;
	}

}
