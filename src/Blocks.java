/**********************************************************
 * Version of Puyo-Puyo Game 
 * See README for rules of the game.
 * 1) User can rotate the pair of blocks.
 * 2) Blocks are  circles without images.
 * 
 * Author: Daniel Castanon-Quiroz 
 * email: danielcq55@gmail.com
 */







import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;

class Block{
    
    int 	  centerX,centerY;
    int 	  radius;
    Color 	  color;    
    int       index;      //place in the pipe
    int       pipe;       //number of the pipe who gets this block, -1 if there is not in a pipe
    boolean  in_CR_queue;   //is it in a chain reaction queue 
    
    
    
    Block(int centerX, int centerY, int radius,Color color){
     this.centerX= centerX;   
     this.centerY= centerY;
     this.radius = radius;
     this.color  = color;     
     
     in_CR_queue    = false; 
     index       = -1 ;  
     pipe        = -1 ;  
    }
    
    
    public void paint(Graphics g){
        

        g.setColor(color);
        g.fillOval(centerX-radius,centerY-radius, 2*radius,2*radius);
        
        
    }
    
    
    /*/////////////////////////////////////////////////////////////////
    Collision tests with other blocks
    *//////////////////////////////////////////////////////////////////

    public boolean CollideDown(Block b){
        
        return  centerY+radius >= b.centerY-radius;
        
    }
    
    
    public boolean CollideLeft(Block b){
        
        return   b.centerX+radius <= centerX-radius ;
        
    }
    
    public boolean CollideRight(Block b){
        
        return  b.CollideLeft(this);
        
    }  
    
}


//Our games consist of pipes
//where the blocks can travel  
class BlockPipe{
  
  
  LinkedList blocks;

  
  public BlockPipe(){
      blocks= new LinkedList();
  }
  


  public void paint(Graphics g){
      
      Iterator itr= blocks.iterator();
      
      while(itr.hasNext())
          {
              Object element=   itr.next();
              Block b = (Block) element;
              b.paint(g);
          }
        
      
  }
  
  public int getSize()
  {
	  
	  return blocks.size();
  }
  
  
}


//Pair of blocks
class BlockPair{
  
      Block blockA; 
      Block blockB;
      int   blockB_OR;  //  Orientation 0 on right, 1 on top,  2 on left, 3 on bottom

      
  BlockPair(){

	  blockB_OR=0;// by default A is at the left and B at the right
  
  }
    
      public void paint(Graphics g){
          blockA.paint(g);
          blockB.paint(g);
      }
     
}