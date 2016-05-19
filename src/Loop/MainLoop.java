package Loop;

// --------- LIBRARIES
import com.jogamp.nativewindow.*;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import Character.Enemy;
// PACKAGES
import Character.Player;
import Objects.BlackHole;
import Objects.MinervaShip;
import Objects.ZakuShip;
import Projectiles.EnemyProjectiles;
import Projectiles.Projectile;
import Utilities.Camera;
import Animations.AnimationData;
import Animations.AnimationDef;
import Animations.FrameDef;
import Background.BackgroundDef;
import Utilities.AABB;

public class MainLoop {

	/*
	 * SPRITE/BG INFORMATION SPRITE SIZE: 80W / 95H TILE SIZE: 128W / 128H
	 * Minerva Ship : 500x300 Zaku Ship: 540x260
	 */

	// GAME STATE VARIABLES
	private static boolean shouldExit;
	private static boolean kbPrevState[] = new boolean[256];
	private static boolean kbState[] = new boolean[256];

	// SCREEN RESOLUTION
	private static final int xRes = 1280;
	private static final int yRes = 1024;

	// STATIC IMGS
	static int endScreen;

	// OPTIMIZATION VARIABLES
	private static int startTile;

	// GAME VARIABLES
	private static final int worldSizeX = 100;
	private static final int worldSizeY = 8;
	private static boolean gravity = true;
	private static ArrayList<Projectile> userProjectiles;
	private static ArrayList<Projectile> mainCharProj;
	private static ArrayList<BlackHole> blackHoleList;
	private static long shotTimer = 0;
	private static AABB prjHitBox;
	private static boolean gameOver = false;

	// GAME ENDING VARIABLES
	private static int escapedZaku = 0;
	private static boolean playerDead = false;

	// ENEMY SPRITE
	private static final int enemySpriteW = 75;
	private static final int enemySpriteH = 115;
	private static final int[] enemySpriteSize = { enemySpriteW, enemySpriteH };

	// FINAL SPRITE/TILE SIZE
	private static final int spriteW = 80;
	private static final int spriteH = 95;
	private static final int[] spriteSize = { spriteW, spriteH };
	private static final int[] spriteHoverSize = { 95, 100 };
	private static final int[] spriteShootSize = { 95, 90 };
	private static final int[] blackHoleSize = { 145, 145 };

	// BACKGROUND TEXTURES
	private static int spaceTileTexture;
	private static int floorTileTexture;
	private static final int[] tileSize = new int[2];

	// OTHER TEXTURES
	private static int bombTexture;
	private static int eBombTexture;
	private static int minervaSprite;
	private static int zakuShipSprite;

	// BACKGROUND DEFINITION
	private static BackgroundDef gameBackground;

	// AUTO FOLLOW CAMERA VARIABLES
	private static int offsetMaxX = worldSizeX * 128 - xRes;
	private static int offsetMinX = 0;

	public static void main(String[] args) {

		// --------- GL SETUP
		GLProfile gl2Profile;

		try {
			gl2Profile = GLProfile.get(GLProfile.GL2);
		} catch (GLException ex) {
			System.out.println("OpenGL max supported version is too low.");
			System.exit(1);
			return;
		}

		// --------- WINDOW CREATION
		GLWindow window = GLWindow.create(new GLCapabilities(gl2Profile));
		window.setSize(xRes, yRes);
		window.setTitle("Operation: Heavy Arms");
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
		window.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent keyEvent) {
				kbState[keyEvent.getKeyCode()] = true;
			}

			public void keyReleased(KeyEvent keyEvent) {
				kbState[keyEvent.getKeyCode()] = false;
			}
		});

		// --------- OPENGL STATE
		window.getContext().makeCurrent();
		GL2 gl = window.getGL().getGL2();
		gl.glViewport(0, 0, xRes, yRes);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glOrtho(0, xRes, yRes, 0, 0, 100);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		// --------- TEXTURES
		// Background
		spaceTileTexture = glTexImageTGAFile(gl, "Space128x128.tga", tileSize);
		floorTileTexture = glTexImageTGAFile(gl, "Floor128x128.tga", tileSize);
		// Projectiles
		int[] laserSize = { 50, 8 }; // 50x8
		int laserTexture = glTexImageTGAFile(gl, "Laser.tga", laserSize);
		int[] bombSize = { 32, 32 };
		bombTexture = glTexImageTGAFile(gl, "ZakuBomb32x32.tga", bombSize);
		int[] eBombSize = { 32, 48 };
		eBombTexture = glTexImageTGAFile(gl, "ZakuEBomb32x48.tga", eBombSize);

		// --------- BACKGROUND DEFINITIONS
		// Space background
		gameBackground = new BackgroundDef(spaceTileTexture, false, 0, worldSizeX * worldSizeY);
		// Floor tiles
		gameBackground.setTile(floorTileTexture, true, 700, 800);
		gameBackground.setTile(floorTileTexture, true, 420, 440);
		gameBackground.setTile(floorTileTexture, true, 560, 575);
		gameBackground.setTile(floorTileTexture, true, 650, 670);
		gameBackground.setTile(floorTileTexture, true, 407, 412);
		gameBackground.setTile(floorTileTexture, true, 512, 513);

		// --------- DEFINE GAME VARIABLES
		// Player
		Player p1 = new Player(0, 650);
		int currentSprite = glTexImageTGAFile(gl, "GundamRunR1.tga", spriteSize);

		// Projectile List
		ArrayList<EnemyProjectiles> zakuBombs = new ArrayList<EnemyProjectiles>();
		ArrayList<EnemyProjectiles> zakuEBombs = new ArrayList<EnemyProjectiles>();

		// Projectile test variable
		EnemyProjectiles testPrj;
		EnemyProjectiles testBomb;
		EnemyProjectiles testEBomb;

		// --------- SHIPS
		int[] minervaSize = { 500, 300 };
		minervaSprite = glTexImageTGAFile(gl, "Minerva.tga", minervaSize);
		MinervaShip minerva = new MinervaShip(0, 450);
		int[] zakuShipSize = { 540, 260 };
		zakuShipSprite = glTexImageTGAFile(gl, "ZakuShip.tga", zakuShipSize);
		ZakuShip zakuShip = new ZakuShip(8900, 325);

		// --------- STATIC IMGS
		// int[] endScreenSize = { 400, 150 };
		// endScreen = glTexImageTGAFile(gl, "EndScreen.tga", endScreenSize);

		// --------- PLAYER ANIMATIONS

		// RUN RIGHT ANIMATION
		FrameDef[] runRight = { new FrameDef(glTexImageTGAFile(gl, "GundamRunR1.tga", spriteSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "GundamRunR2.tga", spriteSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "GundamRunR3.tga", spriteSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "GundamRunR4.tga", spriteSize), (float) 55) };
		AnimationDef runRightDef = new AnimationDef("runRight", runRight);
		AnimationData runRightData = new AnimationData(runRightDef);

		// HOVER ANIMATION
		FrameDef[] hover = { new FrameDef(glTexImageTGAFile(gl, "GundamHover1.tga", spriteHoverSize), (float) 25),
				new FrameDef(glTexImageTGAFile(gl, "GundamHover2.tga", spriteHoverSize), (float) 25) };
		AnimationDef hoverDef = new AnimationDef("hover", hover);
		AnimationData hoverData = new AnimationData(hoverDef);

		// FALLING ANIMATION

		// SHOOT ANIMATION
		FrameDef[] shoot = { new FrameDef(glTexImageTGAFile(gl, "GundamShoot1.tga", spriteShootSize), (float) 25),
				new FrameDef(glTexImageTGAFile(gl, "GundamShoot1.tga", spriteShootSize), (float) 25) };
		AnimationDef shootDef = new AnimationDef("shoot", shoot);
		AnimationData shootData = new AnimationData(shootDef);

		// DEATH ANIMATION

		// --------- ENEMY ANIMATIONS
		FrameDef[] zakuRunRight = { new FrameDef(glTexImageTGAFile(gl, "ZakuRunR1.tga", enemySpriteSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "ZakuRunR2.tga", enemySpriteSize), (float) 55) };
		AnimationDef zakuRunRightDef = new AnimationDef("zakuRunRight", zakuRunRight);
		AnimationData zakuRunRightData = new AnimationData(zakuRunRightDef);

		FrameDef[] zakuRunLeft = { new FrameDef(glTexImageTGAFile(gl, "ZakuRunL1.tga", enemySpriteSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "ZakuRunL2.tga", enemySpriteSize), (float) 55) };
		AnimationDef zakuRunLeftDef = new AnimationDef("zakuRunLeft", zakuRunLeft);
		AnimationData zakuRunLeftData = new AnimationData(zakuRunLeftDef);

		// --------- OBSTACLE ANIMATIONS
		FrameDef[] blackHole = { new FrameDef(glTexImageTGAFile(gl, "BlackHole1.tga", blackHoleSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "BlackHole2.tga", blackHoleSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "BlackHole3.tga", blackHoleSize), (float) 55),
				new FrameDef(glTexImageTGAFile(gl, "BlackHole4.tga", blackHoleSize), (float) 55) };

		AnimationDef blackHoleDef = new AnimationDef("blackhole", blackHole);
		AnimationData blackHoleData = new AnimationData(blackHoleDef);

		// --------- CAMERA
		Camera camera = new Camera(0, 0);

		// --------- PLAYER POS AND TILE TRACKER FOR COLLISION DETECTION
		int spritePrevX = p1.getX();
		int spritePrevY = p1.getY();
		AABB tileAABB;

		// Enemy List
		ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
		Enemy e1 = new Enemy(1100, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e2 = new Enemy(1025, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e3 = new Enemy(1300, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e4 = new Enemy(1500, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e5 = new Enemy(1700, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e6 = new Enemy(2500, 400, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e7 = new Enemy(3000, 25, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e8 = new Enemy(2700, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e9 = new Enemy(2900, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e10 = new Enemy(3250, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e11 = new Enemy(3750, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e12 = new Enemy(4150, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e13 = new Enemy(4450, 50, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e14 = new Enemy(4700, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e15 = new Enemy(4950, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e16 = new Enemy(5300, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e17 = new Enemy(5100, 65, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e18 = new Enemy(5600, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e19 = new Enemy(5875, 400, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e20 = new Enemy(6200, 768, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e21 = new Enemy(6520, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e22 = new Enemy(6850, 450, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e23 = new Enemy(7100, 600, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e24 = new Enemy(7400, 395, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e25 = new Enemy(7700, 400, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		Enemy e26 = new Enemy(8000, 300, zakuRunRight, zakuRunRightDef, zakuRunRightData, zakuRunLeft, zakuRunLeftDef,
				zakuRunLeftData);
		enemyList.add(e1);
		enemyList.add(e2);
		enemyList.add(e3);
		enemyList.add(e4);
		enemyList.add(e5);
		enemyList.add(e6);
		enemyList.add(e7);
		enemyList.add(e8);
		enemyList.add(e9);
		enemyList.add(e10);
		enemyList.add(e11);
		enemyList.add(e12);
		enemyList.add(e13);
		enemyList.add(e14);
		enemyList.add(e15);
		enemyList.add(e16);
		enemyList.add(e17);
		enemyList.add(e18);
		enemyList.add(e19);
		enemyList.add(e20);
		enemyList.add(e21);
		enemyList.add(e22);
		enemyList.add(e23);
		enemyList.add(e24);
		enemyList.add(e25);

		// Obstacle List
		blackHoleList = new ArrayList<BlackHole>();
		BlackHole bh1 = new BlackHole(800, 300, blackHole, blackHoleDef, blackHoleData);
		BlackHole bh2 = new BlackHole(1535, 450, blackHole, blackHoleDef, blackHoleData);
		BlackHole bh3 = new BlackHole(2350, 500, blackHole, blackHoleDef, blackHoleData);
		blackHoleList.add(bh1);
		blackHoleList.add(bh2);
		blackHoleList.add(bh3);

		// --------- TIMING VARIABLES
		long lastFrameNS;
		long curFrameNS = System.nanoTime();

		int lastPhysicsFrameMs = 0;
		long curFrameMs;
		int physicsDeltaMs = 10;

		// --------- GAME LOOP
		while (!shouldExit && escapedZaku < 6 && !playerDead) {
			System.arraycopy(kbState, 0, kbPrevState, 0, kbState.length);
			lastFrameNS = curFrameNS;

			// OS MESSAGE PUMP
			window.display();
			if (!window.isVisible()) {
				shouldExit = true;
				break;
			}

			curFrameNS = System.nanoTime();
			curFrameMs = curFrameNS / 1000000;
			long deltaTimeMS = (curFrameNS - lastFrameNS) / 1000000;

			// Gravity Check
			gravity = true;

			// PHYSICS COLLISION DETECTION AND CORRECTION
			do {
				// Collision with background and resolution
				int startX = camera.getX() / tileSize[0];
				int endX = (camera.getX() + xRes) / tileSize[0];
				int startY = camera.getY() / tileSize[0];
				int endY = (camera.getY() + yRes) / tileSize[1];

				// Collision check player
				for (int i = startX; i < endX; i++) {
					for (int j = startY; j < endY; j++) {
						if (gameBackground.getTile(i, j).isCollision()) {
							tileAABB = new AABB(i * tileSize[0], j * tileSize[1], 128, 128);
							boolean coll = AABBIntersect(p1.getAABB(), tileAABB);
							if (coll) {
								p1.setX(spritePrevX);
								p1.setY(spritePrevY);
								gravity = false;
							}
						}
					}
				}

				// Move projectiles
				for (int i = 0; i < p1.getProjectiles().size(); i++) {
					mainCharProj = p1.getProjectiles();
					Projectile prj = mainCharProj.get(i);
					prj.update();
					prjHitBox = mainCharProj.get(i).getProjHitBox();

					for (Enemy e : enemyList) {
						if (AABBIntersect(prjHitBox, e.getHitbox())) {
							mainCharProj.get(i).setVisible(false);
							mainCharProj.remove(i);
							e.dealDmgTo(10);
							e.update(deltaTimeMS, p1.getX(), p1.getY());
						}
					}
				}

				// Move enemy projectiles
				for (Enemy e : enemyList) {
					if (e.getProjectiles().size() > 0) {
						for (int i = 0; i < e.getProjectiles().size(); i++) {
							zakuBombs = e.getProjectiles();
							testPrj = zakuBombs.get(i);
							testPrj.update();
							AABB projectile = zakuBombs.get(i).getProjHitBox();
							if (AABBIntersect(projectile, p1.getAABB())) {
								zakuBombs.get(i).setVisible(false);
								zakuBombs.remove(i);
								p1.takeDamage(10);
								System.out.println(p1.getHealth());
							}
						}
					}
					if (e.getMissiles().size() > 0) {
						for (int i = 0; i < e.getMissiles().size(); i++) {
							zakuEBombs = e.getMissiles();
							testPrj = zakuEBombs.get(i);
							testPrj.update();
							AABB projectile = zakuEBombs.get(i).getProjHitBox();
							if (AABBIntersect(projectile, p1.getAABB())) {
								zakuEBombs.get(i).setVisible(false);
								zakuEBombs.remove(i);
								p1.takeDamage(20);
								System.out.println(p1.getHealth());
							}
						}
					}
					if (p1.getHealth() <= 0) {
						playerDead = true;
						gameOver = true;
					}
				}

				// Check if player has reached the Zaku Ship
				if (AABBIntersect(p1.getAABB(), zakuShip.getHitbox())) {
					shouldExit = true;
					gameOver = true;
				}

				// Move black holes
				for (BlackHole bh : blackHoleList) {
					bh.patrol();
					bh.getBlackHoleData().update(deltaTimeMS);
				}

				// Black hole collision
				if (blackHoleList.size() > 0) {
					for (BlackHole bh : blackHoleList) {
						if (AABBIntersect(bh.getHitbox(), p1.getAABB())) {
							playerDead = true;
							gameOver = true;
							System.out.println("GAME OVER! Be careful of the black holes!");
						}
					}
				}

				// Collide with enemy
				if (enemyList.size() > 0) {
					for (Enemy e : enemyList) {
						if (AABBIntersect(e.getHitbox(), p1.getAABB())) {
							playerDead = true;
							gameOver = true;
							System.out.println("GAME OVER! Don't get hit by the Zaku's energy field!");
						}
					}
				}

				lastPhysicsFrameMs += physicsDeltaMs;
				// Changed while loop to > instead of <. May need to change back
			} while (lastPhysicsFrameMs + physicsDeltaMs > curFrameMs);

			// STORE PLAYER POS FOR NEXT ITERATION CHECK
			spritePrevX = p1.getX();
			spritePrevY = p1.getY();

			// AI LOOP
			for (Enemy e : enemyList) {
				if (e.getX() > -100) {
					if (e.update(deltaTimeMS, p1.getX(), p1.getY())) {
						e.getRunLeftData().update(deltaTimeMS);
					} else {
						e.getRunRightData().update(deltaTimeMS);
					}

				} else {
					e.setVisible(false);
					escapedZaku++;
					System.out.println(escapedZaku);
				}
			}

			// Delete from list if dead
			for (int i = 0; i < enemyList.size(); i++) {
				if (!enemyList.get(i).isVisible()) {
					enemyList.remove(i);
				}
			}

			// KEY PRESS EVENTS
			if (kbState[KeyEvent.VK_ESCAPE]) {
				shouldExit = true;
			}

			// PLAYER CONTROLS
			if (kbState[KeyEvent.VK_A]) {
				if (p1.getX() - 3 >= 0) {
					p1.addToX(-3);
				}
			}
			if (kbState[KeyEvent.VK_D]) {
				if (p1.getX() + 3 <= (worldSizeX * tileSize[0]) - spriteW) {
					p1.addToX(3);
					runRightData.update(deltaTimeMS);
					currentSprite = runRightData.getCurFrame();
				}
			}
			if (kbState[KeyEvent.VK_W]) {
				if (p1.getY() - 2 >= 0) {
					p1.addToY(-2);
					hoverData.update(deltaTimeMS);
					currentSprite = hoverData.getCurFrame();
					gravity = false;
				}
			}
			if (kbState[KeyEvent.VK_S]) {
				if (p1.getY() + 1 <= (worldSizeY * tileSize[1]) - spriteH) {
					p1.addToY(1);
				}
			}

			// Cooldown between shots
			shotTimer += deltaTimeMS;
			if (kbState[KeyEvent.VK_SPACE]) {
				if (shotTimer > 275) {
					p1.shoot();
					shootData.update(deltaTimeMS);
					currentSprite = shootData.getCurFrame();
					shotTimer = 0;
				}
			}

			gl.glClearColor(0, 0, 0, 1);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

			// FINAL CLEAN UP
			if (gravity) {
				p1.falling();
			}

			// AUTO FOLLOW CAMERA
			camera.setX(p1.getX() - xRes / 2);
			if (camera.getX() > offsetMaxX) {
				camera.setX(offsetMaxX);
			} else if (camera.getX() < offsetMinX) {
				camera.setX(offsetMinX);
			}

			// --------- DRAWING AREA

			// DRAW BACKGROUND
			startTile = BackgroundCheck(camera.getX(), camera.getY());
			for (int i = startTile; i < startTile + (xRes / tileSize[0] + 1); i++) {
				for (int j = 0; j < yRes / tileSize[1]; j++) {
					if (gameBackground.getTile(i, j) != null) {
						glDrawSprite(gl, gameBackground.getTile(i, j).getImage(), i * tileSize[0] - camera.getX(),
								j * tileSize[1] - camera.getY(), tileSize[0], tileSize[1]);
					}
				}
			}

			// DRAW PLAYER
			if (AABBIntersect(camera.getAABB(), p1.getAABB())) {
				glDrawSprite(gl, currentSprite, p1.getX() - camera.getX(), p1.getY() - camera.getY(), spriteSize[0],
						spriteSize[1]);
			}

			// DRAW SPRITES
			if (AABBIntersect(camera.getAABB(), minerva.getHitbox())) {
				glDrawSprite(gl, minervaSprite, minerva.getX() - camera.getX(), minerva.getY() - camera.getY(),
						minervaSize[0], minervaSize[1]);
			}
			if (AABBIntersect(camera.getAABB(), zakuShip.getHitbox())) {
				glDrawSprite(gl, zakuShipSprite, zakuShip.getX() - camera.getX(), zakuShip.getY() - camera.getY(),
						zakuShipSize[0], zakuShipSize[1]);
			}

			// DRAW OBSTACLES
			for (BlackHole bh : blackHoleList) {
				glDrawSprite(gl, bh.getBlackHoleData().getCurFrame(), bh.getX() - camera.getX(),
						bh.getY() - camera.getY(), blackHoleSize[0], blackHoleSize[1]);
			}

			// DRAW PROJECTILES
			userProjectiles = p1.getProjectiles();
			for (int i = 0; i < userProjectiles.size(); i++) {
				Projectile prj = userProjectiles.get(i);
				if (prj.isVisible()) {
					glDrawSprite(gl, laserTexture, prj.getX() - camera.getX(), prj.getY() - camera.getY(), laserSize[0],
							laserSize[1]);
				} else {
					p1.getProjectiles().remove(prj);
				}
			}

			// DRAW ENEMY PROJECTILES
			for (Enemy e : enemyList) {
				zakuBombs = e.getProjectiles();
				for (int i = 0; i < zakuBombs.size(); i++) {
					testBomb = zakuBombs.get(i);
					if (testBomb.isVisible()) {
						glDrawSprite(gl, bombTexture, testBomb.getX() - camera.getX(), testBomb.getY() - camera.getY(),
								bombSize[0], bombSize[1]);
					} else {
						e.getProjectiles().remove(testBomb);
					}
				}
				zakuEBombs = e.getMissiles();
				for (int i = 0; i < zakuEBombs.size(); i++) {
					testEBomb = zakuEBombs.get(i);
					if (testEBomb.isVisible()) {
						glDrawSprite(gl, eBombTexture, testEBomb.getX() - camera.getX(),
								testEBomb.getY() - camera.getY(), eBombSize[0], eBombSize[1]);
					} else {
						e.getProjectiles().remove(testEBomb);
					}
				}
			}

			// DRAW ENEMIES
			for (Enemy e : enemyList) {
				if (AABBIntersect(camera.getAABB(), e.getHitbox())) {
					if (e.isVisible()) {
						if (e.isLeft) {
							glDrawSprite(gl, e.getRunLeftData().getCurFrame(), e.getX() - camera.getX(),
									e.getY() - camera.getY(), enemySpriteW, enemySpriteH);
						} else {
							glDrawSprite(gl, e.getRunRightData().getCurFrame(), e.getX() - camera.getX(),
									e.getY() - camera.getY(), enemySpriteW, enemySpriteH);
						}

					}
				}
			}
		}
		System.exit(0);
	}

	// TEXTURE FUNCTIONS
	public static int glTexImageTGAFile(GL2 gl, String filename, int[] out_size) {
		final int BPP = 4;

		DataInputStream file = null;
		try {
			// Open the file.
			file = new DataInputStream(new FileInputStream(filename));
		} catch (FileNotFoundException ex) {
			System.err.format("File: %s -- Could not open for reading.", filename);
			return 0;
		}

		try {
			// Skip first two bytes of data we don't need.
			file.skipBytes(2);

			// Read in the image type. For our purposes the image type
			// should be either a 2 or a 3.
			int imageTypeCode = file.readByte();
			if (imageTypeCode != 2 && imageTypeCode != 3) {
				file.close();
				System.err.format("File: %s -- Unsupported TGA type: %d", filename, imageTypeCode);
				return 0;
			}

			// Skip 9 bytes of data we don't need.
			file.skipBytes(9);

			int imageWidth = Short.reverseBytes(file.readShort());
			int imageHeight = Short.reverseBytes(file.readShort());
			int bitCount = file.readByte();
			file.skipBytes(1);

			// Allocate space for the image data and read it in.
			byte[] bytes = new byte[imageWidth * imageHeight * BPP];

			// Read in data.
			if (bitCount == 32) {
				for (int it = 0; it < imageWidth * imageHeight; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = file.readByte();
				}
			} else {
				for (int it = 0; it < imageWidth * imageHeight; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = -1;
				}
			}

			file.close();

			// Load into OpenGL
			int[] texArray = new int[1];
			gl.glGenTextures(1, texArray, 0);
			int tex = texArray[0];
			gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, imageWidth, imageHeight, 0, GL2.GL_BGRA,
					GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(bytes));
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

			out_size[0] = imageWidth;
			out_size[1] = imageHeight;
			return tex;
		} catch (IOException ex) {
			System.err.format("File: %s -- Unexpected end of file.", filename);
			return 0;
		}
	}

	public static void glDrawSprite(GL2 gl, int tex, int x, int y, int w, int h) {
		gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glColor3ub((byte) -1, (byte) -1, (byte) -1);
			gl.glTexCoord2f(0, 1);
			gl.glVertex2i(x, y);
			gl.glTexCoord2f(1, 1);
			gl.glVertex2i(x + w, y);
			gl.glTexCoord2f(1, 0);
			gl.glVertex2i(x + w, y + h);
			gl.glTexCoord2f(0, 0);
			gl.glVertex2i(x, y + h);
		}
		gl.glEnd();
	}

	// AABB Intersect Function
	public static boolean AABBIntersect(AABB alt, AABB main) {
		// if alt box is to the right of main
		if (alt.getX() > main.getX() + main.getW()) {
			return false;
		}
		// if alt box is to the left of main
		if (alt.getX() + alt.getW() < main.getX()) {
			return false;
		}
		// if alt is below main
		if (alt.getY() > main.getY() + main.getH()) {
			return false;
		}
		// if alt is above main
		if (alt.getY() + alt.getH() < main.getY()) {
			return false;
		}
		return true;
	}

	public static int BackgroundCheck(int x, int y) {
		int xCoord = x / tileSize[0];
		int yCoord = y / tileSize[1];

		return yCoord * gameBackground.getWidth() + xCoord;
	}
}
