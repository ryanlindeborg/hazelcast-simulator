package com.hazelcast.stabilizer.tests.map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.stabilizer.tests.TestContext;
import com.hazelcast.stabilizer.tests.TestFailureException;
import com.hazelcast.stabilizer.tests.TestRunner;
import com.hazelcast.stabilizer.tests.annotations.Performance;
import com.hazelcast.stabilizer.tests.annotations.Run;
import com.hazelcast.stabilizer.tests.annotations.Setup;
import com.hazelcast.stabilizer.tests.annotations.Teardown;
import com.hazelcast.stabilizer.tests.annotations.ThreadPool;
import com.hazelcast.stabilizer.tests.annotations.Verify;
import com.hazelcast.stabilizer.tests.annotations.Warmup;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MapLockTest {

    private final static ILogger log = Logger.getLogger(MapLockTest.class);

    //props
    public int threadCount = 10;
    public int keyCount = 1000;
    public int logFrequency = 10000;
    public int performanceUpdateFrequency = 10000;
    public String basename = "map";


    private IMap<Integer, Long> map;
    private final AtomicLong operations = new AtomicLong();
    private IMap<String, Map<Integer, Long>> resultsPerWorker;
    private TestContext testContext;

    @Setup
    public void setup(TestContext testContext) throws Exception {
        this.testContext = testContext;
        HazelcastInstance targetInstance = testContext.getTargetInstance();
        map = targetInstance.getMap(basename + "-" + testContext.getTestId());
        resultsPerWorker = targetInstance.getMap("ResultMap" + testContext.getTestId());
    }

    @Teardown
    public void teardown() throws Exception {
        map.destroy();
        resultsPerWorker.destroy();
    }

    @Warmup(global = true)
    public void warmup() throws Exception {
        for (int k = 0; k < keyCount; k++) {
            map.put(k, 0l);
        }
    }

    @Run
    public void run() {
        ThreadPool pool = new ThreadPool();
        for (int k = 0; k < threadCount; k++) {
            pool.spawn(new Worker());
        }
        pool.awaitCompletion();
    }

    @Verify(global = true)
    public void verify() throws Exception {
        long[] amount = new long[keyCount];

        for (Map<Integer, Long> map : resultsPerWorker.values()) {
            for (Map.Entry<Integer, Long> entry : map.entrySet()) {
                amount[entry.getKey()] += entry.getValue();
            }
        }

        int failures = 0;
        for (int k = 0; k < keyCount; k++) {
            long expected = amount[k];
            long found = map.get(k);
            if (expected != found) {
                failures++;
            }
        }

        if (failures > 0) {
            throw new TestFailureException("Failures found:" + failures);
        }
    }

    @Performance
    public long getOperationCount() {
        return operations.get();
    }

    private class Worker implements Runnable {
        private final Random random = new Random();
        private final Map<Integer, Long> result = new HashMap<Integer, Long>();

        @Override
        public void run() {
            for (int k = 0; k < keyCount; k++) {
                result.put(k, 0L);
            }

            long iteration = 0;
            while (!testContext.isStopped()) {
                Integer key = random.nextInt(keyCount);
                long increment = random.nextInt(100);

                map.lock(key);
                try {
                    Long current = map.get(key);
                    Long update = current + increment;
                    map.put(key, update);
                } finally {
                    map.unlock(key);
                }

                increment(key, increment);

                if (iteration % logFrequency == 0) {
                    log.info(Thread.currentThread().getName() + " At iteration: " + iteration);
                }

                if (iteration % performanceUpdateFrequency == 0) {
                    operations.addAndGet(performanceUpdateFrequency);
                }

                iteration++;
            }

            resultsPerWorker.put(UUID.randomUUID().toString(), result);
        }

        private void increment(int key, long increment) {
            result.put(key, result.get(key) + increment);
        }
    }

    public static void main(String[] args) throws Exception {
        MapLockTest test = new MapLockTest();
        new TestRunner().run(test, 20);
    }
}
