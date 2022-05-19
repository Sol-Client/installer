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

import com.formdev.flatlaf.icons.FlatFileChooserListViewIcon;
import com.formdev.flatlaf.icons.FlatFileViewDirectoryIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import io.github.solclient.installer.Launchers;
import io.github.solclient.installer.locale.Locale;

import io.github.solclient.installer.ui.InstallerFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class InstallLocationStep extends JPanel {

	private static final long serialVersionUID = 1L;

	public InstallLocationStep(InstallerFrame frame) {
		setLayout(null);
		JLabel instruction = new JLabel(Locale.getString(Locale.UI_INSTALL_LOCATION));
		instruction.setHorizontalAlignment(SwingConstants.CENTER);
		instruction.setFont(instruction.getFont().deriveFont(20F));
		instruction.setBounds(0, 40, InstallerFrame.WIDTH, 40);
		add(instruction);

		JTextField installationLocation = new JTextField(
				Launchers.getDefaultLocation(Launchers.getLocationsForLauncher(frame.getInstallerType())).toString());
		installationLocation.setBounds(InstallerFrame.WIDTH / 2 - 100, 90, 170, 30);
		add(installationLocation);
		installationLocation.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent event) {
				File file = getFile(installationLocation);
				SwingUtilities.invokeLater(() -> installationLocation.putClientProperty("JComponent.outline",
						!file.canRead() || !file.canWrite() ? "error" : null));
			}

		});
		JButton installationLocationPicker = new JButton();
		installationLocationPicker.setBounds(InstallerFrame.WIDTH / 2 + 70, 90, 30, 30);
		installationLocationPicker.setIcon(new FlatFileViewDirectoryIcon());
		add(installationLocationPicker);
		installationLocationPicker.addActionListener(evt-> {
			JFileChooser chooser = new JFileChooser(getFile(installationLocation));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File input) {
					return input.isDirectory() && input.canWrite() && input.canRead();
				}

				@Override
				public String getDescription() {
					return Locale.getString(Locale.UI_ACCESSIBLE_DIRECTORIES);
				}
			});
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle(Locale.getString(Locale.UI_SELECT_GAMEDIR));
			chooser.setApproveButtonText(Locale.getString(Locale.UI_SELECT));
			if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				installationLocation.setText(selected.getAbsolutePath());
			}
		});
		frame.setNextButtonAction(() -> {
			File file = getFile(installationLocation);
			if (!file.canRead() || !file.canWrite()) { // <- thy return false when the file doesn't exist
				JOptionPane.showMessageDialog(frame, Locale.getString(Locale.UI_NO_GAMEDIR), Locale.getString(Locale.UI_OH_DEAR), JOptionPane.ERROR_MESSAGE);
				return;
			}
			frame.getInstaller().setPath(file);
		});
	}
	
	private static File getFile(JTextField field) {
		return new File(field.getText());
	}

}
