package com.example.btms.ui.tournament;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.db.DatabaseService;
import com.example.btms.service.tournament.GiaiDauService;

/**
 * Unified tournament management component that can work in both full management
 * mode
 * and selection-only mode. Replaces both TournamentSelectDialog and can be
 * embedded
 * in panels or used as a standalone dialog.
 */
@SuppressWarnings("serial")
public class TournamentManagementComponent extends JPanel {
    private final DatabaseService databaseService;
    private final boolean selectionMode;
    private GiaiDauService giaiDauService;
    private GiaiDauManagementPanel managementPanel;
    private GiaiDau selectedTournament;
    private Runnable onSelectionCallback;

    public TournamentManagementComponent(DatabaseService databaseService) {
        this(databaseService, false);
    }

    public TournamentManagementComponent(DatabaseService databaseService, boolean selectionMode) {
        this.databaseService = databaseService;
        this.selectionMode = selectionMode;
        setLayout(new BorderLayout(10, 10));
        updateConnection();
        setupDoubleClickSelection();
    }

    /** Cập nhật kết nối database và render lại panel quản lý giải đấu. */
    public void updateConnection() {
        giaiDauService = (databaseService != null && databaseService.current() != null)
                ? new GiaiDauService(databaseService)
                : null;
        initComponents();
    }

    private void initComponents() {
        removeAll();
        if (giaiDauService != null) {
            managementPanel = new GiaiDauManagementPanel(giaiDauService, selectionMode);
            add(managementPanel, BorderLayout.CENTER);

            if (selectionMode) {
                add(createSelectionButtons(), BorderLayout.SOUTH);
                // Auto-select the most recent active tournament
                javax.swing.SwingUtilities.invokeLater(this::autoSelectMostRecentActiveTournament);
            }
        } else {
            JPanel noConnPanel = new JPanel();
            noConnPanel.add(new javax.swing.JLabel("Chưa có kết nối database. Vui lòng kết nối database trước."));
            add(noConnPanel, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    private JPanel createSelectionButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCreateNew = new JButton("Tạo giải mới");
        JButton btnSelect = new JButton("Chọn");
        JButton btnCancel = new JButton("Hủy");

        btnCreateNew.addActionListener(e -> createNewTournament());
        btnSelect.addActionListener(e -> selectCurrentTournament());
        btnCancel.addActionListener(e -> cancelSelection());

        buttonPanel.add(btnCreateNew);
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);
        return buttonPanel;
    }

    private void setupDoubleClickSelection() {
        // This will be set up after the management panel is created
    }

    private void selectCurrentTournament() {
        if (managementPanel != null) {
            GiaiDau selected = managementPanel.getSelectedGiaiDau();
            if (selected != null) {
                this.selectedTournament = selected;
                if (onSelectionCallback != null) {
                    onSelectionCallback.run();
                }
                return;
            }
        }

        // Check if there are no tournaments available
        try {
            if (giaiDauService != null && giaiDauService.getAllGiaiDau().isEmpty()) {
                int choice = javax.swing.JOptionPane.showConfirmDialog(this,
                        "Chưa có giải đấu nào. Bạn có muốn tạo giải đấu mới không?",
                        "Không có giải đấu",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (choice == javax.swing.JOptionPane.YES_OPTION) {
                    createNewTournament();
                    return;
                }
            }
        } catch (Exception e) {
            // Ignore and show normal message
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn một giải đấu.",
                "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelection() {
        this.selectedTournament = null;
        if (onSelectionCallback != null) {
            onSelectionCallback.run();
        }
    }

    private void createNewTournament() {
        if (giaiDauService != null) {
            Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
            GiaiDauDialog dialog = new GiaiDauDialog(parentWindow, "Tạo giải đấu mới", null, giaiDauService);
            dialog.setVisible(true);
            // Refresh the list after creating new tournament
            refreshGiaiDau();
            // Auto-select the newly created tournament or most recent active one
            javax.swing.SwingUtilities.invokeLater(this::autoSelectMostRecentActiveTournament);
        }
    }

    /**
     * Automatically selects the most recent active tournament in the table
     */
    private void autoSelectMostRecentActiveTournament() {
        if (managementPanel == null || giaiDauService == null) {
            return;
        }

        try {
            // Get active tournaments first, then all tournaments if no active ones
            java.util.List<GiaiDau> activeTournaments = giaiDauService.getActiveGiaiDau();
            java.util.List<GiaiDau> targetList = activeTournaments.isEmpty() ? giaiDauService.getAllGiaiDau()
                    : activeTournaments;

            if (!targetList.isEmpty()) {
                // Find the most recent tournament (highest ID or most recent start date)
                GiaiDau mostRecent = targetList.stream()
                        .max((t1, t2) -> {
                            // First compare by start date (if available)
                            if (t1.getNgayBd() != null && t2.getNgayBd() != null) {
                                int dateCompare = t1.getNgayBd().compareTo(t2.getNgayBd());
                                if (dateCompare != 0)
                                    return dateCompare;
                            }
                            // Then by ID (most recent created)
                            return Integer.compare(t1.getId(), t2.getId());
                        })
                        .orElse(null);

                if (mostRecent != null) {
                    // Select this tournament in the table
                    selectTournamentInTable(mostRecent);

                    // Show a brief message about auto-selection (only in selection mode)
                    if (selectionMode && activeTournaments.contains(mostRecent)) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JLabel statusLabel = new javax.swing.JLabel(
                                    "<html><font color='blue'>✓ Đã tự động chọn giải đấu gần nhất đang hoạt động: " +
                                            mostRecent.getTenGiai() + "</font></html>");
                            statusLabel.setOpaque(false);
                            // Add status label temporarily (will be cleared when user interacts)
                        });
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors in auto-selection
        }
    }

    /**
     * Selects a specific tournament in the management panel table
     */
    private void selectTournamentInTable(GiaiDau tournament) {
        if (managementPanel != null && tournament != null) {
            try {
                // Select the tournament in the table
                managementPanel.selectTournamentById(tournament.getId());

                // Also store it as selected tournament for selection mode
                if (selectionMode) {
                    this.selectedTournament = tournament;
                }
            } catch (Exception e) {
                // Ignore errors in selection
            }
        }
    }

    public GiaiDau getSelectedGiaiDau() {
        return selectionMode ? selectedTournament
                : (managementPanel != null ? managementPanel.getSelectedGiaiDau() : null);
    }

    public void refreshGiaiDau() {
        if (managementPanel != null) {
            managementPanel.refreshData();
        }
    }

    public boolean hasValidSelection() {
        return getSelectedGiaiDau() != null;
    }

    public GiaiDauService getGiaiDauService() {
        return giaiDauService;
    }

    public void setOnSelectionCallback(Runnable callback) {
        this.onSelectionCallback = callback;
    }

    /**
     * Returns the name of the currently auto-selected tournament (if any)
     */
    public String getAutoSelectedTournamentName() {
        if (selectionMode && selectedTournament != null) {
            return selectedTournament.getTenGiai();
        }
        return null;
    }

    /**
     * Static method to show a selection dialog - replaces TournamentSelectDialog
     */
    public static GiaiDau showSelectionDialog(Window parent, DatabaseService databaseService) {
        // Check if there are no tournaments first
        try {
            GiaiDauService tempService = new GiaiDauService(databaseService);
            if (tempService.getAllGiaiDau().isEmpty()) {
                int choice = javax.swing.JOptionPane.showConfirmDialog(parent,
                        "Chưa có giải đấu nào trong hệ thống.\n" +
                                "Bạn có muốn tạo giải đấu mới trước khi tiếp tục?",
                        "Không có giải đấu",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (choice == javax.swing.JOptionPane.YES_OPTION) {
                    // Show create dialog first
                    GiaiDauDialog createDialog = new GiaiDauDialog(parent, "Tạo giải đấu mới", null, tempService);
                    createDialog.setVisible(true);
                    // After creating, continue to show selection dialog
                }
            }
        } catch (Exception e) {
            // Continue with normal flow if there's an error checking
        }

        // Determine dialog title based on available tournaments
        String dialogTitle = "Chọn giải đấu";
        try {
            GiaiDauService tempService = new GiaiDauService(databaseService);
            if (tempService.getAllGiaiDau().isEmpty()) {
                dialogTitle = "Chọn hoặc tạo giải đấu";
            }
        } catch (Exception e) {
            // Use default title if there's an error
        }

        JDialog dialog = new JDialog(parent, dialogTitle, JDialog.ModalityType.APPLICATION_MODAL);
        TournamentManagementComponent component = new TournamentManagementComponent(databaseService, true);

        dialog.setLayout(new BorderLayout());
        dialog.add(component, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setMinimumSize(new java.awt.Dimension(640, 420));

        final GiaiDau[] result = { null };
        component.setOnSelectionCallback(() -> {
            result[0] = component.getSelectedGiaiDau();
            dialog.dispose();
        });

        dialog.setVisible(true);
        return result[0];
    }
}