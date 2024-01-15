package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.ArrayList;
import java.util.List; // Asegúrate de importar las clases necesarias

public final class Pacman4 extends PacmanController {
    private int pacmanPowerTime = 0; // Variable para rastrear el poder de Pac-Man

    @Override // ----------------------------------------------
    public MOVE getMove(Game game, long timeDue) {
        List<GHOST> edibleGhosts = getEdibleGhosts(game);

        if (edibleGhosts.size() > 0) {
            pacmanPowerTime = game.getGhostEdibleTime(edibleGhosts.get(0));

            /* 
            if (getClosestEdibleGhost(game) != null) {
                System.out.println("Edible Ghosts Time: "+pacmanPowerTime);
                for (GHOST ghost : edibleGhosts) {
                    System.out.print(ghost);
                    if (ghost == getClosestEdibleGhost(game)) {
                        System.out.print(" *****");
                    }
                    System.out.println();
                }
            }
            */

        } else {
            pacmanPowerTime = 0;
        }

        MOVE move;
        if (pacmanPowerTime > 0) {
            move = getMoveToPursueGhosts(game);
        } else if (game.getActivePowerPillsIndices().length > 0) {
            move = getMoveToGoToPowerPill(game);
        } else {
            move = getMoveToEatNormalPills(game);
        }

        return move;
    }// ---------------------------------------------

    // Obtener los fantasmas Comestibles
    private List<GHOST> getEdibleGhosts(Game game) {
        List<GHOST> edibleGhosts = new ArrayList<>();

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
            }
        }

        return edibleGhosts;
    }

    // Obtener al fantasma comestible más cercano
    private GHOST getClosestEdibleGhost(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;

        for (GHOST ghost : getEdibleGhosts(game)) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

            if (distance < minDistance) {
                minDistance = distance;
                closestGhost = ghost;
            }
        }
        return closestGhost;
    }

//Obtiene el fantasma NO comestible más cercano
    private GHOST getClosestInedibleGhost(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;

        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost)) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestGhost = ghost;
                }
            }
        }
        return closestGhost;
    }
// Obtiene el fantasma más cercano (no importa si es comible o no)
    private GHOST getClosestGhostToPacman(Game game, GHOST ghost) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int distance = game.getShortestPathDistance(pacmanNode, ghostNode);
        if (distance < minDistance) {
            minDistance = distance;
            closestGhost = ghost;
        }
        return closestGhost;
    }

    private int getClosestPowerPillNode(Game game, int[] powerPillsNode) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();       
            return game.getClosestNodeIndexFromNodeIndex(pacmanNode, powerPillsNode, DM.PATH);
    }

    private boolean areAllGhostsOutOfJail(Game game) {
        for (GHOST ghost : GHOST.values()) {
            int lairTime = game.getGhostLairTime(ghost);
            if (lairTime > 0) {
                return false;
            }
        }
        return true;
    }

    private MOVE getOptimizedMove(Game game, int targetNodeIndex, boolean avoidGhosts) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE[] possibleMoves = game.getPossibleMoves(pacmanNode, game.getPacmanLastMoveMade());
    
        MOVE bestMove = null;
        double bestScore = avoidGhosts ? -Double.MAX_VALUE : Double.MAX_VALUE;
        final int someSafeDistance = 45;
        final double ghostPenalty = 1000;
    
        GHOST closestInedibleGhost = this.getClosestInedibleGhost(game); // Asumiendo que este método ya existe
    
        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);
            if (nextNode != -1) {
                double distanceToTarget = game.getDistance(nextNode, targetNodeIndex, DM.PATH);
                double score = avoidGhosts ? -distanceToTarget : distanceToTarget;
    
                if (avoidGhosts && closestInedibleGhost != null) {
                    int ghostNode = game.getGhostCurrentNodeIndex(closestInedibleGhost);
                    double distanceToGhost = game.getDistance(nextNode, ghostNode, DM.PATH);
    
                    if (distanceToGhost < someSafeDistance) {
                        score -= ghostPenalty;
                    }
                }
    
                if ((avoidGhosts && score > bestScore) || (!avoidGhosts && score < bestScore)) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
    
        return bestMove != null ? bestMove : game.getNextMoveAwayFromTarget(pacmanNode, game.getGhostCurrentNodeIndex(closestInedibleGhost), DM.PATH);
    }
    
    
    

    // Obtener el movimiento para perseguir fantasmas
    private MOVE getMoveToPursueGhosts(Game game) {
        GHOST closestEdibleGhost = this.getClosestEdibleGhost(game);
    
        if (closestEdibleGhost != null) {
            int nearestEdibleGhostNode = game.getGhostCurrentNodeIndex(closestEdibleGhost);
            // Persigue al fantasma comestible más cercano, pero evita los fantasmas no comestibles
            return this.getOptimizedMove(game, nearestEdibleGhostNode, true);
        }
        return this.getMoveToEatNormalPills(game);
    }
    
    private MOVE getMoveToGoToPowerPill(Game game) {
        int[] powerPillNodes = game.getActivePowerPillsIndices();
        if (this.areAllGhostsOutOfJail(game) && powerPillNodes.length > 0) {
            int closestPowerPillNode = this.getClosestPowerPillNode(game, powerPillNodes);
            // Moverse hacia la píldora de poder más cercana, evitando fantasmas
            return this.getOptimizedMove(game, closestPowerPillNode, true);
        }
        return this.getMoveToEatNormalPills(game);
    }
    
    

    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();
        
        if (pills.length > 0) {
            int randomIndex = (int) (Math.random() * pills.length);
            int targetNode = pills[randomIndex];
            // Moverse hacia una píldora normal seleccionada al azar, evitando fantasmas
            return this.getOptimizedMove(game, targetNode, true);
        }
        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }
    
    // Otros
}