package model;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import command.Command;
import command.MovementCommand;
import control.PuzzleController;
import view.PieceView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;


import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class MongoDBModel extends AbstractModel{
	//private MongoClientOptions options;
	private MongoClient client;
	private MongoDatabase db;
	private MongoCollection<Document> coll;
	private int nElements;
	private Stack<Command> undo = new Stack<>();
	private Stack<Command> redo = new Stack<>();
	
	/*Constructor de la clase*/
	public MongoDBModel(int rowNum, int columnNum, int pieceSize, String[] imageList) {
		super(rowNum, columnNum, pieceSize, imageList);
		
		createDB();
		deleteDB();
		nElements=imageList.length;
		for(int i=0;i<this.nElements;i++) {
			PieceModel p= new PieceModel(i, i % columnNum, i / columnNum, pieceSize, imageList[i]);
			coll.insertOne(this.pieceForMongo(p));
			
		}	
	}
	/*Crea la base de datos MongoDB*/
	private void createDB() {
			client = new MongoClient();
			db = client.getDatabase("puzzle");
	       coll = db.getCollection("board");
		
		
	}
	/*Borrar todos los datos de la coleccion, ya que si ha usado antes la coleccion, se provocara inconsistencia*/
	private void deleteDB() {
		BsonDocument deletos= new BsonDocument();
		coll.deleteMany(deletos);
		
	}
	
	/*Metodo auxiliar que recibe una pieza y la transforma en un objeto parseado para la liberia de MongoDB*/
	private Document pieceForMongo(PieceModel p) {
	

		Document piece= new Document("id",p.getId())
				.append("indexRow",p.getIndexRow())
				.append("indexColumn", p.getIndexColumn())
				.append("pieceSize", p.getImageSize())
				.append("imagePath", p.getImagePath())
				;
		
		
		return piece;
		
	}
	

	/*Busca una pieza por id en la base de datos y la devuelve como PieceModel*/
	private PieceModel findPiece(int pos) {
		Bson projection = Projections.fields(Projections.include("id","indexRow","indexColumn","pieceSize","imagePath"));
		
		int iRow = pos % columnNum;
		int iColumn = pos / columnNum;
		ArrayList<Document> query=coll.find(Filters.and(Filters.eq("indexRow",iRow), Filters.eq("indexColumn",iColumn))).projection(projection).into(new ArrayList<Document>());
		Document piece= query.get(0);
		int id=(int) piece.get("id");
		int row=(int) piece.get("indexRow");
		int colum=(int) piece.get("indexColumn");
		int size=(int) piece.get("pieceSize");
		String path=(String) piece.get("imagePath");
		//coll.deleteOne(piece);
		PieceModel p= new PieceModel(id,row,colum,size,path);
		return p;
	}
	/*Metodo que actualiza el tablero cada movimiento*/
	@Override
	public void update(int blankPos, int movedPos) {
		
		PieceModel blank= findPiece(blankPos);	
		PieceModel moved= findPiece(movedPos);
		
		blank.setIndexRow(movedPos % columnNum);
		blank.setIndexColumn(movedPos / columnNum);

		moved.setIndexRow(blankPos % columnNum);
		moved.setIndexColumn(blankPos / columnNum);
		
		int row = blankPos % columnNum;
		int column = blankPos / columnNum;
		coll.replaceOne(Filters.and(Filters.eq("indexRow",row), Filters.eq("indexColumn",column)), pieceForMongo(moved));
		
		row = movedPos % columnNum;
		column = movedPos / columnNum;
		coll.replaceOne(Filters.and(Filters.eq("indexRow",row), Filters.eq("indexColumn",column)), pieceForMongo(blank));
	}
	
	/*No se usan*/
	@Override
	public void addNewPiece(int id, int indexRow, int indexCol, String imagePath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNewPiece(int id, int indexRow, int indexCol) {
		// TODO Auto-generated method stub
		
	}
	
	/*Metodo que guarda en un fichero XML la partida*/
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
			aux = new Element("piece").setText("" + findPiece(i).getId());
			boardElements.addContent(aux);
		}

		Element movements = new Element("movements");
		movements.addContent(undoMovements);
		movements.addContent(redoMovements);

		Element save2 = new Element("game");

		save2.addContent(conf.detach());
		save2.addContent(boardElements);
		save2.addContent(movements);
		
		/*Se importa asi porque si no colisionan las dependencias de codigo*/
		org.jdom2.Document d = new org.jdom2.Document(save2);
		FileWriter writer = new FileWriter("partida.xml");
		XMLOutputter c = new XMLOutputter();
		c.setFormat(Format.getPrettyFormat());
		c.output(d, writer);
		c.output(d, System.out);
		writer.close();
	}
	/*Carga la partida desde un fichero XML*/
	@Override
	public void loadGame(String f, PuzzleController p) {
		String userDir = System.getProperty("user.dir") + File.separator;
		String fileSeparator = System.getProperty("file.separator");

		String file = f;
		File ff = new File(userDir + fileSeparator + file);
		SAXBuilder builder = new SAXBuilder();
		org.jdom2.Document d = null;
		Element root = null;

		try {
			d = (org.jdom2.Document) builder.build(ff);
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
	/*Comprueba si el puzle esta resuelto*/
	@Override
	public boolean isPuzzleSolve() {
		
		int previous = -1;
		
		for (int i=0;i<this.nElements;i++) {
			PieceModel p=this.findPiece(i);
			if (previous != -1 && p.getId() != previous + 1) {
				return false;
			}
			previous = p.getId();
		}
		return true;
		
		
	}
	/*Calcula un movimiento aleatorio para el tablrero*/
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
	/*Obtiene el tamaño de la base de datos MongoDB para trabajar la cuestion de rendimiento*/
	public Double getCollSize(){
		
		Document stats = db.runCommand(new Document("collStats", "board"));
		double memoryUsage = stats.getInteger("size");
		return memoryUsage;
	}
	
	/*Getters y Setters*/
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
