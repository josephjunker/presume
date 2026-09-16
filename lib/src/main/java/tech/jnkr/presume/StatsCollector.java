package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.GeneratorStatsAssertionFailureException;

import java.util.*;
import java.util.function.Function;

public class StatsCollector<T> {
    private final AbstractGenerator<T> generator;
    private final ArrayList<BaseBucketer<T>> bucketers;

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
        bucketers.forEach(BaseBucketer::print);

        bucketers.forEach(BaseBucketer::validate);
    }

    private record BucketWithCount(String name, int count) {}

    private abstract static sealed class BaseBucketer<T>
            permits BooleanBucketer, EnumBucketer, IntegerBucketer {
        protected abstract void consume(T input);

        abstract List<BucketWithCount> getBucketCounts();

        protected abstract List<String> getBucketNames();

        protected abstract int getCountForBucket(String name);

        protected abstract String getTitle();

        protected final Map<String, Float> minimumRatioAssertions = new HashMap<>();
        protected final Map<String, Float> maximumRatioAssertions = new HashMap<>();

        protected void print() {
            List<BucketWithCount> entries = getBucketCounts();

            int maxLabelLength =
                    entries.stream()
                            .map(entry -> entry.name().length())
                            .max(Comparator.naturalOrder())
                            .orElse(0);

            int totalEntries = entries.stream().mapToInt(BucketWithCount::count).sum();

            int leftMargin = maxLabelLength + 3;

            System.out.println(getTitle());
            for (int i = 0; i < getTitle().length(); i++) {
                System.out.print("-");
            }
            System.out.println();
            for (String bucketName : getBucketNames()) {
                int value = getCountForBucket(bucketName);

                int padding = leftMargin - bucketName.length();
                int percent = (int) ((float) value * 100 / (float) totalEntries);

                System.out.print(bucketName);
                for (int i = 0; i < padding; i++) {
                    System.out.print(".");
                }
                System.out.printf("%d%%", percent);
                System.out.println();
            }
            System.out.println();
        }

        protected void validate() {
            float totalEntries = getBucketCounts().stream().mapToInt(BucketWithCount::count).sum();

            for (var entry : minimumRatioAssertions.entrySet()) {
                float value = getCountForBucket(entry.getKey());
                float actualRatio = value / totalEntries;
                float expectedRatio = entry.getValue();
                if (actualRatio < expectedRatio)
                    throw GeneratorStatsAssertionFailureException.minimumFailed(
                            getTitle(), entry.getKey(), expectedRatio, actualRatio);
            }

            for (var entry : maximumRatioAssertions.entrySet()) {
                float value = getCountForBucket(entry.getKey());
                float actualRatio = value / totalEntries;
                float expectedRatio = entry.getValue();
                if (actualRatio > expectedRatio)
                    throw GeneratorStatsAssertionFailureException.maximumFailed(
                            getTitle(), entry.getKey(), expectedRatio, actualRatio);
            }
        }
    }

    public static final class IntegerBucketer extends BaseBucketer<Integer> {
        private final String title;
        private final int bucketCount;
        private final int bucketSize;
        private final int min;
        private final HashMap<String, Integer> counts;

        private IntegerBucketer(String title, int min, int max, int bucketCount) {
            super();
            this.title = title;
            this.bucketCount = bucketCount;
            this.min = min;
            bucketSize = (max - min) / bucketCount;

            counts = new HashMap<>();
        }

        private String getBucketName(int index) {
            int bucketMin = index * bucketSize + min;
            int bucketMax = bucketMin + bucketSize;
            return String.format("%d to %d", bucketMin, bucketMax);
        }

        @Override
        protected void consume(Integer input) {
            int index = (input - min) / bucketSize;
            String bucketName = getBucketName(index);
            counts.compute(bucketName, (name, value) -> Objects.isNull(value) ? 1 : value + 1);
        }

        @Override
        List<BucketWithCount> getBucketCounts() {
            ArrayList<BucketWithCount> result = new ArrayList<>();
            for (int i = 0; i < bucketCount; i++) {
                String bucketName = getBucketName(i);
                result.add(new BucketWithCount(bucketName, counts.getOrDefault(bucketName, 0)));
            }
            return result;
        }

        @Override
        protected List<String> getBucketNames() {
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < bucketCount; i++) {
                result.add(getBucketName(i));
            }
            return result;
        }

        @Override
        protected int getCountForBucket(String name) {
            return counts.getOrDefault(name, 0);
        }

        @Override
        protected String getTitle() {
            return title;
        }
    }

    public static final class BooleanBucketer<T> extends BaseBucketer<T> {
        private final String title;
        private final Function<T, Boolean> discriminator;
        private int trueCount = 0;
        private int falseCount = 0;

        private BooleanBucketer(String title, Function<T, Boolean> discriminator) {
            this.title = title;
            this.discriminator = discriminator;
        }

        public BooleanBucketer<T> withMinimumTrueRatio(float ratio) {
            minimumRatioAssertions.compute(
                    "TRUE",
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));
            return this;
        }

        public BooleanBucketer<T> withMaximumTrueRatio(float ratio) {
            maximumRatioAssertions.compute(
                    "TRUE",
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));
            return this;
        }

        public BooleanBucketer<T> withMinimumFalseRatio(float ratio) {
            minimumRatioAssertions.compute(
                    "FALSE",
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));
            return this;
        }

        public BooleanBucketer<T> withMaximumFalseRatio(float ratio) {
            minimumRatioAssertions.compute(
                    "FALSE",
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));
            return this;
        }

        @Override
        protected void consume(T input) {
            if (discriminator.apply(input)) {
                trueCount++;
            } else {
                falseCount++;
            }
        }

        @Override
        List<BucketWithCount> getBucketCounts() {
            return List.of(
                    new BucketWithCount("TRUE", trueCount),
                    new BucketWithCount("FALSE", falseCount));
        }

        @Override
        protected List<String> getBucketNames() {
            return List.of("TRUE", "FALSE");
        }

        @Override
        protected int getCountForBucket(String name) {
            if (name.equals("TRUE")) return trueCount;
            if (name.equals("FALSE")) return falseCount;
            throw new IllegalArgumentException("TILT: Tried to get bad name from boolean bucket");
        }

        @Override
        protected String getTitle() {
            return title;
        }
    }

    public static final class EnumBucketer<T, Buckets extends Enum<Buckets>>
            extends BaseBucketer<T> {
        private final String title;
        private final HashMap<Buckets, Integer> buckets;
        private final Function<T, Buckets> discriminator;
        private final Buckets[] bucketInEnumOrder;
        private final Class<Buckets> bucketsEnum;

        EnumBucketer(String title, Class<Buckets> bucketsEnum, Function<T, Buckets> discriminator) {
            super();
            this.title = title;
            buckets = new HashMap<>();
            this.discriminator = discriminator;
            bucketInEnumOrder = bucketsEnum.getEnumConstants();
            this.bucketsEnum = bucketsEnum;

            for (Buckets bucket : bucketsEnum.getEnumConstants()) {
                buckets.put(bucket, 0);
            }
        }

        @Override
        List<BucketWithCount> getBucketCounts() {
            return new ArrayList<>(buckets.entrySet())
                    .stream()
                            .map(
                                    entry ->
                                            new BucketWithCount(
                                                    entry.getKey().name(), entry.getValue()))
                            .toList();
        }

        @Override
        protected List<String> getBucketNames() {
            return Arrays.stream(bucketInEnumOrder).map(Enum::name).toList();
        }

        @Override
        protected int getCountForBucket(String name) {
            return buckets.get(Enum.valueOf(bucketsEnum, name));
        }

        @Override
        protected String getTitle() {
            return title;
        }

        protected void consume(T t) {
            Buckets bucket = discriminator.apply(t);
            increment(bucket);
        }

        private void increment(Buckets bucket) {
            buckets.put(bucket, buckets.get(bucket) + 1);
        }

        public EnumBucketer<T, Buckets> withMinimumRatio(Buckets bucket, float ratio) {
            minimumRatioAssertions.compute(
                    bucket.name(),
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.max(existing, ratio));

            return this;
        }

        public EnumBucketer<T, Buckets> withMaximumRatio(Buckets bucket, float ratio) {
            maximumRatioAssertions.compute(
                    bucket.name(),
                    (key, existing) ->
                            Objects.isNull(existing) ? ratio : Math.min(existing, ratio));

            return this;
        }
    }
}
