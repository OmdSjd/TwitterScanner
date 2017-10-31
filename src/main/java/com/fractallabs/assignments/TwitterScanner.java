package com.fractallabs.assignments;

import org.json.simple.JSONObject;
import twitter4j.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

public class TwitterScanner {
    private Long hourlyCount = 0L;
    private Long prevCount = 0L;
    private int index = 0;

    private JSONObject scanner = new JSONObject();
    private JSONArray listChanges = new JSONArray();

    public static class TSValue {
        private final Instant timestamp;
        private final double val;
        public TSValue(Instant timestamp, double val) {
            this.timestamp = timestamp;
            this.val = val;
        }
        public Instant getTimestamp() {
            return timestamp;
        }
        public double getVal() {
            return val;
        }
    }
    /*
    * This method will stream all mentions about companyName
    * It creates an open connection that streams the Twitter posts through listeners once they're posted
    * */
    public TwitterScanner(String companyName) {
        StatusListener statusListener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if(status.getText().toLowerCase().contains(companyName.toLowerCase())){
                    hourlyCount++;
                    //System.out.println(status.getUser().getName() +" @"+status.getUser().getScreenName() + " : " + status.getText());
                }
            }
            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {
                System.out.println("Stall Warning:" + stallWarning);
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(statusListener);
        twitterStream.user(companyName);
        scanner.put("TwitterScanner",listChanges);
    }
    /*
    * This method runs every hour and calls the storeValue method
    * hourlyCount = the count of mentions per hour
    * prevCount = the count of mentions of previous hour
    * c = the percentage change between last hour and this hour
    * index = simple index to keep track of the created JSONObjects (every hour)
    */
    public void run() {
        // Begin aggregating mentions. Every hour, "store" the relative change
        // (e.g. write it to System.out).
        if(hourlyCount > 0){
            double c = 0;
            if(prevCount > 0){
                c = ((hourlyCount - prevCount ) / (double)prevCount)*100;
            }
            index++;
            System.out.println("/******************************************/");
            System.out.println("Hour "+ index + " count: " + hourlyCount + "    prevCount: " + prevCount);
            System.out.println("--------------------");
            storeValue(new TSValue(Instant.now(),Math.round(c*100.0)/100.0));
            System.out.println("/******************************************/");
        }
        prevCount = hourlyCount;
        hourlyCount = 0L;
    }
    /*
    * This method will store a TSValue object as a JSON object
    */
    private void storeValue(TSValue value) {
        JSONObject changes = new JSONObject();
        changes.put("Timestamp", value.getTimestamp());
        changes.put("Value", value.getVal() +"%");
        JSONObject changeObject = new JSONObject();
        changeObject.put(index, changes);
        listChanges.put(changeObject);

        //Calling the writeJson method
        writeJson();

        System.out.println(changeObject);
    }
    /*
    * This will write a JSON file
    * Hourly the JSON file gets overwritten with the newly added element containing the percentage changes and the timestamp of the change
    * */
    public void writeJson(){
        try{
            File file = new File("./src/main/resources/META-INF/changes.json");
            FileWriter fileW = new FileWriter(file);
            fileW.write(scanner.toJSONString());
            fileW.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String ... args) {
        TwitterScanner scanner = new TwitterScanner("@Facebook");
        //Create a Timer object and give it a TimerTask that performs the run method of TwitterScanner
        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                scanner.run();
            }
        };
        //The run method gets called hourly
        timer.schedule (hourlyTask, 0l, 1000*60*60);
    }
}
