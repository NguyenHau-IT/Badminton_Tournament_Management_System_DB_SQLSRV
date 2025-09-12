package com.example.badmintoneventtechnology.ui.net;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.badmintoneventtechnology.util.ui.IconUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class NetworkChooserDialog extends JDialog {
    private NetworkConfig selected;

    private JButton ok;
    private JButton cancel;

    public NetworkChooserDialog(Window owner) {
        super(owner, "Chọn Network Interface", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        // ===== Root (giảm spacing & padding) =====
        JPanel root = new JPanel(new BorderLayout(0, 8)); // 12 -> 8
        root.setBorder(new EmptyBorder(10, 10, 10, 10)); // 14 -> 10
        root.putClientProperty(FlatClientProperties.STYLE, "background: @background");
        setContentPane(root);

        // ===== Header =====
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // ===== Center card (bọc panel chọn network) =====
        NetworkChooserPanel panel = new NetworkChooserPanel();

        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBorder(new EmptyBorder(8, 8, 8, 8)); // 12 -> 8
        centerCard.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; borderWidth:1; borderColor:@Component.borderColor; " +
                        "background: derive(@background,3%);");
        centerCard.add(panel, BorderLayout.CENTER);

        root.add(centerCard, BorderLayout.CENTER);

        // ===== Footer =====
        JPanel footer = buildFooter();
        root.add(footer, BorderLayout.SOUTH);

        // ===== Actions =====
        ok.addActionListener(e -> {
            selected = panel.getSelectedConfig();
            dispose();
        });
        cancel.addActionListener(e -> {
            selected = null;
            dispose();
        });

        // Enter = OK, ESC = Hủy
        getRootPane().setDefaultButton(ok);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel.doClick();
            }
        });

        // Kích thước gọn hơn
        setMinimumSize(new Dimension(480, 160)); // 560x200 -> 480x160
        pack();
        setLocationRelativeTo(owner);

        // App icons
        IconUtil.applyTo(this);

        // Tinh chỉnh UI theo LAF
        applyUIHints();
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(0, 0, 2, 0)); // 4 -> 2
        wrap.putClientProperty(FlatClientProperties.STYLE, "background: @background");

        // Icon (SVG), tự ẩn nếu không có
        JLabel icon = new JLabel();
        try {
            icon.setIcon(new FlatSVGIcon("icons/monitor.svg", 24, 24)); // 28 -> 24
        } catch (Exception ignore) {
            /* không sao */ }
        icon.setBorder(new EmptyBorder(0, 0, 0, 8)); // bớt 10 -> 8
        wrap.add(icon, BorderLayout.WEST);

        // Title + subtitle (stack dọc)
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Chọn Network Interface");
        // font nhỏ hơn 1 nấc so với trước
        title.putClientProperty(FlatClientProperties.STYLE, "font: +2; font.bold: true"); // +3 -> +2

        JLabel subtitle = new JLabel("Chọn card mạng ứng dụng sẽ dùng để giao tiếp.");
        subtitle.putClientProperty(FlatClientProperties.STYLE, "foreground: @disabledText");

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);

        wrap.add(text, BorderLayout.CENTER);

        // Separator mảnh, margin nhỏ
        JSeparator sep = new JSeparator();
        sep.putClientProperty(FlatClientProperties.STYLE, "thickness:1; margin:6,0,0,0;"); // 8 -> 6
        wrap.add(sep, BorderLayout.SOUTH);

        return wrap;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());

        JSeparator sep = new JSeparator();
        sep.putClientProperty(FlatClientProperties.STYLE, "thickness:1; margin:4,0,4,0;"); // 8 -> 4
        footer.add(sep, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); // 8 -> 6
        buttons.setOpaque(false);

        ok = new JButton("OK");
        cancel = new JButton("Hủy");

        // Nút “primary” & “outline” theo FlatLaf
        ok.putClientProperty("JButton.buttonType", "default"); // primary
        cancel.putClientProperty("JButton.buttonType", "toolBar"); // viền mảnh, nhẹ

        // Icon (nếu có)
        try {
            ok.setIcon(new FlatSVGIcon("icons/plug.svg", 18, 18)); // 20 -> 18
            cancel.setIcon(new FlatSVGIcon("icons/login.svg", 18, 18));
        } catch (Exception ignore) {
        }

        // Mnemonics
        ok.setMnemonic('O');
        cancel.setMnemonic('H');

        // Kích thước & margin gọn hơn
        int minW = 88; // 96 -> 88
        ok.putClientProperty(FlatClientProperties.STYLE, "minimumWidth:" + minW + "; arc:12; margin:4,10,4,10;");
        cancel.putClientProperty(FlatClientProperties.STYLE, "minimumWidth:" + minW + "; arc:12; margin:4,10,4,10;");

        buttons.add(ok);
        buttons.add(cancel);
        footer.add(buttons, BorderLayout.CENTER);

        return footer;
    }

    private void applyUIHints() {
        // Những hint chung, FlatLaf sẽ hiểu nếu đã setup
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.arc", 12); // 16 -> 12
        UIManager.put("Button.arc", 12); // 16 -> 12
        UIManager.put("TextComponent.arc", 10); // 12 -> 10

        // Với LAF khác, margin thủ công cho nút vẫn giữ ổn định
        Insets pad = new Insets(6, 12, 6, 12); // 8,14,8,14 -> 6,12,6,12
        if (ok != null)
            ok.setMargin(pad);
        if (cancel != null)
            cancel.setMargin(pad);
    }

    public NetworkConfig getSelected() {
        return selected;
    }
}
