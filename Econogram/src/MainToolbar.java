import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainToolbar extends JToolBar {
	private static final long serialVersionUID = 539485374L;

	Econogram econogram;
	
	private JButton addButton(String tooltip, String textOrUrl, boolean useUrl, ActionFactory actionFactory) {
		JButton btn = new JButton("");
		btn.setFocusable(false);
		btn.setToolTipText(tooltip);
		if (useUrl) {
			btn.setIcon(new ImageIcon(new ImageIcon(textOrUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		} else{
			btn.setText(textOrUrl);
		}
		
		add(btn);
		
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				econogram.actionManager.add(actionFactory.build());
			}
		});
		return btn;
	}
	
	JButton undoButton;
	JButton redoButton;
	
	void updateToolbarEnabledStatus() {
		undoButton.setEnabled(econogram.actionManager.canUndo());
		redoButton.setEnabled(econogram.actionManager.canRedo());
		repaint();
	}
	
	MainToolbar(Econogram e) {
		super("Main Toolbar");
		econogram = e;
		
		setFloatable(true);
		setRollover(true);
		
		addButton("New Document", "src/img/silkicons/page.png", true, econogram.NEW_DOCUMENT_ACTION);
		addButton("Open", "src/img/silkicons/folder.png", true, econogram.OPEN_ACTION);
		addButton("Save", "src/img/silkicons/disk.png", true, econogram.SAVE_ACTION);
		addButton("Export", "src/img/silkicons/picture_go.png", true, econogram.EXPORT_ACTION);
		addButton("Print", "src/img/silkicons/printer.png", true, econogram.PRINT_ACTION);
		
		addSeparator();
		
		addButton("Cut", "src/img/silkicons/cut.png", true, econogram.NOP_ACTION);
		addButton("Copy", "src/img/silkicons/page_copy.png", true, econogram.NOP_ACTION);
		addButton("Paste", "src/img/silkicons/page_paste.png", true, econogram.NOP_ACTION);
		addButton("Delete", "src/img/silkicons/cross.png", true, econogram.DELETE_SELECTED_OBJECT);

		addSeparator();

		undoButton = addButton("Undo", "src/img/silkicons/arrow_undo.png", true, econogram.UNDO_ACTION);
		redoButton = addButton("Redo", "src/img/silkicons/arrow_redo.png", true, econogram.REDO_ACTION);
		
		updateToolbarEnabledStatus();
	}
}
