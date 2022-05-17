package me.mcblueparrot.client.installer.ui;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import me.mcblueparrot.client.installer.Installer;

import me.mcblueparrot.client.installer.ui.step.StepType;

public class InstallerFrame extends JFrame {

	public static final InstallerFrame INSTANCE = new InstallerFrame();
	public static final int WIDTH = 500;
	public static final int HEIGHT = 300;

	private static final long serialVersionUID = 1L;
	private StepType step;
	private Installer installer = new Installer(); 
		private int launcherType;
	private JButton previous;

	public InstallerFrame() {
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("/logo.png")).getImage());

		JLabel title = new JLabel("Sol Client");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 40F));
		title.setBounds(0, 15, WIDTH, 40);
		add(title);

		previous = new JButton("< Back");
		previous.setBounds(20, HEIGHT - 80, 100, 30);
		previous.addActionListener((event) -> previous());
		add(previous);

		setStep(StepType.CHOOSE_LAUNCHER);

//		JButton next = new JButton("Next >");
//		next.setBounds(WIDTH - 110, HEIGHT - 80, 100, 30);
//		add(next);


//		JLabel instruction = new JLabel("Select your launcher");
//		instruction.setHorizontalAlignment(SwingConstants.CENTER);
//		instruction.setFont(instruction.getFont().deriveFont(20F));
//		instruction.setBounds(0, 55, getWidth(), 40);
//		add(instruction);
//
//		JButton installMinecraft = new JButton("Minecraft");
//		installMinecraft.setBounds(140, 115, 100, 30);
//		add(installMinecraft);
//		installMinecraft.addActionListener((event) -> {
//
//		});
//
//		JButton installMultiMC = new JButton("MultiMC");
//		installMultiMC.setBounds(260, 115, 100, 30);
//		add(installMultiMC);

//		JTextField installLocation = new JTextField("/home/me/.minecraft");
//		installLocation.setBounds(getWidth() / 2 - 75, 150, 250, 30);
//		add(installLocation);

//		add(new JButton("Minecraft"));
//		add(new JButton("MultiMC"));
	}

	private void setStep(StepType step) {
		if(this.step != null) {
			remove(this.step.getPanel(this));
		}

		this.step = step;
		add(step.getPanel(this));
		previous.setVisible(step.hasPrevious());
		step.getPanel(this).setBounds(0, 50, getWidth(), HEIGHT - 130);
		step.getPanel(this).updateUI();
	}

	@Override
	public String getTitle() {
		return "Sol Client Installer";
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

	public void previous() {
		if(this.step.hasPrevious()) {
			StepType current = this.step;
			setStep(this.step.previous());
			current.clearState();
		}
	}

	public void next() {
		if(this.step.hasNext()) {
			setStep(this.step.next());
		}
	}

}
