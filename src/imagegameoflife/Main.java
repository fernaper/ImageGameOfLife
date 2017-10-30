/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagegameoflife;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Fernando
 */
public class Main {
    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Image Game of Life");
            f.setResizable(true);
            JPanel cgol = new ImageGameOfLife();
            cgol.setPreferredSize(new Dimension(900, 700));
            cgol.setBackground(new Color(0xFAF8EF));
            cgol.setFont(new Font("SansSerif", Font.BOLD, 48));
            cgol.setFocusable(true);
            
            f.add(cgol, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
