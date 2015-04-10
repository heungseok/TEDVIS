package TED_test;

import generativedesign.Spring;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import processing.data.XML;
import processing.event.MouseEvent;
import controlP5.Bang;
import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Toggle;

public class TED extends PApplet {
	// Static
	public static HashMap<String, ArrayList<String>> KeyTopic_Video = new HashMap<String, ArrayList<String>>();
	public static ArrayList<String> video;
	// public static ArrayList<String> parentsTitle;
	// public static ArrayList<TEDNode> parentsNode;
	public static int lastMouseButton = 0;
	public static boolean guiEvent = false;

	public static HashMap<String, ArrayList<TEDNode>> clusterManger = new HashMap<String, ArrayList<TEDNode>>();
	public static ArrayList<TEDNode> cluster;

	// data file
	private XML xml;
	private Table table;

	// the graph
	private TEDGraph myTEDGraph;
	private TEDNode MainNode;
	
	
	
	

	// ------ ZOOM ------
	private float zoom = 0.75f;
	private boolean autoZoom = false;

	// spring
	private float springLength = 100.0f;
	private float springStiffness = 0.4f;
	private float springDamping = 0.9f;

	// node
	private int resultCount = 10;
	private float nodeRadius = 200.0f;
	private float nodeStrength = 15.0f;
	private float nodeDamping = 0.5f;
	private float minNodeDiameter = 8.0f;
	private boolean colorizeNodes = true;
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
	private Slider[] sliders;
	private Range[] ranges;
	private Toggle[] toggles;
	private Bang[] bangs;

	// ------ image output ------
	private boolean saveOneFrame = false;
	private boolean savePDF = false;

	public void setup() {
		size(1850, 950);
		myTEDGraph = new TEDGraph(this);
		// makeTree();
		// LoadTopicData();
		// LoadClusterData();
		//SNA_Data();
		
		/**
		 * load rules data
		 * 2015.4.9
		 * Heungseok Park
		 */
		//Rules_Data_before_Gephi();
		Rules_Data_after_Gephi();
		
		setupGUI();
		guiEvent = false;
		smooth();
		noStroke();
		
		/*
		for (String key : clusterManger.keySet()) {
			 println(key);
		}
		 println(clusterManger.size());
		 */
		
		
		// println(clusterManger.get("live music"));
		// println(clusterManger.get("live music").size());
		// println(clusterManger.get("conducting*media*film*state-building*ted prize*photography"));
		// println(clusterManger.get("conducting*media*film*state-building*ted prize*photography").size());
		// make window resizable
		// frame.setResizable(true);

		// MainNode = (TEDNode) myTEDGraph.addNode(0, "TED", 100, 100);
		// parentsTitle = new ArrayList<String>();
		// parentsNode = new ArrayList<TEDNode>();
		// parentsTitle.add("TED");
		// parentsNode.add(MainNode);
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
		myTEDGraph.setResultCount(resultCount);

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
		myTEDGraph.setColorizeNodes(colorizeNodes);

		myTEDGraph.setShowText(showText);
		myTEDGraph.setShowRolloverText(showRolloverText);
		myTEDGraph.setShowRolloverNeighbours(showRolloverNeighbours);

		myTEDGraph.update();
		myTEDGraph.draw();
		// ------ image output ------
		if (savePDF) {
			savePDF = false;
			println("saving to pdf �� finishing");
			endRecord();
			println("saving to pdf �� done");
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
		float b = abs(cos((float) ((1 - theValue) * PI * 4.5))) * 60
				+ (theValue) * 40;
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

			if (key == '1') {
				colorizeNodes = !colorizeNodes;
				Toggle t = (Toggle) controlP5.controller("colorizeNodes");
				t.setState(colorizeNodes);
			}
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
			// tell graph that mouse was pressed
			boolean eventHandled = myTEDGraph.mousePressed(); // polymorphism
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
		// noLoop();
	}

	String timestamp() {
		return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS",
				Calendar.getInstance());
	}

	
	/*
	 * Data Process
	 * 
	 *  2015.04.09
	 * 	Heungseok Park. - Rules_Data_xxx()
	 */
		
	public void Rules_Data_before_Gephi(){
		
		table = loadTable("rules.csv", "header");
		
		for (TableRow row : table.rows()){
			
			String from = row.getString("from");
			StringTokenizer st = new StringTokenizer(from, ",");
			ArrayList <String> from_list = new ArrayList<String>();
			
			while(st.hasMoreTokens()){
				String temp = st.nextToken();
				from_list.add(temp);
				System.out.println(temp);
				
			}
			System.out.println("\n");
			 
			String to = row.getString("to");
			float temp_x = random(-2500, 2500);
			float temp_y = random(-2500, 2500);
			
			for(int i=0; i<from_list.size(); i++){
				
				myTEDGraph.addNode(1, from_list.get(i), temp_x, temp_y);
				myTEDGraph.addNode(2, to, temp_x, temp_y);
				myTEDGraph.addSpring(from_list.get(i), to);
				
			}
		}
	}
	
	
	public void Rules_Data_after_Gephi(){
		
		table = loadTable("gephi_edge.csv", "header");
		
		for (TableRow row : table.rows()){
			
			String from = row.getString("from");
			String to = row.getString("to");
			int from_module = row.getInt("from_module");
			int to_module = row.getInt("to_module");
			System.out.println(from + "," + from_module + "," + to + "," + to_module); 
			
			float temp_x = random(-2500, 2500);
			float temp_y = random(-2500, 2500);
			myTEDGraph.addNode(from_module, from, temp_x, temp_y);
			myTEDGraph.addNode(to_module, to, temp_x, temp_y);
		}
		
		for (TableRow row : table.rows()) {
			String fromTitle = row.getString("from");
			String toTitle = row.getString("to");
			myTEDGraph.addSpring(fromTitle, toTitle);
		}
		for (TableRow row : table.rows()) {
			String topic = row.getString("to");
			if (clusterManger.get(topic) != null) {
				continue;
			}
			TEDNode tempNode = myTEDGraph.nodeMap.get(topic);

			tempNode.set(random(-5000, 5000), random(-5000, 5000));
			cluster = new ArrayList<TEDNode>();
			cluster.add(tempNode);
			for (int i = 0; i < myTEDGraph.springs.size(); i++) {
				Spring tempSpring = myTEDGraph.springs.get(i);
				if (((TEDNode) tempSpring.toNode).equals(tempNode)) {
					tempSpring.fromNode.set(tempNode.x + random(-5, 5),
							tempNode.y + random(-5, 5));
					cluster.add((TEDNode) tempSpring.fromNode);
				}
			}
			clusterManger.put(topic, cluster);
		}
		//show relation between topic and video 
		

		
		
		
	}
	
	
	public void SNA_Data() {
		
		table = loadTable("SNA_data.csv", "header");
		
		for (TableRow row : table.rows()){
			
			String from = row.getString("from");
			String to = row.getString("to");
			float temp_x = random(-2500, 2500);
			float temp_y = random(-2500, 2500);
			myTEDGraph.addNode(1, from, temp_x, temp_y);
			myTEDGraph.addNode(2, to, temp_x, temp_y);
		}
		
		for (TableRow row : table.rows()) {
			String fromTitle = row.getString("from");
			String toTitle = row.getString("to");
			myTEDGraph.addSpring(fromTitle, toTitle);
		}
		for (TableRow row : table.rows()) {
			String topic = row.getString("to");
			if (clusterManger.get(topic) != null) {
				continue;
			}
			TEDNode tempNode = myTEDGraph.nodeMap.get(topic);

			tempNode.set(random(-5000, 5000), random(-5000, 5000));
			cluster = new ArrayList<TEDNode>();
			cluster.add(tempNode);
			for (int i = 0; i < myTEDGraph.springs.size(); i++) {
				Spring tempSpring = myTEDGraph.springs.get(i);
				if (((TEDNode) tempSpring.toNode).equals(tempNode)) {
					tempSpring.fromNode.set(tempNode.x + random(-5, 5),
							tempNode.y + random(-5, 5));
					cluster.add((TEDNode) tempSpring.fromNode);
				}
			}
			clusterManger.put(topic, cluster);
		}
		//show relation between topic and video 
		
		
		
		for (TableRow row : table.rows()) {
			String fromTitle = row.getString("cfrom");
			String toTitle = row.getString("cto");
			//myTEDGraph.addSpring(fromTitle, toTitle);
			String setA_name = null;
			String setB_name = null;
			ArrayList<TEDNode> setA = null;
			ArrayList<TEDNode> setB = null;
			TEDNode setA_node = null;
			TEDNode setB_node = null;
			for (String key : clusterManger.keySet()) {
				for (int i = 0; i < clusterManger.get(key).size(); i++) {
					if (clusterManger.get(key).get(i).id.equals(fromTitle)) {
						setA_name = key;
						setA = clusterManger.get(key);
						setA_node = clusterManger.get(key).get(i);
					}
					if (clusterManger.get(key).get(i).id.equals(toTitle)) {
						setB_name = key;
						setB = clusterManger.get(key);
						setB_node = clusterManger.get(key).get(i);
					}
				}
			}
			if (setA == null || setB == null) {
				continue;
			}
			if (setA.equals(setB)) {
				continue;
			}
			setA_node.setIndex(3);
			setB_node.setIndex(3);
			// find two sets

			
			String[] spilt1 = null;
			String[] spilt2 = null;
			try {
				spilt1 = setA_name.split("#");
			} catch (PatternSyntaxException e) {
				spilt1 = new String[1];
			}

			try {
				spilt2 = setB_name.split("#");
				
			} catch (PatternSyntaxException e) {
				spilt2 = new String[1];
			}
			
			ArrayList<TEDNode> union = new ArrayList<TEDNode>();
			for (int i = 0; i < spilt1.length; i++) {
				union.add(setA.get(i));
			}
			for (int i = 0; i < spilt2.length; i++) {
				union.add(setB.get(i));
			}
			for (int i = spilt1.length; i < setA.size(); i++) {
				union.add(setA.get(i));
			}
			for (int i = spilt2.length; i < setB.size(); i++) {
				union.add(setB.get(i));
			}
			String newName = setA_name + "#" + setB_name;
			
			if(spilt1.length > spilt2.length){//moving SetB
				for(int i = 0; i < spilt2.length; i++){
					setB.get(i).set(setA_node.x+random(-2000, 2000), setA_node.y+random(-2000, 2000));
					for(int j = 0; j < myTEDGraph.springs.size(); j++){
						if(myTEDGraph.springs.get(j).toNode.equals(setB.get(i))){
							myTEDGraph.springs.get(j).fromNode.set(setB.get(i).x+random(-5, 5), setB.get(i).y+random(-5, 5));
						}
					}
				}
			}else{
				for(int i = 0; i < spilt1.length; i++){//moving SetA
					setA.get(i).set(setB_node.x+random(-2000, 2000), setB_node.y+random(-2000, 2000));
					for(int j = 0; j < myTEDGraph.springs.size(); j++){
						if(myTEDGraph.springs.get(j).toNode.equals(setA.get(i))){
							myTEDGraph.springs.get(j).fromNode.set(setA.get(i).x+random(-5, 5), setA.get(i).y+random(-5, 5));
						}
					}
				}
			}
			
			if(myTEDGraph.addSpring(fromTitle, toTitle)){
				setA_node.setIndex(3);
				setB_node.setIndex(3);
			}else{
				
			}
			
			
			clusterManger.put(newName, union);
			//println(newName);
			//println(clusterManger.get(newName));
			clusterManger.remove(setA_name);
			clusterManger.remove(setB_name);
		}
		
	}
	
	
	
	

	public void makeTree() {
		String bigtopic = "Technology";
		video = new ArrayList<String>();
		for (int i = 0; i < 7; i++) {
			video.add(Integer.toString(i + 1));
		}
		KeyTopic_Video.put(bigtopic, video);
		bigtopic = "Design";
		video = new ArrayList<String>();
		for (int i = 0; i < 7; i++) {
			video.add(Integer.toString(i + 8));
		}
		KeyTopic_Video.put(bigtopic, video);
		bigtopic = "Entertainment";
		video = new ArrayList<String>();
		for (int i = 0; i < 7; i++) {
			video.add(Integer.toString(i + 15));
		}
		KeyTopic_Video.put(bigtopic, video);
		bigtopic = "TED";
		video = new ArrayList<String>();
		video.add("Technology");
		video.add("Design");
		video.add("Entertainment");
		KeyTopic_Video.put(bigtopic, video);
	}

	public void LoadClusterData() {
		// load XML
		xml = loadXML("cluster.xml");

		XML[] children = xml.getChildren("element");
		// println("cluster's length : " + children.length);
		for (int i = 0; i < children.length; i++) {
			XML temp_topic = children[i].getChild("children");
			// println(temp_topic);
			XML[] topics = temp_topic.getChildren("element");
			XML temp_id = children[i].getChild("cluster_id");

			String bigTopic = (temp_id.getContent().toString());

			video = new ArrayList<String>();
			for (int j = 0; j < topics.length; j++) {
				video.add(topics[j].getContent().toString().toLowerCase());
			}
			KeyTopic_Video.put(bigTopic, video);
		}
		// println(KeyTopic_Video);
	}

	public void LoadTopicData() {

		table = loadTable("KeyTopics.csv", "header");

		// println(table.getRowCount() + " total rows in table");

		for (TableRow row : table.rows()) {
			String VideoName = row.getString("VideoName");
			String KeyTopic = row.getString("KeyTopic");
			// Topic temp_topic = new Topic(VideoName, KeyTopic);
			if (KeyTopic_Video.get(KeyTopic) == null) {
				video = new ArrayList<String>();
				video.add(VideoName);
				KeyTopic_Video.put(KeyTopic, video);
			} else {
				ArrayList temp = (ArrayList) KeyTopic_Video.get(KeyTopic);
				temp.add(VideoName);
				KeyTopic_Video.put(KeyTopic, temp);
			}
		}
		ArrayList temp = (ArrayList) KeyTopic_Video.get("transportation");
		// println(temp);
		// println(KeyTopic_Video);
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

		toggles[ti] = controlP5.addToggle("autoZoom", autoZoom, left, top
				+ posY, 15, 15);
		// java.lang.String theName, boolean theValue
		toggles[ti++].setLabel("Adjust Zoom Automatically");
		sliders[si++] = controlP5.addSlider("zoom", 0.05f, 1, left, top + posY
				+ 20, len, 15);
		// java.lang.String theName, float theMin, float theMax, int theX, int
		// theY, int theWidth, int theHeight
		posY += 50;

		sliders[si++] = controlP5.addSlider("resultCount", 1, 50, left, top
				+ posY, len, 15);
		posY += 30;

		sliders[si++] = controlP5.addSlider("springLength", 10, 500, left, top
				+ posY, len, 15);
		sliders[si++] = controlP5.addSlider("springStiffness", 0, 1, left, top
				+ posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("springDamping", 0, 1, left, top
				+ posY + 40, len, 15);
		posY += 70;

		sliders[si++] = controlP5.addSlider("nodeRadius", 0, 500, left, top
				+ posY + 0, len, 15);
		sliders[si++] = controlP5.addSlider("nodeStrength", 0, 50, left, top
				+ posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("nodeDamping", 0, 1, left, top
				+ posY + 40, len, 15);
		posY += 70;

		toggles[ti] = controlP5.addToggle("invertBackground", invertBackground,
				left + 0, top + posY, 15, 15);
		toggles[ti++].setLabel("Invert Background");
		toggles[ti] = controlP5.addToggle("drawFishEyed", drawFishEyed,
				left + 150, top + posY, 15, 15);
		toggles[ti++].setLabel("Draw Fish-Eyed");
		sliders[si++] = controlP5.addSlider("lineWeight", 1, 20, left, top
				+ posY + 20, len, 15);
		sliders[si++] = controlP5.addSlider("lineAlpha", 0, 100, left, top
				+ posY + 40, len, 15);
		sliders[si++] = controlP5.addSlider("linkColor", 0, 1, left, top + posY
				+ 60, len, 15);
		posY += 90;

		// ranges[ri++] =
		// controlP5.addRange("nodeDiameter",0,30,minNodeDiameter,maxNodeDiameter,left,top+posY+0,len,15);
		toggles[ti] = controlP5.addToggle("colorizeNodes", colorizeNodes, left,
				top + posY, 15, 15);
		toggles[ti++].setLabel("Colorize Nodes");
		sliders[si++] = controlP5.addSlider("minNodeDiameter", 0, 60, left, top
				+ posY + 20, len, 15);
		/*
		 * sliders[si++] = controlP5.addSlider("nodeDiameterFactor", 0, 100,
		 * left, top + posY + 40, len, 15);
		 */
		// sliders[si++] =
		// controlP5.addSlider("nodeColor",0,1,left,top+posY+40,len,15);
		posY += 70;

		toggles[ti] = controlP5.addToggle("showText", showText, left, top
				+ posY, 15, 15);
		toggles[ti++].setLabel("Show Text");
		toggles[ti] = controlP5.addToggle("showRolloverNeighbours",
				showRolloverNeighbours, left + 100, top + posY, 15, 15);
		toggles[ti++].setLabel("Show Neighbours on Rollover");
		posY += 30;

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
	}

	public void drawGUI() {
		controlP5.show();
		controlP5.draw();
	}

	public void controlEvent(ControlEvent theControlEvent) {
		guiEvent = true;

		GUI = controlP5.group("menu").isOpen();

		if (theControlEvent.isController()) {
			if (theControlEvent.controller().name().equals("editText")) {
				myTEDGraph.confirmEdit(theControlEvent.controller()
						.stringValue());
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
