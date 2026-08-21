package tech.jnkr.presume.harness;

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
        // if (expr instanceof Expr.Mul(Expr left, Expr right)) System.out.println("foo");
        // if (expr instanceof Expr.Mul(Expr left, Expr right)) System.out.println(left);
        // if (expr instanceof Expr.Mul(Expr left, Expr right)) System.out.println(right);

        if (expr instanceof Expr.Mul(Expr left, Expr right)
                && left instanceof Expr.Add
                && right instanceof Expr.Neg) return 99;

        return eval(expr);
    }
}
