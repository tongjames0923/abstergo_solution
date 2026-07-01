package com.tbs.thread.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * 长期线程执行器
 *
 * @author tongj
 */
public class LongTermThreadExecutor implements Executor, DisposableBean {
    List<Thread> threads = new CopyOnWriteArrayList<>();

    @Override
    public void execute(@NotNull Runnable command) {
        Thread thread = new Thread(command);
        threads.add(thread);
        thread.start();
    }

    @Override
    public void destroy() throws Exception {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        threads.clear();
    }
}
