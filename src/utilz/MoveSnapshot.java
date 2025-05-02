package utilz;
import chessPieces.Piece;

public class MoveSnapshot {
    public Piece movedPiece;
    public Piece capturedPiece;
    public int fromRow, fromCol, toRow, toCol;
    public boolean whiteTurnBeforeMove;

    // Optional first-move flags (use if you implement castling or pawn promotion logic)
    public boolean movedPieceFirstMove;
    public boolean capturedPieceFirstMove;

    public MoveSnapshot(Piece movedPiece, Piece capturedPiece,
                        int fromRow, int fromCol, int toRow, int toCol) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }
}

