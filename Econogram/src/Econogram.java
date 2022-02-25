import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class Econogram implements MouseWheelListener, MouseListener, MouseMotionListener {
	
	JFrame frame;
	String filename;
	String filepath;
	boolean unsavedChanges;
	
	double mouseDownX;
	double mouseDownY;
	boolean panDragMode;
	double panOnMouseDownX;
	double panOnMouseDownY;
	DrawObject draggingObject;
	
	Canvas canvas;
	Axis primaryAxis;
	PropertiesPanel propertiesPanel;

	JScrollBar hzScrollBar;
	JScrollBar vtScrollBar;
	
	Random rng;
	
	int getRandomInt(int max) {
		if (rng == null) {
			rng = new Random();
		}
		return rng.nextInt(max);
	}
	
	JLabel statusLabel;
	void updateTitle() {
		frame.setTitle(String.format("Econogram - %c%s", unsavedChanges ? '*' : ' ', filename == null ? "Untitled Diagram" : filename));
	}
	
	void updateScrollbarSizes() {
		hzScrollBar.setVisibleAmount((int)((canvas.getWidth() / canvas.getZoom()) / (canvas.getUsedWidth() * canvas.getZoom()) * 1000 / canvas.getZoom()));
	}
	
	void performSavableAction() {
		unsavedChanges = true;
		updateTitle();
	}
	
	void save() {
		if (filename == null) {
			saveAs();
			return;
		}
		
		System.out.printf("%s\n", canvas.serialise());
				
		unsavedChanges = false;
		updateTitle();
	}
	
	void saveAs() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save As...");
		
		fileChooser.setFileFilter(new FileNameExtensionFilter("*.edi", "Econogram Diagram"));
		
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			
			String path = fileChooser.getSelectedFile().getAbsolutePath();
			filename = fileChooser.getSelectedFile().getName();

			if (!path.toLowerCase().endsWith(".edi")) {
				filename += ".edi";
				path += ".edi";
			}
			
			filepath = path;
			save();
		}
	}
	
	void addViewMenuBar(JMenuBar menu) {
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		menu.add(viewMenu);
		
		JMenuItem zoom100Button = new JMenuItem("Zoom to 100%            ");
		zoom100Button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoom100Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.setZoom(1.0);
			}
		});
		viewMenu.add(zoom100Button);
		
		JMenuItem zoomInButton = new JMenuItem("Zoom In");
		zoomInButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomIn();
			}
		});
		viewMenu.add(zoomInButton);
		
		JMenuItem zoomOutButton = new JMenuItem("Zoom Out");
		zoomOutButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomOut();
			}
		});
		viewMenu.add(zoomOutButton);
	}
	
	void addHelpMenuBar(JMenuBar menu) {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		menu.add(helpMenu);
		
		JMenuItem howToBtn = new JMenuItem("Help...");
		howToBtn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		howToBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		helpMenu.add(howToBtn);
		
		JMenuItem aboutBtn = new JMenuItem("About...           ");
		aboutBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				final ImageIcon icon = new ImageIcon();
				try {
					icon.setImage(ImageIO.read(getClass().getResourceAsStream("/img/icon.png")));
				} catch (IOException e1) {
				}
		        JOptionPane.showMessageDialog(null, "Econogram\nVersion 0.1\n\nCopyright Alex Boxall 2022\nAll rights reserved.\n", "About", JOptionPane.INFORMATION_MESSAGE, icon);
			}
		});
		helpMenu.add(aboutBtn);
	}
	
	void addWindowMenuBar(JMenuBar menu) {
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic('W');
		menu.add(windowMenu);
		
		
	}
	
	void addToolsMenuBar(JMenuBar menu) {
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');

		menu.add(toolsMenu);
		
		
	}
	
	void addInsertMenuBar(JMenuBar menu) {
		JMenu insertMenu = new JMenu("Insert");
		insertMenu.setMnemonic('I');
		menu.add(insertMenu);
		
		
		JMenuItem axisButton = new JMenuItem("Axis");
		axisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addObject(new Axis(new Coordinate(250.0 + getRandomInt(350), 100.0 + getRandomInt(250))));
				canvas.repaint();
			}
		});
		insertMenu.add(axisButton);
		
		JMenuItem labelButton1 = new JMenuItem("Label");
		labelButton1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		labelButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryAxis == null) {
					JOptionPane.showMessageDialog(frame, "There is no primary axis. Select an axis\nand then go to Edit > Set Primary Axis", "No primary axis", JOptionPane.ERROR_MESSAGE);
				} else {
					primaryAxis.addChild(new Label(new Coordinate(250.0 + getRandomInt(350), 100.0 + getRandomInt(250)), "New bound label"));
					canvas.repaint();
				}
				
			}
		});
		insertMenu.add(labelButton1);
		
		JMenuItem labelButton2 = new JMenuItem("Free Label");
		labelButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.addObject(new Label(new Coordinate(250.0 + getRandomInt(350), 100.0 + getRandomInt(250)), "New free label"));
				canvas.repaint();
			}
		});
		insertMenu.add(labelButton2);
		
	}
	
	void addEditMenuBar(JMenuBar menu) {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		menu.add(editMenu);
		
		JMenuItem setAxisButton = new JMenuItem("Set Primary Axis      ");
		setAxisButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (propertiesPanel.object != null && propertiesPanel.object.getClass() == (new Axis(new Coordinate(0.0, 0.0))).getClass()) {
					primaryAxis = (Axis) propertiesPanel.object;
				} else {
					JOptionPane.showMessageDialog(frame, "Please select an axis first.", "No axis selected", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		editMenu.add(setAxisButton);
	}
	
	void addFileMenuBar(JMenuBar menu) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		menu.add(fileMenu);
		
		
		JMenuItem newButton = new JMenuItem("New from Template...            ");
		newButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		fileMenu.add(newButton);
		
		JMenuItem new2Button = new JMenuItem("New Blank Diagram...");
		new2Button.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		new2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		fileMenu.add(new2Button);
		
		JMenuItem new3Button = new JMenuItem("New Window...");
		new3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Econogram.main(null);
			}
		});
		fileMenu.add(new3Button);
		
		fileMenu.add(new JSeparator());
		
		JMenuItem saveButton = new JMenuItem("Save");
		saveButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		fileMenu.add(saveButton);
		
		JMenuItem saveAsButton = new JMenuItem("Save As...");
		saveAsButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		fileMenu.add(saveAsButton);
		
		fileMenu.add(new JSeparator());

		
		JMenuItem exportButton = new JMenuItem("Export...");
		exportButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Export As...");
				
				fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "PNG Image"));
				
				if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					
					String path = fileChooser.getSelectedFile().getAbsolutePath();
					if (!path.toLowerCase().endsWith(".png")) {
						path += ".png";
					}
					
					//export(path, ExportQuality.Normal);
				}
			}
		});
		fileMenu.add(exportButton);
		
		fileMenu.add(new JSeparator());
		
		JMenuItem exitButton = new JMenuItem("Exit");
		exitButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		fileMenu.add(exitButton);
	
	}
	
	Econogram() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		unsavedChanges = true;
		filename = null;
		filepath = null;
		
		frame = new JFrame("");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		try {
			ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(new File("econogram.exe"));
			frame.setIconImage(icon.getImage());
		} catch (Exception e) { ; }
		
		canvas = new Canvas(this);
		canvas.setZoom(1.0);
		canvas.setPan(0.0, 0.0);
		primaryAxis = new Axis(new Coordinate(150, 90));
		canvas.addObject(primaryAxis);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		
		propertiesPanel = new PropertiesPanel();
		
		JPanel renderScrollPane = new JPanel(new BorderLayout());
		renderScrollPane.add(canvas);
		
		hzScrollBar = new JScrollBar();
		hzScrollBar.setOrientation(Adjustable.HORIZONTAL);
		hzScrollBar.setUnitIncrement(10);
		hzScrollBar.setMinimum(0);
		hzScrollBar.setMaximum(1000);
		hzScrollBar.setVisibleAmount((int)(canvas.getWidth() / canvas.getUsedWidth() * 1000));

		hzScrollBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				double pos = hzScrollBar.getValue();
				double relativePos = (pos / ((double)(hzScrollBar.getMaximum() - hzScrollBar.getMinimum()))) * canvas.getUsedWidth();
				canvas.setPan(relativePos * canvas.getZoom(), canvas.getPanY());
			}
		});
		renderScrollPane.add(hzScrollBar, BorderLayout.SOUTH);
		renderScrollPane.setPreferredSize(new Dimension(850, 750));
		
		JLabel zoomLabel;

		statusLabel = new JLabel("Ready");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

		JPanel statusBar = new JPanel(new BorderLayout());
		JPanel innerStatusBar = new JPanel(new BorderLayout());
		statusBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
		statusBar.add(innerStatusBar);
		innerStatusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
		innerStatusBar.add(statusLabel, BorderLayout.WEST);
		
		JPanel zoomPanel = new JPanel();
		
		JSlider zoomSlider = new JSlider(0, 2000, 1000);
		zoomSlider.setFocusable(false);
		zoomSlider.setPreferredSize(new Dimension(120, 20));

		JButton zoomOutButton = new JButton("-");
		zoomOutButton.setFocusable(false);
		zoomOutButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		zoomOutButton.setBorder(new EmptyBorder(4, 8, 4, 8));
		zoomOutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomOut();
				updateScrollbarSizes();
			}
		});
		JButton zoomInButton = new JButton("+");
		zoomInButton.setFocusable(false);
		zoomInButton.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		zoomInButton.setBorder(new EmptyBorder(4, 8, 4, 8));
		zoomInButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.zoomIn();	
				updateScrollbarSizes();
			}
		});
		zoomLabel = new JLabel("100%");
		zoomLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		zoomLabel.setPreferredSize(new Dimension(35, 25));
		JLabel zoomLabel2 = new JLabel("Zoom: ");
		zoomLabel2.setFont(new Font("Arial", Font.PLAIN, 12));
		zoomPanel.add(zoomLabel2, BorderLayout.EAST);
		zoomPanel.add(zoomOutButton);
		zoomPanel.add(zoomSlider);
		zoomPanel.add(zoomInButton);
		zoomPanel.add(zoomLabel);
		zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
				updateScrollbarSizes();
            }
        });
		
		statusBar.add(zoomPanel, BorderLayout.EAST);
		

		JPanel mainGroup = new JPanel(new BorderLayout());
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, renderScrollPane, propertiesPanel);
		split.setResizeWeight(1);

		Dimension minimumSize = new Dimension(0, 0);
		split.getLeftComponent().setMinimumSize(minimumSize);
		split.getRightComponent().setMinimumSize(minimumSize);
		
		mainGroup.add(split, BorderLayout.CENTER);
		mainGroup.add(statusBar, BorderLayout.PAGE_END);
		
		JMenuBar menu = new JMenuBar();
		frame.setJMenuBar(menu);
		
		addFileMenuBar(menu);
		addEditMenuBar(menu);
		addViewMenuBar(menu);
		addInsertMenuBar(menu);
		addToolsMenuBar(menu);
		addWindowMenuBar(menu);
		addHelpMenuBar(menu);
		
		frame.setPreferredSize(new Dimension(1400, 900));
		frame.setContentPane(mainGroup);
		frame.pack();
		frame.setVisible(true);
		
		
		canvas.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	updateScrollbarSizes();
		    }
		});

		updateTitle();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateScrollbarSizes();
				
				Dimension defaultSize = new Dimension(250, 0);
				split.getRightComponent().setSize(defaultSize);
			}
		});
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {		
		int notches = e.getWheelRotation();

		if (e.isControlDown()) {
			if (notches > 0) canvas.zoomOut();
			else if (notches < 0) canvas.zoomIn();
			
		} else {
			canvas.scrollY(((double) notches) * 25.0);
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDownX = e.getX();
		mouseDownY = e.getY();
		
		double x = (e.getX() + canvas.getPanX()) / canvas.getZoom();
		double y = (e.getY() + canvas.getPanY()) / canvas.getZoom();
		
		DrawObject objClickedOn = canvas.getObjectAtPosition(x, y);
		
		if (objClickedOn == null) {
			panDragMode = true;
			panOnMouseDownX = canvas.getPanX();
			panOnMouseDownY = canvas.getPanY();
			statusLabel.setText("Ready");
			propertiesPanel.detach();
		
		} else {
			panOnMouseDownX = x;
			panOnMouseDownY = y;
			draggingObject = objClickedOn;
			panDragMode = false;
			statusLabel.setText(String.format("You clicked on a %s", objClickedOn.getName()));
			propertiesPanel.attach(objClickedOn);
		}
	
		canvas.repaint();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		canvas.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		if (panDragMode) {
			canvas.setPan(panOnMouseDownX - e.getX() + mouseDownX, panOnMouseDownY - e.getY() + mouseDownY);
		
		} else {
			if (draggingObject != null) {
				double x = (e.getX() + canvas.getPanX()) / canvas.getZoom();
				double y = (e.getY() + canvas.getPanY()) / canvas.getZoom();
				
				
				double deltaX = x - panOnMouseDownX;
				double deltaY = y - panOnMouseDownY;
				
				draggingObject.mouseDragging(deltaX, deltaY);
				propertiesPanel.regenerate();
				
				panOnMouseDownX = x;
				panOnMouseDownY = y;
				
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
	public static void main(String args[]) {
		new Econogram();
	}
}
