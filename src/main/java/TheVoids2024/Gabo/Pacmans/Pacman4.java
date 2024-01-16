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

            if (getClosestEdibleGhost(game) != null) {
                System.out.println("Edible Ghosts Time: " + pacmanPowerTime);
                for (GHOST ghost : edibleGhosts) {
                    System.out.print(ghost);
                    if (ghost == getClosestEdibleGhost(game)) {
                        System.out.print(" *****");
                    }
                    System.out.println();
                }
            }

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

    // Obtiene el fantasma NO comestible más cercano
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

    private GHOST getClosestGhostToPacman(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        GHOST closestGhost = null;
        int minDistance = Integer.MAX_VALUE;

        for (GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

            if (distance < minDistance) {
                minDistance = distance;
                closestGhost = ghost;
            }
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
        final int someSafeDistance = 55;
        final double ghostPenalty = 1000;

        GHOST closestInedibleGhost = this.getClosestInedibleGhost(game);

        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);
            if (nextNode != -1) {
                double distanceToTarget = game.getDistance(nextNode, targetNodeIndex, DM.PATH);
                double score = avoidGhosts ? -distanceToTarget : distanceToTarget;

                if (avoidGhosts && closestInedibleGhost != null) {
                    int ghostNode = game.getGhostCurrentNodeIndex(closestInedibleGhost);
                    double distanceToGhost = game.getDistance(nextNode, ghostNode, DM.PATH);

                    // Verifica si el siguiente nodo es una intersección
                    if (game.isJunction(nextNode)) {
                        // Revisa las intersecciones cercanas para anticipar fantasmas
                        for (int junctionNode : game.getJunctionIndices()) {
                            double futureDistanceToGhost = game.getDistance(junctionNode, ghostNode, DM.PATH);
                            if (futureDistanceToGhost < someSafeDistance) {
                                score -= ghostPenalty;
                            }
                        }
                    }

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

        return bestMove != null ? bestMove
                : game.getNextMoveAwayFromTarget(pacmanNode, game.getGhostCurrentNodeIndex(closestInedibleGhost),
                        DM.PATH);
    }

    private MOVE getMoveToPursueGhosts(Game game) {
        // Obtiene el fantasma más cercano, sin importar si es comestible o no.
        GHOST closestGhost = this.getClosestGhostToPacman(game);

        if (closestGhost != null) {
            int closestGhostNode = game.getGhostCurrentNodeIndex(closestGhost);

            // Determina si debería evitar fantasmas (basado en si están comestibles o no).
            boolean shouldAvoidGhosts = pacmanPowerTime <= 0 && !game.isGhostEdible(closestGhost);

            // Usa getOptimizedMove para decidir el mejor movimiento.
            return this.getOptimizedMove(game, closestGhostNode, shouldAvoidGhosts);
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
            int pacmanNode = game.getPacmanCurrentNodeIndex();
            int closestPillNode = getClosestNode(game, pacmanNode, pills);

            // Moverse hacia la píldora normal más cercana
            return this.getOptimizedMove(game, closestPillNode, true);
        }

        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }

    // Método auxiliar para encontrar el nodo más cercano en un conjunto de nodos
    private int getClosestNode(Game game, int fromNode, int[] nodes) {
        int closestNode = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int node : nodes) {
            int distance = game.getShortestPathDistance(fromNode, node);
            if (distance < minDistance) {
                minDistance = distance;
                closestNode = node;
            }
        }

        return closestNode;
    }

    // Otros
}