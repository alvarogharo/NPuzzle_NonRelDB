package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import command.Command;
import command.MovementCommand;
import control.PuzzleController;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;


public class BaseXModel extends AbstractModel{
	private Context c;
	private String collectionPath;
	//private String root;
	private int nElements;
	private Stack<Command> undo = new Stack<>();
	private Stack<Command> redo = new Stack<>();
	
	
	

	
	public BaseXModel(int rowNum, int columnNum, int pieceSize, String[] imageList, String Path,String xmlName) 
			throws BaseXException {
		super(rowNum, columnNum, pieceSize, imageList);
		
		collectionPath = Path+xmlName;
		System.out.println(collectionPath);
		//Crea la base de datos de BaseX
		c= new Context();
		createBaseX(Path,xmlName);
		//Borra los datos del XML
		deleteQuery();
		//writeXML( Path, xmlName);
		this.nElements=imageList.length;
		//annadir piezas
		for(int i=0;i<imageList.length;i++) {
			PieceModel Piece=new PieceModel(i, i % columnNum, i / columnNum, pieceSize, imageList[i]);
			System.out.println(Piece.getId()+"  "+Piece.getImagePath());
			insertXML(Piece);
		}
		writeInFile();
		
	}
	/*borra un nodo PieceModel*/
	private void deleteQuery() throws BaseXException {
		XQuery deleteXML= new XQuery(" delete node /board/PieceModel");
		deleteXML.execute(c);
	}
	
	/*insertar un nodo en la base de datos*/
	private void insertXML(PieceModel Piece) throws BaseXException {
		
		XQuery insert= new XQuery("insert node "+XMLParser(Piece)+" into /board "); 
		insert.execute(c);
	}
	/*Transforma PiceModel en un objeto que se pueda utilizar en sentencias XQuery*/
	private String XMLParser(PieceModel p) {
		
		String xml2="element PieceModel{ element id { "+p.getId()+"},  element indexRow{"+p.getIndexRow()+"},"
				+ "element indexColumn{"+p.getIndexColumn()+"},element pieceSize{"+p.getImageSize()+"},element imagePath{'"+p.getImagePath()+"'}}";
		return xml2;
	}
	/*Metodo auxiliar para crear bases de datos XQuery*/
	private void createBaseX(String Path,String xmlName ) throws BaseXException {
		new CreateDB("board",Path).execute(c);
		new CreateDB("Collection").execute(c);
		new Add("board.xml",Path).execute(c);
	}
	
	/*Obtiene las piezas por el ID. Se borran porqe solo se usan en el update*/
	private PieceModel getPieceData(int pos, boolean flag) throws BaseXException {
		// for $patata in /catalog/book where $patata/author=('Galos, Mike') return $patata
		
		int iRow = pos % columnNum;
		int iColumn = pos / columnNum;
		String aux = "for $item in /board/PieceModel where ($item/indexRow=('"+iRow+"') and $item/indexColumn=('"+iColumn+"')) return $item/";
		XQuery getPiece= new XQuery(aux+"id");
		int id= Integer.parseInt(getPiece.execute(c).replaceAll("<[^>]+>", ""));
		getPiece= new XQuery(aux+"indexRow");
		int indexRow=Integer.parseInt(getPiece.execute(c).replaceAll("<[^>]+>", ""));
		getPiece= new XQuery(aux+"indexColumn");
		int indexColumn=Integer.parseInt(getPiece.execute(c).replaceAll("<[^>]+>", ""));
		getPiece= new XQuery(aux+"pieceSize");
		int PieceSize=Integer.parseInt(getPiece.execute(c).replaceAll("<[^>]+>", ""));
		getPiece= new XQuery(aux+"imagePath");
		String imagePath= getPiece.execute(c);
		imagePath = imagePath.replaceAll("<[^>]+>", "");
		//crear un Piece Model en funcion de varia queries
		PieceModel piece= new PieceModel(id,indexRow,indexColumn,PieceSize,imagePath);
		//System.out.println(indexRow);
		
		if(flag) {
		getPiece= new XQuery("for $item in /board/PieceModel where ($item/indexRow=('"+iRow+"') and $item/indexColumn=('"+iColumn+"')) return delete node $item");
		getPiece.execute(c);
		}


		return piece;
		
	}
	
	/*Actualiza las posiciones de los elementos del tablero*/
	@Override
	public void update(int blankPos, int movedPos)  {
		
		try {
			
		PieceModel blank = getPieceData(blankPos,true); 
		PieceModel moved= getPieceData(movedPos,true);
		//System.out.println(blank.getImagePath());
		blank.setIndexRow(movedPos % columnNum);
		blank.setIndexColumn(movedPos / columnNum);

		moved.setIndexRow(blankPos % columnNum);
		moved.setIndexColumn(blankPos / columnNum);
		
		insertXML(moved);
		insertXML(blank);
		writeInFile();
		
		}catch (BaseXException e) {
			e.printStackTrace();
		}
	}
	
	/*Comprueba si el puzzle está resuelto*/
	@Override
	public boolean isPuzzleSolve() {
		
		try {
		int previous = -1;
		
		for (int i=0;i<nElements;i++) {
			PieceModel p= this.getPieceData(i, false);
			
			if (previous != -1 && p.getId() != previous + 1) {
				return false;
			}
			previous = p.getId();
		}
		
		}catch (BaseXException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/*Realiza un movimiento aleatorio en el tablero*/
	@Override
	public int[] getRandomMovement(int lastPos, int pos) {
		int rand = -1;
		boolean aux = false;
		Random rnd = new Random();

		while (true) {
			rand = rnd.nextInt(columnNum*rowNum);
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

	@Override
	public void addNewPiece(int id, int indexRow, int indexCol, String imagePath) {
		
		
	}

	@Override
	public void addNewPiece(int id, int indexRow, int indexCol) {
		
		
	}
	
	
	/*Alamacena en un XML el estado actual del tablero*/
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
		for (int i = 0; i < this.nElements; i++) {
			aux = new Element("piece").setText("" + this.getPieceData(i, false).getId());
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
		writer.close();
	}
	/*Carga el tablero de un fichero XML*/
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
	/*Escribe en un fichero XML el tablero para que haya persistencia del modelo*/
	private void writeInFile() {
		try {
			Element Board;
			Element Piece;
			Element id;
			Element row;
			Element column;
			Element size;
			Element path;
		
			Board=new Element("board");
			for(int i=0;i<this.nElements;i++) {
				//System.out.println(i);
				PieceModel p= this.getPieceData(i, false);
				String idString= ""+p.getId();
				String rowString=""+p.getIndexRow();
				String columnString=""+p.getIndexColumn();
				String sizeString=""+p.getImageSize();
				String pathString=""+p.getImagePath();
				//System.out.println(pathString);
				id=new Element("id").setText(idString);
				row=new Element("indexRow").setText(rowString);
				column=new Element("indexColumn").setText(columnString);
				size=new Element("pieceSize").setText(sizeString);
				path=new Element("imagePath").setText(pathString);
				Piece= new Element("PieceModel");
				
				Piece.addContent(id);
				Piece.addContent(row);
				Piece.addContent(column);
				Piece.addContent(size);
				Piece.addContent(path);
				Board.addContent(Piece);
				}
			Document d= new Document(Board);
			FileWriter writer = new FileWriter("board.xml");
			XMLOutputter c = new XMLOutputter();
			c.setFormat(Format.getPrettyFormat());
			c.output(d, writer);
			writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	

		}
	
	/*Obtiene el tamaño del fichero XML para trabajar la cuestion de rendimiento*/
	public long getFileSize(){
		return new File(collectionPath).length();
	}
	/*Getters y setters*/
	public Stack<Command> getRedo() {
		return redo;
	}

	public void setRedo(Stack<Command> redo) {
		this.redo = redo;
	}
	public Stack<Command> getUndo() {
		return undo;
	}

	public void setUndo(Stack<Command> undo) {
		this.undo = undo;
	}
	
	
	
	}
