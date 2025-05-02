package ai;
import java.util.HashMap;


public class TranspositionTable {
    private final HashMap<Long, TTEntry> table = new HashMap<>();

    public void put(long hash, TTEntry entry) {
        table.put(hash, entry);
    }

    public TTEntry get(long hash) {
        return table.get(hash);
    }

    public boolean contains(long hash) {
        return table.containsKey(hash);
    }
}

class TTEntry {
    public int depth;
    public int score;
    public int flag; // EXACT = 0, LOWERBOUND = -1, UPPERBOUND = 1
    public ChessAI.Move bestMove;

    public TTEntry(int depth, int score, int flag, ChessAI.Move bestMove) {
        this.depth = depth;
        this.score = score;
        this.flag = flag;
        this.bestMove = bestMove;
    }
}
