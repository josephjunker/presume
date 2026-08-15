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
    @DisplayName("Trace matches expected structure")
    public void tracesMatch() {
        TraceZipper zipper = trace.toZipper();

        assertFocusValue(zipper, traceStructure.firstSub.first);
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
}
