

package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.Constants.DM;

public final class Ghosts5 extends GhostController {

    private final EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        MOVE pacmanLastMove = game.getPacmanLastMoveMade();

        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost)) {
                MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
                if (ghost == GHOST.BLINKY) {
                    moves.put(ghost, getPursueMove(game, ghost, possibleMoves, pacmanIndex));
                } else {
                    if (isGhostInDanger(game, ghost) || isPacmanCloseToPowerPill(game)) {
                        moves.put(ghost, getDisperseMove(game, ghost, possibleMoves, pacmanIndex));
                    } else if (isCloserToPowerPillThanPacman(game, ghost, pacmanIndex)) {
                        moves.put(ghost, moveToPowerPill(game, ghost, possibleMoves));
                    } else {
                        int nextPacmanPosition = predictNextPacmanPosition(game, pacmanIndex, pacmanLastMove);
                        moves.put(ghost, getPursueMove(game, ghost, possibleMoves, nextPacmanPosition));
                    }
                }
            }
        }
        return moves;
    }

    private boolean isGhostInDanger(Game game, GHOST ghost) {
        return game.getGhostEdibleTime(ghost) > 0;
    }

    private boolean isPacmanCloseToPowerPill(Game game) {
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        for (int powerPillIndex : game.getActivePowerPillsIndices()) {
            if (game.getShortestPathDistance(pacmanIndex, powerPillIndex) <= 60) {
                return true;
            }
        }
        return false;
    } 
        private MOVE getDisperseMove(Game game, GHOST currentGhost, MOVE[] possibleMoves, int pacmanIndex) {
        MOVE bestMove = null;
        int maxDistance = -1;
        int ghostLocation = game.getGhostCurrentNodeIndex(currentGhost);

        for (MOVE move : possibleMoves) {
            int neighbourIndex = game.getNeighbour(ghostLocation, move);
            int distanceFromPacman = game.getShortestPathDistance(pacmanIndex, neighbourIndex);
            int distanceFromGhosts = getMinDistanceToOtherGhosts(game, neighbourIndex, currentGhost);

            int combinedDistance = distanceFromPacman + distanceFromGhosts;
            if (combinedDistance > maxDistance) {
                maxDistance = combinedDistance;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int getMinDistanceToOtherGhosts(Game game, int nodeIndex, GHOST excludeGhost) {
        int minDistance = Integer.MAX_VALUE;
        for (GHOST ghost : GHOST.values()) {
            if (ghost != excludeGhost && game.getGhostLairTime(ghost) == 0) {
                int distance = game.getShortestPathDistance(nodeIndex, game.getGhostCurrentNodeIndex(ghost));
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }
    

    private boolean isCloserToPowerPillThanPacman(Game game, GHOST ghost, int pacmanIndex) {
        int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
        for (int powerPillIndex : game.getActivePowerPillsIndices()) {
            if (game.getShortestPathDistance(ghostIndex, powerPillIndex) < game.getShortestPathDistance(pacmanIndex, powerPillIndex)) {
                return true;
            }
        }
        return false;
    }

    private MOVE moveToPowerPill(Game game, GHOST ghost, MOVE[] possibleMoves) {
        // Logic to move towards the closest power pill
        // ...

        // Example: Find the closest power pill and move towards it
        int ghostIndex = game.getGhostCurrentNodeIndex(ghost);
        int closestPowerPill = findClosestPowerPill(game, ghostIndex);
        return game.getNextMoveTowardsTarget(ghostIndex, closestPowerPill, DM.PATH);
    }

    private int findClosestPowerPill(Game game, int ghostIndex) {
        int closestPowerPill = -1;
        int minDistance = Integer.MAX_VALUE;
        for (int powerPillIndex : game.getActivePowerPillsIndices()) {
            int distance = game.getShortestPathDistance(ghostIndex, powerPillIndex);
            if (distance < minDistance) {
                minDistance = distance;
                closestPowerPill = powerPillIndex;
            }
        }
        return closestPowerPill;
    }

    private int predictNextPacmanPosition(Game game, int pacmanIndex, MOVE lastMove) {
        // Predicts Pac-Man's next position
        // Use getApproximateNextMoveAwayFromTarget to estimate Pac-Man's next position
        MOVE nextMove = game.getApproximateNextMoveAwayFromTarget(pacmanIndex, game.getPacmanCurrentNodeIndex(), lastMove, DM.PATH);
        return game.getNeighbour(pacmanIndex, nextMove);
    }

    private MOVE getPursueMove(Game game, GHOST ghost, MOVE[] possibleMoves, int targetIndex) {
        MOVE bestMove = null;
        int minDistance = Integer.MAX_VALUE;
        int ghostLocation = game.getGhostCurrentNodeIndex(ghost);

        for (MOVE move : possibleMoves) {
            int neighbourIndex = game.getNeighbour(ghostLocation, move);
            int distance = game.getShortestPathDistance(targetIndex, neighbourIndex);
            if (distance < minDistance) {
                minDistance = distance;
                bestMove = move;
            }
        }
        return bestMove;
    }
}

