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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import io.github.solclient.installer.Launchers;

import io.github.solclient.installer.ui.InstallerFrame;

public class InstallLocationStep extends JPanel {

	private static final long serialVersionUID = 1L;

	public InstallLocationStep(InstallerFrame frame) {
		setLayout(null);
		JLabel instruction = new JLabel("Install Location");
		instruction.setHorizontalAlignment(SwingConstants.CENTER);
		instruction.setFont(instruction.getFont().deriveFont(20F));
		instruction.setBounds(0, 25, InstallerFrame.WIDTH, 40);
		add(instruction);
				
		JTextField installationLocation = new JTextField(
				Launchers.getDefaultLocation(Launchers.getLocationsForLauncher(InstallerFrame.INSTANCE.getInstallerType())).toString());
		installationLocation.setBounds(InstallerFrame.WIDTH / 2 - 100, 75, 200, 30);
		add(installationLocation);
		installationLocation.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent event) {
				SwingUtilities.invokeLater(() -> installationLocation.putClientProperty("JComponent.outline",
						!getFile(installationLocation).exists() ? "error" : null));
 			}

		});

		JButton installButton = new JButton("Done");
		installButton.setBounds(InstallerFrame.WIDTH / 2 - 50, 125, 100, 30);
		installButton.addActionListener((event) -> {
			File file = getFile(installationLocation);
			if(!file.exists()) {
				JOptionPane.showMessageDialog(frame, "Could not find the specified file", "Oh Dear", JOptionPane.ERROR_MESSAGE);
				return;
			}
						frame.getInstaller().setPath(file);
			frame.next();
		});
		add(installButton);
	}

	private static File getFile(JTextField field) {
		return new File(field.getText());
	}

}
