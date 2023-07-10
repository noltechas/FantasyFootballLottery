import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ScheduleScreen extends JFrame {
    private static final int NUM_ROWS = 2;
    private static final int NUM_COLUMNS = 7;
    private static final int HIGHLIGHT_WEEK_1 = 9;
    private static final int HIGHLIGHT_WEEK_2 = 10;
    private static final Color HIGHLIGHT_COLOR = new Color(216, 191, 255); // Bright purple-ish color

    public ScheduleScreen(ArrayList<Week> weeks, ArrayList<ArrayList<Team>> divisions) {
        setTitle("Schedule");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(1500, 1000);
        setLocationRelativeTo(null); // Center the frame on the screen

        Font customFont;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/Jerseyletters-lP5V.ttf")).deriveFont(Font.PLAIN, 20);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Handle font loading error
            customFont = new Font("SansSerif", Font.PLAIN, 12); // Fallback font
        }

        Font customFont2;
        try {
            customFont2 = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/PaladinsGradient-R6VW.otf")).deriveFont(Font.PLAIN, 11);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Handle font loading error
            customFont2 = new Font("SansSerif", Font.PLAIN, 12); // Fallback font
        }

        Font customFont3;
        try {
            customFont3 = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/PaladinsCondensed-rB77.otf")).deriveFont(Font.PLAIN, 13);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            // Handle font loading error
            customFont3 = new Font("SansSerif", Font.PLAIN, 12); // Fallback font
        }

        JPanel mainPanel = new JPanel(new GridLayout(NUM_ROWS, NUM_COLUMNS));

        for (Week week : weeks) {
            JPanel weekPanel = new JPanel(new GridLayout(week.getMatchups().size() + 1, 1));
            weekPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            String weekLabelText = "Week " + week.getNumber();
            if (week.getNumber() == HIGHLIGHT_WEEK_1 || week.getNumber() == HIGHLIGHT_WEEK_2)
                weekLabelText = "All-Star Week";
            if(week.getNumber() == 8 && divisions.size() == 6)
                weekLabelText = "All-Star Week";

            JLabel weekLabel = new JLabel(weekLabelText);
            weekLabel.setFont(weekLabel.getFont().deriveFont(Font.BOLD));
            weekLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally
            weekLabel.setFont(customFont);

            weekPanel.add(weekLabel);

            for (String matchup : week.getMatchups()) {
                JLabel matchupLabel = new JLabel(matchup);
                matchupLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally
                matchupLabel.setFont(customFont2);

                String[] teams = matchup.split(" vs. ");
                if (teams.length == 2 && areInSameDivision(teams[0], teams[1], divisions)) {
                    Font underlineFont = customFont3.deriveFont(customFont3.getStyle());
                    matchupLabel.setFont(underlineFont);
                }

                weekPanel.add(matchupLabel);
            }

            if (week.getNumber() == HIGHLIGHT_WEEK_1 || week.getNumber() == HIGHLIGHT_WEEK_2) {
                weekPanel.setBackground(HIGHLIGHT_COLOR);
            }
            if(week.getNumber() == 8 && divisions.size() == 6)
                weekPanel.setBackground(HIGHLIGHT_COLOR);

            mainPanel.add(weekPanel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);

        setVisible(true);
    }

    private boolean areInSameDivision(String team1, String team2, ArrayList<ArrayList<Team>> divisions) {
        for (ArrayList<Team> division : divisions) {
            boolean foundTeam1 = false;
            boolean foundTeam2 = false;
            for (Team team : division) {
                if (team.name.equals(team1)) {
                    foundTeam1 = true;
                }
                if (team.name.equals(team2)) {
                    foundTeam2 = true;
                }
            }
            if (foundTeam1 && foundTeam2) {
                return true;
            }
        }
        return false;
    }
}
