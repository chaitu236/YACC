/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 *
 * @author chaitanya
 */
public class Chess {

    /**
     * @param args the command line arguments
     */
    private HashSet<Piece> whitePieces;
    private HashSet<Piece> blackPieces;
    private Square[][] squares;
    private File moveFile;
    private PrintWriter fileWriter;
    
    private int turn;
    private int moveNo;
    private int gameState;
    private int moveCountFromLastPawnKill;
    
    private boolean whiteInCheck;
    private boolean blackInCheck;
    
    private boolean revertMove;
        
    public Chess(){
        init();
    }
    /*
     * Called before starting a new game
     */
    private void init(){
        nullifyAll();
        
        initSquares();
        initPieces();
        setMovableSquares();
        initFile();
        
        turn=DEF.WHITE;
        moveNo=1;
        gameState=DEF.PLAYING;
        moveCountFromLastPawnKill=0;
        
        whiteInCheck=false;
        blackInCheck=false;
        revertMove=false;
    }
    /*
     * Called before init to ensure that every variable is null and streams closed
     */
    private void nullifyAll(){
        whitePieces=null;
        blackPieces=null;
        squares=null;
        
        if(fileWriter!=null){
            fileWriter.close();
            fileWriter=null;
        }
        
        moveFile=null;
        
        turn=-1;
        moveNo=-1;
        gameState=-1;
        moveCountFromLastPawnKill=-1;
        
        whiteInCheck=false;
        blackInCheck=false; //not necessary.. but just for elegance
        revertMove=false;
    }
    
    private void initFile(){
        if(!DEF.WRITE_TO_FILE)
            return;
        
        moveFile=new File("moves.pgn");
        try{
        fileWriter=new PrintWriter(new FileOutputStream(moveFile));
        }catch(FileNotFoundException e){
            System.err.println("FILE NOT FOUND");
        }
    }
    
    private void initSquares(){
        squares=new Square[8][8];
        for(int i=0;i<8;i++)
            for(int j=0;j<8;j++){
                squares[i][j]=new Square(i, j);
            }
    }
    
    private void initPieces(){
        whitePieces=new HashSet<Piece>(16);
        blackPieces=new HashSet<Piece>(16);
        
        for(int i=0;i<2;i++){
            HashSet<Piece> pcs=(i==0)?whitePieces:blackPieces;
            int row=(i==0)?0:7;
            int color=(i==0)?DEF.WHITE:DEF.BLACK;
            
            pcs.add(new Piece(DEF.ROOK, color, squares[row][0]));
            pcs.add(new Piece(DEF.KNIGHT, color, squares[row][1]));
            pcs.add(new Piece(DEF.BISHOP, color, squares[row][2]));
            pcs.add(new Piece(DEF.QUEEN, color, squares[row][3]));
            pcs.add(new Piece(DEF.KING, color, squares[row][4]));
            pcs.add(new Piece(DEF.BISHOP, color, squares[row][5]));
            pcs.add(new Piece(DEF.KNIGHT, color, squares[row][6]));
            pcs.add(new Piece(DEF.ROOK, color, squares[row][7]));
            
            row=(i==0)?1:6;
            for(int j=0;j<8;j++){
                pcs.add(new Piece(DEF.PAWN, color, squares[row][j]));
            }
        }
    }
    
    private void run(){
        while(gameState==DEF.PLAYING){
            monkeyAI();
        }
        DEF.OUT("*************GAME OVER************");
        switch(gameState){
            case DEF.DRAW_50_MOVES: DEF.OUT("DRAW 50 MOVES"); break;
            case DEF.DRAW_REPETITION: DEF.OUT("DRAW REPETITION"); break;
            case DEF.STALEMATE: DEF.OUT("STALEMATE"); break;
            default: DEF.OUT("state "+gameState+" turn "+turn);
        }
        printDebug(DEF.WHITE);
        printDebug(DEF.BLACK);
        
        nullifyAll();
    }
    
    private Piece getKing(HashSet<Piece> pieces){
        Piece res=null;
        for(Piece p: pieces){
            if(p.type==DEF.KING){
                res=p;
                break;
            }
        }
        return res;
    }
    /*
     * Monkey would play better than this... :P
     */
    private void monkeyAI(){
        HashSet<Piece> tPieces=(HashSet<Piece>)((turn==DEF.WHITE)?whitePieces:blackPieces).clone();
        
        HashSet<Piece> movablePieces=new HashSet<Piece>();
        //HashSet<Square> movableSquares=new HashSet<Square>();
        if(turn==DEF.WHITE){
            if(whiteInCheck){
                Piece tp=getKing(tPieces);
                tPieces.clear();
                
                if(tp!=null)
                    tPieces.add(tp);
            }
        }
        else{
            if(blackInCheck){
                Piece tp=getKing(tPieces);
                tPieces.clear();
                
                if(tp!=null)
                    tPieces.add(tp);
            }
        }
        
        for(Piece p: tPieces){
            if(p.movableSquares.isEmpty())
                continue;
            movablePieces.add(p);
            //movableSquares.addAll(p.movableSquares);
        }
        
        if(movablePieces.isEmpty()){
            gameState=DEF.STALEMATE; // not exactly..
            return;
        }
        //Piece toMovePiece=movablePieces.get((int)(Math.random()*0.99*movablePieces.size()));
        Object[] ar=movablePieces.toArray();
        Piece toMovePiece=(Piece)ar[((int)(Math.random()*0.99*ar.length))];
        
        //Square toMoveSquare=toMovePiece.movableSquares.get((int)(Math.random()*0.99*toMovePiece.movableSquares.size()));
        ar=toMovePiece.movableSquares.toArray();
        Square toMoveSquare=(Square)ar[((int)(Math.random()*0.99*ar.length))];
        
        move(toMovePiece, toMoveSquare);
        
    }
    /*
     * This method does not check for the correctness of moves, just a few assertions.. 
     * it just makes the move and updates datastructures (class fields)
     */
    private void move(Piece piece, Square square){
        DEF.OUT(piece+""+square);
        
        Square origSquare=piece.square;//holds original square incase the new move results in a check in which case it is to be reverted
        
        assert(turn==piece.color);
        
        if(turn==DEF.WHITE){
            if(whiteInCheck)
                assert(piece.type==DEF.KING);
        }
        else{
            if(blackInCheck)
                assert(piece.type==DEF.KING);
        }
        
        assert(piece.movableSquares.contains(square));
        
        printMove(piece.square, square);
        
        HashSet<Piece> toUpdatePieces=new HashSet<Piece>();
        /*
         * Removing traces of this piece from all squares
         */
        for(Square tSquare: piece.attackedSquares){
            ((piece.color==DEF.WHITE)?tSquare.blackPiece:tSquare.whitePiece).remove(piece);
            piece.influencedSquares.remove(tSquare);
        }
        for(Square tSquare: piece.defendedSquares){
            ((piece.color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece).remove(piece);
            piece.influencedSquares.remove(tSquare);
        }
        for(Square tSquare: piece.influencedSquares){
            ((piece.color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece).remove(piece);            
        }
        /*
         * Pieces interested in the old square are updated..
         */
        toUpdatePieces.addAll(piece.square.blackPiece);
        //DEF.OUT("black pcs "+piece.square.blackPiece.size());
        toUpdatePieces.addAll(piece.square.whitePiece);
        //DEF.OUT("white pcs "+piece.square.whitePiece.size());
        
        piece.square.piece=null;
        piece.square.blackPiece.clear();
        piece.square.whitePiece.clear();
        
        if(square.piece!=null){
            
            DEF.OUT("KILL");
            
            int color=square.piece.color;
            /*
             * removing traces of this piece from all squares
             */
            for(Square tSquare: square.piece.attackedSquares){
                ((square.piece.color==DEF.WHITE)?tSquare.blackPiece:tSquare.whitePiece).remove(square.piece);
                square.piece.influencedSquares.remove(tSquare);
            }
            for(Square tSquare: square.piece.defendedSquares){
                ((square.piece.color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece).remove(square.piece);
                piece.influencedSquares.remove(tSquare);
            }
            for(Square tSquare: square.piece.influencedSquares){
                ((square.piece.color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece).remove(square.piece);            
            }
            
            /*
             * removing the killed piece from pieces list...
             */
            ((color==DEF.WHITE)?whitePieces:blackPieces).remove(square.piece);
            
            moveCountFromLastPawnKill=0;
        }
        /*
         * pieces interested in the new square are updated
         */
        toUpdatePieces.addAll(square.blackPiece);
        //DEF.OUT("new black sq "+square.blackPiece.size());
        toUpdatePieces.addAll(square.whitePiece);
        //DEF.OUT("new white sq "+square.whitePiece.size());
        
        piece.square=square; //assigning piece to the new square
        square.piece=piece;
        square.blackPiece.clear();
        square.whitePiece.clear();
        
        toUpdatePieces.add(piece); //the piece just moved should also be updated...
        
        //DEF.OUT(toUpdatePieces.size()+" pieces to update");
        
        for(Piece p: toUpdatePieces){
            /*
             * Emptying all the vectors of the piece to be updated. They will be repopulated by call to set****Squares(piece)
             */
            updatePiece(p);
        }
        
        if(moveCountFromLastPawnKill>=50){
            gameState=DEF.DRAW_50_MOVES;
        }
        
        
        
        checkForCheck(piece);
        
        if(piece.color==DEF.WHITE && whiteInCheck){
            revertMove=true;
            move(piece, origSquare);
            revertMove=false;
        }
        if(piece.color==DEF.BLACK && blackInCheck){
            revertMove=true;
            move(piece, origSquare);
            revertMove=false;
        }
        
        /*
         * If move really happens
         */
        if(origSquare!=piece.square && !revertMove){
            turn=(turn==DEF.WHITE)?DEF.BLACK:DEF.WHITE;
            moveCountFromLastPawnKill++;
            moveNo++;
            
            displayBoard();            
        }
    }
    /*
     * Repoulates the field values of Piece
     */
    private void updatePiece(Piece p){
        p.attackedSquares.clear();
        p.defendedSquares.clear();
        p.influencedSquares.clear();
        p.movableSquares.clear();

        switch(p.type){
            case DEF.KING: setKingSquares(p); break;
            case DEF.ROOK: setRookSquares(p); break;
            case DEF.BISHOP: setBishopSquares(p); break;
            case DEF.KNIGHT: setKnightSquares(p); break;
            case DEF.QUEEN: setQueenSquares(p); break;
            case DEF.PAWN: setPawnSquares(p); break;
        }
    }
    /*
     * Tests whether the current piece that was just move puts in check the other king.
     */
    private void checkForCheck(Piece piece){
        int color=piece.color;
        
        blackInCheck=false;
        whiteInCheck=false;
        
        for(Square sq: piece.attackedSquares){
            if(sq.piece!=null && sq.piece.type==DEF.KING){
                if(color==DEF.WHITE)
                    blackInCheck=true;
                else
                    whiteInCheck=true;
            }
        }
    }
    /*
     * All the moves are sent to this method and it prints it on screen and 
     * if a flag is set, writes the moves to a pgn file.
     */
    private void printMove(Square src, Square dest){
        String move="";
        
        if(moveNo%2==1)
            move+=(moveNo/2+1)+". ";

        move+=src+""+dest+" ";
        
        DEF.OUT(move);
        
        if(DEF.WRITE_TO_FILE)
            fileWriter.print(move);
    }
    
    private void attachPiece(Piece piece){
        
    }
    /*
     * invoked before moving to another square or before capturing...
     */
    private void detachPiece(Piece piece){
        
        
        //toUpdatePieces.add(piece);
        
        
    }
    
    private void setMovableSquares(){
        for(int i=0;i<2;i++){
            
            HashSet<Piece> tPieces=(i==0)?whitePieces:blackPieces;
            
            for(Piece p: tPieces){
                switch(p.type){
                    case DEF.KING: setKingSquares(p); break;
                    case DEF.ROOK: setRookSquares(p); break;
                    case DEF.BISHOP: setBishopSquares(p); break;
                    case DEF.KNIGHT: setKnightSquares(p); break;
                    case DEF.QUEEN: setQueenSquares(p); break;
                    case DEF.PAWN: setPawnSquares(p); break;
                }
            }
        }
    }
    /*
     * Should only be called from move()
     */
    private void setKingSquares(Piece piece){
        assert(piece.type==DEF.KING);
        
        int color=piece.color;
        int row=piece.square.row;
        int file=piece.square.file;
        
        for(int i=-1;i<2;i++)
            for(int j=-1;j<2;j++){
                
                int tr=row+i;
                int tf=file+j;
                
                if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                    continue;
                
                Square tSquare=squares[tr][tf]; //this square is visible to the king
                HashSet<Piece> oppPcs=(color==DEF.WHITE)?tSquare.blackPiece:tSquare.whitePiece;
                HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
                
                if(oppPcs.isEmpty() && (tSquare.piece==null || tSquare.piece.color!=color)){
                    piece.movableSquares.add(tSquare); //only movable squares are added
                    //DEF.OUT("movable "+tSquare);
                }
                
                ownPcs.add(piece); // all visible squares to the king are updated with the king's piece
                piece.influencedSquares.add(tSquare); //a record of influenced squares, used to update ownPcs when this piece is moved
                
                if(tSquare.piece!=null){
                    if(tSquare.piece.color==piece.color){
                        piece.defendedSquares.add(tSquare);
                        //DEF.OUT("king defended "+tSquare);
                    }
                    else{
                        piece.attackedSquares.add(tSquare);
                        //DEF.OUT("king attacked "+tSquare);
                    }
                }
                //DEF.OUT("visible "+tSquare);
            }
    }
    /*
     * Should only be called from move()
     */
    private void setRookSquares(Piece piece){
        //assert(piece.type==DEF.ROOK); don't assert because we use the same fn for queen squares
        
        int color=piece.color;
        int row=piece.square.row;
        int file=piece.square.file;
        
        //for squares on same file and above
        for(int i=1;i<8-row;i++){
            int tr=row+i;
            int tf=file;
            
            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;
            Square tSquare=squares[tr][tf]; //this square is visible to the rook            
                        
            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);
            
            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
        //for squares on same file and below
        for(int i=1;i<=row;i++){
            int tr=row-i;
            int tf=file;
            
            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;
            Square tSquare=squares[tr][tf]; //this square is visible to the rook            
                        
            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);
            
            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
        //for squares on same row and to left
        for(int i=1;i<=file;i++){
            int tr=row;
            int tf=file-i;
            
            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;
            Square tSquare=squares[tr][tf]; //this square is visible to the rook            
                        
            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);
            
            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
        //for squares on same file and to right
        for(int i=1;i<8-file;i++){
            int tr=row;
            int tf=file+i;
            
            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;
            Square tSquare=squares[tr][tf]; //this square is visible to the rook            
                        
            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);
            
            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
    }
    /*
     * Should only be called from move()
     */
    public void setBishopSquares(Piece piece){
        //assert(piece.type==DEF.BISHOP); don't assert because we use the same fn for queen squares
        
        int color=piece.color;
        int row=piece.square.row;
        int file=piece.square.file;

        //check in north west
        for(int i=1;i<8-row;i++){
            
            int tr=row+i;
            int tf=file+i;

            if(tr<0 || tr>7 || tf<0 || tf>7)
                continue;

            Square tSquare=squares[tr][tf]; //this square is visible          

            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);

            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                }
                break;
            }
        }
        //check in north east
        for(int i=1;i<8-row;i++){
            
            int tr=row+i;
            int tf=file-i;

            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;

            Square tSquare=squares[tr][tf]; //this square is visible to the rook            

            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);

            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
        //check in south west
        for(int i=1;i<=row;i++){
            
            int tr=row-i;
            int tf=file+i;

            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;

            Square tSquare=squares[tr][tf]; //this square is visible to the rook            

            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);

            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
        //check in south west
        for(int i=1;i<=row;i++){
            
            int tr=row-i;
            int tf=file-i;

            if(tr<0 || tr>7 || tf<0 || tf>7 || (tr==row && tf==file))
                continue;

            Square tSquare=squares[tr][tf]; //this square is visible to the rook            

            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            piece.movableSquares.add(tSquare);

            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                    //DEF.OUT("rook defended "+tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    //DEF.OUT("rook attacked "+tSquare);
                }
                break;
            }
        }
    }
    /*
     * Should only be called from move()
     */
    public void setQueenSquares(Piece piece){
        setBishopSquares(piece);
        setRookSquares(piece);
    }
    /*
     * Should only be called from move()
     */
    public void setPawnSquares(Piece piece){
        assert(piece.type==DEF.PAWN);
        
        int color=piece.color;
        int row=piece.square.row;
        int file=piece.square.file;
        
        if(row+1<8 && row-1>=0 && squares[row+((color==DEF.WHITE)?1:-1)][file].piece==null){
            piece.movableSquares.add(squares[row+((color==DEF.WHITE)?1:-1)][file]);
            HashSet<Piece> ownPcs=(color==DEF.BLACK)?squares[row-1][file].blackPiece:squares[row+1][file].whitePiece;
            ownPcs.add(piece);
        }
        
        for(int i=-1;i<2;i=i+2){
            int tr=row+((color==DEF.WHITE)?1:-1);
            int tf=file+i;

            if(tr<0 || tr>7 || tf<0 || tf>7)
                continue;

            Square tSquare=squares[tr][tf]; //this square is visible to the rook            

            HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
            ownPcs.add(piece);
            piece.influencedSquares.add(tSquare);
            //piece.movableSquares.add(tSquare); these are not movable squares all the time....

            if(tSquare.piece!=null){
                if(tSquare.piece.color==piece.color){
                    piece.defendedSquares.add(tSquare);
                    piece.movableSquares.remove(tSquare);
                }
                else{
                    piece.attackedSquares.add(tSquare);
                    piece.movableSquares.add(tSquare);
                }
                break;
            }            
        }
        
        if(color==DEF.WHITE && row==1){
            Square tSquare=squares[row+2][file];
            if(tSquare.piece==null && squares[row+1][file].piece==null){
                piece.movableSquares.add(tSquare);
                HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
                ownPcs.add(piece);
            }
        }
        if(color==DEF.BLACK && row==6){
            Square tSquare=squares[row-2][file];
            if(tSquare.piece==null && squares[row-1][file].piece==null){
                piece.movableSquares.add(tSquare);
                HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
                ownPcs.add(piece);
            }
        }
        /*
         * Handle promotions
         */
        if(color==DEF.WHITE && row==7){
            piece.type=DEF.PROMOTION_PIECE;
            updatePiece(piece);
        }
        if(color==DEF.BLACK && row==0){
            piece.type=DEF.PROMOTION_PIECE;
            updatePiece(piece);
        }
        
    }
    /*
     * Should only be called from move()
     */
    public void setKnightSquares(Piece piece){
        assert(piece.type==DEF.BISHOP);
        
        int color=piece.color;
        int row=piece.square.row;
        int file=piece.square.file;
        
        for(int i=-2;i<3;i++){
            
            if(i==0)
                continue;
            
            for(int j=-2;j<3;j++){
                
                if(j==0)
                    continue;
                
                if(i*i==j*j)
                    continue;
                
                int tr=row+i;
                int tf=file+j;

                if(tr<0 || tr>7 || tf<0 || tf>7)
                    continue;

                Square tSquare=squares[tr][tf]; //this square is visible           

                HashSet<Piece> ownPcs=(color==DEF.BLACK)?tSquare.blackPiece:tSquare.whitePiece;
                ownPcs.add(piece);
                
                piece.influencedSquares.add(tSquare);
                piece.movableSquares.add(tSquare);

                if(tSquare.piece!=null){
                    if(tSquare.piece.color==piece.color){
                        piece.defendedSquares.add(tSquare);
                        piece.movableSquares.remove(tSquare);
                    }
                    else{
                        piece.attackedSquares.add(tSquare);
                    }
                }
            }
        }
    }
        
    public static void main(String[] args) {
        Chess chess=new Chess();
        //chess.displayBoard();
        chess.run();
        //chess.test();
    }
    
    public void test(){
//        for(int i=0;i<8;i++)
//            for(int j=0;j<8;j++){
//                squares[i][j].printDebug();
//            }
        //printPcs(DEF.WHITE);
        Piece p=squares[1][7].piece;
        squares[1][7].printDebug();
        //p.printDebug();
        
        move(p, squares[5][7]);
//        printPcs(DEF.WHITE);
//        DEF.OUT("*********************");
//        printPcs(DEF.BLACK);
//        DEF.OUT("*********************");
        displayBoard();
        p.printDebug();
        squares[6][6].piece.printDebug();
        squares[6][6].printDebug();
    }
    
    public void printDebug(int color){
        DEF.OUT("color= "+color+" blackPieces.size() "+blackPieces.size());
        for(Piece p:(color==DEF.WHITE)?whitePieces:blackPieces){
            p.printDebug();
        }
    }
    
    public void displayBoard(){
        for(int i=7;i>=0;i--){
            for(int j=0;j<8;j++){
                if(squares[i][j].piece==null)
                    System.out.print(". ");
                else
                    System.out.print(squares[i][j].piece+" ");
            }
            System.out.println("");
        }
        System.out.println("");
    }
}
