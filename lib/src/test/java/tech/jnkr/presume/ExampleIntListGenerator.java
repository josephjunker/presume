package tech.jnkr.presume;

import tech.jnkr.presume.generators.GenerationSource;

import java.util.ArrayList;

public class ExampleIntListGenerator extends AbstractGenerator<ArrayList<Integer>> {
    @Override
    protected ArrayList<Integer> gen(GenerationSource source) {
        // TODO: remove this. checking whether the first value doesn't shrink
        source.getInteger();
        int length = source.getInteger(0, 10);
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            result.add(source.getInteger());
        }

        return result;
    }
}
