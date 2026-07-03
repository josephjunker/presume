package tech.jnkr.presume.internal;

import org.jspecify.annotations.Nullable;

import tech.jnkr.presume.internal.atoms.DrawAtom;

import java.util.LinkedList;
import java.util.List;

public class HistoryCursor {
    private @Nullable DrawAtom focus;
    private LinkedList<DrawAtom> leftSiblings;
    private LinkedList<DrawAtom> rightSiblings;
    private LinkedList<History> children;
    private LinkedList<Breadcrumb> breadcrumbs;
    private LinkedList<History> leftSiblingHistories;
    private LinkedList<History> rightSiblingHistories;

    public HistoryCursor(History history) {
        this.focus = history.draws.getFirst();
        this.leftSiblings = new LinkedList<>();
        this.rightSiblings = new LinkedList<>(history.draws.subList(1, history.draws.size()));
        this.children = new LinkedList<>(history.children);
        this.breadcrumbs = new LinkedList<>();
        this.leftSiblingHistories = new LinkedList<>();
        this.rightSiblingHistories = new LinkedList<>();
    }

    public boolean goRight() {
        if (focus == null) return false;

        leftSiblings.push(focus);
        focus = rightSiblings.pop();

        return true;
    }

    private boolean goDown() {
        if (children.isEmpty()) return false;

        if (focus != null) rightSiblings.push(focus);
        while (!leftSiblings.isEmpty()) rightSiblings.push(leftSiblings.pop());

        breadcrumbs.push(
                new Breadcrumb(rightSiblings, leftSiblingHistories, rightSiblingHistories));

        return true;
    }

    private record Breadcrumb(
            List<DrawAtom> node, List<History> leftSiblings, List<History> rightSiblings) {}
}
