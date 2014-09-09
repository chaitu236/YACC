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
public class Square {
    public Piece piece;
    
    public HashSet<Piece> whitePiece; //white pieces which have influence in this square
    public HashSet<Piece> blackPiece; //black pieces which have influence in this square
    //public HashSet<Piece> oppPiece;
    
    int row;
    int file;
    
    public Square(int row, int file){
        assert(row>=0 && row<8);
        assert(file>=0 && file<8);
        this.row=row;
        this.file=file;
        
        whitePiece=new HashSet<Piece>();
        blackPiece=new HashSet<Piece>();
    }
    
    public String toString(){
        //return "("+row+", "+file+")";
        return ""+(char)(file+'a')+""+(row+1);
    }
    
    public void printDebug(){
        System.out.println("------------------------------");
        System.out.println(this+""+this.piece);
        System.out.println("\n********whitepiece*********");
        for(Piece pc: this.whitePiece)
            System.out.print(pc+" "+pc.square+", ");
        System.out.println("\n*********blackpiece***********");
        for(Piece pc: this.blackPiece)
            System.out.print(pc+" "+pc.square+", ");
        System.out.println("\n------------------------------");
    }
}
