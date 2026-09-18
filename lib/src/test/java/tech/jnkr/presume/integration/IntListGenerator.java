package tech.jnkr.presume.integration;

import org.jspecify.annotations.NonNull;

import tech.jnkr.presume.AbstractGenerator;
import tech.jnkr.presume.GenerationSource;

import java.util.ArrayList;

public class IntListGenerator extends AbstractGenerator<ArrayList<Integer>> {
    @Override
    protected ArrayList<Integer> gen(@NonNull GenerationSource source) {
        int length = source.getInteger(0, 10);
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            result.add(source.getInteger());
        }

        return result;
    }
}
