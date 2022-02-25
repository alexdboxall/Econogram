import java.util.ArrayList;
import java.util.List;

public abstract class DrawObject {
	protected Canvas canvasParent;
	protected DrawObject parent;
	protected List<DrawObject> children;
	Coordinate relativePosition;
	
	public DrawObject(Coordinate coord) {
		parent = null;
		children = new ArrayList<DrawObject>();
		relativePosition = coord;
	}
	
	public void mouseDragging(double deltaX, double deltaY) {
		relativePosition.x += deltaX;
		relativePosition.y += deltaY;
		update();
	}
	
	public String serialise() {
		String serial = String.format("%f,%f,%s", relativePosition.x, relativePosition.y, getSerialisation());
		
		String data = String.format("<%d,%s%d;", serial.length(), serial, children.size());
		
		for (DrawObject child : children) {
			String childSerial = child.serialise();
			data += String.format("%d:%s", childSerial.length(), childSerial);
			
		}
		
		data += ">";
		
		return data;
	}
	
	public abstract String getSerialisation();
	public abstract void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives);
	public abstract void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives);
	public abstract String getName();
	
	protected boolean selected;
	
	public boolean isSelected() {
		return selected;
	}
	
	public void markSelected(boolean state) {
		System.out.printf("Marking object with name %s with %s\n", getName(), state ? "true" : "false");
		selected = state;
	}
	
	public abstract List<PropertyEntry> getPropertiesPanelLayout();
	public abstract void updateProperty(PropertyEntry property);
	
	public void setCanvasParent(Canvas c) {
		canvasParent = c;
	}
	
	public Canvas getCanvasParent() {
		if (canvasParent != null) {
			return canvasParent;

		} else if (parent != null) {
			parent.getCanvasParent();
		}
		
		return null;
	}
	
	public void update() {
		if (canvasParent != null) {
			canvasParent.repaint();

		} else if (parent != null) {
			parent.update();
		}
	}
	
	public void updatePropertiesPanel() {
		if (canvasParent != null) {
			canvasParent.updatePropertiesPanel();

		} else if (parent != null) {
			parent.updatePropertiesPanel();
		}
	}
	
	public void addChild(DrawObject child) {
		children.add(child);
		
		if (child.parent == null) {
			child.parent = this;
		} else {
			assert false;
		}
	}
	
	public List<DrawPrimative> getRender(Coordinate base) {
		List<DrawPrimative> primatives = new ArrayList<DrawPrimative>();
		
		addDrawPrimativesPreChild(base, primatives);

		for (DrawObject child : children) {
			primatives.addAll(child.getRender(new Coordinate(base, child.relativePosition)));
		}
		
		addDrawPrimativesPostChild(base, primatives);

		return primatives;
	}
	
}
