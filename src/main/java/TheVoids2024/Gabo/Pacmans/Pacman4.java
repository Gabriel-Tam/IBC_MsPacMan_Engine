package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;

public final class Pacman4 extends PacmanController {
    private GHOST closestGhost;
    private GHOST secondGhost;
    private GHOST thirdGhost;
    private GHOST farthestGhost;

    private boolean BLINKYedible;
    private boolean INKYedible;
    private boolean PINKYedible;
    private boolean SUEedible;

    private double closestGhostdistance;
    private double secondGhostdistance;
    private double thirdGhostdistance;
    private double farthestGhostdistance;
    private int closestGhostNode;
    private int secondGhostNode;
    private int thirdGhostNode;
    private int farthestGhostNode;

    private int closestPowerPill;
    private int secondPowerPill;
    private int thirdPowerPill;
    private int farthestPowerPill;
    private double closestPowerPillDistance;
    private double secondPowerPillDistance;
    private double thirdPowerPillDistance;
    private double farthestPowerPillDistance;

    @Override
    public MOVE getMove(Game game, long timeDue) {
        // Obtener información sobre fantasmas y píldoras de poder
        getGhostInfo(game);
        getPowerPillsInfo(game);

        // Agrega tu lógica para determinar el movimiento aquí
        // Por ejemplo, podrías basar el movimiento en la información recopilada sobre fantasmas y píldoras de poder.

        return lastMove; // Mantén esto por ahora si aún no has implementado la lógica específica.
    }

    private void getGhostInfo(Game game) {
        BLINKYedible = game.isGhostEdible(GHOST.BLINKY);
        INKYedible = game.isGhostEdible(GHOST.INKY);
        PINKYedible = game.isGhostEdible(GHOST.PINKY);
        SUEedible = game.isGhostEdible(GHOST.SUE);

        double dstClosest = Double.MAX_VALUE;
        double dstSecond = Double.MAX_VALUE;
        double dstThird = Double.MAX_VALUE;
        double dstFarthest = Double.MAX_VALUE;

        int pacmanNode = game.getPacmanCurrentNodeIndex();

        for (GHOST g : GHOST.values()) {
            double dst = game.getDistance(pacmanNode, game.getGhostCurrentNodeIndex(g), DM.PATH);
            if (dstClosest > dst) {
                dstFarthest = dstThird;
                farthestGhostdistance = thirdGhostdistance;
                dstThird = dstSecond;
                thirdGhostdistance = secondGhostdistance;
                dstSecond = dstClosest;
                secondGhostdistance = closestGhostdistance;
                dstClosest = dst;
                closestGhostdistance = dst;
                closestGhost = g;
            } else if (dst < dstSecond) {
                dstFarthest = dstThird;
                farthestGhostdistance = thirdGhostdistance;
                dstThird = dstSecond;
                thirdGhostdistance = secondGhostdistance;
                dstSecond = dst;
                secondGhostdistance = dst;
                secondGhost = g;
            } else if (dst < dstThird) {
                dstFarthest = dstThird;
                farthestGhostdistance = thirdGhostdistance;
                dstThird = dst;
                thirdGhostdistance = dst;
                thirdGhost = g;
            } else {
                dstFarthest = dst;
                farthestGhostdistance = dst;
                farthestGhost = g;
            }
        }

        closestGhostNode = game.getGhostCurrentNodeIndex(closestGhost);
        secondGhostNode = game.getGhostCurrentNodeIndex(secondGhost);
        thirdGhostNode = game.getGhostCurrentNodeIndex(thirdGhost);
        farthestGhostNode = game.getGhostCurrentNodeIndex(farthestGhost);
    }

    private void getPowerPillsInfo(Game game) {
        int[] powerPills = game.getActivePowerPillsIndices();
        if (powerPills.length > 0) {
            int pacmanNode = game.getPacmanCurrentNodeIndex();
            closestPowerPill = game.getClosestNodeIndexFromNodeIndex(pacmanNode, powerPills, DM.PATH);
            farthestPowerPill = game.getFarthestNodeIndexFromNodeIndex(pacmanNode, powerPills, DM.PATH);

            int third = -1;
            int second = -1;
            if (powerPills.length > 2) {
                for (int i = 0; i < powerPills.length; ++i) {
                    if (powerPills[i] != closestPowerPill && powerPills[i] != farthestPowerPill) {
                        if (second == -1) {
                            second = powerPills[i];
                        } else {
                            third = powerPills[i];
                        }
                    }
                }

                if (game.getDistance(pacmanNode, second, DM.PATH) > game.getDistance(pacmanNode, third, DM.PATH)) {
                    int temp = second;
                    second = third;
                    third = temp;
                }

                secondPowerPill = second;
                thirdPowerPill = third;
                secondPowerPillDistance = game.getDistance(pacmanNode, secondPowerPill, DM.PATH);
                thirdPowerPillDistance = game.getDistance(pacmanNode, thirdPowerPill, DM.PATH);
            }

            closestPowerPillDistance = game.getDistance(pacmanNode, closestPowerPill, DM.PATH);
            farthestPowerPillDistance = game.getDistance(pacmanNode, farthestPowerPill, DM.PATH);
        }
    }
}
