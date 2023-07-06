import javax.swing.*;
import java.awt.*;
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


        JPanel mainPanel = new JPanel(new GridLayout(NUM_ROWS, NUM_COLUMNS));

        for (Week week : weeks) {
            JPanel weekPanel = new JPanel(new GridLayout(week.getMatchups().size() + 1, 1));
            weekPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            String weekLabelText = "Week " + week.getNumber();
            if (week.getNumber() == HIGHLIGHT_WEEK_1 || week.getNumber() == HIGHLIGHT_WEEK_2)
                weekLabelText += " (All-Star Week)";
            if(week.getNumber() == 8 && divisions.size() ==6)
                weekLabelText += " (All-Star Week)";

            JLabel weekLabel = new JLabel(weekLabelText);
            weekLabel.setFont(weekLabel.getFont().deriveFont(Font.BOLD));
            weekLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally

            weekPanel.add(weekLabel);

            for (String matchup : week.getMatchups()) {
                JLabel matchupLabel = new JLabel(matchup);
                matchupLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally

                String[] teams = matchup.split(" vs. ");
                if (teams.length == 2 && areInSameDivision(teams[0], teams[1], divisions)) {
                    Font font = matchupLabel.getFont();
                    Font underlineFont = font.deriveFont(font.getStyle());
                    matchupLabel.setFont(underlineFont);
                    matchupLabel.setText("<html><u>" + matchupLabel.getText() + "</u></html>");
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
