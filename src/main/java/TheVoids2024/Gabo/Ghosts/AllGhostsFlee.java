package TheVoids2024.Gabo.Ghosts;

import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class AllGhostsFlee extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    private void fleeFromPacman(Game game) {
        int pacManNode = game.getPacmanCurrentNodeIndex();

        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost)) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                MOVE move = game.getNextMoveAwayFromTarget(ghostNode, pacManNode, DM.PATH);
                moves.put(ghost, move);
            }
        }
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        fleeFromPacman(game);
        return moves;
    }
}
