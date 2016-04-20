package com.dashboard.budget;

import java.util.Date;

/**
   This class implements a basic stop watch.
   @author Charlie McDowell
 */
public class StopWatch {
    private int hours, minutes, seconds, milliseconds;
    private Date start;

    /**
       Create a stop watch that is initialized to 0.
     */
    public StopWatch() {}

    /**
       Create a stop watch that is intialized to the specified amount of time.
       Behavior is unpredictable if any initial values are less than zero.
       @param hours - number of initial hours
       @param minutes - number of initial minutes
       @param seconds - number of initial seconds
       @param milliseconds - number of initial milliseconds
     */
    public StopWatch(int hours, int minutes, int seconds, int milliseconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        
        // for now assume all values are non-negative
        if (this.milliseconds >= 1000) {
            this.seconds = this.seconds + this.milliseconds / 1000;
            this.milliseconds = this.milliseconds % 1000;
        }
        if (this.seconds >= 60) {
            this.minutes = this.minutes + this.seconds / 60;
            this.seconds = this.seconds % 60;
        }
        if (this.minutes >= 60) {
            this.hours = this.hours + this.minutes / 60;
            this.minutes = this.minutes % 60;
        }
    }
    /**
       Start the stop watch running - continuing to count from the last stop.
       Has no effect if already running.
     */
    public void start() {
        // do nothing if already started
        if (start == null) {
            start = new Date();
        }
    }
    /**
       Stop the stop watch. Has no effect if already stopped.
     */
    public void stop() {
        // do nothing if already stopped
        if (start != null) {
            Date now = new Date();
            int interval = (int)(now.getTime() - start.getTime());
            milliseconds = milliseconds + interval % 1000;
            seconds = seconds + (interval / 1000) % 60;
            minutes = minutes + (interval / 60000) % 60;
            hours = hours + (interval / 3600000);
            start = null;
        }
    }
    /**
       Get the current value of the stop watch without changing it's state.
       That is, if it was running it keeps running if it was stopped it remains stopped.
       @return a copy of this stop watch stopped at the time of this stop watch.
     */
    public StopWatch get() {
        if (start != null) {
            // it is running - grab a snap shot and return that
            Date now = new Date();
            int interval = (int)(now.getTime() - start.getTime());
            int milliseconds = this.milliseconds + interval % 1000;
            int seconds = this.seconds + (interval / 1000) % 60;
            int minutes = this.minutes + (interval / 60000) % 60;
            int hours = this.hours + (interval / 3600000);
            return new StopWatch(hours, minutes, seconds, milliseconds);
        }
        else {
            // it is stopped - return a copy of this one
            return new StopWatch(hours, minutes, seconds, milliseconds);
        }
    }
    /**
       Stop the watch and set it to 0.
     */
    public void reset() {
        start = null;
        hours = minutes = seconds = milliseconds = 0;
    }
    /**
       A string representation of the current
       time in the form hours:minutes:seconds:milliseconds. Like get(), it has no
       effect on the state of the watch.
     */
    public String toString() {
        //return hours + ":" + minutes + ":" + seconds + ":" + milliseconds;
    	return minutes + " mins " + seconds + " secs";
    }
}