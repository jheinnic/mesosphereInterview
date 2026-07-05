package test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

public class Snippet3 {
	static class Thing implements Action0 {
		private final String format;
		private final long interval;
		private final int counter;
		private final Worker worker;
		private PublishSubject<String> source;

		Thing(Thing prev) {
			this.source = prev.source;
			this.format = prev.format;
			this.worker = prev.worker;
			this.interval = prev.interval;
			this.counter = prev.counter + 1;
		}

		Thing(PublishSubject<String> source, Worker worker, long interval, String format) {
			this.source = source;
			this.format = format;
			this.worker = worker;
			this.interval = interval;
			this.counter = 0;
		}

		@Override
		public void call() {
			this.source.onNext(
				String.format(this.format, this.counter));
			System.out.println("Task1:" + Thread.currentThread().getName());
			this.worker.schedule(
				new Thing(this), this.interval, TimeUnit.MILLISECONDS);
		}
		
		public void begin() {
			this.worker.schedule(this, this.interval, TimeUnit.MILLISECONDS);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		TestScheduler myScheduler1 = Schedulers.test();
		TestScheduler myScheduler5 = Schedulers.test();
		Scheduler myScheduler25 = Schedulers.newThread();
		Scheduler myScheduler2 = Schedulers.computation();
		Scheduler myScheduler7 = Schedulers.trampoline();
		TestScheduler myScheduler40 = Schedulers.test();
		Scheduler immediate = Schedulers.immediate();
		System.out.println("Foo");
		
		long baseDur = 100;
		Worker worker = myScheduler25.createWorker();
		PublishSubject<String> source = PublishSubject.create();
		Thing x1 = new Thing(source, worker, baseDur, "x1(%d)");
		Thing x5 = new Thing(source, worker, 5*baseDur, "x5(%d)");
		Thing x7 = new Thing(source, worker, 7*baseDur, "x7(%d)");
		Thing x25 = new Thing(source, worker, 25*baseDur, "x25(%d)");
		Thing x40 = new Thing(source, worker, 40*baseDur, "x40(%d)");
		
		x1.begin();
		x5.begin();
		x7.begin();
		x25.begin();
		x40.begin();
		
		Observable.interval(baseDur*3, TimeUnit.MILLISECONDS, myScheduler25)
		.doOnNext(i -> {
			System.out.println("Task2a: " + Thread.currentThread().getName());
		})
		.subscribeOn(myScheduler25)
		.observeOn(myScheduler25)
		.subscribe(i -> {
			System.out.println("Task2b: " + Thread.currentThread().getName());
		});

		Observable.interval(baseDur*5, TimeUnit.MILLISECONDS, myScheduler25)
		.doOnNext(i -> {
			System.out.println("Task3a: " + Thread.currentThread().getName());
		})
		.observeOn(myScheduler25)
		.subscribe(i -> {
			System.out.println("Task3b: " + Thread.currentThread().getName());
		});
		
		source
			.subscribeOn(myScheduler7)
			.buffer(100, TimeUnit.MILLISECONDS, myScheduler2)
			.map(r -> String.join(", ", r))
			.filter(r -> ! r.isEmpty())
			.toBlocking()
			.subscribe(n -> { System.out.println(String.format("Y(%d/%d): %s", myScheduler2.now(), myScheduler25.now(), n)); });
		
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

