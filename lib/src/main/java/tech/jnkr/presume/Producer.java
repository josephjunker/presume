package tech.jnkr.presume;

interface Producer<T> {
    T produce(DrawAtom atom);
}
