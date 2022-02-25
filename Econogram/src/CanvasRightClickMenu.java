import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class CanvasRightClickMenu extends RightClickMenu {
	CanvasRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}

	public void init() {
		JMenuItem addLabel = new JMenuItem("Insert Label");
		addLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createLabelAtMouse(false);
			}
		});
		add(addLabel); 
		
		JMenuItem addLabel2 = new JMenuItem("Insert Free Label");
		addLabel2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createLabelAtMouse(true);
			}
		});
		add(addLabel2); 
	}
}