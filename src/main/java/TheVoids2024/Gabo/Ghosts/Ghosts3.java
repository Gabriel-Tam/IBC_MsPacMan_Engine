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
            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
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
        for (int i : powerPills) {
            int n = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), i, game.getPacmanLastMoveMade());
            if (n <= 60) {
                // System.out.println("si esta cerca: " + n);
                return true;
            }
        }
        return false;
    }
    
    private MOVE getRunAwayMove(Game game, GHOST ghost, MOVE[] possibilitiesMoves) {
        Map<MOVE, Integer> allMovesValues = new HashMap<MOVE, Integer>(possibilitiesMoves.length);
        int ghLocation = game.getGhostCurrentNodeIndex(ghost);
        int pcLocation = game.getPacmanCurrentNodeIndex();
        for (MOVE move : possibilitiesMoves) {
            int neighbour = game.getNeighbour(ghLocation, move);
            int distanceValue = game.getShortestPathDistance(pcLocation, neighbour, game.getPacmanLastMoveMade());
            allMovesValues.put(move, distanceValue);
        }
        
        // Best move
        int bestDistance = Integer.MIN_VALUE;
        MOVE bestMove = null;
        for (MOVE move : possibilitiesMoves) {
            if (allMovesValues.get(move) != null)
                if (allMovesValues.get(move) > bestDistance) {
                    bestDistance = allMovesValues.get(move);
                    bestMove = move;
                }
        }
        // System.out.println(ghost.name() + " : " + bestMove + " RunAway");
        return bestMove;
    }

    private MOVE getPursueMove(Game game, GHOST ghost, MOVE[] possibleMoves) {
        Map<MOVE, Integer> allMovesValues = new HashMap<MOVE, Integer>(possibleMoves.length);
        int ghLocation = game.getGhostCurrentNodeIndex(ghost);
        int pcLocation = game.getPacmanCurrentNodeIndex();
        for (MOVE move : possibleMoves) {
            int neighbour = game.getNeighbour(ghLocation, move);
            int distanceValue = game.getShortestPathDistance(pcLocation, neighbour, game.getPacmanLastMoveMade());
            allMovesValues.put(move, distanceValue);
        }           
        
        // Best move
        int bestDistance = Integer.MAX_VALUE;
        MOVE bestMove = null;
        
        for (MOVE move : possibleMoves) {
            if (allMovesValues.get(move) != null)
                if (allMovesValues.get(move) < bestDistance) {
                    bestDistance = allMovesValues.get(move);
                    bestMove = move;
                }
        }
        return bestMove;
    }
}
