package tech.jnkr.presume.internal.atoms;

import java.util.Random;

public class AtomSource {
    private final Random random = new Random();

    public DrawAtom getAtom() {
        float index = random.nextFloat();
        if (index < 0.02f) return new Trivial1();
        if (index < 0.04f) return new Trivial2();
        if (index < 0.95f)
            return new Regular(Math.abs(random.nextInt()), random.nextBoolean(), false);
        return new Edge(Math.abs(random.nextInt()), random.nextBoolean());
    }
}
