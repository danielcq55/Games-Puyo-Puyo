/**********************************************************
 * Minimal version of Puyo-Puyo Game 
 * See README for rules of the game.
 * 1) User can't rotate the pair of blocks  she/he can move.
 * 2) Blocks are circles without images.
 * 
 * Author: Daniel Castanon-Quiroz 
 * email: danielcq55@gmail.com
 */




import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The Skeleton of this class comes from the book 
 *  Killer Game Programming in Java, A. Davison , 2005.  
 */


public class PuyoFrame extends JFrame implements WindowListener

{
  private static int DEFAULT_FPS = 80;

  private PuyoPanel myPanel;        // where the worm is drawn
  private JTextField jtfBox;   // displays no.of boxes used
  private JTextField jtfTime;  // displays time spent in game


  public PuyoFrame(int period)
  { super("2D Puyo Game");
    makeGUI(period);

    addWindowListener( this );
    pack();
    setResizable(false);
    setVisible(true);
  }  // end of WormChase() constructor


  private void makeGUI(int period)
  {
    Container c = getContentPane();    // default BorderLayout used

    myPanel = new PuyoPanel(this, period);
    c.add(myPanel, "Center");

    JPanel ctrls = new JPanel();   // a row of textfields
    ctrls.setLayout( new BoxLayout(ctrls, BoxLayout.X_AXIS));

    jtfBox = new JTextField("Score: 0");
    jtfBox.setEditable(false);
    ctrls.add(jtfBox);

    jtfTime = new JTextField("Time: 0 secs");
    jtfTime.setEditable(false);
    ctrls.add(jtfTime);

    c.add(ctrls, "South");
  }  // end of makeGUI()


  public void setScoreNumber(long score)
  {  jtfBox.setText("Score: " + score);  }

  public void setTimeSpent(long t)
  {  jtfTime.setText("Time: " + t + " secs"); }
  

  // ----------------- window listener methods -------------

  public void windowActivated(WindowEvent e) 
  { myPanel.resumeGame();  }

  public void windowDeactivated(WindowEvent e) 
  {  myPanel.pauseGame();  }


  public void windowDeiconified(WindowEvent e) 
  {  myPanel.resumeGame();  }

  public void windowIconified(WindowEvent e) 
  {  myPanel.pauseGame(); }


  public void windowClosing(WindowEvent e)
  {  myPanel.stopGame();  }

  public void windowClosed(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

  // ----------------------------------------------------

  public static void main(String args[])
  { 
    int fps = DEFAULT_FPS;
    if (args.length != 0)
      fps = Integer.parseInt(args[0]);

    int period = (int) 1000.0/fps;
    System.out.println("fps: " + fps + "; period: " + period + " ms");

    new PuyoFrame(period);    // ms
  }

} // end of WormChase class

