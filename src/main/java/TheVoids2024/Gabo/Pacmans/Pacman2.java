package TheVoids2024.Gabo.Pacmans;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class Pacman2 extends PacmanController {
    private static final int SECURITY_DISTANCE = 25;
    private static final int POWER_PILL_DISTANCE_LIMIT = 70;
    private static final int INITIAL_GHOST_DISTANCE_LIMIT = 60;

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        int pacmanPosition = game.getPacmanCurrentNodeIndex();

        if (game.isJunction(pacmanPosition)) {
            List<Constants.GHOST> nearGhosts = getNearbyGhosts(INITIAL_GHOST_DISTANCE_LIMIT, game, pacmanPosition);

            if (nearGhosts.size() > 2 && allGhostsOut(game)) {
                Constants.MOVE powerPillMove = gotoPowerPill(game, nearGhosts);
                if (powerPillMove != null) {
                    return powerPillMove;
                }
            }

            Constants.MOVE escapeMove = runAway(game);
            if (escapeMove != null) {
                return escapeMove;
            } else {
                int ghostToAttack = findEdibleGhost(game);
                return (ghostToAttack != -1)
                        ? game.getApproximateNextMoveTowardsTarget(pacmanPosition, ghostToAttack, game.getPacmanLastMoveMade(), DM.PATH)
                        : getBestPillPath(game);
            }
        } else {
            return null;
        }
    }

    private Constants.MOVE runAway(Game game) {
        int pacmanPosition = game.getPacmanCurrentNodeIndex();

        if (!game.isJunction(pacmanPosition)) {
            return null;
        } else {
            List<Constants.GHOST> nearGhosts = getNearbyGhosts(SECURITY_DISTANCE, game, pacmanPosition);

            if (nearGhosts.size() <= 1) {
                return (nearGhosts.size() == 1)
                        ? game.getApproximateNextMoveAwayFromTarget(pacmanPosition, game.getGhostCurrentNodeIndex(nearGhosts.get(0)), game.getPacmanLastMoveMade(), DM.PATH)
                        : null;
            } else {
                int maxPills = -1;
                int distanceMax = 0;
                int[] possibleIndices = game.getNeighbouringNodes(pacmanPosition, game.getPacmanLastMoveMade());
                int furthestIndex = -1;

                for (int possibleIndex : possibleIndices) {
                    int tempDistance = calculateTotalGhostDistance(game, nearGhosts, possibleIndex);

                    if (!isThereGhost(game, possibleIndex, game.getMoveToMakeToReachDirectNeighbour(pacmanPosition, possibleIndex), nearGhosts) &&
                            (tempDistance > distanceMax || (tempDistance == distanceMax && maxPills < pillsUntilNextJunction(possibleIndex, game.getMoveToMakeToReachDirectNeighbour(pacmanPosition, possibleIndex), game, 0)))) {
                        distanceMax = tempDistance;
                        furthestIndex = possibleIndex;
                        maxPills = pillsUntilNextJunction(possibleIndex, game.getMoveToMakeToReachDirectNeighbour(pacmanPosition, possibleIndex), game, 0);
                    }
                }

                return game.getMoveToMakeToReachDirectNeighbour(pacmanPosition, furthestIndex);
            }
        }
    }

    private List<Constants.GHOST> getNearbyGhosts(int limit, Game game, int pacman) {
        List<Constants.GHOST> nearbyGhosts = new ArrayList<>();

        for (Constants.GHOST ghostType : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghostType);

            if (game.getGhostLairTime(ghostType) <= 0 &&
                    game.getDistance(ghostNode, pacman, game.getGhostLastMoveMade(ghostType), DM.PATH) <= (double) limit &&
                    !game.isGhostEdible(ghostType)) {
                nearbyGhosts.add(ghostType);
            }
        }

        return nearbyGhosts;
    }

    private Constants.MOVE gotoPowerPill(Game game, List<Constants.GHOST> nearGhosts) {
        int pacmanPosition = game.getPacmanCurrentNodeIndex();
        int[] powerPills = game.getActivePowerPillsIndices();

        if (powerPills.length > 0) {
            int nearestPowerPill = -1;
            double distance = Double.MAX_VALUE;

            for (int powerPillIndex : powerPills) {
                double temp = game.getDistance(pacmanPosition, powerPillIndex, game.getPacmanLastMoveMade(), DM.PATH);

                if (temp < distance) {
                    distance = temp;
                    nearestPowerPill = powerPillIndex;
                }
            }

            if (distance < (double) POWER_PILL_DISTANCE_LIMIT) {
                Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(pacmanPosition, nearestPowerPill, game.getPacmanLastMoveMade(), DM.PATH);
                int nextIndex = game.getNeighbour(pacmanPosition, move);

                for (Constants.GHOST ghostType : nearGhosts) {
                    if (!(game.getDistance(pacmanPosition, nearestPowerPill, game.getPacmanLastMoveMade(), DM.PATH) > game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nearestPowerPill, game.getGhostLastMoveMade(ghostType), DM.PATH))
                            || !(game.getDistance(game.getGhostCurrentNodeIndex(ghostType), pacmanPosition, game.getGhostLastMoveMade(ghostType), DM.PATH) > game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nextIndex, game.getGhostLastMoveMade(ghostType), DM.PATH))) {
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

    private boolean isThereGhost(Game game, int index, Constants.MOVE movement, List<Constants.GHOST> nearGhosts) {
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
                if (game.getGhostCurrentNodeIndex(ghostType) == nextIndex && game.getDistance(game.getGhostCurrentNodeIndex(ghostType), index, game.getGhostLastMoveMade(ghostType), DM.PATH) <= game.getDistance(index, nextIndex, game.getPacmanLastMoveMade(), DM.PATH)) {
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

    private int findEdibleGhost(Game game) {
        int msPacmanNode = game.getPacmanCurrentNodeIndex();
        int closestGhost = -1;
        double closestTime = Double.MAX_VALUE;

        for (Constants.GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(msPacmanNode, game.getGhostCurrentNodeIndex(ghost), game.getPacmanLastMoveMade());
                double timeToReach = (double) distance * 3.0 / 2.0;

                if (!(timeToReach > (double) game.getGhostEdibleTime(ghost)) && timeToReach < closestTime) {
                    closestGhost = game.getGhostCurrentNodeIndex(ghost);
                    closestTime = timeToReach;
                }
            }
        }

        return closestGhost;
    }

    private Constants.MOVE getBestPillPath(Game game) {
        int pacmanPosition = game.getPacmanCurrentNodeIndex();

        if (!game.isJunction(pacmanPosition)) {
            return null;
        } else {
            int[] nodes = game.getNeighbouringNodes(pacmanPosition, game.getPacmanLastMoveMade());
            Map<Constants.MOVE, Integer> results = new HashMap<>();
            List<Constants.MOVE> movesDone = new ArrayList<>();

            for (int node : nodes) {
                Constants.MOVE dir = game.getNextMoveTowardsTarget(pacmanPosition, node, DM.PATH);
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
                Integer closestPill = getNearestPillIndex(game);
                finalMove = game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), closestPill, game.getPacmanLastMoveMade(), DM.PATH);
            }

            return finalMove;
        }
    }

    private Integer getNearestPillIndex(Game game) {
        Integer nearestPill = null;
        double closestDistance = -1.0;

        for (int pillIndex : game.getActivePillsIndices()) {
            double distance = (double) game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pillIndex, game.getPacmanLastMoveMade());
            if (nearestPill == null || distance < closestDistance) {
                nearestPill = pillIndex;
                closestDistance = distance;
            }
        }

        return nearestPill;
    }

    private int pillsUntilNextJunction(int nodeIndex, Constants.MOVE direction, Game game, int result) {
        if (game.isJunction(nodeIndex)) {
            return result;
        } else {
            int res = result;
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

    private int calculateTotalGhostDistance(Game game, List<Constants.GHOST> ghosts, int targetNode) {
        int totalDistance = 0;
        for (Constants.GHOST ghostType : ghosts) {
            totalDistance += game.getDistance(game.getGhostCurrentNodeIndex(ghostType), targetNode, game.getGhostLastMoveMade(ghostType), DM.PATH);
        }
        return totalDistance;
    }

    private int getIndexOfNextGhostJunction(Game game, int ghost, Constants.GHOST ghostType) {
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
}
