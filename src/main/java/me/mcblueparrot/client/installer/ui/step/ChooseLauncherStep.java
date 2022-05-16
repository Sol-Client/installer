package me.mcblueparrot.client.installer.ui.step;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import me.mcblueparrot.client.installer.Launcher;
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
			frame.setLauncher(Launcher.MINECRAFT);
			frame.next();
		});

		JButton installPolyMC = new JButton("PolyMC");
		installPolyMC.setBounds(260, 90, 100, 30);
		add(installPolyMC);
		installPolyMC.addActionListener((event) -> {
			frame.setLauncher(Launcher.POLYMC);
			frame.next();
		});
	}

}
