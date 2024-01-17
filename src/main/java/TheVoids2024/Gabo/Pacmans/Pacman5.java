package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.ArrayList;
import java.util.List;

public final class Pacman5 extends PacmanController {
    private int pacmanPowerTime = 0; // Tiempo restante de poder de Pac-Man
    private int pacmanNode; // Ubicación actual de Pac-Man
    private MOVE pacmanLastMove; // Último movimiento realizado por Pac-Man
    private List<GHOST> edibleGhosts; // Lista de fantasmas comestibles
    private List<GHOST> inedibleGhosts; // Lista de fantasmas no comestibles
    private int[] powerPillsNode; // Ubicaciones de las píldoras de poder restantes
    private static final int SOME_SAFE_DISTANCE = 20;

    public Pacman5() {
        edibleGhosts = new ArrayList<>();
        inedibleGhosts = new ArrayList<>();
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        updateGameInfo(game); // Actualiza la información del juego

        MOVE move;
        if (pacmanPowerTime > 0) {
            move = getMoveToPursueGhosts(game);
        } else if (game.getActivePowerPillsIndices().length > 0) {
            move = getMoveToGoToPowerPill(game);
        } else {
            move = getMoveToEatNormalPills(game);
        }

        return move;
    }

    private void updateGameInfo(Game game) {
        pacmanNode = game.getPacmanCurrentNodeIndex();
        pacmanLastMove = game.getPacmanLastMoveMade();
        getEdibleGhostsInfo(game);
        getInedibleGhostsInfo(game);
        powerPillsNode = game.getActivePowerPillsIndices();

        // Si hay fantasmas comestibles, Pac-Man está bajo el efecto de una Power Pill
        pacmanPowerTime = edibleGhosts.isEmpty() ? 0 : game.getGhostEdibleTime(GHOST.values()[0]);

        // Imprimir la información actualizada
        System.out.println("Pacman Node: " + pacmanNode);
        System.out.println("Pacman Last Move: " + pacmanLastMove);
        System.out.println("Pacman Power Time: " + pacmanPowerTime);
        System.out.print("Edible Ghosts: ");
        for (GHOST ghost : edibleGhosts) {
            System.out.print(ghost + " ");
        }
        System.out.println();
        System.out.print("Inedible Ghosts: ");
        for (GHOST ghost : inedibleGhosts) {
            System.out.print(ghost + " ");
        }
        System.out.println();
        System.out.print("Power Pills: ");
        for (int pill : powerPillsNode) {
            System.out.print(pill + " ");
        }
        System.out.println("\n---------------------------");
    }

    private void getEdibleGhostsInfo(Game game) {
        edibleGhosts.clear();
        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
            }
        }
    }

    private void getInedibleGhostsInfo(Game game) {
        inedibleGhosts.clear();
        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                inedibleGhosts.add(ghost);
            }
        }
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

    private int getNearestEdibleGhostNode(Game game) {
        int closestDistance = Integer.MAX_VALUE;
        int closestGhostNode = -1;

        for (GHOST ghost : this.edibleGhosts) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestGhostNode = ghostNode;
            }
        }

        return closestGhostNode;
    }

    private int getNearestInedibleGhostNode(Game game) {
        int closestDistance = Integer.MAX_VALUE;
        int closestGhostNode = -1;

        for (GHOST ghost : this.inedibleGhosts) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            int distance = game.getShortestPathDistance(pacmanNode, ghostNode);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestGhostNode = ghostNode;
            }
        }

        return closestGhostNode;
    }

    private int[] calculatePathMetrics(Game game, int location, int destination) {
        int[] path = game.getShortestPath(location, destination);
        boolean collisionDetected = false;
        int maxSafeDistance = 15;
        int pathScore = 0;

        for (int i = 0; i < path.length; i++) {
            int currentNode = path[i];
            if (isNodeSafe(game, currentNode)) {
                // Incrementa la puntuación si el nodo contiene una píldora
                if (game.getPillIndex(currentNode) != -1) {
                    pathScore += 10;
                }
                maxSafeDistance = i;
            } else {
                // Colisión detectada, detiene el análisis del camino
                collisionDetected = true;
                break;
            }
        }

        // Métricas: [colisión (0 o 1), distancia segura máxima, puntuación]
        return new int[] { collisionDetected ? 1 : 0, maxSafeDistance, pathScore };
    }

    // Verifica si el nodo está a una distancia segura de los fantasmas no
    // comestibles
    private boolean isNodeSafe(Game game, int node) {

        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                if (game.getShortestPathDistance(node, ghostNode) < SOME_SAFE_DISTANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    // Obtener el movimiento para perseguir fantasmas
    private MOVE getMoveToPursueGhosts(Game game) {

        // Si hay un fantasma comestible cerca, intenta perseguirlo
        if (getNearestEdibleGhostNode(game) != -1) {
            // Verifica si la ruta hacia el fantasma comestible es segura
            int[] metricsToEdible = calculatePathMetrics(game, pacmanNode, getNearestEdibleGhostNode(game));
            boolean collisionWithInedible = metricsToEdible[0] == 1;

            if (!collisionWithInedible) {
                return game.getNextMoveTowardsTarget(pacmanNode, this.getNearestEdibleGhostNode(game), DM.PATH);
            } else {
                // Si hay riesgo de colisión con un fantasma no comestible, intenta evadirlo
                return game.getNextMoveAwayFromTarget(pacmanNode, this.getNearestInedibleGhostNode(game), DM.PATH);
            }
        }
        // Si no hay fantasmas comestibles cercanos, opta por comer píldoras normales
        return getMoveToEatNormalPills(game);
    }

    private int getNearestPPillLocation(Game game) {
        int nearestPowerPillNode = -1;
        int nearestPowerPillPath = Integer.MAX_VALUE; // Cambia a un valor máximo inicial
        
        for (int powerPill : this.powerPillsNode) {
            int powerPillDist = game.getShortestPathDistance(this.pacmanNode, powerPill, this.pacmanLastMove); // Usar DM.PATH para obtener la distancia real
            if (powerPillDist < nearestPowerPillPath) {
                nearestPowerPillNode = powerPill;
                nearestPowerPillPath = powerPillDist;
            }
        }
        
        return nearestPowerPillNode;
    }
    

    private MOVE getMoveToGoToPowerPill(Game game) {
        int nearestPowerPillNode = getNearestPPillLocation(game);
    
        if (areAllGhostsOutOfJail(game) && nearestPowerPillNode != -1) {
            int[] metrics = calculatePathMetrics(game, pacmanNode, nearestPowerPillNode);
            boolean isPathSafe = metrics[0] == 0; // Verifica si no hay colisión en el camino hacia la píldora de poder
    
            if (isPathSafe) {
                return game.getNextMoveTowardsTarget(pacmanNode, nearestPowerPillNode, DM.PATH);
            }
        }
    
        // Si no se encuentra un camino seguro hacia la píldora de poder más cercana, opta por comer píldoras normales
        return getMoveToEatNormalPills(game);
    }
    
    
    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();

        if (pills.length > 0) {
            MOVE bestMove = MOVE.NEUTRAL;
            int highestScore = Integer.MIN_VALUE;
            int safestNode = -1;

            for (int pill : pills) {
                int[] metrics = calculatePathMetrics(game, pacmanNode, pill);
                boolean isPathSafe = metrics[0] == 0; // Verifica si no hay colisión en el camino hacia la píldora
                int pathScore = metrics[2]; // La puntuación del camino hacia la píldora

                if (isPathSafe && pathScore > highestScore) {
                    highestScore = pathScore;
                    safestNode = pill;
                }
            }

            if (safestNode != -1) {
                bestMove = game.getNextMoveTowardsTarget(pacmanNode, safestNode, DM.PATH);
            }

            return bestMove;
        }

        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }


    private MOVE getSafeMoveToTarget(Game game, int target) {
        // Calcula el movimiento más seguro hacia un objetivo
        return game.getPacmanLastMoveMade();// ...
    }

    private boolean isMoveSafe(Game game, MOVE move) {
        // Evalúa si un movimiento es seguro en términos de proximidad a fantasmas no
        // comestibles
        return true;// ...
    }

    private double calculateSafetyDistance(Game game, int node) {
        // Calcula la distancia de seguridad de un nodo a los fantasmas
        return 4;
    }

    private int getClosestNode(Game game, int fromNode, int[] nodes) {
        // Encuentra el nodo más cercano en un conjunto de nodos
        return 4;
    }
}

// Métodos para obtener información sobre fantasmas comestibles
