package jexample.internal.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jexample.Depends;
import jexample.JExampleRunner;
import jexample.internal.Example;
import jexample.internal.ExampleGraph;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

public class TestDependencies {

    public static class A {
        
    }
    
    public static class B extends A {
        
    }
    
    @RunWith( JExampleRunner.class )
    public static class C {
        @Test
        public B empty() {
            return new B();
        }
        @Test
        @Depends("empty")
        public A test(A a) {
            return a;
        }
    }
    
    @Test
    public void testPolymorphicDepedency() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        $.add( C.class );
        
        //assertEquals( 1, $.getClasses().size() );
        assertEquals( 2, $.getMethods().size() );
        
        Example t = $.getExample( C.class, "test" );
        Example e = $.getExample( C.class, "empty" );
        
        assertNotNull(t);
        assertNotNull(e);
        assertEquals( 1, t.providers.size() );
        assertEquals( e, t.providers.iterator().next() );
    }
    
    @Test
    public void runPolymorphicDepedency() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        Result result = new JUnitCore().run($.newJExampleRunner(C.class));
        assertTrue( result.wasSuccessful() );
        assertEquals( 2, result.getRunCount() );
        
        Example t = $.getExample( C.class, "test" );
        Example e = $.getExample( C.class, "empty" );
 
        assertNotSame( e.returnValue, t.returnValue );
    }

    @RunWith( JExampleRunner.class )
    public static class D {
        @Test
        public B empty() {
            return new B();
        }
        @Test
        @Depends("empty")
        public B b(B b) {
            return b;
        }
        @Test
        @Depends("b")
        public A a(A a) {
            return a;
        }
    }

    @Test
    public void testPolymorphicDepedency2() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        $.add( D.class );
        
        //assertEquals( 1, $.getClasses().size() );
        assertEquals( 3, $.getMethods().size() );
        
        Example a = $.getExample( D.class, "a" );
        Example b = $.getExample( D.class, "b" );
        Example e = $.getExample( D.class, "empty" );
        
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(e);
        assertEquals( 1, b.providers.size() );
        assertEquals( e, b.providers.iterator().next() );
        assertEquals( 1, a.providers.size() );
        assertEquals( b, a.providers.iterator().next() );
    }

    @RunWith( JExampleRunner.class )
    public static class E_fail {
        @Test
        public A empty() {
            return new A();
        }
        @Test
        @Depends("empty")
        public B b(B b) {
            return b;
        }
    }
    
    @RunWith( JExampleRunner.class )
    public static class F extends D {
        @Test
        public A another() {
            return null;
        }
        @Override
        @Test
        @Depends("another")
        public B b(B b) {
            return b;
        }
    }
    
    @Test
    public void testFailPolymorphicDepedency() throws Exception {
        try {
            ExampleGraph $ = new ExampleGraph();
            $.add( E_fail.class );
        }
        catch (InitializationError err) {
            assertEquals( 1, err.getCauses().size() );
            return;
        }
        fail("InitializationError expected!");
    }
    
    @Test
    @Ignore
    public void testPolymorphicDepedency3() throws Exception {
        ExampleGraph $ = new ExampleGraph();
        $.add( F.class );
        
        //assertEquals( 1, $.getClasses().size() );
        assertEquals( 3, $.getMethods().size() );
        
        Example a = $.getExample( F.class, "a" );
        Example b = $.getExample( F.class, "b" );
        Example x = $.getExample( F.class, "another" );
        
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(x);
        assertEquals( 1, b.providers.size() );
        assertEquals( x, b.providers.iterator().next() );
        assertEquals( 1, a.providers.size() );
        assertEquals( b, a.providers.iterator().next() );
    }
    
}