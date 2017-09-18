import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Game {

	BufferedImage image;
	Graphics gameGraphics;
	int mode;
	int difficulty;
	int enemies;
	int score;
	int level;
	int playerDirection;
	int numPacDots;
	int numPowerPellets;
	int invincibleTime;
	int maxHealth;
	int ammo;
	int fireRate;
	int cooldown;
	boolean pause;
	boolean p_pressed;
	String[] map;
	URL imageURL;
	Player player;
	Enemy[] enemy;
	PowerUp[] pacDots;
	PowerUp[] powerPellets;
	PowerUp[] powerUps;
	ArrayList<PowerUp> a;
	ArrayList<Projectile> b;
	ArrayList<Projectile> c;
	Dimension mapSize;
	Dimension[] locations;
	Clip clip;
	AudioInputStream audioIn;
	Window window;
	Thread bob;
	Thread slowPlayer;
	Thread speedPlayer;
	Thread playerProtect;
	Thread strawberry;
	Thread cooldownThread;
	Thread doubleScore;
	public Game(Window gameWindow) {
		window = gameWindow;
	}
	public void gameStart(int gameMode,int gameDifficulty,int prevLevel,int prevScore) {
		mode = gameMode;
		difficulty = gameDifficulty;
		level = prevLevel + 1;
		score = prevScore + 1;
		image = new BufferedImage(window.xSize/2,window.ySize/2,BufferedImage.TYPE_INT_ARGB);
		invincibleTime = 0;
		cooldown = 0;
		b = new ArrayList<Projectile>();
		c = new ArrayList<Projectile>();
		fireRate = 1;
		gameGraphics = image.getGraphics();
		//Create enemies
		enemies = 8-(difficulty*2);
		if(difficulty == 0) enemies = 10;
		enemy = new Enemy[enemies];
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("Maps/map1.dat")));
		mapSize = new Dimension();
		locations = new Dimension[3];
		numPacDots = 0;
		try {
			int lines = 0;
			String line = in.readLine();
			mapSize.width = line.length();
			while(line != null) {
				lines++;
				if(line.contains("X")) {
					locations[0] = new Dimension(line.indexOf("X"),lines);
				}
				if(line.contains("E")) {
					locations[1] = new Dimension(line.indexOf("E"),lines);
				}
				if(line.contains("P")) {
					locations[2] = new Dimension(line.indexOf("P"),lines);
				}
				line = in.readLine();
			}
			mapSize.height = lines;
			map = new String[lines];
			in.close();
			in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("Maps/map1.dat")));
			for(int x=0;x<lines;x++) {
				map[x] = in.readLine();
			}
			for(int x=0;x<mapSize.height;x++) {
				String myString = map[x];
				while(myString.contains("0")) {
					numPacDots++;
					myString = myString.substring(myString.indexOf("0")+1);
				}
			}
			numPowerPellets = 0;
			for(int x=0;x<mapSize.height;x++) {
				String myString = map[x];
				while(myString.contains("Q")) {
					numPowerPellets++;
					myString = myString.substring(myString.indexOf("Q")+1);
				}
			}
			pacDots = new PowerUp[numPacDots];
			int counter = 0;
			for(int x=0;x<mapSize.height;x++) {
				String myString = map[x];
				while(myString.contains("0")) {
					pacDots[counter] = new PowerUp(0,myString.indexOf("0") + map[x].length() - myString.length(),x);
					myString = myString.substring(myString.indexOf("0")+1);
					counter++;
				}
			}
			powerPellets = new PowerUp[numPowerPellets];
			counter = 0;
			for(int x=0;x<mapSize.height;x++) {
				String myString = map[x];
				while(myString.contains("Q")) {
					powerPellets[counter] = new PowerUp(1,myString.indexOf("Q") + map[x].length() - myString.length(),x);
					if(difficulty == 0 && new Random().nextInt(10) == 0) powerPellets[counter].type = 2;
					myString = myString.substring(myString.indexOf("Q")+1);
					counter++;
				}
			}
			ammo = 0;
			if(mode != 2) {
				numPowerPellets = 0;
				ammo = 100;
				powerUps = new PowerUp[2];
				powerUps[0] = createPowerUp(60);
				powerUps[1] = createPowerUp(0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Place enemies and player
		String[] names = new String[4];
		Random random = new Random();
		names[0] = "Inky";
		names[1] = "Pinky";
		names[2] = "Blinky";
		names[3] = "Clyde";
		maxHealth = 6;
		if(mode == 0) maxHealth = 2;
		player = new Player(locations[2],maxHealth);
		for(int x=0;x<enemies;x++) {
			enemy[x] = new Enemy(locations[1],names[random.nextInt(4)]);
		}
		
		//Start music
		String url;
		switch(difficulty) {
		
		case 3: url = "easy";
		 break;
		case 2: url = "menu";
		 break;
		case 1: url = "hard";
		 break;
		default: url = "insane";
		 break;
		}
		URL musicURL = getClass().getClassLoader().getResource("pacman_" + url + ".wav");
		try {
			audioIn = AudioSystem.getAudioInputStream(musicURL);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Audio is missing or corrupted.");
		}
		//Game loop
		playGame();
	}
	public void playGame() {
		bob = new Thread() {
			public void run() {
				cooldownThread.start();
				a = new ArrayList<PowerUp>();
				for(int x=0;x<enemies && mode != -1;x++) {
					for(int y=0;y<20 && mode != -1;y++) {
						enemy[x].location.height -= 0.1;
						drawGame();
					}
					for(int y=0;y<Math.abs(x-((float) enemies/2.0)) && mode != -1;y++) {
						enemy[x].location.width += Math.signum(x-(enemies)/2);
						if(String.valueOf(map[(int) enemy[x].location.height - 1].charAt((int) (enemy[x].location.width - 1))).equals("1")) {
							enemy[x].location.width -= (Math.signum((int) enemy[x].location.width - (int) (mapSize.width/2)));
							enemy[x].location.height += 1;
						}
						drawGame();
					}
				}
				playerDirection = 1;
				KeyListener key = new KeyListener() {

					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
						switch(e.getKeyCode()) {
						
						case KeyEvent.VK_W: case KeyEvent.VK_UP: playerDirection = 4;
						 break;
						case KeyEvent.VK_S: case KeyEvent.VK_DOWN: playerDirection = 2;
						 break;
						case KeyEvent.VK_A: case KeyEvent.VK_LEFT: playerDirection = 3;
						 break;
						case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: playerDirection = 1;
						 break;
						case KeyEvent.VK_P: if(! p_pressed) pause = ! pause;
											p_pressed = true;
						 break;
						case KeyEvent.VK_SPACE: if(cooldown <= 0 && mode != 2) fireBullet();
						 break;
						
						}
					}

					@Override
					public void keyReleased(KeyEvent e) {
						// TODO Auto-generated method stub
						if(e.getKeyCode() == KeyEvent.VK_P) {
							p_pressed = false;
						}
					}

					@Override
					public void keyTyped(KeyEvent e) {
						// TODO Auto-generated method stub
					}
					
				};
				window.addKeyListener(key);
				pause = false;
				while(player.health > 0 && mode != -1 && numPacDots > 0) {
					//Actual game in here
					if(! pause) {
						
						//player movement
						for(int x=0;x<5 && mode>-1;x++) {
							playerMove();
							player.location.width = (player.location.width % mapSize.width + mapSize.width) % mapSize.width;
							player.location.height = (player.location.height % mapSize.height + mapSize.height) % mapSize.height;
							for(Enemy e : enemy) {
								e.location.width = (e.location.width % mapSize.width + mapSize.width) % mapSize.width;
								e.location.height = (e.location.height % mapSize.height + mapSize.height) % mapSize.height;
							}
							for(Projectile p : b.toArray(new Projectile[0])) {
								moveProjectile(p);
							}
							enemyMove();
							drawGame();
							
							//Decrement invincibility time
							invincibleTime--;
						}
						player.location.width = Math.round(player.location.width*4.0)/4.0;
						player.location.height = Math.round(player.location.height*4.0)/4.0;
						player.status--;
						if(player.status==1) {
							player.status = 11;
							player.health--;
						}
						if(player.health==1) {
							PrintWriter writer;
							try {
								FileReader oj;
								oj = new FileReader("save.dat");
								BufferedReader gojao = new BufferedReader(oj);
								String ghj = gojao.readLine();
								gojao.close();
								ghj = ghj.substring(0,4) + "1" + ghj.substring(5);
								writer = new PrintWriter("save.dat");
								writer.print(ghj);
								writer.flush();
								writer.close();
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						if(player.location.width % 1 == 0 && player.location.height % 1 == 0 && player.direction>=0) player.direction = playerDirection;
						if(player.direction<0) player.direction++;
						for(Enemy e : enemy) {
							e.location.width = Math.round(e.location.width*4.0)/4.0;
							e.location.height = Math.round(e.location.height*4.0)/4.0;
							if(e.location.width % 1 == 0 && e.location.height % 1 == 0 && e.direction>=0)  e.direction = new Random().nextInt(4) + 1;
							if(player.location.width == e.location.width && player.location.height == e.location.height) {
								if(invincibleTime <= 0) {
									player.health -= 2;
								} else {
									e.health = 0;
								}
							}
							if(e.health <= 0) {
								e.location.width = locations[1].width + 1;
								e.location.height = locations[1].height;
								score += 30 + difficulty < 2 ? 20 : 0;
								e.respawnTime = 250;
								PrintWriter writer;
								try {
									FileReader oj;
									oj = new FileReader("save.dat");
									BufferedReader gojao = new BufferedReader(oj);
									String ghj = gojao.readLine();
									gojao.close();
									int ghostKill = Integer.parseInt(ghj.substring(2,4));
									if(ghostKill<99) ghostKill++;
									ghj = ghj.substring(0,2) + String.format("%02d",ghostKill) + ghj.substring(4);
									writer = new PrintWriter("save.dat");
									writer.print(ghj);
									writer.flush();
									writer.close();
								} catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								if(e.status>=-41 && e.status<0) {
									PrintWriter writeR;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,21) + "1" + ghj.substring(22);
										writeR = new PrintWriter("save.dat");
										writeR.print(ghj);
										writeR.flush();
										writeR.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else if(e.status<-41) {
									PrintWriter writeR;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,22) + "1" + ghj.substring(23);
										writeR = new PrintWriter("save.dat");
										writeR.print(ghj);
										writeR.flush();
										writeR.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else if(e.status>0 && e.status<=21) {
									PrintWriter writeR;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,25) + "1" + ghj.substring(26);
										writeR = new PrintWriter("save.dat");
										writeR.print(ghj);
										writeR.flush();
										writeR.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else if(e.status>21) {
									PrintWriter writeR;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,26) + "1" + ghj.substring(27);
										writeR = new PrintWriter("save.dat");
										writeR.print(ghj);
										writeR.flush();
										writeR.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
								if(e.direction<0) {
									PrintWriter writeR;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,24) + "1" + ghj.substring(25);
										writeR = new PrintWriter("save.dat");
										writeR.print(ghj);
										writeR.flush();
										writeR.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
							if(e.status == -42) {
								e.status = -52;
								e.health--;
							} else if(e.status >= -41 && e.status < 0) {
								e.direction = 0;
							} else if(e.status == 1 || e.status == 22) {
								e.health = 0;
							} else if(e.status<=-42) {
								for(Enemy f : enemy) {
									if(e.location.width == f.location.width && e.location.height == f.location.height && e!=f) f.status = -52;
								}
								if(player.location.width==e.location.width && player.location.height==e.location.height) player.status = 11;
							} else if(e.status>21) {
								for(Enemy f : enemy) {
									if(e.location.width == f.location.width && e.location.height == f.location.height && e!=f) f.status = 42;
								}
							}
							e.status -= Math.signum(e.status);
							if(e.direction<0) e.direction++;
						}
						for(int x=0;x<numPacDots;x++) {
							if(player.location.width == pacDots[x].location.width && player.location.height == pacDots[x].location.height) {
								pacDots[x] = pacDots[numPacDots-1];
								pacDots[numPacDots-1] = null;
								numPacDots--;
								score += 10;
								if(mode != 2) ammo++;
								PrintWriter writer;
								try {
									FileReader oj;
									oj = new FileReader("save.dat");
									BufferedReader gojao = new BufferedReader(oj);
									String ghj = gojao.readLine();
									gojao.close();
									int pacNum = Integer.parseInt(ghj.substring(7,11));
									if(pacNum<9999) pacNum++;
									ghj = ghj.substring(0,7) + String.format("%04d",pacNum) + ghj.substring(11);
									writer = new PrintWriter("save.dat");
									writer.print(ghj);
									writer.flush();
									writer.close();
								} catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
						for(int x=0;x<numPowerPellets;x++) {
							if(player.location.width == powerPellets[x].location.width && player.location.height == powerPellets[x].location.height) {
								if(powerPellets[x].type==1) {
									invincibleTime = 250;
									score += 100;
									PrintWriter writer;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,11) + "1" + ghj.substring(12);
										writer = new PrintWriter("save.dat");
										writer.print(ghj);
										writer.flush();
										writer.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								} else {
									slowPlayer.start();
									PrintWriter writer;
									try {
										FileReader oj;
										oj = new FileReader("save.dat");
										BufferedReader gojao = new BufferedReader(oj);
										String ghj = gojao.readLine();
										gojao.close();
										ghj = ghj.substring(0,12) + "1" + ghj.substring(13);
										writer = new PrintWriter("save.dat");
										writer.print(ghj);
										writer.flush();
										writer.close();
									} catch (FileNotFoundException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
								powerPellets[x] = powerPellets[numPowerPellets-1];
								numPowerPellets--;
							}
						}
						if(mode != 2) {
							for(PowerUp p : powerUps) {
								p.duration--;
								if(p.duration > 0) {
									if(player.location.width == p.location.width && player.location.height == p.location.height) {
										doSomething(p);
										p.duration = 0;
									}
								}
								if(p.duration <= -30) {
									p = createPowerUp(60);
								}
							}
							for(Projectile p : b.toArray(new Projectile[0])) {
								for(Enemy e : enemy) {
									if(p.location.width == e.location.width && p.location.height == e.location.height) {
										switch(p.type) {
										
										case 0: e.health -= 1;
												b.remove(p);
										 break;
										case 1: if(e.status>=-41) e.status = -41 + difficulty < 2 ? 20 + difficulty == 0 ? 10 : 0 : 0;
												if(e.status<-41) e.status = 0;
												b.remove(p);
										 break;
										case 2: if(e.status==0) e.status = -52;
												if(e.status<0 && e.status>=-41) e.status = 0;
												b.remove(p);
										 break;
										case 3: e.health --;
												p.direction = ((p.direction + 1) % 4) + 1;
												if(p.location.width % 1 != 0 || p.location.height % 1 != 0) moveProjectile(p);
												p.count--;
												PrintWriter writeR;
												try {
													FileReader oj;
													oj = new FileReader("save.dat");
													BufferedReader gojao = new BufferedReader(oj);
													String ghj = gojao.readLine();
													gojao.close();
													ghj = ghj.substring(0,23) + "1" + ghj.substring(24);
													writeR = new PrintWriter("save.dat");
													writeR.print(ghj);
													writeR.flush();
													writeR.close();
												} catch (FileNotFoundException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												if(p.count == 0) b.remove(p);
										 break;
										case 4: p.location.width = Math.floor(p.location.width);
												p.location.height = Math.floor(p.location.height);
												p.direction = 0;
												if(p.count == -1) p.count = 60;
												if(e.direction>0) e.direction = -20;
										 break;
										case 5: if(e.status==0) e.status = 21;
												if(e.status>21) e.status = 0;
												b.remove(p);
										 break;
										case 6: if(e.status==0) e.status = 42;
												if(e.status>0 && e.status<42) e.status = 0;
												b.remove(p);
										 break;
										}
									}
								}
								if(p.type==4 && p.direction==0) {
									for(Projectile z : b.toArray(new Projectile[0])) {
										if(p.location.width == z.location.width && p.location.height == z.location.height) z.type = 0;
									}
									if(player.location.width==p.location.width && player.location.height==p.location.height && player.direction>0) player.direction = -20;
									p.count--;
									if(p.count == 0) b.remove(p);
								}
							}
						}
					}
					drawGame();
				}
				if(player.health <= 0) {
					JOptionPane.showMessageDialog(null,"Sorry, you lost.");
					image = null;
					gameGraphics = null;
					map = null;
					player = null;
					enemy = null;
					pacDots = null;
					powerPellets = null;
					powerUps = null;
					a = null;
					b = null;
					c = null;
					mapSize = null;
					locations = null;
					clip = null;
					audioIn = null;
					bob = null;
					slowPlayer = null;
					speedPlayer = null;
					playerProtect = null;
					strawberry = null;
					cooldownThread = null;
					doubleScore = null;
					PrintWriter writer;
					try {
						FileReader oj;
						oj = new FileReader("save.dat");
						BufferedReader gojao = new BufferedReader(oj);
						String ghj = gojao.readLine();
						gojao.close();
						ghj = ghj.substring(0,4) + "11" + ghj.substring(6);
						writer = new PrintWriter("save.dat");
						writer.print(ghj);
						writer.flush();
						writer.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if(numPacDots <= 0) {
					score += level * 1000;
					if(difficulty<3) {
						score+=100;
						if(difficulty<2) {
							score+=400;
							if(difficulty<1) {
								score+=500;
							}
						}
					}
					if(level==9 && mode==2) {
						PrintWriter writer;
						try {
							FileReader oj;
							oj = new FileReader("save.dat");
							BufferedReader gojao = new BufferedReader(oj);
							String ghj = gojao.readLine();
							gojao.close();
							ghj = "1" + ghj.substring(1);
							writer = new PrintWriter("save.dat");
							writer.print(ghj);
							writer.flush();
							writer.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if(score>=100000 && mode==1) {
						PrintWriter writer;
						try {
							FileReader oj;
							oj = new FileReader("save.dat");
							BufferedReader gojao = new BufferedReader(oj);
							String ghj = gojao.readLine();
							gojao.close();
							ghj = "11" + ghj.substring(2);
							writer = new PrintWriter("save.dat");
							writer.print(ghj);
							writer.flush();
							writer.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					JOptionPane.showMessageDialog(null,"You have reached the next level!");
					JOptionPane.showMessageDialog(null,"Total score: " + score);
					gameStart(mode,difficulty,level,score);
				}
				clip.stop();
				window.repaint();
			}
		};
		slowPlayer = new Thread() {
			public void run() {
				player.speed /= 2.0;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				player.speed *= 2.0;
			}
		};
		speedPlayer = new Thread() {
			public void run() {
				player.speed *= 2.0;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				player.speed /= 2.0;
			}
		};
		playerProtect = new Thread() {
			public void run() {
				int playerHealth = player.health;
				for(int x=0;x<1000;x++) {
					player.health = playerHealth;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		strawberry = new Thread() {
			public void run() {
				ammo = 100;
				fireRate = 4;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fireRate = 1;
			}
		};
		cooldownThread = new Thread() {
			public void run() {
				while(mode != -1) {
					cooldown--;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		doubleScore = new Thread() {
			public void run() {
				int scoreA = score;
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				score += score-scoreA;
			}
		};
		bob.start();
	}
	public void fireBullet() {
		b.add(c.get(0) == null && ammo-- < 5 ? new Projectile(player.location,player.direction,0) : fireBall());
		cooldown = 100/fireRate;
	}
	public Projectile fireBall() {
		ammo -= 4;
		c.get(0).location = player.location;
		c.get(0).direction = player.direction;
		if(c.get(0).type == 3) c.get(0).count = 10;
		return c.remove(0);
	}
	public PowerUp createPowerUp(int duration) {
		Random random = new Random();
		int randNum = random.nextInt(1000);
		int powerUpClass;
		int type;
		int x;
		int y;
		PowerUp newPowerUp;
		if(randNum < 500) {
			powerUpClass = 0;
		} else if(randNum < 800) {
			powerUpClass = 1;
		} else if(randNum < 950){
			powerUpClass = 2;
		} else if(randNum < 997) {
			powerUpClass = 3;
		} else {
			powerUpClass = 4;
		}
		type = 0;
		if(mode == 1) {
			switch(powerUpClass) {
				
			case 0: type = 1 + (difficulty == 0 && random.nextInt(10) == 0 ? 1 : 0);
			 break;
			case 1: type = 8;
			 break;
			case 2: type = random.nextInt(2) + 6;
			 break;
			case 3: type = random.nextInt(3) + 3;
			 break;
			
			}
		} else if(mode == 0) {
			if(difficulty > 1) powerUpClass = 0;
			switch(powerUpClass) {
				
			case 0: type = 1 + (difficulty == 0 && random.nextInt(10) == 0 ? 1 : 0);
			 break;
			default: type = random.nextInt(2) + 7 + powerUpClass * 2;
			 break;
			
			}
		}
		x = 0;
		y = 0;
		while(!String.valueOf(map[y].charAt(x)).equals("0")) {
			x = random.nextInt(mapSize.width);
			y = random.nextInt(mapSize.height);
		}
		newPowerUp = new PowerUp(type,x,y);
		newPowerUp.duration = duration;
		return newPowerUp;
	}
	public void playerMove() {
		switch(player.direction) {
		
		case 1: player.location.width += String.valueOf(map[(int) (player.location.height - 1)].charAt((int) (player.location.width) % mapSize.width)).equals("1") ? 0 : (0.1*player.speed);
				break;
		case 2: player.location.height += String.valueOf(map[(int) (player.location.height) % mapSize.height].charAt((int) (player.location.width - 1))).equals("1") ? 0 : (0.1*player.speed);
				break;
		case 3: player.location.width -= String.valueOf(map[(int) (player.location.height - 1)].charAt((int) Math.ceil(player.location.width + mapSize.width - 2) % mapSize.width)).equals("1") ? 0 : (0.1*player.speed);
				break;
		case 4: player.location.height -= String.valueOf(map[(int) Math.ceil(player.location.height + mapSize.height - 2) % mapSize.height].charAt((int) (player.location.width - 1))).equals("1") ? 0 : (0.1*player.speed);
				break;
		default: break;
		
		}
	}
	public void moveProjectile(Projectile p) {
		switch(p.direction) {
		
		case 1: p.location.width += String.valueOf(map[(int) (p.location.height - 1)].charAt((int) (p.location.width) % mapSize.width)).equals("1") ? doWall(p) : 0.2;
				break;
		case 2: p.location.height += String.valueOf(map[(int) (p.location.height) % mapSize.height].charAt((int) (p.location.width - 1))).equals("1") ? doWall(p) : 0.2;
				break;
		case 3: p.location.width -= String.valueOf(map[(int) (p.location.height - 1)].charAt((int) Math.ceil(p.location.width + mapSize.width - 2) % mapSize.width)).equals("1") ? doWall(p) : 0.2;
				break;
		case 4: p.location.height -= String.valueOf(map[(int) Math.ceil(p.location.height + mapSize.height - 2) % mapSize.height].charAt((int) (p.location.width - 1))).equals("1") ? doWall(p) : 0.2;
				break;
		default: break;
		
		}
	}
	public void enemyMove() {
		for(Enemy e : enemy) {
			if(e.respawnTime == 0) {
				try {
					switch(e.direction) {
					
					case 1: e.location.width += String.valueOf(map[(int) (e.location.height + mapSize.height- 1) % mapSize.height].charAt((int) (e.location.width + mapSize.width) % mapSize.width)).equals("1") ? 0 : e.status>0 && e.status<=21 ? 0.2 : e.status>21 ? 0.05 : 0.1;
							break;
					case 2: e.location.height += String.valueOf(map[(int) (e.location.height + mapSize.height) % mapSize.height].charAt((int) (e.location.width + mapSize.width- 1) % mapSize.width)).equals("1") ? 0 : e.status>0 && e.status<=21 ? 0.2 : e.status>21 ? 0.05 : 0.1;
							break;
					case 3: e.location.width -= String.valueOf(map[(int) (e.location.height + mapSize.height- 1) % mapSize.height].charAt((int) Math.ceil(e.location.width + mapSize.width - 2) % mapSize.width)).equals("1") ? 0 : e.status>0 && e.status<=21 ? 0.2 : e.status>21 ? 0.05 : 0.1;
							break;
					case 4: e.location.height -= String.valueOf(map[(int) Math.ceil(e.location.height + mapSize.height - 2) % mapSize.height].charAt((int) (e.location.width + mapSize.width - 1) % mapSize.width)).equals("1") ? 0 : e.status>0 && e.status<=21 ? 0.2 : e.status>21 ? 0.05 : 0.1;
							break;
					default: break;
					
					}
				} catch(NullPointerException n) {
					//Do nothing
				}
			} else {
				e.respawnTime--;
			}
		}
	}
	public double doWall(Projectile p) {
		switch(p.type) {
		
		case 3: p.direction = ((p.direction + 1) % 4) + 1;
				p.count--;
		 break;
		case 4: p.direction = 0;
				p.count = 60;
		 break;
		default: c.remove(p);
		 break;
		
		}
		return p.type == 3 ? 0.2 : 0;
	}
	public void doSomething(PowerUp p) {
		switch(p.type) {
		
		case 1: invincibleTime = 250;
		 break;
		case 2: slowPlayer.start();
		 break;
		case 3: speedPlayer.start();
				playerProtect.start();
				score += 200;
				doubleScore.start();
				PrintWriter writer;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,13) + "1" + ghj.substring(14);
					writer = new PrintWriter("save.dat");
					writer.print(ghj);
					writer.flush();
					writer.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 4: player.health = 6;
				score += 2000;
				PrintWriter writeR;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,14) + "1" + ghj.substring(15);
					writeR = new PrintWriter("save.dat");
					writeR.print(ghj);
					writeR.flush();
					writeR.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 5: player.health = Math.min(player.health+1,maxHealth);
				score += 1000;
				createBomb(5);
				PrintWriter writER;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,15) + "1" + ghj.substring(16);
					writER = new PrintWriter("save.dat");
					writER.print(ghj);
					writER.flush();
					writER.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 6: player.health = Math.min(player.health+1,maxHealth);
				score += 500;
				createBomb(6);
				createBomb(6);
				PrintWriter wriTER;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,16) + "1" + ghj.substring(17);
					wriTER = new PrintWriter("save.dat");
					wriTER.print(ghj);
					wriTER.flush();
					wriTER.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 7: strawberry.start();
				score += 1000;
				PrintWriter wrITER;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,17) + "1" + ghj.substring(18);
					wrITER = new PrintWriter("save.dat");
					wrITER.print(ghj);
					wrITER.flush();
					wrITER.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 8: player.health = Math.min(6,player.health+1);
				score += 500;
				ammo = 100;
				PrintWriter wRITER;
				try {
					FileReader oj;
					oj = new FileReader("save.dat");
					BufferedReader gojao = new BufferedReader(oj);
					String ghj = gojao.readLine();
					gojao.close();
					ghj = ghj.substring(0,18) + "1" + ghj.substring(19);
					wRITER = new PrintWriter("save.dat");
					wRITER.print(ghj);
					wRITER.flush();
					wRITER.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		 break;
		case 9: case 10: case 11: case 12: case 13: case 14: addBall(p.type-8);
				score += 100 + p.type>10 ? 400 + p.type>12 ? 500 : 0 : 0;
		 break;
		}
		PrintWriter writer;
		try {
			FileReader oj;
			oj = new FileReader("save.dat");
			BufferedReader gojao = new BufferedReader(oj);
			String ghj = gojao.readLine();
			gojao.close();
			ghj = ghj.substring(0,6) + "1" + ghj.substring(7);
			writer = new PrintWriter("save.dat");
			writer.print(ghj);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void addBall(int ballType) {
		c.add(new Projectile(player.location,player.direction,ballType));
	}
	public void createBomb(final int type) {
		a.add(new PowerUp(type,(int) player.location.width-1,(int) player.location.height-1));
		KeyListener l = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getKeyCode() == KeyEvent.VK_Z) {
					if(type == 5) {
						for(Enemy e : enemy){
							e.location.width = locations[1].width + 1;
							e.location.height = locations[1].height;
							e.respawnTime = 250;
						}
					} else if(type == 6) {
						for(int x=0;x<3000;x++) {
							a.get(0).draw(gameGraphics,mapSize,window);
							try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						for(Enemy e : enemy) {
							if(Math.abs(e.location.width - (double) a.get(0).location.width) <= 1 && Math.abs(e.location.height - (double) a.get(0).location.height) <= 1) {
								e.location.width = locations[1].width + 1;
								e.location.height = locations[1].height;
								e.respawnTime = 250;
							}
						}
					}
					window.removeKeyListener(this);
					a.remove(0);
					PrintWriter writer;
					try {
						FileReader oj;
						oj = new FileReader("save.dat");
						BufferedReader gojao = new BufferedReader(oj);
						String ghj = gojao.readLine();
						gojao.close();
						ghj = ghj.substring(0,type+14) + "1" + ghj.substring(type+15);
						writer = new PrintWriter("save.dat");
						writer.print(ghj);
						writer.flush();
						writer.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
		};
		window.addKeyListener(l);
	}
	public void drawHearts() {
		int drawHealth = player.health;
		ImageIcon heartImage;
		String url;
		for(int x=0;x<maxHealth/2.0;x++) {
			if(drawHealth <= 0) {
				url = "Empty";
			} else if(drawHealth == 1) {
				url = "Half";
			} else {
				url = "Full";
			}
			imageURL = Main.class.getResource("Player Graphics/Heart (" + url + ").png");
			heartImage = new ImageIcon(imageURL);
			gameGraphics.drawImage(heartImage.getImage(),x*window.xSize/(2*mapSize.width),0,window.xSize/(2*mapSize.width),window.ySize/(2*mapSize.height),null);
			drawHealth -= 2;
		}
	}
	public void drawGame() {
		gameGraphics.setColor(Color.BLACK);
		gameGraphics.fillRect(0,0,window.xSize/2,window.ySize/2);
		gameGraphics.setColor(Color.BLUE);
		for(int x=0;x<mapSize.height;x++) {
			String line = map[x];
			while(line.contains("1")) {
				gameGraphics.fillRect((line.indexOf("1") + map[x].length()-line.length())*window.xSize/(2*mapSize.width),x*(window.ySize/(2*mapSize.height)),window.xSize/(2*mapSize.width)+1,window.ySize/(2*mapSize.height)+1);
				line = line.substring(line.indexOf("1")+1);
			}
		}
		drawHearts();
		for(int x = 0;x<numPacDots;x++) {
			pacDots[x].draw(gameGraphics,mapSize,window);
		}
		for(int x = 0;x<numPowerPellets;x++) {
			powerPellets[x].draw(gameGraphics,mapSize,window);
		}
		if(mode != 2 && mode >= 0) {
			for(PowerUp p : powerUps) {
				if(p.duration > 0) p.draw(gameGraphics,mapSize,window);
			}
		}
		for(Projectile p : b.toArray(new Projectile[0])) {
			p.draw(gameGraphics,mapSize,window);
		}
		//Removed image because it's not to scale
		//gameGraphics.drawImage(background.getImage(),0,0,window.xSize/2,window.ySize/2,null);
		for(Enemy p: enemy) {
			gameGraphics.drawImage(p.image(invincibleTime).getImage(),(int) ((window.xSize/(mapSize.width*2)+1)*(p.location.width-1)),(int) (window.ySize/(mapSize.height*2)*(p.location.height-1)),(int) ((window.xSize/(mapSize.width*2)+1)*p.location.width),(int) (window.ySize/(mapSize.height*2)*p.location.height),0,0,p.image(invincibleTime).getIconWidth(),p.image(invincibleTime).getIconHeight(),null);
		}
		gameGraphics.drawImage(player.image().getImage(),(int) ((window.xSize/(mapSize.width*2)+1)*(player.location.width-1)),(int) (window.ySize/(mapSize.height*2)*(player.location.height-1)),(int) ((window.xSize/(mapSize.width*2)+1)*(player.location.width)),(int) (window.ySize/(mapSize.height*2)*player.location.height),0,0,player.image().getIconWidth(),player.image().getIconHeight(),null);
		window.draw(new ImageIcon(image),window.xSize/4,window.ySize/4,window.xSize/4*3,window.ySize/4*3);
		window.repaint();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return;
		}
	}
}
