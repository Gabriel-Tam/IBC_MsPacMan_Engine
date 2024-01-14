package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public final class Pacman8 extends PacmanController {

    @Override
    public MOVE getMove(Game game, long timeDue) {
        int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
        GHOST[] ghostTypes = GHOST.values();

        // Obtener fantasmas comestibles y no comestibles más cercanos
        GHOST ghostCercanoComestible = obtenerFantasmaCercanoComestible(game, ghostTypes);
        GHOST ghostCercanoNoComestible = obtenerFantasmaCercanoNoComestible(game, ghostTypes);

        // Tomar decisiones basadas en la presencia de fantasmas comestibles
        if (ghostCercanoComestible != null) {
            // Huir de los fantasmas comestibles
            return game.getNextMoveAwayFromTarget(pacmanNodeIndex, game.getGhostCurrentNodeIndex(ghostCercanoComestible), game.getPacmanLastMoveMade(), DM.PATH);
        } else if (ghostCercanoNoComestible != null) {
            // Ir tras los fantasmas no comestibles
            System.out.println("Fantasma NO comestible cercano: " + ghostCercanoNoComestible.name());
            return game.getNextMoveTowardsTarget(pacmanNodeIndex, game.getGhostCurrentNodeIndex(ghostCercanoNoComestible), game.getPacmanLastMoveMade(), DM.PATH);
        } else {
            // No hay fantasmas, hacer una elección aleatoria
            return MOVE.values()[(int) (Math.random() * MOVE.values().length)];
        }
    }

    // Método auxiliar para obtener el tipo de fantasma comestible más cercano
    private GHOST obtenerFantasmaCercanoComestible(Game game, GHOST[] ghostTypes) {
        int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
        GHOST ghostCercano = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (GHOST ghostType : ghostTypes) {
            if (game.isGhostEdible(ghostType)) {
                Integer ghostNodeIndex = game.getGhostCurrentNodeIndex(ghostType);

                // Verificar si el índice del nodo del fantasma es no nulo y válido
                if (ghostNodeIndex != null && ghostNodeIndex >= 0 && ghostNodeIndex < game.getNumberOfNodes()) {
                    int distancia = game.getShortestPathDistance(pacmanNodeIndex, ghostNodeIndex);
                    if (distancia != -1 && distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        ghostCercano = ghostType;
                    }
                }
            }
        }

        return ghostCercano;
    }

    // Método auxiliar para obtener el tipo de fantasma no comestible más cercano
    private GHOST obtenerFantasmaCercanoNoComestible(Game game, GHOST[] ghostTypes) {
        int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
        GHOST ghostCercano = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (GHOST ghostType : ghostTypes) {
            if (!game.isGhostEdible(ghostType)) {
                Integer ghostNodeIndex = game.getGhostCurrentNodeIndex(ghostType);

                // Verificar si el índice del nodo del fantasma es no nulo y válido
                if (ghostNodeIndex != null && ghostNodeIndex >= 0 && ghostNodeIndex < game.getNumberOfNodes()) {
                    int distancia = game.getShortestPathDistance(pacmanNodeIndex, ghostNodeIndex);
                    if (distancia != -1 && distancia < distanciaMinima) {
                        distanciaMinima = distancia;
                        ghostCercano = ghostType;
                    }
                }
            }
        }

        return ghostCercano;
    }
}
