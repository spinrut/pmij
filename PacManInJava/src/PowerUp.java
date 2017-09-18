import java.awt.Dimension;
import java.awt.Graphics;
import java.net.URL;

import javax.swing.ImageIcon;


public class PowerUp {
	int type;
	int duration;
	Dimension location;
	URL imageURL;
	ImageIcon image;
	public PowerUp(int powerType,int x,int y) {
		location = new Dimension(x+1,y+1);
		type = powerType;
	}
	public void draw(Graphics g,Dimension mapSize,Window window) {
		switch(type) {
		
		case 0: imageURL = Main.class.getResource("Miscellaneous Powerups/Pac-Dot.png");
		 break;
		case 1: imageURL = Main.class.getResource("Miscellaneous Powerups/Power Pellet.png");
		 break;
		case 2: imageURL = Main.class.getResource("Miscellaneous Powerups/Fake Pellet.png");
		 break;
		case 3: imageURL = Main.class.getResource("Miscellaneous Powerups/Power Star.png");
		 break;
		case 4: imageURL = Main.class.getResource("Miscellaneous Powerups/Full Heal.png");
		 break;
		case 5: imageURL = Main.class.getResource("Miscellaneous Powerups/Pineapple Warhead.png");
		 break;
		case 6: imageURL = Main.class.getResource("Miscellaneous Powerups/Cherry Bomb.png");
		 break;
		case 7: imageURL = Main.class.getResource("Miscellaneous Powerups/Strawberry.png");
		 break;
		case 8: imageURL = Main.class.getResource("Miscellaneous Powerups/Banana.png");
		 break;
		case 9: imageURL = Main.class.getResource("Balls/Ice Ball.png");
		 break;
		case 10: imageURL = Main.class.getResource("Balls/Fire Ball.png");
		 break;
		case 11: imageURL = Main.class.getResource("Balls/Bouncy Ball.png");
		 break;
		case 12: imageURL = Main.class.getResource("Balls/Sticky Ball.png");
		 break;
		case 13: imageURL = Main.class.getResource("Balls/Light Ball.png");
		 break;
		case 14: imageURL = Main.class.getResource("Balls/Dark Ball.png");
		 break;
		}
		image = new ImageIcon(imageURL);
		g.drawImage(image.getImage(),(int) ((window.xSize/(mapSize.width*2)+1)*(location.width - 0.75)),(int) (window.ySize/(mapSize.height*2)*(location.height - 0.75)),(int) ((window.xSize/(mapSize.width*2)+1)*(location.width-0.25)),(int) (window.ySize/(mapSize.height*2)*(location.height-0.25)),0,0,image.getIconWidth(),image.getIconHeight(),null);
	}
}
