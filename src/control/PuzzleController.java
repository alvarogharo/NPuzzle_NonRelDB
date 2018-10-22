package control;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import command.Command;
import command.MovementCommand;
import model.AbstractModel;
import model.BaseXModel;
import model.BoardModel;
import model.MongoDBModel;
import observer.Observer;
import view.BoardView;
import view.PuzzleGUI;

public class PuzzleController extends AbstractController {

	private BoardView boardView;
	private AbstractModel boardModel;
	
	private MongoDBModel mongoModel;
	private BaseXModel baseXModel;
	
	private Stack<Command> undoStack = new Stack<>();
	private Stack<Command> redoStack = new Stack<>();
	private boolean firstInteraction = false;
	private boolean solved = false;
	private boolean solving = false;
	private boolean undoing = false;
	private boolean shuffling = false;
	
	private long mongoSolveTime;
	private long baseXSolveTime;

	private double mongoSize;
	private long baseXSize;
	
	private int shuffleNum = 0;
	private Element config;

	public PuzzleController(Element cnf) {
		this.config = cnf;
		System.out.println(this.config);
	}
	
	//Genera movimientos aleatorios tantas veces como esten configuradas
	public void shuffle(int shuffleNum) {
		int lastPos = 0;
		int actualPos = 0;
		this.shuffleNum = shuffleNum;
		for (int i = 0; i < shuffleNum; i++) {
			shuffling = true;
			int[] aux = boardModel.getRandomMovement(lastPos, actualPos);
			notifyObservers(aux[0], aux[1]);

			lastPos = aux[0];
			actualPos = aux[1];
		}
		shuffling = false;
	}
	
	//Manda el movimiento pasado a todos los observers añadidos a a lista
	@Override
	public void notifyObservers(int blankPos, int movedPos) {
		if (!solved) {
			
			//Undoing se usa para evitar crear movimientos duplicados en la stack de undo cuando estas haciendo un undo
			if (!undoing) {

				int[] aux = { movedPos, blankPos };
				Command auxC = new MovementCommand(aux, this);
				this.undoStack.push(auxC);

			}
			
			for (Observer item : observerList) {
				
				//Condicionales si se ha dado a resolver y las clases son mongo o basex para medir el rendimiento
				if (solving && item.getClass().equals(MongoDBModel.class)){
					long aux = System.currentTimeMillis();
					item.update(blankPos, movedPos);
					long aux2 = System.currentTimeMillis();
					long result = aux2-aux;
					mongoSolveTime += result;
				}if (solving && item.getClass().equals(BaseXModel.class)){
					long aux = System.currentTimeMillis();
					item.update(blankPos, movedPos);
					long aux2 = System.currentTimeMillis();
					long result = aux2-aux;
					baseXSolveTime += result;
				}else{
					item.update(blankPos, movedPos);
				}
			}
			if (solving){
				
				//Pinta por consola los valores de rendimiento
				System.out.println("BASEX: "+baseXSolveTime);
				
				baseXSize = baseXModel.getFileSize();
				System.out.println("BASEX SIZE: "+baseXSize);
				
				System.out.println("MONGO: "+mongoSolveTime);
				
				mongoSize = mongoModel.getCollSize();
				System.out.println("MONGO SIZE: "+mongoSize);
				
				boardView.showStats(mongoSolveTime, mongoSize, baseXSolveTime, baseXSize);
				
			}
			
			
			firstInteraction = true;
		}
		
		//En caso de que no se este desordenando, no sea la primera iteracion y el puzzle este resuelto se llama a puintar win
		if (!shuffling && firstInteraction && boardModel.isPuzzleSolve()) {
			boardView.activateWin();
			System.out.println("Win!");
			solved = true;
		}
	}
	
	//Maneja la utilizacion de los distintos botones de Jframe
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e.getActionCommand());
		if (e.getActionCommand().equals("save")) {

			try {
				boardModel.save(undoStack, redoStack, config);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else if (e.getActionCommand().equals("load")) {

		loadGame();

		} else if (e.getActionCommand().equals("undo")) {

			undoing = true;
			if (!undoStack.isEmpty()) {
				Command aux = undoStack.pop();
				redoStack.push(aux);
				aux.execute();
			}
			undoing = false;

		} else if (e.getActionCommand().equals("redo")) {

			undoing = true;
			if (!redoStack.isEmpty()) {
				Command aux = redoStack.pop();
				undoStack.push(aux);
				aux.execute();
			}
			undoing = false;

		} else if (e.getActionCommand().equals("clutter")) {

			setSolved(false);
			shuffle(shuffleNum);
			boardView.clearWin();

		} else if (e.getActionCommand().equals("solve")) {
			mongoSolveTime = 0;
			baseXSolveTime = 0;
			
			solving = true;
			while (!undoStack.isEmpty()) {
				undoing = true;
				undoStack.pop().execute();
				undoing = false;
			}
			undoStack.clear();
			redoStack.clear();
			solving = false;

		} else if (e.getActionCommand().equals("exit")) {

			System.exit(0);

		}
	}
	
	//Maneja los clicks en el raton
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		super.mouseClicked(e);

		int[] aux = boardView.movePiece(e.getX(), e.getY());
		if (!redoStack.isEmpty()) {
			redoStack.clear();
		}
		if (aux[0] >= 0) {
			notifyObservers(aux[0], aux[1]);
		}
	}

	//Carga la partida
	private void loadGame() {
		
		//HACE TODA LA LISTA DE UNDO
		//VACIA UNDO Y REDO
		//setsolved(false)
		//HACE LOAD GAME

		boardModel.loadGame("partida.xml", this);

		Stack<Command> undo = (Stack<Command>) boardModel.getUndo().clone();
		Stack<Command> redo = (Stack<Command>) boardModel.getRedo().clone();
		
		for(Command c : undo){
			System.out.println(c.getMovement()[0]+", "+c.getMovement()[1]);
		}

		while (!undoStack.isEmpty()) {
			undoing = true;
			undoStack.pop().execute();
			undoing = false;
		}
		

		setSolved(false);
		undoStack.clear();
		
		while (!undo.isEmpty()) {
			int[] aux = undo.get(0).getMovement();
			undo.remove(0);
			System.out.println(aux[0]+", "+aux[1]);
			notifyObservers(aux[1], aux[0]);
		}

		redoStack.clear();

		while (!redo.isEmpty()) {
			Command aux = redo.pop();
			redoStack.push(aux);
		}

	}
	
	//Añade un observer a la lista
	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		super.addObserver(observer);
	}
	
	//Elimina un observer de la lista
	@Override
	public void removeObserver(Observer observer) {
		// TODO Auto-generated method stub
		super.removeObserver(observer);
	}
	
	
	//Getters y setter
	public BoardView getBoardview() {
		return boardView;
	}

	public void setBoardview(BoardView boardView) {
		this.boardView = boardView;
	}

	public AbstractModel getBoardModel() {
		return boardModel;
	}

	public void setBoardModel(AbstractModel boardModel) {
		this.boardModel = boardModel;
	}

	public boolean isSolved() {
		return solved;
	}

	public void setSolved(boolean solved) {
		this.solved = solved;
	}

	public MongoDBModel getMongoModel() {
		return mongoModel;
	}

	public void setMongoModel(MongoDBModel mongoModel) {
		this.mongoModel = mongoModel;
	}

	public BaseXModel getBaseXModel() {
		return baseXModel;
	}

	public void setBaseXModel(BaseXModel baseXModel) {
		this.baseXModel = baseXModel;
	}

	public long getMongoSolveTime() {
		return mongoSolveTime;
	}

	public void setMongoSolveTime(long mongoSolveTime) {
		this.mongoSolveTime = mongoSolveTime;
	}

	public long getBaseXSolveTime() {
		return baseXSolveTime;
	}

	public void setBaseXSolveTime(long baseXSolveTime) {
		this.baseXSolveTime = baseXSolveTime;
	}

	public double getMongoSize() {
		return mongoSize;
	}

	public void setMongoSize(double mongoSize) {
		this.mongoSize = mongoSize;
	}

	public long getBaseXSize() {
		return baseXSize;
	}

	public void setBaseXSize(long baseXSize) {
		this.baseXSize = baseXSize;
	}

}
