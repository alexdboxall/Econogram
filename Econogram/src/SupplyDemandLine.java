import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class SupplyDemandLine extends PrimaryLine {

	double gradient = -2.0;
	boolean verticalLine = false;
	double sliderVal;
	
	public void copy() {
		SupplyDemandLine sdl = new SupplyDemandLine(relativePosition, gradient, verticalLine);
		sdl.parent = this.parent;
		sdl.label.text = this.label.text;
		sdl.gradient = this.gradient;
		sdl.verticalLine = this.verticalLine;
		sdl.relativePosition = new Coordinate(this.relativePosition.x, this.relativePosition.y);
		parent.addChild(sdl);
	}

	@Override
	public String objectType3DigitID() {
		return "SDL";
	}
	
	public SupplyDemandLine(String fullSerial, int uid, Canvas canvas, DrawObject parent_) {
		super(fullSerial, uid, canvas, parent_);
		deserialiseTree(fullSerial);
	}
	
	public SupplyDemandLine(Coordinate coord, double defaultGradient, boolean vertical) {
	
		super(coord);
		
		alignDragWithPointLineDotsX2 = true;
		verticalLine = vertical;
		gradient = defaultGradient;
		canDrag = true;
	}
	
	public String getRawDefaultText() {
		return verticalLine ? "LRAS" : gradient == 0 ? "L" : gradient > 0.0 ? "D" : "S";
	}

	@Override
	public String getDefaultLabelText() {
		String text = getRawDefaultText();
		if (text.equals("LRAS")) {
			if (getCanvasParent().econogram.lrasLabelCounter > 0) text += String.format("^%d^", getCanvasParent().econogram.lrasLabelCounter);
			getCanvasParent().econogram.lrasLabelCounter++;
		}
		if (text.equals("L")) {
			if (getCanvasParent().econogram.hzLineLabelCounter > 0) text += String.format("^%d^", getCanvasParent().econogram.hzLineLabelCounter);
			getCanvasParent().econogram.hzLineLabelCounter++;
		}
		if (text.equals("S")) {
			if (getCanvasParent().econogram.supplyLineLabelCounter > 0) text += String.format("^%d^", getCanvasParent().econogram.supplyLineLabelCounter);
			getCanvasParent().econogram.supplyLineLabelCounter++;
		}
		if (text.equals("D")) {
			if (getCanvasParent().econogram.demandLineLabelCounter > 0) text += String.format("^%d^", getCanvasParent().econogram.demandLineLabelCounter);
			getCanvasParent().econogram.demandLineLabelCounter++;
		}
		return text;
	}

	@Override
	public List<IntersectableLine> getLineBreakdown() {
		List<IntersectableLine> l = new ArrayList<IntersectableLine>();
		
		//we must split this up so selection works properly (the bounding box of a large diagonal line
		//would be essentially the entire axis)
				
		final double increment = 2.0;
		if (verticalLine) {
			//done backwards so the label appears at the top instead of the bottom
			for (double i = 999.0; i > -999.0; i -= increment) {
				l.add(new IntersectableLine(new Coordinate(0, i), new Coordinate(0, i + increment)));
			}
		} else {
			for (double i = -999.0; i < 999.0; i += increment) {
				l.add(new IntersectableLine(new Coordinate(i, i * gradient), new Coordinate(i + increment, (i + increment) * gradient)));
			}
		}
		
		return l;
	}

	@Override
	public void reloadOnDeserialisation(String data_) {
		String data = primaryLineDeserialisation(data_);

		verticalLine = data.charAt(0) == 'V';
		
		String parts[] = data.substring(2).split(",");
		gradient = Double.parseDouble(parts[0]);
	}

	@Override
	public String getSerialisation() {
		return String.format("%s%c,%f", primaryLineSerialisation(), verticalLine ? 'Y' : 'N', gradient);
	}

	@Override
	public String getName() {
		return "Supply/Demand Line";
	}
	
	PropertyEntrySlider slider;
	
	@Override
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		slider = new PropertyEntrySlider("gradient", "Gradient:", -3.5, 3.5, Math.sqrt(Math.abs(gradient)) * (gradient < 0 ? -1 : 1), false, 0.1, 0.1);
		slider.textUpdateAction = new Action() {

			@Override
			public boolean execute() {
				double v = Double.parseDouble(slider.getJLabel().getText());
				v = (v > 0 ? -1 : 1) * v * v;
				slider.getJLabel().setText(String.format("%.2f", v));
				return false;
			}

			@Override
			public boolean undo() {
				return false;
			}

			@Override
			public boolean redo() {
				return false;
			}
			
		};
		
		if (verticalLine) {
			slider.disabled = true;
		}
		
		properties.add(new PropertyEntryTextBox("x", "X:", String.format("%.1f", relativePosition.x)));
		properties.add(new PropertyEntryTextBox("y", "Y:", String.format("%.1f", relativePosition.y)));
		properties.add(slider);
		properties.add(new PropertyEntryCheckbox("vertical", "Vertical:", verticalLine));

		update();

		return properties;
	}

	@Override
	public void updateProperty(PropertyEntry property) {
		slider.disabled = verticalLine;
		
		if (property.id.equals("gradient")) {
			double v = Math.pow(((PropertyEntrySlider) property).value, 2.0) * (((PropertyEntrySlider) property).value < 0 ? -1 : 1);
			sliderVal = ((PropertyEntrySlider) property).value;
			gradient = v;
			if (label != null) {
				mouseDragging(0, 0);
			}
			update();
		}
		if (property.id.equals("vertical")) {
			verticalLine = ((PropertyEntryCheckbox) property).selected;
			mouseDragging(0, 0);
			update();
		}
		if (property.id.equals("x")) {
			try {
				relativePosition.x = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				mouseDragging(0, 0);
				update();
			} catch (Exception e) {
				
			}
		}
		if (property.id.equals("y")) {
			try {
				relativePosition.y = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				mouseDragging(0, 0);
				update();
			} catch (Exception e) {
				
			}
		}
	}
}
