package TheVoids2024.Gabo.Pacmans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

public class Pacman7 extends PacmanController {
    private int SECURITY_DISTANCE = 25;
    private int powerPillDistanceLimit = 70;
    private int initialGhostDistanceLimit = 60;

    public Pacman7() {
    }

    public Constants.MOVE getMove(Game game, long timeDue) {
        if (game.isJunction(game.getPacmanCurrentNodeIndex())) {
            List<Constants.GHOST> ghosts = getCloseGhosts(this.initialGhostDistanceLimit, game, game.getPacmanCurrentNodeIndex());
            Constants.MOVE move;
            if (ghosts.size() > 2 && allGhostsOut(game)) {
                move = goToPowerPill(game, ghosts);
                if (move != null) {
                    return move;
                }
            }

            move = runAwayFromGhosts(game);
            if (move != null) {
                return move;
            } else {
                int ghostToAttack = isPossibleToEatGhost(game);
                return ghostToAttack != -1 ? game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), ghostToAttack, game.getPacmanLastMoveMade(), DM.PATH) : getBetterPillPath(game);
            }
        } else {
            return null;
        }
    }

    List<Constants.GHOST> getCloseGhosts(int limit, Game game, int pacman) {
        List<Constants.GHOST> nearGhosts = new ArrayList<>();
        for (Constants.GHOST ghostType : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghostType);
            if (game.getGhostLairTime(ghostType) <= 0 && game.getDistance(ghostNode, pacman, game.getGhostLastMoveMade(ghostType), DM.PATH) <= (double) limit && !game.isGhostEdible(ghostType)) {
                nearGhosts.add(ghostType);
            }
        }
        return nearGhosts;
    }

    private Constants.MOVE goToPowerPill(Game game, List<Constants.GHOST> nearGhosts) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int[] powerPills = game.getActivePowerPillsIndices();
        if (powerPills.length > 0) {
            int nearestPowerPill = getNearestPowerPill(game, pacman, powerPills);
            if (game.getDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), DM.PATH) < (double) powerPillDistanceLimit) {
                Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), DM.PATH);
                int nextIndex = game.getNeighbour(pacman, move);
                for (Constants.GHOST ghostType : nearGhosts) {
                    if (!(game.getDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade(), DM.PATH) > game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nearestPowerPill, game.getGhostLastMoveMade(ghostType), DM.PATH))
                            || !(game.getDistance(game.getGhostCurrentNodeIndex(ghostType), pacman, game.getGhostLastMoveMade(ghostType), DM.PATH) > game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nextIndex, game.getGhostLastMoveMade(ghostType), DM.PATH))) {
                        return null;
                    }
                }
                return move;
            }
        }
        return null;
    }

    private boolean allGhostsOut(Game game) {
        for (Constants.GHOST ghostType : GHOST.values()) {
            if (game.getGhostLairTime(ghostType) > 0) {
                return false;
            }
        }
        return true;
    }

    private Constants.MOVE runAwayFromGhosts(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();
        int distance = SECURITY_DISTANCE;
        if (!game.isJunction(pacman)) {
            return null;
        } else {
            List<Constants.GHOST> nearGhosts = getCloseGhosts(distance, game, pacman);
            if (nearGhosts.size() <= 1) {
                return nearGhosts.size() == 1 ? game.getApproximateNextMoveAwayFromTarget(pacman, game.getGhostCurrentNodeIndex(nearGhosts.get(0)), game.getPacmanLastMoveMade(), DM.PATH) : null;
            } else {
                int maxPills = -1;
                int distanceMax = 0;
                int[] possibleIndices = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
                int furthestIndex = -1;

                for (int index : possibleIndices) {
                    int tempDistance = 0;
                    for (Constants.GHOST ghostType : nearGhosts) {
                        tempDistance += game.getDistance(game.getGhostCurrentNodeIndex(ghostType), index, game.getGhostLastMoveMade(ghostType), DM.PATH);
                    }
                    if (!isGhostPresent(game, index, game.getMoveToMakeToReachDirectNeighbour(pacman, index), nearGhosts) && (tempDistance > distanceMax || (tempDistance == distanceMax && maxPills < pillsUntilNextJunction(index, game.getMoveToMakeToReachDirectNeighbour(pacman, index), game, 0)))) {
                        distanceMax = tempDistance;
                        furthestIndex = index;
                        maxPills = pillsUntilNextJunction(index, game.getMoveToMakeToReachDirectNeighbour(pacman, index), game, 0);
                    }
                }

                return game.getMoveToMakeToReachDirectNeighbour(pacman, furthestIndex);
            }
        }
    }

    boolean isGhostPresent(Game game, int index, Constants.MOVE movement, List<Constants.GHOST> nearGhosts) {
        int nextIndex = index;
        int[] indexes = game.getNeighbouringNodes(index, movement);
        if (game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != movement && indexes.length == 1) {
            movement = game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
        }

        while (indexes.length == 1) {
            nextIndex = indexes[0];
            indexes = game.getNeighbouringNodes(nextIndex, movement);
            if (game.getMoveToMakeToReachDirectNeighbour(nextIndex, indexes[0]) != movement && indexes.length == 1) {
                movement = game.getMoveToMakeToReachDirectNeighbour(nextIndex, indexes[0]);
            }

            for (Constants.GHOST ghostType : nearGhosts) {
                if (game.getGhostCurrentNodeIndex(ghostType) == nextIndex && game.getDistance(game.getGhostCurrentNodeIndex(ghostType), index, game.getGhostLastMoveMade(ghostType), DM.PATH) <= game.getDistance(index, nextIndex, movement, DM.PATH)) {
                    return true;
                }
            }
        }

        for (Constants.GHOST ghostType : GHOST.values()) {
            if (!game.isGhostEdible(ghostType) && game.getGhostLairTime(ghostType) == 0) {
                int ghostNextJunction = getIndexOfNextGhostJunction(game, game.getGhostCurrentNodeIndex(ghostType), ghostType);
                if (ghostNextJunction == nextIndex && game.getDistance(game.getGhostCurrentNodeIndex(ghostType), ghostNextJunction, game.getGhostLastMoveMade(ghostType), DM.PATH) < game.getDistance(index, ghostNextJunction, movement, DM.PATH)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int isPossibleToEatGhost(Game game) {
        int msPacmanNode = game.getPacmanCurrentNodeIndex();
        int closestGhost = -1;
        double closestT = Double.MAX_VALUE;
        for (Constants.GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(msPacmanNode, game.getGhostCurrentNodeIndex(ghost), game.getPacmanLastMoveMade());
                double sB = (double) distance;
                double t = sB * 3.0 / 2.0;
                if (!(t > (double) game.getGhostEdibleTime(ghost)) && t < closestT) {
                    closestGhost = game.getGhostCurrentNodeIndex(ghost);
                    closestT = t;
                }
            }
        }
        return closestGhost;
    }

    private Constants.MOVE getBetterPillPath(Game game) {
        List<Constants.MOVE> movesDone = new ArrayList<>();
        int pacman = game.getPacmanCurrentNodeIndex();
        if (!game.isJunction(pacman)) {
            return null;
        } else {
            int[] nodes = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
            Map<Constants.MOVE, Integer> results = new HashMap<>();
            int closestPill = nodes.length;

            for (int node : nodes) {
                Constants.MOVE dir = game.getNextMoveTowardsTarget(pacman, node, DM.PATH);
                movesDone.add(dir);
                results.put(dir, pillsUntilNextJunction(node, dir, game, 0));
            }

            Constants.MOVE finalMove = null;
            int bestScore = 0;

            for (Constants.MOVE m : movesDone) {
                if (results.get(m) != null && (Integer) results.get(m) > bestScore) {
                    bestScore = (Integer) results.get(m);
                    finalMove = m;
                }
            }

            if (bestScore == 0) {
                closestPill = getNearestPillIndex(game);
                finalMove = game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), closestPill, game.getPacmanLastMoveMade(), DM.PATH);
            }

            return finalMove;
        }
    }

    private Integer getNearestPillIndex(Game game) {
        Integer nearestPill = null;
        double closestDistance = -1.0;
        for (int pillIndex : game.getActivePillsIndices()) {
            double dist = (double) game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pillIndex, game.getPacmanLastMoveMade());
            if (nearestPill == null || dist < closestDistance) {
                nearestPill = pillIndex;
                closestDistance = dist;
            }
        }
        return nearestPill;
    }

    private int pillsUntilNextJunction(int nodeIndex, Constants.MOVE direction, Game game, int result) {
        int res = result;
        if (game.isJunction(nodeIndex)) {
            return result;
        } else {
            int[] activePills = game.getActivePillsIndices();
            int[] powerPills = game.getActivePowerPillsIndices();
            boolean found = false;
            int idx = 0;

            while (!found && idx < activePills.length) {
                if (activePills[idx] == nodeIndex) {
                    found = true;
                } else {
                    ++idx;
                }
            }

            if (found) {
                res = result + 1;
            }

            idx = 0;

            while (!found && idx < powerPills.length) {
                if (powerPills[idx] == nodeIndex) {
                    found = true;
                } else {
                    ++idx;
                }
            }

            if (found) {
                res += 5;
            }

            int[] nodes = game.getNeighbouringNodes(nodeIndex, direction);
            Constants.MOVE newDir = game.getMoveToMakeToReachDirectNeighbour(nodeIndex, nodes[0]);
            return pillsUntilNextJunction(nodes[0], newDir, game, res);
        }
    }

    public int getIndexOfNextGhostJunction(Game game, int ghost, Constants.GHOST ghostType) {
        int index = ghost;
        if (!game.isJunction(ghost)) {
            Constants.MOVE ghostMovement = game.getGhostLastMoveMade(ghostType);
            int[] indexes = game.getNeighbouringNodes(ghost, ghostMovement);
            if (game.getMoveToMakeToReachDirectNeighbour(ghost, indexes[0]) != ghostMovement && indexes.length == 1) {
                ghostMovement = game.getMoveToMakeToReachDirectNeighbour(ghost, indexes[0]);
            }

            while (indexes.length == 1) {
                index = indexes[0];
                indexes = game.getNeighbouringNodes(index, ghostMovement);
                if (game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != ghostMovement && indexes.length == 1) {
                    ghostMovement = game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
                }
            }
        }

        return index;
    }

    private int getNearestPowerPill(Game game, int pacman, int[] powerPills) {
        int nearestPowerPill = -1;
        double distance = Double.MAX_VALUE;

        for (int i : powerPills) {
            double temp = game.getDistance(pacman, i, game.getPacmanLastMoveMade(), DM.PATH);
            if (temp < distance) {
                distance = temp;
                nearestPowerPill = i;
            }
        }

        return nearestPowerPill;
    }
}
