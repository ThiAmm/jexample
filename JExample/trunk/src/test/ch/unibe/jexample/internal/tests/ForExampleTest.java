package ch.unibe.jexample.internal.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Stack;

import org.junit.Test;

import ch.unibe.jexample.For;
import ch.unibe.jexample.demo.StackTest;
import ch.unibe.jexample.demo.YouFail;

public class ForExampleTest {

    @Test(expected = Exception.class)
    public void cannotInstantiate() throws Exception {
        For.class.getDeclaredConstructor().newInstance();
    }

    @Test(expected = RuntimeException.class)
    public void noSuchTestClass() {
        For.example(Object.class, "#toString");
    }

    @Test
    public void forExampleStackEmpty() {
        Stack<String> stack = For.example(StackTest.class, "emptyStack");
        assertNotNull(null, stack);
        assertEquals(0, stack.size());
    }

    @Test
    public void forExampleStackEmpty2() {
        Stack<String> stack = For.example("ch.unibe.jexample.demo.StackTest#emptyStack");
        assertNotNull(null, stack);
        assertEquals(0, stack.size());
    }

    @Test
    public void forExampleStackWithValue() {
        Stack<String> stack = For.example(StackTest.class, "withValue");
        assertNotNull(null, stack);
        assertEquals(1, stack.size());
    }

    @Test
    public void forExampleStackWithManyValues() {
        Stack<String> stack = For.example(StackTest.class, "withManyValues");
        assertNotNull(null, stack);
        assertEquals(3, stack.size());
    }

    @Test(expected = RuntimeException.class)
    public void forExampleFail() {
        For.example(YouFail.class, "fail");
    }

}
