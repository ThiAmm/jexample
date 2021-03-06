package ch.unibe.jexample.internal.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import ch.unibe.jexample.internal.Example;
import ch.unibe.jexample.internal.ExampleGraph;
import ch.unibe.jexample.internal.util.JExampleError;
import ch.unibe.jexample.internal.util.JExampleError.Kind;

public class DependenciesTest {

    static class A {

    }

    static class B extends A {

    }

    @RunWith(JExample.class)
    static class C {
        @Test
        public B empty() {
            return new B();
        }

        @Given("empty")
        public A test(A a) {
            return a;
        }
    }

    @Test
    public void testPolymorphicDepedency() throws Exception {
        ExampleGraph egg = new ExampleGraph();
        egg.add(C.class);

        assertEquals(2, egg.getMethods().size());

        Example t = egg.findExample(C.class, "test");
        Example e = egg.findExample(C.class, "empty");

        assertNotNull(t);
        assertNotNull(e);
        assertEquals(1, t.producers().size());
        assertEquals(e, t.producers().first());
    }

    @Test
    public void runPolymorphicDepedency() throws Exception {
        ExampleGraph g = new ExampleGraph();
        g.add(C.class);
        Example t = g.findExample(C.class, "test");
        Example e = g.findExample(C.class, "empty");
        t.beSticky();
        e.beSticky();
        Result result = g.runJExample();
        assertTrue(result.wasSuccessful());
        assertEquals(2, result.getRunCount());
        assertNotSame(e.returnValue(), t.returnValue());
    }

    @RunWith(JExample.class)
    static class D {
        @Test
        public B empty() {
            return new B();
        }

        @Given("empty")
        public B b(B b) {
            return b;
        }

        @Given("b")
        public A a(A a) {
            return a;
        }
    }

    @Test
    public void testPolymorphicDepedency2() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        $.add(D.class);

        // assertEquals( 1, $.getClasses().size() );
        assertEquals(3, $.getMethods().size());

        Example a = $.findExample(D.class, "a");
        Example b = $.findExample(D.class, "b");
        Example e = $.findExample(D.class, "empty");

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(e);
        assertEquals(1, b.producers().size());
        assertEquals(e, b.producers().first());
        assertEquals(1, a.producers().size());
        assertEquals(b, a.producers().first());
    }

    @RunWith(JExample.class)
    static class E_fail {
        @Test
        public A empty() {
            return new A();
        }

        @Given("empty")
        public B b(B b) {
            return b;
        }
    }

    @RunWith(JExample.class)
    static class F extends D {
        @Test
        public B another() {
            return null;
        }

        @Override
        @Given("another")
        public B b(B b) {
            return b;
        }
    }

    @Test
    public void testParameterNotAssignableFromProvider() throws JExampleError {
        Class<?>[] classes = { E_fail.class };
        ExampleGraph g = new ExampleGraph();
        Result r = g.runJExample(classes);
        assertEquals(false, r.wasSuccessful());
        assertEquals(2, r.getRunCount());
        assertEquals(1, r.getFailureCount());
        JExampleError err = (JExampleError) r.getFailures().get(0).getException();
        assertEquals(1, err.size());
        assertEquals(Kind.PARAMETER_NOT_ASSIGNABLE, err.getKind());
    }

    @Test
    @Ignore("JExample does not (yet) support inheritence and overrides.")
    public void testPolymorphicDepedency3() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        $.add(F.class);

        // assertEquals( 1, $.getClasses().size() );
        assertEquals(5, $.getMethods().size());

        Example a = $.findExample(F.class, "a");
        Example b = $.findExample(F.class, "b");
        Example x = $.findExample(F.class, "another");

        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(x);
        assertEquals(1, b.producers().size());
        assertEquals(x, b.producers().first());
        assertEquals(1, a.producers().size());
        assertEquals(b, a.producers().first());
    }

    @RunWith(JExample.class)
    static class G {
        @Test(expected = Exception.class)
        public Object provider() throws Exception {
            throw new Exception();
        }

        @Given("provider")
        public void consumer(Object o) {
            // do nothing
        }
    }

    @Test
    public void providerMustNotExpectException() throws JExampleError {
        Class<?>[] classes = { G.class };
        ExampleGraph g = new ExampleGraph();
        Result r = g.runJExample(classes);
        assertEquals(false, r.wasSuccessful());
        assertEquals(2, r.getRunCount());
        assertEquals(1, r.getFailureCount());
        JExampleError err = (JExampleError) r.getFailures().get(0).getException();
        assertEquals(1, err.size());
        assertEquals(Kind.PROVIDER_EXPECTS_EXCEPTION, err.getKind());
    }

    @RunWith(JExample.class)
    static class H {
        @Test
        public Object provider() {
            return new Object();
        }

        @Given("provider")
        public void consumer(Object a, Object b) {
            // do nothing
        }
    }

    @Test
    public void lesProvidersThanParameters() throws JExampleError {
        Class<?>[] classes = { H.class };
        ExampleGraph g = new ExampleGraph();
        Result r = g.runJExample(classes);
        assertEquals(false, r.wasSuccessful());
        assertEquals(2, r.getRunCount());
        assertEquals(1, r.getFailureCount());
        JExampleError err = (JExampleError) r.getFailures().get(0).getException();
        assertEquals(1, err.size());
        assertEquals(Kind.MISSING_PROVIDERS, err.getKind());
    }

}
