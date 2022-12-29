/*
 * MIT License
 *
 * Copyright (c) 2022 TheKodeToad, artDev & other contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.solclient.installer.ui;

import java.awt.Font;

import javax.swing.*;

import io.github.solclient.installer.Installer;
import io.github.solclient.installer.locale.Locale;
import io.github.solclient.installer.ui.step.*;
import io.github.solclient.installer.ui.step.impl.*;

public class InstallerFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 500, HEIGHT = 300;

	private final Installer installer = new Installer();
	private final JButton previous;
	private final JButton next;
	private final StepManager<JPanel> steps;
	private int launcherType;
	private Runnable nextAction;

	public InstallerFrame() {
		steps = new StepManager<JPanel>(new Step[] { new Step<>(() -> new ChooseLauncherStep(this), false),
				new Step<>(() -> new InstallLocationStep(this), true),
				new Step<>(() -> new CustomiseStep(this), true), new Step<>(() -> new InstallStep(this), true) });

		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("/logo.png")).getImage());

		JLabel title = new JLabel("Sol Client");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 40F));
		title.setBounds(0, 15, WIDTH, 40);
		add(title);

		previous = new JButton(Locale.get(Locale.UI_BACK));
		previous.setBounds(20, HEIGHT - 80, 100, 30);
		previous.addActionListener((event) -> previous());
		add(previous);

		next = new JButton(Locale.get(Locale.UI_NEXT));
		next.setBounds(WIDTH - 120, HEIGHT - 80, 100, 30);
		next.addActionListener((event) -> {
			nextAction.run();
			nextAction = () -> {
			};
			next();
		});
		add(next);

		setStep(null, steps.current());
	}

	@Override
	public String getTitle() {
		return Locale.get(Locale.UI_TITLE);
	}

	public void setInstallerType(int launcher) {
		this.launcherType = launcher;
	}

	public int getInstallerType() {
		return launcherType;
	}

	public Installer getInstaller() {
		return this.installer;
	}

	public void setNextButtonAction(Runnable r) {
		this.nextAction = r;
	}

	public void enableNextButton(boolean enabled) {
		previous.setEnabled(enabled);
		next.setEnabled(enabled);
	}

	private void setStep(StepContent<JPanel> previousStep, StepContent<JPanel> step) {
		if (previousStep != null) {
			remove(previousStep.getContent());
		}
		add(step.getContent());
		previous.setVisible(steps.hasLess());
		step.getContent().setBounds(0, 50, getWidth(), HEIGHT - 130);
		step.getContent().updateUI();
		this.repaint();
		next.setVisible(step.getStep().isNextShown());
	}

	public void previous() {
		enableNextButton(true);

		if (steps.hasLess()) {
			setStep(steps.current(), steps.goBackwards());
			next.setText(Locale.get(Locale.UI_NEXT));
		}
	}

	public void next() {
		enableNextButton(true);

		if (steps.hasMore()) {
			setStep(steps.current(), steps.goForwards());

			if (!steps.hasMore()) {
				next.setText(Locale.get(Locale.UI_FINISH));
			}
			return;
		}

		System.exit(0);
	}

}
