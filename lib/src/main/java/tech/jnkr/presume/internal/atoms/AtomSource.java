package tech.jnkr.presume.internal.atoms;

import java.util.Random;

public class AtomSource {
    private final Random random = new Random();

    public DrawAtom getAtom() {
        float index = random.nextFloat();
        if (index < 0.05f) return new Trivial1();
        if (index < 0.1f) return new Trivial2();
        if (index < 0.9f) return new Regular(random.nextFloat(), random.nextBoolean(), false);
        return new Edge(random.nextFloat(), random.nextBoolean());
    }
}
