package com.fractallabs.assignment;

import com.twitterscanner.os.TwitterScanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

public class TwitterScannerTest {
    File file = new File("./src/test/resources/meta-inf/changes.json");
    private JSONObject change = new JSONObject();
    private TwitterScanner.TSValue value;

    @Before
    public void init() {
        long hourlyCount = 20;
        long prevCount = 30;
        Double c = ((hourlyCount - prevCount) / (double) prevCount) * 100;
        value = new TwitterScanner.TSValue(Instant.now(), c);
        change.put("Timestamp", value.getTimestamp().toString());
        change.put("Value", value.getVal());
    }

    @Test
    public void testStoreValue() {
        String s1 = change.get("Timestamp").toString();
        String s2 = value.getTimestamp().toString();
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testWriter() {
        JSONParser parser = new JSONParser();
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(change.toJSONString());
            fileWriter.flush();

            Object obj = parser.parse(new FileReader(file));
            JSONObject n = (JSONObject) obj;
            Assert.assertEquals(n, change);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException p) {
            p.printStackTrace();
        }
    }

    @Test
    public void testConcurrency(){
        LongAdder counter = new LongAdder();
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        int numberOfThreads = 4;
        int numberOfIncrements = 100;

        Runnable incrementAction = () -> IntStream
                .range(0, numberOfIncrements)
                .forEach(i -> counter.increment());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(incrementAction);
            System.out.println(counter.intValue());
        }

        //Assert.assertEquals(counter.sum(), numberOfIncrements * numberOfThreads);
        //Assert.assertEquals(counter.sumThenReset(), numberOfIncrements * numberOfThreads);
        //Assert.assertEquals(counter.sum(), 0);
    }
}
