/**********************************************************
 * Minimal version of Puyo-Puyo Game 
 * See README for rules of the game.
 * 1) User can't rotate the pair of blocks  she/he can move.
 * 2) Blocks are circles without images.
 * 
 * Author: Daniel Castanon-Quiroz 
 * email: danielcq55@gmail.com
 */





import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;


interface GameParameters{
    
    int width=252;
    int height=width*2;
    int num_pipes   = 6;
    int block_radius = width/num_pipes/2 ;
   //Colors of the blocks    
    Color colors[]={Color.RED,Color.BLUE,Color.GREEN, Color.PINK,Color.BLACK, Color.ORANGE,Color.MAGENTA};
    //Orientations for blocks
    int RIGHT=1,LEFT=2,DOWN=3, UP=4;
    //Maximum number of blocks allow in a pipe    
    int max_nblocks_in_pipe=10;
    //Minimum number of blocks of the same color
    //to cause a chain reaction and make them vanish
    int n_chain_reaction=4;
}

public class PuyoGame  implements GameParameters {
	
	BlockPair   current_pair;   // pair of blocks that the user can move
    BlockPipe   pipes[];        //list of pipes in our game board
    LinkedList  floatingBlocks; //the blocks who are falling down and user has no control of them
    long   score=0;
    

    public void StartGame()
    {
     
    	current_pair   = new BlockPair();
    	floatingBlocks = new LinkedList();           
    	pipes=    new BlockPipe[num_pipes];
    
    	for(int i =0;i<num_pipes;++i)
    		pipes[i]=  new BlockPipe();        
        
    	ReleasePair();//release the first pair
                
    }


    public void ReleasePair()
    {
        
    	Random r= new Random();
    	//prepare the left block
    	int blkradius =  block_radius;
    	//by default the pair is located always in the pipes number 2 and 3
    	//since the blocks have to be glued
    	int left_pipe  =2;
     	
    	int color_index=r.nextInt(7); 
    	
    	Block left_block  = 
    			new Block(blkradius+   left_pipe*2*blkradius, height/6 ,blkradius, colors[color_index]); 
    	left_block.pipe= left_pipe;
     
    	//prepare the right block
    	color_index=r.nextInt(7); 
    	int right_pipe =left_pipe + 1;

    	Block right_block = 
    			new Block(blkradius +  right_pipe*2*blkradius, height/6+ height/6*0 ,blkradius, colors[color_index]);
                       right_block.pipe= right_pipe;
          
         
    	
        //store them as the current  pair to control
        current_pair.left =left_block;
        current_pair.right=right_block;
                 
    }    
 


    public boolean Update(){

    	MoveFloatingBlocks(); 
    	MovePair();
    	boolean gameOver=false;
    	for(int i =0;(i<num_pipes)&&!gameOver;++i)
    			gameOver= (pipes[i].getSize()> max_nblocks_in_pipe);        
        
    	
    	return gameOver;
    }
    
    public long GetScore(){
    	
    	return score;
    }

    
    public void Render(Graphics g)
    {
         g.setColor(Color.BLACK);
                
         current_pair.paint(g);
	 
         for(int i =0;i<num_pipes;++i)
        	 pipes[i].paint(g);     
	 
         for(int i =0;i< floatingBlocks.size();++i)
         {
        	 Block blk__= (Block) floatingBlocks.get(i); 
        	 blk__.paint(g);           
	     }
          
          
    }         

    //Move a block that is user control free
    //return true if the block moves, false otherwise
    private boolean MoveBlock(Block blk){
    
    	if(  ! pipes[blk.pipe].blocks.isEmpty())  
    	{    
    		if( ! blk.CollideDown( (Block) pipes[blk.pipe].blocks.getLast() ) )  //Check Collision with                                                                                                     // last block of the Pipe
    		{ blk.centerY+=1; return true;}//advance one pixel
	  
    		else     
    		{
    	   		// if does  collide, add to the current pipe   
    			blk.index = pipes[blk.pipe].blocks.size();//store place 
    			pipes[blk.pipe].blocks.addLast(blk);            
    
    			return false;
      
    		}
   

    	}
  
  
    	//if does not hit the ground, then move
    	else if(!CheckBoundaries(blk,DOWN)){
  		blk.centerY+=1;
  		return true;

    	}
    	//if hits the ground
    	else      
    	{
    		blk.index = pipes[blk.pipe].blocks.size();
    		pipes[blk.pipe].blocks.addLast(blk);                                            
    		return false;          
    	}

      
    }

    //move the current blockPair
    private void MovePair(){
      
    //move the blocks
      boolean b1__ = MoveBlock(current_pair.left) ;
      boolean b2__ = MoveBlock(current_pair.right); 
      

      // left moves, right does not
      if( b1__&& (!b2__) ){

      floatingBlocks.addLast(current_pair.left);//left block is now user control free
      //Checks  chain reaction for the right block that is not moving anymore!!
      SeeChainReaction(current_pair.right);
      ReleasePair();//release another BlockPair for user control      

      }
       
      
      //right moves, left does not
      else if(b2__ && (!b1__) ){
                floatingBlocks.addLast(current_pair.right);//left block is now user control free
                //Checks  chain reaction for the left block that is not moving anymore!!             
                SeeChainReaction(current_pair.left);
                ReleasePair();//release another BlockPair for user control      

      }
      
      //both are not moving
      else if(! (b1__ && b2__))
          {
          
    	  if(current_pair.left.color!=current_pair.right.color)             
    		  SeeChainReaction(current_pair.left);
                
    	  SeeChainReaction(current_pair.right);
    	  ReleasePair();//release another BlockPair for user control                
               
          }
         
  
      
    }


    //Move those blocks that are  user free
    private void MoveFloatingBlocks()
    {
    
    	ListIterator itr= floatingBlocks.listIterator();
    	while(itr.hasNext())
    	{
            Block blk= (Block) itr.next(); 
            
            if(!MoveBlock(blk))             //if doesn´t move no more 
            {
               ListIterator itr__= itr;
               itr.previous();   //get previous element
               
                               
                //Checks  chain reaction!!
                SeeChainReaction((Block) itr.next());
                
                
               itr.previous();   //save previous element
               
               itr__.remove();  //remove
               
            }   
                       
       }    
}

    
    public void ProcessKey(KeyEvent e){
    	
    	
        int key= e.getKeyCode();

        switch(key){
            
            
            case KeyEvent.VK_LEFT:
                MovePairTo(LEFT);
                break;
                
           case KeyEvent.VK_RIGHT:
                MovePairTo(RIGHT);
                break;
                
           case KeyEvent.VK_UP:
        	   MovePairTo(UP);
                break;
                
           case KeyEvent.VK_DOWN:
        	   MovePairTo(DOWN);
                break;     
                
                
        }                           
                                   

  }
        
    

    private void MovePairTo(int dir){
    
    
    	switch(dir)
    	{
    	case LEFT:    {        	//player wants to go left   
    		if(!CheckBoundaries(current_pair.left,LEFT))    
    			if(Move2FormerPipe(current_pair.left)) 
    				Move2FormerPipe(current_pair.right); 
    		break;
      		}	

                          
  
    	case RIGHT : {        	//player wants to go right

      
        	
    		if(!CheckBoundaries(current_pair.right,RIGHT)) 
    			if(Move2NextPipe(current_pair.right))  
    				Move2NextPipe(current_pair.left);         
    		break;
    	}

          case DOWN:{//player wants to go down 
        	  	if(!CheckBoundaries(current_pair.right,DOWN)){           		
        	  		current_pair.right.centerY+=2;
        		    current_pair.left.centerY+=2;        	  
        	  	}
        	  	break; 

          }
                  
                                  
    	}

    }


    //Check if we are hitting a wall
    private boolean CheckBoundaries(Block blk, int dir){
                
    	switch(dir)
    	{
    	case LEFT:  return  blk.pipe == 0;
    	case RIGHT: return  blk.pipe == (num_pipes-1);
    	case DOWN :{
                          
    		return  blk.centerY+block_radius > height;
    		
    	}
    	default:    return  false;
  }

    
    }


  
    //Move Block to the next (right) pipe
    private boolean  Move2NextPipe( Block blk){
        
    	if( ! pipes[blk.pipe +1].blocks.isEmpty())         
    		if(   blk.CollideDown( (Block) pipes[blk.pipe+1].blocks.getLast() ) )    
    			if(   blk.CollideRight( (Block) pipes[blk.pipe+1].blocks.getLast() ) )          
					return false;
    	
  
		blk.pipe++;
		blk.centerX+=2*block_radius;               
  
		return true;
      
	}
  
    
    //Move Block to the former (left) pipe         
    private boolean  Move2FormerPipe(Block blk){

                
    	if( ! pipes[blk.pipe - 1].blocks.isEmpty())       
    		if(   blk.CollideDown( (Block) pipes[blk.pipe-1].blocks.getLast() ) )    
    			if(   blk.CollideLeft( (Block) pipes[blk.pipe-1].blocks.getLast() ) )      
    				return false;
          
    	blk.pipe--;
    	blk.centerX-=2*block_radius;               
  
    	return true;
      
  }

    //See if there is a neighborhood of blocks of the same color
    private void  SeeChainReaction(Block blk)
    {


    	LinkedList queue= new LinkedList();
    	LinkedList list = new LinkedList();

    	queue.addLast(blk);
    	blk.in_CR_queue=true;

      
    	Iterator itr= queue.iterator();
 
    	while(itr.hasNext())
    	{
    		Object element=   itr.next();
    		Block  block  = (Block) element;         
    		LinkedList neighbors= GetNeighbors(block);
    		for(int i=0;i<neighbors.size();++i)
    		{
              
    			Block neighbor= (Block) neighbors.get(i);
                
    			if( ( !neighbor.in_CR_queue) && (neighbor.color==block.color)  )

    			{
    				queue.addLast(neighbor);
    				neighbor.in_CR_queue=true;
    			}          
     
    		}
   
    		list.addLast(queue.removeFirst());
    		itr= queue.iterator();
 
   
 
    	}	
 
 
    	if(list.size() >= n_chain_reaction)
    		
    		for(int i=0;i<list.size();++i)           
    		{ 
    			Object element=   list.get(i);
    			Block  block  = (Block) element; 
    			EraseBlock(block);
    			
    		}	
	
	
    	else
    		for(int i=0;i<list.size();++i)           
    		{ 
            	Object element=   list.get(i);
            	Block  block  = (Block) element; 
            	block.in_CR_queue=false;         
    		}
     

    }
	

    private LinkedList GetNeighbors(Block block){
  
    	LinkedList neighbors = new LinkedList();

    	int bpipe = block.pipe;
    	int bindex= block.index;
  
    	//Get Left Neighbor
    	if(block.pipe > 0) 
    		if(pipes[bpipe-1].blocks.size() > bindex)
    			neighbors.addLast(  pipes[bpipe-1].blocks.get(bindex) );
  
    	//Get Right Neighbor
    	if(block.pipe < num_pipes -1) 
    		if(pipes[bpipe+1].blocks.size() > bindex)
    			neighbors.addLast(  pipes[bpipe+1].blocks.get(bindex) );
      
   
    	//Get Upper
    	if(pipes[bpipe].blocks.size() > bindex + 1)
    		neighbors.addLast(  pipes[bpipe].blocks.get(bindex+1) );
  
  
    	if( bindex-1 > -1 )
    		neighbors.addLast(  pipes[bpipe].blocks.get(bindex-1) );
  
    	return neighbors;
  
 
}



    private void EraseBlock(Block block){
                                 

            for(int k=block.index + 1; k < pipes[block.pipe].blocks.size();++k)
            {
                
                   Object element         =  pipes[block.pipe].blocks.get(k);
                   Block  block_2  =  (Block) element; 
                   block_2.index--;
                   block_2.centerY+=  2*block_radius;
              
            }
                                                              
            pipes[block.pipe].blocks.remove(block.index);   
            score++;
       	 
                   


}





};