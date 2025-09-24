package com.example.btms;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.example.btms.config.ConnectionConfig;
import com.example.btms.ui.main.MainFrame;
import com.example.btms.ui.net.NetworkChooserDialog;
import com.example.btms.ui.net.NetworkConfig;
import com.example.btms.util.ui.IconUtil;
import com.example.btms.ui.theme.UITheme;

@SpringBootApplication
public class BadmintonTournamentManagementSystemApplication {

	private static final AtomicBoolean UI_STARTED = new AtomicBoolean(false);

	@Autowired
	private ConnectionConfig dbCfg;

	public static void main(String[] args) {
		// Tắt headless để cho phép mở Swing UI
		SpringApplication app = new SpringApplication(BadmintonTournamentManagementSystemApplication.class);
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
			// Áp dụng theme (bo góc + FlatLaf) trước khi tạo bất kỳ frame/dialog nào
			UITheme.init();
			NetworkChooserDialog dlg = new NetworkChooserDialog(null);
			dlg.setVisible(true);
			NetworkConfig cfg = dlg.getSelected();
			if (cfg == null) {
				// No network configuration selected. GUI will not be launched. Backend
				// continues.
				return;
			}
			MainFrame mf = new MainFrame(cfg, dbCfg);
			IconUtil.applyTo(mf);
			mf.setVisible(true);
		});
	}
}
