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
package io.github.solclient.installer.ui.step;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.solclient.installer.ui.InstallerFrame;

public enum StepType {
	CHOOSE_LAUNCHER(ChooseLauncherStep::new, false),
	INSTALL_LOCATION(InstallLocationStep::new, true),
	INSTALL(InstallStep::new, true);
	//CUSTOMISE(CustomiseStep::new);

	private JPanel cachedPanel;
	private Function<InstallerFrame, JPanel> panel;
	private final boolean nextShown;

	private StepType(Function<InstallerFrame, JPanel> panel, boolean nextShown) {
		this.panel = panel;
		this.nextShown = nextShown;
	}

	public void clearState() {
		cachedPanel = null;
	}

	public JPanel getPanel(InstallerFrame installer) {
		if (cachedPanel != null) {
			return cachedPanel;
		}

		return cachedPanel = panel.apply(installer);
	}

	public boolean hasNext() {
		return ordinal() < values().length - 1;
	}

	public StepType next() {
		return values()[ordinal() + 1];
	}

	public boolean hasPrevious() {
		return ordinal() != 0;
	}

	public StepType previous() {
		return values()[ordinal() - 1];
	}

	public boolean nextButtonShown() {
		return nextShown;
	}

}
