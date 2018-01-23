package test;

import java.util.ArrayList;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

public class NegWorld {

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
		keysOne.add(0.5 - Math.random());
		nodesOne.add(
			new FibonacciHeapNode<String>("Bar")
		);
		keysOne.add(0.5 - Math.random());
		nodesOne.add(
			new FibonacciHeapNode<String>("Wow")
		);
		keysOne.add(0.5 - Math.random());

		ArrayList<FibonacciHeapNode<String>> nodesTwo = new ArrayList<>(3);
		ArrayList<Double> keysTwo = new ArrayList<>(3);
		nodesTwo.add(
			new FibonacciHeapNode<String>("Foo")
		);
		keysTwo.add(0.5 - Math.random());
		nodesTwo.add(
			new FibonacciHeapNode<String>("Bar")
		);
		keysTwo.add(0.5 - Math.random());
		nodesTwo.add(
			new FibonacciHeapNode<String>("Wow")
		);
		keysTwo.add(0.5 - Math.random());

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

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		String foo2 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		String foo3 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		String foo4 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		String foo5 = hThree.removeMin().getData();

		System.out.println(hThree.size());
		System.out.println(hThree.min().getData());
		System.out.println(hThree.min().getKey());
		String foo6 = hThree.removeMin().getData();
		
		System.out.println(
				String.format("%s %s %s %s %s %s", foo, foo2, foo3, foo4, foo5, foo6));


	}

}
