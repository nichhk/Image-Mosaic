package com.google.appengine.demos.ImageCollage;

/*
Update 12/24
Created class
CollageTimer: Halts the execution of the Collage threads so that the entire MasterCollage method runs in less
than a minute, because we are using Google Servers
 */

public class CollageTimer {
    private double limit; // Length of time MasterCollage is allowed to take in seconds
    public CollageTimer(double limit){
        this.limit = limit*Math.pow(10,9) + System.nanoTime(); // Convert the seconds into nano-seconds
        System.out.println("limit is "+ limit/Math.pow(10,9));
    }

    /*
    Returns true if the time limit has been exceeded
    Inputs:weight: Extra or less time (in nanoseconds) a given Collage.colorBlock() function is alotted
     */

    public boolean timeElapsed(double weight){

        if(System.nanoTime() >limit - weight){
            return true; // Time limit has been exceeded
        }
        return false;
    }

    /*
    Returns the amount of remaining time in nanoseconds
     */

    public double getRemaining(){
        return limit - System.nanoTime();
    }

}