package livingimagesmetamaker1;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Textfield;

import processing.core.PApplet;
import processing.core.PImage;
import toxi.geom.Rect;
import toxi.geom.Triangle2D;
import toxi.geom.Vec2D;

public class LivingImagesMetaMaker extends PApplet {
	private static final long serialVersionUID = 1L;
	
	static String[] pargs;
	
	BaseImageFactory baseImageFactory;
	
	ArrayList<String> filenames = new ArrayList<String>();
	ArrayList<BaseImage> images = new ArrayList<BaseImage>();
	
	boolean donext = false, refreshMarkers = true, readtriangle = false;
	int nextimg = 0, clickcount = 0, faceTriId = 0, detailsIndex = 0;
	Vec2D[] faceTri = new Vec2D[3];
	
	ControlP5 cp5;
	ArrayList<Button> markerbtn = new ArrayList<Button>();
	ArrayList<Textfield> markertxt = new ArrayList<Textfield>();
	Button processbtn, nextbutton, skipbutton;
	
	DropdownList dl;
	int dropmarker = 0;
	
	String ImageFilename = "";
	
	public void setup() {
		//		println(PFont.list());
		
		if (pargs == null || pargs.length < 1) {
			println(pargs.length + " : Usage: LivingImagesMetaMaker [image1], [image2...]");
			println("or use LivingImagesMetaMaker -d [folder]");
			this.exit();
		}
		
		if (pargs[0].equalsIgnoreCase("-d")) {
			File f = new File(pargs[1]);
			if (f == null || !f.isDirectory()) {
				println("invalid folder - " + pargs[1]);
				this.exit();
			}
			FilenameFilter ffl = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					String[] comp = name.split("\\.");
					if (comp[comp.length - 1].equalsIgnoreCase("jpg") || comp[comp.length - 1].equalsIgnoreCase("jpeg")
							|| comp[comp.length - 1].equalsIgnoreCase("png"))
						return true;
					return false;
				}
			};
			
			String[] files = f.list(ffl);
			if (files.length < 1) {
				println("no files found in the folder");
				this.exit();
			}
			
			println(files.length + " images found.");
			
			String fnme = pargs[1];
			if (fnme.substring(fnme.length() - 1).equals("/"))
				fnme = fnme.substring(0, fnme.length() - 1);
			for (int i = 0; i < files.length; i++)
				filenames.add(fnme + "/" + files[i]);
			
		} else {
			for (int i = 0; i < pargs.length; i++)
				filenames.add(pargs[i]);
		}
		
		size(800, 600, JAVA2D);
		smooth();
		textFont(createFont("Play", 18));
		
		baseImageFactory = new BaseImageFactory(this, "/Users/hari/Work/code/LivingImages_MetaMaker/src/data/");
		
		cp5 = new ControlP5(this);
		if (filenames.size() == 1)
			nextbutton = cp5.addButton("save and exit", 0, width - 100, height - 20, 100, 20);
		else
			nextbutton = cp5.addButton("save and next", 0, width - 100, height - 20, 100, 20);
		nextbutton.setId(0);
		
		skipbutton = cp5.addButton("skip", 0, width - 150, height - 20, 40, 20);
		skipbutton.setId(10000);
		
		processbtn = cp5.addButton("process", 0, width - 230, height - 20, 70, 20);
		processbtn.setId(10001);
		
		// DEBUG For testing loading from the xml file
		//		images.add(baseImageFactory.createBaseImage("/Users/hari/Work/code/LivingImages_MetaMaker/src/data/images/5442414379_191f2961a5_b.xml"));
		Directory dir = new Directory(baseImageFactory, "/Users/hari/Work/code/LivingImages_MetaMaker/src/data/directory.xml");
		dir.scan(pargs[1]);
//		dir.scan("/Users/hari/Work/code/LivingImages_MetaMaker/src/data/images");
		dir.save("/Users/hari/Work/code/LivingImages_MetaMaker/src/data/directory.xml");
		//		donext = false;
		
		dl = cp5.addDropdownList("name", 0, 0, 70, 100);
		dl.setId(5000);
		int i = 1;
		for (Person p : dir.values())
			dl.addItem(p.name, i++);
		dl.hide();
		
		nextimg = 0;
		if (nextimg < filenames.size()) {
			//				donext = true;
			background(0);
			ImageFilename = getImageFoldername(filenames.get(nextimg));
			PImage img = loadImage(filenames.get(nextimg));
			if (img != null) {
				displayImage(img);
			}
			if (nextimg >= filenames.size())
				nextbutton.setCaptionLabel("save and exit");
		}
		refreshMarkers = false;
	}
	
	private void displayImage(PImage img) {
		for (int i = 0; i < markerbtn.size(); i++)
			markerbtn.get(i).remove();
		markerbtn.clear();
		
		for (int i = 0; i < markertxt.size(); i++)
			markertxt.get(i).remove();
		markertxt.clear();
		
		dl.hide();
		
		background(0);
		pushMatrix();
		scale(getScaleFactor(img));
		
		image(img, 0, 0);
		popMatrix();
		
		nextbutton.hide();
	}
	
	/**
	 * LivingImagesMetaMaker - displayCollectInfo
	 * 
	 * @param bimg
	 * 
	 */
	private void displayCollectInfo(BaseImage bimg) {
		println(bimg.markers.size() + " markers found.");
		
		//		PGraphicsJava2D pg = (PGraphicsJava2D) createGraphics(bimg.width, bimg.height, JAVA2D);
		//		pg.beginDraw();
		//		pg.background(0, 0);
		//		for (Marker mrk : bimg.markers)
		//			if (mrk.mask != null)
		//				pg.g2.drawImage(mrk.mask, 0, 0, null);
		//		pg.endDraw();
		
		for (int i = 0; i < markerbtn.size(); i++)
			markerbtn.get(i).remove();
		markerbtn.clear();
		
		for (int i = 0; i < markertxt.size(); i++)
			markertxt.get(i).remove();
		markertxt.clear();
		
		dl.hide();
		
		background(0);
		pushMatrix();
		scale(getScaleFactor(bimg));
		
		//		bimg.mask(pg);
		image(bimg, 0, 0);
		//				image(pg, 0, 0);
		
		pushStyle();
		noFill();
		int num = 0;
		for (Marker mrk : bimg.markers) {
			if (mrk.fullrect != null) {
				num++;
				stroke(255, 0, 0, 200);
				rect(mrk.fullrect.x, mrk.fullrect.y, mrk.fullrect.width, mrk.fullrect.height);
				
				// for the UI
				Vec2D tmp = mrk.fullrect.getTopLeft().scale(getScaleFactor(bimg));
				
				if (mrk.facerect != null) {
					rect(mrk.facerect.x, mrk.facerect.y, mrk.facerect.width, mrk.facerect.height);
					
					if (mrk.faceTriangle != null) {
						stroke(0, 255, 0, 200);
						beginShape(POLYGON);
						vertex(mrk.faceTriangle.a.x, mrk.faceTriangle.a.y);
						vertex(mrk.faceTriangle.b.x, mrk.faceTriangle.b.y);
						vertex(mrk.faceTriangle.c.x, mrk.faceTriangle.c.y);
						endShape(CLOSE);
					}
					if (mrk.faceTriangle == null || mrk.miscdata > 0) {
						Button tmpb = cp5.addButton("tri" + num, 0, Math.round(tmp.x + 62), Math.round(tmp.y - 25), 45, 25);
						tmpb.setId(bimg.markers.size() + num);
						tmpb.setCaptionLabel("face tri");
						markerbtn.add(tmpb);
					}
				}
				
				pushStyle();
				noStroke();
				fill(0);
				rect(mrk.fullrect.x, mrk.fullrect.y - 25, 30, 25);
				rect(mrk.fullrect.x, mrk.fullrect.y + mrk.fullrect.height, mrk.fullrect.width, 85);
				fill(255);
				text(num, mrk.fullrect.x + 5, mrk.fullrect.y - 5);
				text(mrk.name, mrk.fullrect.x + 5, mrk.fullrect.y + mrk.fullrect.height + 20);
				text(mrk.urlfacebook, mrk.fullrect.x + 5, mrk.fullrect.y + mrk.fullrect.height + 40);
				text(mrk.urlflickr, mrk.fullrect.x + 5, mrk.fullrect.y + mrk.fullrect.height + 60);
				text(mrk.urltwitter, mrk.fullrect.x + 5, mrk.fullrect.y + mrk.fullrect.height + 80);
				popStyle();
				
				Button tmpb = cp5.addButton("x" + num, 0, Math.round(tmp.x + 30), Math.round(tmp.y - 25), 30, 25);
				tmpb.setId(num);
				tmpb.setCaptionLabel("x");
				markerbtn.add(tmpb);
				
				tmpb = cp5.addButton("detail" + num, 0, Math.round(tmp.x + 110), Math.round(tmp.y - 25), 40, 25);
				tmpb.setId(bimg.markers.size() * 2 + num);
				tmpb.setCaptionLabel("detail");
				markerbtn.add(tmpb);
			}
		}
		popStyle();
		popMatrix();
		
		nextbutton.show();
	}
	
	/**
	 * LivingImagesMetaMaker - getScaleFactor
	 * 
	 * @param bimg
	 * @return
	 * 
	 */
	private float getScaleFactor(BaseImage bimg) {
		if (bimg.width > bimg.height)
			return this.width / (float) bimg.width;
		else
			return this.height / (float) bimg.height;
	}
	
	private float getScaleFactor(PImage bimg) {
		if (bimg.width > bimg.height)
			return this.width / (float) bimg.width;
		else
			return this.height / (float) bimg.height;
	}
	
	public String getImageFoldername(String s) {
		String ts = "";
		String[] tmps = s.split("/");
		for (int i = 0; i < tmps.length - 1; i++)
			ts = ts + tmps[i] + "/";
		return ts;
	}
	
	public void controlEvent(ControlEvent theEvent) {
		this.mouseEvent.consume();
		
		if (theEvent.id() == 0 || theEvent.id() == 10000) {
			if (theEvent.id() == 0)
				images.get(images.size() - 1).save(ImageFilename + images.get(images.size() - 1).name + ".xml");
			//			images.get(images.size() - 1).save("/Users/hari/Work/code/LivingImages_MetaMaker/src/data/images/" + images.get(images.size() - 1).name + ".xml");
			nextimg++;
			if (nextimg < filenames.size()) {
				//				donext = true;
				background(0);
				ImageFilename = getImageFoldername(filenames.get(nextimg));
				PImage img = loadImage(filenames.get(nextimg));
				if (img != null) {
					displayImage(img);
				}
				if (nextimg >= filenames.size())
					nextbutton.setCaptionLabel("save and exit");
			} else
				this.exit();
			
		} else if (theEvent.id() == 10001) {
			BaseImage bimg;
			if (nextimg < filenames.size()) {
				println("Detecting people in image - \"" + filenames.get(nextimg) + "\"");
				ImageFilename = getImageFoldername(filenames.get(nextimg));
				PImage img = loadImage(filenames.get(nextimg));
				if (img != null) {
					bimg = baseImageFactory.createBaseImage(img, filenames.get(nextimg));
					println("Can provide more information for image - \"" + filenames.get(nextimg) + "\"");
					images.clear();
					images.add(bimg);
					donext = false;
					refreshMarkers = true;
				}
			}
		} else if (theEvent.id() <= images.get(images.size() - 1).markers.size()) {
			images.get(images.size() - 1).markers.remove(theEvent.id() - 1);
			refreshMarkers = true;
			
		} else if (theEvent.id() <= images.get(images.size() - 1).markers.size() * 2) {
			faceTriId = theEvent.id() - images.get(images.size() - 1).markers.size() - 1;
			//			faceTri = new Triangle2D();
			clickcount = 0;
			readtriangle = true;
			//			refreshMarkers = true;
			print("Click one eyes and mouth for this face ");
			
		} else if (theEvent.id() <= images.get(images.size() - 1).markers.size() * 3) {
			Marker mrk = images.get(images.size() - 1).markers.get(theEvent.id() - images.get(images.size() - 1).markers.size() * 2 - 1);
			print("Enter the details...");
			detailsIndex = 0;
			mrk.miscdata1 = 1000 + images.get(images.size() - 1).markers.indexOf(mrk);
			readDetails(mrk);
			//			refreshMarkers = true;
			
		} else if (theEvent.id() >= 1000 && theEvent.id() < 5000) {
			Marker mrk = images.get(images.size() - 1).markers.get(theEvent.id() - 1000);
			switch (detailsIndex) {
			case 0:
				mrk.name = theEvent.controller().stringValue();
				detailsIndex++;
				theEvent.controller().remove();
				readDetails(mrk);
				break;
			case 1:
				mrk.urlfacebook = theEvent.controller().stringValue();
				detailsIndex++;
				theEvent.controller().remove();
				readDetails(mrk);
				break;
			case 2:
				mrk.urlflickr = theEvent.controller().stringValue();
				detailsIndex++;
				theEvent.controller().remove();
				readDetails(mrk);
				break;
			case 3:
				mrk.urltwitter = theEvent.controller().stringValue();
				detailsIndex++;
				refreshMarkers = true;
				break;
			
			default:
				break;
			}
		} else if (theEvent.id() == 5000) {
			Marker mrk = images.get(images.size() - 1).markers.get(dropmarker);
			mrk.name = dl.stringValue();
			refreshMarkers = true;
		}
	}
	
	private void readDetails(Marker mrk) {
		Rect nr = new Rect(mrk.fullrect.getTopLeft().scale(this.width / (float) images.get(images.size() - 1).width), mrk.fullrect.getBottomRight().scale(
				getScaleFactor(images.get(images.size() - 1))));
		
		dl.setPosition(Math.round(nr.x + 5), Math.round(nr.y + 20));
		dl.show();
		dropmarker = mrk.miscdata1 - 1000;
		println(dropmarker);
		
		//		Rect nr = new Rect(mrk.fullrect.getTopLeft().scale(this.width / (float) images.get(images.size() - 1).width), mrk.fullrect.getBottomRight().scale(
		//				this.width / (float) images.get(images.size() - 1).width));
		//		Textfield tf = cp5
		//				.addTextfield("detailstxt", Math.round(nr.x + 5), Math.round(nr.y + nr.height + 5 + 20 * detailsIndex), Math.round(nr.width - 10), 20);
		//		tf.setId(mrk.miscdata1);
		//		tf.setFocus(true);
		//		markertxt.add(tf);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (readtriangle == true) {
			BaseImage bimg = images.get(images.size() - 1);
			faceTri[clickcount] = new Vec2D(mouseX, mouseY).scale(1f / getScaleFactor(bimg));
			clickcount++;
			if (clickcount >= 3) {
				readtriangle = false;
				Marker mrk = bimg.markers.get(faceTriId);
				mrk.faceTriangle = new Triangle2D(faceTri[0], faceTri[1], faceTri[2]);
				mrk.miscdata = 1;
				refreshMarkers = true;
				println(" - Ok!");
			}
		}
	}
	
	public void draw() {
		BaseImage bimg;
		//		if (donext && nextimg < filenames.size()) {
		//			println("Detecting people in image - \"" + filenames.get(nextimg) + "\"");
		//			ImageFilename = getImageFoldername(filenames.get(nextimg));
		//			PImage img = loadImage(filenames.get(nextimg));
		//			if (img != null) {
		//				bimg = baseImageFactory.createBaseImage(img, filenames.get(nextimg));
		//				println("Can provide more information for image - \"" + filenames.get(nextimg) + "\"");
		//				images.clear();
		//				images.add(bimg);
		//				donext = false;
		//				refreshMarkers = true;
		//				
		//				// DEBUG for debugging only, remove the following line
		//				//				images.get(images.size() - 1).save(basepath + "images/" + images.get(images.size() - 1).name + ".xml");
		//			}
		//		}
		
		if (refreshMarkers) {
			bimg = images.get(images.size() - 1);
			displayCollectInfo(bimg);
			if (bimg.markers.size() <= 0)
				println("done and found nothing!");
			refreshMarkers = false;
		}
	}
	
	public static void main(String _args[]) {
		pargs = _args.clone();
		PApplet.main(new String[] { livingimagesmetamaker.LivingImagesMetaMaker.class.getName() });
	}
}
