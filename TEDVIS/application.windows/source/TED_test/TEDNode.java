package TED_test;

import generativedesign.Node;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

class TEDNode extends Node {
	PApplet parent;
	int index;
	int index_temp;
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
	boolean availableLinksLoaded = false;
	ArrayList<String> availableLinks;

	// number of links already shown
	int linkCount = 0;
	float ringRadius = 0.0f;

	// number of links pointing to this node
	int backlinkCount = 0;

	// size of the displayed text
	float textsize = 18;

	// color of the node
	int nodeColor;
	
	public boolean ishover = true;
	// //////////////// constructor ////////////////////
	TEDNode(TEDGraph theGraph) {
		super();
		graph = theGraph;
		this.parent = graph.parent;
		nodeColor = parent.color(0);
		init();
	}

	TEDNode(TEDGraph theGraph, float theX, float theY, float dia) {
		super(theX, theY);
		graph = theGraph;
		this.parent = graph.parent;
		nodeColor = parent.color(0);
		this.diameter = dia;
		init();
	}

	/*TEDNode(TEDGraph theGraph, float theX, float theY, float theZ) {
		super(theX, theY, theZ);
		graph = theGraph;
		this.parent = graph.parent;
		nodeColor = parent.color(0);
		init();
	}*/

	TEDNode(TEDGraph theGraph, PVector theVector) {
		super(theVector);
		graph = theGraph;
		this.parent = graph.parent;
		nodeColor = parent.color(0);
		init();
	}

	void init() {
		activationTime = parent.millis();
	}

	// compute stuff
	public void update() {
		super.update();
		if(this.id.equals(TED.search)){
			this.index = 5;
		}else{
			this.index = index_temp;
		}
		PVector spos = graph.screenPos(this);
		sx = spos.x;
		sy = spos.y;
		sfactor = spos.z;

		// length of the text of the article (ignore 1500 chars
		// that are used for menu and other stuff)
		float l = parent.max(htmlString.length() - 1500, 0);
		// make this number a lot smaller and keep it from growing to fast
		l = parent.sqrt(l / 10000.0f);
		// diameter of the main dot
		//diameter = graph.minNodeDiameter + graph.nodeDiameterFactor * l;

		// thickness of the ring around the node
		int hiddenLinkCount = availableLinks.size() - linkCount;
		hiddenLinkCount = parent.max(0, hiddenLinkCount);
		//ringRadius = 1 + parent.sqrt(hiddenLinkCount / 2.0f);
		//diameter += 6;
		//diameter += ringRadius;
	}

	// //////////////////// TEDNode.draw() //////////////////////
	void draw() {
		float d;
		// while loading draw grey ring around node
		//d = (diameter - ringRadius) * sfactor;
		d = (diameter*graph.minNodeDiameter) * sfactor;
		if(ishover){
			switch (index) {
			case 0:
				parent.fill(247, 147, 29, 60);
				break;
			case 1:
				parent.fill(255,242,0,60);
				break;
			case 2:
				parent.fill(0,113,188,60);
				break;
			case 3:
				parent.fill(13,177,75,60);
				break;
			case 4:
				parent.fill(186,85,211,60);
				break;
			case 5:
				parent.fill(0);
				break;
			/*case 0:
				parent.fill(243,
						175,
						65, 65);
				break;
			case 1:
				parent.fill(55,
						141,
						202,65);
				break;
			case 2:
				parent.fill(35,
						178,
						148,65);
				break;
			case 3:
				parent.fill(236,
						115,
						115,65);
				break;
			case 4:
				parent.fill(227,
						87,
						43,65);
				break;
			case 5:
				parent.fill(0);
				break;*/
			}
		}else{
			switch (index) {
			case 0:
				parent.fill(247, 147, 29, 20);
				break;
			case 1:
				parent.fill(255,242,0,20);
				break;
			case 2:
				parent.fill(0,113,188,20);
				break;
			case 3:
				parent.fill(13,177,75,20);
				break;
			case 4:
				parent.fill(186,85,211,20);
				break;
			case 5:
				parent.fill(0);
				break;
			}
		}
		if (graph.invertBackground) {
			if(ishover){
				switch (index) {
				case 0:
					parent.fill(247, 147, 29, 60);
					break;
				case 1:
					parent.fill(255,242,0,60);
					break;
				case 2:
					parent.fill(0,113,188,60);
					break;
				case 3:
					parent.fill(13,177,75,60);
					break;
				case 4:
					parent.fill(186,85,211,60);
					break;
				case 5:
					parent.fill(255);
					break;
				}
			}else{
				switch (index) {
				case 0:
					parent.fill(247, 147, 29, 10);
					break;
				case 1:
					parent.fill(255,242,0,10);
					break;
				case 2:
					parent.fill(0,113,188,10);
					break;
				case 3:
					parent.fill(13,177,75,10);
					break;
				case 4:
					parent.fill(186,85,211,10);
					break;
				case 5:
					parent.fill(255);
					break;
				}
			}
		}
		parent.ellipse(sx, sy, d, d);
	}

	void drawLabel() {
		// draw text
		parent.textAlign(parent.LEFT);
		parent.rectMode(parent.CORNER);
		float tfactor = 1;

		// draw text for rolloverNode
		if (graph.showText) {
			if (wasClicked || (graph.isRollover(this) && graph.showRolloverText)) {
				activationTime = graph.getMillis();

				float ts = textsize / parent.pow(graph.zoom, 0.5f) * tfactor;
				parent.textFont(graph.font, ts);

				float tw = parent.textWidth(id);
				parent.fill(255, 80);
				if (graph.invertBackground)
					parent.fill(0, 80);
				parent.rect(sx + (diameter / 2 + 4) * tfactor * sfactor, sy
						- (ts / 2) * tfactor, (tw + 3) * tfactor, (ts + 3)
						* tfactor);
				parent.fill(80);
				if (graph.isRollover(this) && graph.showRolloverText) {
					parent.fill(0);
					if (graph.invertBackground)
						parent.fill(255);
				}
				parent.text(id, sx + (diameter / 2 + 5) * tfactor * sfactor,
						sy + 6 * tfactor);
			} else {
				// draw text for all nodes that are linked to the rollover node
				if (wasClicked || graph.showRolloverNeighbours) {
					if (graph.isRolloverNeighbour(this)) {
						activationTime = graph.getMillis();
					}
				}

				int dt = graph.getMillis() - activationTime;
				dt = 1;
				if (dt < 10000) {
					float ts = textsize / parent.pow(graph.zoom, 0.5f)
							* tfactor;
					parent.textFont(graph.font, ts);

					float tw = parent.textWidth(id);
					float a = parent.min(3 * (1 - dt / 10000.0f), 1f) * 100;
					parent.fill(255, a * 0.8f);
					if (graph.invertBackground)
						parent.fill(0, a * 0.8f);
					parent.rect(sx + (diameter / 2 + 4) * tfactor * sfactor, sy - (ts / 2) * tfactor, (tw + 3) * tfactor, (ts + 3) * tfactor);
					parent.fill(80, 255);
					parent.text(id, sx + (diameter / 2 + 5) * tfactor * sfactor, sy + 6 * tfactor);
				}
			}
		}
	}

	/*void loaderLoop() {
		if (!availableLinksLoaded) {
			try {
				ArrayList<String> children = (ArrayList) TED.KeyTopic_Video
						.get(this.getID());
				// ArrayList<String> children = children
				for (int i = 0; i < children.size(); i++) {
					availableLinks.add(children.get(i));
				}
				availableLinksLoaded = true;
			} catch (Exception e) {
			}
		}
	}
*/
	// //////////////////GETTER & SETTER///////////////////
	void setIndex(int num) {
		this.index = num;
		this.index_temp = index;
	}

	public void setID(String theID) {
		super.setID(theID);
		if (!theID.equals("")) {
			// load available links
			availableLinksLoaded = false;
			availableLinks = new ArrayList<String>();
		}
	}

	
	public String toString() {
		String s =  this.id+"->"+this.ishover;
		return s;
	}
}
