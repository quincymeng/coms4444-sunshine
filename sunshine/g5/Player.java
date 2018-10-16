package sunshine.g5;

import java.util.List;
import java.util.Collections;
import java.util.*;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math;
import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import java.util.PriorityQueue;

public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    int CollectBales;
    int CarNum;
    double length;
    double range;
    int layers;
    List<Point> bales;
    List<Point> remainingBales;
    List<Double> degres;
    int[] balesdist;
    int[] CarDist;
    int[] CarRDist;
    MutableTrailer[] tlist;
    int outRangeSize;
    int threshHold;
    int outRangeSize2;
    int start;
    int Stcount;
    Map<Integer, List<Integer>> map;
    private static class MutableTrailer implements Trailer
    {
        public Point location;
        public int numBales;

        public MutableTrailer()
        {
            this.location = new Point(0, 0);
            this.numBales = 0;
        }

        public Point getLocation()
        {
            return new Point(location.x, location.y);
        }

        public int getNumBales()
        {
            return numBales;
        }
    }
    public Player() {
        rand = new Random(seed);
    }

    public Point getClosestBale(Tractor tractor, Point farthest)
    {
    	Point closest = new Point(Float.MAX_VALUE, Float.MAX_VALUE);
        for (Point c : bales) {

    		if(remainingBales.contains(c) == false)
    			continue;

            if (calDistance(farthest, c) < calDistance(farthest, closest)){
                closest = c;
            }
        }

        if(remainingBales.contains(closest))
        {
	        remainingBales.remove(closest);
	        return closest;
        }
        else
        	return new Point(0, 0);
    }

    public Point getFarthestBale(Tractor tractor)
    {
    	double max_dist = Float.MIN_VALUE;
    	Point farthest = new Point(0, 0);

    	for(Point p:bales)
    	{
    		if(remainingBales.contains(p) == false)
    			continue;

    		if(calDistance(p) > max_dist)
    		{
    			farthest = p;
    			max_dist = calDistance(p);
    		}
    	}
    	remainingBales.remove(farthest);

    	Point rem1 = new Point(0, 0);
    	Point rem2 = new Point(0, 0);
    	for(int i=0; i<9; i++)
    	{
    		Point temp = getClosestBale(tractor, farthest);
    		if(i == 4 && temp.x != -1)
    			rem1 = temp;
    		if(i == 5 && temp.x != -1)
    			rem2 = temp;
    	}

    	Point final_pos = new Point((rem1.x+rem2.x)/2, (rem1.y+rem2.y)/2);
    	return final_pos;
    }

    public void init(List<Point> bales, int n, double m, double t)
    {
    	if(bales.size()/10 > n*10)
    		threshHold = bales.size()/10;
    	else
    		threshHold = n*10;

        range = 320.0;
        Stcount = 1;
        CarNum  = n ;
        tlist = new MutableTrailer[CarNum];

        for(int i=0;i<CarNum ;i++){
            tlist[i] = new MutableTrailer();
        }

        remainingBales = new ArrayList<>();
        for(Point p: bales){
        	remainingBales.add(p);
        }

        map = new HashMap<>();
        this.bales = bales;
    }

    //cal dist bw 2 pts
    private double calDistance(Point p1, Point p2){
      	return Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    private double calDistance(Point p){
        return Math.sqrt(p.x*p.x + p.y*p.y);
    }

    public Command getCommand(Tractor tractor)
    {
    	System.out.println(bales.size());
    	System.out.println(remainingBales.size());


    	//if tractor at origin
        if(Math.abs(tractor.getLocation().x-0)<1e-2 && Math.abs(tractor.getLocation().y-0)<1e-2){

              if(tractor.getHasBale())  return new Command(CommandType.UNLOAD);

              if(Math.abs(tlist[tractor.getId()].getLocation().x-0)<0.001 && Math.abs(tlist[tractor.getId()].getLocation().y-0)<0.001 && tlist[tractor.getId()].getNumBales()>0){
                  tlist[tractor.getId()].numBales = tlist[tractor.getId()].numBales-1;
                  return new Command(CommandType.UNSTACK);
              }


		    	if(bales.size() == 0)
		    	{
		    		return new Command(CommandType.UNSTACK);
		    	}


                  if(bales.size()<threshHold){

                  		for(int i=0; i<CarNum; i++)
                  			tlist[i].location = new Point(0, 0);

                      if(tractor.getAttachedTrailer()!=null){

                          return new Command(CommandType.DETATCH);

                      }else{
                          Point p = bales.get(0);			//change this to closest bale
                          bales.remove(bales.get(0));	//remove that
                          return Command.createMoveCommand(p);
                      }

                  }


              //if bales on farm
              if(bales.size() > 0){
                  if(tractor.getAttachedTrailer()!=null){
                      if(tractor.getAttachedTrailer().getNumBales()==0){
		              	  Point temp = getFarthestBale(tractor);
		                  tlist[tractor.getId()].location = temp;
                          return Command.createMoveCommand(temp);		//move to trailer

                      }else{
                          tlist[tractor.getId()].location = new Point(0, 0);
                          return new Command(CommandType.DETATCH);
                      }
                  }else{
                      return new Command(CommandType.ATTACH);
                  }

              }else{
              	
                

                  // if(tractor.getAttachedTrailer()!=null){
                  //     if(tractor.getAttachedTrailer().getNumBales()==0){
                  //         //Point p = tlist[tractor.getId()].location;
                  //         //return Command.createMoveCommand(p);		//move to tractor
                  //     }else{

                           tlist[tractor.getId()].location.x = 0;
                           tlist[tractor.getId()].location.y = 0;
                           return new Command(CommandType.DETATCH);
                  //     }
                  // }
                  // else{
                  // 		return Command.createMoveCommand(new Point(0, 0));
                  // }
              }

        }

        //tractor near trailer not at origin
        if(Math.abs(tractor.getLocation().x-tlist[tractor.getId()].location.x)<0.003 && Math.abs(tractor.getLocation().y-tlist[tractor.getId()].location.y)<0.003) {
        	System.out.println("HEREHEREHEREHEREHEREHEREHEREHEER");
            if (tractor.getAttachedTrailer() != null) {
                if (tlist[tractor.getId()].getNumBales() == 10 || remainingBales.size() == 0 || bales.size() == 0) {
                    Point p = new Point(0, 0);
                    return Command.createMoveCommand(p);		//move to origin

                }

                return new Command(CommandType.DETATCH);
            } else {
                if (!tractor.getHasBale()) {
                    if (tlist[tractor.getId()].getNumBales() == 10 || remainingBales.size() == 0 || bales.size() == 0) {
                        return new Command(CommandType.ATTACH);

                    } else {

                        double x = tlist[tractor.getId()].location.x;
                        double y = tlist[tractor.getId()].location.y;
                        Point tractor_loc = new Point(x, y);
                        Point closest = new Point(Float.MAX_VALUE, Float.MAX_VALUE);

                    	for(Point p: bales)
                    	{
                    		if(calDistance(tractor_loc, p) < calDistance(tractor_loc, closest))
                    			closest = p;
                    	}

                    	bales.remove(closest);
                        return Command.createMoveCommand(closest);	
                    }


                	} else {
                        	tlist[tractor.getId()].numBales = tlist[tractor.getId()].numBales + 1;
                        	return new Command(CommandType.STACK);

                }
            }
        }
        
            /*if (tractor.getLocation().x * tractor.getLocation().x + tractor.getLocation().y * tractor.getLocation().y < range * range) {
                if (tractor.getHasBale()) {
                    Point p = new Point(0, 0);
                    return Command.createMoveCommand(p);		//move to origin
                } else {

                    return new Command(CommandType.LOAD);
                }
            } else {
            	*/
                if (tractor.getHasBale()) {
                    Point p = new Point(tlist[tractor.getId()].location.x, tlist[tractor.getId()].location.y);
                    return Command.createMoveCommand(p);		//move to trailer
                } else {

                	if(bales.size() == 0)
                		return Command.createMoveCommand(new Point(0, 0));	 

                    return new Command(CommandType.LOAD);
                }
            //}



    }
}

