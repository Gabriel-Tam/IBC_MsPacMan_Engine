package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class AllGhostsFollow extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    private void chasePacman(Game game) {
        int pacManNode = game.getPacmanCurrentNodeIndex();
        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost)) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                MOVE move = game.getNextMoveTowardsTarget(ghostNode, pacManNode, DM.PATH);
                moves.put(ghost, move);
            }
        }
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        chasePacman(game);
        return moves;
    }
}
