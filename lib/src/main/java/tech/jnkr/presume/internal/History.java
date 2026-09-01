package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class History {
    public RoseTree<ArrayList<DrawAtom>> contents;

    public History(RoseTree<ArrayList<DrawAtom>> contents) {
        this.contents = contents;
    }

    public String toSlug() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            RoseTree<ImmutableList<DrawAtom>> contentsAsImmutable =
                    contents.map(ImmutableList::fromList);

            oos.writeObject(contentsAsImmutable);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static History fromSlug(String slug) {
        byte[] data = Base64.getDecoder().decode(slug);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

            ObjectInputFilter filter =
                    ObjectInputFilter.Config.createFilter(
                            "tech.jnkr.presume.internal.atoms.*;"
                                    + "tech.jnkr.presume.internal.utilities.*;"
                                    + "!*");

            ois.setObjectInputFilter(filter);

            RoseTree<ImmutableList<DrawAtom>> deserialized =
                    (RoseTree<ImmutableList<DrawAtom>>) ois.readObject();

            return new History(deserialized.map(ImmutableList::toArrayList));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof History history)) return false;
        return Objects.equals(contents, history.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contents);
    }
}
