import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class PointRightClickMenu extends RightClickMenu {
	
	PointRightClickMenu(Econogram e, DrawObject c) {
		super(e, c);
	}

	public void init() {
		JMenuItem setPrimary = new JMenuItem("Delete");
		setPrimary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(econogram.DELETE_SELECTED_OBJECT.build());
			}
		});
		add(setPrimary); 
		
		Point point = (Point) object;
		
		if (point.label == null || point.label.parent != point) {
			add(new JSeparator());

			JMenuItem reintroduceLabel = new JMenuItem("Add label");
			reintroduceLabel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					econogram.actionManager.add(new Action() {
						@Override
						public boolean execute() {
							if (point.label != null) {
								point.addChild(point.label);
							} else {
								Label newLabel = new Label(new Coordinate(0.0, 0.0), "New label");
								point.label = newLabel;
								point.addChild(newLabel);
								point.addDrawPrimativesPostChild(point.getAbsolutePosition(), new ArrayList<DrawPrimative>());
							}
							
							point.getCanvasParent().repaint();
							return true;
						}
				
						@Override
						public boolean undo() {
							point.label.delete();
							point.getCanvasParent().repaint();
							return true;
						}
						
						@Override
						public boolean redo() {
							point.addChild(point.label);
							point.getCanvasParent().repaint();
							return false;
						}
					});
				}
			});
			add(reintroduceLabel); 
		}
	}
}
