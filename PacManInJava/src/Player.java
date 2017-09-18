import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;

public class Player {
	PacDimension location;
	int health;
	int direction;
	int frame;
	int status;
	float speed;
	public Player(Dimension position,int maxHealth) {
		speed = 1;
		health = maxHealth;
		frame = 0;
		location = new PacDimension(position);
		location.width += 1;
		direction = 1;
		status = 0;
	}
	public ImageIcon image() {
		frame = (frame + 1) % 50;
		String[] directions = new String[4];
		directions[0] = "Right";
		directions[1] = "Down";
		directions[2] = "Left";
		directions[3] = "Up";
		URL imageURL;
		if(frame > 24) {
			imageURL = Main.class.getResource("Player Graphics/Pac-Man Closed.png");
		} else {
			imageURL= Main.class.getResource("Player Graphics/" + (status>0 ? "Burning" : "") + "Pac-Man Open " + directions[direction-1] + ".png");
		}
		return new ImageIcon(imageURL);
	}
}
