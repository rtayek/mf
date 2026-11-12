package com.tayek.mf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class LatestAnalysisTest {
    @Test void coalescesRapidNavigationAndShowsOnlyNewestResult() {
        List<String> received = new ArrayList<>();
        LatestAnalysis<String> latest = new LatestAnalysis<>(Runnable::run,
                (value, error) -> received.add(value));
        CompletableFuture<String> first = new CompletableFuture<>();
        CompletableFuture<String> newest = new CompletableFuture<>();
        List<String> launched = new ArrayList<>();

        latest.submit(() -> { launched.add("first"); return first; });
        latest.submit(() -> { launched.add("skipped"); return CompletableFuture.completedFuture("skipped"); });
        latest.submit(() -> { launched.add("newest"); return newest; });
        assertEquals(List.of("first"), launched);

        first.complete("old position");
        assertEquals(List.of("first", "newest"), launched);
        assertTrue(received.isEmpty());
        newest.complete("new position");
        assertEquals(List.of("new position"), received);
    }

    @Test void ignoresOldEngineAfterStopAndRestart() {
        List<String> received = new ArrayList<>();
        LatestAnalysis<String> latest = new LatestAnalysis<>(Runnable::run,
                (value, error) -> received.add(value));
        CompletableFuture<String> old = new CompletableFuture<>();
        CompletableFuture<String> current = new CompletableFuture<>();

        latest.submit(() -> old);
        latest.cancel();
        latest.submit(() -> current);
        old.complete("stale");
        current.complete("fresh");
        assertEquals(List.of("fresh"), received);
    }

    @Test void reportsOnlyCurrentFailure() {
        List<Throwable> errors = new ArrayList<>();
        LatestAnalysis<String> latest = new LatestAnalysis<>(Runnable::run,
                (value, error) -> { if (error != null) errors.add(error); });
        CompletableFuture<String> old = new CompletableFuture<>();
        IllegalStateException failure = new IllegalStateException("bad position");

        latest.submit(() -> old);
        latest.submit(() -> CompletableFuture.failedFuture(failure));
        old.completeExceptionally(new IllegalStateException("obsolete"));
        assertEquals(List.of(failure), errors);
    }
}
