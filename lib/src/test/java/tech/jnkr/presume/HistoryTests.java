package tech.jnkr.presume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.RepeatedTest;

import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.utilities.Cons;
import tech.jnkr.presume.internal.utilities.ImmutableList;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryTests {
    public static class TestGenerator1 extends AbstractGenerator<ArrayList<Integer>> {
        @Override
        @NonNull protected ArrayList<Integer> gen(@NonNull GenerationSource source) {
            int first = source.getInteger();
            int second = source.getInteger();
            ArrayList<Integer> subList1 =
                    source.getWeightedBoolean(0.7f)
                            ? source.call(new TestGenerator1())
                            : new ArrayList<>();
            ImmutableList<Integer> subList2 = ImmutableList.empty();
            while (source.getWeightedBoolean(0.7f)) {
                subList2 = new Cons<>(source.getInteger(), subList2);
            }

            return Stream.concat(
                            Stream.of(first, second),
                            Stream.concat(subList1.stream(), subList2.toArrayList().stream()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @RepeatedTest(100)
    public void slugParsingRoundTrips() {
        RecordingSource recordingSource = new RecordingSource();

        var generated = recordingSource.call(new TestGenerator1());
        History recordedHistory =
                new History(recordingSource.getHistory().map(ImmutableList::toArrayList));

        String historySlug = recordedHistory.toSlug();
        History fromSlug = History.fromSlug(historySlug);

        assertEquals(recordedHistory, fromSlug);
    }
}
