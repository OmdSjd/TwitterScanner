package com.twitterscanner.os;

import org.json.simple.JSONObject;
import twitter4j.FilterQuery;
import twitter4j.JSONArray;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.LongAdder;

public class TwitterScanner{
    private LongAdder hourlyCount = new LongAdder();
    private Long prevCount = 0L;
    private int index = 0;

    private JSONObject scanner = new JSONObject();
    private JSONArray listChanges = new JSONArray();

    /*
    * This method will stream all mentions about companyName
    * It creates an open connection that streams the Twitter posts through listeners once they're posted
    * */
    public TwitterScanner(String companyName) {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.onStatus(status -> {
            hourlyCount.increment();
            //System.out.println(status.getUser().getScreenName() + " has tweeted:" + status.getText());
            System.out.println(Thread.currentThread().getName());
        });
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(companyName);
        twitterStream.filter(filterQuery);
        scanner.put("TwitterScanner", listChanges);
    }
    public static void main(String... args) {
        TwitterScanner scanner = new TwitterScanner("bitcoin");
        //Create a Timer object and give it a TimerTask that performs the run method of TwitterScanner
        Timer timer = new Timer();
        System.out.println(Thread.currentThread().getName());
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                scanner.run();
                System.out.println(Thread.currentThread().getName());
            }
        };
        //The run method gets called hourly
        timer.schedule(hourlyTask, 5000, 5000);
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
        double c = 0;
        if (prevCount.intValue() > 0) {
            c = ((hourlyCount.doubleValue() - prevCount.doubleValue()) / prevCount.doubleValue()) * 100;
        }
        index++;
        System.out.println("/******************************************/");
        System.out.println("Hour " + index + " hourlyCount: " + hourlyCount + "    prevCount: " + prevCount);
        System.out.println("--------------------");
        storeValue(new TSValue(Instant.now(), Math.round(c * 100.0) / 100.0));
        System.out.println("/******************************************/");
        prevCount = hourlyCount.longValue();
        hourlyCount.reset();
    }

    /*
    * This method will store a TSValue object as a JSON object
    */
    private void storeValue(TSValue value) {
        JSONObject changes = new JSONObject();
        changes.put("Timestamp", value.getTimestamp());
        changes.put("Value", value.getVal() + "%");
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
    public void writeJson() {
        try {
            File file = new File("./src/main/resources/META-INF/changes.json");
            FileWriter fileW = new FileWriter(file);
            fileW.write(scanner.toJSONString());
            fileW.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}
