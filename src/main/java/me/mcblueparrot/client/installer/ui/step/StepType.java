package me.mcblueparrot.client.installer.ui.step;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.mcblueparrot.client.installer.ui.InstallerFrame;

public enum StepType {
	CHOOSE_LAUNCHER(ChooseLauncherStep::new),
	INSTALL_LOCATION(InstallLocationStep::new),
	CUSTOMISE(CustomiseStep::new);

	private JPanel cachedPanel;
	private Function<InstallerFrame, JPanel> panel;

	private StepType(Function<InstallerFrame, JPanel> panel) {
		this.panel = panel;
	}

	public void clearState() {
		cachedPanel = null;
	}

	public JPanel getPanel(InstallerFrame installer) {
		if(cachedPanel != null) {
			return cachedPanel;
		}

		return cachedPanel = panel.apply(installer);
	}

	public boolean hasNext() {
		return ordinal() < values().length;
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

}
