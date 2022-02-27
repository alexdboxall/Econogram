
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PropertyEntryCheckbox extends PropertyEntry {
	String displayText;

	boolean selected;
	
	PropertyEntryCheckbox self;
		
	PropertyEntryPanel producePanel(DrawObject obj) {
		PropertyEntryPanel panel = new PropertyEntryPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);
		
		self = this;

		JLabel label = new JLabel(displayText, JLabel.LEFT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		JLabel val = new JLabel(displayText, JLabel.LEFT);
		val.setFont(new Font("Courier New", Font.PLAIN, 12));
		
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(selected);
	
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				obj.getCanvasParent().econogram.actionManager.add(new Action() {
					
					@Override
					public boolean execute() {
						boolean changed = selected != checkBox.isSelected();
						selected = checkBox.isSelected();
						obj.updateProperty(self);
						return changed;
					}

					@Override
					public boolean undo() {
						selected = !selected;
						checkBox.setSelected(selected);
						obj.updateProperty(self);
						return true;
					}
					
					@Override
					public boolean redo() {
						return undo();
					}
				});
			}
		});
				
		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(checkBox, c);

		return panel;
	}

	PropertyEntryCheckbox(String id, String displayText, boolean selected) {
		super(id);
		this.displayText = displayText;
		this.selected = selected;
	}
}
