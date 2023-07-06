import java.util.ArrayList;

public class Week {
    private ArrayList<String> matchups;

    public ArrayList<String> getMatchups() {
        return matchups;
    }

    public void setMatchups(ArrayList<String> matchups) {
        this.matchups = matchups;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private int number;

    public Week(int number){
        this.number = number;
        this.matchups = new ArrayList<>();
    }

    public void addMatchup(String matchup){
        this.matchups.add(matchup);
    }
}
