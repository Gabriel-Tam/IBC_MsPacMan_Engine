package TheVoids2024.Gabo.Pacmans;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;

/*
 * Class MsPacMan that implements "Fake Injury" behavior
 */
public final class BritoPacman extends PacmanController {
	//SHORTCUTS TO VITAL INFO ABOUT THE GAME
	//Engine stuff
	private Random rnd = new Random();
	private int tickCount = 1;
	private long highestPacmanDecisionTime = 0;
	private long pacmanStartTime = System.nanoTime();
	private long pacmanPrevStartTime = System.nanoTime();
	private GHOST[] allGhosts = GHOST.values();
	//Pacman info
	private int pacmanLocation;
	private MOVE pacmanLastMove;
	private int pacmanPowerTime;
	//Enemy info
	private ArrayList<GHOST> edibleGhosts;
	private ArrayList<Integer> edibleGhostsLocations;
	private ArrayList<Integer> edibleGhostsDistances;
	private ArrayList<GHOST> nonEdibleGhosts;
	private ArrayList<Integer> nonEdibleGhostsLocations;
	private ArrayList<Integer> nonEdibleGhostsDistances;
	//Map info
	private int [] remPPillsLocations;
	private ArrayList<Integer> remPPillsDistances;
	//Navigation info
	private MOVE[] pcMoves;
	
    
    private void getEdibleGhostsInfo(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	ArrayList<Integer> locations = new ArrayList<Integer>();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for(GHOST ghost : this.allGhosts) {
    		if (game.isGhostEdible(ghost)) {
    			int location = game.getGhostCurrentNodeIndex(ghost);
    			ghosts.add(ghost);
    			locations.add(location);
    		}
    	}
    	this.edibleGhosts = ghosts;
    	this.edibleGhostsLocations = locations;
    	//this.edibleGhostsDistances = distances;
    }
    
    private void getNonEdibleGhostsInfo(Game game) {
    	ArrayList<GHOST> ghosts = new ArrayList<GHOST>();
    	ArrayList<Integer> locations = new ArrayList<Integer>();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for(GHOST ghost : this.allGhosts) {
    		if (!game.isGhostEdible(ghost)) {
    			int location = game.getGhostCurrentNodeIndex(ghost);
    			//int distance = game.getShortestPathDistance(location, this.pacmanLocation, game.getGhostLastMoveMade(ghost));
    			ghosts.add(ghost);
    			locations.add(location);
    			//distances.add(distance);
    		}
    	}
    	this.nonEdibleGhosts = ghosts;
    	this.nonEdibleGhostsLocations = locations;
    } 
    
    private void updateGameInfo(Game game) {
    	this.tickCount += 1;
    	this.pacmanPrevStartTime = this.pacmanStartTime;
    	this.pacmanStartTime = System.nanoTime();

    	this.pacmanLocation = game.getPacmanCurrentNodeIndex();
    	this.pacmanLastMove = game.getPacmanLastMoveMade();
    	if (this.pacmanLastMove==MOVE.NEUTRAL) this.pacmanLastMove = MOVE.LEFT; //Quick fix
    	
    	this.getEdibleGhostsInfo(game);
    	this.getNonEdibleGhostsInfo(game);
    	
    	if (this.edibleGhosts.size() > 0) this.pacmanPowerTime = game.getGhostEdibleTime(edibleGhosts.get(0));
    	else this.pacmanPowerTime = 0;
    	
    	this.remPPillsLocations = game.getActivePowerPillsIndices();
    	ArrayList<Integer> distances = new ArrayList<Integer>();
    	for (int pPillLocation: this.remPPillsLocations) distances.add(game.getShortestPathDistance(this.pacmanLocation, pPillLocation, this.pacmanLastMove));
    	this.remPPillsDistances = distances;
    	
    	this.pcMoves = game.getPossibleMoves(this.pacmanLocation, this.pacmanLastMove);
    }
    
    private void printTickInfo() {
    	long pacmanDecisionTime = System.nanoTime() - this.pacmanStartTime;
    	long tickTime = this.pacmanStartTime - this.pacmanPrevStartTime;
    	if (pacmanDecisionTime > this.highestPacmanDecisionTime) this.highestPacmanDecisionTime = pacmanDecisionTime;
    	//System.out.println("Tick: " + this.tickCount + " -  Pacman Decision Time: " + pacmanDecisionTime/1000 + " us -  TickTime: " + tickTime/1000000 + " ms - High. Time: " + this.highestPacmanDecisionTime/1000 + " us");
    }
    
    @Override
    public MOVE getMove(Game game, long timeDue) {    	
    	//Update game info
    	this.updateGameInfo(game);
    	
    	//PACMAN MAIN LOGIC
    	MOVE move;
    	if(this.pacmanPowerTime > 0) move = getMoveToPursueGhosts(game);
    	else if (remPPillsLocations.length > 0) move = getMoveToGoToPowerPill(game);
    	else move = getMoveToEatRemPills(game); //Eat remaining pills

    	//Print tick info
    	this.printTickInfo();
    	
    	return move;
    }
    
    private int getNearestPPillLocation(Game game) {
    	//if(this.tickCount % 5 == 0) System.out.print("GO TO PILL - ");
    	//Get nearest power pill location
		int nearestPPillLocation = -1;
		int nearestPPillDistance = 10000;
		for(int pPillLocation: this.remPPillsLocations) {
			int pPillDistance = game.getShortestPathDistance(this.pacmanLocation, pPillLocation, this.pacmanLastMove);
			if(pPillDistance < nearestPPillDistance) {
				nearestPPillLocation = pPillLocation;
				nearestPPillDistance = pPillDistance;
			}
		}
		return nearestPPillLocation;
    }
    
    private int getNearestEdibleGhostLocation(Game game) {
    	//if(tickCount % 5 == 0) System.out.print("PURSUE GHOSTS - ");
		//Get nearest edible ghost location
		int nearestGhostLocation = -1;
		int nearestGhostDistance = 10000;
		for(Integer integerGhostLocation: this.edibleGhostsLocations) {
			int ghostLocation = integerGhostLocation.intValue();
			int ghostDistance = game.getShortestPathDistance(this.pacmanLocation, ghostLocation, this.pacmanLastMove);
			if (ghostDistance < nearestGhostDistance) {
				nearestGhostLocation = ghostLocation;
				nearestGhostDistance = ghostDistance;
			}
		}
		return nearestGhostLocation;
    }
    
    private MOVE getMoveToEatRemPills(Game game) {
    	MOVE nextMove;
    	int[] remPills = game.getActivePillsIndices();
    	int destination = remPills[rnd.nextInt(remPills.length)];
    	nextMove = getDefaultMoveToLocation(game, destination);
    	return nextMove;
    }
    
    private MOVE getMoveToPursueGhosts(Game game) {
    	MOVE nextMove;
    	int destination = getNearestEdibleGhostLocation(game);
    	
    	if(game.isJunction(this.pacmanLocation)) {
    		int[] metrics = calculatePathMetrics(game, this.pacmanLocation, destination, this.pacmanLastMove);
    		if (metrics[0]==1) {
    			nextMove = getMoveToEvadeGhost(game, destination);
    		}else {
    			nextMove = getDefaultMoveToLocation(game, destination); //game.getNextMoveTowardsTarget(this.pacmanLocation, destination, this.pacmanLastMove, Constants.DM.EUCLID);
    		}
    	} else {
    		nextMove = getDefaultMoveToLocation(game, destination); //game.getNextMoveTowardsTarget(this.pacmanLocation, destination, this.pacmanLastMove, Constants.DM.EUCLID);
    	}
    	return nextMove;
    }
    
    private MOVE getMoveToGoToPowerPill(Game game) {
    	MOVE nextMove;
    	int destination = getNearestPPillLocation(game);
    	
    	if(game.isJunction(this.pacmanLocation)) {
    		int[] metrics = calculatePathMetrics(game, this.pacmanLocation, destination, this.pacmanLastMove);
    		boolean isSafeToManeuver = calcIfPacmanIsSafeToManuever(game, destination);
    		if (metrics[0]==1 || isSafeToManeuver) {
    			nextMove = getMoveToEvadeGhost(game, destination);
    		}else {
    			nextMove = getDefaultMoveToLocation(game, destination); //game.getNextMoveTowardsTarget(this.pacmanLocation, destination, this.pacmanLastMove, Constants.DM.EUCLID);
    		}
    	} else {
    		nextMove = getDefaultMoveToLocation(game, destination); //game.getNextMoveTowardsTarget(this.pacmanLocation, destination, this.pacmanLastMove, Constants.DM.EUCLID);
    	}
    	return nextMove;
    }
    
    private MOVE getDefaultMoveToLocation(Game game, int destination) {
    	int[] path = game.getShortestPath(this.pacmanLocation, destination, this.pacmanLastMove);
    	return game.getMoveToMakeToReachDirectNeighbour(this.pacmanLocation, path[0]);
    }
    
    private boolean calcIfPacmanIsSafeToManuever(Game game, int objective) {
    	double safeGOToPORatio = 1.42;
    	double lowestGOToPORatio = 10.0;
    	double safeGO = 40; //10 pills
    	double lowestGO = 1000;
    	
    	int pacmanX = game.getNodeXCood(this.pacmanLocation); int pacmanY = game.getNodeYCood(this.pacmanLocation);
    	int objX = game.getNodeXCood(objective); int objY = game.getNodeYCood(objective);
    	double PO = Math.sqrt(Math.pow(pacmanX - objX, 2) + Math.pow(pacmanY - objY, 2));
    	
    	//System.out.print("GO TO PO RATIOS: ");
    	
    	for(GHOST ghost: this.nonEdibleGhosts) {
    		int ghostLocation = game.getGhostCurrentNodeIndex(ghost);
    		int ghostX = game.getNodeXCood(ghostLocation); int ghostY = game.getNodeYCood(ghostLocation);
    		double GO = Math.sqrt(Math.pow(ghostX - objX, 2) + Math.pow(ghostY - objY, 2));
    		double currentGOToPORatio = GO/PO;
    		//System.out.print(currentGOToPORatio + " - ");
    		if (currentGOToPORatio < lowestGOToPORatio) lowestGOToPORatio = currentGOToPORatio;
    		if (GO < lowestGO) lowestGO = GO;
    	}
    	//System.out.println();
    	if (lowestGOToPORatio < safeGOToPORatio || lowestGO < safeGO) return false;
    	else return true;
    }
    
    private MOVE getMoveToEvadeGhost(Game game, int destination) {
    	//For each move MsPacman can make, calculate metrics about its path
    	ArrayList<int[]> allMetrics = new ArrayList<int[]>();
    	for(MOVE move: this.pcMoves) {
    		int futurePacmanLocation = game.getNeighbour(this.pacmanLocation, move); //Advance one move to force different path finding
    		int[] metrics = calculatePathMetrics(game, futurePacmanLocation, destination, move);
    		allMetrics.add(metrics);
    	}
    	//printAllPathsMetrics(allMetrics);
    	//Select best move to make given alternate paths metrics
    	//Ranking criteria: No collision, highest score, longer collision distance  
    	//First phase, detect paths without collision for further analysis
    	ArrayList<int[]> nonCollisionMetrics = new ArrayList<int[]>();
    	ArrayList<MOVE> nonCollisionMoves = new ArrayList<MOVE>();
    	for(int i=0; i<allMetrics.size(); i++) {
    		int collition = allMetrics.get(i)[0];
    		if (collition == 0) {
    			nonCollisionMetrics.add(allMetrics.get(i));
    			nonCollisionMoves.add(this.pcMoves[i]);
    		}
    	}
    	//First ranking criteria. Evaluate scores for NON COLLISION paths (if there is any)
    	if(nonCollisionMetrics.size()>0) {
    		int highestScore = -1;
    		MOVE highestScoreMove = null;
    		for(int i=0; i<nonCollisionMetrics.size(); i++) {
    			int currentScore = nonCollisionMetrics.get(i)[2];
    			if (currentScore > highestScore) {
    				highestScore = currentScore;
    				highestScoreMove = nonCollisionMoves.get(i);
    			}
    		}
    		return highestScoreMove;
    	//Second ranking criteria. Evaluate scores for COLLISION paths
    	} else {
    		int highestScore = -1;
    		MOVE highestScoreMove = null;
    		for(int i=0; i<allMetrics.size(); i++) {
    			int currentScore = allMetrics.get(i)[2];
    			if (currentScore > highestScore) {
    				highestScore = currentScore;
    				highestScoreMove = this.pcMoves[i];
    			}
    		}
    		return highestScoreMove;
    	}
    }
    
    private int[] calculatePathMetrics(Game game, int location, int destination, MOVE lastMoveMade) {
    	//Prevention range modulates area in which MsPacman will try to detect collisions
    	int prevRangeStart = 1; int prevRangeEnd = 40;
    	
    	//Calculate future info useful during collision detection 
    	//1) initialScore (if pacman stepped into a pill when taking a turn)
    	int initialScore = 0;
    	if(game.getPillIndex(location) != -1) initialScore = 10;
    	//2) Future ghosts locations (Used when detecting collisions between junctions)
    	ArrayList<Integer> nonEdibleGhostsFutureLocations = getGhostsFutureLocations(game);
    	
    	//LOGIC
    	//If future location was already occupied or will be occupied by ghost, collision metrics are already known
    	for(int nonEdibleGhostLocation: this.nonEdibleGhostsLocations) {
    		if (nonEdibleGhostLocation==1292) continue; //Do not take in account ghost at lair
    		if (location==nonEdibleGhostLocation) { int[] metrics = {1, 0, 0}; return metrics;}
    	}
    	for(int nonEdibleGhostLocation: nonEdibleGhostsFutureLocations) {
    		if (location==nonEdibleGhostLocation) { int[] metrics = {1, 0, 0}; return metrics;}
    	}	
    	
    	//Detects collisions near junctions, calculates collision distances and max score possible in path to objective
    	//For each node in path to objective (distGO is distance from Pacman to node (a.k.a partial Objective))...
    	int[] path = game.getShortestPath(location, destination, lastMoveMade);
    	for(int distPO=prevRangeStart - 1; distPO < path.length && distPO < prevRangeEnd; distPO++) {
    		// Special collision case: Ghost is between junctions in the current path to destination
    		for(int i = 0; i < nonEdibleGhostsFutureLocations.size(); i++) {
    			if(path[distPO]==nonEdibleGhostsFutureLocations.get(i).intValue()) {
    				int collisionDist = calcCollisionDistance(distPO, 0);
    				int[] metrics = {1, collisionDist+1, 0}; //+1 to take in account initial position
    				metrics[2] = getPathScoreUntilCollision(game, path, collisionDist, initialScore);
    				return metrics;
    			}
    		}
    		//Normal case: Detect collision with non edible ghosts at maze junctions
    		if(game.isJunction(path[distPO])) {
	    		ArrayList<Integer> collisionDistances = new ArrayList<Integer>();
	    		for(int g = 0; g < nonEdibleGhostsFutureLocations.size(); g++) {
	    			//Calculate future location of the ghost to be more precise with collision detection
	    			MOVE ghostLastMove = game.getGhostLastMoveMade(this.nonEdibleGhosts.get(g));
	    			int ghostFutureLocation = nonEdibleGhostsFutureLocations.get(g).intValue();
	    			//Collision is based on who (pacman or the ghost) gets first to the junction
	    			int distGO = game.getShortestPathDistance(ghostFutureLocation, path[distPO], ghostLastMove);
	    			if ((distGO - 1) >= 0) distGO -= 1; //Give ghost a distance boost
	    			if (distGO <= distPO) collisionDistances.add(calcCollisionDistance(distPO, distPO)); //TODO: Take in account EAT_DISTANCE
	    		}
	    		//If collisions happened, calculate metrics for path (take closest collision in account)
	    		//distance to collision is reduced by 1 to take in account first step boost
	    		if (collisionDistances.size() > 0) {
	    			int minCollisionDistance = Collections.min(collisionDistances);
	    			int score = getPathScoreUntilCollision(game, path, minCollisionDistance, initialScore);
	    			int[] metrics = {1, minCollisionDistance+1, score};
	    			return metrics;
	    		}
    		}
    	}
    	//If code got here, there were no collisions, but metrics are still calculated
		int score = getPathScoreUntilCollision(game, path, prevRangeEnd, initialScore);
		int[] metrics = {0, prevRangeEnd+1, score};
		return metrics;
    }
    
    private ArrayList<Integer> getGhostsFutureLocations(Game game){
    	ArrayList<Integer> nonEdibleGhostsFutureLocations = new ArrayList<Integer>();
    	for (GHOST nonEdibleGhost: this.nonEdibleGhosts) {
    		int ghostLocation = game.getGhostCurrentNodeIndex(nonEdibleGhost);
    		MOVE ghostLastMove = game.getGhostLastMoveMade(nonEdibleGhost);
    		if (ghostLocation == 1292) continue; //Skip locations calculation if ghost is in lair
    		MOVE ghostFutureMove = game.getNextMoveTowardsTarget(ghostLocation, this.pacmanLocation, ghostLastMove, Constants.DM.EUCLID);
    		int ghostFutureLocation = game.getNeighbour(ghostLocation, ghostFutureMove);
    		if (ghostFutureLocation == -1) ghostFutureLocation = ghostLocation;
    		//System.out.println("Ghost: " + nonEdibleGhost + "Loc: " + ghostLocation + "Move: " + ghostLastMove + "Fut. Move: " + ghostFutureMove + "Fut. Loc.: " + ghostFutureLocation);
    		nonEdibleGhostsFutureLocations.add(ghostFutureLocation);
    	}
    	return nonEdibleGhostsFutureLocations;
    }
    
    private int calcCollisionDistance(int PO, int GO) {
    	int dist = GO + (PO-1-GO)/2; //-1 to take in account automatic truncation
    	if (dist < 0) {
    		dist = 0;
    		//System.out.println("WARNING!!!!!!!!!!!   " + GO + "   " + PO + "   " + dist);
    	}
    	return dist;
    }
    
    private int getPathScoreUntilCollision(Game game, int[] path, int collisionDist, int baseScore) {
    	int score = baseScore;
    	int[] activePillsIndices = game.getActivePillsIndices();
    	for(int i = 0; i < collisionDist && i < path.length; i++) {
    		for(int index: activePillsIndices) {
    			if(path[i]==index) {
    				score += 10;
    				break;
    			}
    		}
    	}
    	return score;
    }
    
    private void printAllPathsMetrics(ArrayList<int[]> allMetrics) {
    	for(int i = 0; i < allMetrics.size(); i++) {
    		int[] metrics = allMetrics.get(i);
    		switch(this.pcMoves[i]) {
    			case UP: System.out.print("UP - "); break;
    			case DOWN: System.out.print("DOWN - "); break;
    			case RIGHT: System.out.print("RIGHT - "); break;
    			case LEFT: System.out.print("LEFT - "); break;
    			case NEUTRAL: System.out.print("NEUTRAL"); break;
    		}
    		System.out.print("Collision: " + metrics[0] + "   Distance: " + metrics[1] + "   Score: " + metrics[2] + "\n");
    	}
    }
    
    //The following methods are heuristics. Will be added in the next sprint.
    private int getMostIdealPowerPillLocation(Game game) {
    	return 0;
    }
    
    private int getMostIdealEdibleGhostLocation(Game game) {
    	return 0;
    }
    
}