package Character;

import java.util.ArrayList;
import java.util.List;
import Projectiles.Projectile;
import Utilities.AABB;

public class Player {

	private int x;
	private int y;
	private int health;
	private ArrayList<Projectile> projectiles;
	private AABB hitbox;
	private int gravity = 2;

	public Player(int x, int y) {
		this.x = x;
		this.y = y;
		this.health = 50;
		this.projectiles = new ArrayList<Projectile>();
		hitbox = new AABB(x, y, 80, 95);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		this.hitbox.setX(x);
	}

	public void addToX(int x) {
		this.x += x;
		this.hitbox.addToXCoord(x);
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		this.hitbox.setY(y);
	}

	public void addToY(int y) {
		this.y += y;
		this.hitbox.addToYCoord(y);
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}

	public void shoot() {
		Projectile p = new Projectile(this.x + 75, this.y + 20);
		projectiles.add(p);
	}

	public void takeDamage(int x) {
		this.health -= x;
	}

	public AABB getAABB() {
		return this.hitbox;
	}

	public void falling() {
		this.addToY(gravity);
	}
}
