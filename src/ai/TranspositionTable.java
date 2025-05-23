package ai;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimized TranspositionTable for chess AI.
 * - Uses ConcurrentHashMap for thread safety (future-proofing for parallel search).
 * - TTEntry is immutable for safety and efficiency.
 * - Uses static final constants for flag values.
 * - get/put/contains methods are inlined for clarity.
 */
public class TranspositionTable {
    // Thread-safe for possible parallel search, and usually faster than HashMap for read-heavy usage
    private final ConcurrentHashMap<Long, TTEntry> table = new ConcurrentHashMap<>(1 << 16);

    public void put(long hash, TTEntry entry) {
        // Only keep the entry if it is deeper than or equal to the current one (replace only if better)
        table.compute(hash, (k, old) -> (old == null || entry.depth >= old.depth) ? entry : old);
    }

    public TTEntry get(long hash) {
        return table.get(hash);
    }

    public boolean contains(long hash) {
        return table.containsKey(hash);
    }

    public void clear() {
        table.clear();
    }

    public static final class TTEntry {
        public static final int EXACT = 0, LOWERBOUND = -1, UPPERBOUND = 1;

        public final int depth;
        public final int score;
        public final int flag;
        public final ChessAI.Move bestMove;

        public TTEntry(int depth, int score, int flag, ChessAI.Move bestMove) {
            this.depth = depth;
            this.score = score;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }
}