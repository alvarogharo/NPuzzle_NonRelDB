package view;

import control.AbstractController;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Clase que representa la GUI principal.
 * 
 * @author Miguel Ã�ngel
 * @version 1.0
 */
public class PuzzleGUI extends JFrame {

	// Instancia singleton
	public static PuzzleGUI instance = null;
	// Controlador
	public static AbstractController controller;
	// NÃºmero de filas
	public static int rowNum = 0;
	// NÃºmero de columnas
	public static int columnNum = 0;
	// TamaÃ±o de imagen
	public static int imageSize = 0;
	// Array de imagenes
	public static String[] imageList = null;
	// Panel de juego
	private BoardView boardView;

	/**
	 * Constructor privado
	 */
	private PuzzleGUI() {
		super("GMD PuzzleGUI");
		boardView = new BoardView(rowNum, columnNum, imageSize, imageList);
		boardView.addMouseListener(controller);
		this.getContentPane().setLayout(new BorderLayout());
		this.setJMenuBar(createMenuBar());
		this.getContentPane().add(boardView, BorderLayout.CENTER);
		this.getContentPane().add(createSouthPanel(), BorderLayout.SOUTH);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(500, 500);
		this.setLocation(centerFrame());
	}

	// Singleton
	public static PuzzleGUI getInstance() {
		if (instance == null) {
			instance = new PuzzleGUI();
		}
		return (instance);
	}

	public static void initialize(AbstractController controller, int rowNum, int columnNum, int imageSize,
			String[] imageList) {
		PuzzleGUI.controller = controller;
		PuzzleGUI.rowNum = rowNum;
		PuzzleGUI.columnNum = columnNum;
		PuzzleGUI.imageSize = imageSize;
		PuzzleGUI.imageList = imageList;
	}

	// MÃ©todo que crea el panel inferior
	private JPanel createSouthPanel() {
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JButton clutterButton = new JButton("Desordenar");// botÃ³n de
															// desordenar
		clutterButton.setActionCommand("clutter");
		JButton solveButton = new JButton("Resolver");
		solveButton.setActionCommand("solve");

		clutterButton.addActionListener(controller);
		solveButton.addActionListener(controller);

		southPanel.add(clutterButton);
		southPanel.add(solveButton);

		return (southPanel);
	}

	// MÃ©todo que genera la barra de menus
	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();
		JMenu archive = new JMenu("Archive");
		JMenu edit = new JMenu("Edit");
		JMenu help = new JMenu("Help");

		JMenuItem load = new JMenuItem("Load");
		load.setActionCommand("load");
		JMenuItem save = new JMenuItem("Save");
		save.setActionCommand("save");
		JMenuItem undo = new JMenuItem("Undo");
		undo.setActionCommand("undo");
		JMenuItem redo = new JMenuItem("Redo");
		redo.setActionCommand("redo");
		JMenuItem exit = new JMenuItem("Exit");
		exit.setActionCommand("exit");
		JMenuItem info = new JMenuItem("Info");
		info.setActionCommand("info");

		archive.add(load);
		archive.add(save);
		archive.add(exit);
		edit.add(undo);
		edit.add(redo);
		help.add(info);

		menu.add(archive);
		menu.add(edit);
		menu.add(help);

		load.addActionListener(controller);
		save.addActionListener(controller);
		undo.addActionListener(controller);
		redo.addActionListener(controller);
		exit.addActionListener(controller);
		info.addActionListener(controller);

		return (menu);
	}

	// Centrar el frame en el centro de la pantalla.
	private Point centerFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int xCoord = (screenSize.width - this.getWidth()) / 2;
		int yCoord = (screenSize.height - this.getHeight()) / 2;
		return (new Point(xCoord, yCoord));
	}

	public File showFileSelector() {
		File selectedFile = null;
		return (selectedFile);
	}

	public BoardView getBoardView() {
		return boardView;
	}

	// MÃ©todo para actualizar la imagen del tablero
	public void updateBoard(File imageFile) {

	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
