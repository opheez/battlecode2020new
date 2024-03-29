package landscapers;
import battlecode.common.*;

public class Landscaper extends Unit {
	
	boolean stationed = false;

    public Landscaper(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
            if(rc.canDigDirt(dirtohq)){
                rc.digDirt(dirtohq);
            }
        }

        if(rc.getDirtCarrying() == 0){
            tryDig();
        }
        
        //rotate around HQ until you can stand in north, east, south, or west of it. if near hq
        
        if(turnCount >= 200 && rc.getLocation().distanceSquaredTo(hqLoc) < 9) {
        	buildWall(true);
        }else if (!stationed && hqLoc != null && hqLoc.isWithinDistanceSquared(rc.getLocation(), 9)) {
        	for(Direction dir: Util.cornerDirections) {
                if(rc.getLocation().equals(hqLoc.add(dir))) {
                	stationed = true;
                	System.out.println("im stationed");
                }
            }
            if (!stationed) {
            	MapLocation corner = null;
            	for(Direction dir: Util.cornerDirections) {
            		if (rc.senseRobotAtLocation(hqLoc.add(dir)) == null ||
            				rc.senseRobotAtLocation(hqLoc.add(dir)).getType() != RobotType.LANDSCAPER){
            			corner = hqLoc.add(dir);
            			break;
            		}
            	}
            	
            	if(corner != null) {
            		if(nav.rotateTo(corner))
            			System.out.println("going to corner " + corner);
            	}else { //station outside of ring TODO: make so they go on diff sides
            		if(nav.rotateTo(hqLoc) && rc.getLocation().isWithinDistanceSquared(hqLoc, 4)) {
            			stationed = true;
            			System.out.println("gonna try to station at hq");
            		}
            	}
            	/* OLD CODE - rotates around the HQ. change hq.withinDistance to 4 if use!!
            	 * 
	            Direction hqDir = rc.getLocation().directionTo(hqLoc);
	        	Direction[] toTry = {hqDir, hqDir.rotateRight().rotateRight(), hqDir.rotateRight()};
	            for (Direction d : toTry){
	            	System.out.println("test location " + rc.getLocation().add(d));
	                if(nav.tryMove(d)) {
	                	System.out.println("rotating around the hq");
            	 */
            }
        }else if (stationed) {
            buildWall(false);
        }else {
	        // otherwise try to get to the hq
	        if(hqLoc != null){
	            if(nav.goTo(hqLoc))
	            	System.out.println("going to hq");
	        } else {
	            nav.goTo(Util.randomDirection());
	            System.out.println("tryna move random");
	        }
        }

    }

    boolean tryDig() throws GameActionException {
        Direction dir;
        if(hqLoc == null){
            dir = Util.randomDirection();
        } else {
            dir = hqLoc.directionTo(rc.getLocation());
        }
        if(rc.canDigDirt(dir)){
            rc.digDirt(dir);
            rc.setIndicatorDot(rc.getLocation().add(dir), 255, 0, 0);
            return true;
        }else {
            Direction[] toTry = {dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
            for (Direction d : toTry){
                if(rc.canDigDirt(d)) {
                	rc.digDirt(d);
                    return true;
                }
            }
        }
        return false;
    }
    
    void buildWall(boolean move) throws GameActionException{
    	MapLocation bestPlaceToBuildWall = null;
    	MapLocation closebestPlaceToBuildWall = null;

    	int distance = 4;
    	if (move)
    		distance = 9;
        // find best place to build
        if(hqLoc != null) {
            int lowestElevation = 9999999;
            int closelowestElevation = 9999999;
            for (Direction dir : Util.directions) {
                MapLocation tileToCheck = hqLoc.add(dir);
                if(rc.getLocation().distanceSquaredTo(tileToCheck) < distance
                        && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                    if (rc.senseElevation(tileToCheck) < lowestElevation) {
                        lowestElevation = rc.senseElevation(tileToCheck);
                        bestPlaceToBuildWall = tileToCheck;
                        System.out.println("found place to build: " + bestPlaceToBuildWall);
                    }
                }
                MapLocation closetileToCheck = hqLoc.add(dir);
                if(rc.getLocation().distanceSquaredTo(closetileToCheck) < 4
                        && rc.canDepositDirt(rc.getLocation().directionTo(closetileToCheck))) {
                    if (rc.senseElevation(closetileToCheck) < closelowestElevation) {
                        closelowestElevation = rc.senseElevation(closetileToCheck);
                        closebestPlaceToBuildWall = closetileToCheck;
                        System.out.println("found place to build: " + closebestPlaceToBuildWall);
                    }
                }
            }
        }
        // build the wall
        if (bestPlaceToBuildWall != null) {
            if (move && rc.getLocation().distanceSquaredTo(bestPlaceToBuildWall) >= 4) {
            	if(nav.tryMove(rc.getLocation().directionTo(bestPlaceToBuildWall)))
            		System.out.println("move for build to " + rc.getLocation().directionTo(bestPlaceToBuildWall));
            	else rc.depositDirt(rc.getLocation().directionTo(closebestPlaceToBuildWall));
            }else {
            	rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                System.out.println("building a wall");

            }
        }

    }
    
}