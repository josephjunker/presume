package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.GeneratorStatsAssertionFailureException;

import java.util.*;
import java.util.function.Function;

public class StatsCollector<T> {
    private final AbstractGenerator<T> generator;
    private final ArrayList<EnumBucketer<T, ?>> bucketers;

    public StatsCollector(AbstractGenerator<T> generator) {
        this.generator = generator;
        this.bucketers = new ArrayList<>();
    }

    public <Buckets extends Enum<Buckets>> EnumBucketer<T, Buckets> addEnumBucket(
            Class<Buckets> bucketsEnum, String title, Function<T, Buckets> discriminator) {
        var bucketer = new EnumBucketer<>(title, bucketsEnum, discriminator);
        this.bucketers.add(bucketer);

        return bucketer;
    }

    public enum BooleanEnum {
        TRUE,
        FALSE
    }

    public BooleanBucketer<T> addBooleanBucket(String title, Function<T, Boolean> discriminator) {
        BooleanBucketer<T> result = new BooleanBucketer<>(title, discriminator);

        bucketers.add(result);

        return result;
    }

    public void summarize(int runCount) {
        for (int i = 0; i < runCount; i++) {
            T value = generator.internalGen(new PureSource());
            bucketers.forEach(bucketer -> bucketer.consume(value));
        }

        System.out.println("STATS\n=====\n");
        bucketers.forEach(EnumBucketer::print);

        bucketers.forEach(EnumBucketer::validate);
    }

    public static class BooleanBucketer<T> extends EnumBucketer<T, BooleanEnum> {
        private BooleanBucketer(String title, Function<T, Boolean> discriminator) {
            super(
                    title,
                    BooleanEnum.class,
                    (T t) -> discriminator.apply(t) ? BooleanEnum.TRUE : BooleanEnum.FALSE);
        }

        public BooleanBucketer<T> withMinimumTrueRatio(float ratio) {
            super.withMinimumRatio(BooleanEnum.TRUE, ratio);
            return this;
        }

        public BooleanBucketer<T> withMaximumTrueRatio(float ratio) {
            super.withMaximumRatio(BooleanEnum.TRUE, ratio);
            return this;
        }

        public BooleanBucketer<T> withMinimumFalseRatio(float ratio) {
            super.withMinimumRatio(BooleanEnum.FALSE, ratio);
            return this;
        }

        public BooleanBucketer<T> withMaximumFalseRatio(float ratio) {
            super.withMaximumRatio(BooleanEnum.FALSE, ratio);
            return this;
        }
    }

    public static class EnumBucketer<T, Buckets extends Enum<Buckets>> {
        private final String title;
        private final HashMap<Buckets, Integer> buckets;
        private final Function<T, Buckets> discriminator;
        private final Buckets[] bucketInEnumOrder;
        private final HashMap<Buckets, Float> minimumRatioAssertions;
        private final HashMap<Buckets, Float> maximumRatioAssertions;

        protected EnumBucketer(
                String title, Class<Buckets> bucketsEnum, Function<T, Buckets> discriminator) {
            this.title = title;
            buckets = new HashMap<>();
            this.discriminator = discriminator;
            bucketInEnumOrder = bucketsEnum.getEnumConstants();

            for (Buckets bucket : bucketsEnum.getEnumConstants()) {
                buckets.put(bucket, 0);
            }

            minimumRatioAssertions = new HashMap<>();
            maximumRatioAssertions = new HashMap<>();
        }

        private void consume(T t) {
            Buckets bucket = discriminator.apply(t);
            increment(bucket);
        }

        private void increment(Buckets bucket) {
            buckets.put(bucket, buckets.get(bucket) + 1);
        }

        private void print() {
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

        private void validate() {
            float totalEntries = buckets.values().stream().mapToInt(i -> i).sum();

            for (var entry : minimumRatioAssertions.entrySet()) {
                float value = buckets.get(entry.getKey());
                float actualRatio = value / totalEntries;
                float expectedRatio = entry.getValue();
                if (actualRatio < expectedRatio)
                    throw GeneratorStatsAssertionFailureException.minimumFailed(
                            title, entry.getKey().name(), expectedRatio, actualRatio);
            }

            for (var entry : maximumRatioAssertions.entrySet()) {
                float value = buckets.get(entry.getKey());
                float actualRatio = value / totalEntries;
                float expectedRatio = entry.getValue();
                if (actualRatio > expectedRatio)
                    throw GeneratorStatsAssertionFailureException.maximumFailed(
                            title, entry.getKey().name(), expectedRatio, actualRatio);
            }
        }

        public EnumBucketer<T, Buckets> withMinimumRatio(Buckets bucket, float ratio) {
            minimumRatioAssertions.compute(
                    bucket,
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.max(existing, ratio));

            return this;
        }

        public EnumBucketer<T, Buckets> withMaximumRatio(Buckets bucket, float ratio) {
            maximumRatioAssertions.compute(
                    bucket,
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));

            return this;
        }
    }
}
