
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PropertyEntrySlider extends PropertyEntry {
	String displayText;

	double min;
	double max;
	double value;
	double major;
	double minor;
	boolean paintTicks;
	
	PropertyEntrySlider self;
	
	JPanel producePanel(DrawObject obj) {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);
		
		self = this;

		JLabel label = new JLabel(displayText, JLabel.LEFT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		JLabel val = new JLabel(displayText, JLabel.LEFT);
		val.setFont(new Font("Courier New", Font.PLAIN, 12));
		
		JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(100000);
		slider.setValue((int)((value - min) / (max - min) * 100000.0));
		slider.setPreferredSize(new Dimension(120, 30));
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing((int) major);
		slider.setMinorTickSpacing((int) minor);
		slider.setPaintTicks(paintTicks);
		
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				val.setText(String.format("%.1f", ((double) slider.getValue()) / 100000.0 * (max - min) + min));
				value = ((double) slider.getValue()) / 100000.0 * (max - min) + min;
				obj.updateProperty(self);
			}
		});
		
		val.setText(String.format("%.1f", ((double) slider.getValue()) / 100000.0 * (max - min) + min));
		
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(slider, c);
		c.gridx = 2;
		c.gridy = 0;
		panel.add(val, c);
		
		return panel;
	}

	PropertyEntrySlider(String id, String displayText, double min, double max, double value, boolean snap, double major, double minor) {
		super(id);
		this.displayText = displayText;
		this.min = min;
		this.max = max;
		this.value = value;
		this.major = 100000 * major / (max - min);
		this.minor = 100000 * minor / (max - min);
		this.paintTicks = snap;
	}
}
