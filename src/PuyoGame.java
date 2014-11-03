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
    //Directions to move
    int RIGHT=1,LEFT=2,DOWN=3, UP=4;
    //Orientations for blocks
    int RIGHT_OR=0, TOP_OR=1,LEFT_OR=2, BOTTOM_OR=3;
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
    	//prepare the blockA
    	int blkradius =  block_radius;
    	
    	//pipe to init the blockA
    	int left_pipe  =2;    	
    	int color_index=r.nextInt(7); 
    	
    	Block blockA  = 
    			new Block(blkradius+   left_pipe*2*blkradius, height/4 ,blkradius, colors[color_index]); 
    	blockA.pipe= left_pipe;
     
    	//prepare the right block
    	color_index=r.nextInt(7); 
    	int right_pipe =left_pipe + 1;//the blocks are glued

    	Block blockB = 
    			new Block(blkradius +  right_pipe*2*blkradius, height/4+ height/6*0 ,blkradius, colors[color_index]);
        blockB.pipe= right_pipe;
          
                     
        //store them in the pair
        current_pair.blockA =blockA;
        current_pair.blockB=blockB;
        //by default A and B are glued and B is the right
        current_pair.blockB_OR=RIGHT_OR;
                 
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
    	//if  hits the ground
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
      boolean ba__ = MoveBlock(current_pair.blockA) ;
      boolean bb__ = MoveBlock(current_pair.blockB); 

      //blockA does not move and right block is on top      
      if((!ba__) && (current_pair.blockB_OR== TOP_OR))
      {
        
              SeeChainReaction(current_pair.blockA);      
              
              if(current_pair.blockA.color!=current_pair.blockB.color)             
            	  SeeChainReaction(current_pair.blockB);
              
              ReleasePair();

      }

      //blockB does not move and is at the bottom       
      else   if((!bb__) && (current_pair.blockB_OR== BOTTOM_OR))
      {
        

              pipes[current_pair.blockB.pipe].blocks.addLast(current_pair.blockA); //left block is still free !!
              current_pair.blockA.index =current_pair.blockB.index+1;
                    
              SeeChainReaction(current_pair.blockB);      
        
              if(current_pair.blockA.color!=current_pair.blockB.color)             
            	  SeeChainReaction(current_pair.blockA);                        
        
              ReleasePair();

      }
     
      // blockA moves, blockB does not and both are in horizontal position        
      else if( ba__&& (!bb__) ){
    	      floatingBlocks.addLast(current_pair.blockA);
              
    	      //Checks  chain reaction!!
    	      SeeChainReaction(current_pair.blockB);
    	      ReleasePair();
          
      }
 
      // blockB moves, blockA does not and both are in horizontal position     
      else if(bb__ && (!ba__) ){
          	floatingBlocks.addLast(current_pair.blockB);
          
          	//Checks  chain reaction!!
          	SeeChainReaction(current_pair.blockA);
           	ReleasePair();
         
      }

      //  both are not moving and are in horizontal position     
      else if(! (ba__ && bb__))
      {
      
           if(current_pair.blockA.color!=current_pair.blockB.color)             
        	   SeeChainReaction(current_pair.blockA);
            
           	SeeChainReaction(current_pair.blockB);
           	ReleasePair();
           	
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
                
           case KeyEvent.VK_CONTROL:
        	   RotatePair();
                break;     
                
        }                           
                                   

  }
        
    

    private void MovePairTo(int dir){
        
    	switch(dir)
    	{
    	
    		case DOWN:{//player wants to go down
    			
    			switch(current_pair.blockB_OR){

    			// blockB is at the top
     				case TOP_OR:
     					//Check if blockA which is at bottom hits something
     					if(!CheckBoundaries(current_pair.blockA,DOWN))
     						{
     						current_pair.blockA.centerY+=2;
     						current_pair.blockB.centerY+=2;
     						}
     					break; 
     				
     				default:	
     				//block B is at the bottom or by the side of blockA
     					if(!CheckBoundaries(current_pair.blockB,DOWN)){           		
     						current_pair.blockA.centerY+=2;
     						current_pair.blockB.centerY+=2;
     					}
    		    	break; 
    			}
    		 break;
    		}
        	//player wants to go left
        	case LEFT :{
            
        		switch(current_pair.blockB_OR)
        		{
                   // blockB is at the right side
        			case RIGHT_OR:
        			{
        				if(!CheckBoundaries(current_pair.blockA,LEFT))    
        					if(Move2FormerPipe(current_pair.blockA)) 
        						Move2FormerPipe(current_pair.blockB); 
        				break;
        			}
                
        			//blockB  is at the top
        			case TOP_OR:
        			{
        				if(!CheckBoundaries(current_pair.blockA,LEFT))    
        					if(Move2FormerPipe(current_pair.blockA)) 
        						Move2FormerPipe(current_pair.blockB); 
        				break;
        			}
                 
        			//blockB  is at the left side        			
        			case LEFT_OR:
        				{
        					if(!CheckBoundaries(current_pair.blockB,LEFT))    
        						if(Move2FormerPipe(current_pair.blockB)) 
        							Move2FormerPipe(current_pair.blockA); 
        					break;
        				}
                 
                 
        			//blockB is at the bottom        			                
                    case BOTTOM_OR:
                    {
                    	if(!CheckBoundaries(current_pair.blockA,LEFT))    
                    		if(Move2FormerPipe(current_pair.blockB)) 
                    			Move2FormerPipe(current_pair.blockA); 
                    	break;
                    }
         
        		}
                break;   //case left             		
        	}	
        
                    
        	//player wants to go right
        	case RIGHT : {

        		switch(current_pair.blockB_OR)
        		{
        		   //blockB is at the right side    			
                	case RIGHT_OR:
                	{    

                		if(!CheckBoundaries(current_pair.blockB,RIGHT)) 
                			if(Move2NextPipe(current_pair.blockB))  
                				Move2NextPipe(current_pair.blockA); 
                    
                		break;
                	}
                
                
                	//blockB is at the top    			                
                	case TOP_OR:
                	{    

                		if(!CheckBoundaries(current_pair.blockB,RIGHT)) 
                			if(Move2NextPipe(current_pair.blockA))  
                				Move2NextPipe(current_pair.blockB); 
                    
                		break;
                	}
                   
                   
                	//blockB is at the left side    			                                	
                	case LEFT_OR:
                	{    

                		if(!CheckBoundaries(current_pair.blockA,RIGHT)) 
                			if(Move2NextPipe(current_pair.blockA))  
                				Move2NextPipe(current_pair.blockB); 
                    
                		break;
                	}
                
                	//blockB is at the bottom    			                                	                	
                	case BOTTOM_OR:
                	{    

                		if(!CheckBoundaries(current_pair.blockB,RIGHT)) 
                			if(Move2NextPipe(current_pair.blockB))  
                				Move2NextPipe(current_pair.blockA); 
                    
                		break;
                	}	                                                                     
            
        		}	

        		break;//switch right
        	}

    	}//switch dir
    	
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


    //Check Block to the next (right) pipe
    private boolean  Move2NextPipe( Block blk){
        
    	if( ! pipes[blk.pipe +1].blocks.isEmpty())         
    		if(   blk.CollideDown( (Block) pipes[blk.pipe+1].blocks.getLast() ) )    
    			if(   blk.CollideRight( (Block) pipes[blk.pipe+1].blocks.getLast() ) )          
					return false;
    	
  
		blk.pipe++;
		blk.centerX+=2*block_radius;               
  
		return true;
      
	}
  
    
         
    private boolean  Move2FormerPipe(Block blk){

                
    	if( ! pipes[blk.pipe - 1].blocks.isEmpty())       
    		if(   blk.CollideDown( (Block) pipes[blk.pipe-1].blocks.getLast() ) )    
    			if(   blk.CollideLeft( (Block) pipes[blk.pipe-1].blocks.getLast() ) )      
    				return false;
          
    	blk.pipe--;
    	blk.centerX-=2*block_radius;               
  
    	return true;
      
  }


    
    //User can rotate the pair of blocks
    public void RotatePair(){
        
      
    //we can only rotate blockB and only counterclockwise    
     int orientation=  current_pair.blockB_OR;
      
    
          switch(orientation)
          {
              
              case RIGHT_OR:          // right to top
                     {
                         current_pair.blockB_OR=TOP_OR;
                         current_pair.blockB.centerX=current_pair.blockA.centerX;
                         current_pair.blockB.centerY=current_pair.blockA.centerY - 2*block_radius;
                         current_pair.blockB.pipe=current_pair.blockA.pipe;
                        
                         break;
                         
                       }
              
    
              case  TOP_OR:          //  top to left
              {
            	  if(current_pair.blockB.pipe > 0)//we don't hit the left wall
                  	{//by default we can rotate
            		  boolean rotate__=true;
            		    //check if the precedent pipe is not empty
            		  	if( ! pipes[current_pair.blockA.pipe-1].blocks.isEmpty())    
            		  	{   //check if the left block is still moving 
            		  		if(current_pair.blockA.CollideDown( (Block) pipes[current_pair.blockA.pipe-1].blocks.getLast()))
            		  		{        //check if the right block does not hit the pipe we want to rotate using left block
            		  				if(current_pair.blockA.CollideLeft( (Block) pipes[current_pair.blockA.pipe-1].blocks.getLast()))
            		  				{
            		  					rotate__ = false;//we cannot rotate
            		  				}
            		  		}
            		  	}
            		  	
            		  	if(rotate__){
            		  		current_pair.blockB.centerY=current_pair.blockA.centerY;
            		  		current_pair.blockB.centerX=current_pair.blockA.centerX -  2*block_radius;
            		  		current_pair.blockB_OR=LEFT_OR;
            		  		current_pair.blockB.pipe--;
            		  	}
                
                  }
                 
                      
                  break;
               }
      
                            
              case  LEFT_OR:          //  left to   bottom 
              {
            	  
                     //do we hit the ground?
                     if(CheckBoundaries(current_pair.blockB,DOWN))
                      break;
                                    
                     //block for tests
                      Block block__=   new Block(0,0,0, Color.RED);
                      block__.radius=  block_radius;
                      block__.centerY= current_pair.blockA.centerY +  2*block_radius;
             
                      //if we rotate would be hitting the ground
                      if(CheckBoundaries(block__,DOWN))  
                    	  break;             
                                        
                      // test whether is anything at the bottom of the right block
                      if(!pipes[current_pair.blockB.pipe].blocks.isEmpty())
                    	  if( block__.CollideDown( (Block) pipes[current_pair.blockB.pipe].blocks.getLast() ))
                         break;
                      
                                                    
                      
                      block__.centerX= current_pair.blockA.centerX;
                                            
                      	// test whether is anything at the bottom of the left block                     
                      	if(!pipes[current_pair.blockA.pipe].blocks.isEmpty())
                      		if( block__.CollideDown( (Block) pipes[current_pair.blockA.pipe].blocks.getLast() ))
                      			break;
                    
                      
                      //if we rotate would be hitting the ground
                      if(CheckBoundaries(current_pair.blockB,DOWN))  
                    	  break;                                           
              
                      	current_pair.blockB_OR=BOTTOM_OR;
                      	current_pair.blockB.centerY=current_pair.blockA.centerY +  2*block_radius;
                      	current_pair.blockB.centerX=current_pair.blockA.centerX;
                      	current_pair.blockB.pipe=current_pair.blockA.pipe;                                                   
                  
                  break;
               } 
     
     
     
     
     
            case  BOTTOM_OR:          //  bottom to right
              {
                  if(current_pair.blockB.pipe < num_pipes - 1)
                  {
                                           
                      boolean rotate__=true;
                      
                      if( ! pipes[current_pair.blockB.pipe+1].blocks.isEmpty())         
                    	  if(  current_pair.blockB.CollideDown( (Block) pipes[current_pair.blockB.pipe +1].blocks.getLast()))    
                    		  if(  current_pair.blockB.CollideRight( (Block) pipes[current_pair.blockB.pipe+1].blocks.getLast() ))
                    			  rotate__ = false;   
                      
                      if(rotate__) 
                      {
                    	  current_pair.blockB_OR=RIGHT_OR;
                    	  current_pair.blockB.centerY=current_pair.blockA.centerY ;
                    	  current_pair.blockB.centerX=current_pair.blockA.centerX +  2*block_radius;
        
                    	  current_pair.blockB.pipe++;
                      }
                  	}
                 
                  	break;
               }	
     
          
          }
      
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