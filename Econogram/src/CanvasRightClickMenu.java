import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class CanvasRightClickMenu extends RightClickMenu {
	CanvasRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}

	public void init() {
		JMenuItem addPoint = new JMenuItem("Insert Point");
		addPoint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createPointAtMouse(false);
			}
		});
		add(addPoint); 
		
		JMenuItem addPoint2 = new JMenuItem("Insert Free Point");
		addPoint2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createPointAtMouse(true);
			}
		});
		add(addPoint2); 
		
		add(new JSeparator()); 

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
		
		add(new JSeparator()); 

		JMenuItem arrowLabel = new JMenuItem("Insert Arrow");
		arrowLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createArrowAtMouse(false);
			}
		});
		add(arrowLabel); 
		
		JMenuItem arrowLabel2 = new JMenuItem("Insert Free Arrow");
		arrowLabel2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.createArrowAtMouse(true);
			}
		});
		add(arrowLabel2); 
	}
}
