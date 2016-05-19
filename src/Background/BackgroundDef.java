package Background;

public class BackgroundDef {
	private int width = 100; // x-tiles
	private int height = 8; // y-tiles
	private Tile[] tiles;

	public BackgroundDef(int image, boolean c, int start, int end) {
		tiles = new Tile[(width * height) + 1];
		// place tiles
		for (int i = start; i < end; i++) {
			tiles[i] = new Tile(image, c);
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Tile getTile(int x, int y) {
		return tiles[(y * width) + x];
	}

	public void setTile(int image, boolean c, int start, int end) {
		for (int i = start; i < end; i++) {
			tiles[i] = new Tile(image, c);
		}
	}
}