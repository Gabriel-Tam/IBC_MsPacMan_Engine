package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pacman1 extends PacmanController {
    int pacmanNode;
    MOVE pacmanMove;


    @Override
    public MOVE getMove(Game game, long timeDue) {
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
    
        // Primero verifica si es necesario huir de los fantasmas
        MOVE moveAway = runAway(game);
        if (moveAway != null) {
            return moveAway;
        }
    
        // Luego verifica si hay fantasmas comestibles cerca para perseguir
        int ghostToAttack = isPossibleToEat(game);
        if (ghostToAttack != -1) {
            return game.getApproximateNextMoveTowardsTarget(pacmanNode, ghostToAttack, pacmanMove, DM.PATH);
        }
        
        // Intenta ir hacia una píldora de poder si hay muchos fantasmas cerca
        if (getNearbyGhosts(60, game, pacmanNode).size() > 2 && allOut(game)) {
            MOVE moveToPowerPill = gotoPowerPill(game);
            if (moveToPowerPill != null) {
                return moveToPowerPill;
            }
        }
        
        // Busca la pastilla normal más cercana con mejor camino
        MOVE betterPillPath = getBetterPillPath(game);
        if (betterPillPath != null) {
            return betterPillPath;
        }
        
        // Si no hay una dirección clara, devuelve NEUTRAL
        return MOVE.NEUTRAL;
    }
    
    

    List<Constants.GHOST> getNearbyGhosts(int limit, Game game, int pacman) {
        List<Constants.GHOST> nearGhosts = new ArrayList<>();
        for (Constants.GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghostMove = game.getGhostLastMoveMade(ghost);
            if (game.getGhostLairTime(ghost) <= 0
                    && game.getDistance(ghostNode, pacman, ghostMove, DM.PATH) <= limit
                    && !game.isGhostEdible(ghost)) {
                nearGhosts.add(ghost);
            }
        }
        return nearGhosts;
    }

    private Constants.MOVE getBetterPillPath(Game game) {
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
        if (!game.isJunction(pacmanNode)) {
            return MOVE.NEUTRAL;
        } else {
            int[] neighborNodes = game.getNeighbouringNodes(pacmanNode, pacmanMove);
            Map<Constants.MOVE, Integer> results = new HashMap<>();

            for (int neiNode : neighborNodes) {
                Constants.MOVE direction = game.getNextMoveTowardsTarget(pacmanNode, neiNode, DM.PATH);
                results.put(direction, pillsUntilNextJunction(neiNode, direction, game, 0));
            }
            Constants.MOVE finalMove = null;
            int bestScore = 0;
            for (Map.Entry<Constants.MOVE, Integer> entry : results.entrySet()) {
                Constants.MOVE move = entry.getKey();
                int score = entry.getValue();

                if (score > bestScore) {
                    bestScore = score;
                    finalMove = move;
                }
            }
            if (bestScore == 0) {
                int nearestPill = getNearestPillIndex(game);
                finalMove = game.getApproximateNextMoveTowardsTarget(pacmanNode, nearestPill, pacmanMove, DM.PATH);
            }
            return finalMove;
        }
    }

    private static final int POWERPILL_SCORE = 5;

    private int pillsUntilNextJunction(int nodeIndex, Constants.MOVE direction, Game game, int result) {
        int[] pills = game.getActivePillsIndices();
        int[] powerPills = game.getActivePowerPillsIndices();
        boolean found = false;
        int index = 0;

        if (game.isJunction(nodeIndex)) {
            return result;
        } else {
            if (!found && index < pills.length && pills[index] == nodeIndex) {
                found = true;
                result += 1;
            }
            if (!found && index < powerPills.length && powerPills[index] == nodeIndex) {
                found = true;
                result += POWERPILL_SCORE;
            }

            int[] nodes = game.getNeighbouringNodes(nodeIndex, direction);
            if (nodes.length > 0) {
                Constants.MOVE newDirection = game.getMoveToMakeToReachDirectNeighbour(nodeIndex, nodes[0]);
                return pillsUntilNextJunction(nodes[0], newDirection, game, result);
            } else {
                return result;
            }
        }
    }

    private Integer getNearestPillIndex(Game game) {
        Integer nearestPill = null;
        double closestDistance = -1.0;
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
        for (int pillIndex : game.getActivePillsIndices()) {
            double dist = game.getShortestPathDistance(pacmanNode, pillIndex, pacmanMove);
            if (nearestPill == null || dist < closestDistance) {
                nearestPill = pillIndex;
                closestDistance = dist;
            }
        }
        return nearestPill;
    }

    public int isPossibleToEat(Game game) {
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
        int closestGhostNode = -1;
        double closestTime = Double.MAX_VALUE;

        for (Constants.GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(pacmanNode, ghostNode, pacmanMove);

                // Calcular el tiempo necesario para alcanzar al fantasma
                double timeToGo = distance * 3.0 / 2.0;

                // Verificar si el tiempo es suficiente para alcanzar al fantasma
                if (timeToGo <= game.getGhostEdibleTime(ghost) && timeToGo < closestTime) {
                    closestGhostNode = ghostNode;
                    closestTime = timeToGo;
                }
            }
        }
        return closestGhostNode;
    }

    private Constants.MOVE runAway(Game game) {
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
        int SECURITY_DISTANCE = 25;

        if (!game.isJunction(pacmanNode)) {
            return MOVE.NEUTRAL;
        }

        List<Constants.GHOST> nearGhosts = getNearbyGhosts(SECURITY_DISTANCE, game, pacmanNode);

        if (!nearGhosts.isEmpty()) {
            int numNearGhosts = nearGhosts.size();
            if (numNearGhosts == 1) {
                return game.getApproximateNextMoveAwayFromTarget(pacmanNode,
                        game.getGhostCurrentNodeIndex(nearGhosts.get(0)), pacmanMove, DM.PATH);
            } else if (numNearGhosts > 1) {
                int furthestIndex = findFurthestNode(game, pacmanNode, nearGhosts);
                return game.getMoveToMakeToReachDirectNeighbour(pacmanNode, furthestIndex);
            }
        }

        return MOVE.NEUTRAL; // Manejo del caso cuando nearGhosts está vacía
    }

    private int findFurthestNode(Game game, int pacmanNode, List<Constants.GHOST> nearGhosts) {
        this.pacmanMove = game.getPacmanLastMoveMade();
        int distance_max = 0;
        int furthestIndex = -1;
        int maxPills = -1;

        int[] possibleIndices = game.getNeighbouringNodes(pacmanNode, pacmanMove);

        for (int index : possibleIndices) {
            MOVE moveToIndex = game.getMoveToMakeToReachDirectNeighbour(pacmanNode, index);
            int tempDistance = totalGhostDistance(game, index, nearGhosts, moveToIndex);

            if (!isThereGhost(game, index, moveToIndex, nearGhosts)
                    && (tempDistance > distance_max || (tempDistance == distance_max
                            && maxPills < pillsUntilNextJunction(index, moveToIndex, game, 0)))) {
                distance_max = tempDistance;
                furthestIndex = index;
                maxPills = pillsUntilNextJunction(index, moveToIndex, game, 0);
            }
        }

        return furthestIndex;
    }

    boolean isThereGhost(Game game, int index, Constants.MOVE move, List<Constants.GHOST> nearGhosts) {
        while (true) {
            int[] neighbors = game.getNeighbouringNodes(index, move);

            if (neighbors.length != 1) {
                break;
            }

            int nextIndex = neighbors[0];
            Constants.MOVE nextMove = game.getMoveToMakeToReachDirectNeighbour(index, nextIndex);

            if (neighbors.length == 1 && game.getMoveToMakeToReachDirectNeighbour(index, neighbors[0]) != move) {
                move = game.getMoveToMakeToReachDirectNeighbour(index, neighbors[0]);
            }

            for (Constants.GHOST ghost : nearGhosts) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                MOVE ghostMove = game.getGhostLastMoveMade(ghost);
                double ghost_Next = game.getDistance(ghostNode, nextIndex, ghostMove, DM.PATH);
                double pacman_Next = game.getDistance(index, nextIndex, move, DM.PATH);

                if (ghostNode == nextIndex && ghost_Next <= pacman_Next) {
                    return true;
                }
            }

            for (Constants.GHOST ghost : GHOST.values()) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                MOVE ghostMove = game.getGhostLastMoveMade(ghost);
                if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                    int ghostNextJunction = nextGhostCrossing(game, ghost);

                    if (ghostNextJunction == nextIndex) {
                        double ghost_Junction = game.getDistance(ghostNode, ghostNextJunction, ghostMove, DM.PATH);
                        double pacman_Junction = game.getDistance(index, ghostNextJunction, nextMove, DM.PATH);

                        if (ghost_Junction < pacman_Junction) {
                            return true;
                        }
                    }
                }
            }
            index = nextIndex;
            move = nextMove;
        }
        return false;
    }

    public int nextGhostCrossing(Game game, Constants.GHOST ghost) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        MOVE ghostMove = game.getGhostLastMoveMade(ghost);
        if (!game.isJunction(ghostNode)) {
            Constants.MOVE ghost_movement = ghostMove;
            int[] indexes = game.getNeighbouringNodes(ghostNode, ghost_movement);

            if (game.getMoveToMakeToReachDirectNeighbour(ghostNode, indexes[0]) != ghost_movement
                    && indexes.length == 1) {
                ghost_movement = game.getMoveToMakeToReachDirectNeighbour(ghostNode, indexes[0]);
            }

            while (indexes.length == 1) {
                ghostNode = indexes[0];
                indexes = game.getNeighbouringNodes(ghostNode, ghost_movement);

                if (game.getMoveToMakeToReachDirectNeighbour(ghostNode, indexes[0]) != ghost_movement
                        && indexes.length == 1) {
                    ghost_movement = game.getMoveToMakeToReachDirectNeighbour(ghostNode, indexes[0]);
                }
            }
        }

        return ghostNode;
    }

    private int totalGhostDistance(Game game, int targetNode, List<Constants.GHOST> ghosts, MOVE moveToIndex) {
        int totalDistance = 0;
        for (Constants.GHOST ghost : ghosts) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            totalDistance += game.getDistance(ghostNode, targetNode, moveToIndex, DM.PATH);
        }
        return totalDistance;
    }

    private Constants.MOVE gotoPowerPill(Game game) {
        this.pacmanNode = game.getPacmanCurrentNodeIndex();
        this.pacmanMove = game.getPacmanLastMoveMade();
        int nearestPowerPill = game.getClosestNodeIndexFromNodeIndex(pacmanNode, game.getActivePowerPillsIndices(), DM.PATH);
    
        if (nearestPowerPill != -1) {
            double distance = game.getDistance(pacmanNode, nearestPowerPill, pacmanMove, DM.PATH);
    
            if (distance < 70) { // Puedes ajustar este valor según sea necesario
                return game.getApproximateNextMoveTowardsTarget(pacmanNode, nearestPowerPill, pacmanMove, DM.PATH);
            }
        }
        return null;
    }
    

    private boolean allOut(Game game) {
        for (Constants.GHOST ghost : GHOST.values()) {
            if (game.getGhostLairTime(ghost) > 0) {
                return false;
            }
        }
        return true;
    }

}
