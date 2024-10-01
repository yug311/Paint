package Misc.paintprogram;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.*;
import java.awt.image.*;

public class PaintProgram2 extends JPanel implements MouseListener, MouseMotionListener, ActionListener, AdjustmentListener, ChangeListener
{
	JFrame frame;
	ArrayList<Point> points;
	//Stack<ArrayList<Point>> freeLines;
	Stack<Object> shapes;
	Stack<Object> undoStack;
	boolean drawingFreeLine = true, drawingRectangle, drawingOval, erasing, firstClick = true, drawingStraightLine, drawingTriangle;
	Shape currentShape;
	Color currentColor = Color.BLACK;
	int penWidth = 2, initX, initY;
	JMenu colorMenu, file;
	JMenuItem[] colorOptions;
	Color [] colors;
	JMenuBar menubar;
	JColorChooser colorChooser;
	JMenuItem save, load, clear, exit;
	ImageIcon saveImage, loadImage, rectImage, ovalImage, eraserImage, freeLineImage, undoImage, redoImage;
	String currentDirectory = System.getProperty("user.dir");
	JFileChooser fileChooser = new JFileChooser(currentDirectory);
	BufferedImage loadedImage;
	JScrollBar penWidthBar;

	JButton rectButton, ovalButton, eraserButton, freeLineButton, straightLineButton, triangleButton, undo, redo;
	Color backgroundColor = Color.WHITE;
	Color oldLastColor;

	public PaintProgram2()
	{
		points = new ArrayList<Point>();
	//	freeLines = new Stack<ArrayList<Point>>();
		shapes = new Stack<Object>();
		undoStack = new Stack<Object>();

		menubar = new JMenuBar();

		file = new JMenu("File");
		file.setLayout(new GridLayout(4, 1));

		save = new JMenuItem("Save", KeyEvent.VK_S);
		save.addActionListener(this);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		load = new JMenuItem("Load", KeyEvent.VK_L);
		load.addActionListener(this);
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

		clear = new JMenuItem("Clear");
		clear.addActionListener(this);

		exit = new JMenuItem("Exit");
		exit.addActionListener(this);

		saveImage = new ImageIcon("images/saveimg.png");
		saveImage = new ImageIcon(saveImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		loadImage = new ImageIcon("images/loadimg.png");
		loadImage = new ImageIcon(loadImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		save.setIcon(saveImage);
		load.setIcon(loadImage);

		file.add(save);
		file.add(load);
		file.add(clear);
		file.add(exit);

		menubar.add(file);

		colorMenu = new JMenu("Color Options");
		colors = new Color[]{Color.RED, Color.ORANGE,Color.YELLOW,Color.GREEN,Color.BLUE};
		colorOptions = new JMenuItem[colors.length];

		colorMenu.setLayout(new GridLayout(colors.length, 1));

		for(int x = 0; x<colors.length; x++)
		{
			colorOptions[x] = new JMenuItem();
			colorOptions[x].setPreferredSize(new Dimension(50, 30));
			colorOptions[x].putClientProperty("colorindex", x);
			colorOptions[x].setBackground(colors[x]);
			colorOptions[x].addActionListener(this);
			colorMenu.add(colorOptions[x]);
		}

		menubar.add(colorMenu);
		currentColor = colors[0];

		// rectImage, ovalImage, eraserImage, freeLineImage
		rectButton = new JButton();
		rectButton.addActionListener(this);
		rectButton.setFocusable(false);
		ovalButton = new JButton();
		ovalButton.addActionListener(this);
		ovalButton.setFocusable(false);
		eraserButton = new JButton();
		eraserButton.addActionListener(this);
		eraserButton.setFocusable(false);
		freeLineButton = new JButton();
		freeLineButton.addActionListener(this);
		freeLineButton.setFocusable(false);
		straightLineButton = new JButton("---");
		straightLineButton.addActionListener(this);
		straightLineButton.setFocusable(false);
		triangleButton = new JButton("Tri");
		triangleButton.addActionListener(this);
		triangleButton.setFocusable(false);
		undo = new JButton();
		undo.addActionListener(this);
		undo.setFocusable(false);
		redo = new JButton();
		redo.addActionListener(this);
		redo.setFocusable(false);


		rectImage = new ImageIcon("images/rectImg.png");
		rectImage = new ImageIcon(rectImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		ovalImage = new ImageIcon("images/ovalImg.png");
		ovalImage = new ImageIcon(ovalImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		eraserImage = new ImageIcon("images/eraserImg.png");
		eraserImage = new ImageIcon(eraserImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		freeLineImage = new ImageIcon("images/freelineImg.png");
		freeLineImage = new ImageIcon(freeLineImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
	
		undoImage = new ImageIcon("images/undoImg.png");
		undoImage = new ImageIcon(undoImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		redoImage = new ImageIcon("images/redoImg.png");
		redoImage = new ImageIcon(redoImage.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

		rectButton.setIcon(rectImage);
		ovalButton.setIcon(ovalImage);
		eraserButton.setIcon(eraserImage);
		freeLineButton.setIcon(freeLineImage);
		//straightLineButton.setIcon(freeLineImage);
		//triangleButton.setIcon(freeLineImage);
		undo.setIcon(undoImage);
		redo.setIcon(redoImage);

		menubar.add(freeLineButton);
		menubar.add(rectButton);
		menubar.add(ovalButton);
		menubar.add(eraserButton);
		menubar.add(straightLineButton);
		menubar.add(triangleButton);
		menubar.add(undo);
		menubar.add(redo);

		penWidthBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 50);
		penWidthBar.addAdjustmentListener(this);
		penWidth = penWidthBar.getValue();
		menubar.add(penWidthBar);

		frame = new JFrame("paint program");
		frame.add(this);
		frame.setSize(1400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		colorChooser = new JColorChooser();
		colorChooser.getSelectionModel().addChangeListener(this);
		colorMenu.add(colorChooser);

		frame.add(menubar, BorderLayout.NORTH);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setColor(backgroundColor);
		g.fillRect(0, 0, frame.getWidth(), frame.getHeight());


		if(loadedImage != null)
		{
			g.drawImage(loadedImage, 0, 0, null);
		}

		Graphics2D g2 = (Graphics2D)g;
		//Iterator it = freeLines.iterator();
		Iterator it = shapes.iterator();
		int index = 0;


		while(it.hasNext())
		{
  			Object shape = it.next();

  			if(shape instanceof BufferedImage)
  			{
				g.drawImage(loadedImage, 0, 0, this);
			}

  			else if(shape instanceof ArrayList<?>)
  			{
  				@SuppressWarnings("unchecked")
				ArrayList<Point> p = (ArrayList<Point>)shape; //ADD SHAPE HERE MAYBE
				g2.setStroke(new BasicStroke(p.get(0).getPenWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(p.get(0).getColor());

				for(int x = 0; x<p.size() - 1; x++)
				{
					Point p1 = p.get(x);
					Point p2 = p.get(x+1);
					g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				}
			}

			else if(shape instanceof Rectangle)
			{
				Rectangle rect = (Rectangle)shape;
				g2.setStroke(new BasicStroke(rect.getPenWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

				g.setColor(rect.getColor());
				g2.draw(rect.getShape());
			//	g2.drawPolygon(new int[] {10, 20, 30}, new int[] {100, 20, 100}, 3);

			}

			else if(shape instanceof Oval)
			{
				Oval oval = (Oval)shape;
				g2.setStroke(new BasicStroke(oval.getPenWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

				g.setColor(oval.getColor());
				g2.draw(oval.getShape());
			}

			else if(shape instanceof Line)
			{
				Line line = (Line)shape;
				g2.setStroke(new BasicStroke(line.getPenWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

				g.setColor(line.getColor());
				g2.draw(line.getShape());
			}

			else if(shape instanceof Triangle)
			{
				Triangle tri = (Triangle)shape;
				g2.setStroke(new BasicStroke(tri.getPenWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

				g.setColor(tri.getColor());
				g2.drawPolygon(new int []{tri.getx1(), tri.getx2(), tri.getx3()}, new int []{tri.gety1(), tri.gety2(), tri.gety3()}, 3);
			}
		}

		if(drawingFreeLine && points.size() > 0)
		{
			g2.setStroke(new BasicStroke(points.get(0).getPenWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			g.setColor(points.get(0).getColor());

			for(int x = 0; x<points.size() - 1; x++)
			{
				Point p1 = points.get(x);
				Point p2 = points.get(x+1);
				g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == eraserButton)
		{
			erasing = true;
			drawingFreeLine = true;
			drawingRectangle = false;
			drawingOval = false;
			drawingStraightLine = false;
			currentColor = backgroundColor;
			drawingTriangle = false;
		}

		else if(e.getSource() == freeLineButton)
		{
			erasing = false;
			drawingFreeLine = true;
			drawingRectangle = false;
			drawingOval = false;
			drawingStraightLine = false;
			//currentColor = shapes.peek().getColor();
			drawingTriangle = false;
		}

		else if(e.getSource() == rectButton)
		{
			erasing = false;
			drawingFreeLine = false;
			drawingRectangle = true;
			drawingOval = false;
			drawingStraightLine = false;
			drawingTriangle = false;
		}

		else if(e.getSource() == ovalButton)
		{
			erasing = false;
			drawingFreeLine = false;
			drawingRectangle = false;
			drawingOval = true;
			drawingStraightLine = false;
			drawingTriangle = false;
		}

		else if(e.getSource() == straightLineButton)
		{
			erasing = false;
			drawingFreeLine = false;
			drawingRectangle = false;
			drawingOval = false;
			drawingStraightLine = true;
			drawingTriangle = false;
		}

		else if(e.getSource() == triangleButton)
		{
			erasing = false;
			drawingFreeLine = false;
			drawingRectangle = false;
			drawingOval = false;
			drawingStraightLine = false;
			drawingTriangle = true;
		}

		else if(e.getSource() == undo)
		{
			if(shapes.size() > 0)
			{
				undoStack.push(shapes.pop());
			}
			repaint();
		}

		else if(e.getSource() == redo)
		{
			if(undoStack.size() > 0)
			{			
				shapes.push(undoStack.pop());
			}
			repaint();
		}

else{ //new statement
		if(e.getSource() == clear)
		{
			//freeLines = new Stack<ArrayList<Point>>();
			shapes = new Stack<Object>();
			loadedImage = null;
			repaint();
		}

		else if(e.getSource() == exit)
		{
			System.exit(0);
		}

		else if(e.getSource() == load)
		{
			fileChooser.showOpenDialog(null);
			File imgFile = fileChooser.getSelectedFile();

			if(imgFile != null && imgFile.toString().indexOf(".png") >= 0)
			{
				try
				{
					loadedImage = ImageIO.read(imgFile);
				}
				catch(IOException ee){}

				//freeLines = new Stack<ArrayList<Point>>();
				shapes = new Stack<Object>();
				shapes.push(loadedImage);
				repaint();
			}

			else
			{
				if(imgFile == null)
				{
					JOptionPane.showMessageDialog(null, "Load a png");
				}
			}

		}

		else if(e.getSource() == save)
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter("*.png", "png");
			fileChooser.setFileFilter(filter);

			if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();

				try
				{
					String st = file.getAbsolutePath();
					if(st.indexOf(".png") >=0)
					{
						st = st.substring(0, st.length() - 4);
					}
					ImageIO.write(createImage(), "png", new File(st + ".png"));
				}
				catch(IOException ee){}
			}
		}

		else
		{
			int index = (int)((JMenuItem)e.getSource()).getClientProperty("colorindex");
			currentColor = colors[index];
		}
}
	}

	public BufferedImage createImage()
	{
		int width = this.getWidth();
		int height = this.getHeight();
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		this.paint(g2);
		g2.dispose();
		return img;
	}

	public void mouseReleased(MouseEvent e)
	{
		if(drawingFreeLine)
		{
			if(points.size() > 0)
			{
				shapes.push(points);
				//freeLines.push(points);
			//	drawingFreeLine = false;
				points = new ArrayList<Point>();
			}
		}
		firstClick = true;
		repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		penWidth = penWidthBar.getValue();
	}

	public void mouseDragged(MouseEvent e)
	{
	//	drawingFreeLine = true;
		if(drawingFreeLine)
		{
			Color c = currentColor;
			if(erasing)
			{
				c = backgroundColor;
			}
			points.add(new Point(e.getX(), e.getY(), currentColor, penWidth));

		}

		else if(drawingRectangle)
		{
			if(firstClick)
			{
				initX = e.getX();
				initY = e.getY();
				shapes.push(new Rectangle(initX, initY, penWidth, 0, 0, currentColor));
				firstClick = false;
				
			}

			else
			{
				int width = Math.abs(initX - e.getX());
				int height= Math.abs(initY - e.getY());
				Rectangle rect = (Rectangle)shapes.peek();
				rect.setWidth(width);
				rect.setHeight(height);

				if(e.getX() < initX)
				{
					rect.setX(e.getX());
				}

				if(e.getY() < initY)
				{
					rect.setY(e.getY());
				}

			}
		}

		else if(drawingOval)
		{
			if(firstClick)
			{
				initX = e.getX();
				initY = e.getY();
				shapes.push(new Oval(initX, initY, penWidth, 0, 0, currentColor));
				firstClick = false;
			}

			else
			{
				int width = Math.abs(initX - e.getX());
				int height= Math.abs(initY - e.getY());
				Oval oval = (Oval)shapes.peek();
				oval.setWidth(width);
				oval.setHeight(height);

				if(e.getX() < initX)
				{
					oval.setX(e.getX());
				}

				if(e.getY() < initY)
				{
					oval.setY(e.getY());
				}

			}
		}

		else if(drawingStraightLine)
		{
			if(firstClick)
			{
				initX = e.getX();
				initY = e.getY();
				shapes.push(new Line(initX, initY, penWidth, initX, initY, currentColor));
				firstClick = false;
				
			}

			else
			{
				int width = e.getX();
				int height= e.getY();
				Line line = (Line)shapes.peek();
				line.setWidth(width);
				line.setHeight(height);
			}
		}


		else if(drawingTriangle)
		{
			if(firstClick)
			{
				initX = e.getX();
				initY = e.getY();
				shapes.push(new Triangle(initX, initY, initX, initY, initX, initY, penWidth, currentColor));
				firstClick = false;
			}

			else
			{
				int x2 = e.getX();
				int y2= initY;

				int x3 = (initX + x2)/2;
				int y3= e.getY();

				Triangle tri = (Triangle)shapes.peek();
				tri.setx2(x2);
				tri.sety2(y2);
				tri.setx3(x3);
				tri.sety3(y3);



			}
		}
		repaint();
	}
	public void stateChanged(ChangeEvent e)
	{
		currentColor = colorChooser.getColor();
	}

	//non
	public void mouseExited(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}

	public static void main(String[]args)
	{
		PaintProgram2 app = new PaintProgram2();
	}

	public class Point
	{
		int x;
		int y;
		Color color;
		int penWidth;

		public Point(int x, int y, Color color, int penWidth)
		{
			this.x = x;
			this.y = y;
			this.color = color;
			this.penWidth = penWidth;
		}

		public int getX()
		{
			return x;
		}

		public int getY()
		{
			return y;
		}

		public int getPenWidth()
		{
			return penWidth;
		}

		public Color getColor()
		{
			return color;
		}


	}

	public class Shape
	{
		int x, y, penWidth, width, height;
		Color color;

		public Shape(int x, int y, int penWidth, int width, int height, Color color)
		{
			this.x = x;
			this.y = y;
			this.penWidth = penWidth;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		public int getX(){return x;}
		public int getY(){return y;}
		public int getPenWidth(){return penWidth;}
		public int getWidth(){return width;}
		public int getHeight(){return height;}
		public Color getColor(){return color;}

		public void setX(int newX){x = newX;}
		public void setY(int newY){y = newY;}
		public void setWidth(int newW){width = newW;}
		public void setHeight(int newH){height = newH;}
	}

	public class Rectangle extends Shape
	{
		//int x, y, penWidth, width, height;
		//Color color;

		public Rectangle(int x, int y, int penWidth, int width, int height, Color color)
		{
			super(x, y, penWidth, width, height, color);
		}

		public Rectangle2D.Double getShape()
		{
			return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
		}

	}

	public class Oval extends Shape
	{
		public Oval(int x, int y, int penWidth, int width, int height, Color color)
		{
			super(x, y, penWidth, width, height, color);
		}

		public Ellipse2D.Double getShape()
		{
			return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
		}
	}

	public class Line extends Shape
	{
		public Line(int x, int y, int penWidth, int width, int height, Color color)
		{
			super(x, y, penWidth, width, height, color);
		}

		public Line2D.Double getShape()
		{
			return new Line2D.Double(getX(), getY(), getWidth(), getHeight());
		}
	}

	public class Triangle
	{
		int x1, x2, x3, y1, y2, y3, penWidth;
		Color color;
		public Triangle(int x1, int x2, int x3, int y1, int y2, int y3, int penWidth, Color color)
		{
			this.x1 = x1;
			this.x2 = x2;
			this.x3 = x3;
			this.y1 = y1;
			this.y2 = y2;
			this.y3 = y3;
			this.penWidth = penWidth;
			this.color = color;
		}

		public int getx1(){return x1;}
		public int getx2(){return x2;}
		public int getx3(){return x3;}
		public int gety1(){return y1;}
		public int gety2(){return y2;}
		public int gety3(){return y3;}
		public int getPenWidth(){return penWidth;}
		public Color getColor(){return color;}

		public void setx1(int newx1){x1 = newx1;}
		public void setx2(int newx2){x2 = newx2;}
		public void setx3(int newx3){x3 = newx3;}
		public void sety1(int newy1){y1 = newy1;}
		public void sety2(int newy2){y2 = newy2;}
		public void sety3(int newy3){y3 = newy3;}

	}


}