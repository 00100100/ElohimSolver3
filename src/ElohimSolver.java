import java.util.ArrayList;
import java.util.Scanner;

import Pieces.*;

/**
 * ElohimSolver
 * 
 * solves a game called "Sigil of Elohim" and its tetris box puzzles.
 * the game was made by DevolverDigital. Available for PC/Mobile for free.
 * 
 * This code is easily modifiable so that you can:
 * 1) Have a box with "holes"
 * 2) Have diverse Pieces.
 * 
 * This code was created for part A of the game.
 * 
 * Please note, "bruteforcing backtracking" may take a while to produce the solution.
 * Turning off System.printStuff makes things faster.
 * 
 * Pruning ideas?
 * - currentBox space left counter (something like this?)??
 * - remove combinations for end parts. 123 = 132 = 213 = 231 = 312 = 321???
 * 
 * email - LK00100100@gmail.com
 * 
 * @author LK00100100
 *
 */
public class ElohimSolver {

	private static long startTime;				//stop-watch for the program
	private static int smallestPieceSize;	//the smallest piece size (in terms of blocks). a "PieceT" has 4 blocks.

	public static void main(String[] args) {

		//read some standard input (aka the console).
		Scanner sc = new Scanner(System.in);

		ArrayList<Piece> pieceArray; 			//stores the pieces. (hopefully, you input them in piece order)
		PieceArrayBuilder pf = new PieceArrayBuilder();

		//for the box
		int rows;
		int cols;
		int[][] box;

		//get user input on piece quantity.
		//There is no error checking, cause let's face it... that's work.
		System.out.println("Programmer Note: There isn't any input error checking cause I am lazy");
		System.out.println("How many L Pieces: ");
		pf.addPieceL(sc.nextInt());
		System.out.println("How many L Reversed Pieces: ");
		pf.addPieceLReversed(sc.nextInt());
		System.out.println("How many Square Pieces: ");
		pf.addPieceSquare(sc.nextInt());
		System.out.println("How many Straight Pieces: ");
		pf.addPieceStraight(sc.nextInt());
		System.out.println("How many T Pieces: ");
		pf.addPieceT(sc.nextInt());
		System.out.println("How many Thunder Pieces: ");
		pf.addPieceThunder(sc.nextInt());
		System.out.println("How many Thunder Reversed Pieces: ");
		pf.addPieceThunderReversed(sc.nextInt());
		pieceArray = pf.getArrayPieces();
		pf.printArray();

		smallestPieceSize = pf.getSmallestPiece();

		//get box size and make one.
		System.out.println("How many rows in the box: ");
		rows = sc.nextInt();
		System.out.println("How many columns in the box: ");
		cols = sc.nextInt();
		box = new int[rows][cols];

		//clears the box matrix (note, you can add holes after these loops)
		for(int i = 0; i < rows; i++)
			for(int j = 0; j < cols; j++)
				box[i][j] = 0;

		startTime = System.currentTimeMillis();

		findSolution(box, pieceArray);

		//print time duration of algorithm
		printStopwatchTime(startTime);

		System.out.println("No solution was found");

		sc.close();

	}

	private static void findSolution(int[][] currentBox, ArrayList<Piece> piecesRemaining){

		if(isSolution(piecesRemaining)){
			solutionFound(currentBox);
			System.exit(0);	//end program immediately.
		}
		else{//no solution, process

			//go through all the remaining pieces.
			for(int i = 0; i < piecesRemaining.size(); i++){

				//System.out.println("pieces remaining: " + piecesRemaining.toString());
				Piece currentPiece = piecesRemaining.get(i);

				//check if the previous piece is the same as current.
				//if so, just go to the next piece since we don't want to recalculate the same things.
				//note: assumed piecesRemaining is sorted.
				if(i > 0){
					if(currentPiece.equals(piecesRemaining.get(i - 1))){
						continue;
					}
				}

				boolean fittedOnce = false;	//if at least one piece's rotation cannot be placed anywhere, back up one step.

				//go through all rotations of this piece
				for(int j = 0; j < currentPiece.rotations; j++){

					//go through all rows of currentBox (except the last ones cause that's unnecessary)
					for(int row = 0; row < currentBox.length - currentPiece.row + 1; row++){

						//go through all columns of currentBox (except the last ones cause that's unnecessary)
						for(int col = 0; col < currentBox[0].length - currentPiece.col + 1; col++){

							//if we can fit the piece.
							if(fitPiece(currentBox, currentPiece, row, col)){
								Piece temp = piecesRemaining.remove(i);

								//if placing the piece caused a small bad "bubbles" of space, then don't do the recursion.
								if(bubbleCheckInit(temp, currentBox, row, col, smallestPieceSize) == false){
									fittedOnce = true;

									//update variables, recursively go deeper into the rabbit hole...
									findSolution(currentBox, piecesRemaining);
								} 

								//go back one step. remove the piece from the box,
								//and add it back to the pool of pieces
								removePiece(currentBox, currentPiece, row, col);
								piecesRemaining.add(i, temp);

							}
							//else do nothing

						}

					}

					//rotate piece
					currentPiece.rotate();

				}//end - for rotations

				//if the current piece cannot fit at all (despite rotations) and without creating bad "bubbles" of small space.
				//then don't bother going through the remaining pieces.
				if(fittedOnce == false){
					break;	//break out of - for(go through all the remaining pieces.);
				}

			}

		}//end else(not a solution)

	}


	//TODO this method can become more complicated and do more for pruning?
	/**
	 * bubbleCheckInit
	 * 
	 * Ex: placing a T in a 2x3 box creates two "bubbles" of size 1.
	 * If the smallest remaining piece is of size > 1, then return true, there is a bad bubble.
	 * 
	 * 0+0	0 = bubble
	 * +++	+ = piece
	 * 
	 * @param lastPiece - the Piece just placed
	 * @param currentBox - the currentBox state
	 * @param rowPlaced - row of Piece placed
	 * @param colPlaced - col of Piece placed
	 * @param minSize - the minimum Size of the bubble has to be.
	 * @return boolean - true if a bubble smaller than minSize exists
	 */
	private static boolean bubbleCheckInit(Piece lastPiece, int[][] currentBox, int rowPlaced, int colPlaced, int minSize){

		//keeps track of the "bubble numbers" (the size the space can fit. limited to minSize)
		int[][] bubbleBox = new int[currentBox.length][currentBox[0].length];

		//initialize the bubbleBox
		//the lastPiece is centered.
		//-1 = occupied
		//0 = unchecked
		//>0 = checked, with biggest bubble size found (limited by minSize).
		for(int row = 0; row < bubbleBox.length; row++){
			for(int col = 0; col < bubbleBox[0].length; col++){
				//if the spot is occupied.
				if(currentBox[row][col] != 0 ){
					//fill in bubbleBox as occupied.
					bubbleBox[row][col] = -1 ;
				}
				else{
					//fill in bubbleBox as unoccupied (never done recursion).
					bubbleBox[row][col] = 0;
				}

			}
		}

		//top left corner of the search space (used in for loop below)
		int row = rowPlaced - 1;
		int col = colPlaced - 1;

		int retVal;

		//go through every space ONLY around the piece.
		for(int i = 0; i < lastPiece.row + 2; i++){
			for(int j = 0; j < lastPiece.col + 2; j++){

				//valid range check
				if(row + i < bubbleBox.length && col + j < bubbleBox[0].length){
					if(row + i >= 0 && col + j >= 0){
						//if this space is unoccupied and hasn't been checked.
						if(bubbleBox[row + i][col + j] == 0){

							if( (retVal = bubbleCheck(bubbleBox, row + i, col + j, 0, minSize)) < minSize){
								
								//System.out.println("BUBBLE DETECTED");
								//System.out.println("retVal: " + retVal);
								//printIntMatrix(bubbleBox);
								//System.out.println();
								return true;

							}
							//System.out.println("retVal: " + retVal);
							bubbleBox[row + i][col + j] = retVal;

							//set every positive value to the retVal
							for(int x = 0; x < bubbleBox.length; x++){
								for(int y = 0; y < bubbleBox[0].length; y++){
									//if the spot is occupied.
									if(bubbleBox[x][y] > 0 ){
										bubbleBox[x][y] = retVal;
									}

								}
							}
							
							//printIntMatrix(bubbleBox);
							//System.out.println();

						}
					}
				}

			}
		}	

		return false;
	}

	/**
	 * bubbleCheck
	 * 
	 * this finds bubbles and alters the bubbleBox
	 * 
	 * @param bubbleBox - keeps track of the size of the bubbles.
	 * @param row - row to check for bubbleBox
	 * @param col - col to check for bubbleBox
	 * @param currentSize - current bubble size
	 * @param minSize - the bubble has to be at least this size. this is used to stop recursion.
	 * @return
	 */
	private static int bubbleCheck(int[][] bubbleBox, int row, int col, int currentSize, int minSize){

		//check the bubbleBox for a value, so we don't have to use too much recursion.
		int retVal;

		//check valid range for bubbleBox
		if(row < 0 || col < 0) 
			return currentSize;
		if(row >= bubbleBox.length || col >= bubbleBox[0].length) 
			return currentSize;

		//if this bubbleBox space is occupied by a piece or void, just go back.
		if(bubbleBox[row][col] == -1)
			return currentSize;
		//if this space has a bubble size number, return the bigger number;
		else if(bubbleBox[row][col] > 0){
			if(bubbleBox[row][col] > currentSize)
				return bubbleBox[row][col];
			else
				return currentSize;
		}

		//else no bubble number, fill it in.
		bubbleBox[row][col] = ++currentSize;

		//if we've reached the minSize limit, just return. no need to keep going deeper.
		if(currentSize == minSize)
			return currentSize;

		//check left (first)
		//if the square we checked has a larger bubble number, then the current square
		//has the larger bubble number.
		retVal = bubbleCheck(bubbleBox, row, col - 1, currentSize, minSize);
		if(retVal > currentSize){
			currentSize = retVal;
			bubbleBox[row][col] = currentSize;
			
			if(currentSize == minSize)
				return currentSize;
		}

		//check up (second)
		retVal = bubbleCheck(bubbleBox, row - 1, col, currentSize, minSize);
		if(retVal > currentSize){
			currentSize = retVal;
			bubbleBox[row][col] = currentSize;
			
			if(currentSize == minSize)
				return currentSize;
		}

		//check right
		retVal = bubbleCheck(bubbleBox, row, col + 1, currentSize, minSize);
		if(retVal > currentSize){
			currentSize = retVal;
			bubbleBox[row][col] = currentSize;
			
			if(currentSize == minSize)
				return currentSize;
		}

		//check down
		retVal = bubbleCheck(bubbleBox, row + 1, col, currentSize, minSize);
		if(retVal > currentSize){
			currentSize = retVal;
			bubbleBox[row][col] = currentSize;
			
			if(currentSize == minSize)
				return currentSize;
		}

		return currentSize;
	}

	/**
	 * fitPiece
	 * 
	 * attempts to jam in a piece at the set coordinates. returns true if fits, false if not.
	 * 
	 * @param currentBox - the box that will be modified with the fit (if it fits) piece.
	 * @param thePiece - the piece to attempt to fit
	 * @param row of the box.
	 * @param column of the box.
	 * @return boolean - success or failure of piece fitting
	 */
	private static boolean fitPiece(int[][] currentBox, Piece thePiece, int row, int col){

		try{
			//attempt to see if we can fit thePiece in the currentBox.
			for(int i = 0; i < thePiece.row; i++){
				for(int j = 0; j < thePiece.col; j++){

					//if piece section exists, remove it from the currentBox
					if(thePiece.space[i][j] != 0){
						//currentBox has that spot occupied
						if(currentBox[row + i][col + j] != 0)
							return false;
					}

				}
			}
		}
		catch(ArrayIndexOutOfBoundsException ex){//the fun lazy way.
			return false;
		}

		//put the piece in.
		for(int i = 0; i < thePiece.row; i++){
			for(int j = 0; j < thePiece.col; j++){

				//if piece section exists, remove it from the currentBox
				if(thePiece.space[i][j] != 0){
					//currentBox has that spot occupied
					currentBox[row + i][col + j] = thePiece.id;
				}

			}
		}

		//System.out.println("piece Id added was: " + thePiece.id);
		return true;

	}

	/**
	 * removePiece
	 * 
	 * removes the piece
	 * it is implied that the piece exists when you call this method
	 * 
	 * @param currentBox
	 * @param thePiece
	 * @param row - currentBox's row
	 * @param col - currentBox's column
	 */
	private static void removePiece(int[][] currentBox, Piece thePiece, int row, int col) {

		//go through every square in the piece and only remove a piece section if
		//the piece section exists
		for(int i = 0; i < thePiece.row; i++){
			for(int j = 0; j < thePiece.col; j++){

				//if piece section exists, remove it from the currentBox
				if(thePiece.space[i][j] != 0){
					currentBox[row + i][col + j] = 0;
				}

			}
		}

		//System.out.println("piece Id removed was: " + thePiece.id);
	}

	/**
	 * isSolution
	 * 
	 * checks to see if this is the solution
	 * 
	 * @param piecesRemaining
	 * @return boolean - true on solution; false if not.
	 */
	private static boolean isSolution(ArrayList<Piece> piecesRemaining){

		if(piecesRemaining.size() == 0)
			return true;

		return false;

	}

	/**
	 * solutionFound
	 * 
	 * a solution was found. Print it out
	 * 
	 * @param currentBox
	 */
	private static void solutionFound(int[][] currentBox){
		printIntMatrix(currentBox);

		printStopwatchTime(startTime);

	}

	private static void printStopwatchTime(long timeStart){
		long endTime = System.currentTimeMillis() - timeStart;
		long second = (endTime / 1000) % 60;
		long minute = (endTime / (1000 * 60)) % 60;
		long hour = (endTime / (1000 * 60 * 60)) % 24;
		System.out.printf("Duration (HH/MM/SS): %2d:%2d:%2d ", hour, minute, second);
	}

	private static void printIntMatrix(int[][] theMatrix){
		for(int i = 0; i < theMatrix.length; i++){
			for(int j = 0; j < theMatrix[0].length; j++){
				System.out.printf("%2d ", theMatrix[i][j]);
			}
			System.out.println();
		}
	}
}
