package tech.jnkr.presume;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StatsCollector<T> {
    private final AbstractGenerator<T> generator;
    private final ArrayList<Bucketer<T, ?>> bucketers;

    public StatsCollector(AbstractGenerator<T> generator) {
        this.generator = generator;
        this.bucketers = new ArrayList<>();
    }

    public <Buckets extends Enum<Buckets>> StatsCollector<T> withBucket(
            Class<Buckets> bucketsEnum, String title, Function<T, Buckets> discriminator) {
        bucketers.add(new Bucketer<>(title, bucketsEnum, discriminator));

        return this;
    }

    public enum BooleanEnum {
        TRUE,
        FALSE
    }

    public StatsCollector<T> withBooleanBucket(String title, Function<T, Boolean> discriminator) {
        bucketers.add(
                new Bucketer<>(
                        title,
                        BooleanEnum.class,
                        t -> discriminator.apply(t) ? BooleanEnum.TRUE : BooleanEnum.FALSE));

        return this;
    }

    public void summarize(int runCount) {
        for (int i = 0; i < runCount; i++) {
            T value = generator.internalGen(new PureSource());
            bucketers.forEach(bucketer -> bucketer.consume(value));
        }

        System.out.println("STATS\n=====\n");
        bucketers.forEach(Bucketer::print);
    }

    public static class Bucketer<T, Buckets extends Enum<Buckets>> {
        private final String title;
        private final HashMap<Buckets, Integer> buckets;
        private final Function<T, Buckets> discriminator;
        private final Buckets[] bucketInEnumOrder;

        public Bucketer(
                String title, Class<Buckets> bucketsEnum, Function<T, Buckets> discriminator) {
            this.title = title;
            buckets = new HashMap<>();
            this.discriminator = discriminator;
            bucketInEnumOrder = bucketsEnum.getEnumConstants();

            for (Buckets bucket : bucketsEnum.getEnumConstants()) {
                buckets.put(bucket, 0);
            }
        }

        public void consume(T t) {
            Buckets bucket = discriminator.apply(t);
            increment(bucket);
        }

        private void increment(Buckets bucket) {
            buckets.put(bucket, buckets.get(bucket) + 1);
        }

        public void print() {
            ArrayList<Map.Entry<Buckets, Integer>> entries = new ArrayList<>(buckets.entrySet());

            int maxLabelLength =
                    entries.stream()
                            .map(entry -> entry.getKey().name().length())
                            .max(Comparator.naturalOrder())
                            .orElse(0);

            int totalEntries = entries.stream().mapToInt(Map.Entry::getValue).sum();

            int leftMargin = maxLabelLength + 3;

            System.out.println(title);
            for (int i = 0; i < title.length(); i++) {
                System.out.print("-");
            }
            System.out.println();
            for (Buckets bucket : bucketInEnumOrder) {
                int value = buckets.get(bucket);
                String name = bucket.name();

                int padding = leftMargin - name.length();
                int percent = (int) ((float) value * 100 / (float) totalEntries);

                System.out.print(name);
                for (int i = 0; i < padding; i++) {
                    System.out.print(".");
                }
                System.out.printf("%d%%", percent);
                System.out.println();
            }
            System.out.println();
        }
    }
}
