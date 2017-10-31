package com.fractallabs.assignment;

import com.fractallabs.assignments.TwitterScanner;
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
}
