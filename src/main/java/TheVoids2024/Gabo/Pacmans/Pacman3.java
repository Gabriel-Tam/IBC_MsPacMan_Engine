package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Game;
import pacman.game.Constants.MOVE;
import java.util.Arrays;
import pacman.game.Constants.GHOST;

public final class Pacman3 extends PacmanController {

    private int[] powerPills = new int[4];
    private double[] powerPillDistances = new double[4];
    private boolean[] ghostsEdible = new boolean[4]; // Un solo vector para los 4 fantasmas
    private double[] ghostDistances = new double[4];
    private int[] ghostNodes = new int[4];
    
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
        updateGame(game);
    
        // Ejemplo de lógica para decidir el movimiento
        if (anyGhostIsThreatening(game) && closestPowerPillIsAccessible(game)) {
            return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), closestPowerPill(game), DM.PATH);
        } else if (anyEdibleGhostIsClose(game)) {
            return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), closestEdibleGhost(game), DM.PATH);
        } else {
            // Estrategia por defecto
            Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
            if (possibleMoves.length > 0) {
                return possibleMoves[0]; // Devuelve el primer movimiento posible
            } else {
                return MOVE.NEUTRAL; // En caso de que no haya movimientos posibles, devuelve NEUTRAL
            }
        }
    }

    private boolean anyGhostIsThreatening(Game game) {
        // Implementar lógica para determinar si algún fantasma es una amenaza
        // Por ejemplo, si hay un fantasma no comestible dentro de cierta distancia
        for (int i = 0; i < 4; i++) {
            if (!ghostsEdible[i] && ghostDistances[i] < 58) {
                return true;
            }
        }
        return false;
    }
    

    private boolean closestPowerPillIsAccessible(Game game) {
        // Determinar si la píldora de poder más cercana es accesible y segura para alcanzar
        // Por ejemplo, si la distancia a la píldora de poder más cercana es menor a cierto umbral
        return powerPillDistances[0] < 58; // Umbral de distancia
    }

    private boolean anyEdibleGhostIsClose(Game game) {
        // Verificar si hay algún fantasma comestible cerca y accesible
        // Por ejemplo, si hay un fantasma comestible dentro de cierta distancia
        for (int i = 0; i < 4; i++) {
            if (ghostsEdible[i] && ghostDistances[i] < 15) {
                return true;
            }
        }
        return false;
    }

    private int closestPowerPill(Game game) {
        // Encuentra la píldora de poder más cercana a Pac-Man
        // Por ejemplo, devuelve el índice de la píldora de poder con la menor distancia
        int closestIndex = 0;
        for (int i = 1; i < 4; i++) {
            if (powerPillDistances[i] < powerPillDistances[closestIndex]) {
                closestIndex = i;
            }
        }
        return powerPills[closestIndex];
    }

    private int closestEdibleGhost(Game game) {
        // Encuentra el fantasma comestible más cercano a Pac-Man
        // Por ejemplo, devuelve el índice del fantasma comestible con la menor distancia
        int closestIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (ghostsEdible[i] && (closestIndex == -1 || ghostDistances[i] < ghostDistances[closestIndex])) {
                closestIndex = i;
            }
        }
        return closestIndex != -1 ? ghostNodes[closestIndex] : -1;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        return decideMove(game);
    }
}
