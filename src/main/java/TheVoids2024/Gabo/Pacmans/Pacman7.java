package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.Arrays;

public final class Pacman7 extends PacmanController {
    private boolean isPowerPillActive = false;

    public Pacman7() {
    }

    public MOVE getMove(Game game, long timeDue) {
        if (allGhostsOut(game)) {

            return goToPowerPill(game);
        } else {
            return isPowerPillActive ? attackGhosts(game) : eatNormalPillsOrAvoidGhosts(game);
        }
    }

    private MOVE eatNormalPillsOrAvoidGhosts(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        int nearestGhost = getNearestGhost(game);

        if (nearestGhost == -1 || game.getShortestPathDistance(pacmanNode, nearestGhost) > 20) {
            // No ghosts nearby or ghosts are far away, eat normal pills
            return eatNormalPills(game);
        } else {
            // Ghosts are nearby, try to avoid them or eat if edible
            if (areAllGhostsEdible(game)) {
                return eatEdibleGhost(game);
            } else {
                return avoidGhosts(game);
            }
        }
    }

    private MOVE avoidGhosts(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int nearestGhost = getNearestGhost(game);

        if (nearestGhost != -1) {
            return game.getApproximateNextMoveAwayFromTarget(pacman, nearestGhost, game.getPacmanLastMoveMade(), DM.PATH);
        } else {
            // No ghosts nearby, stop
            return MOVE.NEUTRAL;
        }
    }

    private MOVE eatEdibleGhost(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int[] edibleGhosts = getEdibleGhosts(game);

        if (edibleGhosts.length > 0) {
            int targetGhost = edibleGhosts[0];  // Target the first edible ghost
            return game.getApproximateNextMoveTowardsTarget(pacman, targetGhost, game.getPacmanLastMoveMade(), DM.PATH);
        } else {
            // No edible ghosts, stop
            return MOVE.NEUTRAL;
        }
    }

    private MOVE eatNormalPills(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int[] activePills = game.getActivePillsIndices();

        if (activePills.length > 0) {
            int targetPill = activePills[0];  // Target the first active pill
            return game.getApproximateNextMoveTowardsTarget(pacman, targetPill, game.getPacmanLastMoveMade(), DM.PATH);
        } else {
            // No active pills, stop
            return MOVE.NEUTRAL;
        }
    }

    private MOVE attackGhosts(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int nearestGhost = getNearestGhost(game);

        if (nearestGhost != -1) {
            return game.getApproximateNextMoveTowardsTarget(pacman, nearestGhost, game.getPacmanLastMoveMade(), DM.PATH);
        } else {
            // No ghosts nearby, stop
            return MOVE.NEUTRAL;
        }
    }

    private MOVE goToPowerPill(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        int[] powerPills = game.getActivePowerPillsIndices();
        MOVE pacmanMove = game.getPacmanLastMoveMade();

        if (powerPills.length > 0) {
            int targetPowerPill = getNearestPowerPill(game, pacmanNode, powerPills);
            return game.getApproximateNextMoveTowardsTarget(pacmanNode, targetPowerPill, pacmanMove, DM.PATH);
        } else {
            // No power pills, stop
            return MOVE.NEUTRAL;
        }
    }

    private int getNearestGhost(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int[] ghosts = getActiveGhosts(game);

        if (ghosts.length > 0) {
            return game.getClosestNodeIndexFromNodeIndex(pacman, ghosts, DM.PATH);
        } else {
            return -1; // No ghosts
        }
    }

    private int[] getEdibleGhosts(Game game) {
        int[] ghosts = new int[GHOST.values().length];
        int count = 0;

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                ghosts[count++] = game.getGhostCurrentNodeIndex(ghost);
            }
        }

        return Arrays.copyOfRange(ghosts, 0, count);
    }

    private int[] getActiveGhosts(Game game) {
        int[] ghosts = new int[GHOST.values().length];
        int count = 0;

        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostLairTime(ghost) <= 0) {
                ghosts[count++] = game.getGhostCurrentNodeIndex(ghost);
            }
        }

        return Arrays.copyOfRange(ghosts, 0, count);
    }

    private boolean areAllGhostsEdible(Game game) {
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost)) {
                return false;
            }
        }
        return true;
    }

    private int getNearestPowerPill(Game game, int pacmanNode, int[] powerPills) {
        int nearestPowerPill = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int powerPill : powerPills) {
            int distance = game.getShortestPathDistance(pacmanNode, powerPill);
            if (distance < minDistance) {
                minDistance = distance;
                nearestPowerPill = powerPill;
            }
        }

        return nearestPowerPill;
    }

    private boolean allGhostsOut(Game game) {
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostLairTime(ghost) != 0) {
                return false;
            }
        }
        return true;
    }

}
