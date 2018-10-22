package config;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

public class ConfigXML {
	private String userDir = System.getProperty("user.dir") + File.separator;
	private String file;
	private Document d;
	SAXBuilder builder;
	private File ff;

	private int imageSize;
	private int rowNumber;
	private int colNum;
	private List<String> imgNames;
	private int shuffle;
	
	/*Constructor del objeto encargado de la lectura del fichero de configuracion*/
	public ConfigXML(String f) throws JDOMException, IOException {
		this.file = f;
		ff = new File(this.file);
		this.builder = new SAXBuilder();
		d = (Document) this.builder.build(ff);
	}
	
	/*Metodo de lectura del fichero de configuracion*/
	public Element readXML() {
		Element e = (Element) d.getRootElement();
		e.getAttributes();
		String imageSizeS = e.getChildText("imageSize");
		imageSize = Integer.parseInt(imageSizeS);

		String rowN = e.getChildText("rowNum");
		rowNumber = Integer.parseInt(rowN);
		String colN = e.getChildText("columNum");
		colNum = Integer.parseInt(colN);
		String shuffleN = e.getChildText("shuffle");
		this.shuffle = Integer.parseInt(shuffleN);
		List<Element> imageList = e.getChildren("imageList").get(0).getChildren("imgName");
		ArrayList<String> imageListNames = new ArrayList();
		int i = 0;
		for (Element ef : imageList) {
			imageListNames.add(ef.getValue());
		}
		this.imgNames = imageListNames;
		return e;
	}

	/*getters y setters*/
	public int getShuffle() {
		return shuffle;
	}

	public void setShuffle(int shuffle) {
		this.shuffle = shuffle;
	}

	public int getImageSize() {
		return imageSize;
	}

	public void setImageSize(int imageSize) {
		this.imageSize = imageSize;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public int getColNum() {
		return colNum;
	}

	public void setColNum(int colNum) {
		this.colNum = colNum;
	}

	public List<String> getImgNames() {
		return imgNames;
	}

	public void setImgNames(List<String> imgNames) {
		this.imgNames = imgNames;
	}

	public String getUserDir() {
		return userDir;
	}

	public void setUserDir(String userDir) {
		this.userDir = userDir;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

}
