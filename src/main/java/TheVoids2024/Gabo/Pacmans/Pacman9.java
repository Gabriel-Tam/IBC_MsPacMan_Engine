package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.*;

public final class Pacman9 extends PacmanController {
    private int pacmanPowerTime = 0;
    private int pacmanNode;
    private MOVE pacmanNextMove;
    private List<GHOST> edibleGhosts = new ArrayList<>();
    private List<GHOST> inedibleGhosts = new ArrayList<>();
    private Map<GHOST, Integer> edibleGhostNodes = new HashMap<>();
    private Map<Integer, int[]> powerPillNodes = new HashMap<>();
    private final int SECURITY_DISTANCE = 30;


    @Override
    public MOVE getMove(Game game, long timeDue) {
        updateGameInfo(game);

        MOVE move;
        if (pacmanPowerTime > 0) {
            move = getMoveToPursueGhosts(game);
        } else if (game.getActivePowerPillsIndices().length > 0) {
            move = getMoveGoToPowerPill(game);
        } else {
            move = getMoveToEatNormalPills(game);
        }

        return move;
    }

    private void updateGameInfo(Game game) {
        getGhostsInfo(game);
        pacmanNode = game.getPacmanCurrentNodeIndex();
        pacmanNextMove = game.getPacmanLastMoveMade();
        powerPillNodes.clear();

        for (int powerPillIndex : game.getActivePowerPillsIndices()) {
            int x = game.getNodeXCood(powerPillIndex);
            int y = game.getNodeYCood(powerPillIndex);
            powerPillNodes.put(powerPillIndex, new int[]{x, y});
        }

        pacmanPowerTime = edibleGhosts.isEmpty() ? 0 : game.getGhostEdibleTime(edibleGhosts.get(0));
    }

    private void getGhostsInfo(Game game) {
        edibleGhosts.clear();
        inedibleGhosts.clear();
        edibleGhostNodes.clear();

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
                edibleGhostNodes.put(ghost, game.getGhostCurrentNodeIndex(ghost));
            } else if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                inedibleGhosts.add(ghost);
            }
        }
    }

    private boolean areAllGhostsOutOfJail(Game game) {
        return Arrays.stream(GHOST.values()).allMatch(ghost -> game.getGhostLairTime(ghost) == 0);
    }

    private MOVE getMoveGoToPowerPill(Game game) {
        if (areAllGhostsOutOfJail(game)) {
            int[] powerPills = game.getActivePowerPillsIndices();

            if (powerPills.length > 0) {
                double closestDistance = Double.MAX_VALUE;
                int closestPowerPill = -1;

                for (int powerPillIndex : powerPills) {
                    double distance = game.getDistance(pacmanNode, powerPillIndex, DM.PATH);

                    if (!areInedibleGhostsNear(game, pacmanNode)) {
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestPowerPill = powerPillIndex;
                        }
                    }
                }

                if (closestPowerPill != -1) {
                    return game.getApproximateNextMoveTowardsTarget(pacmanNode, closestPowerPill, pacmanNextMove, DM.PATH);
                }
            }
        }

        return this.getMoveToEatNormalPills(game);
    }

    private boolean areInedibleGhostsNear(Game game, int pacmanNode) {
        return inedibleGhosts.stream().anyMatch(ghost -> game.getDistance(pacmanNode, game.getGhostCurrentNodeIndex(ghost), DM.PATH) < SECURITY_DISTANCE);
    }

    private MOVE getBestMoveToTarget(Game game, int NodeIndex) {
        MOVE[] possibleMoves = game.getPossibleMoves(pacmanNode, pacmanNextMove);
        MOVE bestMove = null;
        double bestRisk = Double.MAX_VALUE;

        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);

            if (nextNode != -1) {
                double risk = calculateRisk(game, nextNode);

                if (risk < bestRisk) {
                    bestRisk = risk;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private double calculateRisk(Game game, int node) {
        double risk = 0.0;

        for (GHOST ghost : edibleGhosts) {
            int ghostNode = edibleGhostNodes.get(ghost);
            double distanceToGhost = game.getDistance(node, ghostNode, DM.PATH);
            risk += (1.0 / (distanceToGhost + 1)) * 0.5;
        }

        for (GHOST ghost : inedibleGhosts) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            double distanceToGhost = game.getDistance(node, ghostNode, DM.PATH);
            risk += (1.0 / (distanceToGhost + 1)) * 2.0;
        }

        return risk;
    }

    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();

        if (pills.length > 0) {
            int closestPill = getClosestPill(game, pills);
            return this.getBestMoveToTarget(game, closestPill);
        }
        return lastMove;
    }

    private int getClosestPill(Game game, int[] pills) {
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

    private MOVE getMoveToPursueGhosts(Game game) {
        GHOST closestEdibleGhost = getClosestEdibleGhostToPacman(game);

        if (closestEdibleGhost != null) {
            int ghostNode = edibleGhostNodes.get(closestEdibleGhost);
            return game.getNextMoveTowardsTarget(pacmanNode, ghostNode, DM.PATH);
        } else {
            return getBestMoveToTarget(game, 97);
        }
    }

    private GHOST getClosestEdibleGhostToPacman(Game game) {
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;

        for (GHOST ghost : edibleGhosts) {
            int ghostNode = edibleGhostNodes.get(ghost);
            int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

            if (distance < minDistance) {
                minDistance = distance;
                closestGhost = ghost;
            }
        }

        return closestGhost;
    }
}
