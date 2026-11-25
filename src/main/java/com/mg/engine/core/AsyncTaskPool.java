package com.mg.engine.core;

import com.loan12suquan.hd.platform.HdPlatform;
import java.util.LinkedList;

/**
 * Thread-safe single-worker queued task pool used for background audio playback,
 * screen transitions, and AI move computation.
 */
public class AsyncTaskPool implements Runnable {
    private static AsyncTaskPool instance;

    private final Object lock = new Object();
    private final LinkedList<AsyncTask> queue = new LinkedList<AsyncTask>();
    private volatile boolean stopped = true;
    private boolean workerStarted = false;

    private AsyncTaskPool() {
        this.stopped = false;
    }

    public static synchronized AsyncTaskPool getInstance() {
        if (instance == null) {
            instance = new AsyncTaskPool();
        }
        return instance;
    }

    public void init(int capacity) {
        if (capacity <= 0) {
            throw new RuntimeException("Size of pool cannot be less than or equal 0");
        }
        if (HdPlatform.isWeb()) {
            this.stopped = false;
            this.workerStarted = true;
            return;
        }
        synchronized (this.lock) {
            this.stopped = false;
            if (!this.workerStarted) {
                this.workerStarted = true;
                Thread worker = new Thread(this, "Loan12SuQuan-AsyncWorker");
                worker.setDaemon(true);
                worker.start();
            }
        }
    }

    public void submit(final AsyncTask task) {
        if (task == null) {
            return;
        }
        if (HdPlatform.isWeb()) {
            HdPlatform.backend().scheduleDelayed(1, new Runnable() {
                @Override
                public void run() {
                    if (stopped) {
                        return;
                    }
                    try {
                        task.runTask();
                    } catch (Throwable err) {
                        DebugLogForm.log(err);
                        err.printStackTrace();
                    }
                }
            });
            return;
        }
        synchronized (this.lock) {
            this.queue.addLast(task);
            this.lock.notifyAll();
        }
    }

    public void run() {
        while (!this.stopped) {
            AsyncTask next = null;
            synchronized (this.lock) {
                while (!this.stopped && this.queue.isEmpty()) {
                    try {
                        this.lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!this.stopped && !this.queue.isEmpty()) {
                    next = this.queue.removeFirst();
                }
            }
            if (next != null) {
                try {
                    next.runTask();
                } catch (Throwable err) {
                    DebugLogForm.log(err);
                    err.printStackTrace();
                }
            }
        }
    }

    public void shutdown() {
        synchronized (this.lock) {
            this.stopped = true;
            this.queue.clear();
            this.lock.notifyAll();
        }
    }
}



