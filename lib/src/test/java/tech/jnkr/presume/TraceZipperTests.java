package tech.jnkr.presume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.jnkr.presume.generators.GenerationSource;
import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.TraceZipper;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.ConcreteIntegerGenerator;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Just;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.Nothing;

// There are additional tests in SourceAndTraceTests
public class TraceZipperTests {
    public record TestTraceStructure1(
            TestTraceStructure3 firstSub,
            int first,
            TestTraceStructure2 secondSub,
            TestTraceStructure2 thirdSub,
            int second,
            int third) {}

    public record TestTraceStructure2(
            int first,
            int second,
            TestTraceStructure3 firstSub,
            int third,
            TestTraceStructure3 secondSub) {}

    public record TestTraceStructure3(int first, int second) {}

    public static class Generator1 extends AbstractGenerator<TestTraceStructure1> {
        @Override
        protected TestTraceStructure1 gen(GenerationSource source) {
            TestTraceStructure3 firstSub = source.call(new Generator3());
            int x1 = source.getInteger();
            TestTraceStructure2 secondSub = source.call(new Generator2());
            TestTraceStructure2 thirdSub = source.call(new Generator2());
            int x2 = source.getInteger();
            source.call(new Generator5());
            int x3 = source.getInteger();

            return new TestTraceStructure1(firstSub, x1, secondSub, thirdSub, x2, x3);
        }
    }

    public static class Generator2 extends AbstractGenerator<TestTraceStructure2> {
        @Override
        protected TestTraceStructure2 gen(GenerationSource source) {
            int x1 = source.getInteger();
            int x2 = source.getInteger();
            TestTraceStructure3 sub1 = source.call(new Generator3());
            int x3 = source.getInteger();
            TestTraceStructure3 sub2 = source.call(new Generator3());

            return new TestTraceStructure2(x1, x2, sub1, x3, sub2);
        }
    }

    public static class Generator3 extends AbstractGenerator<TestTraceStructure3> {
        @Override
        protected TestTraceStructure3 gen(GenerationSource source) {
            int x1 = source.getInteger();
            source.call(new Generator4());
            int x2 = source.getInteger();
            source.call(new Generator4());

            return new TestTraceStructure3(x1, x2);
        }
    }

    public static class Generator4 extends AbstractGenerator<Boolean> {
        @Override
        protected Boolean gen(GenerationSource source) {
            return true;
        }
    }

    public static class Generator5 extends AbstractGenerator<Boolean> {
        @Override
        protected Boolean gen(GenerationSource source) {
            source.call(new Generator4());
            return true;
        }
    }

    Trace trace;
    TestTraceStructure1 traceStructure;

    @BeforeEach
    public void setup() {
        RecordingSource recordingSource = new RecordingSource();

        traceStructure = recordingSource.call(new Generator1());
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        ReplayingSource replayingSource1 = new ReplayingSource(recordedHistory);
        replayingSource1.call(new Generator1());

        trace = replayingSource1.finalTrace();
    }

    @Test
    @DisplayName("chaseToAtomIndex(0) is the same as focus()")
    public void chasingToIndex0IsFocus() {
        TraceZipper zipper = trace.toZipper();
        assertEquals(zipper.focus(), zipper.chaseToAtomIndex(0).chain(TraceZipper::focus));
    }

    @Test
    @DisplayName("Trace matches expected structure")
    public void tracesMatch() {
        TraceZipper zipper = trace.toZipper();

        assertFocusValue(zipper, traceStructure.firstSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(1), traceStructure.firstSub.second);
        assertFocusValue(zipper.chaseToAtomIndex(2), traceStructure.first);
        assertFocusValue(zipper.chaseToAtomIndex(3), traceStructure.secondSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(4), traceStructure.secondSub.second);
        assertFocusValue(zipper.chaseToAtomIndex(5), traceStructure.secondSub.firstSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(6), traceStructure.secondSub.firstSub.second);
        assertFocusValue(zipper.chaseToAtomIndex(7), traceStructure.secondSub.third);
        assertFocusValue(zipper.chaseToAtomIndex(8), traceStructure.secondSub.secondSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(9), traceStructure.secondSub.secondSub.second);

        assertFocusValue(zipper.chaseToAtomIndex(10), traceStructure.thirdSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(11), traceStructure.thirdSub.second);
        assertFocusValue(zipper.chaseToAtomIndex(12), traceStructure.thirdSub.firstSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(13), traceStructure.thirdSub.firstSub.second);
        assertFocusValue(zipper.chaseToAtomIndex(14), traceStructure.thirdSub.third);
        assertFocusValue(zipper.chaseToAtomIndex(15), traceStructure.thirdSub.secondSub.first);
        assertFocusValue(zipper.chaseToAtomIndex(16), traceStructure.thirdSub.secondSub.second);

        assertFocusValue(zipper.chaseToAtomIndex(17), traceStructure.second);
        assertFocusValue(zipper.chaseToAtomIndex(18), traceStructure.third);

        assertEquals(zipper.chaseToAtomIndex(19), Maybe.empty());
    }

    public void assertFocusValue(TraceZipper zipper, int value) {
        Maybe<DrawAtom> focus = zipper.focus();
        switch (focus) {
            case Nothing() -> throw new RuntimeException("Assertion failed");
            case Just(DrawAtom atom) -> {
                ConcreteIntegerGenerator intGen = new ConcreteIntegerGenerator(() -> atom);
                assertEquals(intGen.gen(), value);
            }
        }
    }

    public void assertFocusValue(Maybe<TraceZipper> maybeZipper, int value) {
        switch (maybeZipper) {
            case Nothing() -> throw new RuntimeException("Expected a non-empty Maybe<TraceZipper>");
            case Just(var zipper) -> assertFocusValue(zipper, value);
        }
    }
}
