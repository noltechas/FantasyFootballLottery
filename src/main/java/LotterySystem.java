import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class LotterySystem {
    private static final String JSON_FILE = "teams.json";
    private ScheduleScreen scheduleScreen;
    private final List<Team> teams;
    private final List<Team> revealedTeams;
    private List<Team> lotteryBalls;
    private List<Team> draftPicks;
    private int currentPick;
    private JTextField teamNameField;
    private JTextField teamBallsField;
    private JTextArea resultArea;
    private JTextArea teamListArea;
    private boolean showPercentages;
    private JTextArea additionalArea;
    private JButton nextPickButton;
    private JButton performLotteryButton;
    private JButton viewScheduleButton;
    private JList<String> percentChanceList;
    private ArrayList<ArrayList<Team>> divisions;
    private JTextArea[] divisionAreas;
    private JLabel[] divisionLabels;
    private final ArrayList<Week> weeks;
    Font customFont;
    Font customFont2;


    public LotterySystem() throws IOException, FontFormatException {
        teams = loadTeams();
        revealedTeams = new ArrayList<>();
        customFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("font.ttf"))).deriveFont(20f);
        customFont2 = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("casino.ttf"))).deriveFont(16f);
        weeks = new ArrayList<>();
        createAndShowGUI();
    }
    private void createAndShowGUI() {
        JFrame frame = new JFrame("Fantasy Football Draft Lottery");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 1000);
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        // Set the application icon
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                try {
                    Image iconImage = ImageIO.read(new File("src/main/java/uno.png"));
                    Taskbar.getTaskbar().setIconImage(iconImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            ImageIcon icon = new ImageIcon("uno.png");
            frame.setIconImage(icon.getImage());
        }

        teamNameField = new JTextField(10);
        teamBallsField = new JTextField(10);

        JButton addTeamButton = new JButton("Add Team");
        addTeamButton.addActionListener(e -> {
            addTeam(teamNameField.getText(), Integer.parseInt(teamBallsField.getText()));
            teamNameField.setText("");
            teamBallsField.setText("");
        });

        viewScheduleButton = new JButton("View Schedule");
        viewScheduleButton.addActionListener(e -> scheduleScreen = new ScheduleScreen(weeks,divisions));
        viewScheduleButton.setEnabled(false);

        JButton deleteTeamButton = new JButton("Delete Team");
        deleteTeamButton.addActionListener(e -> {
            deleteTeam(teamNameField.getText());
            teamNameField.setText("");
        });

        performLotteryButton = new JButton("Perform Lottery");
        performLotteryButton.addActionListener(e -> performLottery());

        percentChanceList = new JList<>();
        percentChanceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTeamName = percentChanceList.getSelectedValue();
                if (selectedTeamName != null) {
                    selectedTeamName = selectedTeamName.split(":")[0].trim();
                    showDetailedChances(selectedTeamName);
                }
            }
        });


        nextPickButton = new JButton("Show Next Pick");
        nextPickButton.addActionListener(e -> showNextPick());
        nextPickButton.setEnabled(false);

        int extra = 6;
        if(teams.size() == 18)
            extra = 3;
        else if(teams.size() == 16)
            extra = 1;
        resultArea = new JTextArea(teams.size()+extra, 14);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(255,255,204));

        teamListArea = new JTextArea(10, 15);
        teamListArea.setEditable(false);
        teamListArea.setBackground(new Color(255,255,204));

        JTextArea percentChanceArea = new JTextArea(teams.size() + extra, 15);
        percentChanceArea.setEditable(false);
        percentChanceArea.setBackground(new Color(255,255,204));

        additionalArea = new JTextArea(teams.size()+extra, 15);
        additionalArea.setEditable(false);
        additionalArea.setBackground(new Color(255,255,204));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.weightx = 1;

        JTextArea divisionArea = new JTextArea(teams.size() + extra, 15);
        divisionArea.setEditable(false);
        divisionArea.setBackground(Color.ORANGE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Team Name:"), gbc);
        gbc.gridx = 1;
        panel.add(teamNameField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Number of Balls:"), gbc);
        gbc.gridx = 3;
        panel.add(teamBallsField, gbc);
        gbc.gridx = 4;
        panel.add(addTeamButton, gbc);
        gbc.gridx = 5;
        panel.add(deleteTeamButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        panel.add(new JScrollPane(teamListArea), gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 3;
        panel.add(performLotteryButton, gbc);
        gbc.gridx = 3;
        panel.add(nextPickButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel resultsLabel = new JLabel("Lottery Results                       ");
        resultsLabel.setFont(customFont);
        panel.add(resultsLabel, gbc);
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(new JScrollPane(resultArea), gbc);

        percentChanceList.setPrototypeCellValue(String.format("%1$-65s", ""));
        percentChanceList.setVisibleRowCount(teams.size()); // This should be adjusted based on your needs
        percentChanceList.setFixedCellHeight(teams.size()+1); // This should be adjusted based on your needs
        JScrollPane percentChanceScrollPane = new JScrollPane(percentChanceList);
        percentChanceScrollPane.getViewport().setBackground(Color.ORANGE);
        gbc.gridx = 2;
        gbc.gridy = 3;
        JLabel remainingTeamsLabel = new JLabel("Remaining Teams");
        remainingTeamsLabel.setFont(customFont);
        panel.add(remainingTeamsLabel, gbc);
        gbc.gridy = 4;
        panel.add(percentChanceScrollPane, gbc);

        gbc.gridx = 4;
        gbc.gridy = 3;
        JLabel chanceForEachPickLabel = new JLabel("Chance for Each Pick");
        chanceForEachPickLabel.setFont(customFont);
        panel.add(chanceForEachPickLabel, gbc);
        gbc.gridy = 4;
        panel.add(new JScrollPane(additionalArea), gbc);

        int areas;
        if(teams.size() == 16)
            areas = 4;
        else if(teams.size() == 18)
            areas = 6;
        else
            areas = 5;
        divisionAreas = new JTextArea[areas];
        int divisionColumns = 2; // We always want 2 columns of divisions
        int divisionRows = teams.size() == 16 ? 2 : 3; // Number of rows depends on the number of divisions

        JPanel divisionPanel = new JPanel(new GridLayout(divisionRows, divisionColumns)); // Set the layout to a grid
        divisionPanel.setBackground(Color.LIGHT_GRAY);

        ArrayList<Color> colors = new ArrayList<>();
        colors.add(new Color(255,51,51));
        colors.add(new Color(255,153,51));
        colors.add(new Color(255,255,51));
        colors.add(new Color(51,200,51));
        colors.add(new Color(51,153,255));
        colors.add(new Color(178,102,255));

        divisionLabels = new JLabel[divisionAreas.length];
        for (int i = 0; i < divisionAreas.length; i++) {
            JTextArea divisionTextArea = new JTextArea(5, 15);
            divisionTextArea.setFont(customFont2);
            divisionAreas[i] = divisionTextArea;
            divisionAreas[i].setEditable(false);
            divisionAreas[i].setBackground(colors.get(i));

            JPanel individualDivisionPanel = new JPanel(new BorderLayout());
            JLabel divisionLabel = new JLabel("Division X");
            divisionLabels[i] = divisionLabel;
            divisionLabel.setHorizontalAlignment(JLabel.CENTER);
            divisionLabel.setFont(customFont);
            individualDivisionPanel.add(divisionLabel, BorderLayout.NORTH);
            individualDivisionPanel.add(new JScrollPane(divisionAreas[i]), BorderLayout.CENTER);

            divisionPanel.add(individualDivisionPanel);

            // Center text horizontally within the JTextArea
            divisionAreas[i].setAlignmentX(JTextField.CENTER);
        }

        gbc.gridx = 6;
        gbc.gridheight = 2; // to span the height of two rows
        gbc.gridy = 4;
        panel.add(divisionPanel, gbc);

        // Add the button to your panel
        // Reset the grid height to 1 for the viewScheduleButton
        gbc.gridheight = 1;

        // Adjust the grid position for the viewScheduleButton
        gbc.gridx = 6;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(viewScheduleButton, gbc);

        // Toggle button to show/hide percentages
        JButton togglePercentagesButton = new JButton("Toggle Win Percentages");
        togglePercentagesButton.addActionListener(e -> toggleShowPercentages());
        gbc.gridy = 3; // Adjust the grid position for the togglePercentagesButton
        panel.add(togglePercentagesButton, gbc);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
        showTeams();
    }

    private void toggleShowPercentages() {
        showPercentages = !showPercentages;
        showDivisions();
    }

    private void showDetailedChances(String teamName) {
        additionalArea.setText("");
        ArrayList<Integer> placements = new ArrayList<>();
        ArrayList<Team> updatedBalls = new ArrayList<>();
        for (Team tea : teams) {
            if (!revealedTeams.contains(tea)) {
                for (int j = 0; j < tea.balls; j++)
                    updatedBalls.add(tea);
            }
        }
        for(int i = 0; i < 25000; i++) {
            ArrayList<Team> fakeLotteryBalls = new ArrayList<>(updatedBalls);
            Random rand = new Random();
            int placement = 1;
            boolean found = false;
            while (!fakeLotteryBalls.isEmpty() && !found) {
                Collections.shuffle(fakeLotteryBalls);
                Team winner = fakeLotteryBalls.get(rand.nextInt(fakeLotteryBalls.size()));
                ArrayList<Team> newSet = new ArrayList<>();
                if(Objects.equals(winner.name, teamName)){
                    found = true;
                    placements.add(placement);
                }
                placement++;
                for (Team fakeLotteryBall : fakeLotteryBalls) {
                    if (!Objects.equals(fakeLotteryBall.name, winner.name)) {
                        newSet.add(fakeLotteryBall);
                    }
                }
                fakeLotteryBalls = newSet;
            }
        }
        for(int i = 1; i < teams.size()-revealedTeams.size()+1; i++){
            double chance = ((double) Collections.frequency(placements, i) / (double) placements.size()) * 100;
            additionalArea.append("Pick #" + i + ": " + String.format("%.1f", chance) + "%\n");
        }
    }

    private List<Team> loadTeams() {
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(JSON_FILE)))) {
            return gson.fromJson(reader, new TypeToken<List<Team>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveTeams() {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(JSON_FILE)).toURI()))) {
            gson.toJson(teams, writer);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void showTeams() {
        teamListArea.setText("");
        for (Team team : teams) {
            teamListArea.append(team.name + " - " + team.balls + " balls\n");
        }
    }

    private void addTeam(String name, int balls) {
        Team team = new Team(name, balls, 0, 0);
        teams.add(team);
        saveTeams();
        showTeams();
    }

    private void deleteTeam(String name) {
        teams.removeIf(team -> team.name.equals(name));
        saveTeams();
        showTeams();
    }

    public static boolean compareDoubles(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    private void showDivisions() {
        ArrayList<Double> winsArray = new ArrayList<>();
        ArrayList<Integer> takenRanks = new ArrayList<>();
        for (ArrayList<Team> division : divisions) {
            double wins = 0;
            double losses = 0;
            for (Team team : division) {
                wins += team.wins;
                losses += team.losses;
            }
            if (wins + losses > 0)
                winsArray.add((wins / (wins + losses)));
        }
        Collections.sort(winsArray);
        Collections.reverse(winsArray);

        for (int i = 0; i < divisions.size(); i++) {
            int wins = 0, losses = 0;
            divisionAreas[i].setText(""); // clear the text
            for (Team team : divisions.get(i)) {
                String teamName = team.name;
                if (showPercentages) {
                    double combinedWinPercentage = getCombinedWinPercentage(divisions.get(i), team);
                    if (combinedWinPercentage > 0) {
                        teamName += " (" + combinedWinPercentage + "%)";
                    }
                }
                divisionAreas[i].append(teamName + "\n");
                wins += team.wins;
                losses += team.losses;
            }
            int rank = 0;
            double winPercentage = (double) wins / ((double) wins + (double) losses);
            for (int j = 0; j < winsArray.size(); j++) {
                if (compareDoubles(winsArray.get(j), winPercentage, 0.001) && !takenRanks.contains(j)) {
                    rank = j;
                    takenRanks.add(rank);
                    break;
                }
            }
            winPercentage = ((double) wins / ((double) wins + (double) losses)) * 100;
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            String formattedResult = decimalFormat.format(winPercentage);
            if(showPercentages) {
                if (wins + losses > 0)
                    divisionLabels[i].setText("Division " + (rank + 1) + " (" + formattedResult + "%)");
                else
                    divisionLabels[i].setText("Division X");
            }
            else{
                if (wins + losses > 0)
                    divisionLabels[i].setText("Division " + (rank + 1));
                else
                    divisionLabels[i].setText("Division X");
            }
        }
    }

    private double getCombinedWinPercentage(ArrayList<Team> division, Team excludeTeam) {
        int wins = 0;
        int losses = 0;
        for (Team team : division) {
            if (team != excludeTeam) { // Exclude the current team
                wins += team.wins;
                losses += team.losses;
            }
        }
        if (wins + losses > 0) {
            double winPercentage = (double) wins / ((double) wins + (double) losses) * 100;
            return Double.parseDouble(new DecimalFormat("#.#").format(winPercentage));
        } else {
            return 0.0;
        }
    }

    private void performLottery() {
        performLotteryButton.setEnabled(false);
        divisions = new ArrayList<>();
        int amount;
        if (teams.size() == 20)
            amount = 5;
        else if (teams.size() == 18)
            amount = 6;
        else
            amount = 4;
        for (int i = 0; i < amount; i++) {
            divisions.add(new ArrayList<>());
        }

        recreateLotteryBalls();
        updatePercentChance();
        resultArea.setText("");
        currentPick = teams.size();
        draftPicks = new ArrayList<>();

        Random rand = new Random();
        while (!lotteryBalls.isEmpty()) {
            Collections.shuffle(lotteryBalls);
            Team winner = lotteryBalls.get(rand.nextInt(lotteryBalls.size()));
            draftPicks.add(winner);
            ArrayList<Team> newSet = new ArrayList<>();
            for (Team lotteryBall : lotteryBalls) {
                if (!Objects.equals(lotteryBall.name, winner.name)) {
                    newSet.add(lotteryBall);
                }
            }
            lotteryBalls = newSet;
        }
        nextPickButton.setEnabled(true);

        // Print results to console
        for (int i = 0; i < teams.size(); i++)
            System.out.println((i + 1) + ". " + draftPicks.get(i).name);
    }

    private void generateSchedule() {

        int numWeeks = 14;
        int numTeamsPerDivision = teams.size() / divisions.size();
        int[][] teamSchedules = new int[teams.size()][14];

        if(teams.size() == 20) {
            teamSchedules = new int[][]{
                    {8, 17, 6, 20, 4, 19, 3, 2, 7, 18, 3, 2, 5, 4},
                    {19, 7, 4, 8, 5, 3, 20, 1, 6, 17, 4, 1, 3, 18},
                    {18, 8, 20, 6, 7, 2, 1, 4, 17, 5, 1, 4, 2, 19},
                    {17, 18, 2, 19, 1, 8, 5, 3, 20, 7, 2, 3, 6, 1},
                    {6, 9, 7, 11, 2, 10, 4, 8, 12, 3, 6, 7, 1, 8},
                    {5, 11, 1, 3, 8, 7, 12, 9, 2, 10, 5, 8, 4, 7},
                    {11, 2, 5, 12, 3, 6, 8, 10, 1, 4, 8, 5, 9, 6},
                    {1, 3, 11, 2, 6, 4, 7, 5, 9, 12, 7, 6, 10, 5},
                    {12, 5, 10, 15, 11, 14, 16, 6, 8, 13, 11, 10, 7, 12},
                    {15, 16, 9, 13, 12, 5, 11, 7, 14, 6, 12, 9, 8, 11},
                    {7, 6, 8, 5, 9, 12, 10, 14, 13, 16, 9, 12, 15, 10},
                    {9, 15, 16, 7, 10, 11, 6, 13, 5, 8, 10, 11, 14, 9},
                    {20, 14, 15, 10, 17, 16, 15, 12, 11, 9, 16, 19, 18, 14},
                    {16, 13, 18, 16, 15, 9, 19, 11, 10, 20, 17, 15, 12, 13},
                    {10, 12, 13, 9, 14, 17, 13, 16, 18, 19, 20, 14, 11, 16},
                    {14, 10, 12, 14, 18, 13, 9, 15, 19, 11, 13, 17, 20, 15},
                    {4, 1, 19, 18, 13, 15, 18, 20, 3, 2, 14, 16, 19, 20},
                    {3, 4, 14, 17, 16, 20, 17, 19, 15, 1, 19, 20, 13, 2},
                    {2, 20, 17, 4, 20, 1, 14, 18, 16, 15, 18, 13, 17, 3},
                    {13, 19, 3, 1, 19, 18, 2, 17, 4, 14, 15, 18, 16, 17}
            };
        }
        else if(teams.size() == 18){
            teamSchedules = new int[][]{
                    {15, 6, 8, 10, 13, 2, 3, 5, 7, 9, 14, 3, 2, 4},
                    {6, 9, 15, 8, 18, 1, 13, 14, 5, 4, 3, 7, 1, 3},
                    {4, 13, 7, 15, 6, 5, 1, 12, 8, 14, 2, 1, 9, 2},
                    {3, 5, 12, 9, 11, 6, 16, 7, 10, 2, 8, 6, 5, 1},
                    {9, 4, 11, 6, 15, 3, 7, 1, 2, 10, 6, 8, 4, 12},
                    {2, 1, 9, 5, 3, 4, 10, 8, 12, 11, 5, 4, 17, 7},
                    {8, 16, 3, 13, 8, 9, 5, 4, 1, 17, 9, 2, 18, 6},
                    {7, 17, 1, 2, 7, 16, 9, 6, 3, 18, 4, 5, 11, 9},
                    {5, 2, 6, 4, 16, 7, 8, 18, 14, 1, 7, 17, 3, 8},
                    {14, 18, 17, 1, 12, 13, 6, 16, 4, 5, 11, 15, 12, 11},
                    {12, 15, 5, 18, 4, 17, 14, 13, 16, 6, 10, 12, 8, 10},
                    {11, 14, 4, 17, 10, 18, 15, 3, 6, 13, 16, 11, 10, 5},
                    {18, 3, 14, 7, 1, 10, 2, 11, 17, 12, 15, 14, 16, 15},
                    {10, 12, 13, 16, 17, 15, 11, 2, 9, 3, 1, 13, 15, 18},
                    {1, 11, 2, 3, 5, 14, 12, 17, 18, 16, 13, 10, 14, 13},
                    {17, 7, 18, 14, 9, 8, 4, 10, 11, 15, 12, 18, 13, 17},
                    {16, 8, 10, 12, 14, 11, 18, 15, 13, 7, 18, 9, 6, 16},
                    {13, 10, 16, 11, 2, 12, 17, 9, 15, 8, 17, 16, 7, 14}
            };
        }
        else if(teams.size() == 16){
            teamSchedules = new int[][]{
                    {11, 3, 10, 2, 4, 12, 5, 8, 7, 6, 3, 4, 9, 2},
                    {9, 11, 4, 1, 12, 10, 3, 5, 8, 7, 4, 3, 6, 1},
                    {12, 1, 6, 4, 7, 5, 2, 4, 9, 8, 1, 2, 11, 10},
                    {8, 12, 2, 3, 1, 11, 10, 3, 6, 5, 2, 1, 7, 9},
                    {7, 16, 8, 6, 13, 3, 1, 2, 15, 4, 7, 8, 14, 6},
                    {14, 15, 3, 5, 16, 7, 8, 13, 4, 1, 8, 7, 2, 5},
                    {5, 13, 15, 8, 3, 6, 16, 14, 1, 2, 5, 6, 4, 8},
                    {4, 14, 5, 7, 15, 13, 6, 1, 2, 3, 6, 5, 16, 7},
                    {2, 10, 12, 11, 14, 15, 12, 10, 3, 16, 11, 13, 1, 4},
                    {16, 9, 1, 12, 11, 2, 4, 9, 14, 13, 15, 11, 12, 3},
                    {1, 2, 16, 9, 10, 4, 15, 12, 13, 14, 9, 10, 3, 12},
                    {3, 4, 9, 10, 2, 1, 9, 11, 16, 15, 13, 14, 10, 11},
                    {15, 7, 14, 16, 5, 8, 14, 6, 11, 10, 12, 9, 15, 16},
                    {6, 8, 13, 15, 9, 16, 13, 7, 10, 11, 16, 12, 5, 15},
                    {13, 6, 7, 14, 8, 9, 11, 16, 5, 12, 10, 16, 13, 14},
                    {10, 5, 11, 13, 6, 14, 7, 15, 12, 9, 14, 15, 8, 13}
            };
        }

        ArrayList<Team> sortedTeams = new ArrayList<>();
        ArrayList<ArrayList<Team>> originalDivisionOrder = new ArrayList<>();
        for(ArrayList<Team> division : divisions) {
            ArrayList<Team> newList = new ArrayList<>();
            for(Team team : division){
                newList.add(new Team(team.name,team.balls,team.wins,team.losses));
            }
            originalDivisionOrder.add(newList);
        }

        Collections.shuffle(divisions);
        for (ArrayList<Team> division : divisions) Collections.shuffle(division);

        // Set schedules for all teams
        for(int i = 0; i < divisions.size(); i++){
            for(int j = 0; j < divisions.get(i).size(); j++){
                divisions.get(i).get(j).setSchedule(teamSchedules[(i * numTeamsPerDivision) + j]);
                sortedTeams.add(divisions.get(i).get(j));
            }
        }

        for (int i = 0; i < numWeeks; i++) {
            Week week = new Week(i+1);
            // Loop through all the teams
            for (Team sortedTeam : sortedTeams) {
                boolean shouldAdd = true;
                for (int k = 0; k < week.getMatchups().size(); k++) {
                    if (week.getMatchups().get(k).contains(sortedTeam.name)) {
                        shouldAdd = false;
                        break;
                    }
                }
                if (shouldAdd)
                    week.addMatchup(sortedTeam.name + " vs. " + sortedTeams.get(sortedTeam.schedule[i] - 1).name);
            }
            Collections.shuffle(week.getMatchups());
            weeks.add(week);
        }
        divisions = originalDivisionOrder;
    }

    private void updatePercentChance() {
        additionalArea.setText("");
        Collections.reverse(teams);
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Team team : teams) {
            if (!revealedTeams.contains(team)) {
                model.addElement(team.name);
            }
        }
        percentChanceList.setModel(model);
        Collections.reverse(teams);
    }
    private void showNextPick() {
        if (currentPick > 0) {
            currentPick--;
            Team pickedTeam = draftPicks.get(currentPick);
            resultArea.setText("Pick #" + (currentPick + 1) + ": " + pickedTeam.name + "\n" + resultArea.getText());
            resultArea.setCaretPosition(0);
            additionalArea.setCaretPosition(0);
            revealedTeams.add(pickedTeam);

            if(teams.size() == 18){
                if(currentPick == 17 || currentPick == 6 || currentPick == 5)
                    divisions.get(5).add(pickedTeam);
                else if(currentPick == 16 || currentPick == 7 || currentPick == 4)
                    divisions.get(4).add(pickedTeam);
                else if(currentPick == 15 || currentPick == 8 || currentPick == 3)
                    divisions.get(3).add(pickedTeam);
                else if(currentPick == 14 || currentPick == 9 || currentPick == 2)
                    divisions.get(2).add(pickedTeam);
                else if(currentPick == 13 || currentPick == 10 || currentPick == 1)
                    divisions.get(1).add(pickedTeam);
                else
                    divisions.get(0).add(pickedTeam);
            }
            else if(teams.size() == 16){
                if(currentPick == 15 || currentPick == 8 || currentPick == 7 || currentPick == 0)
                    divisions.get(3).add(pickedTeam);
                else if(currentPick == 14 || currentPick == 9 || currentPick == 6 || currentPick == 1)
                    divisions.get(2).add(pickedTeam);
                else if(currentPick == 13 || currentPick == 10 || currentPick == 5 || currentPick == 2)
                    divisions.get(1).add(pickedTeam);
                else
                    divisions.get(0).add(pickedTeam);
            }
            else if(teams.size() == 20){
                if(currentPick == 15 || currentPick == 14 || currentPick == 5 || currentPick == 4)
                    divisions.get(4).add(pickedTeam);
                else if(currentPick == 16 || currentPick == 13 || currentPick == 6 || currentPick == 3)
                    divisions.get(3).add(pickedTeam);
                else if(currentPick == 17 || currentPick == 12 || currentPick == 7 || currentPick == 2)
                    divisions.get(2).add(pickedTeam);
                else if(currentPick == 18 || currentPick == 11 || currentPick == 8 || currentPick == 1)
                    divisions.get(1).add(pickedTeam);
                else
                    divisions.get(0).add(pickedTeam);
            }
            updatePercentChance();
            showDivisions();
        }
        if (currentPick == 0) {
            nextPickButton.setEnabled(false);
            viewScheduleButton.setEnabled(true);
            generateSchedule();
            //scheduleScreen = new ScheduleScreen(weeks,divisions);
        }
    }
    private void recreateLotteryBalls() {
        lotteryBalls = new ArrayList<>();
        for (Team team : teams) {
            for (int i = 0; i < team.balls; i++) {
                lotteryBalls.add(team);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LotterySystem();
            } catch (IOException | FontFormatException e) {
                e.printStackTrace();
            }
        });
    }

}
