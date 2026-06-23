package tech.jnkr.presume;

import java.util.ArrayList;
import java.util.Random;

public abstract class Generator<T> {
    protected abstract T gen(Drawer drawer);

    protected static class Drawer {
        private final ArrayList<PrimitiveDraw> history = new ArrayList<>();
        private final ArrayList<Drawer> children = new ArrayList<>();
        private final Random random = new Random();

        Drawer() {}

        public boolean genBoolean() {
            boolean result = random.nextBoolean();
            history.add(new BooleanDraw(result));
            return result;
        }

        public int genInt() {
            int result = random.nextInt();
            history.add(new IntDraw(result));
            return result;
        }

        public float genFloat() {
            float result = random.nextFloat();
            history.add(new FloatDraw(result));
            return result;
        }

        public double genDouble() {
            double result = random.nextDouble();
            history.add(new DoubleDraw(result));
            return result;
        }

        public long genLong() {
            long result = random.nextLong();
            history.add(new LongDraw(result));
            return result;
        }

        public <T> T call(Generator<T> generator) {
            Drawer childDrawer = new Drawer();
            children.add(childDrawer);
            return generator.gen(childDrawer);
        }
    }
}
