package TED_test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PFont;
import processing.data.Table;
import processing.data.TableRow;
import processing.event.MouseEvent;

import com.opencsv.CSVReader;

import controlP5.Bang;
import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.ListBox;
import controlP5.ListBoxItem;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Toggle;

public class TED extends PApplet {
	// Static
	public static int lastMouseButton = 0;
	public static boolean guiEvent = false;
	public static String search;
	public static HashMap<String, ArrayList<String>> topic_vdieo = new HashMap<String, ArrayList<String>>();
	public static ArrayList<String> videoSet;

	private static ListBoxItem[] vdieo;
	private static ListBox list2;
	public static void showVideoList(ArrayList<String> videos){
		list2.clear();
		vdieo = new ListBoxItem[videos.size()];
		for(int i = 0; i < vdieo.length; i++){
			vdieo[i] = list2.addItem(videos.get(i), i+1);
		}
	}
	
	// data file
	private Table table;

	// the graph
	private TEDGraph myTEDGraph;

	// ------ ZOOM ------
	private float zoom = 0.30f;
	private boolean autoZoom = true;

	// spring
	private float springLength = 500.0f;
	private float springStiffness = 0.25f;
	private float springDamping = 0.9f;

	// node
	//private int resultCount = 10;
	private float nodeRadius = 200.0f;
	private float nodeStrength = 15.0f;
	private float nodeDamping = 0.5f;
	private float minNodeDiameter = 1.0f;
	//private boolean colorizeNodes = true;
	// float nodeDiameterFactor = 20;
	// float nodeColor = 1;

	// change background & viewport
	private boolean invertBackground = false;
	private boolean drawFishEyed = false;

	// edge
	private float lineWeight = 1.0f;
	private float lineAlpha = 100.0f;
	private float linkColor = 0.89f;

	private boolean showText = true;
	private boolean showRolloverText = true;
	private boolean showRolloverNeighbours = false;

	// PApplet thisPApplet = this;
	
	

	// ------ mouse interaction ------
	private boolean dragging = false;
	private float offsetX = 0, offsetY = 0, clickX = 0, clickY = 0,
			clickOffsetX = 0, clickOffsetY = 0;

	// ------ GUI ------
	private ControlP5 controlP5;
	private boolean GUI = false;
	private ListBox list;
	private Textfield textfield;
	private Slider[] sliders;
	private Range[] ranges;
	private Toggle[] toggles;
	private Bang[] bangs;
	private int recommendIndex = 0;
	private int videoIndex = 0;
	// ------ image output ------
	private boolean saveOneFrame = false;
	private boolean savePDF = false;
	
	public static void main(String args[]){
		PApplet.main(new String[]{TED_test.TED.class.getName()});
		
	}

	public void setup() {
		size(1200, 750);
		myTEDGraph = new TEDGraph(this);
		loadVideoData();
		SocialData();
		setupGUI();
		guiEvent = false;
		smooth();
		noStroke();
		// make window resizable
		// frame.setResizable(true);
	}

	public void draw() {
		
		if (savePDF) {
			beginRecord(PDF, timestamp() + ".pdf");
			myTEDGraph.freezeTime = true;
		}

		// ------ white/black background ------
		int bgColor = color(255);
		if (invertBackground) {
			bgColor = color(0);
		}
		background(bgColor);

		// ------ update zooming and position of graph ------
		myTEDGraph.autoZoom = autoZoom;
		if (autoZoom) {
			boolean tmpGuiEvent = guiEvent;
			controlP5.controller("zoom").setValue(myTEDGraph.getZoom());
			guiEvent = tmpGuiEvent;
		} else {
			myTEDGraph.setZoom(zoom);
			// canvas dragging
			if (dragging) {
				myTEDGraph.setOffset(clickOffsetX + (mouseX - clickX),
						clickOffsetY + (mouseY - clickY));
			}
		}
		// ------ update and draw graph ------
		//myTEDGraph.setResultCount(resultCount);

		myTEDGraph.setSpringLength(springLength);
		myTEDGraph.setSpringStiffness(springStiffness);
		myTEDGraph.setSpringDamping(springDamping);

		myTEDGraph.setNodeRadius(nodeRadius);
		myTEDGraph.setNodeStrength(-nodeStrength);
		myTEDGraph.setNodeDamping(nodeDamping);

		myTEDGraph.setInvertBackground(invertBackground);
		myTEDGraph.setDrawHyperbolic(drawFishEyed);

		myTEDGraph.setLineWeight(lineWeight);
		myTEDGraph.setLineAlpha(lineAlpha);
		myTEDGraph.setLinkColor(linearColor(linkColor));

		myTEDGraph.setMinNodeDiameter(minNodeDiameter);
		// myTEDGraph.setNodeDiameterFactor(nodeDiameterFactor);
		//myTEDGraph.setColorizeNodes(colorizeNodes);

		myTEDGraph.setShowText(showText);
		myTEDGraph.setShowRolloverText(showRolloverText);
		myTEDGraph.setShowRolloverNeighbours(showRolloverNeighbours);

		myTEDGraph.update();
		myTEDGraph.draw();
		// ------ image output ------
		if (savePDF) {
			savePDF = false;
			println("saving to pdf – finishing");
			endRecord();
			println("saving to pdf – done");
			myTEDGraph.freezeTime = false;
		}
		if (saveOneFrame == true) {
			saveFrame(timestamp() + ".png");
			saveOneFrame = false;
		}
		drawGUI();
	}

	int linearColor(float theValue) {
		pushStyle();
		colorMode(HSB, 360, 100, 100, 100);
		float h = (theValue) * 360;
		float s = abs(sin((1 - theValue) * PI * 9)) * 60 + (1 - theValue) * 40;
		float b = abs(cos((float) ((1 - theValue) * PI * 4.5))) * 60 + (theValue) * 40;
		int newColor = color(h, s, b);
		popStyle();
		return newColor;
	}

	// ------ key and mouse events ------

	public void keyPressed() {
		if (!myTEDGraph.editing) {
			if (key == 'm' || key == 'M') {
				GUI = controlP5.group("menu").isOpen();
				GUI = !GUI;
			}
			if (GUI)
				controlP5.group("menu").open();
			else
				controlP5.group("menu").close();

			/*if (key == '1') {
				colorizeNodes = !colorizeNodes;
				Toggle t = (Toggle) controlP5.controller("colorizeNodes");
				t.setState(colorizeNodes);
			}*/
			if (key == '2') {
				drawFishEyed = !drawFishEyed;
				Toggle t = (Toggle) controlP5.controller("drawFishEyed");
				t.setState(drawFishEyed);
			}
			if (keyCode == UP)
				zoom += 0.05;
			if (keyCode == DOWN)
				zoom -= 0.05;
			zoom = max(zoom, 0.1f);

			if (key == 's' || key == 'S') {
				saveOneFrame = true;
			}
			if (key == 'p' || key == 'P') {
				savePDF = true;
				println("saving to pdf - starting (this may take some time)");
			}
		}
	}

	public void mousePressed() {
		lastMouseButton = mouseButton;
		if (!guiEvent) {
			//SocialData();
			// tell graph that mouse was pressed
			boolean eventHandled = myTEDGraph.mousePressed(); 
			// if the graph didn't do anything with the mouse event
			if (!eventHandled) {
				// canvas dragging
				if (mouseButton == RIGHT) {
					dragging = true;
					clickX = mouseX;
					clickY = mouseY;
					clickOffsetX = myTEDGraph.offset.x;
					clickOffsetY = myTEDGraph.offset.y;
				}
			}
		}
	}

	public void mouseReleased() {
		if (!guiEvent) {
			boolean eventHandled = myTEDGraph.mouseReleased();
		}
		guiEvent = false;
		dragging = false;
	}

	public void mouseEntered(MouseEvent e) {
		loop();
	}

	public void mouseExited(MouseEvent e) {
		//noLoop();
	}

	String timestamp() {
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS",Calendar.getInstance());
	}
	// //////////data process////////////
	public void SocialData() {
		myTEDGraph.makeDataStructure();
		table = loadTable("./data/link.csv", "header");
		int i = 0;
		float diameter = 60.0f;
		float bx, ax;
		float by, ay;
		float temp_x,temp_y;
		final int value = 10;
		for (TableRow row : table.rows()) {
			bx = random(-(value + i), value + i);
			by = random(-(value + i), value + i);
			i = i + 10;
			diameter = diameter - 0.15f;
			ax = random(-(value + i), value + i);
			ay = random(-(value + i), value + i);
			temp_x = ax;
			temp_y = ay;
			
			String nodeTitle = row.getString("vertex");
			int id = row.getInt("group_id");
			float beteweeness = row.getFloat("beteweeness");
			if (!nodeTitle.equals("")) {
				if(bx<0){bx = -1*bx;}
				if(by<0){by = -1*by;}
				if(ax<0){ax = -1*ax;}
				if(ay<0){ay = -1*ay;}
				while ((bx < ax) || (by < ay)) {
					ax = random(-(value + i), value + i);
					ay = random(-(value + i), value + i);
					temp_x = ax;
					temp_y = ay;
					if(ax<0){ax = -1*ax;}
					if(ay<0){ay = -1*ay;}
				}
				//myTEDGraph.addNode(id, nodeTitle, temp_x, temp_y, beteweeness/10);
				myTEDGraph.addNode(id, nodeTitle, temp_x, temp_y, diameter);
			}
			
		}
		for (TableRow row : table.rows()) {
			String fromTitle = row.getString("from");
			String toTitle = row.getString("to");
			myTEDGraph.addSpring(fromTitle, toTitle);
		}
	}
	public void recommendData(int select) {
		if(select == 0){
			return;
		}
		myTEDGraph.makeDataStructure();
		CSVReader reader = null;
		try {
			//reader = new CSVReader(new FileReader("feature.csv"));
			reader = new CSVReader(new FileReader("./data/TopicCpt_update.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int rangeFactor = 0;
		//float diameter = 60.0f;
		float bx, ax;
		float by, ay;
		float temp_x,temp_y;
		final int value = 2;
		String[] nextLine;
		String nodeTitle;
		int nodeId;
		float nodeProbability;
		try {
			while ((nextLine = reader.readNext()) != null) {
				if(rangeFactor == 0){
					rangeFactor = rangeFactor + 30;
				}else{
					bx = random(-(value + rangeFactor), value + rangeFactor);
					by = random(-(value + rangeFactor), value + rangeFactor);
					rangeFactor = rangeFactor + 30;
					ax = random(-(value + rangeFactor), value + rangeFactor);
					ay = random(-(value + rangeFactor), value + rangeFactor);
					temp_x = ax;
					temp_y = ay;
					nodeTitle = nextLine[3*select-3];
					nodeId = parseInt(nextLine[3*select-2]);
					nodeProbability = parseFloat(nextLine[3*select-1]);
					//System.out.println(nodeTitle+", "+nodeProbability);
					nodeProbability = nodeProbability * 100;
					if (!nodeTitle.equals("")) {
						if(bx<0){bx = -1*bx;}
						if(by<0){by = -1*by;}
						if(ax<0){ax = -1*ax;}
						if(ay<0){ay = -1*ay;}
						while ((bx < ax) || (by < ay)) {
							ax = random(-(value + rangeFactor), value + rangeFactor);
							ay = random(-(value + rangeFactor), value + rangeFactor);
							temp_x = ax;
							temp_y = ay;
							if(ax<0){ax = -1*ax;}
							if(ay<0){ay = -1*ay;}
						}
						myTEDGraph.addNode(nodeId, nodeTitle, temp_x, temp_y, nodeProbability);
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void loadVideoData(){
		CSVReader reader2 = null;
		try {
			reader2 = new CSVReader(new FileReader("./data/video.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String[] nextLine;
		String video;
		String topic;
		String s = null;
		try {
			while ((nextLine = reader2.readNext()) != null) {
				video = nextLine[0];
				topic = nextLine[1];
				if(topic_vdieo.get(topic) == null){
					videoSet = new ArrayList<String>();
					videoSet.add(video);
					topic_vdieo.put(topic, videoSet);
				}else{
					ArrayList<String> temp = topic_vdieo.get(topic);
					temp.add(video);
				}
			}
			System.out.println(topic_vdieo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * exploring TED
	 *
	 * MOUSE left click : show links for the clicked node double left click :
	 * create node shift + left click : remove node alt + left click : show
	 * backlinks for the clicked node right click + drag : drag canvas or node
	 * double right click : open article in browser
	 *
	 * KEYS 1 : toggle colorize nodes 2 : toggle fish eye view arrow up/down :
	 * zoom m : menu open/close s : save png p : save pdf
	 */

	public void setupGUI() {
		PFont font = createFont("arial", 40);
		//PFont font = createFont("./data/miso-regular.ttf", 40);
		int activeColor = color(0, 130, 164);
		controlP5 = new ControlP5(this);
		controlP5.setColorActive(activeColor);// toggle select
		controlP5.setColorBackground(color(170));// bar background
		controlP5.setColorForeground(color(50));// bar foregronud
		controlP5.setColorLabel(color(0));// label color
		controlP5.setColorValue(color(255));// value color

		ControlGroup ctrl = controlP5.addGroup("menu", 15, 25, 35);
		ctrl.activateEvent(true);
		ctrl.setColorLabel(color(255));
		ctrl.open();// menu open

		sliders = new Slider[30];
		ranges = new Range[30];
		toggles = new Toggle[30];
		bangs = new Bang[30];
		int left = 0;
		int top = 5;
		int len = 300;

		int si = 0;
		int ri = 0;
		int ti = 0;// toggle index
		int bi = 0;
		int posY = 0;

		toggles[ti] = controlP5.addToggle("autoZoom", autoZoom, left, top + posY, 15, 15);
		toggles[ti++].setLabel("Adjust Zoom Automatically");
		sliders[si++] = controlP5.addSlider("zoom", 0.05f, 1, left, top + posY + 20, len, 15);
		// java.lang.String theName, float theMin, float theMax, int theX, int
		// theY, int theWidth, int theHeight
		posY += 50;
		
		sliders[si++] = controlP5.addSlider("springLength", 10, 500, left, top + posY, len, 15);
		sliders[si++] = controlP5.addSlider("springStiffness", 0, 1, left, top + posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("springDamping", 0, 1, left, top + posY + 40, len, 15);
		posY += 70;

		sliders[si++] = controlP5.addSlider("nodeRadius", 0, 500, left, top + posY + 0, len, 15);
		sliders[si++] = controlP5.addSlider("nodeStrength", 0, 50, left, top + posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("nodeDamping", 0, 1, left, top + posY + 40, len, 15);
		posY += 70;

		toggles[ti] = controlP5.addToggle("invertBackground", invertBackground, left + 0, top + posY, 15, 15);
		toggles[ti++].setLabel("Invert Background");
		toggles[ti] = controlP5.addToggle("drawFishEyed", drawFishEyed, left + 150, top + posY, 15, 15);
		toggles[ti++].setLabel("Draw Fish-Eyed");
		sliders[si++] = controlP5.addSlider("lineWeight", 1, 20, left, top + posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("lineAlpha", 0, 100, left, top + posY + 40, len, 15);
		sliders[si++] = controlP5.addSlider("linkColor", 0, 1, left, top + posY + 60, len, 15);
		posY += 90;
		
		sliders[si++] = controlP5.addSlider("minNodeDiameter", 1.0f, 10.0f, left, top + posY + 20, len, 15);
		posY += 70;

		toggles[ti] = controlP5.addToggle("showText", showText, left, top + posY, 15, 15);
		toggles[ti++].setLabel("Show Text");
		posY += 70;
		
		textfield = controlP5.addTextfield("search").setPosition(left+10,posY).setSize(200,40).setFont(font);
		posY += 100;
		
		list = controlP5.addListBox("myList").setPosition(left+10, posY).setSize(200, 200).setItemHeight(30).setBarHeight(30);
		list2 = controlP5.addListBox("Video").setPosition(width-300, 150).setSize(300, 300).setItemHeight(50).setBarHeight(100);
		
		for (int i = 0; i < si; i++) {
			sliders[i].setGroup(ctrl);
			sliders[i].captionLabel().toUpperCase(true);
			sliders[i].captionLabel().style().padding(4, 3, 3, 3);
			sliders[i].captionLabel().style().marginTop = -4;
			sliders[i].captionLabel().style().marginLeft = 0;
			sliders[i].captionLabel().style().marginRight = -14;
			sliders[i].captionLabel().setColorBackground(0x99ffffff);
		}
		for (int i = 0; i < ri; i++) {
			ranges[i].setGroup(ctrl);
			ranges[i].captionLabel().toUpperCase(true);
			ranges[i].captionLabel().style().padding(4, 3, 3, 3);
			ranges[i].captionLabel().style().marginTop = -4;
			ranges[i].captionLabel().setColorBackground(0x99ffffff);
		}
		for (int i = 0; i < ti; i++) {
			toggles[i].setGroup(ctrl);
			toggles[i].setColorLabel(color(50));
			toggles[i].captionLabel().style().padding(4, 3, 3, 3);
			toggles[i].captionLabel().style().marginTop = -20;
			toggles[i].captionLabel().style().marginLeft = 18;
			toggles[i].captionLabel().style().marginRight = 5;
			toggles[i].captionLabel().setColorBackground(0x99ffffff);
		}
		for (int i = 0; i < bi; i++) {
			bangs[i].setGroup(ctrl);
			bangs[i].setColorLabel(color(50));
			bangs[i].captionLabel().style().padding(4, 3, 3, 3);
			bangs[i].captionLabel().style().marginTop = -20;
			bangs[i].captionLabel().style().marginLeft = 33;
			bangs[i].captionLabel().style().marginRight = 5;
			bangs[i].captionLabel().setColorBackground(0x99ffffff);
		}
		textfield.setGroup(ctrl);
		list.setGroup(ctrl);
		list.captionLabel().toUpperCase(true);
		list.captionLabel().getFont().setSize(15);
		list.captionLabel().set("Recommander");
		list.captionLabel().style().marginTop = 3;
		list.valueLabel().style().marginTop = 3;

		ListBoxItem lbi11 = list.addItem("Fruchterman Reingold layout", 11);
		ListBoxItem lbi1 = list.addItem("Funny", 1);
		ListBoxItem lbi2 = list.addItem("Jaw-dropping", 2);
		ListBoxItem lbi3 = list.addItem("Ingenious", 3);
		ListBoxItem lbi4 = list.addItem("Inspiring", 4);
		ListBoxItem lbi5 = list.addItem("Beautiful", 5);
		ListBoxItem lbi6 = list.addItem("Persuasive", 6);
		ListBoxItem lbi7 = list.addItem("Courageous", 7);
		ListBoxItem lbi8 = list.addItem("Longwinded", 8);
		ListBoxItem lbi9 = list.addItem("Informative", 9);
		ListBoxItem lbi10 = list.addItem("Fascinating", 10);
		
		list2.setGroup(ctrl);
		list2.captionLabel().toUpperCase(true);
		list2.captionLabel().getFont().setSize(20);
		list2.captionLabel().set("Talk's List");
		list2.captionLabel().style().marginTop = 20;
		list2.captionLabel().style().marginLeft = 30;
		
	}
	
	public void drawGUI() {
		controlP5.show();
		controlP5.draw();
	}

	public void controlEvent(ControlEvent theControlEvent) {
		guiEvent = true;
		GUI = controlP5.group("menu").isOpen();
		if (theControlEvent.isAssignableFrom(Textfield.class)) {
			println("controlEvent: accessing a string from controller '"
					+ theControlEvent.getName() + "': "
					+ theControlEvent.getStringValue());
		}
		if (theControlEvent.isGroup() && theControlEvent.name().equals("myList")) {
			recommendIndex = (int) theControlEvent.group().value();
			int test = (int) theControlEvent.group().value();
			//println("test " + test + ", " + recommendIndex);
			if(recommendIndex == 11){
				SocialData();
			}else{
				recommendData(recommendIndex);
			}
			
		}
		if(theControlEvent.isGroup() && theControlEvent.name().equals("Video")){
			videoIndex = (int) theControlEvent.group().value();
			
			println(vdieo[videoIndex-1].getName());
			try{
				link("http://www.ted.com/search?cat=talks&per_page=12&q="
						+ vdieo[videoIndex-1].getName().replace(" ", "+"));
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}

	public void invertBackground() {
		guiEvent = true;
		invertBackground = !invertBackground;
		updateColors(invertBackground);
	}

	public void updateColors(boolean stat) {
		ControllerGroup ctrl = controlP5.getGroup("menu");
		if (stat == false) {
			textfield.setColorLabel(color(50));
			textfield.captionLabel().setColorBackground(0x99ffffff);
		} else {
			textfield.setColorLabel(color(200));
			textfield.captionLabel().setColorBackground(0x99000000);
		}
		for (int i = 0; i < sliders.length; i++) {
			if (sliders[i] == null)
				break;
			if (stat == false) {
				sliders[i].setColorLabel(color(50));
				sliders[i].captionLabel().setColorBackground(0x99ffffff);
			} else {
				sliders[i].setColorLabel(color(200));
				sliders[i].captionLabel().setColorBackground(0x99000000);
			}
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] == null)
				break;
			if (stat == false) {
				ranges[i].setColorLabel(color(50));
				ranges[i].captionLabel().setColorBackground(0x99ffffff);
			} else {
				ranges[i].setColorLabel(color(200));
				ranges[i].captionLabel().setColorBackground(0x99000000);
			}
		}
		for (int i = 0; i < toggles.length; i++) {
			if (toggles[i] == null)
				break;
			if (stat == false) {
				toggles[i].setColorLabel(color(50));
				toggles[i].captionLabel().setColorBackground(0x99ffffff);
			} else {
				toggles[i].setColorLabel(color(200));
				toggles[i].captionLabel().setColorBackground(0x99000000);
			}
		}
		for (int i = 0; i < bangs.length; i++) {
			if (bangs[i] == null)
				break;
			if (stat == false) {
				bangs[i].setColorLabel(color(50));
				bangs[i].captionLabel().setColorBackground(0x99ffffff);
			} else {
				bangs[i].setColorLabel(color(200));
				bangs[i].captionLabel().setColorBackground(0x99000000);
			}
		}
	}

}
