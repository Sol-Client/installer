package me.mcblueparrot.client.installer.ui.step;

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
import me.mcblueparrot.client.installer.DefaultPaths;

import me.mcblueparrot.client.installer.ui.InstallerFrame;

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
				DefaultPaths.getDefaultLocation(DefaultPaths.getLocationsForLauncher(InstallerFrame.INSTANCE.getInstallerType())).toString());
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
