import java.awt.Dimension;
import java.net.URL;
import javax.swing.ImageIcon;

public class Enemy {
	int health;
	int direction;
	int respawnTime;
	int status;
	String[] directions;
	String name;
	PacDimension location;
	URL imageURL;
	public Enemy(Dimension dim,String ghostName) {
		name = ghostName;
		direction = 0;
		directions = new String[4];
		directions[0] = "Right";
		directions[1] = "Down";
		directions[2] = "Left";
		directions[3] = "Up";
		location = new PacDimension(dim);
		location.width += 1;
		health = 4;
		respawnTime = 0;
		status = 0;
		imageURL = Main.class.getResource("Ghosts/" + name + " Right.png");
	}
	public ImageIcon image(int invincibleTime) {
		if(status>21) {
			imageURL = Main.class.getResource("Ghosts/Sick Effect.png");
		} else if(status>0) {
			imageURL = Main.class.getResource("Ghosts/Confused " + directions[direction-1] + ".png");
		} else if(status==0) {
			if(direction > 0) imageURL = Main.class.getResource("Ghosts/" + name + " " + directions[direction-1] + ".png");
			if(invincibleTime > 0) imageURL = Main.class.getResource("Ghosts/PP Effect 1.png");
			if(invincibleTime < 20 && invincibleTime % 4 > 1) imageURL = Main.class.getResource("Ghosts/PP Effect 2.png");
		} else if(status>-42) {
			imageURL = Main.class.getResource("Ghosts/Frozen Effect.png");
		} else {
			imageURL = Main.class.getResource("Ghosts/Burning Effect 1.png");
		}
		return new ImageIcon(imageURL);
	}
}
