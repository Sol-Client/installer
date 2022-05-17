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

package me.mcblueparrot.client.installer.ui.step;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import me.mcblueparrot.client.installer.DefaultPaths;
import me.mcblueparrot.client.installer.Installer;

import me.mcblueparrot.client.installer.ui.InstallerFrame;

public class ChooseLauncherStep extends JPanel {

	private static final long serialVersionUID = 1L;

	public ChooseLauncherStep(InstallerFrame frame) {
		setLayout(null);
		JLabel instruction = new JLabel("Select your launcher");
		instruction.setHorizontalAlignment(SwingConstants.CENTER);
		instruction.setFont(instruction.getFont().deriveFont(20F));
		instruction.setBounds(0, 40, InstallerFrame.WIDTH, 40);
		add(instruction);

		JButton installMinecraft = new JButton("Minecraft");
		installMinecraft.setBounds(140, 90, 100, 30);
		add(installMinecraft);
		installMinecraft.addActionListener((event) -> {
			frame.setInstallerType(DefaultPaths.LAUNCHER_TYPE_MINECRAFT);
			frame.next();
		});

		JButton installPolyMC = new JButton("PolyMC");
		installPolyMC.setBounds(260, 90, 100, 30);
		add(installPolyMC);
		installPolyMC.addActionListener((event) -> {
						frame.setInstallerType(DefaultPaths.LAUNCHER_TYPE_POLYMC);
			//frame.setLauncher(Launcher.POLYMC);
			frame.next();
		});
	}

}
