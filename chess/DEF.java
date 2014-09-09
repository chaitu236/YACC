/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chess;

/**
 *
 * @author chaitanya
 */
public class DEF {
    public static final int KING=0;
    public static final int QUEEN=1;
    public static final int BISHOP=2;
    public static final int ROOK=3;
    public static final int KNIGHT=4;
    public static final int PAWN=5;
    
    public static final int WHITE=0;
    public static final int BLACK=1;
    
    public static final int PLAYING=0;
    public static final int DRAW_50_MOVES=1;
    public static final int DRAW_REPETITION=2;
    public static final int STALEMATE=3;
    public static final int CHECKMATE=4;
    public static final int PROMOTION_PIECE=DEF.QUEEN;
    
    public static final boolean WRITE_TO_FILE=true;
    
    public static void OUT(Object ob){
        System.out.println(ob);
    }
}
