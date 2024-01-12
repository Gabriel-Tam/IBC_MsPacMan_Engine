package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

import java.util.Arrays;
import pacman.game.Constants.GHOST;

public final class Pacman3 extends PacmanController {
    private static final int BLINKY_INDEX = 0;
    private static final int INKY_INDEX = 1;
    private static final int PINKY_INDEX = 2;
    private static final int SUE_INDEX = 3;

    private boolean[] ghostsEdible = new boolean[4];
    private double[] ghostDistances = new double[4];
    private int[] ghostNodes = new int[4];

    private boolean BLINKYedible;
    private boolean INKYedible;
    private boolean PINKYedible;
    private boolean SUEedible;

    private int[] powerPills = new int[4];
    private double[] powerPillDistances = new double[4];

    private void updateGame(Game game) {
        getGhostInfo(game);
        getPowerPillsInfo(game);
    }

    private void getGhostInfo(Game game) {
        Constants.GHOST[] ghosts = GHOST.values();

        Arrays.sort(ghosts, (g1, g2) -> Double.compare(
                game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(g1), DM.PATH),
                game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(g2), DM.PATH)
        ));

        for (int i = 0; i < 4; i++) {
            ghostsEdible[i] = game.isGhostEdible(ghosts[i]);
            ghostNodes[i] = game.getGhostCurrentNodeIndex(ghosts[i]);
            ghostDistances[i] = game.getDistance(game.getPacmanCurrentNodeIndex(), ghostNodes[i], DM.PATH);
        }

        BLINKYedible = ghostsEdible[BLINKY_INDEX];
        INKYedible = ghostsEdible[INKY_INDEX];
        PINKYedible = ghostsEdible[PINKY_INDEX];
        SUEedible = ghostsEdible[SUE_INDEX];
    }

    private void getPowerPillsInfo(Game game) {
        Integer[] powerPillsInteger = Arrays.stream(game.getActivePowerPillsIndices()).boxed().toArray(Integer[]::new);

        if (powerPillsInteger.length > 0) {
            Arrays.sort(powerPillsInteger, (p1, p2) -> Double.compare(
                    game.getDistance(game.getPacmanCurrentNodeIndex(), p1, DM.PATH),
                    game.getDistance(game.getPacmanCurrentNodeIndex(), p2, DM.PATH)
            ));

            for (int i = 0; i < Math.min(4, powerPillsInteger.length); i++) {
                powerPills[i] = powerPillsInteger[i];
                powerPillDistances[i] = game.getDistance(game.getPacmanCurrentNodeIndex(), powerPills[i], DM.PATH);
            }
        }
    }
        private MOVE decideMove(Game game) {
            //Logica para decidir el movimiento de pacman
            return lastMove;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        updateGame(game);
        return decideMove(game);
    }
}
