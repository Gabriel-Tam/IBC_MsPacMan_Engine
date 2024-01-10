package TheVoids2024.Gabo.Ghosts;


import java.util.EnumMap;
import java.util.Random;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts1 extends GhostController {
    private EnumMap<GHOST, MOVE> moves = new EnumMap<>(GHOST.class);
    private GHOST[] ghostnames = GHOST.values();
    private MOVE[] allMoves = MOVE.values();
    private Random rnd = new Random();

    private boolean pacManClosePowerPills(Game game, GHOST blinky) {
        int[] powerPills = game.getActivePowerPillsIndices();
        for (int i : powerPills) {
            int n = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), i, game.getPacmanLastMoveMade());
            if (n <= 50) {
                return true;
            }
        }
        return false;
    }

    private void blinkyFollowsMsPacMan(Game game, GHOST blinky) {
        int blinkyNode = game.getGhostCurrentNodeIndex(blinky);
        int msPacManNode = game.getPacmanCurrentNodeIndex();
        MOVE blinkyMove;
        if (this.pacManClosePowerPills(game, blinky) || game.getGhostEdibleTime(blinky) > 0) {
            blinkyMove = game.getNextMoveAwayFromTarget(blinkyNode, msPacManNode, DM.PATH);
        } else {
            blinkyMove = game.getNextMoveTowardsTarget(blinkyNode, msPacManNode, DM.PATH);
        }
        this.moves.put(blinky, blinkyMove);
    }

    private boolean pacManCloseGhost(Game game, GHOST clyde) {
        int pacManNode = game.getPacmanCurrentNodeIndex();
        int clydeNode = game.getGhostCurrentNodeIndex(clyde);
        int distance = game.getShortestPathDistance(pacManNode, clydeNode, game.getPacmanLastMoveMade());
        return distance <= 85;
    }

    private void clydeFollowPacMan(Game game, GHOST clyde) {
        int pacManNode = game.getPacmanCurrentNodeIndex();
        int clydeNode = game.getGhostCurrentNodeIndex(clyde);
        MOVE clydeMove;
        if (this.pacManCloseGhost(game, clyde)) {
            clydeMove = game.getNextMoveTowardsTarget(clydeNode, pacManNode, DM.PATH);
        } else if (game.getGhostEdibleTime(clyde) > 0) {
            clydeMove = game.getNextMoveAwayFromTarget(clydeNode, pacManNode, DM.PATH);
        } else {
            clydeMove = allMoves[rnd.nextInt(allMoves.length)];
        }
        this.moves.put(clyde, clydeMove);
    }

    private void pinkyLockPacMan(Game game, GHOST pinky) {
        int pinkyNode = game.getGhostCurrentNodeIndex(pinky);
        int pacManNode = game.getPacmanCurrentNodeIndex();
        MOVE pinkyMove;
        if (game.getGhostEdibleTime(pinky) > 0) {
            pinkyMove = game.getNextMoveAwayFromTarget(pinkyNode, pacManNode, DM.PATH);
        } else {
            pinkyMove = game.getNextMoveTowardsTarget(pinkyNode, pacManNode + 5, DM.PATH);
        }
        this.moves.put(pinky, pinkyMove);
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        for (GHOST ghost : this.ghostnames) {
            if (game.doesGhostRequireAction(ghost)) {
                if (ghost.equals(GHOST.BLINKY)) {
                    blinkyFollowsMsPacMan(game, ghost);
                } else if (ghost.equals(GHOST.SUE)) {
                    clydeFollowPacMan(game, ghost);
                } else if (ghost.equals(GHOST.PINKY)) {
                    pinkyLockPacMan(game, ghost);
                } else if (ghost.equals(GHOST.SUE)) {
                    pinkyLockPacMan(game, ghost);
                }

            }
        }
        return this.moves;
    }
}
