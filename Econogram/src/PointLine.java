import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public class PointLine extends DrawObject {
	protected boolean vertical = false;
	
	@Override
	public String getSerialisation() {
		return String.format("%c", vertical ? 'V' : 'H');
	}
	
	public PointLine(Coordinate relativePos, boolean vertical) {
		super(relativePos);
		this.vertical = vertical;
	}

	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return null;
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		return properties;
	}

	public void updateProperty(PropertyEntry property) {
		
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {

	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		if (parent == null || parent.parent == null) {
			return;
		}

		if (vertical) {
			primatives.add(new PrimativeGuideLine(this, base, new Coordinate(base.x, ((Axis)parent.parent).axisSize + parent.parent.getAbsolutePosition().y)));
		} else {
			primatives.add(new PrimativeGuideLine(this, base, new Coordinate(parent.parent.getAbsolutePosition().x, base.y)));
		}
	}

	@Override
	public String getName() {
		return "Point";
	}
}
