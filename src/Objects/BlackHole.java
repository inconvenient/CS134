package Objects;

import Animations.AnimationData;
import Animations.AnimationDef;
import Animations.FrameDef;
import Utilities.AABB;

public class BlackHole {

	// 145x145
	public FrameDef[] blackHole;
	public AnimationDef blackHoleDef;
	public AnimationData blackHoleData;

	private int x;
	private int y;
	private AABB hitbox;
	private int initialStart;
	private int patrolSpeed = 2;
	private boolean patrolDown = true;

	public BlackHole(int x, int y, FrameDef[] blackHole, AnimationDef blackHoleDef, AnimationData blackHoleData) {
		this.blackHole = blackHole;
		this.blackHoleDef = blackHoleDef;
		this.blackHoleData = blackHoleData;
		this.x = x;
		this.y = y;
		this.hitbox = new AABB(x, y, 145, 145);
		this.initialStart = y;
	}

	public FrameDef[] getBlackHole() {
		return blackHole;
	}

	public void setBlackHole(FrameDef[] blackHole) {
		this.blackHole = blackHole;
	}

	public AnimationDef getBlackHoleDef() {
		return blackHoleDef;
	}

	public void setBlackHoleDef(AnimationDef blackHoleDef) {
		this.blackHoleDef = blackHoleDef;
	}

	public AnimationData getBlackHoleData() {
		return blackHoleData;
	}

	public void setBlackHoleData(AnimationData blackHoleData) {
		this.blackHoleData = blackHoleData;
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

	public AABB getHitbox() {
		return hitbox;
	}

	public void setHitbox(AABB hitbox) {
		this.hitbox = hitbox;
	}

	public void patrol() {
		int maxUp = initialStart - 300;
		int maxDown = initialStart + 300;

		if (this.y != maxDown && patrolDown) {
			this.y += patrolSpeed;
			hitbox.addToYCoord(patrolSpeed);
			if (this.y == maxDown) {
				patrolDown = false;
			}
		} else {
			this.y -= patrolSpeed;
			hitbox.addToYCoord(-patrolSpeed);
			if (this.y == maxUp) {
				patrolDown = true;
			}
		}
	}
}
