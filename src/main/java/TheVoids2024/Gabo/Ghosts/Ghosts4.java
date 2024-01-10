package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts4 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    private GHOST pursuerGhost = GHOST.BLINKY; // Fantasma que perseguirá a Pac-Man

    private void sophisticatedStrategy(Game game) {
        int pacManNode = game.getPacmanCurrentNodeIndex();

        // Fantasma perseguidor sigue a Pac-Man
        handlePursuerMovement(game, pacManNode);

        // Resto de los fantasmas toman decisiones sofisticadas
        handleSophisticatedMovement(game, pacManNode);
    }

    private void handlePursuerMovement(Game game, int pacManNode) {
        int pursuerNode = game.getGhostCurrentNodeIndex(pursuerGhost);
        MOVE pursuerMove = game.getNextMoveTowardsTarget(pursuerNode, pacManNode, DM.PATH);
        moves.put(pursuerGhost, pursuerMove);
    }

    private void handleSophisticatedMovement(Game game, int pacManNode) {
        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost) && ghost != pursuerGhost) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);

                // Verificar si Pac-Man está cerca de una píldora de poder
                boolean pacManCloseToPowerPill = isPacManCloseToPowerPill(game, pacManNode);

                // Tomar decisiones en función de la situación
                MOVE move;
                if (pacManCloseToPowerPill) {
                    // Si Pac-Man está cerca de una píldora de poder, huir
                    move = game.getNextMoveAwayFromTarget(ghostNode, pacManNode, DM.PATH);
                } else {
                    // Si no, tomar una decisión basada en la distancia
                    move = makeDecisionBasedOnDistance(game, ghostNode, pacManNode);
                }

                moves.put(ghost, move);
            }
        }
    }

    private boolean isPacManCloseToPowerPill(Game game, int pacManNode) {
        int[] powerPills = game.getActivePowerPillsIndices();
        for (int powerPillIndex : powerPills) {
            int distanceToPowerPill = game.getShortestPathDistance(pacManNode, powerPillIndex, game.getPacmanLastMoveMade());
            if (distanceToPowerPill <= 20) { // Ajusta según sea necesario
                return true;
            }
        }
        return false;
    }

    private MOVE makeDecisionBasedOnDistance(Game game, int ghostNode, int pacManNode) {
        int distanceToPacMan = game.getShortestPathDistance(ghostNode, pacManNode, game.getPacmanLastMoveMade());

        // Puedes ajustar estos valores según tus preferencias
        if (distanceToPacMan <= 40) {
            // Si Pac-Man está relativamente cerca, huir
            return game.getNextMoveAwayFromTarget(ghostNode, pacManNode, DM.PATH);
        } else {
            // Si Pac-Man está lejos, acercarse
            return game.getNextMoveTowardsTarget(ghostNode, pacManNode, DM.PATH);
        }
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        sophisticatedStrategy(game);
        return moves;
    }
}
