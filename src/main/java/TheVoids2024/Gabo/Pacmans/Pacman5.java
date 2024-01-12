package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

import java.util.Random;

public final class Pacman5 extends PacmanController {
    private Random random = new Random();
    private int dangerDistance = 20;

    public Pacman5() {
    }

    public MOVE getMove(Game game, long timeDue) {
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        int numPowerPills = game.getNumberOfActivePowerPills();
        int nearestAttackingGhost = getNearestGhost(game, false);
        int nearestEdibleGhost = getNearestGhost(game, true);
        int nearestPill = getNearestPill(game);

        if (numPowerPills <= 0) {
            if (game.getShortestPathDistance(pacmanIndex, nearestAttackingGhost) < dangerDistance) {
                return game.getApproximateNextMoveAwayFromTarget(pacmanIndex, nearestAttackingGhost, game.getPacmanLastMoveMade(), DM.PATH);
            } else if (nearestEdibleGhost != -1 && game.getShortestPathDistance(pacmanIndex, nearestEdibleGhost, game.getPacmanLastMoveMade()) < dangerDistance * 4) {
                return game.getMoveToMakeToReachDirectNeighbour(pacmanIndex, nearestEdibleGhost);
            } else {
                return game.getApproximateNextMoveTowardsTarget(pacmanIndex, nearestPill, game.getPacmanLastMoveMade(), DM.MANHATTAN);
            }
        } else {
            if (game.getShortestPathDistance(pacmanIndex, nearestAttackingGhost) < dangerDistance) {
                return game.getApproximateNextMoveAwayFromTarget(pacmanIndex, nearestAttackingGhost, game.getPacmanLastMoveMade(), DM.PATH);
            } else if (game.getShortestPathDistance(pacmanIndex, nearestPill) > game.getShortestPathDistance(pacmanIndex, nearestAttackingGhost) && game.getShortestPathDistance(pacmanIndex, nearestAttackingGhost) < 4 * dangerDistance) {
                return game.getApproximateNextMoveAwayFromTarget(pacmanIndex, nearestAttackingGhost, game.getPacmanLastMoveMade(), DM.PATH);
            } else if (nearestEdibleGhost == -1 || !areAllGhostsEdible(game) && game.getShortestPathDistance(nearestEdibleGhost, pacmanIndex, game.getPacmanLastMoveMade()) >= game.getShortestPathDistance(pacmanIndex, nearestAttackingGhost, game.getPacmanLastMoveMade())) {
                return game.getShortestPathDistance(pacmanIndex, nearestPill, game.getPacmanLastMoveMade()) < game.getShortestPathDistance(nearestAttackingGhost, nearestPill, game.getPacmanLastMoveMade()) ? game.getMoveToMakeToReachDirectNeighbour(pacmanIndex, nearestPill) : null;
            } else {
                return game.getMoveToMakeToReachDirectNeighbour(pacmanIndex, nearestEdibleGhost);
            }
        }
    }

 private int getNearestGhost(Game game, boolean edible) {
    int pacmanIndex = game.getPacmanCurrentNodeIndex();
    int[] ghostIndices = new int[GHOST.values().length];
    int index = 0;

    for (GHOST ghost : GHOST.values()) {
        if (game.isGhostEdible(ghost) == edible) {
            ghostIndices[index] = game.getGhostCurrentNodeIndex(ghost);
            index++;
        }
    }

    return index > 0 ? game.getClosestNodeIndexFromNodeIndex(pacmanIndex, ghostIndices, DM.PATH) : -1;
}


    private int getNearestPill(Game game) {
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        int[] activePills = game.getActivePillsIndices();
        return activePills.length > 0 ? game.getClosestNodeIndexFromNodeIndex(pacmanIndex, activePills, DM.PATH) : -1;
    }
   
    private boolean areAllGhostsEdible(Game game) {
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost)) {
                return false;
            }
        }
        return true;
    }


}
