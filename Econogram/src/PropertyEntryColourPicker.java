import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PropertyEntryColourPicker extends PropertyEntry {
	String displayText;
	int colour;
	PropertyEntryColourPicker self;
	
	JPanel producePanel(DrawObject obj) {
		JPanel panel = new JPanel();
		
		self = this;
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(layout);

		JLabel label = new JLabel(displayText, JLabel.RIGHT);
		label.setFont(new Font("Courier New", Font.PLAIN, 12));

		JLabel colbox = new JLabel("######  ", JLabel.LEFT);
		colbox.setForeground(new Color(colour));
		colbox.setFont(new Font("Courier New", Font.BOLD, 12));

		JButton changebox = new JButton();
		changebox.setText("Choose");
		changebox.setFont(new Font("Courier New", Font.BOLD, 12));
		changebox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color col = JColorChooser.showDialog(null, "Choose a colour", Color.BLACK);
				colour = col.getRGB() & 0xFFFFFF;
				obj.updateProperty(self);
				obj.updatePropertiesPanel();
			}
		});

		c.gridx = 0;
		c.gridy = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.gridy = 0;
		panel.add(colbox, c);
		c.gridx = 2;
		c.gridy = 0;
		panel.add(changebox, c);
		
		return panel;
	}

	PropertyEntryColourPicker(String id, String displayText, int col) {
		super(id);
		this.displayText = displayText;
		colour = col;
	}
}
