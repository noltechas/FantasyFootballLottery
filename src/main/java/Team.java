class Team {
    String name;
    int balls;
    int wins;
    int losses;
    int[] schedule;

    Team(String name, int balls, int wins, int losses) {
        this.name = name;
        this.balls = balls;
        this.wins = wins;
        this.losses = losses;
    }

    public void setSchedule(int[] schedule){
        this.schedule = schedule;
    }
}