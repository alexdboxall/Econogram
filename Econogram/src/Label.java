import java.util.ArrayList;
import java.util.List;

public class Label extends DrawObject {
	String text;
	double fontsize;

	@Override
	public String getSerialisation() {
		return String.format("%f,%s", fontsize, text);
	}
	
	public Label(Coordinate relativePos, String string) {
		super(relativePos);

		fontsize = 18.0;
		text = string;
	}
	
	public List<PropertyEntry> getPropertiesPanelLayout() {
		ArrayList<PropertyEntry> properties = new ArrayList<PropertyEntry>();
		
		properties.add(new PropertyEntryTextBox("text", "Text:", text.replace("\n", "\\n")));
		properties.add(new PropertyEntryTextBox("x", "X:", String.format("%.1f", relativePosition.x)));
		properties.add(new PropertyEntryTextBox("y", "Y:", String.format("%.1f", relativePosition.y)));
		properties.add(new PropertyEntrySlider("fontsize", "Font size:", 8.0, 48.0, fontsize, false, 1.0, 0.5));

		return properties;
	}

	public void updateProperty(PropertyEntry property) {
		if (property.id.equals("text")) {
			text = ((PropertyEntryTextBox) property).dataText.replace("\\n", "\n");
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
		if (property.id.equals("fontsize")) {
			try {
				fontsize = ((PropertyEntrySlider) property).value;
				update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addDrawPrimativesPreChild(Coordinate base, List<DrawPrimative> primatives) {
		
	}

	@Override
	public void addDrawPrimativesPostChild(Coordinate base, List<DrawPrimative> primatives) {
		String[] lines = text.split("\n");

		double yShift = 0.0;
		
		double widestLine = 0.0;
		for (String line : lines) {
			PrimativeText dummy = new PrimativeText(this, line, base);
			if (dummy.getWidth() > widestLine) {
				widestLine = dummy.getWidth();
			}
		}
		
		for (String line : lines) {
			PrimativeText dummy = new PrimativeText(this, line, base);
			double xShift = (widestLine - dummy.getWidth()) / 2;
			PrimativeText real = new PrimativeText(this, line, new Coordinate(base, new Coordinate(xShift, yShift)));
			real.setFontSize(fontsize);
			primatives.add(real);
			yShift += dummy.getHeight();
		}
	}

	@Override
	public String getName() {
		return "Label";
	}

	public void setFontSize(double d) {
		fontsize = d;
	}
}
