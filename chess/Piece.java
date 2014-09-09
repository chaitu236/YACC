/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

import java.util.HashSet;

/**
 *
 * @author chaitanya
 */
public class Piece {
    public int type;
    public int color;
    
    public HashSet<Square> influencedSquares; //squares which are attacked or defended or can be moved to by this piece
    public HashSet<Square> movableSquares; // squares to which piece can be moved..
    public HashSet<Square> attackedSquares; //influenced squares which contain pieces of same color
    public HashSet<Square> defendedSquares; //influenced squares which contain pieces of other color
    
    public Square square;
    
    public Piece(int type, int color, Square square){
        assert(type>=DEF.KING && type<=DEF.PAWN);
        assert(color==DEF.WHITE || color==DEF.BLACK);
        
        this.type=type;
        this.color=color;
        
        setSquare(square);
        
        influencedSquares=new HashSet<Square>();
        movableSquares=new HashSet<Square>();
        attackedSquares=new HashSet<Square>();
        defendedSquares=new HashSet<Square>();
    }
    
    public void setSquare(Square square){
        if(this.square!=null){
            //this.square.oppPiece=null;
            this.square.piece=null;
        }
        
        this.square=square;
        this.square.piece=this;
        //this.square.oppPiece=(color==DEF.WHITE)?this.square.blackPiece:this.square.whitePiece;
    }
    
    public String toString(){
        String res=null;
        
        switch(type){
            case DEF.KING: res="k"; break;
            case DEF.QUEEN: res="q"; break;
            case DEF.BISHOP: res="b"; break;
            case DEF.KNIGHT: res="n"; break;
            case DEF.PAWN: res="p"; break;
            case DEF.ROOK: res="r"; break;
        }
        if(color==DEF.WHITE)
            res=res.toUpperCase();
        return res;
    }
    
    public void printDebug(){
        System.out.println("------------------------------");
        System.out.println(this+""+square);
        System.out.println("\n********influenced*********");
        for(Square sq: this.influencedSquares)
            System.out.print(sq+", ");
        System.out.println("\n*********movable***********");
        for(Square sq: this.movableSquares)
            System.out.print(sq+", ");
        System.out.println("\n*********attacked**********");
        for(Square sq: this.attackedSquares)
            System.out.print(sq+", ");
        System.out.println("\n*********defended**********");
        for(Square sq: this.defendedSquares)
            System.out.print(sq+", ");
        System.out.println("\n------------------------------");
    }
}
