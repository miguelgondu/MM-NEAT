package me.jakerg.rougelike;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import edu.southwestern.tasks.gvgai.zelda.level.Dungeon;
import me.jakerg.rougelike.screens.*;


/**
 * Rougelike app to simulate Zelda dungeons
 * 
 * Starter code is from : http://trystans.blogspot.com/
 * @author gutierr8
 *
 */
public class RougelikeApp extends JFrame implements KeyListener{
	private static final long serialVersionUID = 1060623638149583738L;
	
	private AsciiPanel terminal;
	private Screen screen; // Which screen to display?
	
	/**
	 * Constructor to test basic Rougelike functionality
	 */
	public RougelikeApp() {
		super();
		terminal = new AsciiPanel();
		terminal.setAsciiFont(AsciiFont.TALRYTH_15_15);
		add(terminal);
		pack();
		screen = new StartScreen();
		addKeyListener(this);
		repaint();
	}
	
	public RougelikeApp(Dungeon dungeon) {
		super();
		terminal = new AsciiPanel();
		terminal.setAsciiFont(AsciiFont.CP437_16x16); // Set Asciifont to appear bigger
		add(terminal);
		pack();
		screen = new StartScreen(dungeon); // Set the start screen with a dungeon to let start screen know that we want to play the dungon provided
		addKeyListener(this);
		repaint();
	}
	
	/**
	 * Anytime there is input this is called to display to the terminal
	 */
	public void repaint() {
		terminal.clear();
		screen.displayOutput(terminal);
		super.repaint();
	}

	public void keyTyped(KeyEvent e) {} // Not used 

	/**
	 * Whenever a key is pressed get the screen from the input and repaint
	 */
	public void keyPressed(KeyEvent e) {
		screen = screen.respondToUserInput(e);
		repaint();
	}

	public void keyReleased(KeyEvent e) {} // Not used
	
	/**
	 * Main method to test rougelike w/o dungeon (will load random caves)
	 * @param args
	 */
	public static void main(String[] args) {
		RougelikeApp app = new RougelikeApp();
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setVisible(true);
	}
	
	/**
	 * Function to call if there is a dungeon to be used
	 * @param dungeon Dungeon to be played
	 */
	public static void startDungeon(Dungeon dungeon) {
		RougelikeApp app = new RougelikeApp(dungeon);
		app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close closes that window ONLY not every JFrame window
		app.setVisible(true);
	}

}
