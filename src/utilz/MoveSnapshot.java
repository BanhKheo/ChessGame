package utilz;

import chessPieces.Piece;

public class MoveSnapshot {
    public final Piece movedPiece;
    public final Piece capturedPiece;

    public final int fromRow, fromCol, toRow, toCol;

    public final boolean whiteTurnBeforeMove;

    // True if the moved piece had not moved before this move (for castling, pawn first move, etc.)
    public final boolean movedPieceFirstMove;
    public final boolean capturedPieceFirstMove;

    // Castling info (only used for king moves that are castling)
    public final int castlingRookFromCol; // Original rook column before castling (-1 if not castling)
    public final int castlingRookToCol;   // Rook destination col after castling (-1 if not castling)

    // Pawn promotion info (extend if you want to support undoing promotions)
    // public final Piece promotedPiece; // optional

    public MoveSnapshot(
            Piece movedPiece, Piece capturedPiece,
            int fromRow, int fromCol, int toRow, int toCol,
            boolean whiteTurnBeforeMove, boolean movedPieceFirstMove
    ) {
        this(movedPiece, capturedPiece, fromRow, fromCol, toRow, toCol,
                whiteTurnBeforeMove, movedPieceFirstMove, false, -1, -1);
    }

    public MoveSnapshot(
            Piece movedPiece, Piece capturedPiece,
            int fromRow, int fromCol, int toRow, int toCol,
            boolean whiteTurnBeforeMove, boolean movedPieceFirstMove,
            boolean capturedPieceFirstMove, int castlingRookFromCol, int castlingRookToCol
    ) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.whiteTurnBeforeMove = whiteTurnBeforeMove;
        this.movedPieceFirstMove = movedPieceFirstMove;
        this.capturedPieceFirstMove = capturedPieceFirstMove;
        this.castlingRookFromCol = castlingRookFromCol;
        this.castlingRookToCol = castlingRookToCol;
    }

    public boolean isCastlingMove() {
        return castlingRookFromCol != -1 && castlingRookToCol != -1;
    }

    public boolean isWhiteTurnBeforeMove() {
        return whiteTurnBeforeMove;
    }
}