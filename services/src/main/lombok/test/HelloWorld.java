package test;

import java.util.ArrayList;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

public class HelloWorld {

	public static void main(String[] args) {
		System.out.println("Heyoo");
		
		// TODO Auto-generated method stub
		FibonacciHeap<String> hOne = new FibonacciHeap<>();
		FibonacciHeap<String> hTwo = new FibonacciHeap<>();
		
		ArrayList<FibonacciHeapNode<String>> nodesOne = new ArrayList<>(3);
		ArrayList<Double> keysOne = new ArrayList<>(3);
		nodesOne.add(
			new FibonacciHeapNode<String>("Foo")
		);
		keysOne.add(Math.abs(Math.random()));
		nodesOne.add(
			new FibonacciHeapNode<String>("Bar")
		);
		keysOne.add(Math.abs(Math.random()));
		nodesOne.add(
			new FibonacciHeapNode<String>("Wow")
		);
		keysOne.add(Math.abs(Math.random()));

		ArrayList<FibonacciHeapNode<String>> nodesTwo = new ArrayList<>(3);
		ArrayList<Double> keysTwo = new ArrayList<>(3);
		nodesTwo.add(
			new FibonacciHeapNode<String>("Foo")
		);
		keysTwo.add(Math.abs(Math.random()));
		nodesTwo.add(
			new FibonacciHeapNode<String>("Bar")
		);
		keysTwo.add(Math.abs(Math.random()));
		nodesTwo.add(
			new FibonacciHeapNode<String>("Wow")
		);
		keysTwo.add(Math.abs(Math.random()));

		hOne.insert(
			nodesOne.get(0), keysOne.get(0));
		hOne.insert(
			nodesOne.get(1), keysOne.get(1));
		hOne.insert(
			nodesOne.get(2), keysOne.get(2));

		hTwo.insert(
			nodesTwo.get(0), keysTwo.get(0));
		hTwo.insert(
			nodesTwo.get(1), keysTwo.get(1));
		hTwo.insert(
			nodesTwo.get(2), keysTwo.get(2));
		
		System.out.println(String.format("%f %f %f", keysOne.get(0), keysOne.get(1),keysOne.get(2)));
		System.out.println(String.format("%f %f %f", keysTwo.get(0), keysTwo.get(1),keysTwo.get(2)));
		
		FibonacciHeap<String> hThree = FibonacciHeap.union(hOne, hTwo);
		System.out.println(hOne.size());
		System.out.println(hOne.min());
		System.out.println(hTwo.size());
		System.out.println(hTwo.min());
		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		
		String foo = hThree.removeMin().getData();
		hThree.decreaseKey(
			new FibonacciHeapNode<String>(foo), -0.0002
		);
		if (foo.contentEquals("Foo")) {
			hThree.decreaseKey(nodesOne.get(0), 0.000);
		} else if(foo.contentEquals("Bar")) {
			hThree.decreaseKey(nodesOne.get(1), 0.0000);
		} else {
			hThree.decreaseKey(nodesOne.get(2), 0.0);
		}
		String foo2 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		foo = hThree.removeMin().getData();
		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());

		if (foo.contentEquals("Foo")) {
			hThree.decreaseKey(nodesTwo.get(0), 0.00030);
		} else if(foo.contentEquals("Bar")) {
			hThree.decreaseKey(nodesTwo.get(1), 0.00030);
		} else {
			hThree.decreaseKey(nodesTwo.get(2), 0.00030);
		}
		foo2 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		hThree.removeMin();
		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());


	}

}
