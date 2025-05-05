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
                        int fromRow, int fromCol, int toRow, int toCol, boolean whiteTurnBeforeMove, boolean movedPieceFirstMove) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.whiteTurnBeforeMove = whiteTurnBeforeMove;
        this.movedPieceFirstMove = movedPieceFirstMove;
        this.capturedPieceFirstMove = false; // Set if needed for en passant or other case
    }

    public boolean isWhiteTurnBeforeMove() {
        return whiteTurnBeforeMove;
    }
}

