package TED_test;

import generativedesign.Node;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

class TEDNode extends Node {
	PApplet Pparent;
	String layerName;
	//ArrayList<TEDNode> children;
	//TEDNode parent;
	int index;
	// screenPosition and screenScalingFactor
	float sx = 0.0f;
	float sy = 0.0f;
	float sfactor = 1.0f;
	// reference to the force directed graph
	TEDGraph graph;

	// last activation (rollover) time
	int activationTime;

	// is this a node that was clicked on
	boolean wasClicked = false;

	// this will contain the respective html as an ArrayList (each line as a
	// string)
	// ArrayList htmlList = new ArrayList();
	String htmlString = "";
	boolean htmlLoaded = false;

	// available links
	//boolean availableLinksLoaded = false;
	//ArrayList<String> availableLinks;

	// number of links already shown
	int linkCount = 0;
	float ringRadius = 0.0f;

	// number of links pointing to this node
	int backlinkCount = 0;

	// size of the displayed text
	float textsize = 18;

	// color of the node
	int nodeColor;

	// //////////////// constructor ////////////////////
	TEDNode(TEDGraph theGraph) {
		super();
		graph = theGraph;
		this.Pparent = graph.parent;
		nodeColor = Pparent.color(0);
		init();
	}

	TEDNode(TEDGraph theGraph, float theX, float theY, String layer, TEDNode par) {
		super(theX, theY);
		graph = theGraph;
		layerName = layer;
		//parent = par;
		//children = new ArrayList<TEDNode>();
		this.Pparent = graph.parent;
		nodeColor = Pparent.color(0);
		init();
	}

	TEDNode(TEDGraph theGraph, float theX, float theY) {
		super(theX, theY);
		graph = theGraph;
		this.Pparent = graph.parent;
		nodeColor = Pparent.color(0);
		init();
	}

	TEDNode(TEDGraph theGraph, float theX, float theY, float theZ) {
		super(theX, theY, theZ);
		graph = theGraph;
		this.Pparent = graph.parent;
		nodeColor = Pparent.color(0);
		init();
	}

	TEDNode(TEDGraph theGraph, PVector theVector) {
		super(theVector);
		graph = theGraph;
		this.Pparent = graph.parent;
		nodeColor = Pparent.color(0);
		init();
	}

	void init() {
		activationTime = Pparent.millis();
	}

	// compute stuff
	public void update() {
		super.update();

		PVector spos = graph.screenPos(this);
		sx = spos.x;
		sy = spos.y;
		sfactor = spos.z;

		// length of the text of the article (ignore 1500 chars
		// that are used for menu and other stuff)
		float l = Pparent.max(htmlString.length() - 1500, 0);
		// make this number a lot smaller and keep it from growing to fast
		l = Pparent.sqrt(l / 10000.0f);
		// diameter of the main dot
		diameter = graph.minNodeDiameter + graph.nodeDiameterFactor * l;

		// thickness of the ring around the node
		//int hiddenLinkCount = availableLinks.size() - linkCount;
		//hiddenLinkCount = Pparent.max(0, hiddenLinkCount);
		//ringRadius = 1 + Pparent.sqrt(hiddenLinkCount / 2.0f);
		diameter += 6;
		diameter += ringRadius;
	}

	// //////////////////// TEDNode.draw() //////////////////////
	void draw() {
		float d;
		// while loading draw grey ring around node
		d = diameter * sfactor;
		Pparent.fill(128);
		Pparent.ellipse(sx, sy, d, d);

		d = (diameter - ringRadius) * sfactor;
		switch (index){
		case 0:
			Pparent.fill(68, 114, 217);
			break;
		case 1:
			Pparent.fill(160, 68, 217);
			break;
		case 2:
			Pparent.fill(68, 217, 182);
			break;
		case 3:
			Pparent.fill(217, 68, 205);
			break;
		case 4:
			Pparent.fill(217, 205, 68);
			break;
		case 5:
			Pparent.fill(91, 217, 68);
			break;
		case 6:
			Pparent.fill(160, 217, 68);
			break;
		case 7:
			Pparent.fill(217, 68, 68);
			break;
		case 8:
			Pparent.fill(217, 137, 68);
			break;
		case 9:
			Pparent.fill(68, 182, 217);
			break;
		case 10:
			Pparent.fill(68, 217, 114);
			break;
		case 11:
			Pparent.fill(91, 68, 217);
			break;
		case 12:
			Pparent.fill(217, 68, 137);
			break;
		
		}
		
		/*
		switch (index) {
		case 0:
			Pparent.fill(12);
			break;
		case 1:
			Pparent.fill(204, 102, 0);
			break;
		case 2:
			Pparent.fill(104, 202, 0);
			break;
		case 3:
			Pparent.fill(54, 102, 100);
			d = (50 - ringRadius)* sfactor;
			break;
		}
		
		
		if (graph.invertBackground) {
			switch (index) {
			case 0:
				Pparent.fill(255);
				break;
			case 1:
				Pparent.fill(204, 102, 0);
				break;
			case 2:
				Pparent.fill(104, 202, 0);
				break;
			case 3:
				Pparent.fill(54, 102, 100);
				d = (50 - ringRadius)* sfactor;
				break;
			}
		}
*/		
		
		Pparent.ellipse(sx, sy, d, d);
	}

	void drawLabel() {
		// draw text
		Pparent.textAlign(Pparent.LEFT);
		Pparent.rectMode(Pparent.CORNER);
		float tfactor = 1;

		// draw text for rolloverNode
		if (graph.showText) {
			if (wasClicked
					|| (graph.isRollover(this) && graph.showRolloverText)) {
				activationTime = graph.getMillis();

				float ts = textsize / Pparent.pow(graph.zoom, 0.5f) * tfactor;
				Pparent.textFont(graph.font, ts);

				float tw = Pparent.textWidth(id);
				Pparent.fill(255, 80);
				if (graph.invertBackground)
					Pparent.fill(0, 80);
				Pparent.rect(sx + (diameter / 2 + 4) * tfactor * sfactor, sy - (ts / 2) * tfactor, (tw + 3) * tfactor, (ts + 3) * tfactor);
				Pparent.fill(80);
				if (graph.isRollover(this) && graph.showRolloverText) {
					Pparent.fill(0);
					if (graph.invertBackground)
						Pparent.fill(255);
				}
				Pparent.text(id, sx + (diameter / 2 + 5) * tfactor * sfactor,
						sy + 6 * tfactor);
			} else {
				// draw text for all nodes that are linked to the rollover node
				if (wasClicked || graph.showRolloverNeighbours) {
					if (graph.isRolloverNeighbour(this)) {
						activationTime = graph.getMillis();
					}
				}

				int dt = graph.getMillis() - activationTime;
				if (dt < 10000) {
					float ts = textsize / Pparent.pow(graph.zoom, 0.5f)
							* tfactor;
					Pparent.textFont(graph.font, ts);

					float tw = Pparent.textWidth(id);
					float a = Pparent.min(3 * (1 - dt / 10000.0f), 1f) * 100;
					Pparent.fill(255, a * 0.8f);
					if (graph.invertBackground)
						Pparent.fill(0, a * 0.8f);
					Pparent.rect(sx + (diameter / 2 + 4) * tfactor * sfactor,
							sy - (ts / 2) * tfactor, (tw + 3) * tfactor,
							(ts + 3) * tfactor);
					Pparent.fill(80, a);
					Pparent.text(id, sx + (diameter / 2 + 5) * tfactor
							* sfactor, sy + 6 * tfactor);
				}
			}
		}
	}

	void loaderLoop() {
		/*if (!availableLinksLoaded) {
			try {
				ArrayList<String> children = (ArrayList) TED.KeyTopic_Video.get(this.getID());
				// ArrayList<String> children = children
				for (int i = 0; i < children.size(); i++) {
					availableLinks.add(children.get(i));
				}
				availableLinksLoaded = true;
			} catch (Exception e) {
			}
		}*/
	}

	// //////////////////GETTER & SETTER///////////////////
	void setIndex(int num) {
		this.index = num;
	}
	int getIndex() {
		return this.index;
	}
	public void setID(String theID) {
		super.setID(theID);
		/*if (!theID.equals("")) {
			// load available links
			availableLinksLoaded = false;
			availableLinks = new ArrayList<String>();
		}*/
	}

	/*ArrayList<TEDNode> getChildren() {
		return children;
	}*/

	public String toString() {
		String s ="name: " + this.id;
		return s;
	}
}
