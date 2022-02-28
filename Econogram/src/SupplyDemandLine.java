import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class SupplyDemandLine extends PrimaryLine {

	double gradient = -2.0;
	boolean verticalLine = false;
	double sliderVal;
	
	public SupplyDemandLine(Coordinate coord) {
		super(coord);
		
		alignDragWithPointLineDotsX2 = true;

		canDrag = true;
	}

	@Override
	public List<IntersectableLine> getLineBreakdown() {
		List<IntersectableLine> l = new ArrayList<IntersectableLine>();
		
		//we must split this up so selection works properly (the bounding box of a large diagonal line
		//would be essentially the entire axis)
				
		final double increment = 2.0;
		if (verticalLine) {
			for (double i = -999.0; i < 999.0; i += increment) {
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
	public String getSerialisation() {
		return String.format("%c,%f,%f", verticalLine ? 'Y' : 'N', gradient, padding);
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
			update();
		}
		if (property.id.equals("vertical")) {
			verticalLine = ((PropertyEntryCheckbox) property).selected;
			update();
		}
		if (property.id.equals("x")) {
			try {
				relativePosition.x = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				update();
			} catch (Exception e) {
				
			}
		}
		if (property.id.equals("y")) {
			try {
				relativePosition.y = Double.parseDouble(((PropertyEntryTextBox) property).dataText);
				update();
			} catch (Exception e) {
				
			}
		}
	}
}
