package io.github.solclient.installer.ui.step;

import java.util.LinkedList;

public final class StepManager<T> {

	private final Step<T>[] stepPool;
	private final LinkedList<StepContent<T>> navigation = new LinkedList<>();

	public StepManager(Step<T>[] stepPool) {
		this.stepPool = stepPool;
		goForwards();
	}

	public StepContent<T> goBackwards() {
		if (!hasLess())
			throw new IllegalStateException("Cannot go backwards");

		navigation.removeLast();
		return current();
	}

	public StepContent<T> goForwards() {
		if (!hasMore())
			throw new IllegalStateException("Cannot go forwards");

		StepContent<T> newContent = stepPool[navigation.size()].createContent();
		navigation.addLast(newContent);
		return newContent;
	}

	public StepContent<T> current() {
		return navigation.getLast();
	}

	public boolean hasMore() {
		return navigation.size() < stepPool.length;
	}

	public boolean hasLess() {
		return navigation.size() > 1;
	}

}
