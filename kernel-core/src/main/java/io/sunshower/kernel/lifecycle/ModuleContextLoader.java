package io.sunshower.kernel.lifecycle;

import io.sunshower.kernel.dependencies.DependencyGraph;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;

@AllArgsConstructor
public class ModuleContextLoader {

  final DependencyGraph dependencyGraph;

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidInstantiatingObjectsInLoops"})
  public ModuleLoader createModuleContext() {
    val results = new ModuleFinder[dependencyGraph.size()];
    val moduleIterator = dependencyGraph.iterator();
    var i = 0;
    while (moduleIterator.hasNext()) {
      results[i++] = new KernelModuleFinder(moduleIterator.next());
    }
    return new ModuleLoader(results);
  }
}
