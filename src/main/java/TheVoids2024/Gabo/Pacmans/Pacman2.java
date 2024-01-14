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


        
    @Override// ----------------------------------------------

    public MOVE getMove(Game game, long timeDue) {
        List<GHOST> edibleGhosts = getEdibleGhosts(game);

        if (edibleGhosts.size() > 0) {
            pacmanPowerTime = game.getGhostEdibleTime(edibleGhosts.get(0));

            System.out.println("Pac-Man Power Time: " + pacmanPowerTime);
            for (GHOST ghost : edibleGhosts) {
                System.out.println("Edible Ghost: " + ghost);
            }
             System.out.println("Edible ghost mas cercano: "+this.getClosestEdibleGhost(game));
            //* */
            

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

    private List<GHOST> getEdibleGhosts(Game game) {
        List<GHOST> edibleGhosts = new ArrayList<>();

        for (GHOST ghost : GHOST.values()) {
            if (game.isGhostEdible(ghost)) {
                edibleGhosts.add(ghost);
            }
        }

        return edibleGhosts;
    }

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


    // Implementa estos métodos según tu lógica de juego
    private MOVE getMoveToPursueGhosts(Game game) {
        int pacmanNode = game.getPacmanCurrentNodeIndex();
        for (GHOST ghost : GHOST.values()) {
            int ghostNode = game.getGhostCurrentNodeIndex(ghost);
            if (game.isGhostEdible(ghost))lastMove = game.getNextMoveTowardsTarget(pacmanNode, ghostNode, DM.PATH);
            else   lastMove = game.getNextMoveAwayFromTarget(pacmanNode, ghostNode,DM.PATH);
             
        }
        return lastMove;
    }

    private MOVE getMoveToGoToPowerPill(Game game) {
        return lastMove;
        /* ... */ }

    private MOVE getMoveToEatNormalPills(Game game) {
        return lastMove;
        /* ... */ }

    // Otros
}