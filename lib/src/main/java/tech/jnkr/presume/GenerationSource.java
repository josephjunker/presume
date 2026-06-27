package tech.jnkr.presume;

public interface GenerationSource {
    public boolean genBoolean();

    public int genInt();

    public float genFloat();

    public double genDouble();

    public long genLong();

    public <T> T call(Generator<T> generator);
}
