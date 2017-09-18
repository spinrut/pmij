import java.awt.*;

public class PacButton extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PacButton(String string,int xPos,int yPos) {
		setText(string);
		setFont(new Font("Arial",Font.PLAIN,36));
		setForeground(Color.YELLOW);
		setBackground(new Color(0,0,0,0));
		setLocation(xPos,yPos);
		setSize(getPreferredSize());
	}
}
