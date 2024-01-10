package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts3 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);
    private GHOST[] ghostnames = GHOST.values();
    private MOVE[] allMoves = MOVE.values();

    private boolean pacManClosePowerPills(Game game) {
        int pacManNode = game.getPacmanCurrentNodeIndex();
        int[] powerPills = game.getActivePowerPillsIndices();
        for (int powerPill : powerPills) {
            Double distance = game.getDistance(pacManNode, powerPill, game.getPacmanLastMoveMade(), DM.PATH);
            if (distance <= 50) {
                return true;
            }
        }
        return false;
    }

    private void followTargetOrRandom(Game game, GHOST ghost, int targetNode, int minDistance, int maxDistance) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        MOVE move;

        Double distanceToTarget = game.getDistance(ghostNode, targetNode, game.getPacmanLastMoveMade(), DM.PATH);
        if (distanceToTarget >= minDistance && distanceToTarget <= maxDistance) {
            move = game.getNextMoveTowardsTarget(ghostNode, targetNode, DM.PATH);
        } else {
            move = allMoves[game.getGhostLastMoveMade(ghost).ordinal()];
        }

        moves.put(ghost, move);
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();

        int pacManNode = game.getPacmanCurrentNodeIndex();

        for (GHOST ghost : ghostnames) {
            if (game.doesGhostRequireAction(ghost)) {
                if (ghost.equals(GHOST.BLINKY)) {
                    followTargetOrRandom(game, ghost, pacManNode, 0, Integer.MAX_VALUE);
                } else if (ghost.equals(GHOST.SUE)) {
                    followTargetOrRandom(game, ghost, pacManNode, 0, 85);
                } else if (ghost.equals(GHOST.PINKY) || ghost.equals(GHOST.INKY)) {
                    int targetNode = pacManNode + 5;
                    if (!pacManClosePowerPills(game)) {
                        followTargetOrRandom(game, ghost, targetNode, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    } else {
                        // Handle logic when Pac-Man is close to power pills
                        // Add your specific logic here if needed
                        // For now, just continue random movement
                        moves.put(ghost, allMoves[game.getGhostLastMoveMade(ghost).ordinal()]);
                    }
                }
            }
        }

        return this.moves;
    }
}
