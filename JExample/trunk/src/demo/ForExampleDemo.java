package demo;

import java.util.Stack;

import jexample.For;

public class ForExampleDemo {

	public static void main(String[] args) {
		
		Stack stack = For.example(StackTest.class, "withValue");
		
		System.out.println(stack);
		
	}
	
}
