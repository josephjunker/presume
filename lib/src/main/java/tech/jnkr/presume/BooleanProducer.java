package tech.jnkr.presume;

class BooleanProducer implements Producer<Boolean> {
    public Boolean produce(DrawAtom atom) {
        return switch (atom) {
            case Trivial1(), Trivial2() -> false;
            case Regular(float ratio, _, _) -> ratio < 0.5;
            default -> true;
        };
    }
}
