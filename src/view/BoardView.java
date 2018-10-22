package view;

import observer.Observer;

import javax.swing.*;

import model.PieceModel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa la vista del tablero
 * 
 * @author Miguel Ã�ngel
 * @version 1.0
 */
public class BoardView extends JPanel implements Observer {
	public static int imageWidth;
	public static int imageHeight;

	int offsetX;
	int offsetY;

	private int rowNum, columnNum;
	private List<PieceView> iconArray = null;

	public BoardView(int rowNum, int columnNum, int imageSize, String[] imageList) {
		super();
		this.rowNum = rowNum;
		this.columnNum = columnNum;

		this.imageWidth = imageSize * columnNum;
		this.imageHeight = imageSize * rowNum;

		iconArray = new ArrayList<>();

		int posX = 0, posY = 0;
		int drawIndexRow = 0, drawIndexColumn = 0;

		for (int i = 0; i < imageList.length; i++) {

			int indexX = i % columnNum;
			int indexY = i / columnNum;

			drawIndexRow = (indexX) * imageSize;
			drawIndexColumn = (indexY) * imageSize;

			PieceView p = new PieceView(i, indexX, indexY, imageSize, imageList[i]);
			p.setDrawIndexRow(drawIndexRow);
			p.setDrawIndexColumn(drawIndexColumn);
			iconArray.add(p);
		}

	}


	// redimensionamos la imagen para 96*96
	private BufferedImage resizeImage(File fileImage) {
		BufferedImage resizedImage = null;

		return (resizedImage);
	}

	// dividimos la imagen en el nÃºmero
	private BufferedImage[] splitImage(BufferedImage image) {

		// TODO Test
		int chunks = rowNum * columnNum;

		int chunkWidth = image.getWidth() / columnNum;
		int chunkHeight = image.getHeight() / rowNum;
		int count = 0;
		BufferedImage images[] = new BufferedImage[chunks];

		for (int i = 0; i < rowNum; i++) {
			for (int z = 0; z < columnNum; z++) {
				images[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

				Graphics2D gr = images[count++].createGraphics();
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * z, chunkHeight * i,
						chunkWidth * z + chunkWidth, chunkHeight * i + chunkHeight, null);
				gr.dispose();
			}
		}
		return (images);
	}
	
	//Actualiza el estado de la mesa
	public void update(int blankPos, int movedPos) {

		PieceView blank = iconArray.get(blankPos);
		PieceView moved = iconArray.get(movedPos);

		blank.setIndexRow(movedPos % columnNum);
		blank.setIndexColumn(movedPos / columnNum);

		moved.setIndexRow(blankPos % columnNum);
		moved.setIndexColumn(blankPos / columnNum);

		iconArray.set(blankPos, moved);
		iconArray.set(movedPos, blank);

		update(this.getGraphics());
	}
	
	//Actualiza la el pintado de la pantalla
	public void update(Graphics g) {
		paint(g);
	}

	//Pinta todos los elementos de la pantalla
	@Override
	public void paint(Graphics g) {
		offsetX = (this.getWidth() - imageWidth) / 2;
		offsetY = (this.getHeight() - imageHeight) / 2;
		for (PieceView iconImage : iconArray) {
			g.drawImage(iconImage.getImage(), iconImage.getDrawIndexRow() + offsetX,
					iconImage.getDrawIndexColumn() + offsetY, iconImage.getImageSize(), iconImage.getImageSize(), this);

		}
		g.setColor(Color.black);
		g.fillRect(offsetX, 265, imageWidth, 20);
	}
	
	
	//Pinta el texto de partida ganada
	public void activateWin() {
		Graphics g = this.getGraphics();
		int centerX = (this.getWidth() - 92) / 2;
		int centerY = (this.getHeight() + 150) / 2;
		g.setFont(new Font("Times New Roman", Font.BOLD, 20));
		g.setColor(Color.white);
		g.drawString("YOU WIN!", centerX, centerY);
	}
	
	//Pinta las estadisticas de rendimiento de la partida
	public void showStats(Long mongoSolveTime, Double mongoSize, Long baseXSolveTime, Long baseXSize) {
		Graphics g = this.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 270, 100);
		g.setFont(new Font("Times New Roman", Font.BOLD, 20));
		g.setColor(Color.white);
		g.drawString("Mongo solve time "+mongoSolveTime+" millis", 20, 20);
		g.drawString("Mongo DB size "+mongoSize+" bytes", 20, 40);
		g.drawString("BaseX solve time "+baseXSolveTime+" millis", 20, 70);
		g.drawString("BaseX DB size "+baseXSize+" bytes", 20, 90);
		
	}
	
	//Limpia el texto de partida ganada repintando la ventana
	public void clearWin() {
		Graphics g  = this.getGraphics();
		g.setColor(Color.black);
		g.fillRect(offsetX, 265, imageWidth, 20);
	}

	// Dado una posicion X e Y en pixeles localizar una pieza y devolver su
	// posicion
	// en el vector
	public int locatePiece(int posX, int posY) {

		int x = posX - offsetX;
		int y = posY - offsetY;

		int coordX = x / (imageWidth / columnNum);
		int coordY = y / (imageHeight / rowNum);

		return coordX + (coordY * columnNum);
	}

	/**
	 * Mueve la pieza y devuelve las coordenadas en un array de dos posiciones
	 * donde: la primera posicion representa la posicion actual de la pieza
	 * blanca y la segunda posicion representa la posicion actual de la pieza a
	 * mover.
	 * 
	 * @param posX
	 *            posicion X en pixeles del puntero
	 * @param posY
	 *            posicion Y en pixeles del puntero.
	 * @return Array de dos posiciones: posicion actual de la pieza blanca y
	 *         posicion actual de la pieza que tiene que ser movida.
	 */
	public int[] movePiece(int posX, int posY) {

		int blank = -1;
		int piece = locatePiece(posX, posY);

		int[] aux = new int[2];
		aux[1] = piece;

		int i = 0;
		while (blank == -1) {
			if (iconArray.get(i).getId() == 0) {
				blank = i;
			}
			i++;
		}

		if (blank + 1 == piece || blank - 1 == piece || blank + rowNum == piece || blank - rowNum == piece) {
			aux[0] = blank;

			for (int z = 0; z < columnNum - 1; z++) {
				if ((blank == (z * columnNum) + 2 && piece == (z * columnNum) + 3)
						|| (blank == (z * columnNum) + 3 && piece == (z * columnNum) + 2)) {
					aux[0] = -1;
				}
			}
		} else {
			aux[0] = -1;
		}
		return aux;
	}

	@Override
	public String toString() {
		String aux = "";
		for (int i = 0; i < iconArray.size(); i++) {
			aux += ", " + iconArray.get(i);
		}
		return aux;
	}

}
