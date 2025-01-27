package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Pacman10 extends PacmanController {
    private int pacmanPowerTime = 0; // Tiempo restante de poder de Pac-Man
    private int pacmanNode; // Ubicación actual de Pac-Man
    private MOVE pacmanNextMove; // Siguiente movimiento que realizará por Pac-Man
    private List<GHOST> edibleGhosts; // Lista de fantasmas comestibles
    private List<GHOST> inedibleGhosts; // Lista de fantasmas no comestibles
    private Map<GHOST, Integer> edibleGhostNodes; // Mapa de nodos de fantasmas comestibles
    private Map<GHOST, Integer> inedibleGhostNodes; // Mapa de nodos de fantasmas no comestibles
    private Map<Integer, int[]> powerPillNodes;// Ubicaciones de las píldoras de poder restantes
    private int SECURITY_DISTANCE = 30;

    public Pacman10() {
        edibleGhosts = new ArrayList<>();
        inedibleGhosts = new ArrayList<>();
        edibleGhostNodes = new HashMap<>();
        inedibleGhostNodes = new HashMap<>();
        powerPillNodes = new HashMap<>();
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        updateGameInfo(game); // Actualiza la información del juego


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
        // Si hay fantasmas comestibles, Pac-Man está bajo el efecto de una Power Pill
        pacmanPowerTime = edibleGhosts.isEmpty() ? 0 : game.getGhostEdibleTime(edibleGhosts.get(0));

/* 
        // Imprimir la información actualizada
        System.out.println("Pacman Node: " + pacmanNode);
        System.out.println("Pacman Last Move: " + pacmanNextMove);
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
        for (int pillIndex : powerPillNodes.keySet()) {
            System.out.print(pillIndex+" " );
        }
        System.out.println();
    
               System.out.println("\n---------------------------");
        */
    }

    private void getGhostsInfo(Game game) {
        edibleGhosts.clear();
        inedibleGhosts.clear();
        edibleGhostNodes.clear();
        inedibleGhostNodes.clear();

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
                edibleGhostNodes.put(ghost, game.getGhostCurrentNodeIndex(ghost));
            } else if (!game.isGhostEdible(ghost) && game.getGhostLairTime(ghost) == 0) {
                inedibleGhosts.add(ghost);
                inedibleGhostNodes.put(ghost, game.getGhostCurrentNodeIndex(ghost));
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

    private MOVE getMoveGoToPowerPill(Game game) {
        if (areAllGhostsOutOfJail(game)) {
            int[] powerPills = game.getActivePowerPillsIndices();
    
            if (powerPills.length > 0) {
                double closestDistance = Double.MAX_VALUE;
                int closestPowerPill = -1;
    
                for (int powerPillIndex : powerPills) {
                    double distance = game.getDistance(pacmanNode, powerPillIndex, DM.PATH);
    
                    // Verificar si los fantasmas no comestibles están cerca antes de dirigirse a la
                    // píldora de poder
                    if (areInedibleGhostsNear(game, pacmanNode)) {
                        continue; // Evitar dirigirse a la píldora si los fantasmas no comestibles están cerca
                    }
    
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestPowerPill = powerPillIndex;
                    }
                }
    
                if (closestPowerPill != -1) {
                    return game.getApproximateNextMoveTowardsTarget(pacmanNode, closestPowerPill,
                            pacmanNextMove, DM.PATH);
                }
            }
        }
    
        return this.getMoveToEatNormalPills(game); // No se dirige a ninguna píldora de poder
    }
    

    private boolean areInedibleGhostsNear(Game game, int pacmanNode) {
        for (GHOST ghost : inedibleGhosts) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            double distance = game.getDistance(pacmanNode, ghostNode, DM.PATH);
            if (distance < SECURITY_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    private MOVE getBestMoveToTarget(Game game, int NodeIndex) {
        MOVE[] possibleMoves = game.getPossibleMoves(this.pacmanNode, this.pacmanNextMove);
        MOVE bestMove = null;
        double bestRisk = Double.MAX_VALUE;
    
        for (MOVE move : possibleMoves) {
            int nextNode = game.getNeighbour(pacmanNode, move);
            if (nextNode != -1) { // Verifica si el nodo es válido
        
                // Calcular el riesgo en función de la posición y dirección de los fantasmas cercanos
                double risk = calculateRisk(game, nextNode);
        
                // Si el riesgo es menor que el mejor riesgo actual, actualiza la dirección
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
            int ghostNode = inedibleGhostNodes.get(ghost);
            double distanceToGhost = game.getDistance(node, ghostNode, DM.PATH);
            risk += (1.0 / (distanceToGhost + 1)) * 2.0;
        }
    
        return risk;
    }
    

    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();
    
        if (pills.length > 0) {
            int closestPill = getClosestPill(game, pills);
            return this.getBestMoveToTarget(game, closestPill); // Obtener el mejor movimiento hacia la píldora más cercana
        }
        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }
    
    // Método para encontrar la píldora normal más cercana a Pac-Man
    private int getClosestPill(Game game, int[] pills) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
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
            // No hay fantasmas comestibles, puedes manejar esta situación de manera específica
            // como retroceder o moverte hacia las píldoras normales, dependiendo de tu estrategia.
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
