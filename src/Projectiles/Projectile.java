package Projectiles;

import Utilities.AABB;

public class Projectile {

	private int x;
	private int y;
	private int speed;
	private int damage;
	private boolean visible;
	private final int projectileRange = 750;
	private int distanceTraveled = 0;
	private AABB projHitBox;

	public Projectile(int startX, int startY) {
		this.x = startX;
		this.y = startY;
		this.speed = 7;
		this.damage = 25;
		this.visible = true;
		projHitBox = new AABB(startX, startY, 50, 8);
	}

	public int getDistanceTraveled() {
		return distanceTraveled;
	}

	public void setDistanceTraveled(int distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}

	public AABB getProjHitBox() {
		return projHitBox;
	}

	public void setProjHitBox(AABB projHitBox) {
		this.projHitBox = projHitBox;
	}

	public int getProjectileRange() {
		return projectileRange;
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

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void update() {
		x += getSpeed();
		distanceTraveled += getSpeed();
		projHitBox.addToXCoord(getSpeed());
		if (distanceTraveled > projectileRange) {
			visible = false;
		}
	}
}
