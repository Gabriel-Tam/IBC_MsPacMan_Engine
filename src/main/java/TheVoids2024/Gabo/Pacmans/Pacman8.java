package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.*;

public final class Pacman8 extends PacmanController {
    private static final int SECURITY_DISTANCE = 30;
    private GameInfoUpdater gameInfoUpdater;
    private GhostManager ghostManager;
    private PillManager pillManager;

    public Pacman8() {
        gameInfoUpdater = new GameInfoUpdater();
        ghostManager = new GhostManager(SECURITY_DISTANCE);
        pillManager = new PillManager();
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        gameInfoUpdater.update(game);

        if (gameInfoUpdater.isPacmanPowered(game)) {
            return ghostManager.getMoveToPursueGhosts(game, gameInfoUpdater);
        } else if (gameInfoUpdater.hasActivePowerPills()) {
            return pillManager.getMoveGoToPowerPill(game, gameInfoUpdater, ghostManager);
        } else {
            return pillManager.getMoveToEatNormalPills(game, gameInfoUpdater);
        }
    }
}

class GameInfoUpdater {
    private int pacmanNode;
    private MOVE pacmanLastMove;
    private boolean hasActivePowerPills;

    public void update(Game game) {
        pacmanNode = game.getPacmanCurrentNodeIndex();
        pacmanLastMove = game.getPacmanLastMoveMade();
        hasActivePowerPills = game.getActivePowerPillsIndices().length > 0;
    }

    public int getPacmanNode() {
        return pacmanNode;
    }

    public MOVE getPacmanLastMove() {
        return pacmanLastMove;
    }

    public boolean isPacmanPowered(Game game) {
        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActivePowerPills() {
        return hasActivePowerPills;
    }
}

class GhostManager {
    private int securityDistance;
    private List<GHOST> edibleGhosts;
    private List<GHOST> inedibleGhosts;
    private Map<GHOST, Integer> edibleGhostNodes;

    public GhostManager(int securityDistance) {
        this.securityDistance = securityDistance;
        edibleGhosts = new ArrayList<>();
        inedibleGhosts = new ArrayList<>();
        edibleGhostNodes = new HashMap<>();
    }

    public void updateGhostsInfo(Game game) {
        edibleGhosts.clear();
        inedibleGhosts.clear();
        edibleGhostNodes.clear();

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
                edibleGhostNodes.put(ghost, game.getGhostCurrentNodeIndex(ghost));
            } else if (game.getGhostLairTime(ghost) == 0) {
                inedibleGhosts.add(ghost);
            }
        }
    }

    public MOVE getMoveToPursueGhosts(Game game, GameInfoUpdater infoUpdater) {
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;

        for (GHOST ghost : edibleGhosts) {
            int ghostNode = edibleGhostNodes.get(ghost);
            int distance = game.getShortestPathDistance(infoUpdater.getPacmanNode(), ghostNode);

            if (distance < minDistance) {
                minDistance = distance;
                closestGhost = ghost;
            }
        }

        if (closestGhost != null) {
            int ghostNode = edibleGhostNodes.get(closestGhost);
            return game.getNextMoveTowardsTarget(infoUpdater.getPacmanNode(), ghostNode, DM.PATH);
        } else {
            return infoUpdater.getPacmanLastMove();
        }
    }

    public boolean areInedibleGhostsNear(Game game, int node) {
        return inedibleGhosts.stream().anyMatch(
                ghost -> game.getDistance(node, game.getGhostCurrentNodeIndex(ghost), DM.PATH) < securityDistance);
    }
}

class PillManager {
    public MOVE getMoveGoToPowerPill(Game game, GameInfoUpdater infoUpdater, GhostManager ghostManager) {
        int[] powerPills = game.getActivePowerPillsIndices();
        int closestPill = -1;
        double closestDistance = Double.MAX_VALUE;

        for (int powerPillIndex : powerPills) {
            double distance = game.getDistance(infoUpdater.getPacmanNode(), powerPillIndex, DM.PATH);

            if (!ghostManager.areInedibleGhostsNear(game, infoUpdater.getPacmanNode())) {
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPill = powerPillIndex;
                }
            }
        }

        if (closestPill != -1) {
            return game.getApproximateNextMoveTowardsTarget(infoUpdater.getPacmanNode(), closestPill,
                    infoUpdater.getPacmanLastMove(), DM.PATH);
        } else {
            return getMoveToEatNormalPills(game, infoUpdater); // Fallback to eating normal pills
        }
    }

    public MOVE getMoveToEatNormalPills(Game game, GameInfoUpdater infoUpdater) {
        int[] pills = game.getActivePillsIndices();
        int closestPill = getClosestPill(game, pills, infoUpdater.getPacmanNode());

        if (closestPill != -1) {
            return game.getApproximateNextMoveTowardsTarget(infoUpdater.getPacmanNode(), closestPill,
                    infoUpdater.getPacmanLastMove(), DM.PATH);
        }
        return infoUpdater.getPacmanLastMove(); // or any default move
    }

    private int getClosestPill(Game game, int[] pills, int pacmanNode) {
        int closestPill = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int pill : pills) {
            int distance = game.getShortestPathDistance(pacmanNode, pill);

            if (distance < minDistance) {
                minDistance = distance;
                closestPill = pill;
            }
        }

        return closestPill;
    }
}
