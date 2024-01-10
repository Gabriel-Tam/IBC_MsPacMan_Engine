package TheVoids2024.Gabo.Ghosts;
import java.util.EnumMap;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts2 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);

    private void mixedStrategy(Game game) {
        int pacManNode = game.getPacmanCurrentNodeIndex();

        for (GHOST ghost : GHOST.values()) {
            if (game.doesGhostRequireAction(ghost)) {
                int ghostNode = game.getGhostCurrentNodeIndex(ghost);
                MOVE move;

                if (ghost.equals(GHOST.BLINKY)) {
                    // Blinky va directo a por Pac-Man
                    move = game.getNextMoveTowardsTarget(ghostNode, pacManNode, DM.PATH);
                } else {
                    // Los demás fantasmas van a la intersección más cercana a la que irá Pac-Man
                    int[] junctions = game.getJunctionIndices();
                    int nearestJunction = getNearestJunction(game, pacManNode, junctions);
                    move = game.getNextMoveTowardsTarget(ghostNode, nearestJunction, DM.PATH);
                }

                moves.put(ghost, move);
            }
        }
    }

    private int getNearestJunction(Game game, int pacManNode, int[] junctions) {
        int nearestJunction = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int junction : junctions) {
            int distance = (int) game.getDistance(pacManNode, junction, game.getPacmanLastMoveMade(), DM.PATH);

            if (distance < minDistance) {
                minDistance = distance;
                nearestJunction = junction;
            }
        }

        return nearestJunction;
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        moves.clear();
        mixedStrategy(game);
        return moves;
    }
}
