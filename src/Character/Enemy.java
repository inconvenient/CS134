package Character;

import java.util.ArrayList;

import java.util.Random;

import Animations.AnimationData;
import Animations.AnimationDef;
import Animations.FrameDef;
import Projectiles.EnemyProjectiles;
import Utilities.AABB;

public class Enemy {

	private int x;
	private int y;
	private int health;
	boolean visible;
	private int moveSpeed = -1;
	private int initialStart;

	public AABB hitbox;
	private ArrayList<EnemyProjectiles> projectiles = new ArrayList<EnemyProjectiles>();
	private ArrayList<EnemyProjectiles> missiles = new ArrayList<EnemyProjectiles>();

	public float dtAggregate = 0;
	boolean patrolLeft = true;
	public boolean isLeft = true;
	public boolean animateDirection = true;
	public boolean isAggroed = false;

	// --------- ENEMY ANIMATIONS
	FrameDef[] runRight;
	AnimationDef runRightDef;
	AnimationData runRightData;
	FrameDef[] runLeft;
	AnimationDef runLeftDef;
	AnimationData runLeftData;

	public Enemy(int x, int y, FrameDef[] runRight, AnimationDef rrDef, AnimationData rrData, FrameDef[] runLeft,
			AnimationDef rlDef, AnimationData rlData) {
		this.x = x;
		this.y = y;
		this.health = 10;
		visible = true;
		hitbox = new AABB(x, y, 75, 115);
		initialStart = x;
		this.runRight = runRight;
		this.runRightDef = rrDef;
		this.runRightData = rrData;
		this.runLeft = runLeft;
		this.runLeftDef = rlDef;
		this.runLeftData = rlData;

	}

	public AnimationData getRunLeftData() {
		return this.runLeftData;
	}

	public AnimationData getRunRightData() {
		return this.runRightData;
	}

	public ArrayList<EnemyProjectiles> getProjectiles() {
		return projectiles;
	}

	public void addProjectile(EnemyProjectiles p) {
		projectiles.add(p);
	}

	public ArrayList<EnemyProjectiles> getMissiles() {
		return missiles;
	}

	public void addMissiles(EnemyProjectiles p) {
		missiles.add(p);
	}

	public AABB getHitbox() {
		return this.hitbox;
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

	public void dealDmgTo(int x) {
		this.health -= x;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	// dt is in milliseconds
	// returns if enemy is facing left
	public boolean update(float dt, int playerX, int playerY) {
		dtAggregate += dt;

		if (this.health <= 0) {
			visible = false;
		} else {
			// patrol logic
			if (this.x - playerX > 450 && !isAggroed) {
				// true if enemy is facing left, else false
				animateDirection = patrol();
			} else {
				isLeft = true;
				isAggroed = true;
				animateDirection = true;
				this.x += moveSpeed;
				hitbox.addToXCoord(moveSpeed);
				// enemy should only shoot once in a while
				if (dtAggregate > 1400) {
					Random r = new Random();
					int min = 1;
					int max = 100;
					int result = r.nextInt(max - min) + min;
					if (result < 30) {
						shootMissile();
						dtAggregate = 0;
					} else {
						shootLaser();
						dtAggregate = 0;
					}
				}
			}
		}
		return animateDirection;
	}

	public boolean patrol() {
		int maxLeft = initialStart - 75;
		int maxRight = initialStart + 75;
		if (this.x != maxLeft && patrolLeft) {
			this.x += moveSpeed;
			hitbox.addToXCoord(moveSpeed);
			isLeft = true;
			if (this.x == maxLeft) {
				patrolLeft = false;
				isLeft = false;
			}
			return isLeft;
		} else {
			this.x -= moveSpeed;
			hitbox.addToXCoord(-moveSpeed);
			isLeft = false;
			if (this.x == maxRight) {
				patrolLeft = true;
				isLeft = true;
			}
			return isLeft;
		}
	}

	public void shootLaser() {
		EnemyProjectiles p = new EnemyProjectiles(this.x, this.y + 35);
		addProjectile(p);
	}

	public void shootMissile() {
		EnemyProjectiles p = new EnemyProjectiles(this.x, this.y + 35);
		addMissiles(p);
	}
}
