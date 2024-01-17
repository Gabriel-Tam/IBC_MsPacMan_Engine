package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
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
    private MOVE pacmanLastMove; // Último movimiento realizado por Pac-Man
    private List<GHOST> edibleGhosts; // Lista de fantasmas comestibles
    private List<GHOST> inedibleGhosts; // Lista de fantasmas no comestibles
    private Map<GHOST, Integer> edibleGhostNodes; // Mapa de nodos de fantasmas comestibles
    private Map<GHOST, Integer> inedibleGhostNodes; // Mapa de nodos de fantasmas no comestibles
    private Map<Integer, Integer> powerPillNodes;// Ubicaciones de las píldoras de poder restantes
    private int SECURITY_DISTANCE = 30;
    private int powerPillDistanceLimit = 70;

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
        pacmanLastMove = game.getPacmanLastMoveMade();
        powerPillNodes.clear();
        for (int powerPillIndex : game.getActivePowerPillsIndices()) {
            powerPillNodes.put(powerPillIndex, game.getNodeXCood(powerPillIndex), game.getNodeYCood(powerPillIndex));
        }
        // Si hay fantasmas comestibles, Pac-Man está bajo el efecto de una Power Pill
        pacmanPowerTime = 0;

        // Actualiza la información sobre los fantasmas

        // Imprimir la información actualizada
        System.out.println("Pacman Node: " + pacmanNode);
        System.out.println("Pacman Last Move: " + pacmanLastMove);
        System.out.println("Pacman Power Time: " + pacmanPowerTime);
        System.out.print("Edible Ghosts: ");
        for (GHOST ghost : edibleGhosts) {
            System.out.print(ghost + " (Node: " + edibleGhostNodes.get(ghost) + ") ");
        }
        System.out.println();
        System.out.print("Inedible Ghosts: ");
        for (GHOST ghost : inedibleGhosts) {
            System.out.print(ghost + " (Node: " + inedibleGhostNodes.get(ghost) + ") ");
        }
        System.out.println();
        System.out.print("Power Pills: ");
        for (int pill : powerPillsNode) {
            System.out.print(pill + " ");
        }

        // Obtener el fantasma más cercano y mostrarlo
        GHOST closestGhost = getClosestGhostToPacman(game);
        System.out.println("\nClosest Ghost to Pacman: " + closestGhost + " (Node: "
                + (closestGhost != null ? edibleGhostNodes.get(closestGhost) : "") + ")");

        System.out.println("\n---------------------------");
    }

    // Obtiene el fantasma más cercano (no importa si es comible o no)
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

    private Constants.MOVE getMoveGoToPowerPill(Game game) {
        if (areAllGhostsOutOfJail(game)) {
            int[] powerPills = game.getActivePowerPillsIndices();

            if (powerPills.length > 0) {
                double closestDistance = Double.MAX_VALUE;
                int closestPowerPill = -1;

                for (int powerPillIndex : powerPills) {
                    double distance = game.getDistance(pacmanNode, powerPillIndex, DM.PATH);

                    // Verificar si los fantasmas no comestibles están cerca antes de dirigirse a la
                    // píldora de poder
                    if (distance < powerPillDistanceLimit && areInedibleGhostsNear(game, this.pacmanNode)) {
                        continue; // Evitar dirigirse a la píldora si los fantasmas no comestibles están cerca
                    }

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestPowerPill = powerPillIndex;
                    }
                }

                if (closestPowerPill != -1) {
                    return game.getApproximateNextMoveTowardsTarget(this.pacmanNode, closestPowerPill,
                            this.pacmanLastMove, DM.PATH);
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

    private MOVE getMoveToEatNormalPills(Game game) {
        int[] pills = game.getActivePillsIndices();

        if (pills.length > 0) {
            int randomIndex = (int) (Math.random() * pills.length); // Generar un índice aleatorio
            int targetNode = pills[randomIndex]; // Seleccionar una píldora aleatoria

            return this.getBestMoveToTarget(game, targetNode); // Obtener el mejor movimiento hacia la píldora
                                                               // seleccionada
        }
        return lastMove; // Si no hay píldoras normales activas, retorna el último movimiento
    }

private MOVE getMoveToPursueGhosts(Game game) {
    // TODO Auto-generated method stub
    game.getNextMoveTowardsTarget(pacmanNode, , DM.PATH);
}

}
