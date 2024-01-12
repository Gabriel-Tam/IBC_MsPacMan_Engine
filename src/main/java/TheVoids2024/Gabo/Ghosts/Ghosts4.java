package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Ghosts4 extends GhostController {

    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);
    private Map<GHOST, Integer> lastSeenPacmanIndices = new HashMap<>();

    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        int currentPacmanIndex = game.getPacmanCurrentNodeIndex();
        updateLastSeenPacmanIndices(currentPacmanIndex);

        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
                moves.put(ghost, decideMove(game, ghost, possibleMoves, currentPacmanIndex));
            }
        }
        return moves;
    }

    private void updateLastSeenPacmanIndices(int currentPacmanIndex) {
        for (GHOST ghost : GHOST.values()) {
            lastSeenPacmanIndices.put(ghost, currentPacmanIndex);
        }
    }

    private MOVE decideMove(Game game, GHOST ghost, MOVE[] possibleMoves, int currentPacmanIndex) {
        boolean isPinkySupportingBlinky = (ghost == GHOST.PINKY) && isNearOtherGhost(game, GHOST.BLINKY, ghost);

        if (isPinkySupportingBlinky) {
            return getSupportiveMove(game, ghost, possibleMoves, currentPacmanIndex);
        } else if (ghost == GHOST.BLINKY || ghost == GHOST.INKY) {
            return getPursueMove(game, ghost, possibleMoves);
        } else if (game.getGhostEdibleTime(ghost) > 0 || closeToPower(game)) {
            return getRunAwayMove(game, ghost, possibleMoves);
        } else {
            return getPredictiveMove(game, ghost, possibleMoves, currentPacmanIndex);
        }
    }

    private boolean isNearOtherGhost(Game game, GHOST otherGhost, GHOST currentGhost) {
        int otherGhostIndex = game.getGhostCurrentNodeIndex(otherGhost);
        int currentGhostIndex = game.getGhostCurrentNodeIndex(currentGhost);
        return game.getShortestPathDistance(otherGhostIndex, currentGhostIndex) < 30;
    }

    private MOVE getSupportiveMove(Game game, GHOST ghost, MOVE[] possibleMoves, int currentPacmanIndex) {
        // Objetivo: Flanquear a Pac-Man o bloquear su ruta de escape.
        int blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
        MOVE blinkyLastMove = game.getGhostLastMoveMade(GHOST.BLINKY);
    
        // Calcula la ruta de Blinky hacia Pac-Man
        int[] pathFromBlinkyToPacman = game.getShortestPath(blinkyIndex, currentPacmanIndex, blinkyLastMove);
    
        if (pathFromBlinkyToPacman.length > 2) {
            // Intenta tomar una posición que flanquee la posición de Pac-Man con respecto a Blinky
            int flankingPosition = pathFromBlinkyToPacman[pathFromBlinkyToPacman.length / 2];
            return game.getNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), flankingPosition, game.getGhostLastMoveMade(ghost),DM.PATH);
        }
    
        // Si no es posible flanquear, simplemente persigue a Pac-Man
        return game.getNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), currentPacmanIndex, game.getGhostLastMoveMade(ghost),DM.PATH);
    }
    

    private MOVE getPredictiveMove(Game game, GHOST ghost, MOVE[] possibleMoves, int currentPacmanIndex) {
        // Objetivo: Predecir el siguiente movimiento de Pac-Man
        MOVE lastMove = game.getPacmanLastMoveMade();
    
        // Estima la próxima posición de Pac-Man
        int predictedPacmanIndex = game.getNeighbour(currentPacmanIndex, lastMove);
    
        // Si la posición predicha no es válida, usa la posición actual de Pac-Man
        if (predictedPacmanIndex == -1) {
            predictedPacmanIndex = currentPacmanIndex;
        }
    
        return game.getNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), predictedPacmanIndex, game.getGhostLastMoveMade(ghost), DM.PATH);
    }
    
    


    private boolean closeToPower(Game game) {
        int[] powerPills = game.getActivePowerPillsIndices();
        for (int pp : powerPills) {
            int distanceMin = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pp, game.getPacmanLastMoveMade());
            if (distanceMin <= 60) {
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
    // Existing methods (getRunAwayMove, getPursueMove, closeToPower) remain unchanged.
}
