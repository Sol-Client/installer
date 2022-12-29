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

package io.github.solclient.installer.ui.step.impl;

import javax.swing.*;

import io.github.solclient.installer.Launcher;
import io.github.solclient.installer.locale.Locale;
import io.github.solclient.installer.ui.InstallerFrame;
import io.github.solclient.installer.ui.step.Step;

public class ChooseLauncherStep extends JPanel {

	private static final long serialVersionUID = 1L;

	public ChooseLauncherStep(InstallerFrame frame) {
		setLayout(null);
		JLabel instruction = new JLabel(Locale.get(Locale.UI_SELECT_LAUNCHER));
		instruction.setHorizontalAlignment(SwingConstants.CENTER);
		instruction.setFont(instruction.getFont().deriveFont(20F));
		instruction.setBounds(0, 40, InstallerFrame.WIDTH, 40);
		add(instruction);

		JButton installMinecraft = new JButton("Mojang");
		installMinecraft.setBounds(80, 90, 100, 30);
		add(installMinecraft);
		installMinecraft.addActionListener((event) -> {
			frame.setInstallerType(Launcher.MOJANG);
			frame.next();
		});

		JButton installMultiMC = new JButton("MultiMC");
		installMultiMC.setBounds(200, 90, 100, 30);
		add(installMultiMC);
		installMultiMC.addActionListener((event) -> {
			frame.setInstallerType(Launcher.MULTIMC);
			frame.next();
		});

		JButton installPrism = new JButton("Prism");
		installPrism.setBounds(320, 90, 100, 30);
		add(installPrism);
		installPrism.addActionListener((event) -> {
			frame.setInstallerType(Launcher.PRISM);
			frame.next();
		});
	}

}
