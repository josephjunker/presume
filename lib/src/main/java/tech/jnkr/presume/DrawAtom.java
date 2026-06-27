package tech.jnkr.presume;

import java.util.stream.Stream;

sealed interface DrawAtom permits Trivial1, Trivial2, Regular, Edge {
    default Stream<DrawAtom> shrink() {
        return switch (this) {
            case Trivial1() -> Stream.of();
            case Trivial2() -> Stream.of(new Trivial1());
            case Regular(float ratio, boolean sign, boolean simplify) -> {
                Stream<DrawAtom> trivials = Stream.of(new Trivial1(), new Trivial2());
                Stream<DrawAtom> flags = simplifyFlags(ratio, sign, simplify);
                Stream<Regular> reduced =
                        Stream.iterate(
                                new Regular(ratio / 2f, sign, simplify),
                                r -> new Regular(r.ratio() / 2f, r.sign(), r.simplify()));

                yield Stream.concat(
                        Stream.concat(trivials, flags),
                        reduced.flatMap(
                                r ->
                                        Stream.concat(
                                                Stream.of(r),
                                                simplifyFlags(r.ratio(), r.sign(), r.simplify()))));
            }
            case Edge(float ratio, boolean sign) ->
                    Stream.of(new Trivial1(), new Trivial2(), new Regular(ratio, sign, false));
        };
    }

    private Stream<DrawAtom> simplifyFlags(float ratio, boolean sign, boolean simplify) {
        if (sign && simplify) return Stream.of();
        if (!sign && !simplify)
            return Stream.of(
                    new Regular(ratio, true, true),
                    new Regular(ratio, true, false),
                    new Regular(ratio, false, true));
        if (!sign)
            return Stream.of(new Regular(ratio, true, true), new Regular(ratio, true, false));

        return Stream.of(new Regular(ratio, true, true), new Regular(ratio, false, true));
    }
}

record Trivial1() implements DrawAtom {}

record Trivial2() implements DrawAtom {}

record Regular(float ratio, boolean sign, boolean simplify) implements DrawAtom {}

record Edge(float ratio, boolean sign) implements DrawAtom {}
