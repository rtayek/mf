package com.tayek.mf;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** Keeps one analysis in flight and replaces queued work with the latest position. */
final class LatestAnalysis<T> {
    LatestAnalysis(Executor executor, BiConsumer<T, Throwable> onResult) {
        this.executor = Objects.requireNonNull(executor);
        this.onResult = Objects.requireNonNull(onResult);
    }

    void submit(Supplier<CompletableFuture<T>> request) {
        queued = Objects.requireNonNull(request);
        generation++;
        if (!busy) startNext();
    }

    void cancel() {
        generation++;
        activeGeneration = generation;
        queued = null;
        busy = false;
    }

    private void startNext() {
        Supplier<CompletableFuture<T>> request = queued;
        queued = null;
        busy = true;
        long submittedGeneration = generation;
        activeGeneration = submittedGeneration;
        try {
            Objects.requireNonNull(request.get()).whenComplete((value, error) ->
                    executor.execute(() -> complete(submittedGeneration, value, error)));
        } catch (RuntimeException exception) {
            executor.execute(() -> complete(submittedGeneration, null, exception));
        }
    }

    private void complete(long submittedGeneration, T value, Throwable error) {
        if (submittedGeneration != activeGeneration) return;
        busy = false;
        if (queued != null) {
            startNext();
        } else if (submittedGeneration == generation) {
            onResult.accept(value, error);
        }
    }

    private final Executor executor;
    private final BiConsumer<T, Throwable> onResult;
    private Supplier<CompletableFuture<T>> queued;
    private long generation;
    private long activeGeneration;
    private boolean busy;
}
