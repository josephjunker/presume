package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.function.Predicate;

public class Counterexample {
    private final RoseTree<ImmutableList<DrawAtom>> state;

    public Counterexample(RoseTree<ImmutableList<DrawAtom>> state) {
        this.state = state;
    }

    // public Maybe<Counterexample> shrink(Predicate<RoseTree<ImmutableList<DrawAtom>>> reproduces)
    // {}

    private Maybe<ImmutableList<DrawAtom>> shrinkSingleNode(
            Predicate<ImmutableList<DrawAtom>> reproduces) {

        /*

        I want to try to shrink the first item up to 5 times.

        If it reproduces after a shrink: move to the next atom.

        If it does not reproduce after 5 shrinks: move to the next atom.


          HEre's the problem with the rose tree approach: Yes, it keeps shrinks from pulling values
          across the boundary between generators, but it also hides the order that we should shrink in.
          We want to apply shrinking exactly moving forward in time. Consider: Gen A calls gen B to get
          an array of values. B returns 10 items, and A does something for each. Going in rose tree order,
          A would end up shrinking 10 processing results down to their smallest values before we ever tried
          to shrink the array down to a length of 0 or 1. What we actually want to do is shrink the internals
          of B before we try to shrink the rest of A.

          If we want to avoid pulling atoms across the boundaries between generations, we need to track
          both the draw order (a list) and the generator dependencies (a tree).

          I *think* each item in the list is a reference to a source (that generator's source). If you traced
          out all of the object references there would be a tree structure, but I don't think I actually
          need an explicit tree. Still mulling on this.


          Going back, the reason I don't want to make atoms get pulled across generators is basically this:
          Generator A does some stuff, and then calls Generator B. If A shrinks, and then calls B witht the
          same arguments as it did originally, I want B to return the same input that it did originally.
          This means that B's internal state should be different from A's.

          There's a problem here: How do we know which generator B "is" after a shrink? With the rose tree we
          have the order in which generators were invoked, and we assume that they match up across shrinks.
          As in, the first subgenerator we invoke will be B, the next C, etc., no matter how many times we
          do this. I don't see a good alternative to this assumption, unfortunately. It's definitely wrong but
          the only alternative would be to somehow track the classes being used for each subgeneration, and
          it just sounds like a mess and dead end. I think the assumption that generators will line up will be
          true often enough to make this still be helpful.

          So, okay, we still do need the tree then. Each source needs to know what order to pull its sub-generators
          in.


          Trying again: Shrinking takes a history and returns a new history. Shrinking an individual atom still
          returns a whole history, not just an atom, because we're capturing both the change and the reduced number
          of generations used/available before we deplete each source in the tree. The challenge I need to solve is,
          if I'm being driven by a list during shrinking, and driven by a tree in generation, how do I relate the two?

          Possibly: I execute generation based on a tree, and capture a list as I go. The list is not just of the draw
          atoms themselves, because I need to relate the contents of the list to the contents of the tree. I'm thinking
          that one way to do this would be to make the list either a list of coordinates in the tree, a list of zippers,
          or a list of breadcrumbs saying what direction the next "move" through the tree is in.

          The breadcrumb approach seems the most promising? During shrinking, if I want to shrink atom N, I follow the
          breadcrumbs N steps through the tree, building up a zipper as I go. Then, I test 5 shrinks on the focus of
          the zipper: each time I turn the zipper back to a tree and pass it to the replaying source.

          In the replaying source I have two options for building up the next recording: I can put atoms in mutable
          containers, keeping a reference to the last container, and update that container when I do my next generation,
          specifying what direction I moved in. This could be tricky with moving "up", because I'm not clear on how
          I would detect that.

          The second option would be to track two parallel trees in the source: a tree of atoms, and a tree of
          breadcrumbs. When a replay is finished I would take the two and zip them together, or use the breadcrumb
          tree to drive the zipper navigation of the atom tree.

          Really there are only two directions I can track: moving right, and moving down. The only time we move up
          is when a subgenerator completes, so we can use getting to the end of the list as our signal there. We can
          detect moving down before we move, because we know we're facing a subgenerator call. So really we don't
          need to split the detection across multiple invocations.

          Since we don't need to split across multiple invocations, we don't need mutability. I don't like the idea
          of trying to zip two RoseTree<ImmutableList<DrawAtom>> structures together without a static guarantee that they match.
          So the solution is that the RecordingSource will hold RoseTree<ImmutableList<DrawAtomBreadcrumb>>, and
          DrawAtomBreadcrum holds ["right" | "down", DrawAtom]. Making these linked lists means we'll need to reverse them
          before shrinking but that's not a big deal.

          We don't want to do this extra bookkeeping in RecordingSource, only ReplayingSource, because we want to keep
          basic generation fast and only put in extra work during the shrinking phase. So the first time we have a failure
          we need to replay the test unshrunk once to gather our extra tracking info. This isn't a bad thing, it'll help
          us detect flaky tests anyway.

          This is not quite right: the history needs to be mutable during generation, because otherwise we have no
          way of having a child generator push values into the history of parent generators. So we need a
          MutableRoseTree class.
         */

    }
}
