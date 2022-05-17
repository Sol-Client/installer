package me.mcblueparrot.client.installer;

import javax.swing.JFrame;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import me.mcblueparrot.client.installer.ui.InstallerFrame;

public class Main {

	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings", "on");
		FlatDarculaLaf.setup();

		InstallerFrame frame = InstallerFrame.INSTANCE;
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
