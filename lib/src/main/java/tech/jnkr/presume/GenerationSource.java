package tech.jnkr.presume;

public interface GenerationSource {
    public boolean getBoolean();

    public int getInteger();

    public float getFloat();

    public double getDouble();

    public long getLong();

    public <T> T call(Generator<T> generator);
}
