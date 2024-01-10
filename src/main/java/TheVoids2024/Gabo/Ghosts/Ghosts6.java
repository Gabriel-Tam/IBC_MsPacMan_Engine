package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts6 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        int pacManNode = game.getPacmanCurrentNodeIndex();

        // Asigna roles a los fantasmas
        moves.put(GHOST.BLINKY, followPacMan(game, GHOST.BLINKY, pacManNode)); // Líder
        moves.put(GHOST.PINKY, flankPacMan(game, GHOST.PINKY, pacManNode)); // Flanqueador
        moves.put(GHOST.INKY, flankPacMan(game, GHOST.INKY, pacManNode)); // Flanqueador
        moves.put(GHOST.SUE, guardPowerPills(game, GHOST.SUE)); // Guardián

        return moves;
    }

    private MOVE followPacMan(Game game, GHOST ghost, int pacManNode) {
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        return game.getNextMoveTowardsTarget(ghostNode, pacManNode, DM.PATH);
    }

    private MOVE flankPacMan(Game game, GHOST ghost, int pacManNode) {
        // Implementar lógica para flanquear a Pac-Man
        // Esto podría implicar calcular una ruta hacia un nodo lateral a Pac-Man
        // Por ejemplo, seleccionar un nodo a cierta distancia en la ruta de Pac-Man
        // y luego calcular el camino hacia ese nodo

        // Por ahora, esta es solo una implementación básica
        return followPacMan(game, ghost, pacManNode); // Temporalmente sigue a Pac-Man
    }

    private MOVE guardPowerPills(Game game, GHOST ghost) {
        // Implementar lógica para guardar las Power Pills
        // Esto podría implicar moverse hacia la Power Pill más cercana que no esté consumida
        // y luego quedarse cerca de esa área

        // Por ahora, esta es solo una implementación básica
        int ghostNode = game.getGhostCurrentNodeIndex(ghost);
        int[] powerPills = game.getActivePowerPillsIndices();
        if (powerPills.length > 0) {
            return game.getNextMoveTowardsTarget(ghostNode, powerPills[0], DM.PATH);
        }
        return MOVE.NEUTRAL; // Si no hay Power Pills activas, no moverse
    }
}
