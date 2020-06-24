package org.apache.zookeeper.server.metric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class AvgMinMaxCounterTest {

    private long[] values;
    private double avg;
    private long min;
    private long max;
    private long total;
    private long count;

    public AvgMinMaxCounterTest(long[] values, double avg, long min, long max, long total, long count) {
        this.values = values;
        this.avg = avg;
        this.min = min;
        this.max = max;
        this.total = total;
        this.count = count;
    }

    @Parameterized.Parameters
    public static Collection param() {

        return Arrays.asList(new Object[][] {
                {new long[] {}, 0.0, 0, 0, 0, 0},
                {new long[] {0}, 0.0, 0, 0, 0, 1},
                {new long[] {0, Long.MAX_VALUE}, Long.MAX_VALUE / 2 , 0, Long.MAX_VALUE, Long.MAX_VALUE, 2},
                {new long[] {0,  Long.MIN_VALUE}, Long.MIN_VALUE / 2.0 , Long.MIN_VALUE, 0, Long.MIN_VALUE, 2}
        });
    }

    @Test
    public void testGetAvg() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        assertEquals(avg, counter.getAvg(), 0);
    }

    @Test
    public void testGetMin() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        assertEquals(min, counter.getMin());
    }

    @Test
    public void testGetMax() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        assertEquals(max, counter.getMax());
    }

    @Test
    public void testGetTotal() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        assertEquals(total, counter.getTotal());
    }

    @Test
    public void testGetCount() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        assertEquals(count, counter.getCount());
    }

    @Test
    public void testValues() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("avg_" + "metric", avg);
        map.put("min_" + "metric", min);
        map.put("max_" + "metric", max);
        map.put("cnt_" + "metric", count);
        map.put("sum_" + "metric", total);

        assertEquals(map, counter.values());
    }

    @Test
    public void testReset() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        counter.reset();
        assertEquals(0, counter.getCount());
        assertEquals(0, counter.getTotal());
        assertEquals(0, counter.getMin());
        assertEquals(0, counter.getMax());
    }

    @Test
    public void testResetMax() {
        AvgMinMaxCounter counter = new AvgMinMaxCounter("metric");
        for (long value : values) {
            counter.add(value);
        }
        counter.resetMax();
        assertEquals(min, counter.getMax());
    }



}
