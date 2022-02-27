import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public abstract class DrawObject {
	protected Canvas canvasParent;
	protected DrawObject parent;
	protected List<DrawObject> children;
	Coordinate relativePosition;
		
	double relCutoffX = 0.0;
	double relCutoffY = 0.0;
	
	protected boolean canDrag = true;
	protected boolean alignDragWithPointLineDots = false;
	protected boolean alignDragWithPointLineDotsX2 = false;

	public DrawObject(Coordinate coord) {
		parent = null;
		children = new ArrayList<DrawObject>();
		relativePosition = coord;
	}
	
	public void delete() {
		if (parent == null) {
			assert canvasParent != null;
			canvasParent.deleteChild(this);
		} else {
			parent.deleteChild(this);
		}
	}
	
	public void deleteChild(DrawObject obj) {
		children.remove(obj);
	}
		
	public void mouseDragging(double deltaX, double deltaY) {
		relativePosition.x += deltaX + relCutoffX;
		relativePosition.y += deltaY + relCutoffY;
		
		if (alignDragWithPointLineDots) {
			double newX = ((int)(relativePosition.x / 6)) * 6;
			double newY = ((int)(relativePosition.y / 6)) * 6;
			relCutoffX = relativePosition.x - newX;
			relCutoffY = relativePosition.y - newY;
			relativePosition.x = newX;
			relativePosition.y = newY;
		}
		if (alignDragWithPointLineDotsX2) {
			double newX = ((int)(relativePosition.x / 12)) * 12;
			double newY = ((int)(relativePosition.y / 12)) * 12;
			relCutoffX = relativePosition.x - newX;
			relCutoffY = relativePosition.y - newY;
			relativePosition.x = newX;
			relativePosition.y = newY;
		}
		
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
	
	public void doubleClick(Econogram e) {
		e.propertiesPanel.attach(this);
		e.propertiesPanel.goToDoubleClickProperty(this);
	}
	
	public abstract RightClickMenu getRightClickMenu(Econogram e, DrawObject o);
	public abstract String getSerialisation();
	public abstract void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives);
	public abstract void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives);
	public abstract String getName();
	public abstract List<PropertyEntry> getPropertiesPanelLayout();
	public abstract void updateProperty(PropertyEntry property);
	
	protected boolean selected;
	
	public boolean isSelected() {
		return selected;
	}
	
	public void markSelected(boolean state) {
		selected = state;
	}
	
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
		
		child.canvasParent = this.canvasParent;
		
		if (child.parent == null) {
			child.parent = this;
		} else {
			assert false;
		}
	}
	
	public Coordinate getAbsolutePosition() {
		if (parent != null) {
			return new Coordinate(relativePosition, parent.getAbsolutePosition());
		} else {
			return relativePosition;
		}
	}
	
	public List<DrawPrimative> getRender(Coordinate base) {
		List<DrawPrimative> primatives = new ArrayList<DrawPrimative>();
		
		addDrawPrimativesPreChild(base, primatives);

		for (DrawObject child : children) {
			primatives.addAll(child.getRender(new Coordinate(base, child.relativePosition)));
		}
		
		if (parent != null && getCanvasParent() != null && getCanvasParent().isShowingParentGuides()) {
			PrimativeGuideLine pl = new PrimativeGuideLine(this, parent.getAbsolutePosition(), base);
			pl.width = 1.0 / getCanvasParent().zoomPanSettings.zoom;
			pl.colour = 0xC00000;
			pl.length = 6.0;
			primatives.add(pl);
		}
		
		addDrawPrimativesPostChild(base, primatives);

		return primatives;
	}
	
}
