import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class Arrowhead extends DrawObject {
	static protected final double SQRT_3 = 1.7320508;

	static protected final double MAXIMUM_OUTLINE_WIDTH = 50.0;
	static protected final double MAXIMUM_WIDTH_OR_HEIGHT = 50.0;
	static protected final double MINIMUM_WIDTH_OR_HEIGHT = 0.1;
	
	static public final double ANGLE_EAST = 0;
	static public final double ANGLE_NORTH = Math.PI / 2;
	static public final double ANGLE_WEST = Math.PI;
	static public final double ANGLE_SOUTH = 3 * Math.PI / 2;

	static protected final int COLOUR_BLACK = 0x000000;
	static protected final int COLOUR_WHITE = 0xFFFFFF;

	protected double width;
	protected double height;
	protected int fillColour;
	protected int outlineColour;
	protected double outlineWidth;
	
	protected double angle;
	
	@Override
	public String getSerialisation() {
		return String.format("%f,%f,%f,%d,%d,%f", width, height, outlineWidth, fillColour, outlineColour, angle);
	}
	
	public enum BuiltinStyle {
		Normal,
		Outline,
	};
	
	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	@Override
	public RightClickMenu getRightClickMenu(Econogram e, DrawObject o) {
		return null;
	}
	
	@Override
	public String getName() {
		return "Arrowhead";
	}
	
	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("angle")) {
			PropertyEntrySlider slider = (PropertyEntrySlider) property;
			angle = slider.value / 360.0 * Math.PI * 2;
			update();
		}
		if (property.id.equals("outlinewidth")) {
			PropertyEntrySlider slider = (PropertyEntrySlider) property;
			outlineWidth = slider.value;
			update();
		}
		if (property.id.equals("width")) {
			PropertyEntrySlider slider = (PropertyEntrySlider) property;
			width = slider.value;
			height = slider.value;
			update();
		}
		if (property.id.equals("fillcol")) {
			fillColour = ((PropertyEntryColourPicker) property).colour;
			update();
		}
		if (property.id.equals("outlinecol")) {
			outlineColour = ((PropertyEntryColourPicker) property).colour;
			update();
		}
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		double ang = this.angle / (2 * Math.PI) * 360.0;
		while (ang < 0.0) {
			ang += 360.0;
		}
		while (ang > 360.0) {
			ang -= 360.0;
		}
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		properties.add(new PropertyEntryColourPicker("fillcol"	 , "Fill colour: ", fillColour));
		properties.add(new PropertyEntryColourPicker("outlinecol", "Outline colour: ", outlineColour));
		properties.add(new PropertyEntrySlider		("outlinewidth", "Outline width: ", 0.0, 10.0, outlineWidth, false, 1.0, 0.5));
		properties.add(new PropertyEntrySlider		("width", "Arrow size: ", 5.0, 50.0, width, false, 5.0, 0.5));
		properties.add(new PropertyEntrySlider		("angle"	 , "Angle: ", 0.0, 360.0, ang, true, 90.0, 45.0));

		return properties;
	}
	
	public Arrowhead(Coordinate relativePos, BuiltinStyle style, double angle) {
		super(relativePos);
		
		this.angle = angle;

		//set the properties based on the style given
		switch (style) {
		case Outline:
			width = 20.0f;
			height = SQRT_3 * 10.0f;
			outlineWidth = 2.0f;
			fillColour = COLOUR_WHITE;
			outlineColour = COLOUR_BLACK;
			break;
			
		case Normal:
		default:
			width = 20.0f;
			height = SQRT_3 * 10.0f;
			outlineWidth = 0.0f;
			fillColour = COLOUR_BLACK;
			outlineColour = COLOUR_BLACK;
			break;
		}
	}
	
	//throws an exception if a colour value is not in range,
	//called to verify colour values passed into other functions
	public void verifyColourIsValid(int colour) {
		if (colour < 0 || colour > 0xFFFFFF) {
			throw new IllegalArgumentException("Invalid arrowhead colour");
		}
	}
	
	public void setFillColour(int colour) {
		//ensure the colour is valid before setting the fill colour
		verifyColourIsValid(colour);
		fillColour = colour;
	}
	
	public void setOutlineColour(int colour) {
		//ensure the colour is valid before setting the outline colour
		verifyColourIsValid(colour);
		outlineColour = colour;
	}
	
	public void setOutlineWidth(double newWidth) {
		//negative widths are nonsensical
		if (newWidth < 0.0) {
			throw new IllegalArgumentException("Negative arrowhead outline width specified");
		}
		
		//set the width, forcing it to be in range
		outlineWidth = newWidth;
		if (outlineWidth > MAXIMUM_OUTLINE_WIDTH) {
			outlineWidth = MAXIMUM_OUTLINE_WIDTH;
		}
	}
	
	public void setWidth(double newWidth) {
		width = newWidth;
		
		//force it into range in needed
		if (width <= MINIMUM_WIDTH_OR_HEIGHT) {
			width = MINIMUM_WIDTH_OR_HEIGHT;
			
		} else if (width > MAXIMUM_WIDTH_OR_HEIGHT) {
			width = MAXIMUM_WIDTH_OR_HEIGHT;
		}
	}
	
	public void setHeight(double newHeight) {
		height = newHeight;
		
		//force it into range in needed
		if (height <= MINIMUM_WIDTH_OR_HEIGHT) {
			height = MINIMUM_WIDTH_OR_HEIGHT;
			
		} else if (height > MAXIMUM_WIDTH_OR_HEIGHT) {
			height = MAXIMUM_WIDTH_OR_HEIGHT;
		}
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		Coordinate c1 = new Coordinate(base.x + width / 2 * Math.cos(-angle + Math.PI / 2), base.y + width / 2 * Math.sin(-angle + Math.PI / 2));
		Coordinate c2 = new Coordinate(base.x + height * Math.cos(-angle), base.y + height * Math.sin(-angle));
		Coordinate c3 = new Coordinate(base.x + width / 2 * Math.cos(-angle - Math.PI / 2), base.y + width / 2 * Math.sin(-angle - Math.PI / 2));
		
		List<Coordinate> coords = new ArrayList<Coordinate>();
		coords.add(c1);
		coords.add(c2);
		coords.add(c3);
		
		PrimativePolygon polygon = new PrimativePolygon(this, coords);
		polygon.fillColour = fillColour;
		polygon.outlineColour = outlineColour;
		polygon.width = outlineWidth;
		primatives.add(polygon);
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		
	}
	
}
