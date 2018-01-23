package test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public class Snippet {
	public static void main(String[] args) throws InterruptedException {
		TestScheduler myScheduler1 = Schedulers.test();
		TestScheduler myScheduler5 = Schedulers.test();
		Scheduler myScheduler25 = Schedulers.computation();
		Scheduler myScheduler2 = Schedulers.computation();
		Scheduler myScheduler7 = Schedulers.trampoline();
		TestScheduler myScheduler40 = Schedulers.test();
		Scheduler immediate = Schedulers.immediate();
		System.out.println("Foo");
		
		Observable<String> x1 = Observable.interval(100, TimeUnit.MILLISECONDS, myScheduler25)
				.map(r -> String.format("x1(%d)", 1+r)).take(100);
		Observable<String> x5 = Observable.interval(500, TimeUnit.MILLISECONDS, myScheduler25)
				.map(r -> String.format("x5(%d)", 1+r)).take(20);
		Observable<String> x7 = Observable.interval(700, TimeUnit.MILLISECONDS, myScheduler25)
				.map(r -> String.format("x7(%d)", 1+r)).take(14);
		Observable<String> x25 = Observable.interval(2500, TimeUnit.MILLISECONDS, myScheduler25)
				.map(r -> String.format("x25(%d)", 1+r)).take(4);
		Observable<String> x40 = Observable.interval(4000, TimeUnit.MILLISECONDS, myScheduler25)
				.map(r -> String.format("x40(%d)", 1+r)).take(2);
		
		Observable.merge(x1, x5, x7, x25, x40)
//			.observeOn(myScheduler7)
			.subscribeOn(myScheduler7)
//			.take(100 + 20 + 14 + 4 + 2Snippet.java)
//			.doOnNext(v -> { System.out.println( String.format("X %s", v)); })
			.buffer(100, TimeUnit.MILLISECONDS, myScheduler2)
//			.skip(1)
//			.observeOn(Schedulers.immediate())
			.map(r -> String.join(", ", r))
			.filter(r -> ! r.isEmpty())
			.toBlocking()
			.subscribe(n -> { System.out.println(String.format("Y(%d/%d): %s", myScheduler7.now(), myScheduler25.now(), n)); });
		
		Schedulers.start();
		
		System.out.println("Foo1");
		Thread.sleep(5000);
		System.out.println("Foo2");
		Thread.sleep(5000);
		System.out.println("Foo3");
		Thread.sleep(5000);
		System.out.println("Foo4");
		
		for(int ii=0; ii<101; ii++) {
			myScheduler1.advanceTimeBy(10, TimeUnit.MILLISECONDS);
			myScheduler5.advanceTimeBy(10, TimeUnit.MILLISECONDS);
		}
		/*
		myScheduler1.advanceTimeBy(10, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(10, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(10, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(20, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(20, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(70, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(70, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(150, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(150, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(250, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(250, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(500, TimeUnit.MILLISECONDS);
		myScheduler1.advanceTimeBy(500, TimeUnit.MILLISECONDS);
		myScheduler5.advanceTimeBy(1, TimeUnit.SECONDS);
		myScheduler1.advanceTimeBy(1, TimeUnit.SECONDS);
		*/
//		Thread.sleep(20000);
	}
}

