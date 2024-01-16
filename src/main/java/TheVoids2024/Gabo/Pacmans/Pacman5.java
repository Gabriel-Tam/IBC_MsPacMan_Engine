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
    


    private MOVE getMoveToPursueGhosts(Game game) {
        // Devuelve un movimiento para perseguir fantasmas comestibles, considerando la seguridad
        return game.getPacmanLastMoveMade();// ...
    }

    private MOVE getMoveToGoToPowerPill(Game game) {
        // Devuelve un movimiento para ir a la píldora de poder más segura
        return game.getPacmanLastMoveMade();// ...
    }

    private MOVE getMoveToEatNormalPills(Game game) {
        // Devuelve un movimiento para comer la píldora normal más segura
        return game.getPacmanLastMoveMade();// ...
    }

    private MOVE getSafeMoveToTarget(Game game, int target) {
        // Calcula el movimiento más seguro hacia un objetivo
        return game.getPacmanLastMoveMade();// ...
    }

    private boolean isMoveSafe(Game game, MOVE move) {
        // Evalúa si un movimiento es seguro en términos de proximidad a fantasmas no comestibles
        return true;// ...
    }

    private int getSafePillNode(Game game) {
        // Encuentra la píldora más segura (más alejada de los fantasmas)
        return (2);
        // ...
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
