import java.awt.Dimension;
import java.awt.Graphics;
import java.net.URL;

import javax.swing.ImageIcon;


public class Projectile {
	int type;
	int direction;
	int count;
	PacDimension location;
	URL imageURL;
	ImageIcon image;
	public Projectile(PacDimension locationDimension,int playerDirection,int thingType) {
		location = new PacDimension(new Dimension((int) locationDimension.width,(int) locationDimension.height));
		direction = playerDirection;
		type = thingType;
		count = -1;
	}
	public void draw(Graphics g, Dimension mapSize,Window window) {
		switch(type) {
		
		case 0: imageURL = Main.class.getResource("Balls/Normal Ball.png");
		 break;
		case 1: imageURL = Main.class.getResource("Balls/Ice Ball.png");
		 break;
		case 2: imageURL = Main.class.getResource("Balls/Fire Ball.png");
		 break;
		case 3: imageURL = Main.class.getResource("Balls/Bouncy Ball.png");
		 break;
		case 4: imageURL = Main.class.getResource(direction == 0 ? "Maps/Sticky Spot.png" : "Balls/Sticky Ball.png");
		 break;
		case 5: imageURL = Main.class.getResource("Balls/Light Ball.png");
		 break;
		case 6: imageURL = Main.class.getResource("Balls/Dark Ball.png");
		 break;
		
		}
		image = new ImageIcon(imageURL);
		g.drawImage(image.getImage(),(int) ((window.xSize/(mapSize.width*2)+1)*(location.width - 0.75)),(int) (window.ySize/(mapSize.height*2)*(location.height - 0.75)),(int) ((window.xSize/(mapSize.width*2)+1)*(location.width-0.25)),(int) (window.ySize/(mapSize.height*2)*(location.height-0.25)),0,0,image.getIconWidth(),image.getIconHeight(),null);
	}
}
