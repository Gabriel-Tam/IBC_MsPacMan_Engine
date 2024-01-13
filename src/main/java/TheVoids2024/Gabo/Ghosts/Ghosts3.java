package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Ghosts3 extends GhostController {

    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        for (GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghostMove = game.getGhostLastMoveMade(ghost);
            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(ghostNode, ghostMove);
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

    private MOVE getRunAwayMove(Game game, GHOST ghost, MOVE[] posibleMoves) {
        Map<MOVE, Integer> allMoves = new HashMap<MOVE, Integer>(posibleMoves.length);
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();
        for (MOVE move : posibleMoves) {
            int neighbour = game.getNeighbour(ghostNode, move);
            int distanceValue = game.getShortestPathDistance(pacmanNode, neighbour, pacmanMove);
            allMoves.put(move, distanceValue);
        }

        int bestDistance = Integer.MIN_VALUE;
        MOVE bestMove = null;
        for (MOVE move : posibleMoves) {
            if (allMoves.get(move) != null)
                if (allMoves.get(move) > bestDistance) {
                    bestDistance = allMoves.get(move);
                    bestMove = move;
                }
        }
        return bestMove;
    }

    private MOVE getPursueMove(Game game, GHOST ghost, MOVE[] possibleMoves) {
        Map<MOVE, Integer> allMoves = new HashMap<MOVE, Integer>(possibleMoves.length);
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();
        for (MOVE move : possibleMoves) {
            int neighbour = game.getNeighbour(ghostNode, move);
            int distanceValue = game.getShortestPathDistance(pacmanNode, neighbour, pacmanMove);
            allMoves.put(move, distanceValue);
        }

        int bestDistance = Integer.MAX_VALUE;
        MOVE bestMove = null;

        for (MOVE move : possibleMoves) {
            if (allMoves.get(move) != null)
                if (allMoves.get(move) < bestDistance) {
                    bestDistance = allMoves.get(move);
                    bestMove = move;
                }
        }
        return bestMove;
    }
}
