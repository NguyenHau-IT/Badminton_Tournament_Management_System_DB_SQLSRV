package com.example.badmintoneventtechnology;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.badmintoneventtechnology.ui.main.MainFrame;
import com.example.badmintoneventtechnology.ui.net.NetworkChooserDialog;
import com.example.badmintoneventtechnology.ui.net.NetworkConfig;
import com.example.badmintoneventtechnology.util.ui.IconUtil;

@SpringBootApplication
public class BadmintonEventTechnologyApplication {

	private static final AtomicBoolean UI_STARTED = new AtomicBoolean(false);

	public static void main(String[] args) {
		// Tắt headless để cho phép mở Swing UI
		SpringApplication app = new SpringApplication(BadmintonEventTechnologyApplication.class);
		app.setHeadless(false);
		app.run(args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void launchSwingUI() {
		// Tránh mở UI 2 lần (devtools/restart)
		if (!UI_STARTED.compareAndSet(false, true))
			return;

		if (GraphicsEnvironment.isHeadless()) {
			// Headless environment detected. GUI will not be launched. Backend continues.
			return;
		}

		SwingUtilities.invokeLater(() -> {
			NetworkChooserDialog dlg = new NetworkChooserDialog(null);
			dlg.setVisible(true);
			NetworkConfig cfg = dlg.getSelected();
			if (cfg == null) {
				// No network configuration selected. GUI will not be launched. Backend continues.
				return;
			}
			MainFrame mf = new MainFrame(cfg);
			IconUtil.applyTo(mf);
			mf.setVisible(true);
		});
	}
}
