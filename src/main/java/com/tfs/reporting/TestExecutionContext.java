package com.tfs.reporting;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestExecutionContext {
    public static final class State {
        public String id, title, module;
        public Instant start;
        public final AtomicInteger step = new AtomicInteger();
    }

    private static final ThreadLocal<State> STATE = new ThreadLocal<>();
    private TestExecutionContext() {}

    public static void start(String id, String title, String module) {
        State s = new State();
        s.id = id; s.title = title; s.module = module; s.start = Instant.now();
        STATE.set(s);
    }

    public static State current() {
        State s = STATE.get();
        if (s == null) throw new IllegalStateException("No active test context.");
        return s;
    }

    public static void clear() { STATE.remove(); }
}
