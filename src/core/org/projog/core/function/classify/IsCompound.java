package org.projog.core.function.classify;

import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;

/* SYSTEM TEST
 % %TRUE% compound(a(b,c))
 % %TRUE% compound(1+1)
 % %TRUE% compound([a,b,c])
 % %TRUE% compound([])
 % %FALSE% compound(abc)
 % %FALSE% compound(1)
 % %FALSE% compound(1.5)
 % %FALSE% compound(X)
*/
/**
 * <code>compound(X)</code> - checks that a term is a compound term.
 * <p>
 * <code>compound(X)</code> succeeds if <code>X</code> currently stands for a compound term.
 * </p>
 */
public final class IsCompound extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term... args) {
      return evaluate(args[0]);
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate(Term arg) {
      return arg.getType().isStructure();
   }
}