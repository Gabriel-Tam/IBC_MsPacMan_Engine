package TheVoids2024.Dani;

import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.EnumMap;

public final class PacmanDani extends PacmanController {

    private enum State {
        EXPLORING,
        EATING_PILLS,
        CHASING_GHOST,
        EVADING_GHOST,
        SEEKING_POWER_PILL
    }

    private State currentState;
    private EnumMap<GHOST, MOVE> ghostMoves;

    public PacmanDani() {
        currentState = State.EXPLORING; // Estado inicial
        ghostMoves = new EnumMap<>(GHOST.class);
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        updateStateDynamic(game); // Actualiza el estado basado en las observaciones actuales
        return makeMove(game); // Realiza un movimiento basado en el estado actual
    }

    private void updateStateDynamic(Game game) {
        int currentPacmanIndex = game.getPacmanCurrentNodeIndex();
        boolean closeToGhost = closeToAnyGhost(game);
        boolean anyPowerPillNearby = game.getNumberOfActivePowerPills() > 0
                && game.getDistance(currentPacmanIndex, game.getClosestNodeIndexFromNodeIndex(currentPacmanIndex,
                        game.getActivePowerPillsIndices(), Constants.DM.PATH), Constants.DM.PATH) < 40;
        boolean anyEdibleGhost = anyEdibleGhost(game);
        if (game.wasPacManEaten()) {
            currentState = State.EVADING_GHOST;
        } else if (anyEdibleGhost && game.getNumberOfActivePowerPills() > 0) {
            currentState = State.CHASING_GHOST;
        } else if (closeToGhost) {
            currentState = State.EVADING_GHOST;
        } else if (anyPowerPillNearby) {
            currentState = State.SEEKING_POWER_PILL;
        } else {
            currentState = State.EXPLORING;
        }
    }

    private boolean closeToAnyGhost(Game game) {
        int currentPacmanIndex = game.getPacmanCurrentNodeIndex();
        // Consideramos que estamos cerca de un fantasma si está a menos de 20 unidades
        // de distancia.
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) == 0
                    && game.getShortestPathDistance(currentPacmanIndex, game.getGhostCurrentNodeIndex(ghost)) < 20) {
                return true;
            }
        }
        return false;
    }

    private MOVE makeMove(Game game) {
        int currentPacmanIndex = game.getPacmanCurrentNodeIndex();
        MOVE move = MOVE.NEUTRAL;

        // Si hay fantasmas peligrosos cerca, prioriza la evasión
        if (closeToAnyGhost(game)) {
            move = moveAwayFromClosestNonEdibleGhost(game);
        } else {
            // En otros casos, sigue la lógica del estado actual
            switch (currentState) {
                case SEEKING_POWER_PILL:
                    move = moveToClosestPowerPill(game, currentPacmanIndex);
                    break;
                case EATING_PILLS:
                    move = getBestPillPath(game, currentPacmanIndex);
                    break;
                case CHASING_GHOST:
                    move = moveToClosestEdibleGhost(game);
                    break;
                case EXPLORING:
                default:
                    // Evaluación de riesgo y recompensa en todo momento
                    move = evaluateRiskAndReward(game, currentPacmanIndex);
                    break;
            }
        }

        return move != MOVE.NEUTRAL ? move : game.getPacmanLastMoveMade();
    }

    private MOVE moveAwayFromClosestNonEdibleGhost(Game game) {
        int minDistance = Integer.MAX_VALUE;
        GHOST nearestGhost = null;
        int currentPacmanIndex = game.getPacmanCurrentNodeIndex();

        for (GHOST ghost : GHOST.values()) {
            if (!game.isGhostEdible(ghost)) {
                int distance = game.getShortestPathDistance(currentPacmanIndex, game.getGhostCurrentNodeIndex(ghost));
                if (distance < minDistance && distance != -1) {
                    minDistance = distance;
                    nearestGhost = ghost;
                }
            }
        }

        if (nearestGhost != null) {
            // Encuentra el camino más largo (opuesto al fantasma)
            return game.getNextMoveAwayFromTarget(
                    currentPacmanIndex, game.getGhostCurrentNodeIndex(nearestGhost),
                    Constants.DM.PATH);
        }

        return MOVE.NEUTRAL;
    }

    private boolean anyEdibleGhost(Game game) {
        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                return true;
            }
        }
        return false;
    }

    private MOVE evaluateRiskAndReward(Game game, int currentPacmanIndex) {
        MOVE bestMove = MOVE.NEUTRAL;
        double highestScore = Double.NEGATIVE_INFINITY;

        for (MOVE move : game.getPossibleMoves(currentPacmanIndex)) {
            double score = calculateMoveScore(game, currentPacmanIndex, move);
            if (score > highestScore) {
                highestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private double calculateMoveScore(Game game, int currentNode, MOVE move) {
        ArrayList<Integer> predictedGhostPositions = predictGhostPositions(game);
        double riskScore = 0.0;
        double rewardScore = 0.0;
        // Aumenta el riesgo si un fantasma se dirige hacia la misma ubicación
        for (int predictedGhostLocation : predictedGhostPositions) {
            if (predictedGhostLocation != -1 && currentNode != -1) {
                double distance = game.getDistance(currentNode, predictedGhostLocation, move, Constants.DM.PATH);
                if (game.getGhostEdibleTime(
                        GHOST.values()[predictedGhostPositions.indexOf(predictedGhostLocation)]) == 0) {
                    // Mayor riesgo si el fantasma no es comestible
                    riskScore += 2.0 / (distance + 1);
                } else {
                    // Menor riesgo si el fantasma es comestible
                    riskScore += 0.5 / (distance + 1);
                }
            }
        }

        // Recompensa por power pills y píldoras regulares
        int[] powerPills = game.getActivePowerPillsIndices();
        int[] pills = game.getActivePillsIndices();
        for (int powerPill : powerPills) {
            double distance = game.getDistance(currentNode, powerPill, move, Constants.DM.PATH);
            rewardScore += 10.0 / (distance + 1); // Mayor recompensa para power pills cercanas
        }
        for (int pill : pills) {
            double distance = game.getDistance(currentNode, pill, move, Constants.DM.PATH);
            rewardScore += 1.0 / (distance + 1); // Menor recompensa, pero aún valiosa, para píldoras regulares
        }

        return rewardScore - riskScore;
    }

    private ArrayList<Integer> predictGhostPositions(Game game) {
        ArrayList<Integer> predictedGhostPositions = new ArrayList<>();
        for (GHOST ghost : GHOST.values()) {
            int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
            MOVE ghostLastMove = game.getGhostLastMoveMade(ghost);
            int predictedLocation = game.getNeighbour(ghostLocation, ghostLastMove);
            if (predictedLocation != -1) {
                predictedGhostPositions.add(predictedLocation);
            }
        }
        return predictedGhostPositions;
    }

    private MOVE getBestPillPath(Game game, int currentPacmanIndex) {
        int[] pills = game.getActivePillsIndices();
        MOVE bestMove = MOVE.NEUTRAL;
        double maxScore = Double.NEGATIVE_INFINITY;

        for (MOVE move : game.getPossibleMoves(currentPacmanIndex)) {
            double score = 0;
            for (int pillIndex : pills) {
                double distance = game.getDistance(currentPacmanIndex, pillIndex, move, Constants.DM.PATH);
                score += 1.0 / (distance + 1); // Mayor puntuación para pastillas cercanas
            }
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        return bestMove != MOVE.NEUTRAL ? bestMove : game.getPacmanLastMoveMade(); // Si no se encuentra un mejor
                                                                                   // movimiento, continúa en la
                                                                                   // dirección actual
    }

    private MOVE moveToClosestEdibleGhost(Game game) {
        // Estrategia mejorada para cazar fantasmas comestibles
        if (game.getNumberOfActivePowerPills() > 0 && anyEdibleGhost(game)) {
            return chaseEdibleGhosts(game, game.getPacmanCurrentNodeIndex());
        }
        return MOVE.NEUTRAL;
    }

    private MOVE chaseEdibleGhosts(Game game, int currentPacmanIndex) {
        int minDistance = Integer.MAX_VALUE;
        GHOST minGhost = null;

        for (GHOST ghost : GHOST.values()) {
            if (game.getGhostEdibleTime(ghost) > 0) {
                int distance = game.getShortestPathDistance(currentPacmanIndex, game.getGhostCurrentNodeIndex(ghost));
                if (distance < minDistance) {
                    minDistance = distance;
                    minGhost = ghost;
                }
            }
        }

        if (minGhost != null) {
            return game.getNextMoveTowardsTarget(currentPacmanIndex, game.getGhostCurrentNodeIndex(minGhost),
                    Constants.DM.PATH);
        }
        return MOVE.NEUTRAL;
    }

    // Mejora en la búsqueda de power pills y fantasmas
    private MOVE moveToClosestPowerPill(Game game, int currentPacmanIndex) {
        // Si hay power pills disponibles, encuentra la más cercana
        if (game.getNumberOfActivePowerPills() > 0) {
            int[] powerPills = game.getActivePowerPillsIndices();
            int closestPowerPillIndex = game.getClosestNodeIndexFromNodeIndex(currentPacmanIndex, powerPills,
                    Constants.DM.PATH);
            if (closestPowerPillIndex != -1) {
                return game.getNextMoveTowardsTarget(currentPacmanIndex, closestPowerPillIndex, Constants.DM.PATH);
            }
        }
        return MOVE.NEUTRAL; // Si no hay power pills, no se mueve hacia ellas
    }
}