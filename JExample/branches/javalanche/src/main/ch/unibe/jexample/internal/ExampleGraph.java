package ch.unibe.jexample.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

import ch.unibe.jexample.JExample;
import ch.unibe.jexample.internal.graph.Node;
import ch.unibe.jexample.internal.util.CompositeRunner;
import ch.unibe.jexample.internal.util.JExampleError;
import ch.unibe.jexample.internal.util.MethodReference;

/**
 * Validates, describes and runs all JExample tests. Implemented as a singleton
 * in order to persist results between runs of many classes. JUnit's eclipse
 * plug-in runs all classes of a package in one bunch, but restarts JUnit for
 * each new launch. Thus, this singleton should work for Eclipse.
 * 
 * @author Lea Haensenberger
 * @author Adrian Kuhn
 * 
 */
public class ExampleGraph {

    private static ExampleGraph GRAPH;
    
    public static ExampleGraph instance() {
        return GRAPH == null ? GRAPH = new ExampleGraph() : GRAPH;
    }
    private boolean anyHasBeenRun = false;
    private Map<Class<?>,ExampleClass> classes;

    private Map<MethodReference,Example> examples;

    public ExampleGraph() {
        this.examples = new HashMap<MethodReference,Example>();
        this.classes = new HashMap<Class<?>,ExampleClass>();
    }

    public ExampleClass add(Class<?> jclass) throws JExampleError {
        if (anyHasBeenRun) {
        	throwAwayResults();
        	anyHasBeenRun = false;
        }
        ExampleClass egClass = makeExampleClass(jclass);
        egClass.validate();
        egClass.initializeExamples();
        return egClass;
    }

    private void throwAwayResults() {
		for ( Example ex : examples.values()) {
			ex.resetReturnValue();
		}
	}

	public void filter(Filter filter) {
        Iterator<Example> it = examples.values().iterator();
        while (it.hasNext()) {
            if (!filter.shouldRun(it.next().getDescription())) {
                it.remove();
            }
        }
        // copy list of values to avoid concurrent modification
        Collection<Example> copy = new ArrayList<Example>(examples.values());
        for (Example e: copy) {
            for (Node<Example> node: e.node.transitiveClosure()) {
                examples.put(node.value.method, node.value);
            }
        }
    }

    public Example findExample(Class<?> c, String name) {
        Example found = null;
        for (Example e: getExamples()) {
            if (e.method.equals(c, name)) {
                if (found != null) throw new RuntimeException();
                found = e;
            }
        }
        return found;
    }

    public Example findExample(MethodReference ref) {
        for (Example eg: getExamples()) {
            if (ref.equals(eg.method)) return eg;
        }
        return null;
    }

    public Collection<Example> getExamples() {
        return this.examples.values();
    }

    public Collection<MethodReference> getMethods() {
        return this.examples.keySet();
    }

    public void run(ExampleClass testClass, RunNotifier notifier) {
        anyHasBeenRun = true;
        for (Example e: this.getExamples()) {
            if (testClass.contains(e)) {
                e.run(notifier);
            }
        }
    }

    public Result runJExample() {
        CompositeRunner runner = new CompositeRunner();
        for (ExampleClass eg: classes.values()) {
            runner.add(new JExample(eg));
        }
        return new JUnitCore().run(runner);
    }

    public Result runJExample(Class<?>... tests) throws JExampleError {
        for (Class<?> each: tests)
            add(each);
        return runJExample();
    }

    protected Example makeExample(MethodReference method) {
        Example e = examples.get(method);
        if (e != null) return e;
        e = new Example(method, makeExampleClass(method.getActualClass()));
        examples.put(method, e);
        e.initializeDependencies(this);
        return e;
    }

    protected ExampleClass makeExampleClass(Class<?> jclass) {
        ExampleClass egClass = classes.get(jclass);
        if (egClass == null) {
            egClass = new ExampleClass(jclass, this);
            classes.put(jclass, egClass);
        }
        return egClass;
    }

}
