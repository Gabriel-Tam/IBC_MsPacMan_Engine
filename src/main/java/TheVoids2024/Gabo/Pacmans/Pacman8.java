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

public class Pacman8 extends PacmanController {

    private static final int POWER_PILL_DISTANCE_LIMIT = 78;
    private static final int INITIAL_GHOST_DISTANCE_LIMIT = 60;

    public Pacman8() {
    }

    public Constants.MOVE getMove(Game game, long timeDue) {
        if (!game.isJunction(game.getPacmanCurrentNodeIndex())) {
            return null;
        }

        List<Constants.GHOST> nearGhosts = getListOfAllGhosts(game);

        if (nearGhosts.size() > 2 && allGhostsOut(game)) {
            Constants.MOVE move = gotoPowerPill(game, nearGhosts);
            if (move != null) {
                return move;
            }
        }

        Constants.MOVE move = runAway(game, nearGhosts);
        if (move != null) {
            return move;
        } else {
            int ghostToAttack = isPossibleToEat(game);
            return (ghostToAttack != -1) ?
                    game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), ghostToAttack,
                            game.getPacmanLastMoveMade(), DM.PATH) :
                    getBetterPillPath(game);
        }
    }

    private List<Constants.GHOST> getListOfAllGhosts(Game game) {
        List<Constants.GHOST> nearGhosts = new ArrayList<>();
        for (Constants.GHOST ghostType : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghostType);
            if (game.getGhostLairTime(ghostType) <= 0 &&
                    game.getDistance(ghostNode, game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghostType),
                            DM.PATH) <= INITIAL_GHOST_DISTANCE_LIMIT &&
                    !game.isGhostEdible(ghostType)) {
                nearGhosts.add(ghostType);
            }
        }
        return nearGhosts;
    }

    private Constants.MOVE gotoPowerPill(Game game, List<Constants.GHOST> nearGhosts) {
        int pacman = game.getPacmanCurrentNodeIndex();
        
        // Get active power pills indices
        int[] powerPills = game.getActivePowerPillsIndices();
    
        if (powerPills.length > 0) {
            int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(pacman, powerPills, DM.PATH);
    
            if (nearestPowerPill != -1) {
                double distance = game.getShortestPathDistance(pacman, nearestPowerPill, game.getPacmanLastMoveMade());
    
                if (distance < POWER_PILL_DISTANCE_LIMIT) {
                    Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(pacman, nearestPowerPill,
                            game.getPacmanLastMoveMade(), DM.PATH);
                    int nextIndex = game.getNeighbour(pacman, move);
    
                    for (Constants.GHOST ghostType : nearGhosts) {
                        double distanceToPowerPill = game.getDistance(pacman, nearestPowerPill,
                                game.getPacmanLastMoveMade(), DM.PATH);
                        double ghostToPowerPillDistance = game.getDistance(game.getGhostCurrentNodeIndex(ghostType),
                                nearestPowerPill, game.getGhostLastMoveMade(ghostType), DM.PATH);
                        double ghostToPacmanDistance = game.getDistance(game.getGhostCurrentNodeIndex(ghostType), pacman,
                                game.getGhostLastMoveMade(ghostType), DM.PATH);
                        double ghostToNextIndexDistance = game.getDistance(game.getGhostCurrentNodeIndex(ghostType),
                                nextIndex, game.getGhostLastMoveMade(ghostType), DM.PATH);
    
                        if (!(distanceToPowerPill > ghostToPowerPillDistance) &&
                                !(ghostToPacmanDistance > ghostToNextIndexDistance)) {
                            return null;
                        }
                    }
                    return move;
                }
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

    private Constants.MOVE runAway(Game game, List<Constants.GHOST> nearGhosts) {
        int pacman = game.getPacmanCurrentNodeIndex();

        if (!game.isJunction(pacman)) {
            return null;
        }

        if (nearGhosts.size() <= 1) {
            return (nearGhosts.size() == 1) ?
                    game.getApproximateNextMoveAwayFromTarget(pacman, game.getGhostCurrentNodeIndex(nearGhosts.get(0)),
                            game.getPacmanLastMoveMade(), DM.PATH) :
                    null;
        } else {
            int maxPills = -1;
            int furthestIndex = -1;
            Constants.MOVE finalMove = null;

            int[] possibleIndices = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
            for (int possibleIndex : possibleIndices) {
                int tempDistance = 0;

                for (Constants.GHOST ghostType : nearGhosts) {
                    tempDistance += game.getDistance(game.getGhostCurrentNodeIndex(ghostType), possibleIndex,
                            game.getGhostLastMoveMade(ghostType), DM.PATH);
                }

                Constants.MOVE move = game.getMoveToMakeToReachDirectNeighbour(pacman, possibleIndex);
                if (!isThereGhost(game, possibleIndex, move, nearGhosts) &&
                        (tempDistance > maxPills ||
                                (tempDistance == maxPills &&
                                        maxPills < pillsUntilNextJunction(possibleIndex, move, game, 0)))) {
                    maxPills = tempDistance;
                    furthestIndex = possibleIndex;
                    finalMove = move;
                }
            }

            return (finalMove != null) ? finalMove : game.getMoveToMakeToReachDirectNeighbour(pacman, furthestIndex);
        }
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
                double ghostToPowerPillDistance = game.getDistance(game.getGhostCurrentNodeIndex(ghostType), nextIndex,
                        game.getGhostLastMoveMade(ghostType), DM.PATH);
                double ghostToPacmanDistance = game.getDistance(game.getGhostCurrentNodeIndex(ghostType), index,
                        game.getGhostLastMoveMade(ghostType), DM.PATH);
                if (!(ghostToPowerPillDistance > ghostToPacmanDistance)) {
                    return true;
                }
            }
        }

        for (Constants.GHOST ghostType : GHOST.values()) {
            if (!game.isGhostEdible(ghostType) && game.getGhostLairTime(ghostType) == 0) {
                int ghostNextJunction = getIndexOfNextGhostJunction(game, game.getGhostCurrentNodeIndex(ghostType),
                        ghostType);

                if (ghostNextJunction == nextIndex &&
                        game.getDistance(game.getGhostCurrentNodeIndex(ghostType), ghostNextJunction,
                                game.getGhostLastMoveMade(ghostType), DM.PATH) <
                                game.getDistance(index, ghostNextJunction, movement, DM.PATH)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int isPossibleToEat(Game game) {
        int msPacmanNode = game.getPacmanCurrentNodeIndex();
        int closestGhost = -1;
        double closestT = Double.MAX_VALUE;

        for (Constants.GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(msPacmanNode, game.getGhostCurrentNodeIndex(ghost),
                        game.getPacmanLastMoveMade());
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
        int pacman = game.getPacmanCurrentNodeIndex();

        if (!game.isJunction(pacman)) {
            return null;
        } else {
            int[] nodes = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
            Map<Constants.MOVE, Integer> results = new HashMap<>();

            for (int node : nodes) {
                Constants.MOVE dir = game.getNextMoveTowardsTarget(pacman, node, DM.PATH);
                results.put(dir, pillsUntilNextJunction(node, dir, game, 0));
            }

            Constants.MOVE finalMove = null;
            int bestScore = 0;

            for (Map.Entry<Constants.MOVE, Integer> entry : results.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > bestScore) {
                    bestScore = entry.getValue();
                    finalMove = entry.getKey();
                }
            }

            if (bestScore == 0) {
                int closestPill = getNearestPillIndex(game);
                finalMove = game.getApproximateNextMoveTowardsTarget(pacman, closestPill,
                        game.getPacmanLastMoveMade(), DM.PATH);
            }

            return finalMove;
        }
    }

    private Integer getNearestPillIndex(Game game) {
        Integer nearestPill = null;
        double closestDistance = -1.0;

        int[] activePills = game.getActivePillsIndices();

        for (int pillIndex : activePills) {
            double dist = (double) game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pillIndex,
                    game.getPacmanLastMoveMade());

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
}
