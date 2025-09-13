package com.example.badmintoneventtechnology.ui.tournament;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.example.badmintoneventtechnology.model.tournament.Giai;
import com.example.badmintoneventtechnology.service.db.GiaiRelatedService;

public class GiaiContentPanel extends JPanel {
    private final GiaiRelatedService giaiService;
    private final JComboBox<String> cboNoiDung;
    private final JComboBox<String> cboVdv;

    public GiaiContentPanel(GiaiRelatedService giaiService) {
        this.giaiService = giaiService;
        setLayout(new GridLayout(2, 2, 8, 8));
        add(new JLabel("Nội dung thi đấu:"));
        cboNoiDung = new JComboBox<>();
        add(cboNoiDung);
        add(new JLabel("Vận động viên:"));
        cboVdv = new JComboBox<>();
        add(cboVdv);
    }

    public void loadByGiai(Giai giai) {
        cboNoiDung.removeAllItems();
        cboVdv.removeAllItems();
        if (giai == null)
            return;

        try {
            List<String> noiDungList = giaiService.getNoiDungThiDauByGiai(giai.getGiaiId());
            for (String nd : noiDungList)
                cboNoiDung.addItem(nd);
        } catch (Exception e) {
            System.err.println("Lỗi khi load nội dung thi đấu: " + e.getMessage());
            cboNoiDung.addItem("(Không thể tải dữ liệu)");
        }

        try {
            List<String> vdvList = giaiService.getVdvByGiai(giai.getGiaiId());
            for (String vdv : vdvList)
                cboVdv.addItem(vdv);
        } catch (Exception e) {
            System.err.println("Lỗi khi load VĐV: " + e.getMessage());
            cboVdv.addItem("(Không thể tải dữ liệu)");
        }
    }
}
