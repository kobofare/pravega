package com.emc.logservice.storageimplementation.distributedlog;

import com.emc.logservice.common.ConfigurationException;
import com.emc.logservice.common.ObjectClosedException;
import com.emc.logservice.storageabstraction.DataLogNotAvailableException;
import com.twitter.distributedlog.DistributedLogConfiguration;
import com.twitter.distributedlog.DistributedLogConstants;
import com.twitter.distributedlog.namespace.DistributedLogNamespace;
import com.twitter.distributedlog.namespace.DistributedLogNamespaceBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * General client for DistributedLog
 */
class LogClient implements AutoCloseable {
    private static final String DistributedLogUriFormat = "distributedlog://%s:%d/%s";
    private final DistributedLogConfig config;
    private DistributedLogNamespace namespace;
    private boolean closed;

    public LogClient(DistributedLogConfig config) {
        this.config = config;
    }

    public void initialize() throws ConfigurationException, DataLogNotAvailableException {
        if (this.closed) {
            throw new ObjectClosedException(this);
        }

        URI uri = URI.create(String.format(DistributedLogUriFormat, config.getDistributedLogHost(), config.getDistributedLogPort(), config.getDistributedLogNamespace()));

        DistributedLogConfiguration conf = new DistributedLogConfiguration()
                .setImmediateFlushEnabled(true)
                .setOutputBufferSize(0)
                .setPeriodicFlushFrequencyMilliSeconds(0)
                .setLockTimeout(DistributedLogConstants.LOCK_IMMEDIATE)
                .setCreateStreamIfNotExists(true);
        try {
            this.namespace = DistributedLogNamespaceBuilder.newBuilder()
                                                           .conf(conf)
                                                           .uri(uri)
                                                           .regionId(DistributedLogConstants.LOCAL_REGION_ID)
                                                           .clientId("console-writer")
                                                           .build();
        }
        catch (IllegalArgumentException | NullPointerException ex) {
            //configuration issue
            throw new ConfigurationException("Unable to create a DistributedLog Namespace.", ex);
        }
        catch (IOException ex) {
            // Namespace not available, ZooKeeper not reachable, some other environment issue.
            throw new DataLogNotAvailableException("Unable to access DistributedLog Namespace.", ex);
        }

    }

    @Override
    public void close() {
        if(!this.closed) {
            this.namespace.close();
            this.closed = true;
        }
    }

    //
    //    private static final int RecordCount = 100;
    //    private static final int RecordSize = 100;
    //    private final byte[] RecordData;
    //
    //    public DLogTester() {
    //        Random rnd = new Random();
    //        RecordData = new byte[RecordSize];
    //        rnd.nextBytes(RecordData);
    //    }
    //
    //    public void run() throws Exception {
    //        URI uri = URI.create("distributedlog://localhost:7000/messaging/distributedlog");
    //        DistributedLogConfiguration conf = new DistributedLogConfiguration()
    //                .setImmediateFlushEnabled(true)
    //                .setOutputBufferSize(0)
    //                .setPeriodicFlushFrequencyMilliSeconds(0)
    //                .setLockTimeout(DistributedLogConstants.LOCK_IMMEDIATE)
    //                .setCreateStreamIfNotExists(true);
    //
    //
    //        DistributedLogNamespace namespace = null;
    //        DistributedLogManager dlm = null;
    //        AsyncLogWriter writer = null;
    //        ConcurrentHashMap<Long, Long> latenciesById = new ConcurrentHashMap<>();
    //
    //        try {
    //            System.out.println("Opening namespace...");
    //            namespace = DistributedLogNamespaceBuilder.newBuilder()
    //                                                      .conf(conf)
    //                                                      .uri(uri)
    //                                                      .regionId(DistributedLogConstants.LOCAL_REGION_ID)
    //                                                      .clientId("console-writer")
    //                                                      .build();
    //
    //            System.out.println("Opening log...");
    //            dlm = namespace.openLog("messaging-stream-1");
    //
    //            System.out.println("Opening async writer...");
    //            writer = FutureUtils.result(dlm.openAsyncLogWriter());
    //
    //            System.out.println("Writing entries...");
    //            for (int i = 0; i < RecordCount; i++) {
    //                final long txId = System.currentTimeMillis();
    //                LogRecord record = new LogRecord(txId, RecordData);
    //                latenciesById.put(txId, System.nanoTime());
    //                Future<DLSN> dlsn = writer.write(record);
    //                System.out.println("Writing record " + i);
    //                dlsn.addEventListener(new FutureEventListener<DLSN>() {
    //                    @Override
    //                    public void onSuccess(DLSN value) {
    //                        latenciesById.put(txId, System.nanoTime() - latenciesById.get(txId));
    //                    }
    //
    //                    @Override
    //                    public void onFailure(Throwable cause) {
    //                        System.err.println(cause);
    //                    }
    //                });
    //
    //                dlsn.get();
    //            }
    //        }
    //        finally {
    //            if (writer != null) {
    //                System.out.println("Closing writer...");
    //                FutureUtils.result(writer.asyncClose());
    //            }
    //
    //            if (dlm != null) {
    //                System.out.println("Closing log...");
    //                FutureUtils.result(dlm.asyncClose());
    //            }
    //
    //            if (namespace != null) {
    //                System.out.println("Closing namespace...");
    //                namespace.close();
    //            }
    //        }
    //
    //        ArrayList<Long> latencies = new ArrayList<>(latenciesById.values());
    //        latencies.sort(Long::compare);
    //
    //        long sum = 0;
    //        for (int i = 0; i < latencies.size(); i++) {
    //            latencies.set(i, latencies.get(i) / 1000 / 1000);
    //            sum += latencies.get(i);
    //        }
    //
    //        System.out.println("Latencies");
    //        System.out.println("Count, Avg, Min, Max, 50%, 90%, 95%, 99%, 99.9%");
    //        System.out.println(String.format("%d, %f, %d, %d, %d, %d, %d, %d, %d",
    //                latencies.size(),
    //                sum * 1.0 / latencies.size(),
    //                latencies.get(0),
    //                latencies.get(latencies.size() - 1),
    //                latencies.get((int) (latencies.size() * 0.5)),
    //                latencies.get((int) (latencies.size() * 0.9)),
    //                latencies.get((int) (latencies.size() * 0.95)),
    //                latencies.get((int) (latencies.size() * 0.99)),
    //                latencies.get((int) (latencies.size() * 0.999))));
    //    }
}
