package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.ArrayList;
import java.util.List; // Asegúrate de importar las clases necesarias

public final class Pacman2 extends PacmanController {
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
    
    

    // Obtener el mejor movimiento que te aleje del objetivo
    private MOVE getBestMoveAwayFromTarget(Game game, int NodeIndex) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE[] possibleMoves = game.getPossibleMoves(pacmanNode, game.getPacmanLastMoveMade());
        MOVE bestMove = null;
        double bestDistance = -1.0; // Inicializado con un valor mínimo para que el primer movimiento válido se
                                    // convierta en el "mejor".

        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);
            if (nextNode != -1) { // Verifica si el nodo es válido
                double distanceToTarget = game.getDistance(nextNode, NodeIndex, DM.PATH);
                if (distanceToTarget > bestDistance) {
                    bestDistance = distanceToTarget;
                    bestMove = move;
                }
            }
        }

        return bestMove;
    }

    // Obtener el mejor movimiento hacia el objetivo
    private MOVE getBestMoveToTarget(Game game, int NodeIndex) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        MOVE pacmanMove = game.getPacmanLastMoveMade();
        MOVE[] possibleMoves = game.getPossibleMoves(pacmanNode, pacmanMove);
        MOVE bestMove = null;
        double bestDistance = Double.MAX_VALUE;

        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);
            if (nextNode != -1) { // Verifica si el nodo es válido
                double distanceToTarget = game.getDistance(nextNode, NodeIndex, DM.PATH);
                if (distanceToTarget < bestDistance) {
                    bestDistance = distanceToTarget;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    

    // Obtener el movimiento para perseguir fantasmas
    private MOVE getMoveToPursueGhosts(Game game) {
        GHOST closestEdibleGhost = this.getClosestEdibleGhost(game);
        GHOST closestInedibleGhost = this.getClosestInedibleGhost(game);
    
        if (closestEdibleGhost != null && closestInedibleGhost != null) {
            int nearestEdibleGhostNode = game.getGhostCurrentNodeIndex(closestEdibleGhost);
            int nearestInedibleGhostNode = game.getGhostCurrentNodeIndex(closestInedibleGhost);
    
            if (closestEdibleGhost != null) {
                if (this.getClosestGhostToPacman(game, closestEdibleGhost) == closestEdibleGhost) {
                    return this.getBestMoveToTarget(game, nearestEdibleGhostNode);
                } else {
                    return this.getBestMoveAwayFromTarget(game, nearestInedibleGhostNode);
                }
            }
        }
        return this.getMoveToEatNormalPills(game); // Cambia esto a la dirección deseada si no hay otros casos manejados.
    }
    
    
    private MOVE getMoveToGoToPowerPill(Game game) {
        int[] powerPillNodes = game.getActivePowerPillsIndices();
        if (this.areAllGhostsOutOfJail(game)) {
            if (powerPillNodes.length > 0) {
                return this.getBestMoveToTarget(game,this.getClosestPowerPillNode(game, powerPillNodes) );
            }
        }
        return this.getMoveToEatNormalPills(game); // Retorna el último movimiento si no hay píldoras de poder activas o si no todos los fantasmas están fuera de la cárcel.
    }
    

    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();
        
        if (pills.length > 0) {
            int randomIndex = (int) (Math.random() * pills.length); // Generar un índice aleatorio
            int targetNode = pills[randomIndex]; // Seleccionar una píldora aleatoria
            
            return this.getBestMoveToTarget(game, targetNode); // Obtener el mejor movimiento hacia la píldora seleccionada
        }
        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }
    // Otros
}