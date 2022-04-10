import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

/*
 * Object IDs
 * 
 * AXS		Axis
 * ARH		Arrowhead
 * ARR		Arrow
 * CPT		CalculatedPoint
 * KNS		KeynesianLRAS
 * LBL		Label
 * PNT		Point
 * SDL		Supply/Demand Line
 * 
 */
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
		uniqueID = hashCode();
	}
	
	int deserialisationSkipIndex = -1;
	
	public DrawObject(String serialData, int oldUniqueID, Canvas canvas, DrawObject parent_) {
		parent = parent_;
		children = new ArrayList<DrawObject>();
		uniqueID = oldUniqueID;
		canvasParent = canvas;
		
		String[] serialParts = serialData.replace("<", ",").split(",");
		int skip = serialParts[0].length() + 1 + serialParts[1].length() + 1;
		relativePosition = new Coordinate(Double.parseDouble(serialParts[0]), Double.parseDouble(serialParts[1]));
		deserialisationSkipIndex = skip;
	}
	
	public void delete() {
		if (parent == null) {
			assert canvasParent != null;
			canvasParent.deleteChild(this);
		} else {
			parent.deleteChild(this);
		}
		
		if (getCanvasParent() != null) {
			if (getCanvasParent().econogram.propertiesPanel.object == this) {
				getCanvasParent().econogram.propertiesPanel.detach();
			}
		}
	}
	
	public void deleteChild(DrawObject obj) {
		obj.parent = null;
		children.remove(obj);
	}
		
	public void mouseDragging(double deltaX, double deltaY) {
		double oldRelativePositionX = relativePosition.x;
		double oldRelativePositionY = relativePosition.y;
		double oldRelCutoffX = relCutoffX;
		double oldRelCutoffY = relCutoffY;

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
		
		if (relativePosition.x != oldRelativePositionX || relativePosition.y != oldRelativePositionY) {
			getCanvasParent().econogram.actionManager.add(new Action() {
				double oldRelPosX = oldRelativePositionX;
				double oldRelPosY = oldRelativePositionY;
				double oldCutoffX = oldRelCutoffX;
				double oldCutoffY = oldRelCutoffY;
				
				double newRelPosX = relativePosition.x;
				double newRelPosY = relativePosition.y;
				double newCutoffX = relCutoffX;
				double newCutoffY = relCutoffY;
	
				@Override
				boolean isFence() {
					return false;
				}
				
				@Override
				boolean execute() {
					return true;
				}
	
				@Override
				boolean undo() {
					relativePosition.x = oldRelPosX;
					relativePosition.y = oldRelPosY;
					relCutoffX = oldCutoffX;
					relCutoffY = oldCutoffY;
					update();
					return true;
				}
	
				@Override
				boolean redo() {
					relativePosition.x = newRelPosX;
					relativePosition.y = newRelPosY;
					relCutoffX = newCutoffX;
					relCutoffY = newCutoffY;
					update();
					return true;
				}
				
			});
		}
		
		update();
	}
	
	public abstract void reloadOnDeserialisation(String data);
	
	public DrawObject findChildWithUID(int uid) {
		for (DrawObject c : children) {
			if (c.uniqueID == uid) {
				return c;
			}
		}
		return null;
	}
	
	public void deserialiseTree(String fullSerial) {
		String serial = fullSerial.substring(deserialisationSkipIndex);
				
		String ourPartLengthStr = serial.split(",")[0];
		int ourPartLength = Integer.parseInt(ourPartLengthStr);
		
		serial = serial.substring(ourPartLengthStr.length() + 1);

		String ourPartData = serial.substring(0, ourPartLength);
		serial = serial.substring(ourPartLength);
		
		String childrenCountStr = serial.split(";")[0];
		int numChildren = Integer.parseInt(childrenCountStr);
		
		String childData = serial.substring(childrenCountStr.length() + 1);
		
		reloadOnDeserialisation(ourPartData);
				
		for (int i = 0; i < numChildren; ++i) {
			String childSerialLengthString = childData.split(":")[0];
			int childSerialLength = Integer.parseInt(childSerialLengthString);
						
			childData = childData.substring(childSerialLengthString.length() + 1);
			
			String childUID = childData.substring(0, 8);
			int childUIDInt = Integer.parseInt(childUID, 16);
			childData = childData.substring(8);
			String childType = childData.substring(0, 3);
			childData = childData.substring(3);
			
			String childSerial = childData.substring(0, childSerialLength);

			childData = childData.substring(childSerialLength);

			if (childType.equals("ARH")) {
				addChild(new Arrowhead(childSerial, childUIDInt, getCanvasParent(), this));
				
			} else if (childType.equals("AXS")) {
				addChild(new Axis(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("LBL")) {
				addChild(new Label(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("SDL")) {
				addChild(new SupplyDemandLine(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("KNS")) {
				addChild(new KeynesianLRAS(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("PNT")) {
				addChild(new Point(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("CPT")) {
				addChild(new CalculatedPoint(childSerial, childUIDInt, getCanvasParent(), this));
			
			} else if (childType.equals("ARR")) {
				addChild(new Arrow(childSerial, childUIDInt, getCanvasParent(), this));
			
			}
		}
	}
	
	public abstract String objectType3DigitID();
	
	public String serialise() {
		String serial = getSerialisation();
		
		String data = String.format("%f,%f<%d,%s%d;", relativePosition.x, relativePosition.y, serial.length(), serial, children.size());
		
		for (DrawObject child : children) {
			String childSerial = child.serialise();
			data += String.format("%d:%08X%s%s", childSerial.length(), child.getUniqueID(), child.objectType3DigitID(), childSerial);
			
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
	
	protected int uniqueID;
	
	public int getUniqueID() {
		return uniqueID;
	}
	
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
