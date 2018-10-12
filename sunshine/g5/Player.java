package sunshine.g5;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math.*;
import java.util.*;


import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    
    List<Point> bales;
    List<Point> bales_load;
    List<Trailer> trailers;
    Hashtable attach_status;

    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        bales_load = new ArrayList<Point>(bales);
        trailers = new ArrayList<Trailer>();
        attach_status = new Hashtable();
    }



    private Point closest_bale(Tractor tractor) {
        Point trac_loc = tractor.getLocation();
        Point bale = null;
        double min_dist = Double.POSITIVE_INFINITY;

        for (Point x : bales) {
            double cur_dist = Math.sqrt(Math.pow(trac_loc.x - x.x, 2) + Math.pow(trac_loc.y - x.y, 2));
            if (cur_dist < min_dist) {
                min_dist = cur_dist;
                bale = x;
            }
        }
        return bale;
    }
    private Point closest_bale_load(Tractor tractor) {
        Point trac_loc = tractor.getLocation();
        Point bale = null;
        double min_dist = Double.POSITIVE_INFINITY;

        for (Point x : bales_load) {
            double cur_dist = Math.sqrt(Math.pow(trac_loc.x - x.x, 2) + Math.pow(trac_loc.y - x.y, 2));
            if (cur_dist < min_dist) {
                min_dist = cur_dist;
                bale = x;
            }
        }
        return bale;
    }




    private Point far_from_origin_bale() {
        Point origin = new Point(0.0, 0.0);
        Point bale = null;
        double max_dist = Double.NEGATIVE_INFINITY;

        for (Point x : bales) {
            double cur_dist = Math.sqrt(Math.pow(origin.x - x.x, 2) + Math.pow(origin.y - x.y, 2));
            if (cur_dist > max_dist) {
                max_dist = cur_dist;
                bale = x;
            }
        }
        return bale;
    }




    private double dist_calc(Tractor tractor, Point dest) {
        Point trac_loc = tractor.getLocation();
        return Math.sqrt(Math.pow(trac_loc.x - dest.x, 2) + Math.pow(trac_loc.y - dest.y, 2));
    }




    private double time_without_trailer(Tractor tractor, Point bale) {
        Point trac_loc = tractor.getLocation();
        double distance = Math.sqrt(Math.pow(trac_loc.x - bale.x, 2) + Math.pow(trac_loc.y - bale.y, 2));
        double time = (2 * distance / 10) + 20;
        return time;
    }
    private double time_without_trailer(Point tractor, Point bale) {
        Point trac_loc = tractor;
        double distance = Math.sqrt(Math.pow(trac_loc.x - bale.x, 2) + Math.pow(trac_loc.y - bale.y, 2));
        double time = (2 * distance / 10) + 20;
        return time;
    }




    private double time_with_trailer(Tractor tractor, Point bale) {
        Point trac_loc = tractor.getLocation();
        double distance = Math.sqrt(Math.pow(trac_loc.x - bale.x, 2) + Math.pow(trac_loc.y - bale.y, 2));
        double time = ((2 * distance / 4) + 240) / 10 + 40; /* assume each trailer is full each trip */
        return time;
    }
    private double time_with_trailer(Point tractor, Point bale) {
        Point trac_loc = tractor;
        double distance = Math.sqrt(Math.pow(trac_loc.x - bale.x, 2) + Math.pow(trac_loc.y - bale.y, 2));
        double time = ((2 * distance / 4) + 240) / 10 + 40; /* assume each trailer is full each trip */
        return time;
    }
    


    private boolean trailers_to_be_unstacked_at_origin() {
        boolean result = false;
        for (Trailer x : trailers) {
            if (x.getNumBales() > 0 && x.getLocation().equals(new Point(0.0, 0.0))) {
                result = true;
                break;
            }
        }
        return result;
    }




    private Trailer closest_trailer(Tractor tractor) {
        Point trac_loc = tractor.getLocation();

        Trailer closest = null;
        double closest_dist = 0;
        for (Trailer x : trailers) {
            //System.err.println(x.getLocation().x + x.getLocation().y);
            if (x.getLocation().equals(new Point(0.0, 0.0))) {
                continue;
            }
            if (closest == null) {
                closest = x;
                closest_dist = Math.sqrt(Math.pow(trac_loc.x - x.getLocation().x, 2) + Math.pow(trac_loc.y - x.getLocation().y, 2));
            } else {
                double cur_dist = Math.sqrt(Math.pow(trac_loc.x - x.getLocation().x, 2) + Math.pow(trac_loc.y - x.getLocation().y, 2));
                if (cur_dist < closest_dist) {
                    closest = x;
                    closest_dist = cur_dist;
                }
            }
        }
        //System.err.println(closest == null);
        return closest;
    }
    private Trailer closest_trailer(Tractor tractor, ArrayList<Trailer> trailers) {
        Point trac_loc = tractor.getLocation();

        Trailer closest = null;
        double closest_dist = 0;
        for (Trailer x : trailers) {
            //System.err.println(x.getLocation().x + x.getLocation().y);
            if (x.getLocation().equals(new Point(0.0, 0.0))) {
                continue;
            }
            if (closest == null) {
                closest = x;
                closest_dist = Math.sqrt(Math.pow(trac_loc.x - x.getLocation().x, 2) + Math.pow(trac_loc.y - x.getLocation().y, 2));
            } else {
                double cur_dist = Math.sqrt(Math.pow(trac_loc.x - x.getLocation().x, 2) + Math.pow(trac_loc.y - x.getLocation().y, 2));
                if (cur_dist < closest_dist) {
                    closest = x;
                    closest_dist = cur_dist;
                }
            }
        }
        //System.err.println(closest == null);
        return closest;
    }




    private Trailer get_trailer_at_loc(Tractor tractor) {
        Point trac_loc = tractor.getLocation();
        Trailer result = null;

        for (Trailer x : trailers) {
            if (x.getLocation().equals(trac_loc)) {
                result = x;
            }
        }
        return result;
    }








    public Command getCommand(Tractor tractor)
    {
        if (tractor.getLocation().equals(new Point(0.0, 0.0))) {
            //if(tractor.getAttachedTrailer() != null) {return new Command(CommandType.DETATCH);}
            if (tractor.getAttachedTrailer() != null && !trailers.contains(tractor.getAttachedTrailer())) {
                trailers.add(tractor.getAttachedTrailer());
                attach_status.put(tractor.getAttachedTrailer(), 1);
            }

            /*if (tractor.getAttachedTrailer() != null) {
                return Command.createMoveCommand(new Point(30.0,30.0));
            } else {
                Point trailor_loc = trailers.get(0).getLocation();
                return Command.createMoveCommand(trailor_loc);
            }*/
            
            if (tractor.getHasBale()) {
                return new Command(CommandType.UNLOAD);
            }

            if (trailers_to_be_unstacked_at_origin()) {
                return new Command(CommandType.UNSTACK);
            }

            if (tractor.getAttachedTrailer() != null) {
                if (tractor.getAttachedTrailer().getNumBales() > 0) {
                    attach_status.put(tractor.getAttachedTrailer(), 0);
                    return new Command(CommandType.DETATCH);
                }
            }

            /* send tractor out onto field */
            Point dest_bale = far_from_origin_bale();
            //bales.remove(dest_bale);
            //return Command.createMoveCommand(dest_bale);

            if(time_with_trailer(tractor, dest_bale) < time_without_trailer(tractor, dest_bale)) {
                if (tractor.getAttachedTrailer() == null) {
                    attach_status.put(get_trailer_at_loc(tractor), 1);
                    return new Command(CommandType.ATTACH);
                } else {
                    bales.remove(dest_bale);
                    return Command.createMoveCommand(dest_bale);
                }
            } else {
                if (tractor.getAttachedTrailer() != null) {
                    attach_status.put(tractor.getAttachedTrailer(), 0);
                    return new Command(CommandType.DETATCH);
                } else {
                    bales.remove(dest_bale);
                    return Command.createMoveCommand(dest_bale);
                }
            }



             
                /* send tractor out onto field */
                //Point dest_bale = closest_bale(tractor);
                //bales.remove(dest_bale);
                //return Command.createMoveCommand(dest_bale);
            
        } else {

            if (tractor.getAttachedTrailer() != null) {
                if (tractor.getAttachedTrailer().getNumBales() > 0) {
                    return Command.createMoveCommand(new Point(0.0,0.0));
                } else {
                    attach_status.put(tractor.getAttachedTrailer(), 0);
                    return new Command(CommandType.DETATCH);
                }
            }

            /* go collect bales */
            if (tractor.getHasBale() == false) {
                Point dest_bale = closest_bale(tractor);
                Point dest_bale_load = closest_bale_load(tractor);

                //System.err.println("HEYYYYYYY");
                //System.err.println(tractor.getLocation().x + "###" + tractor.getLocation().y + "###" + dest_bale_load.x + "###" + dest_bale_load.y);

                if (!tractor.getLocation().equals(dest_bale_load)) {
                    bales.remove(dest_bale);
                    return Command.createMoveCommand(dest_bale);
                } else {
                    bales_load.remove(dest_bale_load);
                    return new Command(CommandType.LOAD);
                }
            } else {
                //System.err.println("HEYYYYYYYY");
                Trailer closest_trailer = closest_trailer(tractor);
                if (attach_status.get(closest_trailer).equals(1)) {
                    ArrayList<Trailer> temp = new ArrayList<Trailer>(trailers);
                    while (attach_status.get(closest_trailer).equals(1)) {
                        temp.remove(closest_trailer);
                        closest_trailer = closest_trailer(tractor, temp);
                    }
                }


                if (closest_trailer == null) {return Command.createMoveCommand(new Point(0.0,0.0));}

                if (!tractor.getLocation().equals(closest_trailer.getLocation())) {
                    return Command.createMoveCommand(closest_trailer.getLocation());
                } else {
                    if (attach_status.get(get_trailer_at_loc(tractor)).equals(0) && (closest_trailer.getNumBales() == 10 || bales_load.size() == 0)) {
                        attach_status.put(get_trailer_at_loc(tractor), 1);
                        return new Command(CommandType.ATTACH);
                    } else {
                        return new Command(CommandType.STACK);
                    }
                }
            }



            /*
            if (tractor.getAttachedTrailer() != null) {
                return new Command(CommandType.DETATCH);
            } else {
                return Command.createMoveCommand(new Point(0.0,0.0));
            }
            
            if (tractor.getHasBale()) {
                return Command.createMoveCommand(new Point(0.0, 0.0));
            } else {
                return new Command(CommandType.LOAD);
            }*/
        }


        /*
        if (tractor.getHasBale())
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                return new Command(CommandType.UNLOAD);
            }
            else
            {
                return Command.createMoveCommand(new Point(0.0, 0.0));
            }
        }
        else
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                if (rand.nextDouble() > 0.5)
                {
                    if (tractor.getAttachedTrailer() == null)
                    {
                        return new Command(CommandType.ATTACH);
                    }
                    else
                    {
                        return new Command(CommandType.DETATCH);
                    }
                }
                else if (bales.size() > 0)
                {
                    Point p = bales.remove(rand.nextInt(bales.size()));
                    return Command.createMoveCommand(p);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return new Command(CommandType.LOAD);
            }
        }*/
    }
}
