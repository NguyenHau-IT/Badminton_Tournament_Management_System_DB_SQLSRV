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
import com.example.btms.config.H2TcpServerConfig;
import com.example.btms.config.NetworkConfig;
import com.example.btms.config.Prefs;
import com.example.btms.ui.main.MainFrame;
import com.example.btms.ui.net.NetworkChooserDialog;
import com.example.btms.ui.theme.UITheme;
import com.example.btms.util.ui.IconUtil;

@SpringBootApplication
public class BadmintonTournamentManagementSystemApplication {

	private static final AtomicBoolean UI_STARTED = new AtomicBoolean(false);

	@Autowired
	private ConnectionConfig dbCfg;

	@Autowired
	private H2TcpServerConfig h2TcpServerConfig;

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

			// Bước 1: Chọn network (không tạo thêm cửa sổ nào khác)
			NetworkChooserDialog dlg = new NetworkChooserDialog(null);
			dlg.setVisible(true);
			NetworkConfig cfg = dlg.getSelected();
			if (cfg == null) {
				// Người dùng hủy: không mở UI
				return;
			}

			// Lưu interface đã chọn để các màn khác dùng lại
			try {
				if (cfg.ifName() != null && !cfg.ifName().isBlank()) {
					Prefs p = new Prefs();
					p.put("net.ifName", cfg.ifName());
					p.put("ui.network.ifName", cfg.ifName());
				}
			} catch (Throwable ignore) {
			}

			// Khởi động H2 TCP Server với IP đã chọn
			try {
				h2TcpServerConfig.startTcpServer(cfg);
				System.out.println("✅ H2 TCP Server đã khởi động với IP: " + cfg.ipv4Address());
			} catch (Exception e) {
				System.err.println("❌ Không thể khởi động H2 TCP Server: " + e.getMessage());
				e.printStackTrace();
			}

			// Tạo MainFrame nhưng KHÔNG hiển thị; MainFrame sẽ tự hiển thị sau khi
			// hoàn tất kết nối DB + đăng nhập + chọn giải.
			MainFrame mf = new MainFrame(cfg, dbCfg);
			IconUtil.applyTo(mf);
		});
	}
}
