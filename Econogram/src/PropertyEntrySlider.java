
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PropertyEntrySlider extends PropertyEntry {
	String displayText;

	Action textUpdateAction = null;
	
	double min;
	double max;
	double value;
	double major;
	double minor;
	boolean paintTicks;

	PropertyEntrySlider self;
	protected JLabel val;
	
	boolean disabled;
	
	public JLabel getJLabel() {
		return val;
	}
	
	PropertyEntryPanel producePanel(DrawObject obj) {
		PropertyEntryPanel panel = new PropertyEntryPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);
		
		self = this;

		JLabel label = new JLabel(displayText, JLabel.LEFT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		val.setFont(new Font("Courier New", Font.PLAIN, 12));
		
		PEJSliderOverride slider = new PEJSliderOverride();
		slider.setMinimum(0);
		slider.setMaximum(100000);
		slider.setValue((int)((value - min) / (max - min) * 100000.0));
		slider.setPreferredSize(new Dimension(120, 30));
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing((int) major);
		slider.setMinorTickSpacing((int) minor);
		slider.setPaintTicks(paintTicks);
		slider.hasOldValue = false;
		slider.setEnabled(!disabled);
	
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				obj.getCanvasParent().econogram.actionManager.add(new Action() {
					
					@Override
					public boolean execute() {						  
						if (!slider.hasOldValue) {
							slider.oldValue = slider.getValue();
						}
						
						val.setText(String.format("%.1f", ((double) slider.getValue()) / 100000.0 * (max - min) + min));
						if (textUpdateAction != null) {
							textUpdateAction.execute();
						}
						value = ((double) slider.getValue()) / 100000.0 * (max - min) + min;
						obj.updateProperty(self);
					
						slider.hasOldValue = true;

						return !slider.getValueIsAdjusting();
					}

					@Override
					public boolean undo() {
						slider.setValue(slider.oldValue);
						val.setText(String.format("%.1f", ((double) slider.getValue()) / 100000.0 * (max - min) + min));
						if (textUpdateAction != null) {
							textUpdateAction.execute();
						}
						value = ((double) slider.getValue()) / 100000.0 * (max - min) + min;
						obj.updateProperty(self);
						return true;
					}
					
					@Override
					public boolean redo() {
						return execute();
					}
				});
			}
		});
		
		val.setText(String.format("%.1f", ((double) slider.getValue()) / 100000.0 * (max - min) + min));
		if (textUpdateAction != null) {
			System.out.printf("!! 1\n");
			textUpdateAction.execute();
		}
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
		
		val = new JLabel(displayText, JLabel.LEFT);
		
		this.displayText = displayText;
		this.min = min;
		this.max = max;
		this.value = value;
		this.major = 100000 * major / (max - min);
		this.minor = 100000 * minor / (max - min);
		this.paintTicks = snap;
		
		disabled = false;
	}
}
