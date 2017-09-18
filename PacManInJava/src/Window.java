import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.*;

public class Window extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Graphics g;
	Graphics bufferGraphics;
	BufferedImage buffer;
	Toolkit tk;
	int xSize;
	int ySize;
	URL imageURL;
	ImageIcon backgroundImg;
	ImageIcon playScreen;
	public Window() {
		tk = Toolkit.getDefaultToolkit();
		setLayout(null);
		setIgnoreRepaint(true);
		xSize = ((int) tk.getScreenSize().getWidth());
		ySize = ((int) tk.getScreenSize().getHeight());
		setSize(xSize,ySize);
		setUndecorated(true);
		setBackground(Color.BLACK);
		g = getGraphics();
		buffer = new BufferedImage(xSize,ySize,BufferedImage.TYPE_INT_ARGB);
		bufferGraphics = buffer.getGraphics();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	public void draw(ImageIcon image,int xPos,int yPos,int xDest,int yDest) {
		bufferGraphics.drawImage(image.getImage(),xPos,yPos,xDest,yDest,0,0,image.getIconWidth(),image.getIconHeight(),null);
	}
	public void paint(Graphics g) {
		g.drawImage(buffer,0,0,null);
	}
}
