package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import java.util.Random;

public class Ghosts5 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);
    private Random rnd = new Random();
    private GHOST pursuerGhost = GHOST.BLINKY; // Fantasma que perseguirá a Pac-Man

    private void cooperativeStrategy(Game game) {
        // Estrategia cooperativa: un fantasma persigue a Pac-Man, los demás bloquean caminos

        // Obtener la posición actual de Pac-Man
        int pacManNode = game.getPacmanCurrentNodeIndex();

        // Manejar el movimiento del perseguidor
        handlePursuerMovement(game, pacManNode);

        // Manejar el movimiento de los bloqueadores
        handleBlockerMovement(game, pacManNode);
    }

    private void handlePursuerMovement(Game game, int pacManNode) {
        // Fantasma perseguidor sigue a Pac-Man
        int pursuerNode = game.getGhostCurrentNodeIndex(pursuerGhost);
        MOVE pursuerMove = game.getNextMoveTowardsTarget(pursuerNode, pacManNode, DM.PATH);
        moves.put(pursuerGhost, pursuerMove);
    }

    private void handleBlockerMovement(Game game, int pacManNode) {
        // Resto de los fantasmas bloquean caminos estratégicos
        for (GHOST ghost : GHOST.values()) {
            if (ghost != pursuerGhost && game.doesGhostRequireAction(ghost)) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);

                // Estrategia básica: moverse hacia nodos vecinos para bloquear caminos
                MOVE blockerMove = getBlockerMove(game, ghostNode, pacManNode);
                moves.put(ghost, blockerMove);
            }
        }
    }

    private MOVE getBlockerMove(Game game, int ghostNode, int pacManNode) {
        // Estrategia básica: moverse hacia nodos vecinos para bloquear caminos
        MOVE[] possibleMoves = game.getPossibleMoves(ghostNode);

        // Seleccionar un movimiento aleatorio entre los movimientos posibles
        if (possibleMoves.length > 0) {
            int randomIndex = rnd.nextInt(possibleMoves.length);
            return possibleMoves[randomIndex];
        }

        // Si no hay movimientos posibles, quedarse quieto
        return MOVE.NEUTRAL;
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        cooperativeStrategy(game);
        return moves;
    }
}
