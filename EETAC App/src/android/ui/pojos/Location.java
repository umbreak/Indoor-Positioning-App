package android.ui.pojos;

public class Location {
	private double x,y;
	private int id;
	public Location(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public Location(){}

	public double getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

