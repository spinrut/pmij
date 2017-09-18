import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Random;

import javax.sound.sampled.*;
import javax.swing.*;


public class Main implements MouseListener {

	/**
	 * @param args
	 */
	PacButton[] buttons;
	PacButton back;
	PacButton[] mode;
	PacButton[] difficultyButtons;
	URL imageURL;
	ImageIcon backgroundImg;
	Window window;
	Label helpTitle;
	TextArea text;
	Game game;
	Clip clip;
	AudioInputStream audioIn;
	int gameMode;
	int difficulty;
	public Main() {
		window = new Window();
		
		//Load background image
		imageURL = Main.class.getResource("cool.jpg");
		backgroundImg = new ImageIcon(imageURL);
		
		//Draw background, keep loading images
		window.draw(backgroundImg,0,0,window.xSize,window.ySize);
		window.repaint();
		
		//Set up buttons
		buttons = new PacButton[7];
		for(int k=0;k<5;k++) {
			buttons[k] = new PacButton(" ",0,(6-k)*window.ySize/8);
		}
		back = new PacButton("Back",0,0);
		back.addMouseListener(this);
		
		buttons[6] = new PacButton("Emergency Contact",0,window.ySize/5*4);
		buttons[5] = new PacButton("Contact PMiJ",0,window.ySize/5*4);
		
		buttons[4].setText("Play");
		buttons[3].setText("Instructions");
		buttons[2].setText("Story");
		buttons[1].setText("Achievements");
		buttons[0].setText("Exit");
		
		mode = new PacButton[3];
		for(int k=0;k<3;k++) {
			mode[k] = new PacButton(" ",0,(5-k)*window.ySize/8);
			mode[k].addMouseListener(this);
		}
		mode[2].setText("Classic");
		mode[1].setText("Arcade");
		mode[0].setText("Survival");
		
		difficultyButtons = new PacButton[4];
		for(int k=0;k<4;k++) {
			difficultyButtons[k] = new PacButton(" ",0,(5-k)*window.ySize/8);
			difficultyButtons[k].addMouseListener(this);
		}
		difficultyButtons[3].setText("Easy");
		difficultyButtons[2].setText("Normal");
		difficultyButtons[1].setText("Hard");
		difficultyButtons[0].setText("Insane");
		
		//Initialize game to prevent crashes later
		game = new Game(window);
		//Fun effect
		try {
			Random random = new Random();
			Integer randNum;
			ImageIcon randomImg;
			int xPosition;
			int yPosition;
			String url;
			for(int i = 0;i<10;i++) {
				randNum = random.nextInt(3)+1;
				xPosition = random.nextInt(window.xSize);
				yPosition = random.nextInt(window.ySize);
				if(random.nextInt(10) == 9 && i == 9) {
					randNum = 0;
					xPosition = window.xSize/2-200;
					yPosition = window.ySize/2-100;
				}
				url = "Screens/thing"+randNum.toString()+".png";
				imageURL = Main.class.getResource(url);
				randomImg = new ImageIcon(imageURL);
				window.draw(randomImg,xPosition,yPosition,xPosition+randomImg.getIconWidth(),yPosition+randomImg.getIconHeight());
				window.repaint();
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Clear screen
		window.draw(backgroundImg,0,0,window.xSize,window.ySize);
		window.repaint();
		
		for(PacButton p : buttons) {
			p.addMouseListener(this);
			p.setVisible(false);
			window.add(p);
			p.setSize(p.getPreferredSize());
			p.setLocation(window.xSize/5*4-p.getWidth(),p.getY());
			p.setVisible(true);
		}
		
		buttons[6].setLocation(window.xSize/5,buttons[4].getY());
		buttons[5].setLocation(window.xSize/5,buttons[5].getY());
		
		//Set up labels
		helpTitle = new Label();
		helpTitle.setForeground(Color.YELLOW);
		helpTitle.setBackground(new Color(0,0,0,0));
		
		//Set up the text area
		text = new TextArea("",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
		text.setFocusable(false);
		
		//Start music
		URL musicURL = getClass().getClassLoader().getResource("pacman_menu.wav");
		try {
			audioIn = AudioSystem.getAudioInputStream(musicURL);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Audio is missing or corrupted.");
		}
		
		//Load save
		try {
			File ghj = new File("save.dat");
			if(ghj.createNewFile()) createSave();
			ghj = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Start
		drawMenu(window);
	}
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}
	public void drawMenu(Window window) {
		window.draw(backgroundImg,0,0,window.xSize,window.ySize);
		window.repaint();
	}
	public void difficultySelect() {
		//Remove previous buttons
		for(PacButton p : mode) {
			window.remove(p);
		}
		
		//Add in new buttons
		for(PacButton p : difficultyButtons) {
			p.setVisible(false);
			window.add(p);
			p.setSize(p.getPreferredSize());
			p.setLocation(window.xSize/5*4-p.getWidth(),p.getY());
			p.setVisible(true);
		}
		window.repaint();
	}
	public void gameStart() {
		//Stop sound
		clip.stop();
		clip.setMicrosecondPosition(0);
		
		//Remove previous buttons
		for(PacButton p : difficultyButtons) {
			window.remove(p);
		}
		
		//Start game
		window.repaint();
		game.gameStart(gameMode,difficulty,0,0);
	}
	public void createSave() {
		PrintWriter out;
		try {
			out = new PrintWriter("save.dat");
			out.print("000000000000000000000000000-");
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void mouseClicked(MouseEvent e) {
		if(e.getSource().equals(buttons[6])) {
			URI uri;
			try {
				uri = new URI("mailto:help.pac_crumpet%40yahoo.com?subject=Game%20Secrets&body=Dear%20PMiJ%20Developers%2C%0A%0A");
				Desktop dt = Desktop.getDesktop();
				dt.browse(uri);
				System.exit(0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null,"Sorry, your default e-mail program could not be accessed.");
			}
		}
		if(e.getSource().equals(buttons[5])) {
			URI uri;
			try {
				uri = new URI("mailto:pac_crumpets%40yahoo.com?subject=Tea%20Time&body=Dear%20PMiJ%20Developers%2C%0A%0A");
				Desktop dt = Desktop.getDesktop();
				dt.browse(uri);
				System.exit(0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null,"Sorry, your default e-mail program could not be accessed.");
			}
		}
		if(e.getSource().equals(buttons[4])) {
			for(PacButton p : buttons) {
				window.remove(p);
			}
			back.setVisible(false);
			window.add(back);
			back.setSize(back.getPreferredSize());
			back.setLocation(window.xSize/2-back.getWidth()/2,window.ySize/5*4-back.getHeight()/2);
			back.setVisible(true);
			
			//Mode and difficulty select
			for(PacButton p : mode) {
				p.setVisible(false);
				window.add(p);
				p.setSize(p.getPreferredSize());
				p.setLocation(window.xSize/5*4-p.getWidth(),p.getY());
				p.setVisible(true);
			}
			window.repaint();
		} else if(e.getSource().equals(buttons[3])) {
			//Make things invisible
			helpTitle.setVisible(false);
			back.setVisible(false);
			text.setVisible(false);
			//Add help title
			window.add(helpTitle);
			helpTitle.setText("Instructions");
			helpTitle.setFont(new Font("Arial",Font.PLAIN,72));
			helpTitle.setAlignment(Label.CENTER);
			helpTitle.setSize(helpTitle.getPreferredSize());
			helpTitle.setLocation(window.xSize/2-helpTitle.getWidth()/2,(int) (window.ySize/5*1.1));
			//Add back button
			window.add(back);
			back.setSize(back.getPreferredSize());
			back.setLocation(window.xSize/2-back.getWidth()/2,window.ySize/5*4-back.getHeight()/2);
			//Add text box
			BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("help")));
			try {
				String line = in.readLine();
				String bob = "";
				while(line != null) {
					bob = bob + line + "\n";
					line = in.readLine();
				}
				text.setText(bob);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			window.add(text);
			text.setSize(text.getPreferredSize().width,text.getPreferredSize().height*3/2);
			text.setLocation(window.xSize/2-text.getWidth()/2,window.ySize/11*6-text.getHeight()/2);
			//Remove other buttons
			for(PacButton p : buttons) {
				window.remove(p);
			}
			//Show everything
			back.setVisible(true);
			helpTitle.setVisible(true);
			text.setVisible(true);
			window.repaint();
		} else if(e.getSource().equals(buttons[2])) {
			//Make things invisible
			helpTitle.setVisible(false);
			back.setVisible(false);
			text.setVisible(false);
			//Add help title
			window.add(helpTitle);
			helpTitle.setText("Story");
			helpTitle.setFont(new Font("Arial",Font.PLAIN,72));
			helpTitle.setAlignment(Label.CENTER);
			helpTitle.setSize(helpTitle.getPreferredSize());
			helpTitle.setLocation(window.xSize/2-helpTitle.getWidth()/2,(int) (window.ySize/5*1.1));
			//Add back button
			window.add(back);
			back.setSize(back.getPreferredSize());
			back.setLocation(window.xSize/2-back.getWidth()/2,window.ySize/5*4-back.getHeight()/2);
			//Add text box
			BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("story")));
			try {
				String line = in.readLine();
				String bob = "";
				while(line != null) {
					bob = bob + line + "\n";
					line = in.readLine();
				}
				text.setText(bob);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			window.add(text);
			text.setSize(text.getPreferredSize().width,text.getPreferredSize().height*3/2);
			text.setLocation(window.xSize/2-text.getWidth()/2,window.ySize/11*6-text.getHeight()/2);
			//Remove other buttons
			for(PacButton p : buttons) {
				window.remove(p);
			}
			//Show everything
			back.setVisible(true);
			helpTitle.setVisible(true);
			text.setVisible(true);
			window.repaint();
		} else if(e.getSource().equals(buttons[1])) {
			back.setVisible(false);
			window.add(back);
			try {
				FileReader oj;
				oj = new FileReader("save.dat");
				BufferedReader gojao = new BufferedReader(oj);
				String oajog = gojao.readLine();
				if(String.valueOf(oajog.charAt(0)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Arcade final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*2.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*3.5));
				}
				if(String.valueOf(oajog.charAt(1)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Survival final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*3.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*4.5));
				}
				if(Integer.parseInt(String.valueOf(oajog.charAt(2)).concat(String.valueOf(oajog.charAt(3))))>=10) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Ghost Eater final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*4.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*5.5));
				}
				if(String.valueOf(oajog.charAt(4)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Half-Hearted final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*5.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*6.5));
				}
				if(String.valueOf(oajog.charAt(5)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Black-Hearted final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*6.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*7.5));
				}
				if(String.valueOf(oajog.charAt(6)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Powered-Up final.png"));
					window.draw(image,window.xSize/4,(int) (window.ySize/11.0*7.5),(int) window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*8.5));
				}
				if(Integer.parseInt(String.valueOf(oajog.charAt(7)).concat(String.valueOf(oajog.charAt(8)).concat(String.valueOf(oajog.charAt(9)))))>=100) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/NOMZ! final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*2.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*3.5));
				}
				if(Integer.parseInt(String.valueOf(oajog.charAt(7)).concat(String.valueOf(oajog.charAt(8)).concat(String.valueOf(oajog.charAt(9)).concat(String.valueOf(oajog.charAt(10))))))>=1000) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/NOMZ!! final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*3.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*4.5));
				}
				if(String.valueOf(oajog.charAt(11)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Pellet Power final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*4.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*5.5));
				}
				if(String.valueOf(oajog.charAt(12)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Faked Out final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*5.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*6.5));
				}
				if(String.valueOf(oajog.charAt(13)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Star Power final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*6.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*7.5));
				}
				if(String.valueOf(oajog.charAt(14)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/CPR final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*3,(int) (window.ySize/11.0*7.5),(int) window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*8.5));
				}
				if(String.valueOf(oajog.charAt(15)).equals("1") && String.valueOf(oajog.charAt(16)).equals("1") && String.valueOf(oajog.charAt(17)).equals("1") && String.valueOf(oajog.charAt(18)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Fruit Sampler final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*2.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*3.5));
				}
				if(String.valueOf(oajog.charAt(19)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Boom! final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*3.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*4.5));
				}
				if(String.valueOf(oajog.charAt(20)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/KaBOOM! final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*4.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*5.5));
				}
				if(String.valueOf(oajog.charAt(21)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Frozen final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*5.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*6.5));
				}
				if(String.valueOf(oajog.charAt(22)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Burned final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*6.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*7.5));
				}
				if(String.valueOf(oajog.charAt(23)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Boing! final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*6,(int) (window.ySize/11.0*7.5),(int) window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*8.5));
				}
				if(String.valueOf(oajog.charAt(24)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Scraper final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*2.5),(int) window.xSize/4 + window.ySize/11*12,(int) (window.ySize/11.0*3.5));
				}
				if(String.valueOf(oajog.charAt(25)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Blinded final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*3.5),(int) window.xSize/4 + window.ySize/11*12,(int) (window.ySize/11.0*4.5));
				}
				if(String.valueOf(oajog.charAt(26)).equals("1")) {
					ImageIcon image = new ImageIcon(Main.class.getResource("Achievements/Extermination final.png"));
					window.draw(image,window.xSize/4 + window.ySize/11*9,(int) (window.ySize/11.0*4.5),(int) window.xSize/4 + window.ySize/11*12,(int) (window.ySize/11.0*5.5));
				}
				gojao.close();
			} catch(Exception zxc) {
				
			}
			back.setSize(back.getPreferredSize());
			back.setLocation(window.xSize/2-back.getWidth()/2,window.ySize/5*4-back.getHeight()/2);
			for(PacButton p : buttons) {
				window.remove(p);
			}
			back.setVisible(true);
			window.repaint();
		} else if(e.getSource().equals(buttons[0])) {
			System.exit(0);
		} else if(e.getSource().equals(mode[2])) {
			gameMode = 2;
			difficultySelect();
		} else if(e.getSource().equals(mode[1])) {
			try {
				FileReader oj;
				oj = new FileReader("save.dat");
				BufferedReader gojao = new BufferedReader(oj);
				if(String.valueOf(gojao.readLine().charAt(0)).equals("1")) {
					gameMode = 1;
					difficultySelect();
				} else {
					JOptionPane.showMessageDialog(null,"You have not yet unlocked Arcade Mode.\nTo unlock Arcade Mode, you must reach Level 10 in Classic Mode.","Not Unlocked",JOptionPane.ERROR_MESSAGE);
				}
				gojao.close();
			} catch(Exception zxc) {
				
			}
		} else if(e.getSource().equals(mode[0])) {
			try {
				FileReader oj;
				oj = new FileReader("save.dat");
				BufferedReader gojao = new BufferedReader(oj);
				if(String.valueOf(gojao.readLine().charAt(0)).equals("1")) {
					gameMode = 0;
					difficultySelect();
				} else {
					JOptionPane.showMessageDialog(null,"You have not yet unlocked Survival Mode.\nTo unlock Survival Mode, you must attain a score of 100,000 in Arcade Mode.","Not Unlocked",JOptionPane.ERROR_MESSAGE);
				}
				gojao.close();
			} catch(Exception zxc) {
				
			}
		} else if(e.getSource().equals(difficultyButtons[3])) {
			difficulty = 3;
			gameStart();
		} else if(e.getSource().equals(difficultyButtons[2])) {
			difficulty = 2;
			gameStart();
		} else if(e.getSource().equals(difficultyButtons[1])) {
			difficulty = 1;
			gameStart();
		} else if(e.getSource().equals(difficultyButtons[0])) {
			difficulty = 0;
			gameStart();
		} else {
			window.remove(back);
			window.remove(helpTitle);
			window.remove(text);
			for(PacButton p : mode) {
				window.remove(p);
			}
			for(PacButton p : difficultyButtons) {
				window.remove(p);
			}
			game.mode = -1;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			window.draw(backgroundImg,0,0,window.xSize,window.ySize);
			for(PacButton p : buttons) {
				window.add(p);
			}
			drawMenu(window);
			clip.stop();
			clip.setMicrosecondPosition(0);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		}
		((Component) e.getSource()).setForeground(Color.YELLOW);
	}
	public void mouseEntered(MouseEvent e) {
		e.getComponent().setForeground(Color.BLUE);
	}
	public void mouseExited(MouseEvent e) {
		e.getComponent().setForeground(Color.YELLOW);
	}
	public void mousePressed(MouseEvent e) {
		
	}
	public void mouseReleased(MouseEvent e) {
		
	}
}
