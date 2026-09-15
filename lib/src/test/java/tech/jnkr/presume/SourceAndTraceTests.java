package tech.jnkr.presume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.TraceZipper;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Just;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.Nothing;

public class SourceAndTraceTests {
    public static class SampleGenerator1 extends AbstractGenerator<ImmutableList<Object>> {
        @Override
        protected ImmutableList<Object> gen(@NonNull GenerationSource drawer) {
            ImmutableList<Object> result = ImmutableList.empty();

            result = result.push(drawer.getLong()); // 2 atoms
            result = result.push(drawer.getFloat()); // 2 atoms
            result = result.push(drawer.call(new SampleGenerator2())); // 1 atom
            result = result.push(drawer.getBoolean()); // 1 atom
            result = result.push(drawer.call(new SampleGenerator2())); // 1 atom
            result = result.push(drawer.call(new SampleGenerator3())); // 4 atoms
            result = result.push(drawer.getDouble()); // 3 atoms

            result = result.push(drawer.call(new SampleGenerator5()));

            return result;
        }
    }

    // 1 atom
    public static class SampleGenerator2 extends AbstractGenerator<ImmutableList<Object>> {

        @Override
        protected ImmutableList<Object> gen(@NonNull GenerationSource drawer) {
            ImmutableList<Object> result = ImmutableList.empty();

            result = result.push(drawer.call(new SampleGenerator4())); // 0 atoms
            result = result.push(drawer.getInteger()); // 1 atom

            return result;
        }
    }

    // 4 atoms
    public static class SampleGenerator3 extends AbstractGenerator<ImmutableList<Object>> {

        @Override
        protected ImmutableList<Object> gen(@NonNull GenerationSource drawer) {
            ImmutableList<Object> result = ImmutableList.empty();

            result = result.push(drawer.call(new SampleGenerator2())); // 1 atom
            result = result.push(drawer.call(new SampleGenerator2())); // 1 atom
            result = result.push(drawer.call(new SampleGenerator4())); // 0 atoms
            result = result.push(drawer.getFloat()); // 2 atoms

            return result;
        }
    }

    public static class SampleGenerator4 extends AbstractGenerator<Boolean> {
        @Override
        protected Boolean gen(@NonNull GenerationSource drawer) {
            return true;
        }
    }

    public static class SampleGenerator5 extends AbstractGenerator<ImmutableList<Object>> {
        @Override
        protected ImmutableList<Object> gen(@NonNull GenerationSource drawer) {
            int length = drawer.integerGen().withMinimum(0).withMaximum(10).gen(drawer);
            ImmutableList<Object> result = ImmutableList.empty();

            for (int i = 0; i < length; i++) {
                var intermediate =
                        drawer.getBoolean()
                                ? drawer.call(new SampleGenerator3())
                                : drawer.call(new SampleGenerator2());

                result = result.push(intermediate);
            }

            return result;
        }
    }

    @RepeatedTest(100)
    @DisplayName("Replaying a recording gives the same result")
    public void recordThenReplay() {
        RecordingSource recordingSource = new RecordingSource();

        ImmutableList<Object> generated = recordingSource.call(new SampleGenerator1());
        // TODO: getHistory() should return History
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        ReplayingSource replayingSource1 = new ReplayingSource(recordedHistory);
        ImmutableList<Object> replayed1 = replayingSource1.call(new SampleGenerator1());

        assertEquals(generated, replayed1);
    }

    @RepeatedTest(100)
    @DisplayName("Replaying a replay gives the same result")
    public void replayThenReplay() {
        RecordingSource recordingSource = new RecordingSource();

        recordingSource.call(new SampleGenerator1());
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        ReplayingSource replayingSource1 = new ReplayingSource(recordedHistory);
        ImmutableList<Object> replayed1 = replayingSource1.call(new SampleGenerator1());

        ReplayingSource replayingSource2 =
                new ReplayingSource(replayingSource1.finalTrace().toHistory());
        ImmutableList<Object> replayed2 = replayingSource2.call(new SampleGenerator1());

        assertEquals(replayed1, replayed2);
    }

    @RepeatedTest(100)
    @DisplayName("Replaying a replay gives the same trace")
    public void replayThenReplayTrace() {
        RecordingSource recordingSource = new RecordingSource();

        recordingSource.call(new SampleGenerator1());
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        ReplayingSource replayingSource1 = new ReplayingSource(recordedHistory);
        replayingSource1.call(new SampleGenerator1());

        ReplayingSource replayingSource2 =
                new ReplayingSource(replayingSource1.finalTrace().toHistory());
        replayingSource2.call(new SampleGenerator1());

        assertEquals(replayingSource1.finalTrace(), replayingSource2.finalTrace());
    }

    @RepeatedTest(100)
    @DisplayName("A trace zipper can convert to a trace")
    public void zipperToTrace() {
        RecordingSource recordingSource = new RecordingSource();

        recordingSource.call(new SampleGenerator1());
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        ReplayingSource replayingSource = new ReplayingSource(recordedHistory);
        replayingSource.call(new SampleGenerator1());

        Trace trace = replayingSource.finalTrace();

        for (int i = 0; i < 200; i++) {
            Maybe<Trace> navigated = trace.toZipper().chaseToAtomIndex(i).map(TraceZipper::toTrace);

            if (i <= 14) { // minimum number of atoms consumed by generator
                assertTrue(navigated.isJust());
            }

            switch (navigated) {
                case Nothing() -> {}
                case Just(Trace x) -> assertEquals(x, trace);
            }
        }
    }
}
