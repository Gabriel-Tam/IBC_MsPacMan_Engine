
package TheVoids2024.Gabo.Pacmans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;

public class Pacman6 extends PacmanController {
   private int SECURITY_DISTANCE = 25;
   private int powerPillDistanceLimit = 70;
   private int initialGhostDistanceLimit = 60;

   public Pacman6() {
   }

   public Constants.MOVE getMove(Game game, long timeDue) {
      if (game.isJunction(game.getPacmanCurrentNodeIndex())) {
         List<Constants.GHOST> g = this.getListOfAllGhosts(this.initialGhostDistanceLimit, game, game.getPacmanCurrentNodeIndex());
         Constants.MOVE m;
         if (g.size() > 2 && this.allOut(game)) {
            m = this.gotoPowerPill(game, g);
            if (m != null) {
               return m;
            }
         }

         m = this.runAway(game);
         if (m != null) {
            return m;
         } else {
            int ghostToAttack = this.isPossibleToEat(game);
            return ghostToAttack != -1 ? game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), ghostToAttack, game.getPacmanLastMoveMade(), DM.PATH) : this.getBetterPillPath(game);
         }
      } else {
         return null;
      }
   }

   List<Constants.GHOST> getListOfAllGhosts(int limit, Game game, int pacman) {
      List<Constants.GHOST> nearGhosts = new ArrayList();
      Constants.GHOST[] var8;
      int var7 = (var8 = GHOST.values()).length;

      for(int var6 = 0; var6 < var7; ++var6) {
         Constants.GHOST ghostType = var8[var6];
         int ghostNode = game.getGhostCurrentNodeIndex(ghostType);
         if (game.getGhostLairTime(ghostType) <= 0 && game.getDistance(ghostNode, pacman, game.getGhostLastMoveMade(ghostType), DM.PATH) <= (double)limit && !game.isGhostEdible(ghostType)) {
            nearGhosts.add(ghostType);
         }
      }

      return nearGhosts;
   }

   private Constants.MOVE gotoPowerPill(Game g, List<Constants.GHOST> nearGhosts) {
      int pacman = g.getPacmanCurrentNodeIndex();
      int[] powerpills = g.getActivePowerPillsIndices();
      if (powerpills.length > 0) {
         int nearestPowerPill = -1;
         double distance = Double.MAX_VALUE;

         for(int i = 0; i < powerpills.length; ++i) {
            double temp = g.getDistance(pacman, powerpills[i], g.getPacmanLastMoveMade(), DM.PATH);
            if (temp < distance) {
               distance = temp;
               nearestPowerPill = powerpills[i];
            }
         }

         if (distance < (double)this.powerPillDistanceLimit) {
            Constants.MOVE m = g.getApproximateNextMoveTowardsTarget(pacman, nearestPowerPill, g.getPacmanLastMoveMade(), DM.PATH);
            int nextIndex = g.getNeighbour(pacman, m);
            Iterator var11 = nearGhosts.iterator();

            Constants.GHOST ghostType;
            do {
               if (!var11.hasNext()) {
                  return m;
               }

               ghostType = (Constants.GHOST)var11.next();
            } while(!(g.getDistance(pacman, nearestPowerPill, g.getPacmanLastMoveMade(), DM.PATH) > g.getDistance(g.getGhostCurrentNodeIndex(ghostType), nearestPowerPill, g.getGhostLastMoveMade(ghostType), DM.PATH)) || !(g.getDistance(g.getGhostCurrentNodeIndex(ghostType), pacman, g.getGhostLastMoveMade(ghostType), DM.PATH) > g.getDistance(g.getGhostCurrentNodeIndex(ghostType), nextIndex, g.getGhostLastMoveMade(ghostType), DM.PATH)));

            return null;
         }
      }

      return null;
   }

   private boolean allOut(Game g) {
      Constants.GHOST[] var5;
      int var4 = (var5 = GHOST.values()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Constants.GHOST ghostType = var5[var3];
         if (g.getGhostLairTime(ghostType) > 0) {
            return false;
         }
      }

      return true;
   }

   private Constants.MOVE runAway(Game game) {
      int pacman = game.getPacmanCurrentNodeIndex();
      int distance = this.SECURITY_DISTANCE;
      if (!game.isJunction(pacman)) {
         return null;
      } else {
         List<Constants.GHOST> nearGhosts = new ArrayList();
         Constants.GHOST[] var8;
         int var7 = (var8 = GHOST.values()).length;

         int distance_max;
         int j;
         for(distance_max = 0; distance_max < var7; ++distance_max) {
            Constants.GHOST ghostType = var8[distance_max];
            j = game.getGhostCurrentNodeIndex(ghostType);
            if (game.getGhostLairTime(ghostType) <= 0 && game.getDistance(j, pacman, game.getGhostLastMoveMade(ghostType), DM.PATH) <= (double)distance && game.getDistance(pacman, j, game.getPacmanLastMoveMade(), DM.PATH) <= (double)distance && !game.isGhostEdible(ghostType)) {
               nearGhosts.add(ghostType);
            }
         }

         if (nearGhosts.size() <= 1) {
            return nearGhosts.size() == 1 ? game.getApproximateNextMoveAwayFromTarget(pacman, game.getGhostCurrentNodeIndex((Constants.GHOST)nearGhosts.get(0)), game.getPacmanLastMoveMade(), DM.PATH) : null;
         } else {
            int maxPills = -1;
            distance_max = 0;
            int[] posibleIndices = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
            int furthestIndex = -1;

            for(j = 0; j < posibleIndices.length; ++j) {
               int tempDistance = 0;

               Constants.GHOST i;
               for(Iterator var12 = nearGhosts.iterator(); var12.hasNext(); tempDistance = (int)((double)tempDistance + game.getDistance(game.getGhostCurrentNodeIndex(i), posibleIndices[j], game.getGhostLastMoveMade(i), DM.PATH))) {
                  i = (Constants.GHOST)var12.next();
               }

               if (!this.isThereGhost(game, posibleIndices[j], game.getMoveToMakeToReachDirectNeighbour(pacman, posibleIndices[j]), nearGhosts) && (tempDistance > distance_max || tempDistance == distance_max && maxPills < this.pillsUntilNextJunction(posibleIndices[j], game.getMoveToMakeToReachDirectNeighbour(pacman, posibleIndices[j]), game, 0))) {
                  distance_max = tempDistance;
                  furthestIndex = posibleIndices[j];
                  maxPills = this.pillsUntilNextJunction(posibleIndices[j], game.getMoveToMakeToReachDirectNeighbour(pacman, posibleIndices[j]), game, 0);
               }
            }

            return game.getMoveToMakeToReachDirectNeighbour(pacman, furthestIndex);
         }
      }
   }

   boolean isThereGhost(Game game, int index, Constants.MOVE movement, List<Constants.GHOST> nearGhosts) {
      int nextIndex = index;
      int[] indexes = game.getNeighbouringNodes(index, movement);
      if (game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != movement && indexes.length == 1) {
         movement = game.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
      }

      Constants.GHOST ghostType;
      while(indexes.length == 1) {
         nextIndex = indexes[0];
         indexes = game.getNeighbouringNodes(nextIndex, movement);
         if (game.getMoveToMakeToReachDirectNeighbour(nextIndex, indexes[0]) != movement && indexes.length == 1) {
            movement = game.getMoveToMakeToReachDirectNeighbour(nextIndex, indexes[0]);
         }

         Iterator var8 = nearGhosts.iterator();

         while(var8.hasNext()) {
            ghostType = (Constants.GHOST)var8.next();
            if (game.getGhostCurrentNodeIndex(ghostType) == nextIndex && game.getDistance(game.getGhostCurrentNodeIndex(ghostType), index, game.getGhostLastMoveMade(ghostType), DM.PATH) <= game.getDistance(index, nextIndex, game.getPacmanLastMoveMade(), DM.PATH)) {
               return true;
            }
         }
      }

      Constants.GHOST[] var10;
      int var9 = (var10 = GHOST.values()).length;

      for(int var12 = 0; var12 < var9; ++var12) {
         ghostType = var10[var12];
         if (!game.isGhostEdible(ghostType) && game.getGhostLairTime(ghostType) == 0) {
            int ghostnextJunction = this.getIndexOfNextGhostJunction(game, game.getGhostCurrentNodeIndex(ghostType), ghostType);
            if (ghostnextJunction == nextIndex && game.getDistance(game.getGhostCurrentNodeIndex(ghostType), ghostnextJunction, game.getGhostLastMoveMade(ghostType), DM.PATH) < game.getDistance(index, ghostnextJunction, movement, DM.PATH)) {
               return true;
            }
         }
      }

      return false;
   }

   public int isPossibleToEat(Game game) {
      int msPacmanNode = game.getPacmanCurrentNodeIndex();
      int closestGhost = -1;
      double closestT = Double.MAX_VALUE;
      Constants.GHOST[] var9;
      int var8 = (var9 = GHOST.values()).length;

      for(int var7 = 0; var7 < var8; ++var7) {
         Constants.GHOST ghost = var9[var7];
         if (game.getGhostEdibleTime(ghost) > 0) {
            int distance = game.getShortestPathDistance(msPacmanNode, game.getGhostCurrentNodeIndex(ghost), game.getPacmanLastMoveMade());
            double sB = (double)distance;
            double t = sB * 3.0 / 2.0;
            if (!(t > (double)game.getGhostEdibleTime(ghost)) && t < closestT) {
               closestGhost = game.getGhostCurrentNodeIndex(ghost);
               closestT = t;
            }
         }
      }

      return closestGhost;
   }

   private Constants.MOVE getBetterPillPath(Game game) {
      List<Constants.MOVE> movesDone = new ArrayList();
      int pacman = game.getPacmanCurrentNodeIndex();
      if (!game.isJunction(pacman)) {
         return null;
      } else {
         int[] nodes = game.getNeighbouringNodes(pacman, game.getPacmanLastMoveMade());
         Map<Constants.MOVE, Integer> results = new HashMap();
         int[] var9 = nodes;
         int closestPill = nodes.length;

         int bestScore;
         for(bestScore = 0; bestScore < closestPill; ++bestScore) {
            int node = var9[bestScore];
            Constants.MOVE dir = game.getNextMoveTowardsTarget(pacman, node, DM.PATH);
            movesDone.add(dir);
            results.put(dir, this.pillsUntilNextJunction(node, dir, game, 0));
         }

         Constants.MOVE finalMove = null;
         bestScore = 0;
         Iterator var13 = movesDone.iterator();

         while(var13.hasNext()) {
            Constants.MOVE m = (Constants.MOVE)var13.next();
            if (results.get(m) != null && (Integer)results.get(m) > bestScore) {
               bestScore = (Integer)results.get(m);
               finalMove = m;
            }
         }

         if (bestScore == 0) {
            closestPill = this.getNearestPillIndex(game);
            finalMove = game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), closestPill, game.getPacmanLastMoveMade(), DM.PATH);
         }

         return finalMove;
      }
   }

   private Integer getNearestPillIndex(Game game) {
      Integer nearestPill = null;
      double closestDistance = -1.0;
      int[] var8;
      int var7 = (var8 = game.getActivePillsIndices()).length;

      for(int var6 = 0; var6 < var7; ++var6) {
         int pillIndex = var8[var6];
         double dist = (double)game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pillIndex, game.getPacmanLastMoveMade());
         if (nearestPill == null || dist < closestDistance) {
            nearestPill = pillIndex;
            closestDistance = dist;
         }
      }

      return nearestPill;
   }

   private int pillsUntilNextJunction(int nodeIndex, Constants.MOVE direction, Game game, int result) {
      int res = result;
      if (game.isJunction(nodeIndex)) {
         return result;
      } else {
         int[] activePills = game.getActivePillsIndices();
         int[] powerpills = game.getActivePowerPillsIndices();
         boolean found = false;
         int idx = 0;

         while(!found && idx < activePills.length) {
            if (activePills[idx] == nodeIndex) {
               found = true;
            } else {
               ++idx;
            }
         }

         if (found) {
            res = result + 1;
         }

         idx = 0;

         while(!found && idx < powerpills.length) {
            if (powerpills[idx] == nodeIndex) {
               found = true;
            } else {
               ++idx;
            }
         }

         if (found) {
            res += 5;
         }

         int[] nodes = game.getNeighbouringNodes(nodeIndex, direction);
         Constants.MOVE newDir = game.getMoveToMakeToReachDirectNeighbour(nodeIndex, nodes[0]);
         return this.pillsUntilNextJunction(nodes[0], newDir, game, res);
      }
   }

   public int getIndexOfNextGhostJunction(Game g, int ghost, Constants.GHOST ghostType) {
      int index = ghost;
      if (!g.isJunction(ghost)) {
         Constants.MOVE ghost_movement = g.getGhostLastMoveMade(ghostType);
         int[] indexes = g.getNeighbouringNodes(ghost, ghost_movement);
         if (g.getMoveToMakeToReachDirectNeighbour(ghost, indexes[0]) != ghost_movement && indexes.length == 1) {
            ghost_movement = g.getMoveToMakeToReachDirectNeighbour(ghost, indexes[0]);
         }

         while(indexes.length == 1) {
            index = indexes[0];
            indexes = g.getNeighbouringNodes(index, ghost_movement);
            if (g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]) != ghost_movement && indexes.length == 1) {
               ghost_movement = g.getMoveToMakeToReachDirectNeighbour(index, indexes[0]);
            }
         }
      }

      return index;
   }


}
