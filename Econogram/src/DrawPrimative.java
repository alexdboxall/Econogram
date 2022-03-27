
import java.awt.*;

public abstract class DrawPrimative {
	protected DrawObject parent;

	public DrawPrimative(DrawObject parent) {
		this.parent = parent;
	}
	
	public DrawObject getParent() {
		return parent;
	}
	
	public double getHeightGivenPosition(double x, double y) {
		return getHeight();
	}
	
	public double getWidthGivenPosition(double x, double y) {
		return getWidth();
	}
	
	abstract public double getX();
	abstract public double getY();
	abstract public void draw(Graphics g, ZoomPanSettings settings);
	abstract public double getWidth();
	abstract public double getHeight();
}
