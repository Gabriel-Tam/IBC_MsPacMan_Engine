package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Ghosts4 extends GhostController {

    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        GHOST leader = selectLeader(game);
        boolean shouldRunAway = closeToPower(game); // Verifica si Pac-Man está cerca de una píldora de poder

        for (GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghostMove = game.getGhostLastMoveMade(ghost);

            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(ghostNode, ghostMove);

                if (shouldRunAway) {
                    moves.put(ghost, getRunAwayMove(game, ghost, possibleMoves));
                } else {
                    if (ghost == leader) {
                        moves.put(ghost, getPursueMove(game, ghost, possibleMoves));
                    } else {
                        moves.put(ghost, getSupportMove(game, ghost, possibleMoves, leader));
                    }
                }
            }
        }
        return moves;
    }

    private GHOST selectLeader(Game game) {
        GHOST leader = null;
        int minDistance = Integer.MAX_VALUE;
    
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostLairTime(ghost) == 0) { // Comprobamos si el fantasma no está en la guarida
                int distance = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),
                                                           game.getPacmanCurrentNodeIndex());
                if (distance < minDistance) {
                    minDistance = distance;
                    leader = ghost;
                }
            }
        }
    
        return leader != null ? leader : GHOST.BLINKY; // BLINKY por defecto si todos están en la guarida
    }
    

    private MOVE getSupportMove(Game game, GHOST ghost, MOVE[] possibleMoves, GHOST leader) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        int leaderNode = game.getGhostCurrentNodeIndex(leader);
    
        MOVE bestMove = MOVE.NEUTRAL;
        int minDistanceToPacman = Integer.MAX_VALUE;
    
        for (MOVE move : possibleMoves) {
            int newNode = game.getNeighbour(ghostNode, move);
            int distanceToPacman = game.getShortestPathDistance(newNode, pacmanNode);
    
            // Preferimos movimientos que nos acerquen a Pac-Man pero evitando seguir directamente al líder
            if (distanceToPacman < minDistanceToPacman && newNode != leaderNode) {
                minDistanceToPacman = distanceToPacman;
                bestMove = move;
            }
        }
    
        return bestMove;
    }
    

    private boolean closeToPower(Game game) {
        int[] powerPills = game.getActivePowerPillsIndices();
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();
    
        for (int powerPillNode : powerPills) {
            int distance = game.getShortestPathDistance(pacmanNode, powerPillNode, pacmanMove);
            if (distance <= 60 && movingTowardsPowerPill(game, pacmanNode, powerPillNode, pacmanMove)) {
                return true;
            }
        }
        return false;
    }

    private boolean movingTowardsPowerPill(Game game, int pacmanNode, int powerPillNode, MOVE pacmanMove) {
        // Obtener la posición siguiente de Pac-Man basada en su movimiento actual
        int nextPacmanNode = game.getNeighbour(pacmanNode, pacmanMove);
    
        // Verificar si el siguiente nodo de Pac-Man lo acerca más a la píldora de poder
        int distanceFromPacmanToPill = game.getShortestPathDistance(pacmanNode, powerPillNode);
        int distanceFromNextNodeToPill = game.getShortestPathDistance(nextPacmanNode, powerPillNode);
    
        return distanceFromNextNodeToPill < distanceFromPacmanToPill;
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
