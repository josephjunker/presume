package tech.jnkr.presume.integration;

public class ExprOperations {
    public static int eval(Expr expr) {
        return switch (expr) {
            case Expr.Lit(int x) -> x;
            case Expr.Add(Expr left, Expr right) -> eval(left) + eval(right);
            case Expr.Mul(Expr left, Expr right) -> eval(left) * eval(right);
            case Expr.Neg(Expr inner) -> -eval(inner);
        };
    }

    public static int buggyEval1(Expr expr) {
        if (expr instanceof Expr.Mul(Expr left, Expr right)
                && left instanceof Expr.Add
                && right instanceof Expr.Neg) return 99;

        return eval(expr);
    }

    public static int maxDepth(Expr expr) {
        return switch (expr) {
            case Expr.Lit(int ignoredX) -> 1;
            case Expr.Add(Expr left, Expr right) -> Math.max(maxDepth(left), maxDepth(right)) + 1;
            case Expr.Mul(Expr left, Expr right) -> Math.max(maxDepth(left), maxDepth(right)) + 1;
            case Expr.Neg(Expr inner) -> maxDepth(inner) + 1;
        };
    }

    public static boolean containsAdd(Expr expr) {
        return switch (expr) {
            case Expr.Lit(int ignoredX) -> false;
            case Expr.Add(Expr ignoredLeft, Expr ignoredRight) -> true;
            case Expr.Mul(Expr left, Expr right) -> containsAdd(left) || containsAdd(right);
            case Expr.Neg(Expr inner) -> containsAdd(inner);
        };
    }

    public static boolean containsMul(Expr expr) {
        return switch (expr) {
            case Expr.Lit(int ignoredX) -> false;
            case Expr.Add(Expr left, Expr right) -> containsMul(left) || containsMul(right);
            case Expr.Mul(Expr ignoredLeft, Expr ignoredRight) -> true;
            case Expr.Neg(Expr inner) -> containsMul(inner);
        };
    }

    public static boolean containsNeg(Expr expr) {
        return switch (expr) {
            case Expr.Lit(int ignoredX) -> false;
            case Expr.Add(Expr left, Expr right) -> containsNeg(left) || containsNeg(right);
            case Expr.Mul(Expr left, Expr right) -> containsNeg(left) || containsNeg(right);
            case Expr.Neg(Expr ignoredInner) -> true;
        };
    }
}
