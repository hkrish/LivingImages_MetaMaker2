/**
 * 
 */
package livingimagesmetamaker;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import processing.core.PApplet;

/**
 * Type - EditablePath
 * 
 * @author hari 27 Nov 2011
 * 
 * 
 */
public class EditablePath {
	ArrayList<Integer> codes;
	ArrayList<EditablePath.Coord> vertices;
	int wrule;
	
	/**
	 * 
	 */
	public EditablePath() {
		codes = new ArrayList<Integer>();
		vertices = new ArrayList<EditablePath.Coord>();
	}
	
	public void fromPath2D(Path2D p) {
		wrule = p.getWindingRule();
		PathIterator pi = p.getPathIterator(null);
		int type;
		
		codes.clear();
		vertices.clear();
		
		do {
			Coord crd = new Coord();
			type = pi.currentSegment(crd.val);
			codes.add(type);
			vertices.add(crd);
			pi.next();
		} while (!pi.isDone());
	}
	
	public Path2D toPath2D() {
		Path2D p;
		p = new Path2D.Float(wrule);
		
		int type;
		Coord c;
		for (int i = 0; i < codes.size(); i++) {
			type = codes.get(i);
			c = vertices.get(i);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				p.moveTo(c.val[0], c.val[1]);
				break;
			
			case PathIterator.SEG_LINETO:
				p.lineTo(c.val[0], c.val[1]);
				break;
			
			case PathIterator.SEG_QUADTO:
				p.quadTo(c.val[0], c.val[1], c.val[2], c.val[3]);
				break;
			
			case PathIterator.SEG_CUBICTO:
				p.curveTo(c.val[0], c.val[1], c.val[2], c.val[3], c.val[4], c.val[5]);
				break;
			
			case PathIterator.SEG_CLOSE:
				p.closePath();
				break;
			
			default:
				return null;
			}
		}
		return p;
	}
	
	public void drawPoints(PApplet p, float s) {
		int type;
		Coord c;
		for (int i = 0; i < codes.size(); i++) {
			type = codes.get(i);
			c = vertices.get(i);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				p.ellipse(c.val[0], c.val[1], s, s);
				break;
			
			case PathIterator.SEG_LINETO:
				p.ellipse(c.val[0], c.val[1], s, s);
				break;
			
			case PathIterator.SEG_QUADTO:
				p.ellipse(c.val[0], c.val[1], s / 2f, s / 2f);
				p.ellipse(c.val[2], c.val[3], s, s);
				break;
			
			case PathIterator.SEG_CUBICTO:
				p.ellipse(c.val[0], c.val[1], s / 2f, s / 2f);
				p.ellipse(c.val[2], c.val[3], s / 2f, s / 2f);
				p.ellipse(c.val[4], c.val[5], s, s);
				break;
			
			case PathIterator.SEG_CLOSE:
				break;
			}
		}
	}
	
	public class Coord {
		float[] val = new float[6];
	}
}
