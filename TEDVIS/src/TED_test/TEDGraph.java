package TED_test;

import generativedesign.GenerativeDesign;
import generativedesign.Node;
import generativedesign.Spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import controlP5.ControlP5;
import controlP5.Textfield;

class TEDGraph {
	// we use a HashMap to store the nodes, because we frequently have to find
	// them by their id,
	// which is easy to do with a HashMap
	PApplet parent;
	ControlP5 controlP5;
	protected HashMap<String, TEDNode> nodeMap = new HashMap<String, TEDNode>();
	protected ArrayList<Spring> springs = new ArrayList<Spring>();

	boolean flag = true;
	boolean flag2 = true;
	boolean doubleclick = true;
	boolean isDoubleClickEvent = false;
	boolean invertBackground = true;
	boolean drawFishEyed = true;
	float minClickDiameter = 20.0f;

	// hovered node
	Node rolloverNode = null;
	// node that is dragged with the mouse
	Node selectedNode = null;
	// node for which loading is in progress
	Node loadingNode = null;
	// node that was clicked on
	TEDNode clickedNode;

	TEDNode editNode;

	// new node edit
	boolean editing = false;
	ControlP5 editControls;
	Textfield editTextfield;

	// default
	boolean autoZoom = true;
	float zoom = 1.0f;
	float targetZoom = 1.0f;

	float springLength = 100.0f;
	float springStiffness = 0.4f;
	float springDamping = 0.9f;

	int resultCount = 10;
	float nodeRadius = 200.0f;
	float nodeStrength = -15.0f;
	float nodeDamping = 0.5f;
	boolean colorizeNodes = true;
	float minNodeDiameter = 3.0f;
	float nodeDiameterFactor = 1.0f;

	PFont font;
	float textsize;
	boolean showText = true;
	boolean showRolloverText = true;
	boolean showRolloverNeighbours = false;

	float lineWeight = 3.0f;
	float lineAlpha = 100.0f;
	int linkColor;

	// arrays for text analysis
	Pattern[] patterns = new Pattern[0];
	// int[] colors = new color[0];

	float minX = 0.0f;
	float minY = 0.0f;
	float maxX;
	float maxY;

	// center of the boundaries of the graph
	PVector center = new PVector();
	PVector offset = new PVector();
	PVector targetOffset = new PVector();

	// helpers
	int pMillis;
	// for pdf output we need to freeze time to
	// prevent text from disappearing
	boolean freezeTime = false;

	TEDGraph(PApplet a) {
		this.parent = a;
		this.font = parent.createFont("miso-regular.ttf", 12);
		this.linkColor = parent.color(0);
		this.maxX = parent.width;
		this.maxY = parent.height;
		this.editControls = new ControlP5(parent);
		this.pMillis = parent.millis();
	}

	TEDNode addNode(int i, String theID, float theX, float theY) {
		// check if node is already there
		TEDNode findNode = nodeMap.get(theID);
		if (findNode == null) {
			// create a new node
			TEDNode newNode = new TEDNode(this, theX, theY);
			newNode.setIndex(i);
			newNode.setID(theID);
			newNode.setDamping(nodeDamping);
			newNode.setStrength(nodeStrength);
			newNode.setRadius(nodeRadius);
			nodeMap.put(theID, newNode);
			return newNode;
		} else {
			return null;
		}
	}

	void removeNode(Node theNode) {
		// remove springs from/to theNode
		for (int i = springs.size() - 1; i >= 0; i--) {
			Spring s = springs.get(i);
			if (s.fromNode == theNode || s.toNode == theNode) {
				TEDNode from = (TEDNode) s.fromNode;
				TEDNode to = (TEDNode) s.toNode;
				springs.remove(i);
			}
		}

		// remove theNode
		nodeMap.remove(theNode.id);

		// remove single nodes
		Iterator iter = nodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			TEDNode node = (TEDNode) me.getValue();
			if (getSpringIndexByNode(node) < 0 && !node.wasClicked) {
				iter.remove();
			}
		}
	}

	boolean addSpring(String fromID, String toID) {
		TEDNode fromNode = (TEDNode) nodeMap.get(fromID);
		TEDNode toNode = (TEDNode) nodeMap.get(toID);
		//fromNode.set(toNode.x+parent.random(-5, 5), toNode.y+parent.random(-5, 5));
		// if one of the nodes do not exist, stop creating spring
		if (fromNode == null)
			return false;
		if (toNode == null)
			return false;
		/*if((fromNode.getIndex() == 1) && (toNode.getIndex() == 1)){
			return null;
		}*/
		/*if((fromNode.getIndex() == 3) && (toNode.getIndex() == 3)){
			return false;
		}*/
		if((fromNode.getIndex() == 1) && (toNode.getIndex() == 3)){
			return false;
		}
		if((fromNode.getIndex() == 3) && (toNode.getIndex() == 1)){
			return false;
		}
		if (getSpring(fromNode, toNode) == null) {
			// create a new spring
			Spring newSpring = new Spring(fromNode, toNode, springLength,
					springStiffness, 0.9f);
			springs.add(newSpring);
			return true;
		}
		return false;
	}

	// compute stuff
	void update() {
		//parent.println("update");
		// use this function also to get actual width and heigth of the graph
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = -Float.MAX_VALUE;
		maxY = -Float.MAX_VALUE;

		// make an Array out of the values in nodeMap
		Node[] nodes = (Node[]) nodeMap.values().toArray(new Node[0]);
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].attract(nodes);
		}
		for (int i = 0; i < springs.size(); i++) {
			Spring s = (Spring) springs.get(i);
			if (s == null)
				break;
			s.update();
		}
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].update();
			minX = parent.min(nodes[i].x, minX);
			maxX = parent.max(nodes[i].x, maxX);
			minY = parent.min(nodes[i].y, minY);
			maxY = parent.max(nodes[i].y, maxY);
		}
		if (selectedNode != null) {
			// when dragging a node
			selectedNode.x = (parent.mouseX - parent.width / 2) / zoom
					- offset.x;
			selectedNode.y = (parent.mouseY - parent.height / 2) / zoom
					- offset.y;
		} else if (autoZoom) {
			// otherwise recalc zoom, center and offset
			center.set((minX + maxX) / 2, (minY + maxY) / 2, 0);
			targetOffset.set(-center.x, -center.y, 0);

			float dx = maxX - minX;
			float dy = maxY - minY;
			float qx = 100;
			float qy = 100;
			if (dx > 0)
				qx = (parent.width - 50) / dx;
			if (dy > 0)
				qy = (parent.height - 50) / dy;
			targetZoom = parent.min(parent.min(qx, qy), 1);
		}
		// check if there is a node hovered
		rolloverNode = getNodeByScreenPos(parent.mouseX, parent.mouseY);
	}

	// //////////////////////////// graph.draw()////////////////////////
	void draw() {
		//parent.println("draw");
		// TextField tf = (TextField) controlP5.controller("editText");
		if (!editing && editTextfield != null) {
			controlP5.remove("editText");
		}
		if (editTextfield != null) {
			PVector sp = localToGlobal(editNode.sx, editNode.sy);
			editTextfield.setPosition((int) (sp.x + 10), (int) (sp.y - 10));
			editTextfield.setFocus(true);
			// editControls.draw();
		}
		int dt = 0;
		if (!freezeTime) {
			int m = parent.millis();
			dt = m - pMillis;
			pMillis = m;
		}
		// smooth movement of canvas
		PVector d = new PVector();

		float accomplishPerSecond = 0.95f;
		float f = parent.pow(1 / (1 - accomplishPerSecond), -dt / 1000.0f);

		d = PVector.sub(targetOffset, offset);
		d.mult(f);
		offset = PVector.sub(targetOffset, d);

		zoom = targetZoom - ((targetZoom - zoom) * f);

		parent.pushStyle();

		parent.pushMatrix();
		parent.translate(parent.width / 2, parent.height / 2);
		parent.scale(zoom);
		parent.translate(offset.x, offset.y);

		Iterator iter = nodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			TEDNode node = (TEDNode) me.getValue();
			node.loaderLoop();
		}
		
		parent.colorMode(parent.HSB, 360, 100, 100, 100);
		// draw springs
		for (int i = 0; i < springs.size(); i++) {
			Spring s = (Spring) springs.get(i);
			if (s == null)
				break;
			parent.stroke(parent.hue(linkColor), parent.saturation(linkColor),
					parent.brightness(linkColor), lineAlpha);
			parent.strokeWeight(lineWeight);
			drawArrow((TEDNode) s.fromNode, (TEDNode) s.toNode);
			parent.noStroke();
		}

		// draw nodes
		parent.colorMode(parent.RGB, 255, 255, 255, 100);

		iter = nodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			TEDNode node = (TEDNode) me.getValue();
			node.draw();
		}
		// draw node labels
		iter = nodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry me = (Map.Entry) iter.next();
			TEDNode node = (TEDNode) me.getValue();
			node.drawLabel();
		}
		parent.popMatrix();
		parent.popStyle();
	}

	void drawArrow(TEDNode n1, TEDNode n2) {

		PVector d = new PVector(n2.sx - n1.sx, n2.sy - n1.sy);
		float margin1 = n1.diameter * n1.sfactor / 2.0f + 3 + lineWeight / 2;
		float margin2 = n2.diameter * n2.sfactor / 2.0f + 3 + lineWeight / 2;

		if (d.mag() > margin1 + margin2) {
			d.normalize();
			parent.line(n1.sx + d.x * margin1, n1.sy + d.y * margin1, n2.sx - d.x * margin2, n2.sy - d.y * margin2);
			float a = parent.atan2(d.y, d.x);
			parent.pushMatrix();
			parent.translate(n2.sx - d.x * margin2, n2.sy - d.y * margin2);
			parent.rotate(a);
			float l = 1 + lineWeight;
			parent.line(0, 0, -l, -l);
			parent.line(0, 0, -l, l);
			parent.popMatrix();
		}
	}

	// /////////////////////////////Event////////////////////////////////
	boolean mousePressed() {
		clickedNode = (TEDNode) getNodeByScreenPos(parent.mouseX, parent.mouseY);

		if (clickedNode != null) {
			if (parent.mouseButton == parent.RIGHT) {
				selectedNode = clickedNode;
				// double click right -> open page in browser
				if (parent.mouseEvent.getClickCount() == 2) {
					parent.link("http://en.Wikipedia.org/wiki/"	+ encodeURL(clickedNode.id), "_new");
				}
				return true;
			}
		}
		return false;
	}

	boolean mouseReleased() {
		if (selectedNode != null) {
			selectedNode = null;
		}
		if (clickedNode != null) {
			if (TED.lastMouseButton == parent.LEFT) {
				if (parent.keyPressed && parent.keyCode == parent.SHIFT) {
					// delete clicked node
					if (clickedNode == editNode) {
						editing = false;
					}
					removeNode(clickedNode);
					return true;
				} else if (!parent.keyPressed) {
					clickedNode.wasClicked = true;
					/*if (clickedNode.availableLinksLoaded) {
						System.out.println(clickedNode.availableLinks);
						for (int i = 0; i < clickedNode.availableLinks.size(); i++) {
							String title = (String) clickedNode.availableLinks.get(i);
							if (flag && !isDoubleClickEvent) {
								Node addedNode = addNode(i + 1, title, clickedNode.x + parent.random(-5, 5), clickedNode.y + parent.random(-5, 5));
							} else {
								Node addedNode = addNode(clickedNode.index, title, clickedNode.x + parent.random(-5, 5), clickedNode.y + parent.random(-5, 5));
							}
							Spring addedSpring = addSpring(clickedNode.id, title);
						}
						flag = false;
					}*/
					doubleclick = false;
					return true;
				} else if (parent.keyPressed && parent.keyCode == parent.ALT) {
					clickedNode.wasClicked = true;
					// load links to clicked node
				}
			}
		} else {
			// doubleclick on canvas
			if (TED.lastMouseButton == parent.LEFT) {
				if (parent.mouseEvent.getClickCount() == 2) {
					/*if (doubleclick) {
						isDoubleClickEvent = true;
						ArrayList<String> tempTitle = new ArrayList<String>();
						ArrayList<TEDNode> tempParents = new ArrayList<TEDNode>();
						for (int i = 0; i < TED.parentsTitle.size(); i++) {
							ArrayList<String> temp = (ArrayList<String>) TED.KeyTopic_Video .get(TED.parentsTitle.get(i));
							if (temp == null)
								continue;
							for (int j = 0; j < temp.size(); j++) {
								String title = (String) temp.get(j);
								tempTitle.add(title);
								if (flag2) {
									TEDNode addedNode = addNode((j + 1), title, TED.parentsNode.get(i).x + parent.random(-5, 5), TED.parentsNode.get(i).y + parent.random(-5, 5));
									tempParents.add(addedNode);
								} else {
									TEDNode addedNode = addNode(TED.parentsNode.get(i).index, title, TED.parentsNode.get(i).x + parent.random(-5, 5), TED.parentsNode.get(i).y + parent.random(-5, 5));
									tempParents.add(addedNode);
								}
								Spring addedSpring = addSpring(
										TED.parentsTitle.get(i), title);
							}
							flag2 = false;
						}
						TED.parentsNode = tempParents;
						TED.parentsTitle = tempTitle;
						if (TED.parentsNode.size() > 1800) {
							doubleclick = false;
						}
						System.out.println(doubleclick);
					}*/

					return true;
				}
			}
		}

		return false;
	}

	String encodeURL(String name) {
		StringBuffer sb = new StringBuffer();
		byte[] utf8 = name.getBytes();
		for (int i = 0; i < utf8.length; i++) {
			int value = utf8[i] & 0xff;
			if (value < 33 || value > 126) {
				sb.append('%');
				sb.append(parent.hex(value, 2));
			} else {
				sb.append((char) value);
			}
		}
		return sb.toString();
	}

	void confirmEdit(String theText) {
		editing = false;

		float px = editNode.x;
		float py = editNode.y;
		removeNode(editNode);
		TEDNode newNode = (TEDNode) addNode(0, theText, px, py);
		newNode.wasClicked = true;

		TED.guiEvent = false;
	}

	// ////////////////////////////GETTER & SETTER/////////////////////////
	float getWidth() {
		return 1;
	}

	Node getNodeByID(String theID) {
		Node node = (Node) nodeMap.get(theID);
		return node;
	}

	Node getNodeByScreenPos(float theX, float theY) {
		float mx = (theX - parent.width / 2) / zoom - offset.x;
		float my = (theY - parent.height / 2) / zoom - offset.y;

		return getNodeByPos(mx, my);
	}

	Node getNodeByPos(float theX, float theY) {
		Node selectedNode = null;
		Iterator i = nodeMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			TEDNode checkNode = (TEDNode) me.getValue();

			float d = parent.dist(theX, theY, checkNode.sx, checkNode.sy);
			if (d < parent.max(checkNode.diameter / 2, minClickDiameter)) {
				selectedNode = (Node) checkNode;
			}
		}
		return selectedNode;
	}

	int getSpringIndexByNode(Node theNode) {
		for (int i = 0; i < springs.size(); i++) {
			Spring s = (Spring) springs.get(i);
			if (s.fromNode == theNode || s.toNode == theNode) {
				return i;
			}
		}
		return -1;
	}

	Spring getSpring(Node theFromNode, Node theToNode) {
		for (int i = 0; i < springs.size(); i++) {
			Spring s = (Spring) springs.get(i);
			if (s.fromNode == theFromNode && s.toNode == theToNode) {
				return s;
			}
		}
		return null;
	}

	float getZoom() {
		return targetZoom;
	}

	void setZoom(float theZoom) {
		targetZoom = theZoom;
	}

	PVector getOffset() {
		return new PVector(offset.x, offset.y);
	}

	void setOffset(float theOffsetX, float theOffsetY) {
		offset.x = theOffsetX;
		offset.y = theOffsetY;
		targetOffset.x = offset.x;
		targetOffset.y = offset.y;
	}

	Node getLoadingNode() {
		return loadingNode;
	}

	void setLoadingNode(Node theNode) {
		loadingNode = theNode;
	}

	int getMillis() {
		if (freezeTime) {
			return pMillis;
		}
		return parent.millis();
	}

	// // // // // // // interaction with mouse & key // // // // // // // //
	void setResultCount(int theResultCount) {
		resultCount = theResultCount;
	}

	void setSpringLength(float theLength) {
		if (theLength != springLength) {
			springLength = theLength;
			for (int i = 0; i < springs.size(); i++) {
				Spring s = (Spring) springs.get(i);
				s.setLength(springLength);
			}
		}
	}

	void setSpringStiffness(float theStiffness) {
		if (theStiffness != springStiffness) {
			springStiffness = theStiffness;
			for (int i = 0; i < springs.size(); i++) {
				Spring s = (Spring) springs.get(i);
				s.setStiffness(springStiffness);
			}
		}
	}

	void setSpringDamping(float theDamping) {
		if (theDamping != springDamping) {
			springDamping = theDamping;
			for (int i = 0; i < springs.size(); i++) {
				Spring s = (Spring) springs.get(i);
				s.setDamping(springDamping);
			}
		}
	}

	void setNodeRadius(float theRadius) {
		if (theRadius != nodeRadius) {
			nodeRadius = theRadius;
			Iterator i = nodeMap.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				Node node = (Node) me.getValue();
				node.setRadius(nodeRadius);
			}
		}
	}

	void setNodeStrength(float theStrength) {
		if (theStrength != nodeStrength) {
			nodeStrength = theStrength;
			Iterator i = nodeMap.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				Node node = (Node) me.getValue();
				node.setStrength(nodeStrength);
			}
		}
	}

	void setNodeDamping(float theDamping) {
		if (theDamping != nodeDamping) {
			nodeDamping = theDamping;
			Iterator i = nodeMap.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				Node node = (Node) me.getValue();
				node.setDamping(nodeDamping);
			}
		}
	}

	void setInvertBackground(boolean theInvertBackground) {
		invertBackground = theInvertBackground;
	}

	void setDrawHyperbolic(boolean theDrawHyperbolic) {
		drawFishEyed = theDrawHyperbolic;
	}

	void setLineWeight(float theLineWeight) {
		lineWeight = theLineWeight;
	}

	void setLineAlpha(float theLineAlpha) {
		lineAlpha = theLineAlpha;
	}

	void setColorizeNodes(boolean theValue) {
		colorizeNodes = theValue;
	}

	void setMinNodeDiameter(float theValue) {
		minNodeDiameter = theValue;
	}

	void setNodeDiameterFactor(float theValue) {
		nodeDiameterFactor = theValue;
	}

	void setLinkColor(int theLinkColor) {
		linkColor = theLinkColor;
	}

	void setShowText(boolean theShowText) {
		showText = theShowText;
	}

	void setShowRolloverText(boolean theShowRolloverText) {
		showRolloverText = theShowRolloverText;
	}

	void setShowRolloverNeighbours(boolean theShowRolloverNeighbours) {
		showRolloverNeighbours = theShowRolloverNeighbours;
	}

	PVector screenPos(PVector thePos) {
		if (drawFishEyed) {
			// position of the node transformed, so that x and y is 0
			// if the node is in the center of the screen
			float x = thePos.x + offset.x / zoom;
			float y = thePos.y + offset.y / zoom;
			// get polar coordinates of the point
			float[] pol = GenerativeDesign.cartesianToPolar(x, y);
			float distance = pol[0];

			// fisheye projection
			float radius = parent.min(parent.width, parent.height) / 2;
			float distAngle = parent.atan(distance / (radius / 2) * zoom)
					/ parent.HALF_PI;
			float newDistance = distAngle * radius / zoom;

			// transform polar coordinates back into cartesian coordinates
			float[] newPos = GenerativeDesign.polarToCartesian(newDistance,
					pol[1]);
			// new position
			float newX = newPos[0] - offset.x;
			float newY = newPos[1] - offset.y;
			float newScale = parent.min(1.2f - distAngle, 1f);
			return new PVector(newX, newY, newScale);
		} else {
			return new PVector(thePos.x, thePos.y, 1);
		}
	}

	PVector localToGlobal(float theX, float theY) {
		float mx = (theX + offset.x) * zoom + parent.width / 2;
		float my = (theY + offset.y) * zoom + parent.height / 2;

		return new PVector(mx, my);
	}

	PVector globalToLocal(float theX, float theY) {
		float mx = (theX - parent.width / 2) / zoom - offset.x;
		float my = (theY - parent.height / 2) / zoom - offset.y;

		return new PVector(mx, my);
	}

	boolean isLoading(Node theNode) {
		if (theNode == loadingNode)
			return true;
		return false;
	}

	boolean isRollover(Node theNode) {
		if (theNode == rolloverNode)
			return true;
		return false;
	}

	boolean isRolloverNeighbour(Node theNode) {
		if (getSpring(theNode, rolloverNode) != null)
			return true;
		if (getSpring(rolloverNode, theNode) != null)
			return true;
		return false;
	}

	boolean isSelected(Node theNode) {
		if (theNode == selectedNode)
			return true;
		return false;
	}

	public String toString() {
		String s = "";
		Iterator i = nodeMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			Node node = (Node) me.getValue();
			s += node.toString() + "\n";
		}
		return (s);
	}
}
