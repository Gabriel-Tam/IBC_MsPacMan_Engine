package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Ghosts5Better extends GhostController {

    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        for (GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghostMove = game.getGhostLastMoveMade(ghost);
            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(ghostNode,ghostMove);
                moves.put(ghost, decideMove(game, ghost, possibleMoves));
            }
        }
        return moves;
    }

    private MOVE decideMove(Game game, GHOST ghost, MOVE[] possibleMoves) {
        if (ghost == GHOST.BLINKY) {
            return getPursueMove(game, ghost, possibleMoves);
        } else if (game.getGhostEdibleTime(ghost) > 0 || closeToPower(game)) {
            return getRunAwayMove(game, ghost, possibleMoves);
        } else {
            return getPursueMove(game, ghost, possibleMoves);
        }
    }

    private boolean closeToPower(Game game) {
        int[] powerPills = game.getActivePowerPillsIndices();
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();
        for (int pp : powerPills) {
            if (game.getShortestPathDistance(pacmanNode, pp, pacmanMove) <= 60) {
                return true;
            }
        }
        return false;
    }

    private MOVE getBestMove(Game game, GHOST ghost, MOVE[] possibleMoves,
            BiFunction<Integer, Integer, Boolean> comparisonFunction) {
        Map<MOVE, Integer> allMovesValues = new HashMap<>();
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();

        for (MOVE move : possibleMoves) {
            int neighbour = game.getNeighbour(ghostNode, move);
            int distanceValue = game.getShortestPathDistance(pacmanNode, neighbour, pacmanMove);
            allMovesValues.put(move, distanceValue);
        }

        MOVE bestMove = null;
        int bestValue = comparisonFunction.apply(1, 2) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (Map.Entry<MOVE, Integer> entry : allMovesValues.entrySet()) {
            if (comparisonFunction.apply(entry.getValue(), bestValue)) {
                bestValue = entry.getValue();
                bestMove = entry.getKey();
            }
        }
        return bestMove;
    }

    private MOVE getRunAwayMove(Game game, GHOST ghost, MOVE[] possibleMoves) {
        return getBestMove(game, ghost, possibleMoves, (value, best) -> value > best);
    }

    private MOVE getPursueMove(Game game, GHOST ghost, MOVE[] possibleMoves) {
        return getBestMove(game, ghost, possibleMoves, (value, best) -> value < best);
    }
}
