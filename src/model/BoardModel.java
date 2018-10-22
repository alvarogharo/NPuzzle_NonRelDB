package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import command.Command;
import command.MovementCommand;
import control.PuzzleController;

public class BoardModel extends AbstractModel {

	private List<PieceModel> pieceArray;
	private Stack<Command> undo = new Stack<>();
	private Stack<Command> redo = new Stack<>();

	public BoardModel(int rowNum, int columnNum, int pieceSize) {
		super(rowNum, columnNum, pieceSize);
		// TODO Auto-generated constructor stub
	}
	/*Constuctor*/
	public BoardModel(int rowNum, int columnNum, int pieceSize, String[] imageList) {
		super(rowNum, columnNum, pieceSize, imageList);
		pieceArray = new ArrayList<>();

		for (int i = 0; i < imageList.length; i++) {
			pieceArray.add(new PieceModel(i, i % columnNum, i / columnNum, pieceSize, imageList[i]));
		}

	}
	/*Guarda en un fichero XML el estado del tablero*/
	@Override
	public void save(Stack und, Stack re, Element conf) throws IOException {

		int[] array;
		Element aux;
		Element undoMovements = new Element("undoMovements");

		for (int i = 0; i < und.size(); i++) {
			array = ((Command) und.get(i)).getMovement();
			String s = array[0] + "," + array[1];
			aux = new Element("movement").setText(s);
			undoMovements.addContent(aux);
		}

		Element redoMovements = new Element("redoMovements");

		for (int i = 0; i < re.size(); i++) {
			array = ((Command) re.get(i)).getMovement();
			String s = array[0] + "," + array[1];
			aux = new Element("movement").setText(s);
			redoMovements.addContent(aux);
		}

		Element boardElements = new Element("board");
		for (int i = 0; i < pieceArray.size(); i++) {
			aux = new Element("piece").setText("" + pieceArray.get(i).getId());
			boardElements.addContent(aux);
		}

		Element movements = new Element("movements");
		movements.addContent(undoMovements);
		movements.addContent(redoMovements);

		Element save2 = new Element("game");

		save2.addContent(conf.detach());
		save2.addContent(boardElements);
		save2.addContent(movements);

		Document d = new Document(save2);
		FileWriter writer = new FileWriter("partida.xml");
		XMLOutputter c = new XMLOutputter();
		c.setFormat(Format.getPrettyFormat());
		c.output(d, writer);
		//c.output(d, System.out);
		writer.close();
	}
	/*Carga la partida*/
	@Override
	public void loadGame(String f, PuzzleController p) {

		String userDir = System.getProperty("user.dir") + File.separator;
		String fileSeparator = System.getProperty("file.separator");

		String file = f;
		File ff = new File(userDir + fileSeparator + file);
		SAXBuilder builder = new SAXBuilder();
		Document d = null;
		Element root = null;

		try {
			d = (Document) builder.build(ff);
			root = (Element) d.getRootElement();
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		List<Element> boardAux = root.getChildren("board").get(0).getChildren("piece");

		ArrayList<Integer> board = new ArrayList<>();

		for (Element effin : boardAux) {
			board.add(Integer.parseInt(effin.getValue()));
		}

		if (!undo.isEmpty()) {
			undo.clear();
		}

		Element movements = root.getChild("movements");
		List<Element> undoMovements = movements.getChildren("undoMovements").get(0).getChildren("movement");

		for (int i = 0; i < undoMovements.size(); i++) {
			Element undoM = undoMovements.get(i);
			String move1 = undoM.getValue().substring(0, 1);
			String move2 = undoM.getValue().substring(2, 3);
			int[] aux = { Integer.parseInt(move1), Integer.parseInt(move2) };
			Command auxC = new MovementCommand(aux, p);
			undo.add(auxC);
		}

		if (!redo.isEmpty()) {
			redo.clear();
		}

		List<Element> redoMovements = movements.getChildren("redoMovements").get(0).getChildren("movement");

		for (int i = 0; i < redoMovements.size(); i++) {
			Element redoM = redoMovements.get(i);
			String move1 = redoM.getValue().substring(0, 1);
			String move2 = redoM.getValue().substring(2, 3);
			int[] aux = { Integer.parseInt(move1), Integer.parseInt(move2) };
			Command auxC = new MovementCommand(aux, p);
			redo.add(auxC);
		}
	}
	
	/*Actualiza el estado del tablero*/
	@Override
	public void update(int blankPos, int movedPos) {

		PieceModel blank = pieceArray.get(blankPos);
		PieceModel moved = pieceArray.get(movedPos);

		blank.setIndexRow(movedPos % columnNum);
		blank.setIndexColumn(movedPos / columnNum);

		moved.setIndexRow(blankPos % columnNum);
		moved.setIndexColumn(blankPos / columnNum);

		pieceArray.set(blankPos, moved);
		pieceArray.set(movedPos, blank);
	}
	
	/*Comprueba que el puzle esta resuelto*/
	@Override
	public boolean isPuzzleSolve() {
		int previous = -1;

		for (PieceModel p : pieceArray) {
			if (previous != -1 && p.getId() != previous + 1) {
				return false;
			}
			previous = p.getId();
		}
		return true;
	}
	/*Genera un movimiento aleatorio en el tablero*/
	@Override
	public int[] getRandomMovement(int lastPos, int pos) {
		// TODO
		int rand = -1;
		boolean aux = false;
		Random rnd = new Random();

		while (true) {
			rand = rnd.nextInt(columnNum * rowNum);
			// rand = rnd.nextInt(9);
			if (rand != lastPos
					&& (pos + 1 == rand || pos - 1 == rand || pos + columnNum == rand || pos - columnNum == rand)) {
				aux = true;

				for (int i = 0; i < columnNum - 1; i++) {
					if ((pos == (i * columnNum) + 2 && rand == (i * columnNum) + 3)
							|| (pos == (i * columnNum) + 3 && rand == (i * columnNum) + 2)) {
						aux = false;
					}
				}

				if (aux) {
					int[] result = { pos, rand };
					return result;
				}
			}
		}
	}
	
	/*Getters y Setters*/
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return super.getRowCount();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return super.getColumnCount();
	}

	@Override
	public int getPieceSize() {
		// TODO Auto-generated method stub
		return super.getPieceSize();
	}

	@Override
	public String toString() {
		String aux = "";
		for (int i = 0; i < pieceArray.size(); i++) {
			aux += ", " + pieceArray.get(i);
		}
		return aux;
	}

	public List<PieceModel> getPieceArray() {
		return pieceArray;
	}

	public void setPieceArray(List<PieceModel> pieceArray) {
		this.pieceArray = pieceArray;
	}

	public Stack<Command> getUndo() {
		return undo;
	}

	public void setUndo(Stack<Command> undo) {
		this.undo = undo;
	}

	public Stack<Command> getRedo() {
		return redo;
	}

	public void setRedo(Stack<Command> redo) {
		this.redo = redo;
	}

	@Override
	public void addNewPiece(int id, int indexRow, int indexCol, String imagePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNewPiece(int id, int indexRow, int indexCol) {
		// TODO Auto-generated method stub

	}
}
