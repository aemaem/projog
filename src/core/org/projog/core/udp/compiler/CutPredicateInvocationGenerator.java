/*
 * Copyright 2013-2014 S. Webber
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projog.core.udp.compiler;

/**
 * Outputs java code that matches functionality of {@link org.projog.core.function.flow.Cut}
 */
final class CutPredicateInvocationGenerator implements PredicateInvocationGenerator {
   @Override
   public void generate(CompiledPredicateWriter g) {
      if (g.factMetaData().isSingleResultPredicate() == false && g.currentClause().isSingleResult() == true) {
         g.assignTrue("isCut");
      } else if (g.currentClause().isAfterLastMulipleResultConjuction()) {
         g.assignTrue("isCut");
      }
   }
}
