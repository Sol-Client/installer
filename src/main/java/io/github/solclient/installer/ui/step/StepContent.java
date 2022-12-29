package io.github.solclient.installer.ui.step;

public final class StepContent<T> {

	private final Step<T> step;
	private final T content;

	public StepContent(Step<T> step, T content) {
		this.step = step;
		this.content = content;
	}

	public Step<T> getStep() {
		return step;
	}

	public T getContent() {
		return content;
	}

}
